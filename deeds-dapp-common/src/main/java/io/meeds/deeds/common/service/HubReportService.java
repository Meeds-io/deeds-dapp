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

import static io.meeds.deeds.common.utils.HubReportMapper.fromEntity;
import static io.meeds.deeds.common.utils.HubReportMapper.toEntity;
import static io.meeds.wom.api.constant.HubReportStatusType.ERROR_SENDING;
import static io.meeds.wom.api.constant.HubReportStatusType.INVALID;
import static io.meeds.wom.api.constant.HubReportStatusType.NONE;
import static io.meeds.wom.api.constant.HubReportStatusType.PENDING_REWARD;
import static io.meeds.wom.api.constant.HubReportStatusType.REJECTED;
import static io.meeds.wom.api.constant.HubReportStatusType.REWARDED;
import static io.meeds.wom.api.constant.HubReportStatusType.SENT;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.meeds.deeds.common.blockchain.BlockchainConfigurationProperties;
import io.meeds.deeds.common.elasticsearch.model.DeedTenant;
import io.meeds.deeds.common.elasticsearch.model.HubReportEntity;
import io.meeds.deeds.common.elasticsearch.storage.HubReportRepository;
import io.meeds.deeds.common.model.RewardPeriod;
import io.meeds.deeds.common.utils.HubReportMapper;
import io.meeds.wom.api.constant.HubReportStatusType;
import io.meeds.wom.api.constant.ObjectNotFoundException;
import io.meeds.wom.api.constant.WomAuthorizationException;
import io.meeds.wom.api.constant.WomException;
import io.meeds.wom.api.constant.WomRequestException;
import io.meeds.wom.api.model.Hub;
import io.meeds.wom.api.model.HubContract;
import io.meeds.wom.api.model.HubReport;
import io.meeds.wom.api.model.HubReportPayload;
import io.meeds.wom.api.model.HubReportVerifiableData;

import lombok.Getter;

@Component
public class HubReportService {

  public static final String                     HUB_REPORT_RECEIVED            = "uem.report.received";

  public static final String                     HUB_REPORT_SAVED               = "uem.report.saved";

  public static final String                     HUB_REPORT_STATUS_CHANGED      = "uem.report.status.changed";

  public static final String                     HUB_REPORT_TRANSACTION_SENT    = "uem.report.status.transactionSent";

  public static final String                     HUB_REPORT_UEM_REWARD_COMPUTED = "uem.report.computed";

  private static final List<HubReportStatusType> INVALID_STATUSES               = Stream.of(NONE,
                                                                                            INVALID,
                                                                                            REJECTED,
                                                                                            ERROR_SENDING)
                                                                                        .toList();

  @Autowired
  private BlockchainService                      blockchainService;

  @Autowired
  private HubService                             hubService;

  @Autowired
  private TenantService                          tenantService;

  @Autowired
  private ListenerService                        listenerService;

  @Autowired
  private HubReportRepository                    reportRepository;

  @Autowired
  private SettingService                         settingService;

  @Autowired
  private BlockchainConfigurationProperties      blockchainProperties;

  @Value("${io.meeds.whitelistRewardContracts:}")
  private List<String>                           whitelistRewardContractsValues;

  @Value("${io.meeds.test.acceptOudatedReport:false}")
  private boolean                                acceptOudatedReport;

  @Getter
  private Instant                                uemStartDate;

  private List<HubContract>                      whitelistRewardContracts;

  @PostConstruct
  public void init() {
    String value = settingService.get("uemStartDate");
    if (StringUtils.isNotBlank(value)) {
      uemStartDate = Instant.ofEpochMilli(Long.parseLong(value));
    } else if (StringUtils.isNotBlank(blockchainProperties.getUemAddress())) {
      uemStartDate = Instant.now();
      settingService.save("uemStartDate", String.valueOf(uemStartDate.toEpochMilli()));
    }
  }

  public Page<HubReport> getReportsByHub(String hubAddress, Pageable pageable) {
    return getReports(hubAddress, null, pageable);
  }

  public Page<HubReport> getReportsByReward(String rewardId, Pageable pageable) {
    return getReports(null, rewardId, pageable);
  }

  public Page<HubReport> getReports(String hubAddress, String rewardId, Pageable pageable) {
    Page<HubReportEntity> page;
    if (StringUtils.isBlank(hubAddress) && StringUtils.isBlank(rewardId)) {
      pageable = pageable.isUnpaged() ? pageable
                                      : PageRequest.of(pageable.getPageNumber(),
                                                       pageable.getPageSize(),
                                                       pageable.getSortOr(Sort.by(Direction.DESC, "fromDate")));
      page = reportRepository.findAll(pageable);
    } else if (StringUtils.isBlank(rewardId)) {
      pageable = pageable.isUnpaged() ? pageable
                                      : PageRequest.of(pageable.getPageNumber(),
                                                       pageable.getPageSize(),
                                                       pageable.getSortOr(Sort.by(Direction.DESC, "sentDate")));
      page = reportRepository.findByHubAddress(StringUtils.lowerCase(hubAddress), pageable);
    } else if (StringUtils.isBlank(hubAddress)) {
      pageable = pageable.isUnpaged() ? pageable
                                      : PageRequest.of(pageable.getPageNumber(),
                                                       pageable.getPageSize(),
                                                       pageable.getSortOr(Sort.by(Direction.DESC, "sentDate")));
      page = reportRepository.findByRewardId(rewardId, pageable);
    } else {
      page = reportRepository.findByRewardIdAndHubAddress(rewardId, StringUtils.lowerCase(hubAddress), pageable);
    }
    return page.map(HubReportMapper::fromEntity);
  }

  public HubReport getReport(String hash) {
    return reportRepository.findById(StringUtils.lowerCase(hash))
                           .map(HubReportMapper::fromEntity)
                           .orElse(null);
  }

  public List<HubReport> getValidReports(RewardPeriod rewardPeriod) {
    return reportRepository.findBySentDateBetweenAndStatusNotIn(rewardPeriod.getFrom(),
                                                                rewardPeriod.getTo(),
                                                                INVALID_STATUSES)
                           .map(HubReportMapper::fromEntity)
                           .toList();
  }

  public HubReport getValidReport(String rewardId, String hubAddress) {
    return reportRepository.findByRewardIdAndHubAddressAndStatusNotIn(rewardId,
                                                                      StringUtils.lowerCase(hubAddress),
                                                                      INVALID_STATUSES)
                           .map(HubReportMapper::fromEntity)
                           .orElse(null);
  }

  public HubReport getLastReport(String hubAddress,
                                 Instant beforeDate,
                                 String hash,
                                 List<HubReportStatusType> statuses) {
    Pageable pageable = Pageable.ofSize(1);
    Page<HubReportEntity> page;
    if (StringUtils.isBlank(hash)) {
      page =
           reportRepository.findByHubAddressAndSentDateBeforeAndStatusInOrderByFromDateDesc(StringUtils.lowerCase(hubAddress),
                                                                                            beforeDate,
                                                                                            statuses,
                                                                                            pageable);
    } else {
      page =
           reportRepository.findByHubAddressAndSentDateBeforeAndHashNotAndStatusInOrderByFromDateDesc(StringUtils.lowerCase(hubAddress),
                                                                                                      beforeDate,
                                                                                                      StringUtils.lowerCase(hash),
                                                                                                      statuses,
                                                                                                      pageable);
    }
    return page.get()
               .findFirst()
               .map(HubReportMapper::fromEntity)
               .orElse(null);
  }

  public HubReport sendReport(HubReportVerifiableData reportData) throws WomException {
    try {
      if (!reportData.isValid()) {
        throw new WomAuthorizationException("wom.invalidSignedMessage");
      }
    } catch (Exception e) {
      throw new WomAuthorizationException("wom.invalidSignedMessage", e);
    }

    Hub hub = hubService.getHub(reportData.getHubAddress());

    checkHubValidity(hub);
    checkRewardDates(hub, reportData);
    checkTokenWhitelisted(reportData);

    HubReportEntity reportEntity;
    try {
      reportEntity = toReportEntity(hub, reportData);
    } catch (ObjectNotFoundException e) {
      throw new WomException("wom.deedIdNotRecognized", e);
    }
    reportEntity = reportRepository.save(reportEntity);
    listenerService.publishEvent(HUB_REPORT_RECEIVED, reportData.getHash());
    return fromEntity(reportEntity);
  }

  public void saveReportStatus(String hash, HubReportStatusType status) {
    reportRepository.findById(StringUtils.lowerCase(hash))
                    .ifPresent(report -> {
                      if (INVALID_STATUSES.contains(status)) {
                        report.setRewardId(null);
                        report.setRewardHash(null);
                        report.setUemRewardIndex(0d);
                        report.setUemRewardAmount(0d);
                        report.setHubRewardAmountPerPeriod(0d);
                        report.setLastPeriodUemRewardAmount(0d);
                        report.setLastPeriodUemRewardAmountPerPeriod(0d);
                      }
                      report.setStatus(status);
                      reportRepository.save(report);
                      listenerService.publishEvent(HUB_REPORT_STATUS_CHANGED, hash);
                    });
  }

  public void saveReportTransactionHash(String hash, String transactionHash) {
    reportRepository.findById(StringUtils.lowerCase(hash))
                    .ifPresent(report -> {
                      report.setStatus(PENDING_REWARD);
                      report.setRewardTransactionHash(transactionHash);
                      reportRepository.save(report);
                      listenerService.publishEvent(HUB_REPORT_TRANSACTION_SENT, hash);
                    });
  }

  public void saveReportLeaseStatus(HubReport report) {
    reportRepository.findById(report.getHash())
                    .ifPresent(reportEntity -> {
                      reportEntity.setDeedManagerAddress(report.getDeedManagerAddress());
                      reportEntity.setOwnerAddress(report.getOwnerAddress());
                      reportEntity.setOwnerMintingPercentage(report.getOwnerMintingPercentage());
                      reportRepository.save(reportEntity);
                    });
  }

  public void saveReportUEMProperties(HubReport report) {
    reportRepository.findById(StringUtils.lowerCase(report.getHash()))
                    .ifPresent(r -> {
                      r.setRewardId(report.getRewardId());
                      r.setRewardHash(report.getRewardHash());

                      r.setUemRewardIndex(report.getUemRewardIndex());
                      r.setUemRewardAmount(report.getUemRewardAmount());

                      r.setHubRewardLastPeriodDiff(report.getHubRewardLastPeriodDiff());
                      r.setHubRewardAmountPerPeriod(report.getHubRewardAmountPerPeriod());

                      r.setLastPeriodUemRewardAmount(report.getLastPeriodUemRewardAmount());
                      r.setLastPeriodUemDiff(report.getLastPeriodUemDiff());
                      r.setLastPeriodUemRewardAmountPerPeriod(report.getLastPeriodUemRewardAmountPerPeriod());

                      r.setMp(report.getMp());
                      r.setStatus(report.getStatus());
                      reportRepository.save(r);
                      listenerService.publishEvent(HUB_REPORT_UEM_REWARD_COMPUTED, r.getHash());
                    });
  }

  private HubReportEntity toReportEntity(Hub hub, HubReportVerifiableData reportData) throws ObjectNotFoundException {
    HubReportEntity existingEntity = reportRepository.findById(StringUtils.lowerCase(reportData.getHash()))
                                                     .orElse(null);
    String managerAddress = getDeedManagerAddress(hub);
    String ownerAddress = getOwnerAddress(hub);
    HubReport report = new HubReport(reportData.getHash(),
                                     reportData.getSignature(),
                                     reportData.getHubAddress(),
                                     reportData.getDeedId(),
                                     reportData.getFromDate(),
                                     reportData.getToDate(),
                                     reportData.getSentDate(),
                                     reportData.getPeriodType(),
                                     reportData.getUsersCount(),
                                     reportData.getParticipantsCount(),
                                     reportData.getRecipientsCount(),
                                     reportData.getAchievementsCount(),
                                     reportData.getRewardTokenAddress(),
                                     reportData.getRewardTokenNetworkId(),
                                     reportData.getHubRewardAmount(),
                                     reportData.getTransactions(),
                                     StringUtils.lowerCase(hub.getEarnerAddress()),
                                     StringUtils.lowerCase(managerAddress),
                                     StringUtils.lowerCase(ownerAddress),
                                     0,
                                     existingEntity == null ? HubReportStatusType.SENT
                                                            : existingEntity.getStatus(),
                                     null,
                                     existingEntity == null ? 0d
                                                            : existingEntity.getUemRewardIndex(),
                                     existingEntity == null ? 0d
                                                            : existingEntity.getUemRewardAmount(),
                                     existingEntity == null ? 0d
                                                            : existingEntity.getLastPeriodUemRewardAmount(),
                                     existingEntity == null ? 0d
                                                            : existingEntity.getLastPeriodUemDiff(),
                                     existingEntity == null ? 0d
                                                            : existingEntity.getHubRewardAmountPerPeriod(),
                                     existingEntity == null ? 0d
                                                            : existingEntity.getHubRewardLastPeriodDiff(),
                                     existingEntity == null ? 0d
                                                            : existingEntity.getLastPeriodUemRewardAmountPerPeriod(),
                                     existingEntity == null ? 0d
                                                            : existingEntity.getMp(),
                                     existingEntity == null ? null
                                                            : existingEntity.getRewardId(),
                                     existingEntity == null ? null
                                                            : existingEntity.getRewardHash(),
                                     existingEntity == null ? null
                                                            : existingEntity.getRewardTransactionHash());
    return toEntity(report);
  }

  private void checkRewardDates(Hub hub, HubReportVerifiableData report) throws WomRequestException {
    if (acceptOudatedReport) {
      // Testing environment
      return;
    }
    if (report.getToDate().isBefore(hub.getCreatedDate())) {
      throw new WomRequestException("wom.sentReportIsBeforeWoMConnection");
    }
    if (!isReportPeriodValid(report)) {
      throw new WomRequestException("wom.sentReportIsBeforeUEM");
    }
    HubReport lastRewardedReport = getLastHubReport(hub.getAddress(), report.getHash());
    if (lastRewardedReport != null
        && Duration.between(lastRewardedReport.getToDate(), report.getFromDate()).toDays() < 0) {
      throw new WomRequestException("wom.sentReportIsBeforeLastRewardedReport");
    }
  }

  private HubReport getLastHubReport(String hubAddress, String hash) {
    Pageable pageable = Pageable.ofSize(1);
    return reportRepository.findByHubAddressAndHashNotAndStatusInOrderByFromDateDesc(StringUtils.lowerCase(hubAddress),
                                                                                     StringUtils.lowerCase(hash),
                                                                                     Stream.of(NONE,
                                                                                               SENT,
                                                                                               PENDING_REWARD,
                                                                                               REWARDED)
                                                                                           .toList(),
                                                                                     pageable)
                           .get()
                           .findFirst()
                           .map(HubReportMapper::fromEntity)
                           .orElse(null);
  }

  private void checkHubValidity(Hub hub) throws WomRequestException {
    if (hub == null || hub.getDeedId() < 0) {
      throw new WomRequestException("wom.hubNotConnectedToWoM");
    }
  }

  private void checkTokenWhitelisted(HubReportPayload report) throws WomException {
    if (!isWhitelistRewardContract(report.getRewardTokenNetworkId(), report.getRewardTokenAddress())) {
      throw new WomAuthorizationException("wom.unsupporedRewardContract");
    }
  }

  private String getDeedManagerAddress(Hub hub) throws ObjectNotFoundException {
    if (StringUtils.isNotBlank(hub.getDeedManagerAddress())
        && tenantService.isDeedManager(hub.getDeedManagerAddress(), hub.getDeedId())) {
      return hub.getDeedManagerAddress();
    } else {
      DeedTenant deedTenant = tenantService.getDeedTenantOrImport(hub.getDeedId());
      String managerAddress;
      if (StringUtils.isNotBlank(deedTenant.getManagerAddress())
          && tenantService.isDeedManager(deedTenant.getManagerAddress(), hub.getDeedId())) {
        managerAddress = deedTenant.getManagerAddress();
        hub.setDeedManagerAddress(managerAddress);
        hubService.saveHubManagerAddress(hub.getAddress(), managerAddress);
        return managerAddress;
      } else if (StringUtils.isNotBlank(deedTenant.getOwnerAddress())
          && tenantService.isDeedManager(deedTenant.getOwnerAddress(), hub.getDeedId())) {
        managerAddress = deedTenant.getOwnerAddress();
        hub.setDeedManagerAddress(managerAddress);
        hubService.saveHubManagerAddress(hub.getAddress(), managerAddress);
        return managerAddress;
      } else {
        return null;
      }
    }
  }

  private String getOwnerAddress(Hub hub) throws ObjectNotFoundException {
    String ownerAddress;
    if (StringUtils.isNotBlank(hub.getOwnerAddress()) && tenantService.isDeedOwner(hub.getOwnerAddress(), hub.getDeedId())) {
      return hub.getOwnerAddress();
    } else {
      DeedTenant deedTenant = tenantService.getDeedTenantOrImport(hub.getDeedId());
      if (tenantService.isDeedOwner(deedTenant.getOwnerAddress(), hub.getDeedId())) {
        ownerAddress = deedTenant.getOwnerAddress();
        hub.setOwnerAddress(ownerAddress);
        hubService.saveHubOwnerAddress(hub.getAddress(), ownerAddress);
        return ownerAddress;
      } else {
        return null;
      }
    }
  }

  private boolean isReportPeriodValid(HubReportPayload reportData) {
    if (uemStartDate == null) {
      return true;
    } else {
      return uemStartDate.isBefore(reportData.getToDate());
    }
  }

  private boolean isWhitelistRewardContract(long networkId, String contractAddress) throws WomRequestException {
    if (CollectionUtils.isEmpty(whitelistRewardContracts)) {
      try {
        HubContract ethereumMeedToken = new HubContract(blockchainService.getNetworkId(),
                                                        StringUtils.lowerCase(blockchainService.getEthereumMeedTokenAddress()));
        HubContract polygonMeedToken = new HubContract(blockchainService.getPolygonNetworkId(),
                                                       StringUtils.lowerCase(blockchainService.getPolygonMeedTokenAddress()));

        whitelistRewardContracts = new ArrayList<>();
        whitelistRewardContracts.add(ethereumMeedToken);
        whitelistRewardContracts.add(polygonMeedToken);
      } catch (IOException e) {
        throw new WomRequestException("wom.blockchainConnectionError", e);
      }
      if (!CollectionUtils.isEmpty(whitelistRewardContractsValues)) {
        whitelistRewardContractsValues.stream()
                                      .filter(StringUtils::isNotBlank)
                                      .filter(s -> s.contains(":"))
                                      .map(s -> {
                                        String[] parts = s.split(":");
                                        return new HubContract(Long.parseLong(parts[0]), StringUtils.lowerCase(parts[1]));
                                      })
                                      .forEach(whitelistRewardContracts::add);
      }
    }
    return whitelistRewardContracts.contains(new HubContract(networkId, StringUtils.lowerCase(contractAddress)));
  }

}
