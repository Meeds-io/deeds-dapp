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

import static io.meeds.deeds.common.utils.HubReportMapper.toEntity;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import io.meeds.deeds.common.elasticsearch.model.HubReportEntity;
import io.meeds.deeds.common.elasticsearch.model.UemRewardEntity;
import io.meeds.deeds.common.elasticsearch.storage.HubReportRepository;
import io.meeds.deeds.common.elasticsearch.storage.UemRewardRepository;
import io.meeds.deeds.common.utils.HubReportMapper;
import io.meeds.wom.api.constant.WomAuthorizationException;
import io.meeds.wom.api.constant.WomException;
import io.meeds.wom.api.model.HubReport;
import io.meeds.wom.api.model.HubReportVerifiableData;

import lombok.SneakyThrows;

@Component
public class HubReportService {

  public static final String  HUB_REPORT_SAVED          = "uem.report.saved";

  @Autowired
  private BlockchainService   blockchainService;

  @Autowired
  private ListenerService     listenerService;

  @Autowired
  private HubReportRepository reportRepository;

  @Autowired
  private UemRewardRepository rewardRepository;

  /**
   * Used to force compute Engagement Score of last sent report
   * else it will compute the engagement score of the last rewarded
   * report only (ignoring current period reports)
   */
  @Value("${meeds.uem.lastReportEngagementScore:false}")
  private boolean             lastReportEngagementScore = false;

  public Page<HubReport> getReportsByHub(String hubAddress, Pageable pageable) {
    return getReports(hubAddress, 0, pageable);
  }

  public Page<HubReport> getReportsByRewardId(long rewardId, Pageable pageable) {
    return getReports(null, rewardId, pageable);
  }

  public List<HubReport> getReportsByRewardId(long rewardId) {
    return reportRepository.findByRewardId(rewardId)
                           .map(HubReportMapper::fromEntity)
                           .toList();
  }

  public Page<HubReport> getReports(String hubAddress, long rewardId, Pageable pageable) {
    Page<HubReportEntity> page;
    if (StringUtils.isBlank(hubAddress) && rewardId == 0) {
      pageable = pageable.isUnpaged() ? pageable :
                                      PageRequest.of(pageable.getPageNumber(),
                                                     pageable.getPageSize(),
                                                     pageable.getSortOr(Sort.by(Direction.DESC, "fromDate")));
      page = reportRepository.findAll(pageable);
    } else if (rewardId == 0) {
      pageable = pageable.isUnpaged() ? pageable :
                                      PageRequest.of(pageable.getPageNumber(),
                                                     pageable.getPageSize(),
                                                     pageable.getSortOr(Sort.by(Direction.DESC, "sentDate")));
      page = reportRepository.findByHubAddress(StringUtils.lowerCase(hubAddress), pageable);
    } else if (StringUtils.isBlank(hubAddress)) {
      pageable = pageable.isUnpaged() ? pageable :
                                      PageRequest.of(pageable.getPageNumber(),
                                                     pageable.getPageSize(),
                                                     pageable.getSortOr(Sort.by(Direction.DESC, "sentDate")));
      page = reportRepository.findByRewardId(rewardId, pageable);
    } else {
      page = reportRepository.findByRewardIdAndHubAddress(rewardId, StringUtils.lowerCase(hubAddress), pageable);
    }
    return page.map(HubReportMapper::fromEntity);
  }

  public HubReport getReport(long reportId) {
    return reportRepository.findById(reportId)
                           .map(HubReportMapper::fromEntity)
                           .orElseGet(() -> refreshReport(reportId));
  }

  public HubReport getReport(long rewardId, String hubAddress) {
    return reportRepository.findByRewardIdAndHubAddress(rewardId, StringUtils.lowerCase(hubAddress))
                           .map(HubReportMapper::fromEntity)
                           .orElse(null);
  }

  @SneakyThrows
  public HubReport saveReport(HubReportVerifiableData reportData) throws WomException {
    if (!reportData.isValid()) {
      throw new WomAuthorizationException("wom.invalidSignedMessage");
    }
    HubReport report = new HubReport(reportData);
    blockchainService.retrieveReportProperties(report);
    reportRepository.save(toEntity(report));
    listenerService.publishEvent(HUB_REPORT_SAVED, report.getReportId());
    return report;
  }

  public HubReport refreshReport(long reportId) {
    HubReportEntity hubReportEntity = reportRepository.findById(reportId).orElse(null);
    HubReport report = null;
    if (hubReportEntity != null) {
      report = HubReportMapper.fromEntity(hubReportEntity);
      blockchainService.retrieveReportProperties(report);
    } else {
      report = blockchainService.retrieveReportProperties(reportId);
    }
    reportRepository.save(toEntity(report));
    listenerService.publishEvent(HUB_REPORT_SAVED, reportId);
    return report;
  }

  public HubReport refreshReportFraud(long reportId) {
    HubReportEntity hubReportEntity = reportRepository.findById(reportId).orElse(null);
    if (hubReportEntity == null) {
      return null;
    }
    boolean reportFraud = blockchainService.isReportFraud(reportId);
    hubReportEntity.setFraud(reportFraud);
    reportRepository.save(hubReportEntity);
    listenerService.publishEvent(HUB_REPORT_SAVED, reportId);
    return HubReportMapper.fromEntity(hubReportEntity);
  }

  public void computeUemReward(HubReport report, double periodFixedGlobalIndex, double periodRewardAmount) {
    double uemRewardAmount = 0;
    if (!report.isFraud()) {
      uemRewardAmount = BigDecimal.valueOf(report.getFixedRewardIndex())
                                  .multiply(BigDecimal.valueOf(periodRewardAmount))
                                  .divide(BigDecimal.valueOf(periodFixedGlobalIndex),
                                          MathContext.DECIMAL128)
                                  .doubleValue();
    }
    boolean changed = uemRewardAmount != report.getUemRewardAmount();
    report.setUemRewardAmount(uemRewardAmount);
    if (changed) {
      reportRepository.findById(report.getReportId())
                      .ifPresent(hubReportEntity -> {
                        hubReportEntity.setUemRewardAmount(report.getUemRewardAmount());
                        reportRepository.save(hubReportEntity);
                      });
    }
    computeEngagementScore(report.getReportId());
  }

  public double computeEngagementScore(long reportId) {
    HubReportEntity hubReportEntity = reportRepository.findById(reportId).orElseThrow();
    boolean isRewardedPeriod = lastReportEngagementScore || hubReportEntity.getSentDate()
                                                                           .atZone(ZoneOffset.UTC)
                                                                           .toLocalDate()
                                                                           .isBefore(LocalDate.now().with(DayOfWeek.MONDAY));
    if (!isRewardedPeriod) {
      return 0d;
    } else if (hubReportEntity.getEngagementScore() > 0) {
      return hubReportEntity.getEngagementScore();
    } else {
      UemRewardEntity rewardEntity = rewardRepository.findById(hubReportEntity.getRewardId()).orElseThrow();
      BigDecimal averageIndex = BigDecimal.valueOf(rewardEntity.getFixedGlobalIndex())
                                          .divide(BigDecimal.valueOf(rewardEntity.getReportIds().size()), MathContext.DECIMAL128);
      hubReportEntity.setEngagementScore(BigDecimal.valueOf(hubReportEntity.getFixedRewardIndex())
                                                   .multiply(BigDecimal.TEN)
                                                   .divide(averageIndex, MathContext.DECIMAL128)
                                                   .setScale(3, RoundingMode.HALF_EVEN)
                                                   .doubleValue());
      hubReportEntity = reportRepository.save(hubReportEntity);
      return hubReportEntity.getEngagementScore();
    }
  }

}
