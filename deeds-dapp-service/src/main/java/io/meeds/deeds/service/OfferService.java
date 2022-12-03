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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;
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
import org.web3j.abi.datatypes.Address;

import io.meeds.deeds.constant.BlockchainOfferStatus;
import io.meeds.deeds.constant.DeedCard;
import io.meeds.deeds.constant.DeedCity;
import io.meeds.deeds.constant.ExpirationDuration;
import io.meeds.deeds.constant.NoticePeriod;
import io.meeds.deeds.constant.ObjectNotFoundException;
import io.meeds.deeds.constant.OfferType;
import io.meeds.deeds.constant.RentalDuration;
import io.meeds.deeds.constant.RentalPaymentPeriodicity;
import io.meeds.deeds.constant.TransactionStatus;
import io.meeds.deeds.constant.UnauthorizedOperationException;
import io.meeds.deeds.elasticsearch.model.DeedTenant;
import io.meeds.deeds.model.DeedOfferBlockchainState;
import io.meeds.deeds.model.DeedTenantOffer;
import io.meeds.deeds.model.DeedTenantOfferDTO;
import io.meeds.deeds.model.OfferFilter;
import io.meeds.deeds.storage.OfferRepository;
import io.meeds.deeds.utils.DeedTenantOfferMapper;

@Component
public class OfferService {

  private static final Logger      LOG                                   = LoggerFactory.getLogger(OfferService.class);

  public static final String       OFFER_CREATED_EVENT                   = "deed.event.offerCreated";

  public static final String       OFFER_CREATED_CONFIRMED_EVENT         = "deed.event.offerCreatedConfirmed";

  public static final String       OFFER_UPDATED_EVENT                   = "deed.event.offerUpdated";

  public static final String       OFFER_UPDATED_CONFIRMED_EVENT         = "deed.event.offerUpdatedConfirmed";

  public static final String       OFFER_DELETED_EVENT                   = "deed.event.offerDeleted";

  public static final String       OFFER_DELETED_CONFIRMED_EVENT         = "deed.event.offerDeletedConfirmed";

  public static final String       OFFER_CANCELED_EVENT                  = "deed.event.offerCanceled";

  public static final String       OFFER_ACQUISITION_PROGRESS_EVENT      = "deed.event.offerAcquisitionInProgress";

  public static final String       OFFER_ACQUISITION_CONFIRMED_EVENT     = "deed.event.offerAcquisitionConfirmed";

  private static final String      TRANSACTION_HASH_IS_MANDATORY_MESSAGE = "Transaction Hash is Mandatory";

  @Autowired
  private OfferRepository          deedTenantOfferRepository;

  @Autowired
  private ElasticsearchOperations  elasticsearchOperations;

  @Autowired
  private TenantService            tenantService;

  @Autowired
  private BlockchainService        blockchainService;

  @Autowired
  private ListenerService          listenerService;

  private Map<String, StampedLock> blockchainRefreshLocks                = new ConcurrentHashMap<>();

  private Map<String, Long>        blockchainRefreshStamp                = new ConcurrentHashMap<>();

  public Page<DeedTenantOfferDTO> getOffers(OfferFilter offerFilter, Pageable pageable) {
    if (offerFilter.getNetworkId() > 0 && !tenantService.isBlockchainNetworkValid(offerFilter.getNetworkId())) {
      return Page.empty(pageable);
    }
    Criteria criteria = new Criteria("parentId").not().exists();
    criteria.and(criteria, new Criteria("acquired").is(false));

    if (StringUtils.isNotBlank(offerFilter.getOwnerAddress())) {
      criteria.and(criteria, new Criteria("owner").is(StringUtils.lowerCase(offerFilter.getOwnerAddress())));
    }
    if (offerFilter.isExcludeDisabled()) {
      Criteria enabledCriteria = new Criteria("enabled").is(true);
      criteria.and(criteria, enabledCriteria);
    }
    if (offerFilter.isExcludeNotStarted()) {
      Criteria startDateCriteria = new Criteria("startDate").lessThan(Instant.now());
      criteria.and(criteria, startDateCriteria);
    }
    if (offerFilter.getNftId() >= 0) {
      Criteria nftIdCriteria = new Criteria("nftId").is(offerFilter.getNftId());
      criteria.and(nftIdCriteria);
    }
    if (!CollectionUtils.isEmpty(offerFilter.getCardTypes())) {
      Criteria cardTypeCriteria = new Criteria("cardType").in(offerFilter.getCardTypes());
      criteria.and(criteria, cardTypeCriteria);
    }
    if (!CollectionUtils.isEmpty(offerFilter.getOfferTypes())) {
      Criteria offerCriteria = new Criteria("offerType").in(offerFilter.getOfferTypes());
      criteria.and(criteria, offerCriteria);
    }
    if (offerFilter.isExcludeExpired()) {
      Criteria expirationDateCriteria = new Criteria("expirationDate").greaterThan(Instant.now());
      criteria.and(criteria, expirationDateCriteria);
    }
    if (!CollectionUtils.isEmpty(offerFilter.getTransactionStatus())) {
      Criteria transactionStatusCriteria = new Criteria("offerTransactionStatus").in(offerFilter.getTransactionStatus());
      criteria.and(criteria, transactionStatusCriteria);
    }
    if (StringUtils.isNotBlank(offerFilter.getCurrentAddress())) {
      Criteria visibilityCriteria = new Criteria("viewAddresses").in(StringUtils.lowerCase(offerFilter.getCurrentAddress()),
                                                                     DeedTenantOfferMapper.EVERYONE);
      criteria.and(criteria, visibilityCriteria);
    } else {
      Criteria visibilityCriteria = new Criteria("viewAddresses").in(DeedTenantOfferMapper.EVERYONE);
      criteria.and(criteria, visibilityCriteria);
    }
    CriteriaQuery query = new CriteriaQuery(criteria, pageable);
    SearchHits<DeedTenantOffer> result = elasticsearchOperations.search(query, DeedTenantOffer.class);
    SearchPage<DeedTenantOffer> searchPage = SearchHitSupport.searchPageFor(result, pageable);
    return searchPage.map(SearchHit::getContent)
                     .map(DeedTenantOfferMapper::toDTO);
  }

  public DeedTenantOfferDTO getOffer(String id, String walletAddress, boolean refreshFromBlockchain) throws Exception {
    if (refreshFromBlockchain) {
      DeedTenantOffer offer = deedTenantOfferRepository.findById(id).orElse(null);
      id = refreshOfferConcurrently(offer, walletAddress);
    }
    DeedTenantOfferDTO offer = getOffer(id);
    if (offer == null) {
      throw new ObjectNotFoundException();
    }
    if (!offer.isEnabled()) {
      throw new UnauthorizedOperationException();
    }
    return offer;
  }

  public DeedTenantOfferDTO getOffer(String id) {
    DeedTenantOffer offer = deedTenantOfferRepository.findById(id).orElse(null);
    return DeedTenantOfferMapper.toDTO(offer);
  }

  public DeedTenantOfferDTO getOfferByBlockchainId(long blockchainOfferId) throws Exception {
    DeedTenantOffer offer = getOfferByBlockchainOfferId(blockchainOfferId, true);
    return DeedTenantOfferMapper.toDTO(offer);
  }

  public DeedTenantOfferDTO createRentingOffer(String ownerAddress,
                                               String ownerEmail,
                                               DeedTenantOfferDTO deedTenantOfferDTO) throws ObjectNotFoundException,
                                                                                      UnauthorizedOperationException {
    long nftId = deedTenantOfferDTO.getNftId();
    if (!tenantService.isDeedOwner(ownerAddress, nftId)) {
      throw new UnauthorizedOperationException(getNotOwnerMessage(ownerAddress, nftId));
    }
    String transactionHash = StringUtils.lowerCase(deedTenantOfferDTO.getOfferTransactionHash());
    if (StringUtils.isBlank(transactionHash)) {
      throw new IllegalStateException(TRANSACTION_HASH_IS_MANDATORY_MESSAGE);
    }
    DeedTenant deedTenant = getDeedTenant(nftId, ownerAddress);
    if (deedTenant == null) {
      throw new ObjectNotFoundException(getNftNotExistsMessage(nftId));
    }
    checkTransactionHashIsUnknown(transactionHash);
    DeedTenantOffer deedTenantOffer = DeedTenantOfferMapper.fromDTO(deedTenantOfferDTO);
    try {
      // When multiple offers will be supported, then make the id
      // auto incremented
      setOfferNftInformation(deedTenantOffer, deedTenant);
      deedTenantOffer.setOwner(StringUtils.lowerCase(ownerAddress));
      deedTenantOffer.setOwnerEmail(ownerEmail);
      deedTenantOffer = saveOffer(deedTenantOffer);
      return DeedTenantOfferMapper.toDTO(deedTenantOffer);
    } finally {
      listenerService.publishEvent(OFFER_CREATED_EVENT, deedTenantOffer);
    }
  }

  public DeedTenantOfferDTO updateRentingOffer(String walletAddress,
                                               DeedTenantOfferDTO deedTenantOfferDTO) throws ObjectNotFoundException,
                                                                                      UnauthorizedOperationException {
    if (!tenantService.isDeedOwner(walletAddress, deedTenantOfferDTO.getNftId())) {
      throw new UnauthorizedOperationException(getNotOwnerMessage(walletAddress, deedTenantOfferDTO.getNftId()));
    }
    if (!deedTenantOfferDTO.isEnabled()) {
      throw new UnauthorizedOperationException("Offer has already been canceled");
    }
    DeedTenantOffer existingOffer = deedTenantOfferRepository.findById(deedTenantOfferDTO.getId()).orElse(null);
    if (existingOffer == null) {
      throw new ObjectNotFoundException("Offer with id  " + deedTenantOfferDTO.getId() + " doesn't exist");
    }
    // Update local offer variables not saved on Blockchain
    existingOffer.setDescription(deedTenantOfferDTO.getDescription());
    existingOffer.setPaymentPeriodicity(deedTenantOfferDTO.getPaymentPeriodicity());

    String transactionHash = deedTenantOfferDTO.getOfferTransactionHash();
    if (StringUtils.isNotBlank(transactionHash)) {
      checkTransactionHashIsUnknown(transactionHash);
      // There is some updates made on Blockchain offer, add a changelog
      // and wait for a validation to get those changes applied on
      // principal displayed offers to end users
      DeedTenantOffer deedTenantOfferUpdate = DeedTenantOfferMapper.toOfferUpdateChangeLog(deedTenantOfferDTO,
                                                                                           existingOffer);
      deedTenantOfferUpdate = saveOffer(deedTenantOfferUpdate);
      existingOffer.setUpdateId(deedTenantOfferUpdate.getId());
    }
    existingOffer = saveOffer(existingOffer);

    listenerService.publishEvent(OFFER_UPDATED_EVENT, existingOffer);
    return DeedTenantOfferMapper.toDTO(existingOffer);
  }

  public void markOfferAcquisitionInProgress(long nftId, String transactionHash, Instant validStartDate) {
    LOG.debug("Mark Offers of nft {} as acquisition in progress", nftId);

    if (StringUtils.isNotBlank(transactionHash)) {
      checkTransactionHashIsUnknown(transactionHash);
    }
    List<DeedTenantOffer> offers = deedTenantOfferRepository.findByNftId(nftId);
    if (!CollectionUtils.isEmpty(offers)) {
      offers.forEach(parentOffer -> {
        if (isOfferOngoing(parentOffer, null, validStartDate)) {
          createOfferAcquisitionChangeLog(parentOffer, transactionHash);
        }
      });
    }
  }

  public void deleteRentingOffer(String walletAddress, String offerId, String transactionHash) throws ObjectNotFoundException,
                                                                                               UnauthorizedOperationException {
    DeedTenantOffer existingOffer = deedTenantOfferRepository.findById(offerId).orElse(null);
    if (existingOffer == null) {
      throw new ObjectNotFoundException("Offer with id  " + offerId + " doesn't exist");
    }
    if (!tenantService.isDeedOwner(walletAddress, existingOffer.getNftId())) {
      throw new UnauthorizedOperationException(getNotOwnerMessage(walletAddress, existingOffer.getNftId()));
    }
    if (!existingOffer.isEnabled()) {
      throw new UnauthorizedOperationException("Offer has already been canceled");
    }
    if (StringUtils.isBlank(transactionHash)) {
      throw new IllegalStateException(TRANSACTION_HASH_IS_MANDATORY_MESSAGE);
    }
    checkTransactionHashIsUnknown(transactionHash);

    DeedTenantOffer offerDelete = DeedTenantOfferMapper.toOfferChangeLog(existingOffer, transactionHash);
    offerDelete = saveOffer(offerDelete);

    existingOffer.setDeleteId(offerDelete.getId());
    existingOffer = saveOffer(existingOffer);

    listenerService.publishEvent(OFFER_DELETED_EVENT, existingOffer);
  }

  public void cancelOffers(String newOwner, long nftId) {
    LOG.debug("Cancel offers of nftId {} due to a changed nft owner {}",
              nftId,
              newOwner);
    List<DeedTenantOffer> offers = deedTenantOfferRepository.findByOwnerNotAndNftIdAndEnabledTrue(newOwner, nftId);
    if (!CollectionUtils.isEmpty(offers)) {
      offers.forEach(offer -> {
        LOG.debug("Cancel offer {} due to a changed nft owner {}",
                  offer.getOfferId(),
                  newOwner);
        cancelOffer(offer);
      });
    }
  }

  public List<DeedTenantOfferDTO> getPendingTransactions() {
    return deedTenantOfferRepository.findByOfferTransactionStatusInOrderByCreatedDateAsc(Arrays.asList(TransactionStatus.IN_PROGRESS))
                                    .stream()
                                    .map(DeedTenantOfferMapper::toDTO)
                                    .collect(Collectors.toList());
  }

  public void updateRentingOfferStatusFromBlockchain(String offerId, // NOSONAR
                                                     Map<BlockchainOfferStatus, DeedOfferBlockchainState> minedEvents) throws Exception {
    DeedTenantOffer offer = deedTenantOfferRepository.findById(offerId).orElse(null);
    if (offer == null) {
      throw new IllegalArgumentException("Wrong Offer technical internal identifier " + offerId);
    }
    if (minedEvents.isEmpty()) {
      saveOfferTransactionAsError(offer.getId());
    } else {
      if (minedEvents.size() > 1) {
        LOG.warn("Mined events for a single transaction seems to hold more than one Offer event. This is not supported yet. Will use the first retrieved event");
      }
      Entry<BlockchainOfferStatus, DeedOfferBlockchainState> entry = minedEvents.entrySet().iterator().next();
      updateRentingOfferStatusFromBlockchain(offerId, entry.getValue(), entry.getKey());
    }
  }

  public DeedTenantOffer updateOfferFromBlockchain(DeedOfferBlockchainState blockchainOffer,
                                                   boolean blockchainScan) throws Exception {
    DeedTenantOffer offer = getParentOfferByBlockchainId(blockchainOffer.getId().longValue());
    try {
      return updateOfferFromBlockchain(offer, blockchainOffer);
    } finally {
      if (blockchainScan && offer != null) {
        LOG.debug("Delete acquired UI Refresh Lock on Offer after blockchain scan finished {}",
                  offer.getParentId());
        // Remove lock of offer refreshing by UI
        String parentId = offer.getId();
        releaseExplicitOfferRefreshLock(parentId);
      }
    }
  }

  public void markOfferAsAcquired(long offerId, Instant leaseEndDate) throws Exception {
    LOG.debug("Mark offer {} as acquired by a Tenant and cancel all offer before the new Lease end date", offerId);
    DeedTenantOffer offer = getOfferByBlockchainOfferId(offerId, true);
    if (offer != null) {
      updateRentingOfferStatusFromBlockchain(offer.getId(), null, BlockchainOfferStatus.OFFER_ACQUIRED);

      List<DeedTenantOffer> offers = deedTenantOfferRepository.findByNftId(offer.getNftId());
      if (!CollectionUtils.isEmpty(offers)) {
        offers.forEach(parentOffer -> {
          if (isOfferOngoing(parentOffer, offer, leaseEndDate)) {
            cancelOffer(parentOffer);
          }
        });
      }
    }
  }

  public void saveOfferTransactionAsError(String offerId) throws Exception {
    DeedTenantOffer offer = deedTenantOfferRepository.findById(offerId).orElse(null);
    if (offer == null) {
      throw new IllegalArgumentException("Wrong Offer id");
    }
    if (isChangelog(offer)) {
      DeedTenantOffer parentOffer = deedTenantOfferRepository.findById(offer.getParentId()).orElse(null);
      cancelChangeLog(parentOffer, offer);
    } else if (!blockchainService.isOfferEnabled(offer.getOfferId())) {
      deedTenantOfferRepository.delete(offer);
    } else {
      LOG.warn("Don't know what to do with a parent offer {} with blockchain Id {} that exists on blockchain and that is meant to have a valid transaction",
               offer.getId(),
               offer.getOfferId());
    }
  }

  private DeedTenantOffer getParentOfferFromChangelog(DeedTenantOffer offer) {
    if (!isChangelog(offer)) {
      return offer;
    }
    DeedTenantOffer parentOffer = deedTenantOfferRepository.findById(offer.getParentId()).orElse(null);
    if (parentOffer == null) {
      return offer;
    } else {
      // Copy fields that can't be retrieved from Contract
      // and are stored locally only
      parentOffer.setDescription(offer.getDescription());
      parentOffer.setPaymentPeriodicity(offer.getPaymentPeriodicity());
      return parentOffer;
    }
  }

  private String refreshOfferConcurrently(DeedTenantOffer offer, String walletAddress) throws Exception {
    if (offer == null) {
      throw new ObjectNotFoundException();
    }
    // Get refreshed parent offer after applying changelog
    String offerId = isChangelog(offer) ? offer.getParentId() : offer.getId();
    boolean isRefreshPermitted = acquireExplicitOfferRefreshLock(offerId);
    if (isRefreshPermitted) {
      try {
        LOG.info("Refreshing Changed offer with id {} on blockchain on request of wallet {}",
                 offerId,
                 walletAddress);
        refreshOffer(offer);
      } catch (ObjectNotFoundException e) {
        LOG.debug("It seems that there was a concurrent refresh with scheduled tasks, the offer was already refreshed. Original error: {}",
                  e.getMessage());
      }
    } else {
      LOG.debug("Refresh already made by another process of changed offer with id {}. Try just to retrieve the newer version.",
                offerId);
    }
    return offerId;
  }

  private void refreshOffer(DeedTenantOffer offer) throws Exception { // NOSONAR
    if (isOfferNotConfirmedYet(offer)) {
      if (isOfferTransactionInProgress(offer)) {
        Map<BlockchainOfferStatus, DeedOfferBlockchainState> offerTransactionEvents;
        offerTransactionEvents = blockchainService.getOfferTransactionEvents(offer.getOfferTransactionHash());
        if (!offerTransactionEvents.isEmpty()) {// Avoid marking Transaction as
                                                // error while it's an instant
                                                // refresh
          updateRentingOfferStatusFromBlockchain(offer.getId(), offerTransactionEvents);
        }
      }
    } else {
      long blockNumber = blockchainService.getLastBlock();
      DeedOfferBlockchainState blockchainOffer = blockchainService.getOfferById(BigInteger.valueOf(offer.getOfferId()),
                                                                                BigInteger.valueOf(blockNumber),
                                                                                offer.getOfferTransactionHash());
      if (isOfferDeleted(blockchainOffer)) {
        // It seems that offer was previously confirmed as created and then
        // deleted from blockchain
        updateRentingOfferStatusFromBlockchain(StringUtils.isBlank(offer.getDeleteId()) ? offer.getId() : offer.getDeleteId(),
                                               blockchainOffer,
                                               BlockchainOfferStatus.OFFER_DELETED);
      } else {
        if (StringUtils.isNotBlank(offer.getUpdateId())) {
          updateRentingOfferStatusFromBlockchain(blockchainOffer,
                                                 offer.getUpdateId(),
                                                 BlockchainOfferStatus.OFFER_UPDATED);
        }
        Set<String> acquisitionIds = offer.getAcquisitionIds();
        if (!CollectionUtils.isEmpty(acquisitionIds)) {
          for (String acquisitionId : acquisitionIds) {
            updateRentingOfferStatusFromBlockchain(blockchainOffer,
                                                   acquisitionId,
                                                   BlockchainOfferStatus.OFFER_ACQUIRED);
          }
        }
      }
    }
  }

  private void updateRentingOfferStatusFromBlockchain(DeedOfferBlockchainState blockchainOffer,
                                                      String offerChangeLogId,
                                                      BlockchainOfferStatus blockchainStatus) throws Exception {
    if (StringUtils.isNotBlank(offerChangeLogId)) {
      DeedTenantOffer changeLogOffer = deedTenantOfferRepository.findById(offerChangeLogId).orElse(null);
      if (isChangelog(changeLogOffer)
          && isOfferTransactionInProgress(changeLogOffer)
          && blockchainService.isTransactionMined(changeLogOffer.getOfferTransactionHash())) {

        if (blockchainService.isTransactionConfirmed(changeLogOffer.getOfferTransactionHash())) {
          updateRentingOfferStatusFromBlockchain(offerChangeLogId,
                                                 blockchainOffer,
                                                 blockchainStatus);
        } else {
          cancelOffer(changeLogOffer);
        }

      }
    }
  }

  private void updateRentingOfferStatusFromBlockchain(String updateOrDeleteOrParentId, // NOSONAR
                                                      DeedOfferBlockchainState blockchainOffer,
                                                      BlockchainOfferStatus status) throws Exception {
    DeedTenantOffer offer = deedTenantOfferRepository.findById(updateOrDeleteOrParentId).orElse(null);
    if (offer == null) {
      throw new ObjectNotFoundException("Offer with id " + updateOrDeleteOrParentId + "wasn't found");
    }
    long blockchainOfferId = blockchainOffer == null ? offer.getOfferId() : blockchainOffer.getId().longValue();
    LOG.debug("Attempt to refresh Changed offer with internal id {} and status {} retrieved from blockchain with id {}",
              updateOrDeleteOrParentId,
              status,
              blockchainOfferId);
    // While creating an offer, the offer Id is generated on Blockchain, while
    // in DB, we will have to generate an independent id. Thus we will have to
    // verify manually each time the unicity of blockchain Id before storing it
    // as new offer
    boolean isCreated = status == BlockchainOfferStatus.OFFER_CREATED && !isBlockchainOfferIdKnown(blockchainOfferId);
    boolean isDeleted = status == BlockchainOfferStatus.OFFER_DELETED;
    boolean isAcquired = status == BlockchainOfferStatus.OFFER_ACQUIRED;
    boolean isUpdated = status == BlockchainOfferStatus.OFFER_UPDATED
        || (!isDeleted && !isAcquired && !isCreated && offer.getOfferId() > 0);
    DeedTenantOffer offerToChange = null;
    try {
      if (isCreated) {
        if (blockchainOffer != null) {
          offerToChange = createOfferByBlockchainChange(blockchainOffer, offer);
        }
      } else if (isDeleted) {
        offerToChange = deleteOfferByBlockchainChange(blockchainOffer, offer);
      } else if (isAcquired) {
        offerToChange = markOfferAcquiredByBlockchainChange(offer);
      } else if (isUpdated) {
        offerToChange = updateOfferBlockchainChange(blockchainOffer, offer);
      } else {
        LOG.warn("Wasn't able to determine offer {} event type. Simply disable it for now", offer.getOfferId());
        offer.setEnabled(false);
        offer.setOfferTransactionStatus(TransactionStatus.NONE);
        saveOffer(offer);
      }
    } finally {
      if (offerToChange != null) {
        if (isCreated) {
          listenerService.publishEvent(OFFER_CREATED_CONFIRMED_EVENT, DeedTenantOfferMapper.toDTO(offerToChange));
        } else if (isUpdated) {
          listenerService.publishEvent(OFFER_UPDATED_CONFIRMED_EVENT, DeedTenantOfferMapper.toDTO(offerToChange));
        } else if (isDeleted) { // NOSONAR
          listenerService.publishEvent(OFFER_DELETED_CONFIRMED_EVENT, DeedTenantOfferMapper.toDTO(offerToChange));
        } else if (isAcquired) { // NOSONAR
          listenerService.publishEvent(OFFER_ACQUISITION_CONFIRMED_EVENT, DeedTenantOfferMapper.toDTO(offerToChange));
        }
      }
    }
  }

  private void createOfferAcquisitionChangeLog(DeedTenantOffer parentOffer, String transactionHash) {
    DeedTenantOffer offerChangeLog = DeedTenantOfferMapper.toOfferChangeLog(parentOffer, transactionHash);
    offerChangeLog = saveOffer(offerChangeLog, false);

    Set<String> acquisitionIds = CollectionUtils.isEmpty(parentOffer.getAcquisitionIds()) ? new HashSet<>()
                                                                                          : new HashSet<>(parentOffer.getAcquisitionIds());
    acquisitionIds.add(offerChangeLog.getId());
    parentOffer.setAcquisitionIds(acquisitionIds);
    parentOffer = saveOffer(parentOffer);

    listenerService.publishEvent(OFFER_ACQUISITION_PROGRESS_EVENT, parentOffer);

    LOG.debug("Mark Offer {} as acquisition in progress, acquisition ids for current NFT = {}",
              parentOffer.getOfferId(),
              acquisitionIds);
  }

  private DeedTenantOffer createOfferByBlockchainChange(DeedOfferBlockchainState blockchainOffer,
                                                        DeedTenantOffer offer) throws Exception {
    boolean offerDeleted = isOfferDeleted(blockchainOffer);
    boolean offerIdKnown = isBlockchainOfferIdKnown(blockchainOffer.getId().longValue());
    boolean invalidMinedOffer = offerDeleted || offerIdKnown;
    if (invalidMinedOffer
        && StringUtils.isNotBlank(offer.getOfferTransactionHash())
        && !blockchainService.isTransactionConfirmed(offer.getOfferTransactionHash())) {
      offer.setOfferTransactionStatus(TransactionStatus.ERROR);
      offer.setEnabled(false);
      offer.setViewAddresses(Collections.emptyList());
      LOG.warn("Disabled invalid Offer with id {}. offerIdKnown= {}, offerDeleted={}",
               blockchainOffer.getId().longValue(),
               offerDeleted,
               offerIdKnown);
      return saveOffer(offer);
    } else if (invalidMinedOffer) {
      LOG.warn("Invalid Offer status retrieved from blockchain with id {}. offerIdKnown= {}, offerDeleted={}. Ignore adding it for now and wait for periodic check again",
               blockchainOffer.getId().longValue(),
               offerDeleted,
               offerIdKnown);
      return null;
    } else {
      offer.setEnabled(true);
      offer.setOfferTransactionStatus(TransactionStatus.VALIDATED);
    }
    offer.setCreatedDate(Instant.now());
    return updateOfferFromBlockchainStatus(offer, blockchainOffer, false);
  }

  private DeedTenantOffer deleteOfferByBlockchainChange(DeedOfferBlockchainState blockchainOffer,
                                                        DeedTenantOffer offer) {
    LOG.debug("Refreshing deleted offer with id {} switch blockchain status",
              offer.getOfferId());

    DeedTenantOffer offerToChange = getParentOfferFromChangelog(offer);
    offerToChange.setEnabled(false);
    offerToChange.setLastCheckedBlock(getLastCheckedBlockNumber(blockchainOffer, offerToChange));
    return saveOfferAndDeleteChangeLog(offerToChange, offer.getId());
  }

  private DeedTenantOffer markOfferAcquiredByBlockchainChange(DeedTenantOffer offer) {
    LOG.debug("Refreshing acuired offer with id {} switch blockchain status",
              offer.getOfferId());

    DeedTenantOffer parentOffer = getParentOfferFromChangelog(offer);
    parentOffer.setAcquired(true);
    return saveOfferAndDeleteChangeLog(parentOffer, offer.getId());
  }

  private DeedTenantOffer updateOfferBlockchainChange(DeedOfferBlockchainState blockchainOffer,
                                                      DeedTenantOffer offer) throws Exception {
    long lastCheckedBlock = offer.getLastCheckedBlock();
    DeedTenantOffer offerToChange = getParentOfferFromChangelog(offer);
    boolean isChangelogAlreadyApplied = offerToChange.getLastCheckedBlock() > lastCheckedBlock;
    boolean isDeedOfferDeleted = isOfferDeleted(blockchainOffer);
    if (isDeedOfferDeleted) {
      LOG.debug("Offer Changelog with id {} of parent offer id {} (offerId = {}) will not be applied since the offer is already deleted",
                offer.getId(),
                offer.getParentId(),
                offer.getOfferId());
    } else if (isChangelogAlreadyApplied) { // NOSONAR Keep this for
                                            // eventual evolutions on code
                                            // to be detected
      LOG.debug("Offer Changelog with id {} of parent offer id {} (offerId = {}) was already applied, delete it",
                offer.getId(),
                offer.getParentId(),
                offer.getOfferId());
    } else {
      offerToChange = updateOfferFromBlockchainStatus(offerToChange, blockchainOffer, true);
    }
    return saveOfferAndDeleteChangeLog(offerToChange, offer.getId());
  }

  private void cancelOffer(DeedTenantOffer offer) {
    if (offer == null) {
      return;
    }
    if (isChangelog(offer)) {
      DeedTenantOffer parentOffer = deedTenantOfferRepository.findById(offer.getParentId()).orElse(null);
      cancelChangeLog(parentOffer, offer);
    } else {
      offer.setEnabled(false);
      offer = saveOfferAndDeleteChangeLogs(offer);
      listenerService.publishEvent(OFFER_CANCELED_EVENT, offer);
    }
  }

  private void cancelChangeLog(DeedTenantOffer parentOffer, DeedTenantOffer offer) {
    if (parentOffer == null) {
      // Orphan changelog, delete simply delete it
      deedTenantOfferRepository.delete(offer);
    } else {
      saveOfferAndDeleteChangeLog(parentOffer, offer.getId());
    }
  }

  private boolean isChangelog(DeedTenantOffer offer) {
    return offer != null && StringUtils.isNotBlank(offer.getParentId());
  }

  private DeedTenantOffer saveOfferAndDeleteChangeLog(DeedTenantOffer parentOffer, String changeLogId) {
    if (isChangelog(parentOffer) || parentOffer == null) {
      throw new IllegalArgumentException("Attempt to save changelog offer: " + parentOffer);
    }
    LOG.debug("Delete offer changelog after applying it with id {} for parent id {}",
              changeLogId,
              parentOffer.getId());
    if (StringUtils.equals(parentOffer.getUpdateId(), changeLogId)) {
      parentOffer.setUpdateId(null);
      parentOffer = saveOffer(parentOffer);
      // Avoid deleting changelog before saving parent
      // To make sure that parent has no "updateId" on it
      // even if deletion fails or is interrupted
      deedTenantOfferRepository.deleteById(changeLogId);
      return parentOffer;
    } else if (StringUtils.equals(parentOffer.getDeleteId(), changeLogId)) {
      return saveOfferAndDeleteChangeLogs(parentOffer);
    } else if (parentOffer.getAcquisitionIds() != null && parentOffer.getAcquisitionIds().contains(changeLogId)) {
      return saveOfferAndDeleteChangeLogs(parentOffer);
    } else {
      return saveOffer(parentOffer);
    }
  }

  private DeedTenantOffer saveOfferAndDeleteChangeLogs(DeedTenantOffer parentOffer) {
    if (isChangelog(parentOffer) || parentOffer == null) {
      throw new IllegalArgumentException("Attempt to save changelog offer: " + parentOffer);
    }
    parentOffer.setUpdateId(null);
    parentOffer.setDeleteId(null);
    parentOffer.setAcquisitionIds(Collections.emptySet());
    parentOffer = saveOffer(parentOffer);
    // Avoid deleting changelog before saving parent
    // To make sure that parent has no "updateId" on it
    // even if deletion fails or is interrupted
    deedTenantOfferRepository.deleteByParentId(parentOffer.getId());
    return parentOffer;
  }

  private DeedTenantOffer updateOfferFromBlockchainStatus(DeedTenantOffer offer,
                                                          DeedOfferBlockchainState blockchainOffer,
                                                          boolean isUpdate) throws Exception {
    checkOfferBlockchainStatus(offer, blockchainOffer, isUpdate);
    offer.setOfferTransactionStatus(TransactionStatus.VALIDATED);
    return updateOfferFromBlockchain(offer, blockchainOffer);
  }

  private DeedTenantOffer updateOfferFromBlockchain(DeedTenantOffer offer,
                                                    DeedOfferBlockchainState blockchainOffer) throws Exception {
    if (blockchainOffer == null) {
      throw new IllegalArgumentException("Blockchain Offer is null");
    }
    boolean isDeletedOffer = isOfferDeleted(blockchainOffer);
    long blockchainOfferId = blockchainOffer.getId().longValue();
    long nftId = blockchainOffer.getDeedId().longValue();
    String ownerAddress = blockchainOffer.getCreator();
    long blockNumber = blockchainOffer.getBlockNumber().longValue();
    if (offer == null) {
      if (isDeletedOffer) {
        LOG.debug("The retrieved offer doesn't exist neither on database nor on blockchain, this seems to be an outdated event about deleted Offer, ignore it");
        return null;
      }
      if (isOfferTransactionHashDuplicated(blockchainOffer.getTransactionHash(), null)) {
        LOG.debug("The retrieved offer {} creation event already known, ignore this event", blockchainOfferId);
        return null;
      }
      offer = new DeedTenantOffer();
      offer.setOfferType(OfferType.RENTING);
      offer.setPaymentPeriodicity(RentalPaymentPeriodicity.ONE_MONTH);
      offer.setOfferTransactionStatus(TransactionStatus.VALIDATED);
      offer.setCreatedDate(Instant.now());
      offer.setModifiedDate(offer.getCreatedDate());

      DeedTenant deedTenant = getDeedTenant(nftId, ownerAddress);
      setOfferNftInformation(offer, deedTenant);
      LOG.info("Add offer created directly on blockchain with id {} on deed {} as enabled = {}",
               blockchainOfferId,
               nftId,
               offer.isEnabled());
    } else {
      long lastCheckedBlock = offer.getLastCheckedBlock();
      if (blockNumber > 0 && lastCheckedBlock > 0 && lastCheckedBlock >= blockNumber) {
        if (isDeletedOffer && offer.isEnabled() && !blockchainService.isOfferEnabled(blockchainOfferId)) {
          LOG.debug("It seems that Offer {} has been already deleted in blockchain in block {}. Mark it as disabled.",
                    offer.getOfferId(),
                    lastCheckedBlock);
          offer.setEnabled(false);
          saveOffer(offer);
        } else {
          LOG.debug("Cancel Offer Status Refresh which is already updated in block {} (last checked block {}). Ignore updating.",
                    blockNumber,
                    lastCheckedBlock);
        }
        return offer;
      }
      offer.setModifiedDate(Instant.now());
    }
    offer.setLastCheckedBlock(getLastCheckedBlockNumber(blockchainOffer, offer));
    copyBlockchainAttributes(offer, blockchainOffer);
    offer = saveOffer(offer);

    LOG.debug("Refresh done for Offer {} from blockchain with id {}",
              offer.getId(),
              offer.getOfferId());
    return offer;
  }

  private void copyBlockchainAttributes(DeedTenantOffer offer, DeedOfferBlockchainState blockchainOffer) throws Exception {
    long blockchainOfferId = blockchainOffer.getId().longValue();
    long nftId = blockchainOffer.getDeedId().longValue();
    if (blockchainOfferId <= 0 || nftId <= 0) {
      LOG.debug("It seems that we have a bug here, since we attempt copy a deleted blockchain offer. Ignore updating.");
      return;
    }
    if (StringUtils.isBlank(offer.getOfferTransactionHash()) && StringUtils.isNotBlank(blockchainOffer.getTransactionHash())) {
      offer.setOfferTransactionHash(blockchainOffer.getTransactionHash());
    }
    offer.setOfferId(blockchainOfferId);
    offer.setNftId(nftId);
    offer.setOwner(blockchainOffer.getCreator());
    offer.setAmount(blockchainOffer.getPrice().divide(BigInteger.valueOf(10).pow(18)).doubleValue());
    offer.setAllDurationAmount(blockchainOffer.getAllDurationPrice().divide(BigInteger.valueOf(10).pow(18)).doubleValue());
    offer.setStartDate(Instant.ofEpochSecond(blockchainOffer.getOfferStartDate().longValue()));
    offer.setEnabled(blockchainService.isOfferEnabled(offer.getOfferId()));
    long expirationTimeInSeconds = blockchainOffer.getOfferExpirationDate().longValue();
    offer.setExpirationDate(expirationTimeInSeconds == 0 ? DeedTenantOfferMapper.MAX_DATE_VALUE // NOSONAR
                                                         : Instant.ofEpochSecond(expirationTimeInSeconds));
    if (StringUtils.equals(Address.DEFAULT.getValue(), blockchainOffer.getAuthorizedTenant())) {
      offer.setHostAddress(null);
    } else {
      offer.setHostAddress(StringUtils.lowerCase(blockchainOffer.getAuthorizedTenant()));
    }
    offer.setOwnerMintingPercentage(blockchainOffer.getOwnerMintingPercentage().intValue());
    setDuration(offer, blockchainOffer.getMonths().intValue());
    setNoticePeriod(offer, blockchainOffer.getNoticePeriod().intValue());
    setExpirationDuration(offer, blockchainOffer.getOfferExpirationDays().intValue());
  }

  private boolean isOfferOngoing(DeedTenantOffer parentOffer, DeedTenantOffer offer, Instant leaseEndDate) {
    if (!parentOffer.isEnabled()
        || parentOffer.isAcquired()
        || parentOffer.getOfferTransactionStatus() == TransactionStatus.ERROR
        || isChangelog(parentOffer)
        || (offer != null && StringUtils.equals(parentOffer.getId(), offer.getId()))) {
      return false;
    } else if (isOfferStartsAfter(parentOffer, leaseEndDate)) {
      LOG.debug("Offer {} of Acquired NFT {} will not be marqued as 'acquisition in progress' since its start date is far in the future after acquired lease end",
                parentOffer.getId(),
                parentOffer.getNftId());
      return false;
    }
    return true;
  }

  private boolean isOfferStartsAfter(DeedTenantOffer offer, Instant leaseEndDate) {
    return offer.getStartDate() != null
        && offer.getStartDate().isAfter(leaseEndDate);
  }

  private void checkTransactionHashIsUnknown(String transactionHash) {
    boolean isKnown = isOfferTransactionHashDuplicated(transactionHash, null);
    if (isKnown) {
      throw new IllegalStateException("Offer with same Transaction Hash " + transactionHash + " already exists");
    }
  }

  private boolean isOfferTransactionHashDuplicated(String transactionHash, String offerId) {
    DeedTenantOffer offer = getOfferByTransactionHash(transactionHash);
    return offer != null && (StringUtils.isBlank(offerId) || !StringUtils.equals(offerId, offer.getId()));
  }

  private DeedTenantOffer getOfferByTransactionHash(String transactionHash) {
    return deedTenantOfferRepository.findByOfferTransactionHash(StringUtils.lowerCase(transactionHash));
  }

  private boolean isBlockchainOfferIdKnown(long blockchainOfferId) {
    return !CollectionUtils.isEmpty(deedTenantOfferRepository.findByOfferIdAndParentIdIsNull(blockchainOfferId));
  }

  private void setExpirationDuration(DeedTenantOffer offer, int expirationDurationDays) {
    switch (expirationDurationDays) {
    case 0:
      offer.setExpirationDuration(null);
      break;
    case 1:
      offer.setExpirationDuration(ExpirationDuration.ONE_DAY);
      break;
    case 3:
      offer.setExpirationDuration(ExpirationDuration.THREE_DAYS);
      break;
    case 7:
      offer.setExpirationDuration(ExpirationDuration.ONE_WEEK);
      break;
    case 30:
      offer.setExpirationDuration(ExpirationDuration.ONE_MONTH);
      break;
    default:
      offer.setExpirationDuration(null);
      break;
    }
  }

  private void setNoticePeriod(DeedTenantOffer offer, int noticePeriodDuration) {
    switch (noticePeriodDuration) {
    case 0:
      offer.setNoticePeriod(NoticePeriod.NO_PERIOD);
      break;
    case 1:
      offer.setNoticePeriod(NoticePeriod.ONE_MONTH);
      break;
    case 2:
      offer.setNoticePeriod(NoticePeriod.TWO_MONTHS);
      break;
    case 3:
      offer.setNoticePeriod(NoticePeriod.THREE_MONTHS);
      break;
    default:
      LOG.warn("Unsupported Rental Notice Period duration found in transaction : {} Months",
               noticePeriodDuration);
      break;
    }
  }

  private void setDuration(DeedTenantOffer offer, int rentalDurationInMonths) {
    switch (rentalDurationInMonths) {
    case 1:
      offer.setDuration(RentalDuration.ONE_MONTH);
      break;
    case 3:
      offer.setDuration(RentalDuration.THREE_MONTHS);
      break;
    case 6:
      offer.setDuration(RentalDuration.SIX_MONTHS);
      break;
    case 12:
      offer.setDuration(RentalDuration.ONE_YEAR);
      break;
    default:
      LOG.warn("Unsupported Rental Months duration found in transaction : {} Months", rentalDurationInMonths);
      break;
    }
  }

  private DeedTenantOffer saveOffer(DeedTenantOffer offer) {
    return saveOffer(offer, true);
  }

  private DeedTenantOffer saveOffer(DeedTenantOffer offer, boolean checkTransactionHashDuplication) { // NOSONAR
    if (offer.getExpirationDuration() == null) {
      offer.setExpirationDate(DeedTenantOfferMapper.MAX_DATE_VALUE);
    }
    if (offer.isAcquired() || !offer.isEnabled()) {
      offer.setEnabled(false);
      offer.setViewAddresses(Collections.emptyList());
    } else if (offer.getOfferId() != 0 && offer.getOfferTransactionStatus() == TransactionStatus.VALIDATED) {
      offer.setViewAddresses(Collections.singletonList(DeedTenantOfferMapper.EVERYONE));
    } else {
      offer.setViewAddresses(Collections.singletonList(StringUtils.lowerCase(offer.getOwner())));
    }
    String id = offer.getId();
    if (StringUtils.isBlank(offer.getOfferTransactionHash())) {
      throw new IllegalStateException("Offer " + id + "/" + offer.getOfferId() + " transaction hash is mandatory");
    }
    // Can't add field unicity on ElasticSearch, thus we have to make a manual
    // check each time
    boolean isNewOffer = StringUtils.isBlank(id);
    if (!isNewOffer) {
      DeedTenantOffer existingOffer = deedTenantOfferRepository.findById(id).orElse(null);
      if (existingOffer == null) {
        throw new IllegalStateException("Offer to update doesn't exists");
      }
    }
    boolean isChangelog = isChangelog(offer);
    if (isNewOffer
        && !isChangelog
        && offer.getOfferId() > 0
        && isBlockchainOfferIdKnown(offer.getOfferId())) {
      throw new IllegalStateException("Offer with same identifier already exists, must not save twice the same Parent OfferId");
    }
    // Can't add field unicity on ElasticSearch, thus we have to make a manual
    // check each time
    if (isNewOffer
        && checkTransactionHashDuplication
        && isOfferTransactionHashDuplicated(offer.getOfferTransactionHash(), id)) {
      throw new IllegalStateException("Offer with same transaction hash already exists");
    }
    String parentId = offer.getParentId();
    try {
      return deedTenantOfferRepository.save(offer);
    } finally {
      if (isChangelog && isNewOffer) {
        LOG.debug("Delete acquired UI Refresh Lock on Offer {} after a new changelog has been added",
                  parentId);
        releaseExplicitOfferRefreshLock(parentId);
      }
    }
  }

  private boolean acquireExplicitOfferRefreshLock(String offerId) {
    try {
      StampedLock lock = blockchainRefreshLocks.computeIfAbsent(offerId, key -> new StampedLock());
      long stamp = lock.tryWriteLock();
      if (stamp > 0) {
        blockchainRefreshStamp.put(offerId, stamp);
        return true;
      } else {
        // Wait 3 seconds until Refresh made effectively by other process
        lock.tryWriteLock(3, TimeUnit.SECONDS);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      LOG.debug("Refresh Lock on Offer {} not acquired after 3 seconds, proceed to refresh offer drom DB",
                offerId);
    }
    return false;
  }

  private void releaseExplicitOfferRefreshLock(String parentOfferId) {
    StampedLock lock = blockchainRefreshLocks.remove(parentOfferId);
    if (lock != null && blockchainRefreshStamp.containsKey(parentOfferId)) {
      Long stamp = blockchainRefreshStamp.remove(parentOfferId);
      try {
        lock.unlock(stamp);
      } catch (Exception e) {
        LOG.debug("Wasn't able to release Blockchain Refresh lock of parent offer with id {}. Unlocked anyway and released.",
                  parentOfferId,
                  e);
      }
    }
  }

  private long getLastCheckedBlockNumber(DeedOfferBlockchainState blockchainOffer, DeedTenantOffer offer) {
    return Math.max(blockchainOffer.getBlockNumber().longValue(), offer.getLastCheckedBlock());
  }

  private String getNftNotExistsMessage(long nftId) {
    return "Deed Tenant with id " + nftId + " doesn't exists";
  }

  private String getNotOwnerMessage(String walletAddress, long nftId) {
    return walletAddress + " isn't owner of Deed NFT #" + nftId;
  }

  private boolean isOfferNotConfirmedYet(DeedTenantOffer offer) {
    return offer.getOfferId() == 0;
  }

  private boolean isOfferTransactionInProgress(DeedTenantOffer offer) {
    return offer.getOfferTransactionStatus() == TransactionStatus.IN_PROGRESS
        && StringUtils.isNotBlank(offer.getOfferTransactionHash());
  }

  private void checkOfferBlockchainStatus(DeedTenantOffer offer, DeedOfferBlockchainState blockchainOffer, boolean isUpdate) {
    if (isUpdate && offer.getOfferId() != blockchainOffer.getId().longValue()) {
      LOG.warn("HACK Tentative or Bug? The transaction hash has been replaced by a transaction of another OFFER Identifier: {} VS {}. The information will be retrieved from Blockchain anyway to replace it.",
               offer.getOfferId(),
               blockchainOffer.getId().longValue());
    }
    if (offer.getNftId() != blockchainOffer.getDeedId().longValue()) {
      LOG.warn("HACK Tentative or Bug? The transaction hash has been replaced by a transaction of another DEED: {} VS {}. The information will be retrieved from Blockchain anyway to replace it.",
               offer.getNftId(),
               blockchainOffer.getDeedId().longValue());
    }
    if (!StringUtils.equalsIgnoreCase(offer.getOwner(), blockchainOffer.getCreator())) {
      LOG.warn("HACK Tentative or Bug? The transaction owner isn't the same as Offer owner: {} VS {}. The information will be retrieved from Blockchain anyway to replace it.",
               blockchainOffer.getCreator(),
               offer.getOwner());
    }
  }

  private void setOfferNftInformation(DeedTenantOffer deedTenantOffer, DeedTenant deedTenant) {
    if (deedTenant.getCardType() >= 0) {
      DeedCard cardType = DeedCard.values()[deedTenant.getCardType()];
      deedTenantOffer.setCardType(cardType);
      deedTenantOffer.setMintingPower(cardType.getMintingPower());
    }
    if (deedTenant.getCityIndex() >= 0) {
      deedTenantOffer.setCity(DeedCity.values()[deedTenant.getCityIndex()]);
    }
  }

  private DeedTenant getDeedTenant(long nftId, String managerAddress) {
    DeedTenant deedTenant;
    try {
      deedTenant = tenantService.getDeedTenantOrImport(managerAddress, nftId);
    } catch (UnauthorizedOperationException e) {
      deedTenant = tenantService.getDeedTenant(nftId);
    } catch (ObjectNotFoundException e) {
      throw new IllegalStateException("Unable to locate nft with id " + nftId, e);
    }
    return deedTenant;
  }

  private DeedTenantOffer getOfferByBlockchainOfferId(long blockchainOfferId, boolean updateIfNotFound) throws Exception {
    DeedTenantOffer offer = getParentOfferByBlockchainId(blockchainOfferId);
    if (offer == null) {
      if (updateIfNotFound) {
        DeedOfferBlockchainState blockchainOffer = blockchainService.getOfferById(BigInteger.valueOf(blockchainOfferId),
                                                                                  null,
                                                                                  null);
        return updateOfferFromBlockchain(blockchainOffer, false);
      } else {
        return null;
      }
    } else {
      return offer;
    }
  }

  private DeedTenantOffer getParentOfferByBlockchainId(long blockchainOfferId) {
    List<DeedTenantOffer> offers = deedTenantOfferRepository.findByOfferIdAndParentIdIsNull(blockchainOfferId);
    if (CollectionUtils.isEmpty(offers)) {
      return null;
    } else {
      if (offers.size() > 1) {
        LOG.warn("It seems that we have more than one parent offer with id {}, this is likely a bug! Retrieve first one only. List = {}",
                 blockchainOfferId,
                 offers);
      }
      return offers.get(0);
    }
  }

  private boolean isOfferDeleted(DeedOfferBlockchainState blockchainOffer) {
    return blockchainOffer.getId() == null
        || blockchainOffer.getId().longValue() == 0
        || blockchainOffer.getDeedId() == null
        || blockchainOffer.getDeedId().longValue() == 0;
  }

}
