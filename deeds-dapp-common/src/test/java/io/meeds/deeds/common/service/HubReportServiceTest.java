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
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.deeds.common.service;

import static io.meeds.deeds.common.service.HubReportService.HUB_REPORT_SAVED;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import io.meeds.deeds.common.elasticsearch.model.HubReportEntity;
import io.meeds.deeds.common.elasticsearch.storage.HubReportRepository;
import io.meeds.wom.api.constant.WomAuthorizationException;
import io.meeds.wom.api.constant.WomException;
import io.meeds.wom.api.model.HubReport;
import io.meeds.wom.api.model.HubReportPayload;
import io.meeds.wom.api.model.HubReportVerifiableData;

@SpringBootTest(classes = {
                            HubReportService.class,
})
@ExtendWith(MockitoExtension.class)
public class HubReportServiceTest {

  private static final Pageable PAGEABLE                  = Pageable.ofSize(10);

  @MockBean
  private BlockchainService     blockchainService;

  @MockBean
  private ListenerService       listenerService;

  @MockBean
  private HubReportRepository   reportRepository;

  @Autowired
  private HubReportService      hubReportService;

  private Credentials           hubCredentials            =
                                               Credentials.create("0x1da4ef21b864d2cc526dbdb2a120bd2874c36c9d0a1fb7f8c63d7f7a8b41de8f");

  private String                hubAddress                = hubCredentials.getAddress();

  private long                  deedId                    = 3l;

  private long                  reportId                  = 56684l;

  private long                  rewardId                  = 6398841l;

  private long                  periodStartTime           = ZonedDateTime.now()
                                                                         .with(DayOfWeek.MONDAY)
                                                                         .minusWeeks(3)
                                                                         .toLocalDate()
                                                                         .atStartOfDay(ZoneOffset.UTC)
                                                                         .toEpochSecond();

  private long                  periodEndTime             = ZonedDateTime.now()
                                                                         .with(DayOfWeek.MONDAY)
                                                                         .minusWeeks(2)
                                                                         .toLocalDate()
                                                                         .atStartOfDay(ZoneOffset.UTC)
                                                                         .toEpochSecond();

  private short                 city                      = 1;

  private short                 cardType                  = 3;

  private short                 mintingPower              = 120;

  private long                  maxUsers                  = Long.MAX_VALUE;

  private int                   ownerMintingPercentage    = 60;

  private double                fixedRewardIndex          = 0.005446d;

  private double                ownerFixedIndex           = 0.0032676d;

  private double                tenantFixedIndex          = 0.0021784d;

  private double                lastPeriodUemRewardAmount = 84d;

  private double                uemRewardAmount           = 90d;

  private double                hubRewardAmount           = 150d;

  private Instant               updatedDate               = Instant.now();

  private boolean               fraud                     = false;

  private long                  usersCount                = 125l;

  private long                  participantsCount         = 85l;

  private long                  recipientsCount           = 65l;

  private int                   achievementsCount         = 55698;

  private String                rewardTokenAddress        = "0x334d85047da64738c065d36e10b2adeb965000d0";

  private long                  rewardTokenNetworkId      = 1l;

  private String                deedManagerAddress        = "0x609a6f01b7976439603356e41d5456b42df957b7";

  private String                ownerAddress              = "0x27d282d1e7e790df596f50a234602d9e761d22aa";

  private String                periodType                = "WEEK";

  private String                txHash                    =
                                       "0xef4e9db309b5dd7020ce463ae726b4d0759e1de0635661de91d8d98e83ae2862";

  private Instant               sentDate                  = Instant.now();

  @Test
  void getReportsByHub() {
    when(reportRepository.findByHubAddress(eq(StringUtils.lowerCase(hubAddress)), any())).thenReturn(new PageImpl<>(Arrays.asList(newHubReportEntity())));
    Page<HubReport> reports = hubReportService.getReportsByHub(hubAddress, PAGEABLE);
    assertNotNull(reports);
    assertEquals(1, reports.getSize());
    HubReport report = reports.getContent().get(0);
    assertNotNull(report);
    assertEquals(report.getReportId(), reportId);
    assertEquals(report.getRewardId(), rewardId);
    assertEquals(report.getHubAddress(), StringUtils.lowerCase(hubAddress));
    assertEquals(report.getDeedManagerAddress(), StringUtils.lowerCase(deedManagerAddress));
    assertEquals(report.getOwnerAddress(), StringUtils.lowerCase(ownerAddress));
    assertEquals(report.getOwnerMintingPercentage(), ownerMintingPercentage);
    assertEquals(report.getDeedId(), deedId);
    assertEquals(report.getCity(), city);
    assertEquals(report.getCardType(), cardType);
    assertEquals(report.getMintingPower(), mintingPower);
    assertEquals(report.getMaxUsers(), maxUsers);
    assertEquals(report.getFromDate(), fromDate());
    assertEquals(report.getToDate(), toDate());
    assertEquals(report.getSentDate(), sentDate);
    assertEquals(report.getPeriodType(), periodType);
    assertEquals(report.getUsersCount(), usersCount);
    assertEquals(report.getParticipantsCount(), participantsCount);
    assertEquals(report.getRecipientsCount(), recipientsCount);
    assertEquals(report.getAchievementsCount(), achievementsCount);
    assertEquals(report.getRewardTokenAddress(), rewardTokenAddress);
    assertEquals(report.getRewardTokenNetworkId(), rewardTokenNetworkId);
    assertEquals(report.getTransactions(), transactions());
    assertEquals(report.getHubRewardAmount(), hubRewardAmount);
    assertEquals(report.getFixedRewardIndex(), fixedRewardIndex);
    assertEquals(report.getOwnerFixedIndex(), ownerFixedIndex);
    assertEquals(report.getTenantFixedIndex(), tenantFixedIndex);
    assertEquals(report.isFraud(), fraud);
    assertEquals(report.getLastPeriodUemRewardAmount(), lastPeriodUemRewardAmount);
    assertEquals(report.getUemRewardAmount(), uemRewardAmount);
    assertEquals(report.getUpdatedDate(), updatedDate);
  }

  @Test
  void getReportsByRewardIdByPage() {
    when(reportRepository.findByRewardId(eq(rewardId), any())).thenReturn(new PageImpl<>(Arrays.asList(newHubReportEntity())));
    Page<HubReport> reports = hubReportService.getReportsByRewardId(rewardId, PAGEABLE);
    assertNotNull(reports);
    assertEquals(1, reports.getSize());
    HubReport report = reports.getContent().get(0);
    assertNotNull(report);
    assertEquals(report.getReportId(), reportId);
    assertEquals(report.getRewardId(), rewardId);
    assertEquals(report.getHubAddress(), StringUtils.lowerCase(hubAddress));
    assertEquals(report.getDeedManagerAddress(), StringUtils.lowerCase(deedManagerAddress));
    assertEquals(report.getOwnerAddress(), StringUtils.lowerCase(ownerAddress));
    assertEquals(report.getOwnerMintingPercentage(), ownerMintingPercentage);
    assertEquals(report.getDeedId(), deedId);
    assertEquals(report.getCity(), city);
    assertEquals(report.getCardType(), cardType);
    assertEquals(report.getMintingPower(), mintingPower);
    assertEquals(report.getMaxUsers(), maxUsers);
    assertEquals(report.getFromDate(), fromDate());
    assertEquals(report.getToDate(), toDate());
    assertEquals(report.getSentDate(), sentDate);
    assertEquals(report.getPeriodType(), periodType);
    assertEquals(report.getUsersCount(), usersCount);
    assertEquals(report.getParticipantsCount(), participantsCount);
    assertEquals(report.getRecipientsCount(), recipientsCount);
    assertEquals(report.getAchievementsCount(), achievementsCount);
    assertEquals(report.getRewardTokenAddress(), rewardTokenAddress);
    assertEquals(report.getRewardTokenNetworkId(), rewardTokenNetworkId);
    assertEquals(report.getTransactions(), transactions());
    assertEquals(report.getHubRewardAmount(), hubRewardAmount);
    assertEquals(report.getFixedRewardIndex(), fixedRewardIndex);
    assertEquals(report.getOwnerFixedIndex(), ownerFixedIndex);
    assertEquals(report.getTenantFixedIndex(), tenantFixedIndex);
    assertEquals(report.isFraud(), fraud);
    assertEquals(report.getLastPeriodUemRewardAmount(), lastPeriodUemRewardAmount);
    assertEquals(report.getUemRewardAmount(), uemRewardAmount);
    assertEquals(report.getUpdatedDate(), updatedDate);
  }

  @Test
  void getReportsByRewardId() {
    when(reportRepository.findByRewardId(eq(rewardId))).thenReturn(Stream.of(newHubReportEntity()));
    List<HubReport> reports = hubReportService.getReportsByRewardId(rewardId);
    assertNotNull(reports);
    assertEquals(1, reports.size());
    HubReport report = reports.get(0);
    assertNotNull(report);
    assertEquals(report.getReportId(), reportId);
    assertEquals(report.getRewardId(), rewardId);
    assertEquals(report.getHubAddress(), StringUtils.lowerCase(hubAddress));
    assertEquals(report.getDeedManagerAddress(), StringUtils.lowerCase(deedManagerAddress));
    assertEquals(report.getOwnerAddress(), StringUtils.lowerCase(ownerAddress));
    assertEquals(report.getOwnerMintingPercentage(), ownerMintingPercentage);
    assertEquals(report.getDeedId(), deedId);
    assertEquals(report.getCity(), city);
    assertEquals(report.getCardType(), cardType);
    assertEquals(report.getMintingPower(), mintingPower);
    assertEquals(report.getMaxUsers(), maxUsers);
    assertEquals(report.getFromDate(), fromDate());
    assertEquals(report.getToDate(), toDate());
    assertEquals(report.getSentDate(), sentDate);
    assertEquals(report.getPeriodType(), periodType);
    assertEquals(report.getUsersCount(), usersCount);
    assertEquals(report.getParticipantsCount(), participantsCount);
    assertEquals(report.getRecipientsCount(), recipientsCount);
    assertEquals(report.getAchievementsCount(), achievementsCount);
    assertEquals(report.getRewardTokenAddress(), rewardTokenAddress);
    assertEquals(report.getRewardTokenNetworkId(), rewardTokenNetworkId);
    assertEquals(report.getTransactions(), transactions());
    assertEquals(report.getHubRewardAmount(), hubRewardAmount);
    assertEquals(report.getFixedRewardIndex(), fixedRewardIndex);
    assertEquals(report.getOwnerFixedIndex(), ownerFixedIndex);
    assertEquals(report.getTenantFixedIndex(), tenantFixedIndex);
    assertEquals(report.isFraud(), fraud);
    assertEquals(report.getLastPeriodUemRewardAmount(), lastPeriodUemRewardAmount);
    assertEquals(report.getUemRewardAmount(), uemRewardAmount);
    assertEquals(report.getUpdatedDate(), updatedDate);
  }

  @Test
  void getAllReports() {
    when(reportRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Arrays.asList(newHubReportEntity())));
    Page<HubReport> reports = hubReportService.getReports(null, 0, PAGEABLE);
    assertNotNull(reports);
    assertEquals(1, reports.getSize());
    HubReport report = reports.getContent().get(0);
    assertNotNull(report);
    assertEquals(report.getReportId(), reportId);
    assertEquals(report.getRewardId(), rewardId);
    assertEquals(report.getHubAddress(), StringUtils.lowerCase(hubAddress));
    assertEquals(report.getDeedManagerAddress(), StringUtils.lowerCase(deedManagerAddress));
    assertEquals(report.getOwnerAddress(), StringUtils.lowerCase(ownerAddress));
    assertEquals(report.getOwnerMintingPercentage(), ownerMintingPercentage);
    assertEquals(report.getDeedId(), deedId);
    assertEquals(report.getCity(), city);
    assertEquals(report.getCardType(), cardType);
    assertEquals(report.getMintingPower(), mintingPower);
    assertEquals(report.getMaxUsers(), maxUsers);
    assertEquals(report.getFromDate(), fromDate());
    assertEquals(report.getToDate(), toDate());
    assertEquals(report.getSentDate(), sentDate);
    assertEquals(report.getPeriodType(), periodType);
    assertEquals(report.getUsersCount(), usersCount);
    assertEquals(report.getParticipantsCount(), participantsCount);
    assertEquals(report.getRecipientsCount(), recipientsCount);
    assertEquals(report.getAchievementsCount(), achievementsCount);
    assertEquals(report.getRewardTokenAddress(), rewardTokenAddress);
    assertEquals(report.getRewardTokenNetworkId(), rewardTokenNetworkId);
    assertEquals(report.getTransactions(), transactions());
    assertEquals(report.getHubRewardAmount(), hubRewardAmount);
    assertEquals(report.getFixedRewardIndex(), fixedRewardIndex);
    assertEquals(report.getOwnerFixedIndex(), ownerFixedIndex);
    assertEquals(report.getTenantFixedIndex(), tenantFixedIndex);
    assertEquals(report.isFraud(), fraud);
    assertEquals(report.getLastPeriodUemRewardAmount(), lastPeriodUemRewardAmount);
    assertEquals(report.getUemRewardAmount(), uemRewardAmount);
    assertEquals(report.getUpdatedDate(), updatedDate);
  }

  @Test
  void getReportsByHubAndRewardId() {
    when(reportRepository.findByRewardIdAndHubAddress(eq(rewardId), eq(StringUtils.lowerCase(hubAddress)), any(Pageable.class))).thenReturn(new PageImpl<>(Arrays.asList(newHubReportEntity())));
    Page<HubReport> reports = hubReportService.getReports(hubAddress, rewardId, PAGEABLE);
    assertNotNull(reports);
    assertEquals(1, reports.getSize());
    HubReport report = reports.getContent().get(0);
    assertNotNull(report);
    assertEquals(report.getReportId(), reportId);
    assertEquals(report.getRewardId(), rewardId);
    assertEquals(report.getHubAddress(), StringUtils.lowerCase(hubAddress));
    assertEquals(report.getDeedManagerAddress(), StringUtils.lowerCase(deedManagerAddress));
    assertEquals(report.getOwnerAddress(), StringUtils.lowerCase(ownerAddress));
    assertEquals(report.getOwnerMintingPercentage(), ownerMintingPercentage);
    assertEquals(report.getDeedId(), deedId);
    assertEquals(report.getCity(), city);
    assertEquals(report.getCardType(), cardType);
    assertEquals(report.getMintingPower(), mintingPower);
    assertEquals(report.getMaxUsers(), maxUsers);
    assertEquals(report.getFromDate(), fromDate());
    assertEquals(report.getToDate(), toDate());
    assertEquals(report.getSentDate(), sentDate);
    assertEquals(report.getPeriodType(), periodType);
    assertEquals(report.getUsersCount(), usersCount);
    assertEquals(report.getParticipantsCount(), participantsCount);
    assertEquals(report.getRecipientsCount(), recipientsCount);
    assertEquals(report.getAchievementsCount(), achievementsCount);
    assertEquals(report.getRewardTokenAddress(), rewardTokenAddress);
    assertEquals(report.getRewardTokenNetworkId(), rewardTokenNetworkId);
    assertEquals(report.getTransactions(), transactions());
    assertEquals(report.getHubRewardAmount(), hubRewardAmount);
    assertEquals(report.getFixedRewardIndex(), fixedRewardIndex);
    assertEquals(report.getOwnerFixedIndex(), ownerFixedIndex);
    assertEquals(report.getTenantFixedIndex(), tenantFixedIndex);
    assertEquals(report.isFraud(), fraud);
    assertEquals(report.getLastPeriodUemRewardAmount(), lastPeriodUemRewardAmount);
    assertEquals(report.getUemRewardAmount(), uemRewardAmount);
    assertEquals(report.getUpdatedDate(), updatedDate);
  }

  @Test
  void getReportByHubAndRewardId() {
    HubReport report = hubReportService.getReport(rewardId, hubAddress);
    assertNull(report);

    when(reportRepository.findByRewardIdAndHubAddress(eq(rewardId),
                                                      eq(StringUtils.lowerCase(hubAddress)))).thenReturn(Optional.of(newHubReportEntity()));
    report = hubReportService.getReport(rewardId, hubAddress);
    assertNotNull(report);
    assertEquals(report.getReportId(), reportId);
    assertEquals(report.getRewardId(), rewardId);
    assertEquals(report.getHubAddress(), StringUtils.lowerCase(hubAddress));
    assertEquals(report.getDeedManagerAddress(), StringUtils.lowerCase(deedManagerAddress));
    assertEquals(report.getOwnerAddress(), StringUtils.lowerCase(ownerAddress));
    assertEquals(report.getOwnerMintingPercentage(), ownerMintingPercentage);
    assertEquals(report.getDeedId(), deedId);
    assertEquals(report.getCity(), city);
    assertEquals(report.getCardType(), cardType);
    assertEquals(report.getMintingPower(), mintingPower);
    assertEquals(report.getMaxUsers(), maxUsers);
    assertEquals(report.getFromDate(), fromDate());
    assertEquals(report.getToDate(), toDate());
    assertEquals(report.getSentDate(), sentDate);
    assertEquals(report.getPeriodType(), periodType);
    assertEquals(report.getUsersCount(), usersCount);
    assertEquals(report.getParticipantsCount(), participantsCount);
    assertEquals(report.getRecipientsCount(), recipientsCount);
    assertEquals(report.getAchievementsCount(), achievementsCount);
    assertEquals(report.getRewardTokenAddress(), rewardTokenAddress);
    assertEquals(report.getRewardTokenNetworkId(), rewardTokenNetworkId);
    assertEquals(report.getTransactions(), transactions());
    assertEquals(report.getHubRewardAmount(), hubRewardAmount);
    assertEquals(report.getFixedRewardIndex(), fixedRewardIndex);
    assertEquals(report.getOwnerFixedIndex(), ownerFixedIndex);
    assertEquals(report.getTenantFixedIndex(), tenantFixedIndex);
    assertEquals(report.isFraud(), fraud);
    assertEquals(report.getLastPeriodUemRewardAmount(), lastPeriodUemRewardAmount);
    assertEquals(report.getUemRewardAmount(), uemRewardAmount);
    assertEquals(report.getUpdatedDate(), updatedDate);
  }

  @Test
  void getReportById() {
    when(reportRepository.findById(reportId)).thenReturn(Optional.of(newHubReportEntity()));
    HubReport report = hubReportService.getReport(reportId);
    assertNotNull(report);
    assertEquals(report.getReportId(), reportId);
    assertEquals(report.getRewardId(), rewardId);
    assertEquals(report.getHubAddress(), StringUtils.lowerCase(hubAddress));
    assertEquals(report.getDeedManagerAddress(), StringUtils.lowerCase(deedManagerAddress));
    assertEquals(report.getOwnerAddress(), StringUtils.lowerCase(ownerAddress));
    assertEquals(report.getOwnerMintingPercentage(), ownerMintingPercentage);
    assertEquals(report.getDeedId(), deedId);
    assertEquals(report.getCity(), city);
    assertEquals(report.getCardType(), cardType);
    assertEquals(report.getMintingPower(), mintingPower);
    assertEquals(report.getMaxUsers(), maxUsers);
    assertEquals(report.getFromDate(), fromDate());
    assertEquals(report.getToDate(), toDate());
    assertEquals(report.getSentDate(), sentDate);
    assertEquals(report.getPeriodType(), periodType);
    assertEquals(report.getUsersCount(), usersCount);
    assertEquals(report.getParticipantsCount(), participantsCount);
    assertEquals(report.getRecipientsCount(), recipientsCount);
    assertEquals(report.getAchievementsCount(), achievementsCount);
    assertEquals(report.getRewardTokenAddress(), rewardTokenAddress);
    assertEquals(report.getRewardTokenNetworkId(), rewardTokenNetworkId);
    assertEquals(report.getTransactions(), transactions());
    assertEquals(report.getHubRewardAmount(), hubRewardAmount);
    assertEquals(report.getFixedRewardIndex(), fixedRewardIndex);
    assertEquals(report.getOwnerFixedIndex(), ownerFixedIndex);
    assertEquals(report.getTenantFixedIndex(), tenantFixedIndex);
    assertEquals(report.isFraud(), fraud);
    assertEquals(report.getLastPeriodUemRewardAmount(), lastPeriodUemRewardAmount);
    assertEquals(report.getUemRewardAmount(), uemRewardAmount);
    assertEquals(report.getUpdatedDate(), updatedDate);
  }

  @Test
  void getReportByIdWhenNotFound() {
    when(blockchainService.retrieveReportProperties(reportId)).thenReturn(newHubReport());
    HubReport report = hubReportService.getReport(reportId);
    assertNotNull(report);
    assertEquals(report.getReportId(), reportId);
    assertEquals(report.getRewardId(), rewardId);
    assertEquals(report.getHubAddress(), StringUtils.lowerCase(hubAddress));
    assertEquals(report.getDeedManagerAddress(), StringUtils.lowerCase(deedManagerAddress));
    assertEquals(report.getOwnerAddress(), StringUtils.lowerCase(ownerAddress));
    assertEquals(report.getOwnerMintingPercentage(), ownerMintingPercentage);
    assertEquals(report.getDeedId(), deedId);
    assertEquals(report.getCity(), city);
    assertEquals(report.getCardType(), cardType);
    assertEquals(report.getMintingPower(), mintingPower);
    assertEquals(report.getMaxUsers(), maxUsers);
    assertEquals(report.getFromDate(), fromDate());
    assertEquals(report.getToDate(), toDate());
    assertEquals(report.getSentDate(), sentDate);
    assertEquals(report.getPeriodType(), periodType);
    assertEquals(report.getUsersCount(), usersCount);
    assertEquals(report.getParticipantsCount(), participantsCount);
    assertEquals(report.getRecipientsCount(), recipientsCount);
    assertEquals(report.getAchievementsCount(), achievementsCount);
    assertEquals(report.getRewardTokenAddress(), rewardTokenAddress);
    assertEquals(report.getRewardTokenNetworkId(), rewardTokenNetworkId);
    assertEquals(report.getTransactions(), transactions());
    assertEquals(report.getHubRewardAmount(), hubRewardAmount);
    assertEquals(report.getFixedRewardIndex(), fixedRewardIndex);
    assertEquals(report.getOwnerFixedIndex(), ownerFixedIndex);
    assertEquals(report.getTenantFixedIndex(), tenantFixedIndex);
    assertEquals(report.isFraud(), fraud);
    assertEquals(report.getLastPeriodUemRewardAmount(), lastPeriodUemRewardAmount);
    assertEquals(report.getUemRewardAmount(), uemRewardAmount);
    assertEquals(report.getUpdatedDate(), updatedDate);
    verify(listenerService).publishEvent(HUB_REPORT_SAVED, reportId);
  }

  @Test
  void saveReportWhenUnverified() {
    HubReportVerifiableData hubReportVerifiableData = newHubReportVerifiableData();
    hubReportVerifiableData.setSignature(hubReportVerifiableData.getSignature().replace("1", "2"));
    assertThrows(WomAuthorizationException.class, () -> hubReportService.saveReport(hubReportVerifiableData));
  }

  @Test
  void saveReport() throws WomException {
    HubReportVerifiableData hubReportVerifiableData = newHubReportVerifiableData();
    HubReport report = hubReportService.saveReport(hubReportVerifiableData);
    assertNotNull(report);
    verify(reportRepository).save(any());
    verify(listenerService).publishEvent(HUB_REPORT_SAVED, reportId);
  }

  @Test
  void refreshReportFraudWhenNotFound()  {
    when(reportRepository.findById(reportId)).thenReturn(Optional.empty());
    hubReportService.refreshReportFraud(reportId);
    verify(reportRepository, never()).save(any());
    verify(listenerService, never()).publishEvent(HUB_REPORT_SAVED, reportId);
  }

  @Test
  void refreshReportFraudWhenFalse() {
    HubReportEntity hubReportEntity = newHubReportEntity();
    when(reportRepository.findById(reportId)).thenReturn(Optional.of(hubReportEntity));
    hubReportService.refreshReportFraud(reportId);
    verify(reportRepository).save(any());
    verify(listenerService).publishEvent(HUB_REPORT_SAVED, reportId);
    assertFalse(hubReportEntity.isFraud());
  }

  @Test
  void refreshReportFraudWhenTrue() {
    HubReportEntity hubReportEntity = newHubReportEntity();
    when(reportRepository.findById(reportId)).thenReturn(Optional.of(hubReportEntity));
    when(blockchainService.isReportFraud(reportId)).thenReturn(true);
    hubReportService.refreshReportFraud(reportId);
    verify(reportRepository).save(any());
    verify(listenerService).publishEvent(HUB_REPORT_SAVED, reportId);
    assertTrue(hubReportEntity.isFraud());
  }

  @Test
  void computeUemRewardWhenFraud() {
    when(reportRepository.findById(reportId)).thenReturn(Optional.of(newHubReportEntity()));
    HubReport hubReport = newHubReport();
    hubReport.setFraud(true);
    hubReportService.computeUemReward(hubReport, 2d, fixedRewardIndex * 2);
    assertEquals(0, hubReport.getUemRewardAmount());
    verify(reportRepository).save(any());
  }

  @Test
  void computeUemReward() {
    when(reportRepository.findById(reportId)).thenReturn(Optional.of(newHubReportEntity()));
    HubReport hubReport = newHubReport();
    hubReportService.computeUemReward(hubReport, fixedRewardIndex * 2, 2d);
    assertEquals(1d, hubReport.getUemRewardAmount());
    verify(reportRepository).save(any());
  }

  private HubReportVerifiableData newHubReportVerifiableData() {
    HubReportPayload reportPayload = new HubReportPayload(reportId,
                                                          hubAddress,
                                                          deedId,
                                                          fromDate(),
                                                          toDate(),
                                                          sentDate,
                                                          periodType,
                                                          usersCount,
                                                          participantsCount,
                                                          recipientsCount,
                                                          achievementsCount,
                                                          rewardTokenAddress,
                                                          rewardTokenNetworkId,
                                                          hubRewardAmount,
                                                          transactions());
    String signature = signHubMessage(reportPayload.generateRawMessage(), hubCredentials.getEcKeyPair());
    String hash = Hash.sha3(signature);
    return new HubReportVerifiableData(hash, signature, reportPayload);
  }

  private HubReportEntity newHubReportEntity() {
    return new HubReportEntity(reportId,
                               rewardId,
                               StringUtils.lowerCase(hubAddress),
                               StringUtils.lowerCase(deedManagerAddress),
                               StringUtils.lowerCase(ownerAddress),
                               ownerMintingPercentage,
                               deedId,
                               city,
                               cardType,
                               mintingPower,
                               maxUsers,
                               fromDate(),
                               toDate(),
                               sentDate,
                               periodType,
                               usersCount,
                               participantsCount,
                               recipientsCount,
                               achievementsCount,
                               rewardTokenAddress,
                               rewardTokenNetworkId,
                               transactions(),
                               hubRewardAmount,
                               fixedRewardIndex,
                               ownerFixedIndex,
                               tenantFixedIndex,
                               fraud,
                               lastPeriodUemRewardAmount,
                               uemRewardAmount,
                               updatedDate);
  }

  private HubReport newHubReport() {
    return new HubReport(reportId,
                         StringUtils.lowerCase(hubAddress),
                         deedId,
                         fromDate(),
                         toDate(),
                         sentDate,
                         periodType,
                         usersCount,
                         participantsCount,
                         recipientsCount,
                         achievementsCount,
                         rewardTokenAddress,
                         rewardTokenNetworkId,
                         hubRewardAmount,
                         transactions(),
                         rewardId,
                         city,
                         cardType,
                         mintingPower,
                         maxUsers,
                         StringUtils.lowerCase(deedManagerAddress),
                         StringUtils.lowerCase(ownerAddress),
                         ownerMintingPercentage,
                         fixedRewardIndex,
                         ownerFixedIndex,
                         tenantFixedIndex,
                         fraud,
                         lastPeriodUemRewardAmount,
                         uemRewardAmount,
                         updatedDate);
  }

  private Instant toDate() {
    return Instant.ofEpochSecond(periodEndTime);
  }

  private Instant fromDate() {
    return Instant.ofEpochSecond(periodStartTime);
  }

  private String signHubMessage(String rawRequest, ECKeyPair ecKeyPair) {
    byte[] encodedRequest = rawRequest.getBytes(StandardCharsets.UTF_8);
    Sign.SignatureData signatureData = Sign.signPrefixedMessage(encodedRequest, ecKeyPair);
    byte[] retval = new byte[65];
    System.arraycopy(signatureData.getR(), 0, retval, 0, 32);
    System.arraycopy(signatureData.getS(), 0, retval, 32, 32);
    System.arraycopy(signatureData.getV(), 0, retval, 64, 1);
    return Numeric.toHexString(retval);
  }

  private TreeSet<String> transactions() {
    TreeSet<String> transactions = new TreeSet<>();
    transactions.add(txHash);
    return transactions;
  }

}
