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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthLog.LogObject;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple10;
import org.web3j.tuples.generated.Tuple12;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.tuples.generated.Tuple7;
import org.web3j.tuples.generated.Tuple8;
import org.web3j.tuples.generated.Tuple9;
import org.web3j.utils.EnsUtils;
import org.web3j.utils.Numeric;

import io.meeds.deeds.common.constant.BlockchainLeaseStatus;
import io.meeds.deeds.common.constant.BlockchainOfferStatus;
import io.meeds.deeds.common.constant.CommonConstants.DeedOwnershipTransferEvent;
import io.meeds.deeds.common.elasticsearch.model.DeedTenant;
import io.meeds.deeds.common.model.DeedCity;
import io.meeds.deeds.common.model.DeedLeaseBlockchainState;
import io.meeds.deeds.common.model.DeedOfferBlockchainState;
import io.meeds.deeds.common.model.FundInfo;
import io.meeds.deeds.common.model.WomDeed;
import io.meeds.deeds.common.model.WomHub;
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
import io.meeds.deeds.contract.UserEngagementMinting;
import io.meeds.deeds.contract.WoM;
import io.meeds.deeds.contract.WoM.HubConnectedEventResponse;
import io.meeds.deeds.contract.WoM.HubDisconnectedEventResponse;
import io.meeds.deeds.contract.XMeedsNFTRewarding;
import io.meeds.wom.api.constant.ObjectNotFoundException;
import io.meeds.wom.api.constant.WomException;
import io.meeds.wom.api.model.HubReport;
import io.meeds.wom.api.model.UemReward;

import lombok.SneakyThrows;

@Component
public class BlockchainService {

  private static final Logger    LOG = LoggerFactory.getLogger(BlockchainService.class);

  @Autowired
  @Qualifier("ethereumNetwork")
  private Web3j                  web3j;

  @Autowired
  @Qualifier("polygonNetwork")
  private Web3j                  polygonWeb3j;

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

  @Autowired(required = false)
  private UserEngagementMinting  uemContract;

  @Autowired(required = false)
  @Qualifier("womContractReadOnly")
  private WoM                    womContract;

  @Autowired(required = false)
  @Qualifier("womContractReadWrite")
  private WoM                    womContractWithManager;

  @Autowired
  @Qualifier("ethereumMeedToken")
  private MeedsToken             ethereumToken;

  @Autowired
  @Qualifier("polygonMeedToken")
  private MeedsToken             polygonToken;

  @Autowired
  @Qualifier("sushiPairToken")
  private ERC20                  sushiPairToken;

  private long                   ethereumNetworkId;

  private long                   polygonNetworkId;

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

  public boolean isPolygonTransactionMined(String transactionHash) {
    TransactionReceipt receipt = getPolygonTransactionReceipt(transactionHash);
    return receipt != null;
  }

  /**
   * @param transactionHash Blockchain Transaction Hash
   * @return true if Transaction is Successful
   */
  public boolean isPolygonTransactionConfirmed(String transactionHash) {
    TransactionReceipt receipt = getPolygonTransactionReceipt(transactionHash);
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
   * @return last mined block number on Polygon Blockchain
   */
  public long getPolygonLastBlock() {
    try {
      return polygonWeb3j.ethBlockNumber().send().getBlockNumber().longValue();
    } catch (IOException e) {
      throw new IllegalStateException("Error getting last block number", e);
    }
  }

  public String getDeedManager(long deedId) {
    EthFilter ethFilter = new EthFilter(DefaultBlockParameterName.EARLIEST,
                                        DefaultBlockParameterName.LATEST,
                                        deedTenantProvisioning.getContractAddress()).addSingleTopic(EventEncoder.encode(DeedTenantProvisioning.DELEGATEEADDED_EVENT));
    try {
      EthLog ethLog = web3j.ethGetLogs(ethFilter).send();
      @SuppressWarnings("rawtypes")
      List<LogResult> ethLogs = ethLog.getLogs();
      if (CollectionUtils.isEmpty(ethLogs)) {
        return getDeedOwner(deedId);
      }
      return ethLogs.stream()
                    .map(logResult -> (LogObject) logResult.get())
                    .filter(logObject -> !logObject.isRemoved())
                    .map(LogObject::getTransactionHash)
                    .map(this::getTransactionReceipt)
                    .filter(TransactionReceipt::isStatusOK)
                    .map(this::getMinedDeedTenant)
                    .filter(Objects::nonNull)
                    .distinct()
                    .filter(tenant -> tenant.getNftId() == deedId
                                      && !StringUtils.equalsIgnoreCase(tenant.getManagerAddress(), EnsUtils.EMPTY_ADDRESS)
                                      && isDeedProvisioningManager(tenant.getManagerAddress(), deedId))
                    .findFirst()
                    .map(DeedTenant::getManagerAddress)
                    .orElseGet(() -> this.getDeedOwner(deedId));
    } catch (IOException e) {
      throw new IllegalStateException("Error retrieving event logs for deedTenantProvisioning.getDeedManager", e);
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
                    .toList();
    } catch (IOException e) {
      throw new IllegalStateException("Error retrieving event logs", e);
    }
  }

  @SneakyThrows
  public List<? extends BaseEventResponse> getMinedUemLogs(long fromBlock, long toBlock) { // NOSONAR
    EthFilter ethFilter = new EthFilter(fromBlock == 0 ? DefaultBlockParameterName.EARLIEST :
                                                       new DefaultBlockParameterNumber(fromBlock),
                                        new DefaultBlockParameterNumber(toBlock),
                                        womContract.getContractAddress()).addOptionalTopics(EventEncoder.encode(WoM.HUBCONNECTED_EVENT),
                                                                                            EventEncoder.encode(WoM.HUBDISCONNECTED_EVENT));
    EthLog ethLog = polygonWeb3j.ethGetLogs(ethFilter).send();
    @SuppressWarnings("rawtypes")
    List<LogResult> ethLogs = ethLog.getLogs();
    if (CollectionUtils.isEmpty(ethLogs)) {
      return Collections.emptyList();
    }
    return ethLogs.stream()
                  .map(logResult -> (LogObject) logResult.get())
                  .filter(logObject -> !logObject.isRemoved())
                  .map(LogObject::getTransactionHash)
                  .map(this::getPolygonTransactionReceipt)
                  .filter(Objects::nonNull)
                  .filter(TransactionReceipt::isStatusOK)
                  .map(this::getUemLogs)
                  .flatMap(List::stream)
                  .filter(Objects::nonNull)
                  .toList();
  }

  @SneakyThrows
  public Set<String> getMinedHubConnectionTransactions(long fromBlock, long toBlock) {
    EthFilter ethFilter = new EthFilter(fromBlock == 0 ? DefaultBlockParameterName.EARLIEST :
                                                       new DefaultBlockParameterNumber(fromBlock),
                                        new DefaultBlockParameterNumber(toBlock),
                                        womContract.getContractAddress()).addOptionalTopics(EventEncoder.encode(WoM.HUBCONNECTED_EVENT),
                                                                                            EventEncoder.encode(WoM.HUBDISCONNECTED_EVENT));
    EthLog ethLog = polygonWeb3j.ethGetLogs(ethFilter).send();
    @SuppressWarnings("rawtypes")
    List<LogResult> ethLogs = ethLog.getLogs();
    if (CollectionUtils.isEmpty(ethLogs)) {
      return Collections.emptySet();
    }
    return ethLogs.stream()
                  .map(logResult -> (LogObject) logResult.get())
                  .filter(logObject -> !logObject.isRemoved())
                  .map(LogObject::getTransactionHash)
                  .map(this::getPolygonTransactionReceipt)
                  .filter(Objects::nonNull)
                  .filter(TransactionReceipt::isStatusOK)
                  .map(this::getHubAddresses)
                  .filter(CollectionUtils::isNotEmpty)
                  .flatMap(List::stream)
                  .filter(StringUtils::isNotBlank)
                  .collect(Collectors.toSet());
  }

  /**
   * Retrieves Lease and Offer Events occurred on Renting Contract
   * 
   * @param fromBlock Start block
   * @param toBlock End Block to filter
   * @return {@link List} of {@link Map} of events
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
                      return MapUtils.isEmpty(offerEvents) ? Stream.of(leaseEvents) :
                                     MapUtils.isEmpty(leaseEvents) ? Stream.of(offerEvents)// NOSONAR
                                     : Stream.of(offerEvents, leaseEvents);
                    })
                    .filter(MapUtils::isNotEmpty)
                    .toList();
    } catch (IOException e) {
      throw new IllegalStateException("Error retrieving event logs of mined transactions", e);
    }
  }

  public String getDeedOwner(long deedId) {
    EthFilter ethFilter = new EthFilter(DefaultBlockParameterName.EARLIEST,
                                        DefaultBlockParameterName.LATEST,
                                        deed.getContractAddress()).addOptionalTopics(EventEncoder.encode(Deed.TRANSFERSINGLE_EVENT),
                                                                                     EventEncoder.encode(Deed.TRANSFERBATCH_EVENT));
    try {
      EthLog ethLog = web3j.ethGetLogs(ethFilter).send();
      @SuppressWarnings("rawtypes")
      List<LogResult> ethLogs = ethLog.getLogs();
      if (CollectionUtils.isEmpty(ethLogs)) {
        throw new IllegalStateException("Should never happen: can't find owner by Blockchain Logs of the Deed " + deedId);
      }
      return ethLogs.stream()
                    .map(logResult -> (LogObject) logResult.get())
                    .filter(logObject -> !logObject.isRemoved())
                    .map(LogObject::getTransactionHash)
                    .map(this::getTransactionReceipt)
                    .filter(TransactionReceipt::isStatusOK)
                    .flatMap(this::getTransferOwnershipEvents)
                    .filter(Objects::nonNull)
                    .map(DeedOwnershipTransferEvent::getTo)
                    .distinct()
                    .filter(address -> isDeedOwner(address, deedId))
                    .findFirst()
                    .orElseThrow();
    } catch (IOException e) {
      throw new IllegalStateException("Error retrieving event logs of deed ownership : " + deedId, e);
    }
  }

  /**
   * Retrieves the list of mined ownership transfer of a Deed transactions
   * starting from a block to another
   * 
   * @param fromBlock Start block
   * @param toBlock End Block to filter
   * @return {@link Set} of NFT ID of type {@link DeedOwnershipTransferEvent}
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
                                                       .toList();
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

  public DeedLeaseBlockchainState getLeaseById(BigInteger leaseId,
                                               BigInteger blockNumber,
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
   * @param address Ethereum address to check
   * @param nftId Deed NFT identifier
   * @return true if is manager else false
   */
  public boolean isDeedProvisioningManager(String address, long nftId) {
    return WalletUtils.isValidAddress(address)
           && blockchainCall(deedTenantProvisioning.isProvisioningManager(address, BigInteger.valueOf(nftId)));
  }

  public double getPendingRewards(String address) {
    return WalletUtils.isValidAddress(address) ?
                                               org.exoplatform.wallet.utils.WalletUtils.convertFromDecimals(blockchainCall(uemContract.pendingRewardBalanceOf(address)),
                                                                                                            18) :
                                               0d;
  }

  /**
   * Retrieves from blockchain whether an address is the owner of the deed
   *
   * @param address Ethereum address to check
   * @param nftId Deed NFT identifier
   * @return true if is owner else false
   */
  public boolean isDeedOwner(String address, long nftId) {
    return WalletUtils.isValidAddress(address)
           && blockchainCall(deed.balanceOf(address, BigInteger.valueOf(nftId))).longValue() > 0;
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

  @SneakyThrows
  @SuppressWarnings("unchecked")
  public List<BigInteger> getDeedsOwnedBy(String managerAddress) {
    return deed.nftsOf(managerAddress).send();
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
    return new BigDecimal(totalSupply).divide(BigDecimal.valueOf(10).pow(18),
                                              MathContext.DECIMAL128);
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
    return fundInfo == null ? null :
                            new FundInfo(address,
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
    return cityInfo == null ? null :
                            new DeedCity(currentCityIndex,
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

  @SneakyThrows
  public String getHubOwner(String address) {
    if (womContract == null) {
      return null;
    }
    Tuple4<BigInteger, String, Boolean, BigInteger> hubTuple = womContract.hubs(address).send();
    String owner = hubTuple == null ? null : hubTuple.component2();
    return StringUtils.equals(address, EnsUtils.EMPTY_ADDRESS) ? null : owner;
  }

  @SneakyThrows
  public String getHubByDeedId(long nftId) {
    Tuple9<BigInteger, BigInteger, BigInteger, BigInteger, String, String, String, BigInteger, BigInteger> deedTuple =
                                                                                                                     womContract.nfts(BigInteger.valueOf(nftId))
                                                                                                                                .send();
    return getHubAddressIfConnected(deedTuple == null ? null : deedTuple.component7());
  }

  @SneakyThrows
  public WomDeed getWomDeed(long nftId) {
    if (womContract == null) {
      return null;
    }
    Tuple9<BigInteger, BigInteger, BigInteger, BigInteger, String, String, String, BigInteger, BigInteger> deedTuple =
                                                                                                                     womContract.nfts(BigInteger.valueOf(nftId))
                                                                                                                                .send();
    if (deedTuple == null) {
      return null;
    } else {
      return new WomDeed(deedTuple.component1().shortValue(),
                         deedTuple.component2().shortValue(),
                         deedTuple.component3().shortValue() / 10d,
                         deedTuple.component4().longValue(),
                         deedTuple.component5(),
                         deedTuple.component6(),
                         getHubAddressIfConnected(deedTuple.component7()),
                         deedTuple.component8().shortValue());
    }
  }

  public void autoConnectToWom(long deedId, // NOSONAR
                               short city,
                               short cardType,
                               short mintingPower,
                               long maxUsers,
                               String ownerAddress,
                               String managerAddress,
                               String hubAddress,
                               short ownerMintingPercentage) throws WomException {
    updateDeedStatusOnWom(deedId,
                          city,
                          cardType,
                          mintingPower,
                          maxUsers,
                          ownerAddress,
                          managerAddress,
                          hubAddress,
                          ownerMintingPercentage);
  }

  public void updateWomDeed(long deedId, // NOSONAR
                            short city,
                            short cardType,
                            short mintingPower,
                            long maxUsers,
                            String ownerAddress,
                            String managerAddress,
                            short ownerMintingPercentage) throws WomException {
    updateDeedStatusOnWom(deedId,
                          city,
                          cardType,
                          mintingPower,
                          maxUsers,
                          ownerAddress,
                          managerAddress,
                          EnsUtils.EMPTY_ADDRESS,
                          ownerMintingPercentage);
  }

  @SneakyThrows
  public WomHub getHub(String address) {
    if (womContract == null) {
      return null;
    }
    Tuple4<BigInteger, String, Boolean, BigInteger> hubTuple = womContract.hubs(address).send();
    if (hubTuple == null) {
      return null;
    } else {
      return new WomHub(hubTuple.component1().longValue(),
                        hubTuple.component2(),
                        isHubConnected(address),
                        hubTuple.component4().longValue());
    }
  }

  @SneakyThrows
  public double getUemPeriodicRewardAmount() {
    if (uemContract == null) {
      return 0d;
    }
    BigInteger periodicRewardAmount = uemContract.periodicRewardAmount().send();
    return BigDecimal.valueOf(periodicRewardAmount.doubleValue())
                     .divide(BigDecimal.valueOf(10l).pow(18),
                             MathContext.DECIMAL128)
                     .doubleValue();
  }

  @SneakyThrows
  public void retrieveRewardProperties(UemReward reward) {
    if (uemContract == null) {
      return;
    }
    BigInteger rewardId = BigInteger.valueOf(reward.getRewardId());
    Tuple7<BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger> uemReward =
                                                                                                         uemContract.rewards(rewardId)
                                                                                                                    .send();
    reward.setAmount(new BigDecimal(uemReward.component1()).divide(BigDecimal.valueOf(10).pow(18),
                                                                   MathContext.DECIMAL128)
                                                           .doubleValue());
    long fromReport = uemReward.component3().longValue();
    long toReport = uemReward.component4().longValue();
    List<Long> reportIds = new ArrayList<>();
    if (fromReport > 0) {
      for (long i = fromReport; i <= toReport; i++) {
        reportIds.add(i);
      }
    }
    reward.setReportIds(reportIds);
    reward.setFixedGlobalIndex(new BigDecimal(uemReward.component5()).divide(BigDecimal.valueOf(10).pow(18),
                                                                             MathContext.DECIMAL128)
                                                                     .doubleValue());
    reward.setFromDate(Instant.ofEpochSecond(uemReward.component6().longValue()));
    reward.setToDate(Instant.ofEpochSecond(uemReward.component7().longValue()));
  }

  @SneakyThrows
  public HubReport retrieveReportProperties(long reportId) {
    if (uemContract == null) {
      return null;
    }
    HubReport report = new HubReport();
    report.setReportId(reportId);
    retrieveReportProperties(report);
    return report;
  }

  @SneakyThrows
  public void retrieveReportProperties(HubReport report) {
    if (uemContract == null) {
      return;
    }
    BigInteger reportId = BigInteger.valueOf(report.getReportId());
    Tuple10<String, BigInteger, BigInteger, BigInteger, BigInteger, BigInteger, String, BigInteger, BigInteger, BigInteger> hubReport =
                                                                                                                                      uemContract.hubReports(reportId)
                                                                                                                                                 .send();
    if (StringUtils.equals(hubReport.component1(), EnsUtils.EMPTY_ADDRESS)) {
      throw new IllegalStateException("Report with id " + report.getReportId() + " doesn't exist in Blockchain");
    }
    report.setHubAddress(StringUtils.lowerCase(hubReport.component1()));
    report.setUsersCount(hubReport.component2().longValue());
    report.setRecipientsCount(hubReport.component3().longValue());
    report.setParticipantsCount(hubReport.component4().longValue());
    report.setAchievementsCount(hubReport.component5().longValue());
    report.setHubRewardAmount(new BigDecimal(hubReport.component6()).divide(BigDecimal.valueOf(10).pow(18),
                                                                            MathContext.DECIMAL128)
                                                                    .doubleValue());
    report.setRewardTokenAddress(StringUtils.lowerCase(hubReport.component7()));
    report.setRewardTokenNetworkId(hubReport.component8().longValue());
    report.setFromDate(Instant.ofEpochSecond(hubReport.component9().longValue()));
    report.setToDate(Instant.ofEpochSecond(hubReport.component10().longValue()));

    Tuple9<BigInteger, String, String, BigInteger, BigInteger, BigInteger, BigInteger, Boolean, BigInteger> hubReportReward =
                                                                                                                            uemContract.hubRewards(reportId)
                                                                                                                                       .send();
    report.setRewardId(hubReportReward.component1().longValue());
    report.setOwnerAddress(StringUtils.lowerCase(hubReportReward.component2()));
    report.setDeedManagerAddress(StringUtils.lowerCase(hubReportReward.component3()));
    report.setFixedRewardIndex(new BigDecimal(hubReportReward.component4()).divide(BigDecimal.valueOf(10).pow(18),
                                                                                   MathContext.DECIMAL128)
                                                                           .doubleValue());
    report.setOwnerFixedIndex(new BigDecimal(hubReportReward.component5()).divide(BigDecimal.valueOf(10).pow(18),
                                                                                  MathContext.DECIMAL128)
                                                                          .doubleValue());
    report.setTenantFixedIndex(new BigDecimal(hubReportReward.component6()).divide(BigDecimal.valueOf(10).pow(18),
                                                                                   MathContext.DECIMAL128)
                                                                           .doubleValue());
    report.setOwnerMintingPercentage(new BigDecimal(hubReportReward.component5()).multiply(BigDecimal.valueOf(100))
                                                                                 .divide(new BigDecimal(hubReportReward.component4()),
                                                                                         MathContext.DECIMAL128)
                                                                                 .setScale(0, RoundingMode.HALF_EVEN)
                                                                                 .toBigInteger()
                                                                                 .intValue());
    report.setSentDate(Instant.ofEpochSecond(hubReportReward.component7().longValue()));
    report.setFraud(hubReportReward.component8());
    report.setLastPeriodUemRewardAmount(new BigDecimal(hubReportReward.component9()).divide(BigDecimal.valueOf(10).pow(18),
                                                                                            MathContext.DECIMAL128)
                                                                                    .doubleValue());

    Tuple5<BigInteger, BigInteger, BigInteger, BigInteger, BigInteger> reportDeed = uemContract.hubDeeds(reportId)
                                                                                               .send();
    report.setDeedId(reportDeed.component1().longValue());
    report.setCity(reportDeed.component2().shortValue());
    report.setCardType(reportDeed.component3().shortValue());
    report.setMintingPower(reportDeed.component4().shortValue());
    report.setMaxUsers(reportDeed.component5().longValue());
  }

  @SneakyThrows
  public boolean isReportFraud(long reportId) {
    return uemContract.hubRewards(BigInteger.valueOf(reportId))
                      .send()
                      .component8();
  }

  @SneakyThrows
  public boolean isHubConnected(String address) {
    return womContract.isHubConnected(address).send().booleanValue();
  }

  public String getEthereumMeedTokenAddress() {
    return ethereumToken.getContractAddress();
  }

  public String getPolygonMeedTokenAddress() {
    return polygonToken.getContractAddress();
  }

  public String getUemAddress() {
    return uemContract == null ? null : uemContract.getContractAddress();
  }

  public String getWomAddress() {
    return womContract == null ? null : womContract.getContractAddress();
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
    return new BigDecimal(balance).divide(BigDecimal.valueOf(10).pow(18),
                                          MathContext.DECIMAL128);
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
    return new BigDecimal(balance).divide(BigDecimal.valueOf(10).pow(18),
                                          MathContext.DECIMAL128);
  }

  @SneakyThrows
  public long getNetworkId() {
    if (ethereumNetworkId == 0) {
      ethereumNetworkId = new BigInteger(web3j.netVersion().send().getNetVersion()).longValue();
    }
    return ethereumNetworkId;
  }

  @SneakyThrows
  public long getPolygonNetworkId() {
    if (polygonNetworkId == 0) {
      polygonNetworkId = new BigInteger(polygonWeb3j.netVersion().send().getNetVersion()).longValue();
    }
    return polygonNetworkId;
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

  private List<String> getHubAddresses(TransactionReceipt transactionReceipt) {
    List<String> result = new ArrayList<>();
    List<HubConnectedEventResponse> hubConnectedEvents = WoM.getHubConnectedEvents(transactionReceipt);
    if (CollectionUtils.isNotEmpty(hubConnectedEvents)) {
      HubConnectedEventResponse hubConnectedEventResponse = hubConnectedEvents.get(0);
      result.add(hubConnectedEventResponse.hub);
    }
    List<HubDisconnectedEventResponse> hubDisconnectedEvents = WoM.getHubDisconnectedEvents(transactionReceipt);
    if (CollectionUtils.isNotEmpty(hubDisconnectedEvents)) {
      HubDisconnectedEventResponse hubDisconnectedEventResponse = hubDisconnectedEvents.get(0);
      result.add(hubDisconnectedEventResponse.hub);
    }
    return result;
  }

  private TransactionReceipt getPolygonTransactionReceipt(String transactionHash) {
    return getTransactionReceipt(transactionHash, polygonWeb3j);
  }

  private TransactionReceipt getTransactionReceipt(String transactionHash) {
    return getTransactionReceipt(transactionHash, web3j);
  }

  private TransactionReceipt getTransactionReceipt(String transactionHash, Web3j customWeb3j) {
    try {
      EthGetTransactionReceipt ethGetTransactionReceipt = customWeb3j.ethGetTransactionReceipt(transactionHash).send();
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

  private String getWomContractExceptionMessage(Throwable e) {
    if (e != null) {
      if (StringUtils.contains(e.getMessage(), "wom.")) {
        String message = getWomContractMessage(e.getMessage());
        if (StringUtils.isNotBlank(message)) {
          return message;
        }
      }
      if (e.getCause() != null) {
        return getWomContractExceptionMessage(e.getCause());
      }
    }
    return null;
  }

  private String getWomContractMessage(String message) {
    Matcher matcher = Pattern.compile("wom\\.[a-zA-Z0-9]+").matcher(message);
    if (matcher.find()) {
      return matcher.group();
    }
    return null;
  }

  private String getHubAddressIfConnected(String hubAddress) throws Exception {
    hubAddress = StringUtils.equalsIgnoreCase(hubAddress, EnsUtils.EMPTY_ADDRESS) ? null : hubAddress;
    return hubAddress != null && womContract.isHubConnected(hubAddress).send() ? hubAddress : null;
  }

  private void updateDeedStatusOnWom(long deedId, // NOSONAR
                                     short city,
                                     short cardType,
                                     short mintingPower,
                                     long maxUsers,
                                     String ownerAddress,
                                     String managerAddress,
                                     String hubAddress,
                                     short ownerMintingPercentage) throws WomException {
    try {
      TransactionReceipt transactionReceipt = womContractWithManager.updateDeed(BigInteger.valueOf(deedId),
                                                                                new io.meeds.deeds.contract.WoM.Deed(BigInteger.valueOf(city),
                                                                                                                     BigInteger.valueOf(cardType),
                                                                                                                     BigInteger.valueOf(mintingPower),
                                                                                                                     BigInteger.valueOf(maxUsers),
                                                                                                                     ownerAddress,
                                                                                                                     managerAddress,
                                                                                                                     hubAddress,
                                                                                                                     BigInteger.valueOf(ownerMintingPercentage),
                                                                                                                     BigInteger.valueOf(100l -
                                                                                                                         ownerMintingPercentage)))
                                                                    .send();
      if (transactionReceipt == null) {
        throw new WomException("wom.updateDeedTransactionFailedWithoutReceipt");
      } else if (!transactionReceipt.isStatusOK()) {
        String message = getWomContractMessage(transactionReceipt.getRevertReason());
        if (StringUtils.isNotBlank(message)) {
          throw new WomException(message);
        } else {
          message = getWomContractMessage(transactionReceipt.getStatus());
          if (StringUtils.isNotBlank(message)) {
            throw new WomException(message);
          } else {
            throw new WomException("wom.updateDeedTransactionFailed");
          }
        }
      }
    } catch (Exception e) {
      String message = getWomContractExceptionMessage(e);
      if (StringUtils.isNotBlank(message)) {
        throw new WomException(message);
      } else {
        throw new IllegalStateException("Error While processing Deed Update transaction", e);
      }
    }
  }

  private List<? extends BaseEventResponse> getUemLogs(TransactionReceipt transactionReceipt) {
    return Stream.of(UserEngagementMinting.getReportSentEvents(transactionReceipt),
                     UserEngagementMinting.getReportFraudEvents(transactionReceipt),
                     UserEngagementMinting.getClaimedEvents(transactionReceipt))
                 .flatMap(List::stream)
                 .filter(Objects::nonNull)
                 .toList();
  }

}
