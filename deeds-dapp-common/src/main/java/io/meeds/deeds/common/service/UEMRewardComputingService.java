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
package io.meeds.deeds.common.service;

import static io.meeds.deeds.api.constant.HubReportStatusType.REJECTED;
import static io.meeds.deeds.api.constant.HubReportStatusType.REWARDED;
import static io.meeds.deeds.api.constant.HubReportStatusType.REWARD_TRANSACTION_ERROR;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.web3j.crypto.Hash;

import io.meeds.deeds.api.constant.HubReportStatusType;
import io.meeds.deeds.api.constant.UEMRewardStatusType;
import io.meeds.deeds.api.model.HubReport;
import io.meeds.deeds.api.model.UEMReward;
import io.meeds.deeds.common.model.RewardPeriod;

@Component
public class UEMRewardComputingService {

  private static final Logger              LOG                            =
                                               LoggerFactory.getLogger(UEMRewardComputingService.class);

  public static final String               UEM_HUB_REPORT_REWARD_COMPUTED = "uem.HubReportReward.computed";

  public static final String               UEM_REWARD_COMPUTED            = "uem.Reward.computed";

  public static final String               UEM_REWARD_PERIOD_TYPE         = "WEEK";

  private static final UEMRewardStatusType INITIAL_REWARD_STATUS          = UEMRewardStatusType.NONE;

  @Autowired
  private UEMConfigurationService          uemConfigurationService;

  @Autowired
  private UEMRewardService                 rewardService;

  @Autowired
  private HubReportService                 reportService;

  @Autowired
  private BlockchainService                blockchainService;

  @Autowired
  private HubReportComputingService        hubRewardComputingService;

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

  public void saveRewardTransactionHash(String rewardId, Set<String> reportHashes, String transactionHash) {
    rewardService.saveRewardTransactionHash(rewardId, transactionHash);
    reportHashes.forEach(reportHash -> reportService.saveReportTransactionHash(reportHash, transactionHash));
  }

  public void saveRewardTransactionStatus(String rewardId, Map<String, Boolean> minedTransactions) {
    UEMReward reward = rewardService.getRewardById(rewardId);
    Set<String> pendingTransactions = reward.getTransactionHashes();
    reward.getReportHashes().forEach(reportHash -> saveReportTransactionStatus(reportHash, minedTransactions));
    boolean isCompletelyMined = pendingTransactions.size() == minedTransactions.size();
    boolean isCompletelyConfirmed = minedTransactions.values().stream().allMatch(Boolean::booleanValue);
    if (isCompletelyMined) {
      UEMRewardStatusType rewardStatus = isCompletelyConfirmed ? UEMRewardStatusType.REWARDED
                                                               : UEMRewardStatusType.REWARD_TRANSACTION_ERROR;
      if (rewardStatus != reward.getStatus()) {
        rewardService.saveRewardStatus(reward.getId(), rewardStatus);
      }
    }
  }

  private void saveReportTransactionStatus(String reportHash, Map<String, Boolean> minedTransactions) {
    HubReport report = reportService.getReport(reportHash);
    if (StringUtils.isNotBlank(report.getRewardTransactionHash())) {
      Boolean confirmed = minedTransactions.get(report.getRewardTransactionHash());
      if (confirmed == null) {
        LOG.warn("Report {} seems to have a transaction that is not referenced in its reward {} transactions list",
                 reportHash,
                 report.getRewardId());
      } else {
        HubReportStatusType reportStatus = confirmed.booleanValue() ? REWARDED : REWARD_TRANSACTION_ERROR;
        if (report.getStatus() != reportStatus) {
          reportService.saveReportStatus(reportHash, reportStatus);
        }
      }
    }
  }

  private UEMReward computeUEMReward(RewardPeriod period, UEMReward rewardToRefresh) {
    if (rewardToRefresh != null
        && !CollectionUtils.isEmpty(rewardToRefresh.getTransactionHashes())) {
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
      reward.setPeriodType(UEM_REWARD_PERIOD_TYPE);
      reward.setCreatedDate(Instant.now());
      reward.setFromDate(period.getFrom());
      reward.setToDate(period.getTo());
      reward.setStatus(INITIAL_REWARD_STATUS);
    } else {
      reward = rewardToRefresh;
    }

    Set<String> hubAddresses = hubAddresses(reports);
    double hubRewardsAmount = hubRewardsAmount(reports);
    Set<String> reportHashes = reportHashes(reports);
    Long hubAchievementsCount = hubAchievementsCount(reports);
    double globalEngagementRate = globalEngagementRate(reports);
    double uemRewardAmount = getUemRewardAmount();

    reward.setHubAddresses(hubAddresses);
    reward.setHubRewardsAmount(hubRewardsAmount);
    reward.setReportHashes(reportHashes);
    reward.setHubAchievementsCount(hubAchievementsCount);
    reward.setGlobalEngagementRate(globalEngagementRate);
    reward.setUemRewardAmount(uemRewardAmount);

    // Compute reports indexes
    reports = computeUEMHubRewards(reward, reports);
    double uemRewardIndex = uemRewardIndex(reports);
    reward.setUemRewardIndex(uemRewardIndex);
    TreeMap<String, Double> reportRewards = reportRewards(reports);
    reward.setReportRewards(reportRewards);
    String reportsMerkleRoot = merkleRoot(reward);
    reward.setReportsMerkleRoot(reportsMerkleRoot);

    // Use new instance to validate constructor input and to have compilation
    // error when the parameters list changes
    UEMReward savedReward = rewardService.saveReward(new UEMReward(reward.getId(),
                                                                   reward.generateHash(),
                                                                   reportsMerkleRoot,
                                                                   reward.getFromDate(),
                                                                   reward.getToDate(),
                                                                   UEM_REWARD_PERIOD_TYPE,
                                                                   hubAddresses,
                                                                   reportHashes,
                                                                   reward.getTransactionHashes(),
                                                                   reportRewards,
                                                                   hubAchievementsCount,
                                                                   hubRewardsAmount,
                                                                   uemRewardIndex,
                                                                   uemRewardAmount,
                                                                   getTokenNetworkId(),
                                                                   getTokenAddress(),
                                                                   globalEngagementRate,
                                                                   reward.getStatus(),
                                                                   reward.getCreatedDate()));
    reports.stream()
           .forEach(r -> {
             setUemRewardAmount(savedReward, r);
             reportService.saveReportUEMProperties(r);
             listenerService.publishEvent(UEM_HUB_REPORT_REWARD_COMPUTED, r.getHash());
           });
    listenerService.publishEvent(UEM_REWARD_COMPUTED, savedReward.getId());
    return savedReward;
  }

  private TreeMap<String, Double> reportRewards(List<HubReport> reports) {
    return reports.stream()
                  .collect(Collectors.toMap(HubReport::getHash,
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
                     .map(HubReport::getHash)
                     .map(hash -> hubRewardComputingService.computeUEMHubReward(hash, reward))
                     .toList();
    return reports;
  }

  private double getUemRewardAmount() {
    return uemConfigurationService.getUemRewardAmount();
  }

  private long getTokenNetworkId() {
    try {
      return blockchainService.getPolygonNetworkId();
    } catch (IOException e) {
      throw new IllegalStateException("Error retrieving Blockchain network identifier", e);
    }
  }

  private String getTokenAddress() {
    return blockchainService.getPolygonMeedTokenAddress();
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

  private Set<String> reportHashes(List<HubReport> reports) {
    return reports.stream()
                  .map(HubReport::getHash)
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

  private String merkleRoot(UEMReward reward) {
    return merkleRoot(reward.getReportRewards()
                            .keySet()
                            .stream()
                            .sorted(String::compareToIgnoreCase)
                            .toList());
  }

  private String merkleRoot(List<String> reportHashList) {
    return StringUtils.lowerCase(merkleTree(reportHashList).get(0));
  }

  private List<String> merkleTree(List<String> reportHashList) {
    if (reportHashList.size() == 1) {
      return reportHashList;
    }
    List<String> parentHashes = new ArrayList<>();
    for (int i = 0; i < reportHashList.size(); i += 2) {
      String hashedString = Hash.sha3(reportHashList.get(i) + reportHashList.get(i + 1));
      parentHashes.add(hashedString);
    }
    if (reportHashList.size() % 2 == 1) {
      String lastHash = reportHashList.get(reportHashList.size() - 1);
      String hashedString = Hash.sha3(lastHash + lastHash);
      parentHashes.add(hashedString);
    }
    return merkleTree(parentHashes);
  }

}
