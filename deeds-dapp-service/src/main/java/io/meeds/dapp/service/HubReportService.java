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
package io.meeds.dapp.service;

import static io.meeds.deeds.api.constant.HubReportStatusType.NONE;
import static io.meeds.deeds.api.constant.HubReportStatusType.PENDING_REWARD;
import static io.meeds.deeds.api.constant.HubReportStatusType.REWARDED;
import static io.meeds.deeds.api.constant.HubReportStatusType.SENT;
import static io.meeds.deeds.common.utils.HubReportMapper.fromEntity;
import static io.meeds.deeds.common.utils.HubReportMapper.toEntity;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Sign.SignatureData;
import org.web3j.utils.Numeric;

import io.meeds.deeds.api.constant.HubReportStatusType;
import io.meeds.deeds.api.constant.WomAuthorizationException;
import io.meeds.deeds.api.constant.WomException;
import io.meeds.deeds.api.constant.WomRequestException;
import io.meeds.deeds.api.model.Hub;
import io.meeds.deeds.api.model.HubContract;
import io.meeds.deeds.api.model.HubReport;
import io.meeds.deeds.api.model.HubReportData;
import io.meeds.deeds.api.model.HubReportRequest;
import io.meeds.deeds.common.blockchain.BlockchainConfigurationProperties;
import io.meeds.deeds.common.elasticsearch.model.HubReportEntity;
import io.meeds.deeds.common.elasticsearch.storage.HubReportRepository;
import io.meeds.deeds.common.service.BlockchainService;
import io.meeds.deeds.common.service.ListenerService;
import io.meeds.deeds.common.service.SettingService;
import io.meeds.deeds.common.utils.HubReportMapper;

import lombok.Getter;

@Component
public class HubReportService {

  @Autowired
  private BlockchainService                 blockchainService;

  @Autowired
  private HubService                        hubService;

  @Autowired
  private ListenerService                   listenerService;

  @Autowired
  private HubReportRepository               reportRepository;

  @Autowired
  private SettingService                    settingService;

  @Autowired
  private BlockchainConfigurationProperties blockchainProperties;

  @Value("${io.meeds.whitelistRewardContracts:}")
  private List<String>                      whitelistRewardContractsValues;

  @Value("${io.meeds.test.acceptOudatedReport:false}")
  private boolean                           acceptOudatedReport;

  @Getter
  private Instant                           uemStartDate;

  private List<HubContract>                 whitelistRewardContracts;

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

  public Page<HubReport> getReports(String hubAddress, Pageable pageable) {
    pageable = PageRequest.of(pageable.getPageNumber(),
                              pageable.getPageSize(),
                              pageable.getSortOr(Sort.by(Direction.DESC, "fromDate", "createdDate")));
    return reportRepository.findByHubAddress(StringUtils.lowerCase(hubAddress), pageable)
                           .map(HubReportMapper::fromEntity);
  }

  public HubReport getReport(String hash) {
    return reportRepository.findById(StringUtils.lowerCase(hash))
                           .map(HubReportMapper::fromEntity)
                           .orElse(null);
  }

  public HubReport saveReport(HubReportRequest reportRequest) throws WomException {
    HubReportData reportData = reportRequest.getReport();
    String signature = reportRequest.getSignature();
    String hash = StringUtils.lowerCase(reportRequest.getHash());
    String rawMessage = reportData.getRawMessage();
    String hubAddress = reportData.getHubAddress();

    Hub hub = hubService.getHub(hubAddress);

    checkSignedMessage(hubAddress,
                       signature,
                       rawMessage);
    checkHash(hash, signature);
    checkHubValidity(hub);
    checkRewardDates(hub, hash, reportData);
    checkTokenWhitelisted(reportData);

    HubReportEntity reportEntity = toReportEntity(hash, signature, hub, reportData);
    reportEntity = reportRepository.save(reportEntity);
    listenerService.publishEvent("wom.hubReport.saved", hash);
    return fromEntity(reportEntity);
  }

  private HubReportEntity toReportEntity(String hash, String signature, Hub hub, HubReportData reportData) {
    HubReportEntity existingEntity = reportRepository.findById(hash)
                                                     .orElse(null);
    HubReport report = new HubReport(hash,
                                     signature,
                                     StringUtils.lowerCase(hub.getEarnerAddress()),
                                     StringUtils.lowerCase(hub.getDeedManagerAddress()),
                                     existingEntity == null ? HubReportStatusType.SENT
                                                            : existingEntity.getStatus(),
                                     null,
                                     existingEntity == null ? Instant.now()
                                                            : existingEntity.getCreatedDate(),
                                     existingEntity == null ? 0d
                                                            : existingEntity.getUemRewardIndex(),
                                     existingEntity == null ? 0d
                                                            : existingEntity.getUemRewardAmount(),
                                     existingEntity == null ? 0d
                                                            : existingEntity.getLastPeriodUemRewardAmount(),
                                     existingEntity == null ? 0d
                                                            : existingEntity.getHubRewardAmountPerPeriod(),
                                     existingEntity == null ? 0d
                                                            : existingEntity.getLastPeriodUemRewardAmountPerPeriod(),
                                     existingEntity == null ? null
                                                            : existingEntity.getRewardId(),
                                     existingEntity == null ? null
                                                            : existingEntity.getRewardHash());
    report.setReportData(reportData);
    return toEntity(report);
  }

  private void checkHash(String hash, String signature) throws WomAuthorizationException {
    if (!StringUtils.equalsIgnoreCase(hash, Hash.sha3(signature))) {
      throw new WomAuthorizationException("wom.wrongSignatureHash");
    }
  }

  private void checkRewardDates(Hub hub, String hash, HubReportData report) throws WomRequestException {
    if (!acceptOudatedReport && report.getToDate().isBefore(hub.getCreatedDate())) {
      throw new WomRequestException("wom.sentReportIsBeforeWoMConnection");
    } else if (!acceptOudatedReport && !isReportPeriodValid(report)) {
      throw new WomRequestException("wom.sentReportIsBeforeUEM");
    }

    HubReport lastRewardedReport = getLastHubReport(hub.getAddress(), hash);
    if (!acceptOudatedReport
        && lastRewardedReport != null
        && Duration.between(lastRewardedReport.getToDate(), report.getFromDate()).toDays() < 0) {
      throw new WomRequestException("wom.sentReportIsBeforeLastRewardedReport");
    }
  }

  private HubReport getLastHubReport(String hubAddress, String hash) {
    Pageable pageable = Pageable.ofSize(1);
    return reportRepository.findByHubAddressAndHashNotAndStatusInOrderByFromDateDesc(hubAddress,
                                                                                     hash,
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
    if (!hubService.isDeedManager(hub.getDeedManagerAddress(), hub.getDeedId())) {
      throw new WomRequestException("wom.hubManagerChangedNoReportReceived");
    }
  }

  private void checkTokenWhitelisted(HubReportData report) throws WomException {
    if (!isWhitelistRewardContract(report.getRewardTokenNetworkId(), report.getRewardTokenAddress())) {
      throw new WomAuthorizationException("wom.unsupporedRewardContract");
    }
  }

  private void checkSignedMessage(String hubAddress,
                                  String signedMessage,
                                  String rawMessage) throws WomException {
    if (StringUtils.isBlank(hubAddress)) {
      throw new WomAuthorizationException("wom.emptyHubAddress");
    } else if (StringUtils.isBlank(signedMessage) || StringUtils.isBlank(rawMessage)) {
      throw new WomAuthorizationException("wom.emptySignedMessage");
    }

    try {
      byte[] signatureBytes = Numeric.hexStringToByteArray(signedMessage);
      if (signatureBytes.length < 64) {
        throw new WomAuthorizationException("wom.invalidSignedMessage");
      }
      byte[] r = Arrays.copyOfRange(signatureBytes, 0, 32);
      byte[] s = Arrays.copyOfRange(signatureBytes, 32, 64);
      byte v = signatureBytes[64];
      if (v < 27) {
        v += 27;
      }

      BigInteger publicKey = Sign.signedPrefixedMessageToKey(rawMessage.getBytes(StandardCharsets.UTF_8),
                                                             new SignatureData(v, r, s));
      String recoveredAddress = "0x" + Keys.getAddress(publicKey);
      if (!recoveredAddress.equalsIgnoreCase(hubAddress)) {
        throw new WomAuthorizationException("wom.invalidSignedMessage");
      }
    } catch (WomException e) {
      throw e;
    } catch (Exception e) {
      throw new WomAuthorizationException("wom.invalidSignedMessage", e);
    }
  }

  private boolean isReportPeriodValid(HubReportData reportData) {
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
