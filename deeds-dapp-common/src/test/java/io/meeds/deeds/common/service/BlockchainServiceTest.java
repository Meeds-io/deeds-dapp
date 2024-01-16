/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.codec.binary.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthLog.LogObject;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple12;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tuples.generated.Tuple5;

import io.meeds.deeds.api.constant.ObjectNotFoundException;
import io.meeds.deeds.common.constant.BlockchainOfferStatus;
import io.meeds.deeds.common.constant.CommonConstants.DeedOwnershipTransferEvent;
import io.meeds.deeds.common.model.DeedCity;
import io.meeds.deeds.common.model.DeedOfferBlockchainState;
import io.meeds.deeds.common.model.FundInfo;
import io.meeds.deeds.contract.Deed;
import io.meeds.deeds.contract.Deed.TransferSingleEventResponse;
import io.meeds.deeds.contract.DeedRenting;
import io.meeds.deeds.contract.DeedRenting.OfferCreatedEventResponse;
import io.meeds.deeds.contract.DeedRenting.OfferDeletedEventResponse;
import io.meeds.deeds.contract.DeedRenting.RentPaidEventResponse;
import io.meeds.deeds.contract.DeedTenantProvisioning;
import io.meeds.deeds.contract.ERC20;
import io.meeds.deeds.contract.MeedsToken;
import io.meeds.deeds.contract.TokenFactory;
import io.meeds.deeds.contract.XMeedsNFTRewarding;

@SpringBootTest(classes = {
    BlockchainService.class
})
class BlockchainServiceTest {

  @MockBean(name = "ethereumNetwork")
  private Web3j                            web3j;

  @MockBean(name = "polygonNetwork")
  private Web3j                            polygonWeb3j;

  @MockBean
  private DeedTenantProvisioning           deedTenantProvisioning;

  @MockBean
  private DeedRenting                      deedRenting;

  @MockBean
  private Deed                             deed;

  @MockBean(name = "ethereumMeedToken")
  private MeedsToken                       ethereumToken;

  @MockBean(name = "polygonMeedToken")
  private MeedsToken                       polygonToken;

  @MockBean
  private XMeedsNFTRewarding               xMeedsToken;

  @MockBean
  private TokenFactory                     tokenFactory;

  @MockBean(name = "sushiPairToken")
  private ERC20                            sushiPairToken;

  @Autowired
  private BlockchainService                blockchainService;

  private static MockedStatic<DeedRenting> mockDeedRentingContract = mockStatic(DeedRenting.class);

  @Test
  void testGetDeedCardType() throws Exception {
    assertNotNull(blockchainService);

    when(deed.cardType(any())).thenAnswer(invocation -> {
      BigInteger argument = invocation.getArgument(0, BigInteger.class);
      if (argument == null || argument.shortValue() > 1) {
        throw new IllegalStateException("execution reverted: nftId doesn't exist");
      } else if (argument.shortValue() == 0) {
        throw new RuntimeException();
      } else {
        @SuppressWarnings("unchecked")
        RemoteFunctionCall<BigInteger> remoteFunctionCall = mock(RemoteFunctionCall.class);
        when(remoteFunctionCall.send()).thenReturn(BigInteger.TWO);
        return remoteFunctionCall;
      }
    });

    assertThrows(IllegalStateException.class, () -> blockchainService.getDeedCardType(0));
    assertThrows(ObjectNotFoundException.class, () -> blockchainService.getDeedCardType(2));
    assertThrows(ObjectNotFoundException.class, () -> blockchainService.getDeedCardType(500));
    assertEquals(2, blockchainService.getDeedCardType(1));
  }

  @Test
  void testGetDeedCityIndex() throws Exception {
    assertNotNull(blockchainService);

    when(deed.cityIndex(any())).thenAnswer(invocation -> {
      BigInteger argument = invocation.getArgument(0, BigInteger.class);
      if (argument == null || argument.shortValue() > 1) {
        throw new IllegalStateException("execution reverted: nftId doesn't exist");
      } else if (argument.shortValue() == 0) {
        throw new RuntimeException();
      } else {
        @SuppressWarnings("unchecked")
        RemoteFunctionCall<BigInteger> remoteFunctionCall = mock(RemoteFunctionCall.class);
        when(remoteFunctionCall.send()).thenReturn(BigInteger.TWO);
        return remoteFunctionCall;
      }
    });

    assertThrows(IllegalStateException.class, () -> blockchainService.getDeedCityIndex(0));
    assertThrows(ObjectNotFoundException.class, () -> blockchainService.getDeedCityIndex(2));
    assertThrows(ObjectNotFoundException.class, () -> blockchainService.getDeedCityIndex(500));
    assertEquals(2, blockchainService.getDeedCityIndex(1));
  }

  @Test
  void testIsDeedProvisioningManager() throws Exception {
    assertNotNull(blockchainService);

    String walletAddress = "0xa5ef66131fbd70a1ffa69a95aba172f0a0502e6a";

    when(deedTenantProvisioning.isProvisioningManager(any(), any())).thenAnswer(invocation -> {
      String address = invocation.getArgument(0, String.class);
      BigInteger nftId = invocation.getArgument(1, BigInteger.class);
      if (nftId == null || nftId.shortValue() == 0 || !StringUtils.equals(address, walletAddress)) {
        throw new RuntimeException();
      } else {
        @SuppressWarnings("unchecked")
        RemoteFunctionCall<Boolean> remoteFunctionCall = mock(RemoteFunctionCall.class);
        when(remoteFunctionCall.send()).thenReturn(nftId.shortValue() == 1);
        return remoteFunctionCall;
      }
    });

    assertThrows(RuntimeException.class, () -> blockchainService.isDeedProvisioningManager(walletAddress, 0));
    assertTrue(blockchainService.isDeedProvisioningManager(walletAddress, 1));
    assertFalse(blockchainService.isDeedProvisioningManager(walletAddress, 2));
  }

  @Test
  void testMeedsTotalSupplyNoDecimals() throws Exception {
    assertNotNull(blockchainService);

    BigInteger totalSupply = BigInteger.valueOf(12500);

    when(ethereumToken.totalSupply()).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      RemoteFunctionCall<BigInteger> remoteFunctionCall = mock(RemoteFunctionCall.class);
      when(remoteFunctionCall.send()).thenReturn(totalSupply);
      return remoteFunctionCall;
    });
    assertEquals(new BigDecimal(totalSupply).divide(BigDecimal.valueOf(10).pow(18)),
                 blockchainService.meedsTotalSupplyNoDecimals());
  }

  @Test
  void testXMeedsTotalSupply() throws Exception {
    assertNotNull(blockchainService);

    BigInteger totalSupply = BigInteger.valueOf(12500);

    when(xMeedsToken.totalSupply()).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      RemoteFunctionCall<BigInteger> remoteFunctionCall = mock(RemoteFunctionCall.class);
      when(remoteFunctionCall.send()).thenReturn(totalSupply);
      return remoteFunctionCall;
    });
    assertEquals(totalSupply, blockchainService.xMeedsTotalSupply());
  }

  @Test
  void testSushiPairTotalSupply() throws Exception {
    assertNotNull(blockchainService);

    BigInteger totalSupply = BigInteger.valueOf(12500);

    when(sushiPairToken.totalSupply()).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      RemoteFunctionCall<BigInteger> remoteFunctionCall = mock(RemoteFunctionCall.class);
      when(remoteFunctionCall.send()).thenReturn(totalSupply);
      return remoteFunctionCall;
    });
    assertEquals(totalSupply, blockchainService.sushiPairTotalSupply());
  }

  @Test
  void testTotalAllocationPoints() throws Exception {
    assertNotNull(blockchainService);

    BigInteger totalAllocationPoints = BigInteger.valueOf(12500);

    when(tokenFactory.totalAllocationPoints()).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      RemoteFunctionCall<BigInteger> remoteFunctionCall = mock(RemoteFunctionCall.class);
      when(remoteFunctionCall.send()).thenReturn(totalAllocationPoints);
      return remoteFunctionCall;
    });
    assertEquals(totalAllocationPoints, blockchainService.totalAllocationPoints());
  }

  @Test
  void testTotalFixedPercentage() throws Exception {
    assertNotNull(blockchainService);

    BigInteger totalFixedPercentage = BigInteger.valueOf(12500);

    when(tokenFactory.totalFixedPercentage()).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      RemoteFunctionCall<BigInteger> remoteFunctionCall = mock(RemoteFunctionCall.class);
      when(remoteFunctionCall.send()).thenReturn(totalFixedPercentage);
      return remoteFunctionCall;
    });
    assertEquals(totalFixedPercentage, blockchainService.totalFixedPercentage());
  }

  @Test
  void testSushiPairSymbol() throws Exception {
    assertNotNull(blockchainService);

    String sushiPairSymbol = "SLP";

    when(sushiPairToken.symbol()).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      RemoteFunctionCall<String> remoteFunctionCall = mock(RemoteFunctionCall.class);
      when(remoteFunctionCall.send()).thenReturn(sushiPairSymbol);
      return remoteFunctionCall;
    });
    assertEquals(sushiPairSymbol, blockchainService.sushiPairSymbol());
  }

  @Test
  void testSushiPairSymbolError() throws Exception {
    assertNotNull(blockchainService);

    when(sushiPairToken.symbol()).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      RemoteFunctionCall<String> remoteFunctionCall = mock(RemoteFunctionCall.class);
      when(remoteFunctionCall.send()).thenThrow(Exception.class);
      return remoteFunctionCall;
    });
    assertThrows(IllegalStateException.class, () -> blockchainService.sushiPairSymbol());
  }

  @Test
  void testGetCurrentCity() throws Exception {
    assertNotNull(blockchainService);
    BigInteger currentCityIndex = BigInteger.ONE;

    Tuple4<String, BigInteger, BigInteger, BigInteger> cityInfo =
                                                                new Tuple4<String, BigInteger, BigInteger, BigInteger>("name",
                                                                                                                       BigInteger.valueOf(2),
                                                                                                                       BigInteger.valueOf(3),
                                                                                                                       BigInteger.valueOf(4));

    when(xMeedsToken.currentCityIndex()).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      RemoteFunctionCall<BigInteger> remoteFunctionCall = mock(RemoteFunctionCall.class);
      when(remoteFunctionCall.send()).thenReturn(currentCityIndex);
      return remoteFunctionCall;
    });

    when(xMeedsToken.cityInfo(currentCityIndex)).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      RemoteFunctionCall<Tuple4<String, BigInteger, BigInteger, BigInteger>> remoteFunctionCall = mock(RemoteFunctionCall.class);
      when(remoteFunctionCall.send()).thenReturn(cityInfo);
      return remoteFunctionCall;
    });
    DeedCity expectedCity = new DeedCity(currentCityIndex,
                                         cityInfo.component1(),
                                         cityInfo.component2(),
                                         cityInfo.component3(),
                                         cityInfo.component4());
    assertEquals(expectedCity, blockchainService.getCurrentCity());
    assertEquals(expectedCity.hashCode(), blockchainService.getCurrentCity().hashCode());
  }

  @Test
  void testGetXMeedFundInfo() throws Exception {
    assertNotNull(blockchainService);
    BigInteger totalSupply = BigInteger.valueOf(12500);
    BigInteger pendingReward = BigInteger.valueOf(120);
    BigInteger meedBalance = BigInteger.valueOf(120);
    String xMeedAddress = "xMeeedAddress";

    Tuple5<BigInteger, BigInteger, BigInteger, BigInteger, Boolean> fundInfo =
                                                                             new Tuple5<BigInteger, BigInteger, BigInteger, BigInteger, Boolean>(BigInteger.valueOf(2),
                                                                                                                                                 BigInteger.valueOf(3),
                                                                                                                                                 BigInteger.valueOf(4),
                                                                                                                                                 BigInteger.valueOf(5),
                                                                                                                                                 Boolean.FALSE);

    when(xMeedsToken.getContractAddress()).thenReturn(xMeedAddress);
    when(tokenFactory.fundInfos(xMeedAddress)).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      RemoteFunctionCall<Tuple5<BigInteger, BigInteger, BigInteger, BigInteger, Boolean>> remoteFunctionCall =
                                                                                                             mock(RemoteFunctionCall.class);
      when(remoteFunctionCall.send()).thenReturn(fundInfo);
      return remoteFunctionCall;
    });

    when(xMeedsToken.totalSupply()).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      RemoteFunctionCall<BigInteger> remoteFunctionCall = mock(RemoteFunctionCall.class);
      when(remoteFunctionCall.send()).thenReturn(totalSupply);
      return remoteFunctionCall;
    });

    when(tokenFactory.pendingRewardBalanceOf(xMeedAddress)).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      RemoteFunctionCall<BigInteger> remoteFunctionCall = mock(RemoteFunctionCall.class);
      when(remoteFunctionCall.send()).thenReturn(pendingReward);
      return remoteFunctionCall;
    });

    when(ethereumToken.balanceOf(xMeedAddress)).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      RemoteFunctionCall<BigInteger> remoteFunctionCall = mock(RemoteFunctionCall.class);
      when(remoteFunctionCall.send()).thenReturn(meedBalance);
      return remoteFunctionCall;
    });

    FundInfo expectedFundInfo = new FundInfo(xMeedAddress,
                                             fundInfo.component1(),
                                             fundInfo.component2(),
                                             fundInfo.component3(),
                                             fundInfo.component4(),
                                             fundInfo.component5());
    expectedFundInfo.setTotalSupply(totalSupply);
    expectedFundInfo.setXMeedPendingReward(pendingReward);
    expectedFundInfo.setMeedsBalance(meedBalance);

    assertEquals(expectedFundInfo, blockchainService.getXMeedFundInfo());
    assertEquals(expectedFundInfo.hashCode(), blockchainService.getXMeedFundInfo().hashCode());
  }

  @Test
  void testGetSushiPairFundInfo() throws Exception {
    assertNotNull(blockchainService);
    BigInteger totalSupply = BigInteger.valueOf(12500);
    BigInteger lpBalanceOfTokenFactory = BigInteger.valueOf(120);
    BigInteger meedBalance = BigInteger.valueOf(120);
    String sushiPairAddress = "sushiPairAddress";
    String tokenFactoryAddress = "tokenFactoryAddress";
    String sushiPairSymbol = "SLP";

    Tuple5<BigInteger, BigInteger, BigInteger, BigInteger, Boolean> fundInfo =
                                                                             new Tuple5<BigInteger, BigInteger, BigInteger, BigInteger, Boolean>(BigInteger.valueOf(2),
                                                                                                                                                 BigInteger.valueOf(3),
                                                                                                                                                 BigInteger.valueOf(4),
                                                                                                                                                 BigInteger.valueOf(5),
                                                                                                                                                 Boolean.TRUE);

    when(sushiPairToken.getContractAddress()).thenReturn(sushiPairAddress);
    when(tokenFactory.getContractAddress()).thenReturn(tokenFactoryAddress);
    when(tokenFactory.fundInfos(sushiPairAddress)).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      RemoteFunctionCall<Tuple5<BigInteger, BigInteger, BigInteger, BigInteger, Boolean>> remoteFunctionCall =
                                                                                                             mock(RemoteFunctionCall.class);
      when(remoteFunctionCall.send()).thenReturn(fundInfo);
      return remoteFunctionCall;
    });

    when(sushiPairToken.totalSupply()).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      RemoteFunctionCall<BigInteger> remoteFunctionCall = mock(RemoteFunctionCall.class);
      when(remoteFunctionCall.send()).thenReturn(totalSupply);
      return remoteFunctionCall;
    });

    when(sushiPairToken.symbol()).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      RemoteFunctionCall<String> remoteFunctionCall = mock(RemoteFunctionCall.class);
      when(remoteFunctionCall.send()).thenReturn(sushiPairSymbol);
      return remoteFunctionCall;
    });

    when(sushiPairToken.balanceOf(tokenFactoryAddress)).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      RemoteFunctionCall<BigInteger> remoteFunctionCall = mock(RemoteFunctionCall.class);
      when(remoteFunctionCall.send()).thenReturn(lpBalanceOfTokenFactory);
      return remoteFunctionCall;
    });

    when(ethereumToken.balanceOf(sushiPairAddress)).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      RemoteFunctionCall<BigInteger> remoteFunctionCall = mock(RemoteFunctionCall.class);
      when(remoteFunctionCall.send()).thenReturn(meedBalance);
      return remoteFunctionCall;
    });

    FundInfo expectedFundInfo = new FundInfo(sushiPairAddress,
                                             fundInfo.component1(),
                                             fundInfo.component2(),
                                             fundInfo.component3(),
                                             fundInfo.component4(),
                                             fundInfo.component5());
    expectedFundInfo.setTotalSupply(totalSupply);
    expectedFundInfo.setLpBalanceOfTokenFactory(lpBalanceOfTokenFactory);
    expectedFundInfo.setSymbol(sushiPairSymbol);
    expectedFundInfo.setMeedsBalance(meedBalance);

    assertEquals(expectedFundInfo, blockchainService.getSushiPairFundInfo());
    assertEquals(expectedFundInfo.hashCode(), blockchainService.getSushiPairFundInfo().hashCode());
  }

  @Test
  void testBalanceOfOnPolygon() throws Exception {
    assertNotNull(blockchainService);

    BigInteger balance = BigInteger.valueOf(12500);
    String address = "0x6acA77CF3BaB0C4E8210A09B57B07854a995289a";

    when(polygonToken.balanceOf(address)).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      RemoteFunctionCall<BigInteger> remoteFunctionCall = mock(RemoteFunctionCall.class);
      when(remoteFunctionCall.send()).thenReturn(balance);
      return remoteFunctionCall;
    });
    assertEquals(new BigDecimal(balance).divide(BigDecimal.valueOf(10).pow(18)),
                 blockchainService.meedBalanceOfOnPolygon(address));
  }

  @Test
  void testBalanceOfOnEthereum() throws Exception {
    assertNotNull(blockchainService);

    BigInteger balance = BigInteger.valueOf(12500);
    String address = "0x44D6d6aB50401Dd846336e9C706A492f06E1Bcd4";

    when(ethereumToken.balanceOf(address)).thenAnswer(invocation -> {
      @SuppressWarnings("unchecked")
      RemoteFunctionCall<BigInteger> remoteFunctionCall = mock(RemoteFunctionCall.class);
      when(remoteFunctionCall.send()).thenReturn(balance);
      return remoteFunctionCall;
    });
    assertEquals(new BigDecimal(balance).divide(BigDecimal.valueOf(10).pow(18)),
                 blockchainService.meedBalanceOfNoDecimals(address));
  }

  @SuppressWarnings({
      "unchecked", "rawtypes"
  })
  @Test
  void testGetMinedTransferOwnershipDeedTransactions() throws Exception {
    long fromBlock = 15l;
    long toBlock = 20l;

    Request getLogsRequest = mock(Request.class);
    EthLog ethLogs = mock(EthLog.class);
    when(web3j.ethGetLogs(any())).thenReturn(getLogsRequest);
    when(getLogsRequest.send()).thenReturn(ethLogs);
    Set<DeedOwnershipTransferEvent> minedTransactions = blockchainService.getMinedTransferOwnershipDeedTransactions(fromBlock,
                                                                                                                    toBlock);
    assertNotNull(minedTransactions);
    assertTrue(minedTransactions.isEmpty());

    String transactionHash = "0xab5bc0ece5ef0995fac33c53f4b92d68da952552a73932e51b4c02933237e84f";
    LogResult logResult = mock(LogResult.class);
    LogObject logObject = mock(LogObject.class);
    when(logResult.get()).thenReturn(logObject);
    when(ethLogs.getLogs()).thenReturn(Collections.singletonList(logResult));

    when(logObject.getTransactionHash()).thenReturn(transactionHash);
    Request getTransactionReceiptRequest = mock(Request.class);
    when(web3j.ethGetTransactionReceipt(transactionHash)).thenReturn(getTransactionReceiptRequest);
    EthGetTransactionReceipt ethGetTransactionReceipt = mock(EthGetTransactionReceipt.class);
    when(getTransactionReceiptRequest.send()).thenReturn(ethGetTransactionReceipt);
    TransactionReceipt transactionReceipt = mock(TransactionReceipt.class);
    when(ethGetTransactionReceipt.getResult()).thenReturn(transactionReceipt);
    when(transactionReceipt.isStatusOK()).thenReturn(false);

    minedTransactions = blockchainService.getMinedTransferOwnershipDeedTransactions(fromBlock, toBlock);
    assertNotNull(minedTransactions);
    assertTrue(minedTransactions.isEmpty());

    when(transactionReceipt.isStatusOK()).thenReturn(true);
    MockedStatic<Deed> mockDeedContract = mockStatic(Deed.class);
    String fromAddress = "fromAddress";
    String toAddress = "toAddress";
    long nftId = 2l;

    mockDeedContract.when(() -> Deed.getTransferSingleEvents(transactionReceipt))
                    .thenReturn(Arrays.asList(newTransferSingleEvent(nftId, fromAddress, toAddress),
                                              newTransferSingleEvent(nftId, fromAddress, toAddress),
                                              newTransferSingleEvent(nftId, fromAddress, toAddress)));

    minedTransactions = blockchainService.getMinedTransferOwnershipDeedTransactions(fromBlock, toBlock);
    assertNotNull(minedTransactions);
    assertEquals(1, minedTransactions.size());

    mockDeedContract.when(() -> Deed.getTransferSingleEvents(transactionReceipt))
                    .thenReturn(Arrays.asList(newTransferSingleEvent(nftId, fromAddress, toAddress),
                                              newTransferSingleEvent(nftId + 1, fromAddress, toAddress),
                                              newTransferSingleEvent(nftId + 2, fromAddress, toAddress)));

    minedTransactions = blockchainService.getMinedTransferOwnershipDeedTransactions(fromBlock, toBlock);
    assertNotNull(minedTransactions);
    assertEquals(3, minedTransactions.size());
  }

  @Test
  @SuppressWarnings({
      "unchecked", "rawtypes"
  })
  void testGetCreatedOfferTransactionEvents() throws Exception {
    String transactionHash = "0xab5bc0ece5ef0995fac33c53f4b92d68da952552a73932e51b4c02933237e84f";
    Request getTransactionReceiptRequest = mock(Request.class);
    EthGetTransactionReceipt ethGetTransactionReceipt = mock(EthGetTransactionReceipt.class);

    when(web3j.ethGetTransactionReceipt(transactionHash)).thenReturn(getTransactionReceiptRequest);
    when(getTransactionReceiptRequest.send()).thenReturn(ethGetTransactionReceipt);
    TransactionReceipt transactionReceipt = mock(TransactionReceipt.class);
    when(ethGetTransactionReceipt.getTransactionReceipt()).thenReturn(Optional.of(transactionReceipt));
    when(transactionReceipt.isStatusOK()).thenReturn(false);

    Map<BlockchainOfferStatus, DeedOfferBlockchainState> offerTransactionEvents =
                                                                                blockchainService.getOfferTransactionEvents(transactionHash);
    assertTrue(offerTransactionEvents.isEmpty());

    OfferCreatedEventResponse offerCreateResponse = new OfferCreatedEventResponse();
    offerCreateResponse.deedId = BigInteger.ONE;
    offerCreateResponse.id = BigInteger.TWO;
    offerCreateResponse.owner = "owner";

    mockDeedRentingContract.when(() -> DeedRenting.getOfferCreatedEvents(transactionReceipt))
                           .thenReturn(Collections.singletonList(offerCreateResponse));

    mockOffer(offerCreateResponse.id);

    when(transactionReceipt.isStatusOK()).thenReturn(true);

    offerTransactionEvents = blockchainService.getOfferTransactionEvents(transactionHash);
    assertFalse(offerTransactionEvents.isEmpty());
    assertEquals(1, offerTransactionEvents.size());
  }

  @Test
  @SuppressWarnings({
      "unchecked", "rawtypes"
  })
  void testGetDeletedOfferTransactionEvents() throws Exception {
    String transactionHash = "0xab5bc0ece5ef0995fac33c53f4b92d68da952552a73932e51b4c02933237e84f";
    Request getTransactionReceiptRequest = mock(Request.class);
    EthGetTransactionReceipt ethGetTransactionReceipt = mock(EthGetTransactionReceipt.class);

    when(web3j.ethGetTransactionReceipt(transactionHash)).thenReturn(getTransactionReceiptRequest);
    when(getTransactionReceiptRequest.send()).thenReturn(ethGetTransactionReceipt);
    TransactionReceipt transactionReceipt = mock(TransactionReceipt.class);
    when(ethGetTransactionReceipt.getTransactionReceipt()).thenReturn(Optional.of(transactionReceipt));
    when(transactionReceipt.isStatusOK()).thenReturn(false);

    Map<BlockchainOfferStatus, DeedOfferBlockchainState> offerTransactionEvents =
                                                                                blockchainService.getOfferTransactionEvents(transactionHash);
    assertTrue(offerTransactionEvents.isEmpty());

    OfferDeletedEventResponse offerDeleteResponse = new OfferDeletedEventResponse();
    offerDeleteResponse.deedId = BigInteger.ONE;
    offerDeleteResponse.id = BigInteger.TWO;
    offerDeleteResponse.owner = "owner";

    mockDeedRentingContract.when(() -> DeedRenting.getOfferDeletedEvents(transactionReceipt))
                           .thenReturn(Collections.singletonList(offerDeleteResponse));

    when(transactionReceipt.isStatusOK()).thenReturn(true);

    offerTransactionEvents = blockchainService.getOfferTransactionEvents(transactionHash);
    assertFalse(offerTransactionEvents.isEmpty());
    assertEquals(1, offerTransactionEvents.size());
  }

  @Test
  @SuppressWarnings({
      "unchecked", "rawtypes"
  })
  void testGetPaidOfferTransactionEvents() throws Exception {
    String transactionHash = "0xab5bc0ece5ef0995fac33c53f4b92d68da952552a73932e51b4c02933237e84f";
    Request getTransactionReceiptRequest = mock(Request.class);
    EthGetTransactionReceipt ethGetTransactionReceipt = mock(EthGetTransactionReceipt.class);

    when(web3j.ethGetTransactionReceipt(transactionHash)).thenReturn(getTransactionReceiptRequest);
    when(getTransactionReceiptRequest.send()).thenReturn(ethGetTransactionReceipt);
    TransactionReceipt transactionReceipt = mock(TransactionReceipt.class);
    when(ethGetTransactionReceipt.getTransactionReceipt()).thenReturn(Optional.of(transactionReceipt));
    when(transactionReceipt.isStatusOK()).thenReturn(false);

    Map<BlockchainOfferStatus, DeedOfferBlockchainState> offerTransactionEvents =
                                                                                blockchainService.getOfferTransactionEvents(transactionHash);
    assertTrue(offerTransactionEvents.isEmpty());

    RentPaidEventResponse offerPaidResponse = new RentPaidEventResponse();
    offerPaidResponse.deedId = BigInteger.ONE;
    offerPaidResponse.id = BigInteger.TWO;
    offerPaidResponse.owner = "owner";
    offerPaidResponse.firstRent = true;

    mockDeedRentingContract.when(() -> DeedRenting.getRentPaidEvents(transactionReceipt))
                           .thenReturn(Collections.singletonList(offerPaidResponse));
    mockOffer(offerPaidResponse.id);
    when(transactionReceipt.isStatusOK()).thenReturn(true);

    offerTransactionEvents = blockchainService.getOfferTransactionEvents(transactionHash);
    assertFalse(offerTransactionEvents.isEmpty());
    assertEquals(1, offerTransactionEvents.size());
  }

  @SuppressWarnings({
      "unchecked"
  })
  private void mockOffer(BigInteger offerId) throws Exception {
    Tuple12<BigInteger, BigInteger, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, String, BigInteger> tuple12;
    tuple12 = new Tuple12<>(
                            BigInteger.valueOf(1),
                            BigInteger.valueOf(2),
                            BigInteger.valueOf(3).toString(),
                            BigInteger.valueOf(4),
                            BigInteger.valueOf(5),
                            BigInteger.valueOf(6),
                            BigInteger.valueOf(7),
                            BigInteger.valueOf(8),
                            BigInteger.valueOf(9),
                            BigInteger.valueOf(10),
                            BigInteger.valueOf(11).toString(),
                            BigInteger.valueOf(12));
    RemoteFunctionCall<Tuple12<BigInteger, BigInteger, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, String, BigInteger>> remoteFunctionCall;
    remoteFunctionCall = mock(RemoteFunctionCall.class);
    when(remoteFunctionCall.send()).thenReturn(tuple12);
    when(deedRenting.deedOffers(offerId)).thenReturn(remoteFunctionCall);
  }

  private TransferSingleEventResponse newTransferSingleEvent(long nftId, String fromAddress, String toAddress) {
    TransferSingleEventResponse eventResponse = new TransferSingleEventResponse();
    eventResponse._id = BigInteger.valueOf(nftId);
    eventResponse._from = fromAddress;
    eventResponse._to = toAddress;
    return eventResponse;
  }

}
