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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.tuweni.bytes.Bytes;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple10;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.tuples.generated.Tuple7;
import org.web3j.tuples.generated.Tuple9;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.EnsUtils;

import io.meeds.deeds.contract.TestUEM.ClaimedEventResponse;
import io.meeds.deeds.contract.TestUEM.HubReport;
import io.meeds.deeds.contract.TestUEM.ReportSentEventResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public abstract class BaseUEMContractTest extends BaseWoMContractTest {

  protected static final String     TOKEN_ADDRESS          = "0x0143b71443650AA8eFA76BD82F35c22EBD558090";

  protected static final BigInteger TOKEN_CHAIN_ID         = BigInteger.valueOf(80001);

  protected static final long       FROM_DATE              = ZonedDateTime.now().minusWeeks(2).toEpochSecond();

  protected static final long       TO_DATE                = ZonedDateTime.now().minusWeeks(1).toEpochSecond();

  protected static final long       START_REWARD_TIME_SEC  = 1672617600l;

  protected static final BigInteger PERIODIC_REWARD_AMOUNT = BigInteger.valueOf(5).multiply(BigInteger.valueOf(10).pow(18));

  protected static final BigInteger START_REWARD_TIME      = BigInteger.valueOf(START_REWARD_TIME_SEC);

  protected MeedsToken              meed;

  protected TestUEM                 uem;

  @Override
  public void init(Web3j web3j,
                   TransactionManager transactionManager,
                   ContractGasProvider contractGasProvider) throws Exception {
    super.init(web3j, transactionManager, contractGasProvider);
    meed = MeedsToken.deploy(web3j, transactionManager, contractGasProvider).send();
    this.uem = deployUem(web3j, transactionManager, contractGasProvider);
    this.uem.setPeriodicRewardAmount(PERIODIC_REWARD_AMOUNT).send();
  }

  protected void sendMeedsToUem() throws Exception {
    meed.mint(this.uem.getContractAddress(), BigInteger.valueOf(10).pow(22)).send();
  }

  protected void connectHubs() throws Exception {
    sendGasFeesForAll();
    bridgeDeed(DEED1, OWNER1, TENANT1);
    bridgeDeed(DEED2, OWNER2, TENANT2);
    bridgeDeed(DEED3, OWNER3, TENANT3);

    connectHub(TENANT1_CREDENTIALS, HUB1, DEED1);
    connectHub(TENANT2_CREDENTIALS, HUB2, DEED2);
    connectHub(TENANT3_CREDENTIALS, HUB3, DEED3);
  }

  protected void setValidJoinDate(String hubAddress) throws Exception {
    setValidJoinDate(hubAddress, START_REWARD_TIME_SEC);
  }

  protected void setValidJoinDate(String hubAddress, long timeInMs) throws Exception {
    wom.setJoinDate(hubAddress, BigInteger.valueOf(timeInMs)).send();
  }

  protected void moveBackCurrentPeriodInWeeks(int weeks) throws Exception {
    uem.setTestOnPreviousPeriod(BigInteger.valueOf(weeks)).send();
  }

  protected static HubReport newHubReport() {
    return newHubReport(HUB1,
                        usersCount(DEED1),
                        recipientsCount(DEED1),
                        participantsCount(DEED1),
                        achievementsCount(DEED1),
                        amount(DEED1),
                        FROM_DATE,
                        TO_DATE);
  }

  protected static HubReport newHubReport(String hub, // NOSONAR
                                          BigInteger usersCount,
                                          BigInteger recipientsCount,
                                          BigInteger participantsCount,
                                          BigInteger achievementsCount,
                                          BigInteger amount,
                                          long fromDate,
                                          long toDate) {
    return new HubReport(hub,
                         usersCount,
                         recipientsCount,
                         participantsCount,
                         achievementsCount,
                         amount,
                         TOKEN_ADDRESS,
                         TOKEN_CHAIN_ID,
                         BigInteger.valueOf(fromDate),
                         BigInteger.valueOf(toDate));
  }

  protected static HubReport newHubReport(String hub, // NOSONAR
                                          long usersCount,
                                          long recipientsCount,
                                          long participantsCount,
                                          long achievementsCount,
                                          long amount,
                                          long fromDate,
                                          long toDate) {
    return new HubReport(hub,
                         BigInteger.valueOf(usersCount),
                         BigInteger.valueOf(recipientsCount),
                         BigInteger.valueOf(participantsCount),
                         BigInteger.valueOf(achievementsCount),
                         BigInteger.valueOf(amount),
                         TOKEN_ADDRESS,
                         TOKEN_CHAIN_ID,
                         BigInteger.valueOf(fromDate),
                         BigInteger.valueOf(toDate));
  }

  protected BigInteger getFixedRewardIndex(BigInteger reportId) throws Exception {
    return getHubReportRewardById(reportId).fixedRewardIndex;
  }

  protected RewardPeriod getRewardPeriodByReportId(BigInteger reportId) throws Exception {
    BigInteger rewardPeriodId = getHubReportRewardById(reportId).rewardPeriodId;
    Tuple7<BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger> tuple = uem.rewards(rewardPeriodId)
                                                                                                          .send();
    return new RewardPeriod(tuple.component1(),
                            tuple.component2(),
                            tuple.component3(),
                            tuple.component4(),
                            tuple.component5(),
                            tuple.component6(),
                            tuple.component7());
  }

  protected BigInteger addReportAndCheckProperties(Credentials credentials,
                                                   String hubAddress,
                                                   BigInteger deedId,
                                                   String ownerAddress,
                                                   String tenantAddress,
                                                   long fromTime,
                                                   long toTime) throws Exception {
    BigInteger reportId = addReport(credentials, hubAddress, deedId, fromTime, toTime);
    HubReport hubReport = getHubReportById(reportId);
    assertEquals(hubAddress.replace("0x", "").toLowerCase(), hubReport.hub.replace("0x", "").toLowerCase());
    assertEquals(usersCount(deedId), hubReport.usersCount);
    assertEquals(recipientsCount(deedId), hubReport.recipientsCount);
    assertEquals(participantsCount(deedId), hubReport.participantsCount);
    assertEquals(achievementsCount(deedId), hubReport.achievementsCount);
    assertEquals(amount(deedId), hubReport.amount);
    assertEquals(fromTime, hubReport.fromDate.longValue());
    assertEquals(toTime, hubReport.toDate.longValue());

    HubReportDeed hubDeed = getHubReportDeedById(reportId);
    assertEquals(deedId, hubDeed.deedId);
    assertEquals(maxUsers(deedId), hubDeed.maxUsers);
    assertEquals(mintingPower(deedId), hubDeed.mintingPower);
    assertEquals(cardType(deedId), hubDeed.cardType);
    assertEquals(CITY, hubDeed.city);

    HubReportReward hubReward = getHubReportRewardById(reportId);
    assertFalse(hubReward.fraud);
    assertTrue(hubReward.rewardPeriodId.longValue() > 0);
    long currentRewardId = (now() - START_REWARD_TIME_SEC) / 604800l;
    assertEquals(currentRewardId, hubReward.rewardPeriodId.longValue());
    assertEquals(ownerAddress.replace("0x", "").toLowerCase(), hubReward.owner.replace("0x", "").toLowerCase());
    assertEquals(tenantAddress.replace("0x", "").toLowerCase(), hubReward.tenant.replace("0x", "").toLowerCase());
    assertTrue(now() - hubReward.sentDate.longValue() <= 15);
    assertEquals(BigInteger.ZERO, hubReward.lastRewardedAmount);
    assertEquals(computeFixedRewardIndex(hubReport, hubReward, hubDeed), hubReward.fixedRewardIndex);
    assertEquals(hubReward.fixedRewardIndex.subtract(hubReward.ownerFixedIndex).divide(BigInteger.valueOf(10)),
                 hubReward.tenantFixedIndex.divide(BigInteger.valueOf(10)));
    assertEquals(BigInteger.valueOf(100).subtract(ownerPercentage(deedId)),
                 new BigDecimal(hubReward.tenantFixedIndex.multiply(BigInteger.valueOf(100))).divide(new BigDecimal(hubReward.fixedRewardIndex),
                                                                                                     MathContext.DECIMAL128)
                                                                                             .setScale(5, RoundingMode.HALF_UP)
                                                                                             .toBigInteger());
    assertEquals(ownerPercentage(deedId),
                 new BigDecimal(hubReward.ownerFixedIndex.multiply(BigInteger.valueOf(100))).divide(new BigDecimal(hubReward.fixedRewardIndex),
                                                                                                    MathContext.DECIMAL128)
                                                                                            .setScale(5, RoundingMode.HALF_UP)
                                                                                            .toBigInteger());
    return reportId;
  }

  protected BigInteger addReport(Credentials credentials,
                                 String hubAddress,
                                 BigInteger deedId,
                                 long fromTime,
                                 long toTime) throws Exception {
    TransactionReceipt receipt = loadUem(credentials).addReport(newHubReport(hubAddress,
                                                                             usersCount(deedId),
                                                                             recipientsCount(deedId),
                                                                             participantsCount(deedId),
                                                                             achievementsCount(deedId),
                                                                             amount(deedId),
                                                                             fromTime,
                                                                             toTime))
                                                     .send();
    checkReceipt(receipt);

    List<ReportSentEventResponse> reportSentEvents = TestUEM.getReportSentEvents(receipt);
    assertNotNull(reportSentEvents);
    assertEquals(1, reportSentEvents.size());
    ReportSentEventResponse reportSent = reportSentEvents.get(0);
    assertNotNull(reportSent);
    assertEquals(hubAddress, reportSent.hub);
    assertEquals(uem.lastReportId().send(), reportSent.reportId);
    assertNotEquals(BigInteger.ZERO, reportSent.reportId);
    return reportSent.reportId;
  }

  protected HubReport getHubReportById(BigInteger reportId) throws Exception {
    Tuple10<String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, String, BigInteger, BigInteger, BigInteger> report =
                                                                                                                                   uem.hubReports(reportId)
                                                                                                                                      .send();
    return new HubReport(report.component1(),
                         report.component2(),
                         report.component3(),
                         report.component4(),
                         report.component5(),
                         report.component6(),
                         report.component7(),
                         report.component8(),
                         report.component9(),
                         report.component10());
  }

  protected HubReportDeed getHubReportDeedById(BigInteger reportId) throws Exception {
    Tuple5<BigInteger, BigInteger, BigInteger, BigInteger, BigInteger> hubDeed = uem.hubDeeds(reportId).send();
    return new HubReportDeed(hubDeed.component1(),
                             hubDeed.component2(),
                             hubDeed.component3(),
                             hubDeed.component4(),
                             hubDeed.component5());
  }

  protected HubReportReward getHubReportRewardById(BigInteger reportId) throws Exception {
    Tuple9<BigInteger, String, String, BigInteger, BigInteger, BigInteger, BigInteger, Boolean, BigInteger> hubReward =
                                                                                                                      uem.hubRewards(reportId)
                                                                                                                         .send();
    return new HubReportReward(hubReward.component1(),
                               hubReward.component2(),
                               hubReward.component3(),
                               hubReward.component4(),
                               hubReward.component5(),
                               hubReward.component6(),
                               hubReward.component7(),
                               hubReward.component8(),
                               hubReward.component9());
  }

  protected Recipient getRecipient(String address) throws Exception {
    Tuple3<BigInteger, BigInteger, BigInteger> tuple = uem.recipients(address).send();
    return new Recipient(tuple.component1(), tuple.component2(), tuple.component3());
  }

  @SuppressWarnings("unchecked")
  protected List<BigInteger> getRecipientReportIds(String address) throws Exception {
    return uem.reportsByRecipient(address).send();
  }

  @SuppressWarnings("rawtypes")
  protected TestUEM deployUem(Web3j web3j,
                              TransactionManager transactionManager,
                              ContractGasProvider contractGasProvider) throws Exception {
    TestUEM uemImpl = TestUEM.deploy(web3j, transactionManager, contractGasProvider).send();

    Function function = new Function(UserEngagementMinting.FUNC_INITIALIZE,
                                     Arrays.<Type> asList(new org.web3j.abi.datatypes.Address(160, meed.getContractAddress()),
                                                          new org.web3j.abi.datatypes.Address(160, wom.getContractAddress()),
                                                          new org.web3j.abi.datatypes.generated.Uint256(START_REWARD_TIME)),
                                     Collections.<TypeReference<?>> emptyList());
    byte[] data = Bytes.fromHexString(FunctionEncoder.encode(function)).toArray();
    UserEngagementMintingProxy uemProxy = UserEngagementMintingProxy.deploy(web3j,
                                                                            transactionManager,
                                                                            contractGasProvider,
                                                                            null,
                                                                            uemImpl.getContractAddress(),
                                                                            data)
                                                                    .send();

    return TestUEM.load(uemProxy.getContractAddress(), web3j, transactionManager, contractGasProvider);
  }

  protected TestUEM loadUem(Credentials credentials) {
    return TestUEM.load(uem.getContractAddress(), web3j, credentials, contractGasProvider);
  }

  protected static BigInteger usersCount(BigInteger deedId) {
    if (DEED1.equals(deedId)) {
      return BigInteger.valueOf(550l);
    } else if (DEED2.equals(deedId)) {
      return BigInteger.valueOf(5000l);
    } else if (DEED3.equals(deedId)) {
      return BigInteger.valueOf(100000l);
    } else {
      throw new IllegalStateException(UNKOWN_DEED_ID + deedId);
    }
  }

  protected static BigInteger recipientsCount(BigInteger deedId) {
    if (DEED1.equals(deedId)) {
      return BigInteger.valueOf(322l);
    } else if (DEED2.equals(deedId)) {
      return BigInteger.valueOf(3030l);
    } else if (DEED3.equals(deedId)) {
      return BigInteger.valueOf(9563l);
    } else {
      throw new IllegalStateException(UNKOWN_DEED_ID + deedId);
    }
  }

  protected static BigInteger participantsCount(BigInteger deedId) {
    if (DEED1.equals(deedId)) {
      return BigInteger.valueOf(452l);
    } else if (DEED2.equals(deedId)) {
      return BigInteger.valueOf(4581l);
    } else if (DEED3.equals(deedId)) {
      return BigInteger.valueOf(11163l);
    } else {
      throw new IllegalStateException(UNKOWN_DEED_ID + deedId);
    }
  }

  protected static BigInteger achievementsCount(BigInteger deedId) {
    if (DEED1.equals(deedId)) {
      return BigInteger.valueOf(540978l);
    } else if (DEED2.equals(deedId)) {
      return BigInteger.valueOf(1040978l);
    } else if (DEED3.equals(deedId)) {
      return BigInteger.valueOf(86040978l);
    } else {
      throw new IllegalStateException(UNKOWN_DEED_ID + deedId);
    }
  }

  protected static BigInteger amount(BigInteger deedId) {
    if (DEED1.equals(deedId)) {
      return BigInteger.valueOf(2440l)
                       .multiply(BigInteger.valueOf(10)
                                           .pow(18));
    } else if (DEED2.equals(deedId)) {
      return BigInteger.valueOf(5200l)
                       .multiply(BigInteger.valueOf(10)
                                           .pow(18));
    } else if (DEED3.equals(deedId)) {
      return BigInteger.valueOf(11000l)
                       .multiply(BigInteger.valueOf(10)
                                           .pow(18));
    } else {
      throw new IllegalStateException(UNKOWN_DEED_ID + deedId);
    }
  }

  protected BigInteger computeFixedRewardIndex(HubReport report,
                                               HubReportReward reportReward,
                                               HubReportDeed reportDeed) {
    BigInteger achievementsCount = report.achievementsCount;
    BigInteger participantsCount = report.participantsCount;
    BigInteger hubRewardAmount = report.amount;
    BigInteger lastRewardedAmount = reportReward.lastRewardedAmount;
    if (lastRewardedAmount.longValue() == 0l) { // NOSONAR
      // (amount / lastRewardedAmount) = 1
      // when the whole first time a report is sent by the Hub
      hubRewardAmount = BigInteger.ONE;
      lastRewardedAmount = BigInteger.ONE;
    }
    BigInteger recipientsCount = report.recipientsCount
                                                       .min(reportDeed.maxUsers);
    BigInteger usersCount = report.usersCount;
    BigInteger mintingPower = reportDeed.mintingPower;

    return BigDecimal.valueOf(10)
                     .pow(18)
                     .multiply(new BigDecimal(achievementsCount))
                     .multiply(new BigDecimal(hubRewardAmount))
                     .multiply(new BigDecimal(recipientsCount))
                     .multiply(new BigDecimal(mintingPower))
                     .divide(BigDecimal.valueOf(100))
                     .divide(new BigDecimal(participantsCount),
                             MathContext.DECIMAL128)
                     .divide(new BigDecimal(lastRewardedAmount),
                             MathContext.DECIMAL128)
                     .divide(new BigDecimal(usersCount),
                             MathContext.DECIMAL128)
                     .setScale(0, RoundingMode.DOWN)
                     .toBigInteger();
  }

  protected BigInteger claimAndCheck(Credentials recipientCredentials, // NOSONAR
                                     String targetReceiver,
                                     BigInteger amount,
                                     BigInteger expectedClaimedAmount,
                                     BigInteger expectedTotalClaimedAmount,
                                     BigInteger expectedAccumulatedRewards,
                                     int expectedIndex,
                                     int recipientReportsCount,
                                     BigInteger lastReportId) throws Exception {
    String recipient = recipientCredentials.getAddress();
    String receiver = targetReceiver;
    if (StringUtils.isBlank(targetReceiver)
        || StringUtils.equals(targetReceiver, EnsUtils.EMPTY_ADDRESS)) {
      receiver = recipient;
    }
    BigInteger initialBalance = meed.balanceOf(receiver).send();

    TransactionReceipt receipt = loadUem(recipientCredentials).claim(targetReceiver, amount).send();
    List<ClaimedEventResponse> claimedEvents = TestUEM.getClaimedEvents(receipt);
    assertNotNull(claimedEvents);
    assertEquals(1, claimedEvents.size());
    ClaimedEventResponse claimedEvent = claimedEvents.get(0);
    assertEquals(receiver.replace("0x", "").toLowerCase(),
                 claimedEvent.receiver.replace("0x", "").toLowerCase());
    assertEquals(recipient.replace("0x", "").toLowerCase(),
                 claimedEvent.recipient.replace("0x", "").toLowerCase());
    assertEquals(expectedClaimedAmount, claimedEvent.amount);
    assertEquals(initialBalance.add(claimedEvent.amount), meed.balanceOf(receiver).send());

    Recipient newRecipientProps = getRecipient(recipient);

    assertEquals(expectedTotalClaimedAmount, newRecipientProps.claimedRewards);
    assertEquals(expectedAccumulatedRewards, newRecipientProps.accRewards);
    assertEquals(expectedIndex, newRecipientProps.index.intValue());
    List<BigInteger> recipientReportIds = getRecipientReportIds(recipient);
    assertEquals(recipientReportsCount, recipientReportIds.size());
    if (!lastReportId.equals(BigInteger.ZERO)) {
      assertEquals(lastReportId, recipientReportIds.get(recipientReportIds.size() - 1));
    }

    BigInteger pendingReward = uem.pendingRewardBalanceOf(recipient).send();
    assertEquals(newRecipientProps.accRewards.subtract(newRecipientProps.claimedRewards), pendingReward);

    return claimedEvent.amount;
  }

  protected BigInteger computeRewardByReportAndRecipientType(BigInteger reportId, boolean owner) throws Exception {
    RewardPeriod reward = getRewardPeriodByReportId(reportId);
    HubReportReward reportReward = getHubReportRewardById(reportId);
    return new BigDecimal(reward.amount).multiply(new BigDecimal(owner ? reportReward.ownerFixedIndex :
                                                                       reportReward.tenantFixedIndex))
                                        .divide(new BigDecimal(reward.fixedGlobalIndex),
                                                MathContext.DECIMAL128)
                                        .setScale(0, RoundingMode.DOWN)
                                        .toBigInteger();
  }

  protected void checkPreviouslyRewardedAmount(BigInteger ownerEarnings,
                                               BigInteger reportId1,
                                               BigInteger deedId,
                                               boolean owner) throws Exception {
    BigInteger previouslyRewardedAmount = ownerEarnings.multiply(BigInteger.valueOf(100))
                                                       .divide(owner ? ownerPercentage(deedId) :
                                                                     BigInteger.valueOf(100).subtract(ownerPercentage(deedId)));
    checkPreviouslyRewardedAmount(reportId1, previouslyRewardedAmount);
  }

  protected void checkPreviouslyRewardedAmount(BigInteger reportId, BigInteger previousAmount) throws Exception {
    HubReportReward reportReward1 = getHubReportRewardById(reportId);
    assertEquals(previousAmount.divide(BigInteger.valueOf(100)), // Reduce
                                                                 // precision
                                                                 // into 10^-16
                                                                 // instead of
                                                                 // 10^-18 dor
                                                                 // diff between
                                                                 // solidity and
                                                                 // java
                 reportReward1.lastRewardedAmount.divide(BigInteger.valueOf(100)),
                 "The new generated reports should have used the last perceived rewards in UEM Rewards computation of current week");

    HubReport hubReport1 = getHubReportById(reportId);
    HubReportDeed hubDeed1 = getHubReportDeedById(reportId);
    BigInteger fixedRewardIndex = computeFixedRewardIndex(hubReport1, reportReward1, hubDeed1);
    assertEquals(fixedRewardIndex, reportReward1.fixedRewardIndex, "Second report sent compuation is wrong");
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Data
  public static class HubReportReward {
    // Reward Period Id
    BigInteger rewardPeriodId;

    // Deed Owner address
    String owner;

    // Deed Tenant address
    String tenant;

    // Fixed Reward indice = ( 𝐸𝑑 ∗ 𝐷𝑟 ∗ 𝐷𝑠 ∗ 𝑀) : without 𝐸𝑤
    BigInteger fixedRewardIndex;

    // Fixed Reward indice * ownerSharePercentage
    BigInteger ownerFixedIndex;

    // Fixed Reward indice * tenantSharePercentage
    BigInteger tenantFixedIndex;

    // Sent date of the report
    BigInteger sentDate;

    // Whether the Hub Report is fraud or not
    boolean fraud;

    // Last UEM Period rewarded Amount
    BigInteger lastRewardedAmount;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Data
  public static class HubReportDeed {
    // Reward Period Id
    BigInteger deedId;

    // Deed Owner address
    BigInteger city;

    // Deed Tenant address
    BigInteger cardType;

    // Fixed Reward indice = ( 𝐸𝑑 ∗ 𝐷𝑟 ∗ 𝐷𝑠 ∗ 𝑀) : without 𝐸𝑤
    BigInteger mintingPower;

    // Fixed Reward indice * ownerSharePercentage
    BigInteger maxUsers;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Data
  public static class RewardPeriod {
    // Total configured Reward amount at this time
    BigInteger amount;

    // Total reports count = toReport - fromReport + 1
    BigInteger reportsCount;

    // First report index included in this Reward period
    BigInteger fromReport;

    // Last report index included in this Reward period
    BigInteger toReport;

    // Sum of all reports ( 𝐸𝑑 ∗ 𝐷𝑟 ∗ 𝐷𝑠 ∗ 𝑀)
    // =
    // without 𝐸𝑤 = Fixed Index
    BigInteger fixedGlobalIndex;

    // Start date of reward period
    BigInteger fromDate;

    // End date of reward period
    BigInteger toDate;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Data
  public static class Recipient {
    // Already claimed rewards
    BigInteger claimedRewards;

    // Accumulated Rewards already computed before
    // the "index" of the list of reportIds table
    // thus this may not hold the total rewards amount
    BigInteger accRewards;

    // Last index that was used to compute
    // the accumulated rewards ("accRewards")
    BigInteger index;
  }

}
