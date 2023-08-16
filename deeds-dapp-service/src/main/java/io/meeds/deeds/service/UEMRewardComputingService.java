/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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
package io.meeds.deeds.service;

import static io.meeds.deeds.api.constant.HubReportStatusType.REJECTED;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.meeds.deeds.api.constant.UEMRewardStatusType;
import io.meeds.deeds.api.model.HubReport;
import io.meeds.deeds.api.model.UEMReward;
import io.meeds.deeds.common.model.RewardPeriod;
import io.meeds.deeds.common.service.BlockchainService;
import io.meeds.deeds.common.service.HubReportService;
import io.meeds.deeds.common.service.ListenerService;
import io.meeds.deeds.common.service.UEMConfigurationService;
import io.meeds.deeds.common.service.UEMRewardService;

@Component
public class UEMRewardComputingService {

  public static final String               UEM_HUB_REPORT_REWARD_COMPUTED = "uem.HubReportReward.computed";

  private static final UEMRewardStatusType INITIAL_REWARD_STATUS          = UEMRewardStatusType.NONE;

  private static final String              PERIOD_TYPE                    = "WEEK";

  private static final String              UEM_REWARD_COMPUTED            = "uem.Reward.computed";

  @Autowired
  private UEMConfigurationService          uemConfigurationService;

  @Autowired
  private UEMRewardService                 rewardService;

  @Autowired
  private HubReportService                 reportService;

  @Autowired
  private BlockchainService                blockchainService;

  @Autowired
  private HubRewardComputingService        hubRewardComputingService;

  @Autowired
  private ListenerService                  listenerService;

  public void computePendingRewards() {
    rewardService.getPendingRewards()
                 .map(r -> new RewardPeriod(r.getFromDate(), r.getToDate()))
                 .forEach(this::computeUEMReward);
  }

  public UEMReward computeUEMReward(String rewardId) {
    UEMReward existingReward = rewardService.getRewardById(rewardId);
    if (existingReward != null) {
      return computeUEMReward(new RewardPeriod(existingReward.getFromDate(),
                                               existingReward.getToDate()),
                              existingReward);
    } else {
      return null;
    }
  }

  public UEMReward computeUEMReward(Instant instant) {
    LocalDate date = RewardPeriod.getLocalDate(instant);
    RewardPeriod period = RewardPeriod.getPeriod(date);
    return computeUEMReward(period);
  }

  public UEMReward computeUEMReward(RewardPeriod period) {
    UEMReward existingReward = rewardService.getReward(period);
    return computeUEMReward(period, existingReward);
  }

  private UEMReward computeUEMReward(RewardPeriod period, UEMReward rewardToRefresh) {
    if (rewardToRefresh != null && StringUtils.isNotBlank(rewardToRefresh.getHash())) {
      // Reward already sent to WoM and no re-computing is allowed since the
      // hash is already generated and the data can't be altered
      return rewardToRefresh;
    }

    List<HubReport> reports = filterHubReports(period);
    if (CollectionUtils.isEmpty(reports)) {
      // When no reports, no need to generate an UEM reward for period
      return null;
    }

    UEMReward reward;
    if (rewardToRefresh == null) {
      reward = new UEMReward();
      reward.setPeriodType(PERIOD_TYPE);
      reward.setCreatedDate(Instant.now());
      reward.setFromDate(period.getFrom());
      reward.setToDate(period.getTo());
      reward.setStatus(INITIAL_REWARD_STATUS);
    } else if (StringUtils.isNotBlank(rewardToRefresh.getHash())) {
      return rewardToRefresh;
    } else {
      reward = rewardToRefresh;
    }

    reward.setHash(null);
    reward.setPreviousHash(null);
    reward.setHubAddress(reports.stream()
                                .map(HubReport::getHubAddress)
                                .collect(Collectors.toSet()));
    reward.setHubRewardsAmount(reports.stream()
                                      .map(HubReport::getHubRewardAmount)
                                      .map(BigDecimal::valueOf)
                                      .reduce(BigDecimal.ZERO, BigDecimal::add)
                                      .doubleValue());
    reward.setReportHash(reports.stream()
                                .map(HubReport::getHash)
                                .collect(Collectors.toSet()));
    reward.setHubAchievementsCount(reports.stream()
                                          .map(HubReport::getAchievementsCount)
                                          .reduce(0l, Long::sum));
    reward.setGlobalEngagementRate(reports.stream()
                                          .map(HubReport::getEd)
                                          .map(BigDecimal::valueOf)
                                          .reduce(BigDecimal.ZERO, BigDecimal::add)
                                          .doubleValue());
    reward.setTokenAddress(blockchainService.getEthereumMeedTokenAddress());
    try {
      reward.setTokenNetworkId(blockchainService.getNetworkId());
    } catch (IOException e) {
      throw new IllegalStateException("Error retrieving Blockchain network identifier", e);
    }
    reward.setUemRewardAmount(uemConfigurationService.getUemRewardAmount());
    reports = reports.stream()
                     .map(HubReport::getHash)
                     .map(hash -> hubRewardComputingService.computeUEMHubReward(hash, reward))
                     .toList();
    reward.setUemRewardIndex(reports.stream()
                                    .map(HubReport::getUemRewardIndex)
                                    .map(BigDecimal::valueOf)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                                    .doubleValue());
    UEMReward savedReward = rewardService.saveReward(reward);
    reports.stream()
           .forEach(r -> {
             setUemRewardAmount(reward, r);
             reportService.saveReportUEMProperties(r);
             listenerService.publishEvent(UEM_HUB_REPORT_REWARD_COMPUTED, r.getHash());
           });
    listenerService.publishEvent(UEM_REWARD_COMPUTED, savedReward.getId());
    return savedReward;
  }

  private void setUemRewardAmount(UEMReward reward, HubReport report) {
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

  private List<HubReport> filterHubReports(RewardPeriod rewardPeriod) {
    List<HubReport> reports = reportService.getValidReports(rewardPeriod);
    return reports.stream()
                  .map(HubReport::getHubAddress)
                  .map(StringUtils::lowerCase)
                  .distinct()
                  .map(hubAddress -> computeHubReport(hubAddress, reports))
                  .filter(Objects::nonNull)
                  .toList();
  }

  private HubReport computeHubReport(String hubAddress, List<HubReport> reports) {
    List<HubReport> hubReports = reports.stream()
                                        .filter(r -> StringUtils.equalsIgnoreCase(r.getHubAddress(), hubAddress))
                                        .toList();

    if (hubReports.size() == 1) {
      return hubReports.get(0);
    } else {
      // Get last created report
      HubReport validReport = hubReports.stream()
                                        .sorted((r1, r2) -> r2.getSentDate().compareTo(r1.getSentDate()))
                                        .findFirst()
                                        .orElse(null);
      // Invalidate all other reports
      hubReports.stream()
                .filter(r -> !StringUtils.equalsIgnoreCase(r.getHash(), validReport.getHash()))
                .map(HubReport::getHash)
                .forEach(hash -> reportService.saveReportStatus(hash, REJECTED));
      return validReport;
    }
  }

}
