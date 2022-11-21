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

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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

import io.meeds.deeds.constant.BlockchainLeaseStatus;
import io.meeds.deeds.constant.ObjectNotFoundException;
import io.meeds.deeds.constant.TransactionStatus;
import io.meeds.deeds.constant.UnauthorizedOperationException;
import io.meeds.deeds.elasticsearch.model.DeedTenant;
import io.meeds.deeds.model.DeedLeaseBlockchainState;
import io.meeds.deeds.model.DeedTenantLease;
import io.meeds.deeds.model.DeedTenantLeaseDTO;
import io.meeds.deeds.model.DeedTenantOfferDTO;
import io.meeds.deeds.model.LeaseFilter;
import io.meeds.deeds.storage.LeaseRepository;
import io.meeds.deeds.utils.DeedTenantLeaseMapper;
import io.meeds.deeds.utils.DeedTenantOfferMapper;

@Component
public class LeaseService {

  private static final Logger     LOG                                   = LoggerFactory.getLogger(LeaseService.class);

  public static final String      LEASE_ACQUIRED_EVENT                  = "deed.event.leaseAcquired";

  public static final String      LEASE_ACQUISITION_CONFIRMED_EVENT     = "deed.event.leaseAcquisionConfirmed";

  public static final String      LEASE_RENT_PAYED_EVENT                = "deed.event.leaseRentPayed";

  public static final String      LEASE_RENT_PAYMENT_CONFIRMED_EVENT    = "deed.event.leaseRentPaymentConfirmed";

  public static final String      LEASE_END_EVENT                       = "deed.event.leaseEndSent";

  public static final String      LEASE_ENDED_CONFIRMED_EVENT           = "deed.event.leaseEndedConfirmed";

  public static final String      LEASE_TENANT_EVICTED_CONFIRMED_EVENT  = "deed.event.leaseTenantEvictedConfirmed";

  private static final String     TRANSACTION_HASH_IS_MANDATORY_MESSAGE = "Transaction Hash is Mandatory";

  @Autowired
  private LeaseRepository         deedTenantLeaseRepository;

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
    walletAddress = StringUtils.lowerCase(walletAddress);
    DeedTenantLeaseDTO lease = getLease(leaseId, walletAddress);
    if (refreshFromBlockchain) {
      LOG.debug("Refreshing Changed lease with id {} on blockchain on request of user {}", leaseId, walletAddress);
      long lastBlockNumber = blockchainService.getLastBlock();
      DeedLeaseBlockchainState blockchainLease = blockchainService.getLeaseById(BigInteger.valueOf(leaseId), null);
      blockchainLease.setBlockNumber(BigInteger.valueOf(lastBlockNumber));
      updateLeaseStatusFromBlockchain(blockchainLease, null);
      return getLease(leaseId, walletAddress);
    } else {
      return lease;
    }
  }

  public DeedTenantLeaseDTO getLease(long leaseId, String walletAddress) throws ObjectNotFoundException,
                                                                         UnauthorizedOperationException {
    DeedTenantLease lease = deedTenantLeaseRepository.findById(leaseId).orElse(null);
    if (lease == null) {
      throw new ObjectNotFoundException();
    }
    walletAddress = StringUtils.lowerCase(walletAddress);
    if (StringUtils.equalsIgnoreCase(walletAddress, lease.getManager())
        || StringUtils.equalsIgnoreCase(walletAddress, lease.getOwner())
        || !CollectionUtils.containsAny(lease.getViewAddresses(),
                                        Arrays.asList(DeedTenantOfferMapper.EVERYONE,
                                                      StringUtils.lowerCase(walletAddress)))
        || tenantService.isDeedOwner(walletAddress, lease.getNftId())) {
      return buildLeaseDTO(lease);
    } else {
      throw new UnauthorizedOperationException();
    }
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
      throw new IllegalStateException("Hub Manager Address is mandatory");
    }
    if (StringUtils.isBlank(transactionHash)) {
      throw new IllegalStateException(TRANSACTION_HASH_IS_MANDATORY_MESSAGE);
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
    DeedTenantLease deedTenantLease = deedTenantLeaseRepository.findById(leaseId).orElse(null);
    if (deedTenantLease == null) {
      throw new ObjectNotFoundException("Lease with identifier " + leaseId + "doesn't exist found");
    }
    if (!StringUtils.equalsIgnoreCase(managerAddress, deedTenantLease.getManager())) {
      throw new UnauthorizedOperationException("Lease with id " + leaseId + " does't belong to " + managerAddress);
    }
    if (StringUtils.isBlank(transactionHash)) {
      throw new IllegalStateException(TRANSACTION_HASH_IS_MANDATORY_MESSAGE);
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

  public DeedTenantLeaseDTO endLease(String managerAddress,
                                     long leaseId,
                                     String transactionHash) throws ObjectNotFoundException, UnauthorizedOperationException {
    DeedTenantLease deedTenantLease = deedTenantLeaseRepository.findById(leaseId).orElse(null);

    if (deedTenantLease == null) {
      throw new ObjectNotFoundException("Provided lease with id " + leaseId + "doesn't exists");
    }
    if (!StringUtils.equalsIgnoreCase(managerAddress, deedTenantLease.getManager())) {
      throw new UnauthorizedOperationException("Lease with id " + leaseId + " does't belong to " + managerAddress);
    }
    if (StringUtils.isBlank(transactionHash)) {
      throw new IllegalStateException(TRANSACTION_HASH_IS_MANDATORY_MESSAGE);
    }
    addPendingTransactionHash(deedTenantLease, transactionHash);
    deedTenantLease.setTransactionStatus(TransactionStatus.IN_PROGRESS);
    deedTenantLease.setEndingLease(true);
    deedTenantLease = saveLease(deedTenantLease);

    listenerService.publishEvent(LEASE_END_EVENT, deedTenantLease);
    return buildLeaseDTO(deedTenantLease);
  }

  public void updateLeaseStatusFromBlockchain(DeedLeaseBlockchainState blockchainLease,
                                              BlockchainLeaseStatus status) throws Exception {
    long leaseId = blockchainLease.getId().longValue();
    DeedTenantLease deedTenantLease = deedTenantLeaseRepository.findById(leaseId).orElse(null);
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
                                              Map<BlockchainLeaseStatus, DeedLeaseBlockchainState> minedEvents) throws ObjectNotFoundException {
    DeedTenantLease lease = deedTenantLeaseRepository.findById(leaseId).orElse(null);
    if (lease == null) {
      throw new IllegalArgumentException("Wrong Lease id");
    }
    if (minedEvents.isEmpty()) {
      saveLeaseTransactionAsError(lease, transactionHash);
    } else {
      if (minedEvents.size() > 1) {
        LOG.warn("Mined events for a single transaction seems to hold more than one Offer event. This is not supported yet. Will use the first retrieved event");
      }
      Entry<BlockchainLeaseStatus, DeedLeaseBlockchainState> entry = minedEvents.entrySet().iterator().next();
      BlockchainLeaseStatus status = entry.getKey();
      DeedLeaseBlockchainState blockchainLease = entry.getValue();

      checkLeaseBlockchainStatus(lease, blockchainLease);
      updateLeaseStatusFromBlockchain(lease, blockchainLease, status, transactionHash);
    }
  }

  public List<DeedTenantLease> getPendingTransactions() {
    return deedTenantLeaseRepository.findByTransactionStatusInOrderByCreatedDateAsc(Arrays.asList(TransactionStatus.IN_PROGRESS))
                                    .stream()
                                    .filter(lease -> !CollectionUtils.isEmpty(lease.getPendingTransactions()))
                                    .collect(Collectors.toList());
  }

  public void transferDeedOwnership(String newOnwer, long nftId) throws UnauthorizedOperationException {
    if (!tenantService.isDeedOwner(newOnwer, nftId)) {
      throw new UnauthorizedOperationException("Address " + newOnwer + " isn't owner of the Deed anymore");
    }
    List<DeedTenantLease> leases = deedTenantLeaseRepository.findByEnabledTrueAndNftIdAndEndDateGreaterThan(nftId, Instant.now());
    if (!CollectionUtils.isEmpty(leases)) {
      leases.forEach(lease -> {
        lease.setOwner(newOnwer);
        saveLease(lease);
      });
    }
  }

  public void saveLeaseTransactionAsError(long leaseId, String transactionHash) {
    DeedTenantLease lease = deedTenantLeaseRepository.findById(leaseId).orElse(null);
    if (lease == null) {
      throw new IllegalArgumentException("Wrong Lease identifier");
    }
    saveLeaseTransactionAsError(lease, transactionHash);
  }

  private DeedTenantLease createLeaseFromOffer(DeedTenantOfferDTO deedTenantOffer,
                                               String ownerAddress,
                                               String managerAddress,
                                               String managerEmail,
                                               String transactionHash) throws ObjectNotFoundException {
    DeedTenant deedTenant = getDeedTenant(deedTenantOffer.getNftId(), managerAddress, ownerAddress);
    if (deedTenant == null) {
      throw new ObjectNotFoundException("Deed Tenant with id " + deedTenantOffer.getNftId() + " doesn't exists");
    }
    if (StringUtils.isBlank(deedTenant.getOwnerAddress())) {
      deedTenant.setOwnerAddress(ownerAddress);
      deedTenant = tenantService.saveDeedTenant(deedTenant);
    }
    DeedTenantLease deedTenantLease = deedTenantLeaseRepository.findById(deedTenantOffer.getOfferId())
                                                               .orElse(DeedTenantLeaseMapper.fromOffer(deedTenantOffer,
                                                                                                       deedTenant,
                                                                                                       managerAddress,
                                                                                                       managerEmail,
                                                                                                       StringUtils.lowerCase(transactionHash)));
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
    removePendingTransactionHash(lease, transactionHash);
    long leaseEndDateInSeconds = blockchainLease.getLeaseEndDate().longValue();
    Instant leaseEndDate = Instant.ofEpochSecond(leaseEndDateInSeconds);
    if (lease.isEndingLease()
        && (lease.getEndDate() != null && leaseEndDateInSeconds > 0 && leaseEndDate.isBefore(lease.getEndDate()))) {
      lease.setEndDate(leaseEndDate);
      lease.setEndingLease(false);
    }

    try {
      if (lease.getMonthPaymentInProgress() > 0) {
        int newlyPaidMonths = blockchainLease.getPaidMonths().intValue() - lease.getPaidMonths();
        int monthPaymentInProgress = lease.getMonthPaymentInProgress() - newlyPaidMonths;
        lease.setMonthPaymentInProgress(monthPaymentInProgress > 0 ? monthPaymentInProgress : 0);
      }
      lease.setConfirmed(true);
      lease.setId(leaseId);
      lease.setNftId(blockchainLease.getDeedId().longValue());
      lease.setManager(StringUtils.lowerCase(blockchainLease.getTenant()));
      long blockNumber = blockchainLease.getBlockNumber().longValue();
      if (lease.getId() > 0 && blockNumber > 0) {
        DeedTenantLease freshLeaseEntity = deedTenantLeaseRepository.findById(lease.getId()).orElse(null);
        if (freshLeaseEntity != null && freshLeaseEntity.getLastCheckedBlock() >= blockNumber) {
          LOG.debug("Lease Status is already updated in block {} (last checked {}) with status {}",
                    blockNumber,
                    freshLeaseEntity.getLastCheckedBlock(),
                    status);
          saveLease(lease);
          return;
        }
      }
      lease.setLastCheckedBlock(blockNumber);
      lease.setPaidMonths(blockchainLease.getPaidMonths().intValue());
      lease.setPaidRentsDate(Instant.ofEpochSecond(blockchainLease.getPaidRentsDate().longValue()));
      lease.setStartDate(Instant.ofEpochSecond(blockchainLease.getLeaseStartDate().longValue()));
      lease.setEndDate(leaseEndDate);
      lease.setNoticeDate(Instant.ofEpochSecond(blockchainLease.getNoticePeriodDate().longValue()));
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
      if (lease.getPaidMonths() <= 0 && !CollectionUtils.isEmpty(lease.getPendingTransactions())
          && lease.getPendingTransactions().contains(transactionHash.toLowerCase())) {
        lease.setEnabled(false);
      }
      lease.setEndingLease(false);
      lease.setMonthPaymentInProgress(0);
      removePendingTransactionHash(lease, transactionHash);
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
    return deedTenantLeaseRepository.save(deedTenantLease);
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
    pendingTransactions.add(transactionHash.toLowerCase());
    lease.setPendingTransactions(pendingTransactions);
  }

  private void removePendingTransactionHash(DeedTenantLease lease, String transactionHash) {
    if (StringUtils.isNotBlank(transactionHash) && !CollectionUtils.isEmpty(lease.getPendingTransactions())) {
      List<String> pendingTransactions = new ArrayList<>(lease.getPendingTransactions());
      pendingTransactions.remove(transactionHash.toLowerCase());
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
