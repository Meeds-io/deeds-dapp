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
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthLog.LogObject;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple12;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.tuples.generated.Tuple8;
import org.web3j.utils.Numeric;

import io.meeds.deeds.constant.BlockchainLeaseStatus;
import io.meeds.deeds.constant.BlockchainOfferStatus;
import io.meeds.deeds.constant.CommonConstants.DeedOwnershipTransferEvent;
import io.meeds.deeds.constant.ObjectNotFoundException;
import io.meeds.deeds.contract.Deed;
import io.meeds.deeds.contract.Deed.TransferBatchEventResponse;
import io.meeds.deeds.contract.Deed.TransferSingleEventResponse;
import io.meeds.deeds.contract.DeedRenting;
import io.meeds.deeds.contract.DeedRenting.LeaseEndedEventResponse;
import io.meeds.deeds.contract.DeedRenting.OfferCreatedEventResponse;
import io.meeds.deeds.contract.DeedRenting.OfferDeletedEventResponse;
import io.meeds.deeds.contract.DeedRenting.OfferUpdatedEventResponse;
import io.meeds.deeds.contract.DeedRenting.RentPaidEventResponse;
import io.meeds.deeds.contract.DeedRenting.TenantEvictedEventResponse;
import io.meeds.deeds.contract.DeedTenantProvisioning;
import io.meeds.deeds.contract.DeedTenantProvisioning.TenantStartedEventResponse;
import io.meeds.deeds.contract.DeedTenantProvisioning.TenantStoppedEventResponse;
import io.meeds.deeds.contract.ERC20;
import io.meeds.deeds.contract.MeedsToken;
import io.meeds.deeds.contract.TokenFactory;
import io.meeds.deeds.contract.XMeedsNFTRewarding;
import io.meeds.deeds.elasticsearch.model.DeedCity;
import io.meeds.deeds.elasticsearch.model.DeedTenant;
import io.meeds.deeds.model.DeedLeaseBlockchainState;
import io.meeds.deeds.model.DeedOfferBlockchainState;
import io.meeds.deeds.model.FundInfo;

@Component
public class BlockchainService {

  private static final Logger    LOG = LoggerFactory.getLogger(BlockchainService.class);

  @Autowired
  @Qualifier("ethereumNetwork")
  private Web3j                  web3j;

  @Autowired(required = false)
  private DeedTenantProvisioning deedTenantProvisioning;

  @Autowired(required = false)
  private DeedRenting            deedRenting;

  @Autowired
  private Deed                   deed;

  @Autowired
  private TokenFactory           tokenFactory;

  @Autowired
  private XMeedsNFTRewarding     xMeedsToken;

  @Autowired
  @Qualifier("ethereumMeedToken")
  private MeedsToken             ethereumToken;

  @Autowired
  @Qualifier("polygonMeedToken")
  private MeedsToken             polygonToken;

  @Autowired
  @Qualifier("sushiPairToken")
  private ERC20                  sushiPairToken;

  private long                   networkId;

  /**
   * Return DEED Tenant Status from Blockchain Contract
   *
   * @param  nftId Deed NFT identifier
   * @return       if marked as started else false
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
   * @param  transactionHash Blockchain Transaction Hash
   * @return                 true if Transaction is Mined
   */
  public boolean isTransactionMined(String transactionHash) {
    TransactionReceipt receipt = getTransactionReceipt(transactionHash);
    return receipt != null;
  }

  /**
   * @param  transactionHash Blockchain Transaction Hash
   * @return                 true if Transaction is Successful
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
   * @param  fromBlock Start block
   * @param  toBlock   End Block to filter
   * @return           {@link List} of {@link DeedTenant}
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
   * Retrieves Lease and Offer Events occurred on Renting Contract
   * 
   * @param  fromBlock Start block
   * @param  toBlock   End Block to filter
   * @return           {@link List} of {@link Map} of events
   */
  public List<Map<?, ?>> getMinedRentingTransactions(long fromBlock, // NOSONAR
                                                     long toBlock) {
    EthFilter ethFilter = new EthFilter(new DefaultBlockParameterNumber(fromBlock),
                                        new DefaultBlockParameterNumber(toBlock),
                                        deedRenting.getContractAddress());
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
                    .flatMap(transactionHash -> {
                      Map<?, ?> offerEvents = getOfferTransactionEvents(transactionHash);
                      Map<?, ?> leaseEvents = getLeaseTransactionEvents(transactionHash);
                      return MapUtils.isEmpty(offerEvents) ? Stream.of(leaseEvents)
                                                           : MapUtils.isEmpty(leaseEvents) ? Stream.of(offerEvents)// NOSONAR
                                                                                           : Stream.of(offerEvents, leaseEvents);
                    })
                    .filter(MapUtils::isNotEmpty)
                    .collect(Collectors.toList());
    } catch (IOException e) {
      throw new IllegalStateException("Error retrieving event logs of mined transactions", e);
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

  public Map<BlockchainOfferStatus, DeedOfferBlockchainState> getOfferTransactionEvents(String transactionHash) {// NOSONAR
    try {
      TransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash)
                                                   .send()
                                                   .getTransactionReceipt()
                                                   .orElse(null);
      if (transactionReceipt == null || !transactionReceipt.isStatusOK()) {
        return Collections.emptyMap();
      }

      Map<BlockchainOfferStatus, DeedOfferBlockchainState> events = new EnumMap<>(BlockchainOfferStatus.class);
      List<OfferCreatedEventResponse> createdEvents = DeedRenting.getOfferCreatedEvents(transactionReceipt);
      if (createdEvents != null && !createdEvents.isEmpty()) {
        if (createdEvents.size() > 1) {
          LOG.warn("It seems that in a single transaction, we have more than one offer creation, {} events. This can't be handled, only first one will be handled",
                   createdEvents.size());
        }
        OfferCreatedEventResponse response = createdEvents.get(0);
        DeedOfferBlockchainState deedOffer = getOfferById(response.id, transactionReceipt.getBlockNumber(), transactionHash);
        events.put(BlockchainOfferStatus.OFFER_CREATED, deedOffer);
      }
      List<OfferUpdatedEventResponse> updatedEvents = DeedRenting.getOfferUpdatedEvents(transactionReceipt);
      if (updatedEvents != null && !updatedEvents.isEmpty()) {
        if (updatedEvents.size() > 1) {
          LOG.warn("It seems that in a single transaction, we have more than one offer update, {} events. This can't be handled, only first one will be handled",
                   updatedEvents.size());
        }
        OfferUpdatedEventResponse response = updatedEvents.get(0);
        DeedOfferBlockchainState deedOffer = getOfferById(response.id, transactionReceipt.getBlockNumber(), transactionHash);
        events.put(BlockchainOfferStatus.OFFER_UPDATED, deedOffer);
      }
      List<OfferDeletedEventResponse> deletedEvents = DeedRenting.getOfferDeletedEvents(transactionReceipt);
      if (deletedEvents != null && !deletedEvents.isEmpty()) {
        if (deletedEvents.size() > 1) {
          LOG.warn("It seems that in a single transaction, we have more than one offer delete, {} events. This can't be handled, only first one will be handled",
                   deletedEvents.size());
        }
        OfferDeletedEventResponse response = deletedEvents.get(0);
        DeedOfferBlockchainState deedOffer = new DeedOfferBlockchainState(response.id,
                                                                          transactionReceipt.getBlockNumber(),
                                                                          response.deedId,
                                                                          response.owner,
                                                                          BigInteger.ZERO,
                                                                          BigInteger.ZERO,
                                                                          BigInteger.ZERO,
                                                                          BigInteger.ZERO,
                                                                          BigInteger.ZERO,
                                                                          BigInteger.ZERO,
                                                                          BigInteger.ZERO,
                                                                          Address.DEFAULT.getValue(),
                                                                          BigInteger.ZERO,
                                                                          StringUtils.lowerCase(transactionHash));
        events.put(BlockchainOfferStatus.OFFER_DELETED, deedOffer);
      }
      List<RentPaidEventResponse> rentPaidEvents = DeedRenting.getRentPaidEvents(transactionReceipt);
      if (rentPaidEvents != null && !rentPaidEvents.isEmpty()) {
        if (rentPaidEvents.size() > 1) {
          LOG.warn("It seems that in a single transaction, we have more than one rent paid {} events. This can't be handled, only first one will be handled",
                   rentPaidEvents.size());
        }
        RentPaidEventResponse response = rentPaidEvents.get(0);
        DeedOfferBlockchainState deedOffer = getOfferById(response.id, transactionReceipt.getBlockNumber(), transactionHash);
        if (response.firstRent.booleanValue()) {
          events.put(BlockchainOfferStatus.OFFER_ACQUIRED, deedOffer);
        }
      }
      return events;
    } catch (Exception e) {
      throw new IllegalStateException("Error retrieving transaction receipt " + transactionHash + " logs", e);
    }
  }

  public Map<BlockchainLeaseStatus, DeedLeaseBlockchainState> getLeaseTransactionEvents(String transactionHash) { // NOSONAR
    try {
      TransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash)
                                                   .send()
                                                   .getTransactionReceipt()
                                                   .orElse(null);
      if (transactionReceipt == null || !transactionReceipt.isStatusOK()) {
        return Collections.emptyMap();
      }

      Map<BlockchainLeaseStatus, DeedLeaseBlockchainState> events = new EnumMap<>(BlockchainLeaseStatus.class);
      List<RentPaidEventResponse> rentPaidEvents = DeedRenting.getRentPaidEvents(transactionReceipt);
      if (rentPaidEvents != null && !rentPaidEvents.isEmpty()) {
        if (rentPaidEvents.size() > 1) {
          LOG.warn("It seems that in a single transaction, we have more than one rent paid {} events. This can't be handled, only first one will be handled",
                   rentPaidEvents.size());
        }
        RentPaidEventResponse response = rentPaidEvents.get(0);
        DeedLeaseBlockchainState deedLease = getLeaseById(response.id, transactionReceipt.getBlockNumber(), transactionHash);
        if (response.firstRent.booleanValue()) {
          events.put(BlockchainLeaseStatus.LEASE_ACQUIRED, deedLease);
        } else {
          events.put(BlockchainLeaseStatus.LEASE_PAYED, deedLease);
        }
      }
      List<LeaseEndedEventResponse> leaseEndedEvents = DeedRenting.getLeaseEndedEvents(transactionReceipt);
      if (leaseEndedEvents != null && !leaseEndedEvents.isEmpty()) {
        if (leaseEndedEvents.size() > 1) {
          LOG.warn("It seems that in a single transaction, we have more than one lease ended {} events. This can't be handled, only first one will be handled",
                   leaseEndedEvents.size());
        }
        LeaseEndedEventResponse response = leaseEndedEvents.get(0);
        DeedLeaseBlockchainState deedLease = getLeaseById(response.id, transactionReceipt.getBlockNumber(), transactionHash);
        events.put(BlockchainLeaseStatus.LEASE_ENDED, deedLease);
      }
      List<TenantEvictedEventResponse> tenantEvictedEvents = DeedRenting.getTenantEvictedEvents(transactionReceipt);
      if (tenantEvictedEvents != null && !tenantEvictedEvents.isEmpty()) {
        if (tenantEvictedEvents.size() > 1) {
          LOG.warn("It seems that in a single transaction, we have more than one tenant evicted {} events. This can't be handled, only first one will be handled",
                   tenantEvictedEvents.size());
        }
        TenantEvictedEventResponse response = tenantEvictedEvents.get(0);
        DeedLeaseBlockchainState deedLease = getLeaseById(response.id, transactionReceipt.getBlockNumber(), transactionHash);
        events.put(BlockchainLeaseStatus.LEASE_MANAGER_EVICTED, deedLease);
      }
      return events;
    } catch (Exception e) {
      throw new IllegalStateException("Error retrieving transaction receipt " + transactionHash + " logs", e);
    }
  }

  public DeedOfferBlockchainState getOfferById(BigInteger offerId,
                                               BigInteger blockNumber,
                                               String transactionHash) throws Exception {
    Tuple12<BigInteger, BigInteger, String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, String, BigInteger> offerTuple =
                                                                                                                                                               deedRenting.deedOffers(offerId)
                                                                                                                                                                          .send();

    if (StringUtils.isBlank(transactionHash)) {
      transactionHash = getOfferCreationTransactionHash(offerId);
    }
    return new DeedOfferBlockchainState(offerTuple.component1(),
                                        blockNumber == null ? BigInteger.ZERO : blockNumber,
                                        offerTuple.component2(),
                                        offerTuple.component3(),
                                        offerTuple.component4(),
                                        offerTuple.component5(),
                                        offerTuple.component6(),
                                        offerTuple.component7(),
                                        offerTuple.component8(),
                                        offerTuple.component9(),
                                        offerTuple.component10(),
                                        offerTuple.component11(),
                                        offerTuple.component12(),
                                        StringUtils.lowerCase(transactionHash));
  }

  public DeedLeaseBlockchainState getLeaseById(BigInteger leaseId, BigInteger blockNumber,
                                               String transactionHash) throws Exception {
    Tuple8<BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, String> leaseTuple =
                                                                                                                  deedRenting.deedLeases(leaseId)
                                                                                                                             .send();
    return new DeedLeaseBlockchainState(leaseTuple.component1(),
                                        blockNumber == null ? BigInteger.ZERO : blockNumber,
                                        leaseTuple.component2(),
                                        leaseTuple.component3(),
                                        leaseTuple.component4(),
                                        leaseTuple.component5(),
                                        leaseTuple.component6(),
                                        leaseTuple.component7(),
                                        leaseTuple.component8(),
                                        StringUtils.lowerCase(transactionHash));
  }

  /**
   * Retrieves from blockchain whether an address is the provisioning manager of
   * the deed or not
   *
   * @param  address Ethereum address to check
   * @param  nftId   Deed NFT identifier
   * @return         true if is manager else false
   */
  public boolean isDeedProvisioningManager(String address, long nftId) {
    return blockchainCall(deedTenantProvisioning.isProvisioningManager(address, BigInteger.valueOf(nftId)));
  }

  /**
   * Retrieves from blockchain whether an address is the owner of the deed
   *
   * @param  address Ethereum address to check
   * @param  nftId   Deed NFT identifier
   * @return         true if is owner else false
   */
  public boolean isDeedOwner(String address, long nftId) {
    return blockchainCall(deed.balanceOf(address, BigInteger.valueOf(nftId))).longValue() > 0;
  }

  /**
   * Retrieves from Blockchain DEED card type: - 0 : Common - 1 : Uncommon - 2 :
   * Rare - 3 : Legendary
   *
   * @param  nftId                   Deed NFT identifier
   * @return                         card type index
   * @throws ObjectNotFoundException when NFT with selected identifier doesn't
   *                                   exists
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

  public boolean isOfferEnabled(long offerId) throws Exception {
    DeedOfferBlockchainState offer = getOfferById(BigInteger.valueOf(offerId), null, "0x");
    if (offer != null && offer.getId().longValue() == offerId) {
      DeedLeaseBlockchainState lease = getLeaseById(BigInteger.valueOf(offerId), null, null);
      return lease == null || lease.getId().longValue() == 0;
    }
    return false;
  }

  /**
   * Retrieves from Blockchain DEED city index: - 0 : Tanit - 1 : Reshef - 2 :
   * Ashtarte - 3 : Melqart - 4 : Eshmun - 5 : Kushor - 6 : Hammon
   *
   * @param  nftId                   Deed NFT identifier
   * @return                         card city index
   * @throws ObjectNotFoundException when NFT with selected identifier doesn't
   *                                   exists
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
   * @param  address Fund Address to get its rewarding information
   * @return         {@link FundInfo} with rewarding parameters retrieved from
   *                 Token Factory
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
   * @param  address Address to get its pending rewardings not minted yet
   * @return         {@link BigInteger} for Meed Token value with decimals
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
   * @param  address Ethereum address
   * @return         {@link BigDecimal} representing the balance of address
   *                 which is retrieved from ethereum blockchain. The retrieved
   *                 value is divided by number of decimals of the token (10^18)
   */
  public BigDecimal meedBalanceOfNoDecimals(String address) {
    BigInteger balance = meedBalanceOf(address);
    return new BigDecimal(balance).divide(BigDecimal.valueOf(10).pow(18));
  }

  /**
   * @param  address Ethereum address
   * @return         {@link BigInteger} representing the balance of address
   *                 which is retrieved from ethereum blockchain.
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
   * @param  address Ethereum address
   * @return         {@link BigDecimal} representing the balance of address
   *                 which is retrieved from polygon blockchain. The retrieved
   *                 value is divided by number of decimals of the token (10^18)
   */
  public BigDecimal meedBalanceOfOnPolygon(String address) {
    BigInteger balance = blockchainCall(polygonToken.balanceOf(address));
    return new BigDecimal(balance).divide(BigDecimal.valueOf(10).pow(18));
  }

  public long getNetworkId() throws IOException {
    if (networkId == 0) {
      networkId = new BigInteger(web3j.netVersion().send().getNetVersion()).longValue();
    }
    return networkId;
  }

  @SuppressWarnings("rawtypes")
  public String getOfferCreationTransactionHash(BigInteger offerId) throws IOException {
    try {
      EthFilter ethFilter = new EthFilter(DefaultBlockParameterName.EARLIEST,
                                          DefaultBlockParameterName.LATEST,
                                          deedRenting.getContractAddress()).addSingleTopic(EventEncoder.encode(DeedRenting.OFFERCREATED_EVENT))
                                                                           .addOptionalTopics(Numeric.toHexStringWithPrefixZeroPadded(offerId,
                                                                                                                                      64));
      EthLog ethLog = web3j.ethGetLogs(ethFilter).send();
      List<LogResult> logs = ethLog.getLogs();
      if (CollectionUtils.isNotEmpty(logs)) {
        LogResult logResult = logs.get(0);
        LogObject logObject = (LogObject) logResult.get();
        return logObject.getTransactionHash();
      }
    } catch (Exception e) {
      LOG.warn("Error retrieving Offer Creation Hash, return null instead", e);
    }
    return null;
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
