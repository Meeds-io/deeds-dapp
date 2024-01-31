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
package io.meeds.deeds.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.web3j.EVMTest;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.EnsUtils;

import io.meeds.deeds.contract.TestUEM.ReportSentEventResponse;

@EVMTest
public class UserEngagementMintingTest extends BaseUEMContractTest {

  @BeforeEach
  @Override
  public void init(Web3j web3j,
                   TransactionManager transactionManager,
                   ContractGasProvider contractGasProvider) throws Exception {
    super.init(web3j, transactionManager, new StaticGasProvider(BigInteger.valueOf(41000000l), BigInteger.valueOf(9000000l)));
  }

  @Test
  public void setPeriodicRewardAmount() throws Exception {
    BigInteger periodicRewardAmount = uem.periodicRewardAmount().send();
    assertEquals(PERIODIC_REWARD_AMOUNT, periodicRewardAmount);
  }

  @Test
  public void meedTokenAddress() throws Exception {
    String meedAddress = uem.meed().send();
    assertEquals(meed.getContractAddress(), meedAddress);
  }

  @Test
  public void womAddress() throws Exception {
    String womAddress = uem.wom().send();
    assertEquals(wom.getContractAddress(), womAddress);
  }

  @Test
  public void startRewardsTime() throws Exception {
    BigInteger startRewardsTime = uem.startRewardsTime().send();
    assertEquals(START_REWARD_TIME, startRewardsTime);
  }

  @Test
  public void rewardPeriodicity() throws Exception {
    BigInteger rewardPeriodicity = uem.REWARD_PERIOD_IN_SECONDS().send();
    assertEquals(7 * 24l * 3600l, rewardPeriodicity.longValue());
  }

  @Test
  public void addReportOnlyHubCanSendUEMReport() throws Exception {
    connectHubs();
    setValidJoinDate(HUB1);

    assertThrows(Exception.class,
                 () -> loadUem(TENANT1_CREDENTIALS).addReport(newHubReport()).send(),
                 "uem.onlyHubCanSendUEMReport");
    assertThrows(Exception.class,
                 () -> loadUem(OWNER1_CREDENTIALS).addReport(newHubReport()).send(),
                 "uem.onlyHubCanSendUEMReport");

    TransactionReceipt receipt = loadUem(HUB1_CREDENTIALS).addReport(newHubReport()).send();
    checkReceipt(receipt);

    List<ReportSentEventResponse> reportSentEvents = TestUEM.getReportSentEvents(receipt);
    assertNotNull(reportSentEvents);
    assertEquals(1, reportSentEvents.size());
    ReportSentEventResponse reportSent = reportSentEvents.get(0);
    assertNotNull(reportSent);
    assertEquals(HUB1, reportSent.hub);
    assertEquals(uem.lastReportId().send(), reportSent.reportId);
    assertNotEquals(BigInteger.ZERO, reportSent.reportId);
  }

  @Test
  public void addReportHubIsNotConnectedToWoMUsingDeed() {
    assertThrows(Exception.class,
                 () -> loadUem(HUB1_CREDENTIALS).addReport(newHubReport()).send(),
                 "wom.hubIsNotConnectedToWoMUsingDeed");
  }

  @Test
  public void addReportHubUsersIsMandatory() throws Exception {
    connectHubs();
    setValidJoinDate(HUB1);

    assertThrows(Exception.class,
                 () -> loadUem(HUB1_CREDENTIALS).addReport(newHubReport(HUB1,
                                                                        BigInteger.ZERO,
                                                                        recipientsCount(DEED1),
                                                                        participantsCount(DEED1),
                                                                        achievementsCount(DEED1),
                                                                        amount(DEED1),
                                                                        FROM_DATE,
                                                                        TO_DATE))
                                                .send(),
                 "wom.hubUsersIsMandatory");
  }

  @Test
  public void addReportHubParticipantsCountIsMandatory() throws Exception {
    connectHubs();
    setValidJoinDate(HUB1);

    assertThrows(Exception.class,
                 () -> loadUem(HUB1_CREDENTIALS).addReport(newHubReport(HUB1,
                                                                        usersCount(DEED1),
                                                                        recipientsCount(DEED1),
                                                                        BigInteger.ZERO,
                                                                        achievementsCount(DEED1),
                                                                        amount(DEED1),
                                                                        FROM_DATE,
                                                                        TO_DATE))
                                                .send(),
                 "wom.hubParticipantsCountIsMandatory");
  }

  @Test
  public void addReportHubAchievementsCountIsMandatory() throws Exception {
    connectHubs();
    setValidJoinDate(HUB1);

    assertThrows(Exception.class,
                 () -> loadUem(HUB1_CREDENTIALS).addReport(newHubReport(HUB1,
                                                                        usersCount(DEED1),
                                                                        recipientsCount(DEED1),
                                                                        participantsCount(DEED1),
                                                                        BigInteger.ZERO,
                                                                        amount(DEED1),
                                                                        FROM_DATE,
                                                                        TO_DATE))
                                                .send(),
                 "wom.hubAchievementsCountIsMandatory");
  }

  @Test
  public void addReportHubUsedRewardAmountIsMandatory() throws Exception {
    connectHubs();
    setValidJoinDate(HUB1);

    assertThrows(Exception.class,
                 () -> loadUem(HUB1_CREDENTIALS).addReport(newHubReport(HUB1,
                                                                        usersCount(DEED1),
                                                                        recipientsCount(DEED1),
                                                                        participantsCount(DEED1),
                                                                        achievementsCount(DEED1),
                                                                        BigInteger.ZERO,
                                                                        FROM_DATE,
                                                                        TO_DATE))
                                                .send(),
                 "wom.hubUsedRewardAmountIsMandatory");
  }

  @Test
  public void addReportHubReportHasNotEligibleToDate() throws Exception {
    connectHubs();

    assertThrows(Exception.class,
                 () -> loadUem(HUB1_CREDENTIALS).addReport(newHubReport()).send(),
                 "wom.hubReportHasNotEligibleToDate");
  }

  @Test
  public void addReportWithJoinDateMinusOneWeek() throws Exception {
    connectHubs();

    setValidJoinDate(HUB1);

    long fromTime = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(2).toEpochSecond();
    long toTime = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(1).toEpochSecond();
    TransactionReceipt receipt = loadUem(HUB1_CREDENTIALS).addReport(newHubReport(HUB1,
                                                                                  usersCount(DEED1),
                                                                                  recipientsCount(DEED1),
                                                                                  participantsCount(DEED1),
                                                                                  achievementsCount(DEED1),
                                                                                  amount(DEED1),
                                                                                  fromTime,
                                                                                  toTime))
                                                          .send();
    checkReceipt(receipt);

    List<ReportSentEventResponse> reportSentEvents = TestUEM.getReportSentEvents(receipt);
    assertNotNull(reportSentEvents);
    assertEquals(1, reportSentEvents.size());
    ReportSentEventResponse reportSent = reportSentEvents.get(0);
    assertNotNull(reportSent);
    assertEquals(HUB1, reportSent.hub);
    assertEquals(uem.lastReportId().send(), reportSent.reportId);
    assertNotEquals(BigInteger.ZERO, reportSent.reportId);
  }

  @Test
  public void addReportHubAlreadySentReportInCurrentPeriod() throws Exception {
    connectHubs();

    setValidJoinDate(HUB1);

    long fromTime = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(5).toEpochSecond();
    long toTime = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(4).toEpochSecond();
    TransactionReceipt receipt = loadUem(HUB1_CREDENTIALS).addReport(newHubReport(HUB1,
                                                                                  usersCount(DEED1),
                                                                                  recipientsCount(DEED1),
                                                                                  participantsCount(DEED1),
                                                                                  achievementsCount(DEED1),
                                                                                  amount(DEED1),
                                                                                  fromTime,
                                                                                  toTime))
                                                          .send();
    checkReceipt(receipt);

    List<ReportSentEventResponse> reportSentEvents = TestUEM.getReportSentEvents(receipt);
    assertNotNull(reportSentEvents);
    assertEquals(1, reportSentEvents.size());
    ReportSentEventResponse reportSent = reportSentEvents.get(0);
    assertNotNull(reportSent);
    assertEquals(HUB1, reportSent.hub);
    assertEquals(uem.lastReportId().send(), reportSent.reportId);
    assertNotEquals(BigInteger.ZERO, reportSent.reportId);

    long fromTime2 = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(4).toEpochSecond();
    long toTime2 = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(3).toEpochSecond();

    assertThrows(Exception.class,
                 () -> loadUem(HUB1_CREDENTIALS).addReport(newHubReport(HUB1,
                                                                        usersCount(DEED1),
                                                                        recipientsCount(DEED1),
                                                                        participantsCount(DEED1),
                                                                        achievementsCount(DEED1),
                                                                        amount(DEED1),
                                                                        fromTime2,
                                                                        toTime2))
                                                .send(),
                 "wom.hubAlreadySentReportInCurrentPeriod");
  }

  @Test
  public void addReportDeedAlreadySentReportInCurrentPeriod() throws Exception {
    connectHubs();

    setValidJoinDate(HUB1);
    setValidJoinDate(HUB2);

    long fromTime = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(5).toEpochSecond();
    long toTime = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(4).toEpochSecond();
    TransactionReceipt receipt = loadUem(HUB1_CREDENTIALS).addReport(newHubReport(HUB1,
                                                                                  usersCount(DEED1),
                                                                                  recipientsCount(DEED1),
                                                                                  participantsCount(DEED1),
                                                                                  achievementsCount(DEED1),
                                                                                  amount(DEED1),
                                                                                  fromTime,
                                                                                  toTime))
                                                          .send();
    checkReceipt(receipt);

    List<ReportSentEventResponse> reportSentEvents = TestUEM.getReportSentEvents(receipt);
    assertNotNull(reportSentEvents);
    assertEquals(1, reportSentEvents.size());
    ReportSentEventResponse reportSent = reportSentEvents.get(0);
    assertNotNull(reportSent);
    assertEquals(HUB1, reportSent.hub);
    assertEquals(uem.lastReportId().send(), reportSent.reportId);
    assertNotEquals(BigInteger.ZERO, reportSent.reportId);

    bridgeDeedAndAutoConnect(DEED1, HUB2, OWNER2, TENANT2);
    checkHubConnected(DEED1, HUB2);

    long fromTime2 = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(4).toEpochSecond();
    long toTime2 = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(3).toEpochSecond();

    assertThrows(Exception.class,
                 () -> loadUem(HUB1_CREDENTIALS).addReport(newHubReport(HUB2,
                                                                        usersCount(DEED1),
                                                                        recipientsCount(DEED1),
                                                                        participantsCount(DEED1),
                                                                        achievementsCount(DEED1),
                                                                        amount(DEED1),
                                                                        fromTime2,
                                                                        toTime2))
                                                .send(),
                 "wom.deedAlreadySentReportInCurrentPeriod");

    // Test Should allow Hub2 to send report if switched again to DEED2
    bridgeDeedAndAutoConnect(DEED2, HUB2, OWNER2, TENANT2);
    checkHubConnected(DEED2, HUB2);
    receipt = loadUem(HUB2_CREDENTIALS).addReport(newHubReport(HUB2,
                                                               usersCount(DEED2),
                                                               recipientsCount(DEED2),
                                                               participantsCount(DEED2),
                                                               achievementsCount(DEED2),
                                                               amount(DEED2),
                                                               fromTime2,
                                                               toTime2))
                                       .send();
    checkReceipt(receipt);

    reportSentEvents = TestUEM.getReportSentEvents(receipt);
    assertNotNull(reportSentEvents);
    assertEquals(1, reportSentEvents.size());
    reportSent = reportSentEvents.get(0);
    assertNotNull(reportSent);
    assertEquals(HUB2, reportSent.hub);
    assertEquals(uem.lastReportId().send(), reportSent.reportId);
    assertNotEquals(BigInteger.ZERO, reportSent.reportId);
  }

  @Test
  public void addReportLastReportFromDateMustBeLessThanCurrentReportFromDate() throws Exception {
    connectHubs();

    setValidJoinDate(HUB1);
    setValidJoinDate(HUB2);

    moveBackCurrentPeriodInWeeks(2);
    long fromTime = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(4).toEpochSecond();
    long toTime = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(3).toEpochSecond();
    TransactionReceipt receipt = loadUem(HUB1_CREDENTIALS).addReport(newHubReport(HUB1,
                                                                                  usersCount(DEED1),
                                                                                  recipientsCount(DEED1),
                                                                                  participantsCount(DEED1),
                                                                                  achievementsCount(DEED1),
                                                                                  amount(DEED1),
                                                                                  fromTime,
                                                                                  toTime))
                                                          .send();
    checkReceipt(receipt);

    List<ReportSentEventResponse> reportSentEvents = TestUEM.getReportSentEvents(receipt);
    assertNotNull(reportSentEvents);
    assertEquals(1, reportSentEvents.size());
    ReportSentEventResponse reportSent = reportSentEvents.get(0);
    assertNotNull(reportSent);
    assertEquals(HUB1, reportSent.hub);
    assertEquals(uem.lastReportId().send(), reportSent.reportId);
    assertNotEquals(BigInteger.ZERO, reportSent.reportId);

    moveBackCurrentPeriodInWeeks(0);

    long fromTime2 = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(5).toEpochSecond();
    long toTime2 = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(4).toEpochSecond();
    assertThrows(Exception.class,
                 () -> loadUem(HUB1_CREDENTIALS).addReport(newHubReport(HUB1,
                                                                        usersCount(DEED1),
                                                                        recipientsCount(DEED1),
                                                                        participantsCount(DEED1),
                                                                        achievementsCount(DEED1),
                                                                        amount(DEED1),
                                                                        fromTime2,
                                                                        toTime2))
                                                .send(),
                 "wom.lastReportFromDateMustBeLessThanCurrentReportFromDate");

    receipt = loadUem(HUB1_CREDENTIALS).addReport(newHubReport())
                                       .send();

    reportSentEvents = TestUEM.getReportSentEvents(receipt);
    assertNotNull(reportSentEvents);
    assertEquals(1, reportSentEvents.size());
    reportSent = reportSentEvents.get(0);
    assertNotNull(reportSent);
    assertEquals(HUB1, reportSent.hub);
    assertEquals(uem.lastReportId().send(), reportSent.reportId);
    assertNotEquals(BigInteger.ZERO, reportSent.reportId);
  }

  @Test
  public void addReportPropertiesCheck() throws Exception {
    connectHubs();

    setValidJoinDate(HUB1);
    setValidJoinDate(HUB2);
    setValidJoinDate(HUB3);

    long fromTime = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(4).toEpochSecond();
    long toTime = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(3).toEpochSecond();
    BigInteger reportId1 = addReportAndCheckProperties(HUB1_CREDENTIALS, HUB1, DEED1, OWNER1, TENANT1, fromTime, toTime);
    BigInteger reportId2 = addReportAndCheckProperties(HUB2_CREDENTIALS, HUB2, DEED2, OWNER2, TENANT2, fromTime, toTime);
    BigInteger reportId3 = addReportAndCheckProperties(HUB3_CREDENTIALS, HUB3, DEED3, OWNER3, TENANT3, fromTime, toTime);

    RewardPeriod rewardPeriod = getRewardPeriodByReportId(reportId1);
    assertEquals(PERIODIC_REWARD_AMOUNT, rewardPeriod.amount);
    assertEquals(3, rewardPeriod.reportsCount.longValue());
    assertEquals(reportId1, rewardPeriod.fromReport);
    assertEquals(reportId3, rewardPeriod.toReport);
    assertEquals(getFixedRewardIndex(reportId1)
                                               .add(getFixedRewardIndex(reportId2))
                                               .add(getFixedRewardIndex(reportId3)),
                 rewardPeriod.fixedGlobalIndex);
    assertEquals(ZonedDateTime.now()
                              .with(DayOfWeek.MONDAY)
                              .toLocalDate()
                              .atStartOfDay(ZoneOffset.UTC)
                              .toEpochSecond(),
                 rewardPeriod.fromDate.longValue());
    assertEquals(ZonedDateTime.now()
                              .with(DayOfWeek.MONDAY)
                              .plusWeeks(1)
                              .toLocalDate()
                              .atStartOfDay(ZoneOffset.UTC)
                              .toEpochSecond(),
                 rewardPeriod.toDate.longValue());
  }

  @Test
  public void pendingRewardBalanceOf() throws Exception {
    connectHubs();

    setValidJoinDate(HUB1);
    setValidJoinDate(HUB2);
    setValidJoinDate(HUB3);

    moveBackCurrentPeriodInWeeks(2);

    long fromTime = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(4).toEpochSecond();
    long toTime = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(3).toEpochSecond();

    BigInteger reportId1 = addReport(HUB1_CREDENTIALS, HUB1, DEED1, fromTime, toTime);
    BigInteger reportId2 = addReport(HUB2_CREDENTIALS, HUB2, DEED2, fromTime, toTime);
    BigInteger reportId3 = addReport(HUB3_CREDENTIALS, HUB3, DEED3, fromTime, toTime);

    moveBackCurrentPeriodInWeeks(1);

    BigInteger owner1PendingReward = uem.pendingRewardBalanceOf(OWNER1).send();
    BigInteger tenant1PendingReward = uem.pendingRewardBalanceOf(TENANT1).send();
    BigInteger owner2PendingReward = uem.pendingRewardBalanceOf(OWNER2).send();
    BigInteger tenant2PendingReward = uem.pendingRewardBalanceOf(TENANT2).send();
    BigInteger owner3PendingReward = uem.pendingRewardBalanceOf(OWNER3).send();
    BigInteger tenant3PendingReward = uem.pendingRewardBalanceOf(TENANT3).send();

    RewardPeriod rewardPeriod = getRewardPeriodByReportId(reportId1);
    HubReportReward reportReward1 = getHubReportRewardById(reportId1);
    HubReportReward reportReward2 = getHubReportRewardById(reportId2);
    HubReportReward reportReward3 = getHubReportRewardById(reportId3);
    assertEquals(PERIODIC_REWARD_AMOUNT, rewardPeriod.amount);
    assertEquals(rewardPeriod.amount.divide(BigInteger.valueOf(100)),
                 new BigDecimal(owner1PendingReward.add(tenant1PendingReward)
                                                   .add(owner2PendingReward)
                                                   .add(tenant2PendingReward)
                                                   .add(owner3PendingReward)
                                                   .add(tenant3PendingReward))
                                                                              .divide(BigDecimal.valueOf(100),
                                                                                      MathContext.DECIMAL128)
                                                                              .setScale(0, RoundingMode.HALF_EVEN)
                                                                              .toBigInteger());
    assertEquals(new BigDecimal(rewardPeriod.amount).multiply(new BigDecimal(reportReward1.fixedRewardIndex))
                                                    .divide(new BigDecimal(rewardPeriod.fixedGlobalIndex),
                                                            MathContext.DECIMAL128)
                                                    .divide(BigDecimal.valueOf(100))
                                                    .setScale(0, RoundingMode.DOWN)
                                                    .toBigInteger(),
                 owner1PendingReward.add(tenant1PendingReward).divide(BigInteger.valueOf(100)));
    assertEquals(new BigDecimal(rewardPeriod.amount).multiply(new BigDecimal(reportReward2.fixedRewardIndex))
                                                    .divide(new BigDecimal(rewardPeriod.fixedGlobalIndex),
                                                            MathContext.DECIMAL128)
                                                    .divide(BigDecimal.valueOf(100))
                                                    .setScale(0, RoundingMode.DOWN)
                                                    .toBigInteger(),
                 owner2PendingReward.add(tenant2PendingReward).divide(BigInteger.valueOf(100)));
    assertEquals(new BigDecimal(rewardPeriod.amount).multiply(new BigDecimal(reportReward3.fixedRewardIndex))
                                                    .divide(new BigDecimal(rewardPeriod.fixedGlobalIndex),
                                                            MathContext.DECIMAL128)
                                                    .divide(BigDecimal.valueOf(100))
                                                    .setScale(0, RoundingMode.DOWN)
                                                    .toBigInteger(),
                 owner3PendingReward.add(tenant3PendingReward).divide(BigInteger.valueOf(100)));
  }

  @Test
  public void claim() throws Exception {
    connectHubs();

    setValidJoinDate(HUB1);
    setValidJoinDate(HUB2);
    setValidJoinDate(HUB3);

    sendMeedsToUem();

    long fromTime = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(5).toEpochSecond();
    long toTime = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(4).toEpochSecond();

    // Move to 3 Weeks Before current period
    moveBackCurrentPeriodInWeeks(3);

    // Send reports 3 Weeks Before current period
    BigInteger report1Id1 = addReport(HUB1_CREDENTIALS, HUB1, DEED1, fromTime, toTime);
    BigInteger report2Id1 = addReport(HUB2_CREDENTIALS, HUB2, DEED2, fromTime, toTime);
    BigInteger report3Id1 = addReport(HUB3_CREDENTIALS, HUB3, DEED3, fromTime, toTime);

    RewardPeriod rewardPeriod = getRewardPeriodByReportId(report1Id1);
    assertEquals(PERIODIC_REWARD_AMOUNT, rewardPeriod.amount);

    // Claim (Should be 0 claimed, period not finished yet)
    claimAndCheck(OWNER1_CREDENTIALS,
                  OWNER1,
                  BigInteger.ZERO,
                  BigInteger.ZERO,
                  BigInteger.ZERO,
                  BigInteger.ZERO,
                  0,
                  1,
                  report1Id1);
    claimAndCheck(TENANT1_CREDENTIALS,
                  TENANT1,
                  BigInteger.ZERO,
                  BigInteger.ZERO,
                  BigInteger.ZERO,
                  BigInteger.ZERO,
                  0,
                  1,
                  report1Id1);
    claimAndCheck(OWNER2_CREDENTIALS,
                  OWNER2,
                  BigInteger.ZERO,
                  BigInteger.ZERO,
                  BigInteger.ZERO,
                  BigInteger.ZERO,
                  0,
                  1,
                  report2Id1);
    claimAndCheck(TENANT2_CREDENTIALS,
                  TENANT2,
                  BigInteger.ZERO,
                  BigInteger.ZERO,
                  BigInteger.ZERO,
                  BigInteger.ZERO,
                  0,
                  1,
                  report2Id1);
    claimAndCheck(OWNER3_CREDENTIALS,
                  OWNER3,
                  BigInteger.ZERO,
                  BigInteger.ZERO,
                  BigInteger.ZERO,
                  BigInteger.ZERO,
                  0,
                  0, // Owner Percentage == 0, thus no rewards received
                  BigInteger.ZERO);
    claimAndCheck(TENANT3_CREDENTIALS,
                  TENANT3,
                  BigInteger.ZERO,
                  BigInteger.ZERO,
                  BigInteger.ZERO,
                  BigInteger.ZERO,
                  0,
                  1,
                  report3Id1);

    // Move to 2 Weeks Before current period
    moveBackCurrentPeriodInWeeks(2);

    // Rewards of 3 Weeks Before current period should become claimable
    BigInteger owner1Report1Earnings = computeRewardByReportAndRecipientType(report1Id1, true);
    BigInteger tenant1Report1Earnings = computeRewardByReportAndRecipientType(report1Id1, false);
    BigInteger owner2Report1Earnings = computeRewardByReportAndRecipientType(report2Id1, true);
    BigInteger tenant2Report1Earnings = computeRewardByReportAndRecipientType(report2Id1, false);
    BigInteger owner3Report1Earnings = computeRewardByReportAndRecipientType(report3Id1, true);
    BigInteger tenant3Report1Earnings = computeRewardByReportAndRecipientType(report3Id1, false);

    assertThrows(Exception.class,
                 () -> loadUem(OWNER1_CREDENTIALS).claim(EnsUtils.EMPTY_ADDRESS,
                                                         owner1Report1Earnings
                                                                              .add(BigInteger.ONE)) // +0.000000000000000001
                                                                                                    // Meed
                                                  .send(),
                 "Shouldn't be able to claim more than what we earn");

    // Claim rewards of report sent 3 Weeks Before current period - (minus)
    // 0.000000000000000001 Meed
    claimAndCheck(OWNER1_CREDENTIALS,
                  OWNER1,
                  owner1Report1Earnings.subtract(BigInteger.ONE),
                  owner1Report1Earnings.subtract(BigInteger.ONE),
                  owner1Report1Earnings.subtract(BigInteger.ONE),
                  owner1Report1Earnings,
                  1,
                  1,
                  report1Id1);

    // Claim all rewards of report sent 3 Weeks Before current period
    claimAndCheck(OWNER1_CREDENTIALS,
                  EnsUtils.EMPTY_ADDRESS,
                  BigInteger.ZERO,
                  BigInteger.ONE,
                  owner1Report1Earnings,
                  owner1Report1Earnings,
                  1,
                  1,
                  report1Id1);

    // test when claim again, nothing should be sent
    claimAndCheck(OWNER1_CREDENTIALS,
                  OWNER1,
                  BigInteger.ZERO,
                  BigInteger.ZERO,
                  owner1Report1Earnings,
                  owner1Report1Earnings,
                  1,
                  1,
                  report1Id1);

    // Send reports 2 Week Before current period
    fromTime = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(4).toEpochSecond();
    toTime = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(3).toEpochSecond();

    BigInteger report1Id2 = addReport(HUB1_CREDENTIALS, HUB1, DEED1, fromTime, toTime);
    BigInteger report2Id2 = addReport(HUB2_CREDENTIALS, HUB2, DEED2, fromTime, toTime);
    BigInteger report3Id2 = addReport(HUB3_CREDENTIALS, HUB3, DEED3, fromTime, toTime);

    checkPreviouslyRewardedAmount(owner1Report1Earnings, report1Id2, DEED1, true);
    checkPreviouslyRewardedAmount(owner2Report1Earnings, report2Id2, DEED2, true);
    checkPreviouslyRewardedAmount(tenant3Report1Earnings, report3Id2, DEED3, false);

    // Move to 1 week Before current period
    moveBackCurrentPeriodInWeeks(1);

    BigInteger owner1Report2Earnings = computeRewardByReportAndRecipientType(report1Id2, true);
    BigInteger tenant1Report2Earnings = computeRewardByReportAndRecipientType(report1Id2, false);
    BigInteger owner2Report2Earnings = computeRewardByReportAndRecipientType(report2Id2, true);
    BigInteger tenant2Report2Earnings = computeRewardByReportAndRecipientType(report2Id2, false);
    BigInteger owner3Report2Earnings = computeRewardByReportAndRecipientType(report3Id2, true);
    BigInteger tenant3Report2Earnings = computeRewardByReportAndRecipientType(report3Id2, false);

    // Send reports in current period that shouldn't be claimable
    fromTime = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(3).toEpochSecond();
    toTime = ZonedDateTime.now().with(DayOfWeek.MONDAY).minusWeeks(2).toEpochSecond();
    BigInteger report1Id3 = addReport(HUB1_CREDENTIALS, HUB1, DEED1, fromTime, toTime);
    BigInteger report2Id3 = addReport(HUB2_CREDENTIALS, HUB2, DEED2, fromTime, toTime);
    BigInteger report3Id3 = addReport(HUB3_CREDENTIALS, HUB3, DEED3, fromTime, toTime);

    checkPreviouslyRewardedAmount(owner1Report2Earnings, report1Id3, DEED1, true);
    checkPreviouslyRewardedAmount(owner2Report2Earnings, report2Id3, DEED2, true);
    checkPreviouslyRewardedAmount(tenant3Report2Earnings, report3Id3, DEED3, false);

    claimAndCheck(OWNER1_CREDENTIALS,
                  OWNER1,
                  owner1Report2Earnings,
                  owner1Report2Earnings,
                  owner1Report1Earnings.add(owner1Report2Earnings),
                  owner1Report1Earnings.add(owner1Report2Earnings),
                  2,
                  3,
                  report1Id3);

    // Claim All for others
    claimAndCheck(TENANT1_CREDENTIALS,
                  EnsUtils.EMPTY_ADDRESS,
                  BigInteger.ZERO,
                  tenant1Report1Earnings.add(tenant1Report2Earnings),
                  tenant1Report1Earnings.add(tenant1Report2Earnings),
                  tenant1Report1Earnings.add(tenant1Report2Earnings),
                  2,
                  3,
                  report1Id3);

    claimAndCheck(OWNER2_CREDENTIALS,
                  EnsUtils.EMPTY_ADDRESS,
                  owner2Report1Earnings,
                  owner2Report1Earnings,
                  owner2Report1Earnings,
                  owner2Report1Earnings.add(owner2Report2Earnings),
                  2,
                  3,
                  report2Id3);
    claimAndCheck(OWNER2_CREDENTIALS,
                  EnsUtils.EMPTY_ADDRESS,
                  BigInteger.TWO,
                  BigInteger.TWO,
                  owner2Report1Earnings.add(BigInteger.TWO),
                  owner2Report1Earnings.add(owner2Report2Earnings),
                  2,
                  3,
                  report2Id3);
    claimAndCheck(OWNER2_CREDENTIALS,
                  EnsUtils.EMPTY_ADDRESS,
                  BigInteger.TEN,
                  BigInteger.TEN,
                  owner2Report1Earnings.add(BigInteger.TWO).add(BigInteger.TEN),
                  owner2Report1Earnings.add(owner2Report2Earnings),
                  2,
                  3,
                  report2Id3);
    claimAndCheck(OWNER2_CREDENTIALS,
                  EnsUtils.EMPTY_ADDRESS,
                  BigInteger.ZERO,
                  owner2Report2Earnings.subtract(BigInteger.TWO).subtract(BigInteger.TEN),
                  owner2Report1Earnings.add(owner2Report2Earnings),
                  owner2Report1Earnings.add(owner2Report2Earnings),
                  2,
                  3,
                  report2Id3);

    claimAndCheck(TENANT2_CREDENTIALS,
                  EnsUtils.EMPTY_ADDRESS,
                  BigInteger.ZERO,
                  tenant2Report1Earnings.add(tenant2Report2Earnings),
                  tenant2Report1Earnings.add(tenant2Report2Earnings),
                  tenant2Report1Earnings.add(tenant2Report2Earnings),
                  2,
                  3,
                  report2Id3);

    claimAndCheck(OWNER3_CREDENTIALS,
                  EnsUtils.EMPTY_ADDRESS,
                  BigInteger.ZERO,
                  owner3Report1Earnings.add(owner3Report2Earnings),
                  owner3Report1Earnings.add(owner3Report2Earnings),
                  owner3Report1Earnings.add(owner3Report2Earnings),
                  0,
                  0, // Owner Percentage == 0, thus no rewards received
                  BigInteger.ZERO);

    claimAndCheck(TENANT3_CREDENTIALS,
                  EnsUtils.EMPTY_ADDRESS,
                  BigInteger.ZERO,
                  tenant3Report1Earnings.add(tenant3Report2Earnings),
                  tenant3Report1Earnings.add(tenant3Report2Earnings),
                  tenant3Report1Earnings.add(tenant3Report2Earnings),
                  2,
                  3,
                  report3Id3);

    BigInteger totalRewardsSent = owner1Report1Earnings
                                                       .add(owner1Report2Earnings)
                                                       .add(tenant1Report1Earnings)
                                                       .add(tenant1Report2Earnings)
                                                       .add(owner2Report1Earnings)
                                                       .add(owner2Report2Earnings)
                                                       .add(tenant2Report1Earnings)
                                                       .add(tenant2Report2Earnings)
                                                       .add(owner3Report1Earnings)
                                                       .add(owner3Report2Earnings)
                                                       .add(tenant3Report1Earnings)
                                                       .add(tenant3Report2Earnings);

    // Two period rewards sent only (compare with 17 decimals instead of 18 for
    // diff between Solidity and Java)
    assertEquals(new BigDecimal(PERIODIC_REWARD_AMOUNT.multiply(BigInteger.TWO)).divide(BigDecimal.TEN).toBigInteger(),
                 new BigDecimal(totalRewardsSent).divide(BigDecimal.TEN).setScale(0, RoundingMode.HALF_EVEN).toBigInteger());
  }

}
