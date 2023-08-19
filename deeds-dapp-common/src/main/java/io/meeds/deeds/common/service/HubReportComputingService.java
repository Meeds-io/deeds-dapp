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

import static io.meeds.deeds.api.constant.HubReportStatusType.NONE;
import static io.meeds.deeds.api.constant.HubReportStatusType.PENDING_REWARD;
import static io.meeds.deeds.api.constant.HubReportStatusType.REWARDED;
import static io.meeds.deeds.api.constant.HubReportStatusType.SENT;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.meeds.deeds.api.constant.HubReportStatusType;
import io.meeds.deeds.api.constant.ObjectNotFoundException;
import io.meeds.deeds.api.model.HubReport;
import io.meeds.deeds.api.model.UEMReward;
import io.meeds.deeds.common.constant.DeedCard;
import io.meeds.deeds.common.model.RewardPeriod;

@Component
public class HubReportComputingService {

  public static final double                     UEM_REWARD_PERIOD_IN_DAYS = 7d;

  private static final List<HubReportStatusType> VALID_STATUSES            = Stream.of(NONE,
                                                                                       SENT,
                                                                                       PENDING_REWARD,
                                                                                       REWARDED)
                                                                                   .toList();

  @Autowired
  private UEMRewardService                       rewardService;

  @Autowired
  private HubReportService                       reportService;

  @Autowired
  private TenantService                          tenantService;

  protected HubReport computeUEMHubReward(String hash, UEMReward reward) {
    HubReport report = reportService.getReport(hash);
    if (StringUtils.isBlank(report.getRewardId())) {
      report.setRewardId(reward.getId());
    }

    HubReport lastRewardedReport = computeLastPeriodUemRewardAmount(report);
    computeLastPeriodUemRewardAmountPerPeriod(report, lastRewardedReport, reward);
    computeHubRewardAmountPerPeriod(report, lastRewardedReport, reward);
    computeMintingPower(report);

    double ed = report.getEd();
    double ew = reward.getEw();
    double dr = report.getDr();
    double ds = report.getDs();
    double mp = report.getMp();

    double uemRewardIndex = ew == 0 ? 0d
                                    : BigDecimal.valueOf(ed)
                                                .divide(BigDecimal.valueOf(ew), MathContext.DECIMAL128)
                                                .multiply(BigDecimal.valueOf(dr))
                                                .multiply(BigDecimal.valueOf(ds))
                                                .multiply(BigDecimal.valueOf(mp))
                                                .doubleValue();
    report.setUemRewardIndex(uemRewardIndex);
    return report;
  }

  private HubReport computeLastPeriodUemRewardAmount(HubReport report) {
    HubReport lastRewardedReport = reportService.getLastReport(report.getHubAddress(),
                                                               report.getSentDate(),
                                                               report.getHash(),
                                                               VALID_STATUSES);
    report.setLastPeriodUemRewardAmount(lastRewardedReport == null ? 0d : lastRewardedReport.getUemRewardAmount());
    return lastRewardedReport;
  }

  private void computeLastPeriodUemRewardAmountPerPeriod(HubReport report,
                                                         HubReport lastRewardedReport,
                                                         UEMReward currentReward) {
    if (lastRewardedReport == null) {
      // Number of weeks since last period UEM reward sent to Hub
      report.setLastPeriodUemDiff(0);
      report.setLastPeriodUemRewardAmountPerPeriod(0d);
    } else {
      BigDecimal diffWeeks = getDiffWeeks(currentReward, lastRewardedReport);
      report.setLastPeriodUemDiff(diffWeeks.doubleValue());

      double lastPeriodUemRewardAmountPerPeriod = report.getLastPeriodUemDiff() == 0d ? 0d
                                                                                      : BigDecimal.valueOf(lastRewardedReport.getUemRewardAmount())
                                                                                                  .divide(diffWeeks,
                                                                                                          MathContext.DECIMAL128)
                                                                                                  .doubleValue();
      report.setLastPeriodUemRewardAmountPerPeriod(lastPeriodUemRewardAmountPerPeriod);
    }
  }

  private void computeHubRewardAmountPerPeriod(HubReport report,
                                               HubReport lastRewardedReport,
                                               UEMReward currentReward) {
    if (lastRewardedReport == null) {
      // Number of weeks since last period, 1 if no hub rewards
      report.setHubRewardLastPeriodDiff(1d);
      report.setHubRewardAmountPerPeriod(getHubRewardAmountPerPeriod(report));
    } else {
      BigDecimal diffWeeks = getDiffWeeks(currentReward, lastRewardedReport);
      report.setHubRewardLastPeriodDiff(diffWeeks.doubleValue());

      double hubRewardAmountPerPeriod = report.getHubRewardLastPeriodDiff() == 0d ? getHubRewardAmountPerPeriod(report)
                                                                                  : BigDecimal.valueOf(report.getHubRewardAmount())
                                                                                              .divide(diffWeeks,
                                                                                                      MathContext.DECIMAL128)
                                                                                              .doubleValue();
      report.setHubRewardAmountPerPeriod(hubRewardAmountPerPeriod);
    }
  }

  private double getHubRewardAmountPerPeriod(HubReport report) {
    long diffDays = Duration.between(report.getFromDate(),
                                     report.getToDate())
                            .toDays();
    BigDecimal diffWeeks = BigDecimal.valueOf(diffDays)
                                     .divide(BigDecimal.valueOf(UEM_REWARD_PERIOD_IN_DAYS), MathContext.DECIMAL128);
    return diffDays == 0 ? 0d
                         : BigDecimal.valueOf(report.getHubRewardAmount())
                                     .divide(diffWeeks, MathContext.DECIMAL128)
                                     .doubleValue();
  }

  private BigDecimal getDiffWeeks(UEMReward currentReward, HubReport lastRewardedReport) {
    long diffDays = getDiffDays(currentReward, lastRewardedReport);
    return diffDays == 0 ? BigDecimal.ZERO
                         : BigDecimal.valueOf(diffDays)
                                     .divide(BigDecimal.valueOf(UEM_REWARD_PERIOD_IN_DAYS), MathContext.DECIMAL128);
  }

  private long getDiffDays(UEMReward currentReward, HubReport lastRewardedReport) {
    UEMReward lastUemRewarded = rewardService.getReward(RewardPeriod.getPeriod(lastRewardedReport.getSentDate()));
    if (lastUemRewarded == null) {
      return 0;
    }
    return Duration.between(lastUemRewarded.getFromDate(),
                            currentReward.getFromDate())
                   .toDays();
  }

  private void computeMintingPower(HubReport report) {
    try {
      short cardType = tenantService.getCardType(report.getDeedId());
      DeedCard card = DeedCard.values()[cardType];
      report.setMp(card.getMintingPower());
    } catch (ObjectNotFoundException e) {
      throw new IllegalStateException("Error retrieving deed card type with id '#"
          + report.getDeedId()
          + "' from blockchain", e);
    }
  }

}
