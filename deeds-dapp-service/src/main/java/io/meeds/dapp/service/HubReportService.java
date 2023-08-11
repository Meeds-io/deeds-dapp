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

import static io.meeds.deeds.utils.JsonUtils.toJsonString;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Sign.SignatureData;
import org.web3j.utils.Numeric;

import io.meeds.dapp.elasticsearch.model.DeedHubRewardReport;
import io.meeds.dapp.storage.HubRewardReportRepository;
import io.meeds.deeds.common.service.BlockchainService;
import io.meeds.deeds.common.service.ListenerService;
import io.meeds.deeds.constant.HubRewardReportStatusType;
import io.meeds.deeds.constant.WomAuthorizationException;
import io.meeds.deeds.constant.WomException;
import io.meeds.deeds.constant.WomRequestException;
import io.meeds.deeds.model.Hub;
import io.meeds.deeds.model.HubRewardContract;
import io.meeds.deeds.model.HubRewardPayment;
import io.meeds.deeds.model.HubRewardReport;
import io.meeds.deeds.model.HubRewardReportRequest;
import io.meeds.deeds.model.HubRewardReportStatus;

@Component
public class HubReportService {

  @Autowired
  private BlockchainService         blockchainService;

  @Autowired
  private HubService                hubService;

  @Autowired
  private ListenerService           listenerService;

  @Autowired
  private HubRewardReportRepository hubRewardReportRepository;

  @Value("${io.meeds.whitelistRewardContracts:}")
  private List<String>              whitelistRewardContractsValues;

  @Value("${io.meeds.test.acceptOudatedReport:false}")
  private boolean                   acceptOudatedReport;

  private List<HubRewardContract>   whitelistRewardContracts;

  public Page<HubRewardReportStatus> getRewardReports(String hubAddress,
                                                      Pageable pageable) {
    return hubRewardReportRepository.findByHubAddress(StringUtils.lowerCase(hubAddress), pageable)
                                    .map(this::fromEntity);
  }

  public HubRewardReportStatus getRewardReport(String hash) {
    return hubRewardReportRepository.findById(StringUtils.lowerCase(hash))
                                    .map(this::fromEntity)
                                    .orElse(null);
  }

  public HubRewardReportStatus saveRewardReport(HubRewardReportRequest hubRewardReportRequest) throws WomException {
    HubRewardReport rewardReport = hubRewardReportRequest.getRewardReport();
    String rawMessage = toJsonString(rewardReport);
    String signature = hubRewardReportRequest.getSignature();

    checkSignedMessage(rewardReport.getHubAddress(),
                       signature,
                       rawMessage);
    checkHash(hubRewardReportRequest.getHash(), signature);

    Hub hub = hubService.getHub(rewardReport.getHubAddress());
    checkHubValidity(hub);
    checkRewardDates(hub, rewardReport);
    checkTokenWhitelisted(rewardReport);

    DeedHubRewardReport deedHubRewardReport = saveRewardReportRequest(hub, hubRewardReportRequest);
    listenerService.publishEvent("wom.hubRewardReport.saved", deedHubRewardReport);
    return fromEntity(deedHubRewardReport);
  }

  private DeedHubRewardReport saveRewardReportRequest(Hub hub, HubRewardReportRequest hubRewardReportRequest) {
    DeedHubRewardReport existingEntity = hubRewardReportRepository.findById(hubRewardReportRequest.getHash()).orElse(null);
    DeedHubRewardReport deedHubRewardReport = toEntity(hub, hubRewardReportRequest, existingEntity);
    if (existingEntity == null) {
      deedHubRewardReport.setStatus(HubRewardReportStatusType.SENT);
    }
    return hubRewardReportRepository.save(deedHubRewardReport);
  }

  private void checkHash(String hash, String signature) throws WomAuthorizationException {
    if (!StringUtils.equalsIgnoreCase(hash, Hash.sha3(signature))) {
      throw new WomAuthorizationException("wom.wrongSignatureHash");
    }
  }

  private void checkRewardDates(Hub hub, HubRewardReport rewardReport) throws WomRequestException {
    if (!acceptOudatedReport && rewardReport.getToDate().isBefore(hub.getCreatedDate())) {
      throw new WomRequestException("wom.sentRewardReportEndDateIsBeforeWoMConnection");
    }
  }

  private void checkHubValidity(Hub hub) throws WomRequestException {
    if (hub == null || hub.getDeedId() < 0) {
      throw new WomRequestException("wom.hubNotConnectedToWoM");
    }
    if (!hubService.isDeedManager(hub.getDeedManagerAddress(), hub.getDeedId())) {
      throw new WomRequestException("wom.hubManagerChangedNoReportReceived");
    }
  }

  private void checkTokenWhitelisted(HubRewardReport rewardReport) throws WomException {
    if (!isWhitelistRewardContract(rewardReport.getRewardTokenNetworkId(), rewardReport.getRewardTokenAddress())) {
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

      BigInteger publicKey = Sign.signedPrefixedMessageToKey(rawMessage.getBytes(), new SignatureData(v, r, s));
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

  private boolean isWhitelistRewardContract(long networkId, String contractAddress) throws WomRequestException {
    if (CollectionUtils.isEmpty(whitelistRewardContracts)) {
      try {
        HubRewardContract ethereumMeedToken = new HubRewardContract(blockchainService.getNetworkId(),
                                                                    StringUtils.lowerCase(blockchainService.getEthereumMeedTokenAddress()));
        HubRewardContract polygonMeedToken = new HubRewardContract(blockchainService.getPolygonNetworkId(),
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
                                        return new HubRewardContract(Long.parseLong(parts[0]), StringUtils.lowerCase(parts[1]));
                                      })
                                      .forEach(whitelistRewardContracts::add);
      }
    }
    return whitelistRewardContracts.contains(new HubRewardContract(networkId, StringUtils.lowerCase(contractAddress)));
  }

  private DeedHubRewardReport toEntity(Hub hub, HubRewardReportRequest hubRewardReportRequest,
                                       DeedHubRewardReport existingEntity) {
    HubRewardReport rewardReport = hubRewardReportRequest.getRewardReport();
    DeedHubRewardReport deedHubRewardReport = existingEntity == null ? new DeedHubRewardReport() : existingEntity;
    deedHubRewardReport.setHash(hubRewardReportRequest.getHash());
    deedHubRewardReport.setSignature(hubRewardReportRequest.getSignature());
    deedHubRewardReport.setHubAddress(rewardReport.getHubAddress());
    deedHubRewardReport.setEarnerAddress(StringUtils.lowerCase(hub.getEarnerAddress()));
    deedHubRewardReport.setDeedManagerAddress(StringUtils.lowerCase(hub.getDeedManagerAddress()));
    deedHubRewardReport.setDeedId(rewardReport.getDeedId());
    deedHubRewardReport.setFromDate(rewardReport.getFromDate());
    deedHubRewardReport.setToDate(rewardReport.getToDate());
    deedHubRewardReport.setSentRewardsDate(rewardReport.getSentRewardsDate());
    deedHubRewardReport.setPeriodType(rewardReport.getPeriodType());
    deedHubRewardReport.setParticipantsCount(rewardReport.getParticipantsCount());
    deedHubRewardReport.setRecipientsCount(rewardReport.getRecipientsCount());
    deedHubRewardReport.setAchievementsCount(rewardReport.getAchievementsCount());
    deedHubRewardReport.setRewardAmount(rewardReport.getRewardAmount());
    deedHubRewardReport.setRewardTokenAddress(rewardReport.getRewardTokenAddress());
    deedHubRewardReport.setRewardTokenNetworkId(rewardReport.getRewardTokenNetworkId());
    deedHubRewardReport.setTransactions(rewardReport.getTransactions());
    return deedHubRewardReport;
  }

  private HubRewardReportStatus fromEntity(DeedHubRewardReport deedHubRewardReport) {
    HubRewardReport rewardReport = new HubRewardReport();
    rewardReport.setHubAddress(deedHubRewardReport.getHubAddress());
    rewardReport.setDeedId(deedHubRewardReport.getDeedId());
    rewardReport.setFromDate(deedHubRewardReport.getFromDate());
    rewardReport.setToDate(deedHubRewardReport.getToDate());
    rewardReport.setSentRewardsDate(deedHubRewardReport.getSentRewardsDate());
    rewardReport.setPeriodType(deedHubRewardReport.getPeriodType());
    rewardReport.setParticipantsCount(deedHubRewardReport.getParticipantsCount());
    rewardReport.setRecipientsCount(deedHubRewardReport.getRecipientsCount());
    rewardReport.setAchievementsCount(deedHubRewardReport.getAchievementsCount());
    rewardReport.setRewardAmount(deedHubRewardReport.getRewardAmount());
    rewardReport.setRewardTokenAddress(deedHubRewardReport.getRewardTokenAddress());
    rewardReport.setRewardTokenNetworkId(deedHubRewardReport.getRewardTokenNetworkId());
    rewardReport.setTransactions(deedHubRewardReport.getTransactions());

    HubRewardPayment rewardPayment = null; // TODO
    return new HubRewardReportStatus(deedHubRewardReport.getHash(),
                                     rewardReport,
                                     StringUtils.lowerCase(deedHubRewardReport.getEarnerAddress()),
                                     StringUtils.lowerCase(deedHubRewardReport.getDeedManagerAddress()),
                                     deedHubRewardReport.getStatus(),
                                     null,
                                     rewardPayment);
  }

}
