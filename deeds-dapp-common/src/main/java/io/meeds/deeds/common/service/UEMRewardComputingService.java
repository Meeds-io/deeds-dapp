/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.deeds.common.service;

import static io.meeds.deeds.common.service.UEMRewardService.UEM_REWARD_COMPUTED;
import static io.meeds.wom.api.constant.HubReportStatusType.REJECTED;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.meeds.deeds.common.model.RewardPeriod;
import io.meeds.wom.api.model.HubReport;
import io.meeds.wom.api.model.UEMReward;

@Component
public class UEMRewardComputingService {

  @Autowired
  private UEMConfigurationService   uemConfigurationService;

  @Autowired
  private UEMRewardService          rewardService;

  @Autowired
  private HubReportService          reportService;

  @Autowired
  private BlockchainService         blockchainService;

  @Autowired
  private HubReportComputingService hubRewardComputingService;

  @Autowired
  private ListenerService           listenerService;

  public UEMReward computeUEMReward(long rewardId) {
    List<HubReport> reports = reportService.getReportsByRewardId(rewardId);
    if (CollectionUtils.isEmpty(reports)) {
      // When no reports, no need to generate an UEM reward for period
      return null;
    }
    UEMReward reward = rewardService.getRewardById(rewardId);
    if (reward == null) {
      reward = blockchainService.getUEMReward();
    }

    Set<String> hubAddresses = hubAddresses(reports);
    double hubRewardsAmount = hubRewardsAmount(reports);
    Set<Long> reportIds = reportIds(reports);
    Long hubAchievementsCount = hubAchievementsCount(reports);
    double globalEngagementRate = globalEngagementRate(reports);
    double uemRewardAmount = getUemRewardAmount();

    /// 1. Set Reward data from reports
    reward.setHubAddresses(hubAddresses);
    reward.setHubRewardsAmount(hubRewardsAmount);
    reward.setReportIds(reportIds);
    reward.setHubAchievementsCount(hubAchievementsCount);
    reward.setGlobalEngagementRate(globalEngagementRate);
    reward.setUemRewardAmount(uemRewardAmount);

    // 2. Compute reports indexes
    reports = computeUEMHubRewards(reward, reports);

    // 3. Set Reward properties from report
    double uemRewardIndex = uemRewardIndex(reports);
    TreeMap<Long, Double> reportRewards = reportRewards(reports);

    reward.setUemRewardIndex(uemRewardIndex);
    reward.setReportRewards(reportRewards);

    // Use new instance to validate constructor input and to have compilation
    // error when the parameters list changes
    UEMReward uemReward = new UEMReward(reward.getRewardId(),
                                        reward.getFromDate(),
                                        reward.getToDate(),
                                        uemRewardAmount,
                                        reportIds,
                                        reportRewards,
                                        hubAddresses,
                                        hubAchievementsCount,
                                        hubRewardsAmount,
                                        uemRewardIndex,
                                        globalEngagementRate);
    UEMReward savedReward = rewardService.saveReward(uemReward);

    // 4. Update each report prorata reward amount
    reports.stream()
           .forEach(r -> {
             setUemRewardProperties(savedReward, r);
             reportService.saveReportUEMProperties(r);
           });
    listenerService.publishEvent(UEM_REWARD_COMPUTED, savedReward.getRewardId());
    return savedReward;
  }

  private TreeMap<Long, Double> reportRewards(List<HubReport> reports) {
    return reports.stream()
                  .collect(Collectors.toMap(HubReport::getReportId,
                                            HubReport::getUemRewardAmount,
                                            (v1, v2) -> {
                                              throw new IllegalStateException(String.format("Duplicate key for values %s and %s",
                                                                                            v1,
                                                                                            v2));
                                            },
                                            TreeMap::new));
  }

  private double uemRewardIndex(List<HubReport> reports) {
    return reports.stream()
                  .map(HubReport::getUemRewardIndex)
                  .map(BigDecimal::valueOf)
                  .reduce(BigDecimal.ZERO, BigDecimal::add)
                  .doubleValue();
  }

  private List<HubReport> computeUEMHubRewards(UEMReward reward, List<HubReport> reports) {
    reports = reports.stream()
                     .map(HubReport::getReportId)
                     .map(reportId -> hubRewardComputingService.computeUEMHubReward(reportId, reward))
                     .toList();
    return reports;
  }

  private double getUemRewardAmount() {
    return uemConfigurationService.getConfiguration().getUemRewardAmount();
  }

  private double globalEngagementRate(List<HubReport> reports) {
    return reports.stream()
                  .map(HubReport::getEd)
                  .map(BigDecimal::valueOf)
                  .reduce(BigDecimal.ZERO, BigDecimal::add)
                  .doubleValue();
  }

  private Long hubAchievementsCount(List<HubReport> reports) {
    return reports.stream()
                  .map(HubReport::getAchievementsCount)
                  .reduce(0l, Long::sum);
  }

  private Set<Long> reportIds(List<HubReport> reports) {
    return reports.stream()
                  .map(HubReport::getReportId)
                  .collect(Collectors.toSet());
  }

  private double hubRewardsAmount(List<HubReport> reports) {
    return reports.stream()
                  .map(HubReport::getHubRewardAmount)
                  .map(BigDecimal::valueOf)
                  .reduce(BigDecimal.ZERO, BigDecimal::add)
                  .doubleValue();
  }

  private Set<String> hubAddresses(List<HubReport> reports) {
    return reports.stream()
                  .map(HubReport::getHubAddress)
                  .collect(Collectors.toSet());
  }

  private void setUemRewardProperties(UEMReward reward, HubReport report) {
    report.setRewardId(reward.getRewardId());
    if (reward.getUemRewardIndex() > 0) {
      report.setUemRewardAmount(BigDecimal.valueOf(report.getUemRewardIndex())
                                          .multiply(BigDecimal.valueOf(reward.getUemRewardAmount()))
                                          .divide(BigDecimal.valueOf(reward.getUemRewardIndex()),
                                                  MathContext.DECIMAL128)
                                          .doubleValue());
    } else {
      report.setUemRewardAmount(0d);
    }
  }

}
