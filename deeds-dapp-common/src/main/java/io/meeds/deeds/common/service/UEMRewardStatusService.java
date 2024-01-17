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

import static io.meeds.wom.api.constant.HubReportStatusType.PENDING_REWARD;
import static io.meeds.wom.api.constant.HubReportStatusType.REWARDED;
import static io.meeds.wom.api.constant.HubReportStatusType.REWARD_TRANSACTION_ERROR;
import static io.meeds.wom.api.constant.HubReportStatusType.SENT;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import io.meeds.wom.api.constant.HubReportStatusType;
import io.meeds.wom.api.constant.UEMRewardStatusType;
import io.meeds.wom.api.model.HubReport;

@Component
public class UEMRewardStatusService {

  @Autowired
  private UEMRewardService  rewardService;

  @Autowired
  private HubReportService  reportService;

  @Autowired
  private BlockchainService blockchainService;

  public void saveRewardTransactionHash(String rewardId, Set<String> reportHashes, String transactionHash) {
    rewardService.saveRewardTransactionHash(rewardId, transactionHash);
    reportHashes.forEach(reportHash -> reportService.saveReportTransactionHash(reportHash, transactionHash));
  }

  public void refreshRewardTransactionStatus(String rewardId) {
    List<HubReport> reports = reportService.getReportsByReward(rewardId, Pageable.unpaged()).getContent();
    List<String> pendingTransactions = reports.stream()
                                              .map(HubReport::getRewardTransactionHash)
                                              .filter(Objects::nonNull)
                                              .map(StringUtils::lowerCase)
                                              .toList();
    Map<String, Boolean> transactionStatuses = pendingTransactions.stream()
                                                                  .distinct()
                                                                  .filter(blockchainService::isPolygonTransactionMined)
                                                                  .collect(Collectors.toMap(Function.identity(),
                                                                                            blockchainService::isPolygonTransactionConfirmed));

    reports = reports.stream()
                     .map(report -> saveReportTransactionStatus(report, transactionStatuses))
                     .toList();
    if (isCompletelyRewarded(reports)) {
      rewardService.saveRewardStatus(rewardId, UEMRewardStatusType.REWARDED);
    } else if (hasTransactionError(reports)) {
      rewardService.saveRewardStatus(rewardId, UEMRewardStatusType.REWARD_TRANSACTION_ERROR);
    } else if (hasPendingTransaction(reports)) {
      rewardService.saveRewardStatus(rewardId, UEMRewardStatusType.PENDING_REWARD);
    } else if (hasReportNotRewardedYet(reports) && hasReportRewarded(reports)) {
      rewardService.saveRewardStatus(rewardId, UEMRewardStatusType.PARTIAL_REWARD);
    }
  }

  private boolean hasReportNotRewardedYet(List<HubReport> reports) {
    return reports.stream()
                  .anyMatch(report -> StringUtils.isBlank(report.getRewardTransactionHash())
                      || report.getStatus() == SENT);
  }

  private boolean hasReportRewarded(List<HubReport> reports) {
    return reports.stream()
                  .anyMatch(report -> StringUtils.isNotBlank(report.getRewardTransactionHash())
                      && report.getStatus() == REWARDED);
  }

  private boolean hasPendingTransaction(List<HubReport> reports) {
    return reports.stream()
                  .anyMatch(report -> StringUtils.isNotBlank(report.getRewardTransactionHash())
                      && report.getStatus() == PENDING_REWARD);
  }

  private boolean hasTransactionError(List<HubReport> reports) {
    return reports.stream()
                  .anyMatch(report -> StringUtils.isNotBlank(report.getRewardTransactionHash())
                      && report.getStatus() == REWARD_TRANSACTION_ERROR);
  }

  private boolean isCompletelyRewarded(List<HubReport> reports) {
    return reports.stream()
                  .allMatch(report -> StringUtils.isNotBlank(report.getRewardTransactionHash())
                      && report.getStatus() == REWARDED);
  }

  private HubReport saveReportTransactionStatus(HubReport report, Map<String, Boolean> minedTransactions) {
    if (StringUtils.isNotBlank(report.getRewardTransactionHash())) {
      Boolean confirmed = minedTransactions.get(report.getRewardTransactionHash());
      if (confirmed != null) {
        HubReportStatusType reportStatus = confirmed.booleanValue() ? REWARDED : REWARD_TRANSACTION_ERROR;
        if (report.getStatus() != reportStatus) {
          reportService.saveReportStatus(report.getHash(), reportStatus);
          return reportService.getReport(report.getHash());
        }
      }
    }
    return report;
  }

}
