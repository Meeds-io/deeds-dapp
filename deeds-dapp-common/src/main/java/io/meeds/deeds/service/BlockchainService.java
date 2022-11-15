/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
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
package io.meeds.deeds.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthLog.LogObject;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tuples.generated.Tuple5;

import io.meeds.deeds.constant.CommonConstants.DeedOwnershipTransferEvent;
import io.meeds.deeds.constant.ObjectNotFoundException;
import io.meeds.deeds.contract.Deed;
import io.meeds.deeds.contract.Deed.TransferBatchEventResponse;
import io.meeds.deeds.contract.Deed.TransferSingleEventResponse;
import io.meeds.deeds.contract.DeedTenantProvisioning;
import io.meeds.deeds.contract.DeedTenantProvisioning.TenantStartedEventResponse;
import io.meeds.deeds.contract.DeedTenantProvisioning.TenantStoppedEventResponse;
import io.meeds.deeds.contract.ERC20;
import io.meeds.deeds.contract.MeedsToken;
import io.meeds.deeds.contract.TokenFactory;
import io.meeds.deeds.contract.XMeedsNFTRewarding;
import io.meeds.deeds.model.DeedCity;
import io.meeds.deeds.model.DeedTenant;
import io.meeds.deeds.model.FundInfo;

@Component
public class BlockchainService {

  @Autowired
  @Qualifier("ethereumNetwork")
  private Web3j                             web3j;

  @Autowired(required = false)
  private DeedTenantProvisioning            deedTenantProvisioning;

  @Autowired
  private Deed                              deed;

  @Autowired
  private TokenFactory                      tokenFactory;

  @Autowired
  private XMeedsNFTRewarding                xMeedsToken;

  @Autowired
  @Qualifier("ethereumMeedToken")
  private MeedsToken                        ethereumToken;

  @Autowired
  @Qualifier("polygonMeedToken")
  private MeedsToken                        polygonToken;

  @Autowired
  @Qualifier("sushiPairToken")
  private ERC20                             sushiPairToken;

  /**
   * Return DEED Tenant Status from Blockchain Contract
   *
   * @param nftId Deed NFT identifier
   * @return if marked as started else false
   */
  public boolean isDeedStarted(long nftId) {
    try {
      return deedTenantProvisioning.tenantStatus(BigInteger.valueOf(nftId)).send().booleanValue();
    } catch (Exception e) {
      throw new IllegalStateException("Error retrieving information 'getDeedCityIndex' from Blockchain", e);
    }
  }

  /**
   * Checks if transaction has been mined or not
   * 
   * @param transactionHash Blockchain Transaction Hash
   * @return true if Transaction is Mined
   */
  public boolean isTransactionMined(String transactionHash) {
    TransactionReceipt receipt = getTransactionReceipt(transactionHash);
    return receipt != null;
  }

  /**
   * @param transactionHash Blockchain Transaction Hash
   * @return true if Transaction is Successful
   */
  public boolean isTransactionConfirmed(String transactionHash) {
    TransactionReceipt receipt = getTransactionReceipt(transactionHash);
    return receipt != null && receipt.isStatusOK();
  }

  /**
   * @return last mined block number
   */
  public long getLastBlock() {
    try {
      return web3j.ethBlockNumber().send().getBlockNumber().longValue();
    } catch (IOException e) {
      throw new IllegalStateException("Error getting last block number", e);
    }
  }

  /**
   * Retrieves the list of mined provisioning transactions starting from a block
   * to another
   * 
   * @param fromBlock Start block
   * @param toBlock End Block to filter
   * @return {@link List} of {@link DeedTenant}
   */
  public List<DeedTenant> getMinedProvisioningTransactions(long fromBlock, long toBlock) {
    EthFilter ethFilter = new EthFilter(new DefaultBlockParameterNumber(fromBlock),
                                        new DefaultBlockParameterNumber(toBlock),
                                        deedTenantProvisioning.getContractAddress());
    try {
      EthLog ethLog = web3j.ethGetLogs(ethFilter).send();
      @SuppressWarnings("rawtypes")
      List<LogResult> ethLogs = ethLog.getLogs();
      if (CollectionUtils.isEmpty(ethLogs)) {
        return Collections.emptyList();
      }
      return ethLogs.stream()
                    .map(logResult -> (LogObject) logResult.get())
                    .filter(logObject -> !logObject.isRemoved())
                    .map(LogObject::getTransactionHash)
                    .map(this::getTransactionReceipt)
                    .filter(TransactionReceipt::isStatusOK)
                    .map(this::getMinedDeedTenant)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
    } catch (IOException e) {
      throw new IllegalStateException("Error retrieving event logs", e);
    }
  }

  /**
   * Retrieves the list of mined ownership transfer of a Deed transactions
   * starting from a block to another
   * 
   * @param  fromBlock Start block
   * @param  toBlock   End Block to filter
   * @return           {@link Set} of NFT ID of type
   *                   {@link DeedOwnershipTransferEvent}
   */
  public Set<DeedOwnershipTransferEvent> getMinedTransferOwnershipDeedTransactions(long fromBlock, long toBlock) {
    EthFilter ethFilter = new EthFilter(new DefaultBlockParameterNumber(fromBlock),
                                        new DefaultBlockParameterNumber(toBlock),
                                        deed.getContractAddress());
    try {
      EthLog ethLog = web3j.ethGetLogs(ethFilter).send();
      @SuppressWarnings("rawtypes")
      List<LogResult> ethLogs = ethLog.getLogs();
      if (CollectionUtils.isEmpty(ethLogs)) {
        return Collections.emptySet();
      }
      List<DeedOwnershipTransferEvent> events = ethLogs.stream()
                                                       .map(logResult -> (LogObject) logResult.get())
                                                       .filter(logObject -> !logObject.isRemoved())
                                                       .map(LogObject::getTransactionHash)
                                                       .map(this::getTransactionReceipt)
                                                       .filter(TransactionReceipt::isStatusOK)
                                                       .flatMap(this::getTransferOwnershipEvents)
                                                       .filter(Objects::nonNull)
                                                       .collect(Collectors.toList());
      return new LinkedHashSet<>(events);
    } catch (IOException e) {
      throw new IllegalStateException("Error retrieving event logs", e);
    }
  }

  /**
   * Retrieves from blockchain whether an address is the provisioning manager of
   * the deed or not
   *
   * @param address Ethereum address to check
   * @param nftId Deed NFT identifier
   * @return true if is manager else false
   */
  public boolean isDeedProvisioningManager(String address, long nftId) {
    return blockchainCall(deedTenantProvisioning.isProvisioningManager(address, BigInteger.valueOf(nftId)));
  }

  /**
   * Retrieves from blockchain whether an address is the owner of the deed
   *
   * @param address Ethereum address to check
   * @param nftId Deed NFT identifier
   * @return true if is owner else false
   */
  public boolean isDeedOwner(String address, long nftId) {
    return blockchainCall(deed.balanceOf(address, BigInteger.valueOf(nftId))).longValue() > 0;
  }

  /**
   * Retrieves from Blockchain DEED card type: - 0 : Common - 1 : Uncommon - 2 :
   * Rare - 3 : Legendary
   *
   * @param nftId Deed NFT identifier
   * @return card type index
   * @throws ObjectNotFoundException when NFT with selected identifier doesn't
   *           exists
   */
  public short getDeedCardType(long nftId) throws ObjectNotFoundException {
    try {
      return deed.cardType(BigInteger.valueOf(nftId)).send().shortValue();
    } catch (Exception e) {
      if (StringUtils.contains(e.getMessage(), "execution reverted")) {
        throw new ObjectNotFoundException(e.getMessage());
      } else {
        throw new IllegalStateException("Error retrieving information 'getDeedCardType' from Blockchain", e);
      }
    }
  }

  /**
   * Retrieves from Blockchain DEED city index: - 0 : Tanit - 1 : Reshef - 2 :
   * Ashtarte - 3 : Melqart - 4 : Eshmun - 5 : Kushor - 6 : Hammon
   *
   * @param nftId Deed NFT identifier
   * @return card city index
   * @throws ObjectNotFoundException when NFT with selected identifier doesn't
   *           exists
   */
  public short getDeedCityIndex(long nftId) throws ObjectNotFoundException {
    try {
      return deed.cityIndex(BigInteger.valueOf(nftId)).send().shortValue();
    } catch (Exception e) {
      if (StringUtils.contains(e.getMessage(), "execution reverted")) {
        throw new ObjectNotFoundException(e.getMessage());
      } else {
        throw new IllegalStateException("Error retrieving information 'getDeedCityIndex' from Blockchain", e);
      }
    }
  }

  /**
   * @return {@link BigDecimal} representing the total supply of Meeds Token
   *         which is retrieved from ethereum blockchain. The retrieved value is
   *         divided by number of decimals of the token (10^18)
   */
  public BigDecimal meedsTotalSupplyNoDecimals() {
    BigInteger totalSupply = meedsTotalSupply();
    return new BigDecimal(totalSupply).divide(BigDecimal.valueOf(10).pow(18));
  }

  /**
   * @return {@link BigInteger} representing the total supply of Meeds Token
   *         which is retrieved from ethereum blockchain.
   */
  public BigInteger meedsTotalSupply() {
    return blockchainCall(ethereumToken.totalSupply());
  }

  /**
   * @return {@link BigInteger} representing the total supply of xMeeds Token
   *         which is retrieved from ethereum blockchain.
   */
  public BigInteger xMeedsTotalSupply() {
    return blockchainCall(xMeedsToken.totalSupply());
  }

  /**
   * @return {@link BigInteger} representing the total supply of xMeeds Token
   *         which is retrieved from ethereum blockchain.
   */
  public BigInteger sushiPairTotalSupply() {
    return blockchainCall(sushiPairToken.totalSupply());
  }

  /**
   * @param address Fund Address to get its rewarding information
   * @return {@link FundInfo} with rewarding parameters retrieved from Token
   *         Factory
   */
  public FundInfo getFundInfo(String address) {
    Tuple5<BigInteger, BigInteger, BigInteger, BigInteger, Boolean> fundInfo = blockchainCall(tokenFactory.fundInfos(address));
    return fundInfo == null ? null
                            : new FundInfo(address,
                                           fundInfo.component1(),
                                           fundInfo.component2(),
                                           fundInfo.component3(),
                                           fundInfo.component4(),
                                           fundInfo.component5());
  }

  /**
   * @return {@link DeedCity} representing current minting city
   */
  public DeedCity getCurrentCity() {
    BigInteger currentCityIndex = blockchainCall(xMeedsToken.currentCityIndex());
    Tuple4<String, BigInteger, BigInteger, BigInteger> cityInfo = blockchainCall(xMeedsToken.cityInfo(currentCityIndex));
    return cityInfo == null ? null
                            : new DeedCity(currentCityIndex,
                                           cityInfo.component1(),
                                           cityInfo.component2(),
                                           cityInfo.component3(),
                                           cityInfo.component4());
  }

  /**
   * @return {@link FundInfo} of xMeed Token with rewarding parameters retrieved
   *         from Token Factory
   */
  public FundInfo getXMeedFundInfo() {
    FundInfo fundInfo = getFundInfo(xMeedsToken.getContractAddress());
    fundInfo.setTotalSupply(xMeedsTotalSupply());
    fundInfo.setXMeedPendingReward(pendingRewardBalanceOf(xMeedsToken.getContractAddress()));
    fundInfo.setMeedsBalance(meedBalanceOf(xMeedsToken.getContractAddress()));
    return fundInfo;
  }

  /**
   * @return {@link FundInfo} of Sushi Pair Token with rewarding parameters
   *         retrieved from Token Factory
   */
  public FundInfo getSushiPairFundInfo() {
    FundInfo fundInfo = getFundInfo(sushiPairToken.getContractAddress());
    fundInfo.setSymbol(sushiPairSymbol());
    fundInfo.setLpBalanceOfTokenFactory(stakedSushiPair());
    fundInfo.setTotalSupply(sushiPairTotalSupply());
    fundInfo.setMeedsBalance(meedBalanceOf(sushiPairToken.getContractAddress()));
    return fundInfo;
  }

  /**
   * @param address Address to get its pending rewardings not minted yet
   * @return {@link BigInteger} for Meed Token value with decimals
   */
  public BigInteger pendingRewardBalanceOf(String address) {
    return blockchainCall(tokenFactory.pendingRewardBalanceOf(address));
  }

  /**
   * @return {@link BigInteger} for total allocation points configured in token
   *         Factory
   */
  public BigInteger totalAllocationPoints() {
    return blockchainCall(tokenFactory.totalAllocationPoints());
  }

  /**
   * @return {@link BigInteger} for total fixed percentages configured in token
   *         Factory
   */
  public BigInteger totalFixedPercentage() {
    return blockchainCall(tokenFactory.totalFixedPercentage());
  }

  /**
   * @param address Ethereum address
   * @return {@link BigDecimal} representing the balance of address which is
   *         retrieved from ethereum blockchain. The retrieved value is divided
   *         by number of decimals of the token (10^18)
   */
  public BigDecimal meedBalanceOfNoDecimals(String address) {
    BigInteger balance = meedBalanceOf(address);
    return new BigDecimal(balance).divide(BigDecimal.valueOf(10).pow(18));
  }

  /**
   * @param address Ethereum address
   * @return {@link BigInteger} representing the balance of address which is
   *         retrieved from ethereum blockchain.
   */
  public BigInteger meedBalanceOf(String address) {
    return blockchainCall(ethereumToken.balanceOf(address));
  }

  /**
   * @return Sushi Swap Pair token symbol
   */
  public String sushiPairSymbol() {
    return blockchainCall(sushiPairToken.symbol());
  }

  /**
   * @return total staked SLP amount in TokenFactory
   */
  public BigInteger stakedSushiPair() {
    return blockchainCall(sushiPairToken.balanceOf(tokenFactory.getContractAddress()));
  }

  /**
   * @param address Ethereum address
   * @return {@link BigDecimal} representing the balance of address which is
   *         retrieved from polygon blockchain. The retrieved value is divided
   *         by number of decimals of the token (10^18)
   */
  public BigDecimal meedBalanceOfOnPolygon(String address) {
    BigInteger balance = blockchainCall(polygonToken.balanceOf(address));
    return new BigDecimal(balance).divide(BigDecimal.valueOf(10).pow(18));
  }

  private Stream<DeedOwnershipTransferEvent> getTransferOwnershipEvents(TransactionReceipt transactionReceipt) {
    List<TransferSingleEventResponse> transferSingleEvents = Deed.getTransferSingleEvents(transactionReceipt);
    if (transferSingleEvents != null && !transferSingleEvents.isEmpty()) {
      return transferSingleEvents.stream()
                                 .map(transferSingleEventResponse -> new DeedOwnershipTransferEvent(transferSingleEventResponse._id.longValue(),
                                                                                                    transferSingleEventResponse._from,
                                                                                                    transferSingleEventResponse._to));
    }
    List<TransferBatchEventResponse> transferBatchEvents = Deed.getTransferBatchEvents(transactionReceipt);
    if (transferBatchEvents != null && !transferBatchEvents.isEmpty()) {
      return transferBatchEvents.stream().flatMap(transferBatchEventResponse -> {
        String from = transferBatchEventResponse._from;
        String to = transferBatchEventResponse._to;
        return transferBatchEventResponse._ids.stream()
                                              .map(nftId -> new DeedOwnershipTransferEvent(nftId.longValue(),
                                                                                           from,
                                                                                           to));
      });
    }
    return Stream.empty();
  }

  private DeedTenant getMinedDeedTenant(TransactionReceipt transactionReceipt) {
    List<TenantStartedEventResponse> startedEvents =
                                                   DeedTenantProvisioning.getTenantStartedEvents(transactionReceipt);
    if (startedEvents != null && !startedEvents.isEmpty()) {
      TenantStartedEventResponse tenantStartedEventResponse = startedEvents.get(0);
      DeedTenant deedTenant = new DeedTenant();
      deedTenant.setNftId(tenantStartedEventResponse.nftId.longValue());
      deedTenant.setStartupTransactionHash(transactionReceipt.getTransactionHash());
      deedTenant.setManagerAddress(tenantStartedEventResponse.manager.toLowerCase());
      return deedTenant;
    }
    List<TenantStoppedEventResponse> endedEvents =
                                                 DeedTenantProvisioning.getTenantStoppedEvents(transactionReceipt);
    if (endedEvents != null && !endedEvents.isEmpty()) {
      TenantStoppedEventResponse tenantStoppedEventResponse = endedEvents.get(0);
      DeedTenant deedTenant = new DeedTenant();
      deedTenant.setNftId(tenantStoppedEventResponse.nftId.longValue());
      deedTenant.setShutdownTransactionHash(transactionReceipt.getTransactionHash());
      deedTenant.setManagerAddress(tenantStoppedEventResponse.manager.toLowerCase());
      return deedTenant;
    }
    return null;
  }

  private TransactionReceipt getTransactionReceipt(String transactionHash) {
    try {
      EthGetTransactionReceipt ethGetTransactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).send();
      if (ethGetTransactionReceipt != null) {
        return ethGetTransactionReceipt.getResult();
      }
    } catch (IOException e) {
      throw new IllegalStateException("Error retrieving Receipt for Transaction with hash: " + transactionHash, e);
    }
    return null;
  }

  private <T> T blockchainCall(RemoteFunctionCall<T> remoteCall) {
    try {
      return remoteCall.send();
    } catch (Exception e) {
      throw new IllegalStateException("Error calling blockchain", e);
    }
  }
}
