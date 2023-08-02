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
package io.meeds.dapp.service;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.meeds.dapp.elasticsearch.model.DeedTenantLease;
import io.meeds.dapp.model.DeedTenantLeaseDTO;
import io.meeds.dapp.model.DeedTenantOfferDTO;
import io.meeds.dapp.model.LeaseFilter;
import io.meeds.dapp.storage.LeaseRepository;
import io.meeds.dapp.utils.DeedTenantLeaseMapper;
import io.meeds.dapp.utils.DeedTenantOfferMapper;
import io.meeds.deeds.constant.BlockchainLeaseStatus;
import io.meeds.deeds.constant.ObjectNotFoundException;
import io.meeds.deeds.constant.TransactionStatus;
import io.meeds.deeds.constant.UnauthorizedOperationException;
import io.meeds.deeds.elasticsearch.model.DeedTenant;
import io.meeds.deeds.model.DeedLeaseBlockchainState;
import io.meeds.deeds.service.BlockchainService;
import io.meeds.deeds.service.ListenerService;
import io.meeds.deeds.service.TenantService;

@Component
public class LeaseService {

  private static final Logger     LOG                                   = LoggerFactory.getLogger(LeaseService.class);

  public static final String      LEASE_ACQUIRED_EVENT                  = "deed.event.leaseAcquired";

  public static final String      LEASE_ACQUISITION_CONFIRMED_EVENT     = "deed.event.leaseAcquisionConfirmed";

  public static final String      LEASE_RENT_PAYED_EVENT                = "deed.event.leaseRentPayed";

  public static final String      LEASE_RENT_PAYMENT_CONFIRMED_EVENT    = "deed.event.leaseRentPaymentConfirmed";

  public static final String      LEASE_END_EVENT                       = "deed.event.leaseEndSent";

  public static final String      LEASE_ENDED_CONFIRMED_EVENT           = "deed.event.leaseEndedConfirmed";

  public static final String      LEASE_TENANT_EVICT_EVENT              = "deed.event.leaseTenantEvict";

  public static final String      LEASE_TENANT_EVICTED_CONFIRMED_EVENT  = "deed.event.leaseTenantEvictedConfirmed";

  private static final String     TRANSACTION_HASH_IS_MANDATORY_MESSAGE = "Transaction Hash is Mandatory";

  @Autowired
  private LeaseRepository         leaseRepository;

  @Autowired
  private ElasticsearchOperations elasticsearchOperations;

  @Autowired
  private OfferService            offerService;

  @Autowired
  private TenantService           tenantService;

  @Autowired
  private BlockchainService       blockchainService;

  @Autowired
  private ListenerService         listenerService;

  public Page<DeedTenantLeaseDTO> getLeases(LeaseFilter leaseFilter, Pageable pageable) {
    if (leaseFilter.getNetworkId() > 0 && !tenantService.isBlockchainNetworkValid(leaseFilter.getNetworkId())) {
      return Page.empty(pageable);
    }
    Criteria criteria = new Criteria("enabled").is(true);

    if (!leaseFilter.isIncludeOutdated()) {
      Criteria endDateCriteria = new Criteria("endDate").greaterThan(Instant.now());
      criteria.and(endDateCriteria);
    }

    if (leaseFilter.isExcludeNotConfirmed()) {
      Criteria confirmedCriteria = new Criteria("confirmed").is(true);
      criteria.and(confirmedCriteria);
    }
    if (leaseFilter.getNftId() >= 0) {
      Criteria nftIdCriteria = new Criteria("nftId").is(leaseFilter.getNftId());
      criteria.and(nftIdCriteria);
    }
    if (!CollectionUtils.isEmpty(leaseFilter.getCardTypes())) {
      Criteria cardTypeCriteria = new Criteria("cardType").in(leaseFilter.getCardTypes());
      criteria.and(cardTypeCriteria);
    }
    if (!CollectionUtils.isEmpty(leaseFilter.getTransactionStatus())) {
      Criteria transactionStatusCriteria = new Criteria("transactionStatus").in(leaseFilter.getTransactionStatus());
      criteria.and(transactionStatusCriteria);
    }

    if (StringUtils.isNotBlank(leaseFilter.getCurrentAddress())) {
      Criteria visibilityCriteria = new Criteria("viewAddresses").in(StringUtils.lowerCase(leaseFilter.getCurrentAddress()),
                                                                     DeedTenantOfferMapper.EVERYONE);
      criteria.and(visibilityCriteria);
    } else {
      Criteria visibilityCriteria = new Criteria("viewAddresses").in(DeedTenantOfferMapper.EVERYONE);
      criteria.and(visibilityCriteria);
    }

    if (leaseFilter.getOwner() != null) {
      if (leaseFilter.getOwner().booleanValue()) {
        Criteria ownerCriteria = new Criteria("owner").in(StringUtils.lowerCase(leaseFilter.getCurrentAddress()));
        criteria.and(ownerCriteria);
      } else {
        Criteria managerCriteria = new Criteria("manager").in(StringUtils.lowerCase(leaseFilter.getCurrentAddress()));
        criteria.and(managerCriteria);
      }
    }

    CriteriaQuery query = new CriteriaQuery(criteria, pageable);
    SearchHits<DeedTenantLease> result = elasticsearchOperations.search(query, DeedTenantLease.class);
    SearchPage<DeedTenantLease> searchPage = SearchHitSupport.searchPageFor(result, pageable);
    return searchPage.map(SearchHit::getContent)
                     .map(this::buildLeaseDTO);
  }

  public DeedTenantLeaseDTO getLease(long leaseId,
                                     String walletAddress,
                                     boolean refreshFromBlockchain) throws Exception {
    if (refreshFromBlockchain) {
      LOG.debug("Refreshing Changed lease with id {} on blockchain on request of user {}", leaseId, walletAddress);
      long lastBlockNumber = blockchainService.getLastBlock();
      DeedLeaseBlockchainState blockchainLease = blockchainService.getLeaseById(BigInteger.valueOf(leaseId),
                                                                                BigInteger.valueOf(lastBlockNumber),
                                                                                null);
      updateLeaseStatusFromBlockchain(blockchainLease, null);
    }
    DeedTenantLeaseDTO lease = getLease(leaseId);
    if (lease == null) {
      throw new ObjectNotFoundException();
    }
    return lease;
  }

  public DeedTenantLeaseDTO getLease(long leaseId) {
    DeedTenantLease lease = leaseRepository.findById(leaseId).orElse(null);
    return buildLeaseDTO(lease);
  }

  public DeedTenantLeaseDTO createLease(String managerAddress,
                                        String managerEmail,
                                        String offerId,
                                        String transactionHash) throws ObjectNotFoundException,
                                                                UnauthorizedOperationException {
    DeedTenantOfferDTO deedTenantOffer = offerService.getOffer(offerId);
    if (deedTenantOffer == null) {
      throw new ObjectNotFoundException("Offer with id " + offerId + "wasn't found");
    }
    if (StringUtils.isBlank(managerAddress)) {
      throw new IllegalArgumentException("Hub Manager Address is mandatory");
    }
    if (StringUtils.isBlank(transactionHash)) {
      throw new IllegalArgumentException(TRANSACTION_HASH_IS_MANDATORY_MESSAGE);
    }
    String ownerAddress = deedTenantOffer.getOwner();
    if (!tenantService.isDeedOwner(ownerAddress, deedTenantOffer.getNftId())) {
      throw new UnauthorizedOperationException("Address " + ownerAddress + " isn't owner of the Deed anymore");
    }
    DeedTenantLease deedTenantLease = createLeaseFromOffer(deedTenantOffer,
                                                           ownerAddress,
                                                           managerAddress,
                                                           managerEmail,
                                                           transactionHash);

    listenerService.publishEvent(LEASE_ACQUIRED_EVENT, deedTenantLease);
    return buildLeaseDTO(deedTenantLease);
  }

  public DeedTenantLeaseDTO payRents(String managerAddress,
                                     String ownerAddress,
                                     long leaseId,
                                     int paidMonths,
                                     String transactionHash) throws ObjectNotFoundException,
                                                             UnauthorizedOperationException {
    DeedTenantLease deedTenantLease = leaseRepository.findById(leaseId).orElse(null);
    if (deedTenantLease == null) {
      throw new ObjectNotFoundException("Lease with identifier " + leaseId + "doesn't exist found");
    }
    if (!StringUtils.equalsIgnoreCase(managerAddress, deedTenantLease.getManager())) {
      throw new UnauthorizedOperationException("Lease with id " + leaseId + " does't belong to " + managerAddress);
    }
    if (StringUtils.isBlank(transactionHash)) {
      throw new IllegalArgumentException(TRANSACTION_HASH_IS_MANDATORY_MESSAGE);
    }
    addPendingTransactionHash(deedTenantLease, transactionHash);
    deedTenantLease.setTransactionStatus(TransactionStatus.IN_PROGRESS);
    deedTenantLease.setMonthPaymentInProgress(paidMonths);
    if (!StringUtils.equalsIgnoreCase(deedTenantLease.getOwner(), ownerAddress)
        && tenantService.isDeedOwner(ownerAddress, deedTenantLease.getNftId())) {
      deedTenantLease.setOwner(ownerAddress);
    }
    deedTenantLease = saveLease(deedTenantLease);

    listenerService.publishEvent(LEASE_RENT_PAYED_EVENT, deedTenantLease);
    return buildLeaseDTO(deedTenantLease);
  }

  public DeedTenantLeaseDTO endLease(String managerOrOwnerAddress,
                                     long leaseId,
                                     String transactionHash) throws ObjectNotFoundException, UnauthorizedOperationException {
    DeedTenantLease deedTenantLease = leaseRepository.findById(leaseId).orElse(null);

    if (deedTenantLease == null) {
      throw new ObjectNotFoundException("Provided lease with id " + leaseId + "doesn't exists");
    }
    boolean isManager = tenantService.isDeedManager(managerOrOwnerAddress, deedTenantLease.getNftId());
    boolean isOwner = tenantService.isDeedOwner(managerOrOwnerAddress, deedTenantLease.getNftId());
    if (!isManager && !isOwner) {
      throw new UnauthorizedOperationException("Lease with id " + leaseId + " does't belong to " + managerOrOwnerAddress);
    }
    if (StringUtils.isBlank(transactionHash)) {
      throw new IllegalArgumentException(TRANSACTION_HASH_IS_MANDATORY_MESSAGE);
    }
    addPendingTransactionHash(deedTenantLease, transactionHash);
    deedTenantLease.setTransactionStatus(TransactionStatus.IN_PROGRESS);
    deedTenantLease.setEndingLeaseAddress(StringUtils.lowerCase(managerOrOwnerAddress));
    deedTenantLease.setEndingLease(true);
    if (isOwner) {
      deedTenantLease.setOwner(StringUtils.lowerCase(managerOrOwnerAddress));
      deedTenantLease = saveLease(deedTenantLease);
      listenerService.publishEvent(LEASE_TENANT_EVICT_EVENT, deedTenantLease);
      return buildLeaseDTO(deedTenantLease);
    } else {
      deedTenantLease = saveLease(deedTenantLease);
      listenerService.publishEvent(LEASE_END_EVENT, deedTenantLease);
      return buildLeaseDTO(deedTenantLease);
    }
  }

  public void updateLeaseStatusFromBlockchain(DeedLeaseBlockchainState blockchainLease,
                                              BlockchainLeaseStatus status) throws Exception {
    long leaseId = blockchainLease.getId().longValue();
    DeedTenantLease deedTenantLease = leaseRepository.findById(leaseId).orElse(null);
    if (deedTenantLease == null) {
      DeedTenantOfferDTO offer = offerService.getOfferByBlockchainId(leaseId);
      deedTenantLease = createLeaseFromOffer(offer,
                                             offer.getOwner(),
                                             blockchainLease.getTenant(),
                                             null,
                                             null);
    }
    updateLeaseStatusFromBlockchain(deedTenantLease, blockchainLease, status, null);
  }

  public void updateLeaseStatusFromBlockchain(long leaseId,
                                              String transactionHash,
                                              Map<BlockchainLeaseStatus, DeedLeaseBlockchainState> minedEvents) throws Exception {
    DeedTenantLease lease = leaseRepository.findById(leaseId).orElse(null);
    if (minedEvents.isEmpty()) {
      if (lease == null) {
        throw new ObjectNotFoundException("Wrong Lease id: " + leaseId);
      }
      saveLeaseTransactionAsError(lease, transactionHash);
    } else {
      if (minedEvents.size() > 1) {
        LOG.warn("Mined events for a single transaction seems to hold more than one Offer event. This is not supported yet. Will use the first retrieved event");
      }
      Entry<BlockchainLeaseStatus, DeedLeaseBlockchainState> entry = minedEvents.entrySet().iterator().next();
      BlockchainLeaseStatus status = entry.getKey();
      DeedLeaseBlockchainState blockchainLease = entry.getValue();
      if (lease == null) {
        updateLeaseStatusFromBlockchain(blockchainLease, status);
      } else {
        checkLeaseBlockchainStatus(lease, blockchainLease);
        updateLeaseStatusFromBlockchain(lease, blockchainLease, status, transactionHash);
      }
    }
  }

  public List<DeedTenantLease> getPendingTransactions() {
    return leaseRepository.findByTransactionStatusInOrderByCreatedDateAsc(Arrays.asList(TransactionStatus.IN_PROGRESS))
                          .stream()
                          .filter(lease -> !CollectionUtils.isEmpty(lease.getPendingTransactions()))
                          .toList();
  }

  public void transferDeedOwnership(String newOnwer, long nftId) throws UnauthorizedOperationException {
    if (!tenantService.isDeedOwner(newOnwer, nftId)) {
      throw new UnauthorizedOperationException("Address " + newOnwer + " isn't owner of the Deed anymore");
    }
    List<DeedTenantLease> leases = leaseRepository.findByEnabledTrueAndNftIdAndEndDateGreaterThan(nftId, Instant.now());
    if (!CollectionUtils.isEmpty(leases)) {
      leases.forEach(lease -> {
        lease.setOwner(newOnwer);
        saveLease(lease);
      });
    }
  }

  public void saveLeaseTransactionAsError(long leaseId, String transactionHash) {
    DeedTenantLease lease = leaseRepository.findById(leaseId).orElse(null);
    if (lease == null) {
      throw new IllegalArgumentException("Wrong Lease identifier");
    }
    saveLeaseTransactionAsError(lease, transactionHash);
  }

  private DeedTenantLease createLeaseFromOffer(DeedTenantOfferDTO offer,
                                               String ownerAddress,
                                               String managerAddress,
                                               String managerEmail,
                                               String transactionHash) throws ObjectNotFoundException {
    if (StringUtils.isBlank(ownerAddress)) {
      ownerAddress = offer.getOwner();
    }
    DeedTenant deedTenant = getDeedTenant(offer.getNftId(), managerAddress, ownerAddress);
    if (deedTenant == null) {
      throw new ObjectNotFoundException("Deed Tenant with id " + offer.getNftId() + " doesn't exists");
    }
    if (StringUtils.isBlank(deedTenant.getOwnerAddress())) {
      deedTenant.setOwnerAddress(ownerAddress);
      deedTenant = tenantService.saveDeedTenant(deedTenant);
    }
    DeedTenantLease deedTenantLease = leaseRepository.findById(offer.getOfferId())
                                                     .orElse(DeedTenantLeaseMapper.fromOffer(offer,
                                                                                             deedTenant,
                                                                                             managerAddress,
                                                                                             managerEmail,
                                                                                             StringUtils.lowerCase(transactionHash)));
    if (deedTenantLease == null) {
      throw new ObjectNotFoundException();
    }
    if (StringUtils.isBlank(deedTenantLease.getManager()) && StringUtils.isNotBlank(managerAddress)) {
      deedTenantLease.setManager(managerAddress);
    }
    if (StringUtils.isBlank(deedTenantLease.getManagerEmail()) && StringUtils.isNotBlank(managerEmail)) {
      deedTenantLease.setManagerEmail(managerEmail);
    }
    if (StringUtils.isBlank(deedTenantLease.getOwner()) && StringUtils.isNotBlank(ownerAddress)) {
      deedTenantLease.setOwner(ownerAddress);
    }
    return saveLease(deedTenantLease);
  }

  private void updateLeaseStatusFromBlockchain(DeedTenantLease lease,
                                               DeedLeaseBlockchainState blockchainLease,
                                               BlockchainLeaseStatus status,
                                               String transactionHash) throws ObjectNotFoundException {
    long leaseId = blockchainLease.getId().longValue();
    if (leaseId == 0) {
      throw new ObjectNotFoundException("Retrieved Lease from blockchain has a 0 identifier");
    }
    if (StringUtils.isEmpty(transactionHash)) {
      transactionHash = StringUtils.lowerCase(blockchainLease.getTransactionHash());
    } else {
      transactionHash = StringUtils.lowerCase(transactionHash);
    }
    removePendingTransactionHash(lease, transactionHash);
    long leaseEndDateInSeconds = blockchainLease.getLeaseEndDate().longValue();
    Instant leaseEndDate = Instant.ofEpochSecond(leaseEndDateInSeconds);
    if (lease.isEndingLease()
        && (lease.getEndDate() != null && leaseEndDateInSeconds > 0 && leaseEndDate.isBefore(lease.getEndDate()))) {
      lease.setEndingLease(false);
    }
    if (lease.getMonthPaymentInProgress() > 0) {
      int newlyPaidMonths = blockchainLease.getPaidMonths().intValue() - lease.getPaidMonths();
      int monthPaymentInProgress = lease.getMonthPaymentInProgress() - newlyPaidMonths;
      lease.setMonthPaymentInProgress(monthPaymentInProgress > 0 ? monthPaymentInProgress : 0);
    }
    if (CollectionUtils.isEmpty(lease.getPendingTransactions())) {
      lease.setEndingLease(false);
      lease.setMonthPaymentInProgress(0);
    }
    lease.setConfirmed(true);
    lease.setId(leaseId);
    lease.setNftId(blockchainLease.getDeedId().longValue());
    lease.setManager(StringUtils.lowerCase(blockchainLease.getTenant()));
    lease.setPaidMonths(blockchainLease.getPaidMonths().intValue());
    lease.setPaidRentsDate(Instant.ofEpochSecond(blockchainLease.getPaidRentsDate().longValue()));
    lease.setStartDate(Instant.ofEpochSecond(blockchainLease.getLeaseStartDate().longValue()));
    lease.setEndDate(leaseEndDate);
    lease.setNoticeDate(Instant.ofEpochSecond(blockchainLease.getNoticePeriodDate().longValue()));

    long blockNumber = blockchainLease.getBlockNumber().longValue();
    if (lease.getId() > 0 && blockNumber > 0) {
      DeedTenantLease freshLeaseEntity = leaseRepository.findById(lease.getId()).orElse(null);
      if (freshLeaseEntity != null && freshLeaseEntity.getLastCheckedBlock() >= blockNumber) {
        LOG.debug("Lease Status is already updated in block {} (last checked {}) with status {}. Avoid publish an Event about change made.",
                  blockNumber,
                  freshLeaseEntity.getLastCheckedBlock(),
                  status);
        saveLease(lease);
        return;
      }
    }

    try {
      lease.setLastCheckedBlock(blockNumber);
      lease = saveLease(lease);
    } finally {
      broadcastLeaseEvent(lease, status);
    }
  }

  private void broadcastLeaseEvent(DeedTenantLease lease, BlockchainLeaseStatus status) {
    if (status == BlockchainLeaseStatus.LEASE_ACQUIRED) {
      LOG.debug("Lease {} Acquisition confirmed on blockchain for manager {} and owner {}",
                lease.getId(),
                lease.getManager(),
                lease.getOwner());
      listenerService.publishEvent(LEASE_ACQUISITION_CONFIRMED_EVENT,
                                   buildLeaseDTO(lease));
    } else if (status == BlockchainLeaseStatus.LEASE_PAYED) {
      LOG.debug("Lease {} payment confirmed on blockchain for manager {} for owner {}",
                lease.getId(),
                lease.getManager(),
                lease.getOwner());
      listenerService.publishEvent(LEASE_RENT_PAYMENT_CONFIRMED_EVENT,
                                   buildLeaseDTO(lease));
    } else if (status == BlockchainLeaseStatus.LEASE_ENDED) {
      LOG.debug("Lease {} end confirmed on blockchain for manager {}",
                lease.getId(),
                lease.getManager());
      listenerService.publishEvent(LEASE_ENDED_CONFIRMED_EVENT,
                                   buildLeaseDTO(lease));
    } else if (status == BlockchainLeaseStatus.LEASE_MANAGER_EVICTED) {
      LOG.debug("Lease {} evit tenant {} confirmed on blockchain with owner {}",
                lease.getId(),
                lease.getManager(),
                lease.getOwner());
      listenerService.publishEvent(LEASE_TENANT_EVICTED_CONFIRMED_EVENT,
                                   buildLeaseDTO(lease));
    }
  }

  private void saveLeaseTransactionAsError(DeedTenantLease lease, String transactionHash) {
    if (StringUtils.isNotBlank(transactionHash)) {
      if (!lease.isConfirmed()
          && !CollectionUtils.isEmpty(lease.getPendingTransactions())
          && lease.getPendingTransactions().contains(transactionHash.toLowerCase())) {
        lease.setEnabled(false);
      }
      removePendingTransactionHash(lease, transactionHash);
      if (lease.getPendingTransactions().isEmpty()) {
        lease.setEndingLease(false);
        lease.setMonthPaymentInProgress(0);
      }
      saveLease(lease);
    }
  }

  private DeedTenantLease saveLease(DeedTenantLease deedTenantLease) {
    if (!deedTenantLease.isEnabled()) {
      deedTenantLease.setViewAddresses(Collections.emptyList());
    } else if (!deedTenantLease.isConfirmed()) {
      List<String> viewAddresses = Arrays.asList(StringUtils.lowerCase(deedTenantLease.getOwner()),
                                                 StringUtils.lowerCase(deedTenantLease.getManager()));
      deedTenantLease.setViewAddresses(viewAddresses);
    } else {
      deedTenantLease.setViewAddresses(Collections.singletonList(DeedTenantOfferMapper.EVERYONE));
    }

    if (CollectionUtils.isEmpty(deedTenantLease.getPendingTransactions())) {
      if (deedTenantLease.isConfirmed()) {
        deedTenantLease.setTransactionStatus(TransactionStatus.VALIDATED);
      } else {
        // No pending transaction, and not a confirmed Lease => Lease doesn't
        // really exists
        deedTenantLease.setTransactionStatus(TransactionStatus.ERROR);
      }
    } else {
      deedTenantLease.setTransactionStatus(TransactionStatus.IN_PROGRESS);
    }
    return leaseRepository.save(deedTenantLease);
  }

  private void checkLeaseBlockchainStatus(DeedTenantLease lease, DeedLeaseBlockchainState blockchainLease) {
    if (lease.getId() != blockchainLease.getId().longValue()) {
      LOG.warn("HACK Tentative or Bug? The transaction hash has been replaced by a transaction of another LEASE Identifier: {} VS {}. The information will be retrieved from Blockchain anyway to replace it.",
               lease.getId(),
               blockchainLease.getId().longValue());
    }
    if (lease.getNftId() != blockchainLease.getDeedId().longValue()) {
      LOG.warn("HACK Tentative or Bug? The transaction hash has been replaced by a transaction of another DEED: {} VS {}. The information will be retrieved from Blockchain anyway to replace it.",
               lease.getNftId(),
               blockchainLease.getDeedId().longValue());
    }
    if (!StringUtils.equalsIgnoreCase(lease.getManager(), blockchainLease.getTenant())) {
      LOG.warn("HACK Tentative or Bug? The transaction owner isn't the same as Lease owner: {} VS {}. The information will be retrieved from Blockchain anyway to replace it.",
               blockchainLease.getTenant(),
               lease.getManager());
    }
  }

  private DeedTenantLeaseDTO buildLeaseDTO(DeedTenantLease lease) {
    if (lease == null) {
      return null;
    }
    DeedTenant deedTenant = getDeedTenant(lease.getNftId(), lease.getManager(), lease.getOwner());
    if (deedTenant == null) {
      return DeedTenantLeaseMapper.toDTO(lease, null, null);
    } else {
      return DeedTenantLeaseMapper.toDTO(lease,
                                         deedTenant.getTenantStatus(),
                                         deedTenant.getTenantProvisioningStatus());
    }
  }

  private void addPendingTransactionHash(DeedTenantLease lease, String transactionHash) {
    List<String> pendingTransactions;
    if (!CollectionUtils.isEmpty(lease.getPendingTransactions())) {
      pendingTransactions = new ArrayList<>(lease.getPendingTransactions());
    } else {
      pendingTransactions = new ArrayList<>();
    }
    pendingTransactions.add(StringUtils.lowerCase(transactionHash));
    lease.setPendingTransactions(pendingTransactions);
  }

  private void removePendingTransactionHash(DeedTenantLease lease, String transactionHash) {
    if (StringUtils.isNotBlank(transactionHash) && !CollectionUtils.isEmpty(lease.getPendingTransactions())) {
      List<String> pendingTransactions = new ArrayList<>(lease.getPendingTransactions());
      pendingTransactions.remove(StringUtils.lowerCase(transactionHash));
      lease.setPendingTransactions(pendingTransactions);
    }
  }

  private DeedTenant getDeedTenant(long nftId, String managerAddress, String ownerAddress) {
    DeedTenant deedTenant;
    try {
      try { // NOSONAR
        deedTenant = tenantService.getDeedTenantOrImport(ownerAddress, nftId);
      } catch (UnauthorizedOperationException e) {
        deedTenant = tenantService.getDeedTenantOrImport(managerAddress, nftId);
      }
    } catch (UnauthorizedOperationException e) {
      deedTenant = tenantService.getDeedTenant(nftId);
    } catch (ObjectNotFoundException e) {
      throw new IllegalStateException("Unable to locate nft with id " + nftId, e);
    }
    return deedTenant;
  }

}
