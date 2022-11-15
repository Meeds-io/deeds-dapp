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

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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

import io.meeds.deeds.constant.DeedCard;
import io.meeds.deeds.constant.DeedCity;
import io.meeds.deeds.constant.ObjectAlreadyExistsException;
import io.meeds.deeds.constant.ObjectNotFoundException;
import io.meeds.deeds.constant.OfferType;
import io.meeds.deeds.constant.UnauthorizedOperationException;
import io.meeds.deeds.model.DeedTenant;
import io.meeds.deeds.model.DeedTenantOffer;
import io.meeds.deeds.model.DeedTenantOfferDTO;
import io.meeds.deeds.model.DeedTenantOfferFilter;
import io.meeds.deeds.storage.DeedTenantOfferRepository;
import io.meeds.deeds.utils.Mapper;

@Component
public class DeedTenantOfferService {

  private static final String       OFFER_CREATED_EVENT  = "deed.event.offerCreated";

  private static final String       OFFER_UPDATED_EVENT  = "deed.event.offerUpdated";

  private static final String       OFFER_DELETED_EVENT  = "deed.event.offerDeleted";

  private static final String       OFFER_CANCELED_EVENT = "deed.event.offerCanceled";

  @Autowired
  private DeedTenantOfferRepository deedTenantOfferRepository;

  @Autowired
  private ElasticsearchOperations   elasticsearchOperations;

  @Autowired
  private TenantService             tenantService;

  @Autowired
  private ListenerService           listenerService;

  public Page<DeedTenantOfferDTO> getOffersList(DeedTenantOfferFilter offerFilter, Pageable pageable) {
    Criteria criteria = null;
    if (StringUtils.isNotBlank(offerFilter.getOwnerAddress())) {
      criteria = addAndCriteria(criteria, new Criteria("owner").is(offerFilter.getOwnerAddress().toLowerCase()));
    }
    if (offerFilter.isExcludeDisabled()) {
      Criteria enabledCriteria = new Criteria("enabled").is(true);
      criteria = addAndCriteria(criteria, enabledCriteria);
    }
    if (offerFilter.getNftId() >= 0) {
      Criteria nftIdCriteria = new Criteria("nftId").is(offerFilter.getNftId());
      criteria = addAndCriteria(criteria, nftIdCriteria);
    }
    if (!CollectionUtils.isEmpty(offerFilter.getCardTypes())) {
      Criteria cardTypeCriteria = new Criteria("cardType").in(offerFilter.getCardTypes());
      criteria = addAndCriteria(criteria, cardTypeCriteria);
    }
    if (!CollectionUtils.isEmpty(offerFilter.getOfferTypes())) {
      Criteria offerCriteria = new Criteria("offerType").in(offerFilter.getOfferTypes());
      criteria = addAndCriteria(criteria, offerCriteria);
    }

    if (offerFilter.isExcludeExpired()) {
      Criteria expirationDateCriteria = new Criteria("expirationDate").greaterThan(Instant.now());
      criteria = addAndCriteria(criteria, expirationDateCriteria);
    }
    if (criteria == null) {
      criteria = new Criteria("nftId").exists();
    }
    CriteriaQuery query = new CriteriaQuery(criteria, pageable);
    SearchHits<DeedTenantOffer> result = elasticsearchOperations.search(query, DeedTenantOffer.class);
    SearchPage<DeedTenantOffer> searchPage = SearchHitSupport.searchPageFor(result, pageable);
    return searchPage.map(SearchHit::getContent)
                     .map(Mapper::toDTO);
  }

  public DeedTenantOfferDTO getOffer(String offerId) {
    DeedTenantOffer offer = deedTenantOfferRepository.findById(offerId).orElse(null);
    return Mapper.toDTO(offer);
  }

  public DeedTenantOfferDTO createRentingOffer(String walletAddress,
                                               DeedTenantOfferDTO deedTenantOfferDTO) throws ObjectNotFoundException,
                                                                                      UnauthorizedOperationException,
                                                                                      ObjectAlreadyExistsException {
    long nftId = deedTenantOfferDTO.getNftId();
    if (!tenantService.isDeedOwner(walletAddress, nftId)) {
      throw new UnauthorizedOperationException(getNotOwnerMessage(walletAddress, nftId));
    }
    if (!tenantService.isDeedManager(walletAddress, nftId)) {
      throw new IllegalStateException("Deed Tenant is currently used by another manager, thus can't be rented");
    }
    DeedTenant deedTenant = tenantService.getDeedTenantOrImport(walletAddress, nftId);
    if (deedTenant == null) {
      throw new ObjectNotFoundException(getNftNotExistsMessage(nftId));
    }
    if (deedTenant.getTenantProvisioningStatus() != null && deedTenant.getTenantProvisioningStatus().isStart()) {
      throw new IllegalStateException("Deed Tenant is currently used, thus can't be rented");
    }
    DeedTenantOffer deedTenantOffer = Mapper.fromDTO(deedTenantOfferDTO);
    // When multiple offers will be supported, then make the id
    // auto incremented
    if (deedTenant.getCardType() >= 0) {
      DeedCard cardType = DeedCard.values()[deedTenant.getCardType()];
      deedTenantOffer.setCardType(cardType);
      deedTenantOffer.setMintingPower(cardType.getMintingPower());
    }
    if (deedTenant.getCityIndex() >= 0) {
      deedTenantOffer.setCity(DeedCity.values()[deedTenant.getCityIndex()]);
    }
    deedTenantOffer.setOwner(walletAddress.toLowerCase());
    Instant now = Instant.now();
    deedTenantOffer.setCreatedDate(now);
    deedTenantOffer.setModifiedDate(now);
    deedTenantOffer.setOfferType(OfferType.RENTING);
    if (deedTenantOffer.getExpirationDuration() != null) {
      Instant expirationDate = now.atZone(ZoneOffset.UTC).plus(deedTenantOffer.getExpirationDuration().getPeriod()).toInstant();
      deedTenantOffer.setExpirationDate(expirationDate);
    }
    deedTenantOffer.setEnabled(true);
    DeedTenantOfferFilter enabledNftOffersFilter = DeedTenantOfferFilter.ofNftId(nftId).withExcludeDisabled(true);
    Page<DeedTenantOfferDTO> offersList = getOffersList(enabledNftOffersFilter, Pageable.ofSize(1));
    if (offersList.getTotalElements() > 0) {
      throw new ObjectAlreadyExistsException("NFT with ID " + nftId + " already exists");
    }
    deedTenantOffer = saveOffer(deedTenantOffer);
    listenerService.publishEvent(OFFER_CREATED_EVENT, deedTenantOffer);
    return Mapper.toDTO(deedTenantOffer);
  }

  public DeedTenantOfferDTO updateRentingOffer(String walletAddress,
                                               DeedTenantOfferDTO deedTenantOfferDTO) throws ObjectNotFoundException,
                                                                                      UnauthorizedOperationException {
    if (!tenantService.isDeedOwner(walletAddress, deedTenantOfferDTO.getNftId())) {
      throw new UnauthorizedOperationException(getNotOwnerMessage(walletAddress, deedTenantOfferDTO.getNftId()));
    }
    if (!tenantService.isDeedManager(walletAddress, deedTenantOfferDTO.getNftId())) {
      throw new IllegalStateException("Deed Tenant is currently used by another manager, thus can't be rented");
    }
    DeedTenantOffer existingDeedTenantOffer = deedTenantOfferRepository.findById(deedTenantOfferDTO.getId()).orElse(null);
    if (existingDeedTenantOffer == null) {
      throw new ObjectNotFoundException("Offer with id  " + deedTenantOfferDTO.getId() + " doesn't exist");
    }
    existingDeedTenantOffer.setOwner(walletAddress.toLowerCase());
    existingDeedTenantOffer.setModifiedDate(Instant.now());
    existingDeedTenantOffer.setDescription(deedTenantOfferDTO.getDescription());
    existingDeedTenantOffer.setAmount(deedTenantOfferDTO.getAmount());
    existingDeedTenantOffer.setDuration(deedTenantOfferDTO.getDuration());
    existingDeedTenantOffer.setExpirationDuration(deedTenantOfferDTO.getExpirationDuration());
    existingDeedTenantOffer.setPaymentPeriodicity(deedTenantOfferDTO.getPaymentPeriodicity());
    existingDeedTenantOffer.setSecurityDepositPeriod(deedTenantOfferDTO.getSecurityDepositPeriod());
    existingDeedTenantOffer.setNoticePeriod(deedTenantOfferDTO.getNoticePeriod());
    existingDeedTenantOffer.setOwnerMintingPercentage(deedTenantOfferDTO.getOwnerMintingPercentage());
    if (deedTenantOfferDTO.getExpirationDuration() == null) {
      existingDeedTenantOffer.setExpirationDate(null);
    } else {
      Instant expirationDate = Instant.now()
                                      .atZone(ZoneOffset.UTC)
                                      .plus(deedTenantOfferDTO.getExpirationDuration().getPeriod())
                                      .toInstant();
      existingDeedTenantOffer.setExpirationDate(expirationDate);
    }

    existingDeedTenantOffer = saveOffer(existingDeedTenantOffer);
    listenerService.publishEvent(OFFER_UPDATED_EVENT, existingDeedTenantOffer);
    return Mapper.toDTO(existingDeedTenantOffer);
  }

  public void deleteRentingOffer(String walletAddress, String offerId) throws ObjectNotFoundException,
                                                                       UnauthorizedOperationException {
    DeedTenantOffer existingDeedTenantOffer = deedTenantOfferRepository.findById(offerId).orElse(null);
    if (existingDeedTenantOffer == null) {
      throw new ObjectNotFoundException("Offer with id  " + offerId + " doesn't exist");
    }
    if (!tenantService.isDeedOwner(walletAddress, existingDeedTenantOffer.getNftId())) {
      throw new UnauthorizedOperationException(getNotOwnerMessage(walletAddress, existingDeedTenantOffer.getNftId()));
    }
    listenerService.publishEvent(OFFER_DELETED_EVENT, existingDeedTenantOffer);
    deedTenantOfferRepository.deleteById(offerId);
  }

  public void cancelOffers(String newOwner, long nftId) {
    List<DeedTenantOffer> offers = deedTenantOfferRepository.findByOwnerNotAndNftIdAndEnabledTrue(newOwner, nftId);
    if (!CollectionUtils.isEmpty(offers)) {
      deedTenantOfferRepository.deleteAll(offers);
      offers.forEach(offer -> listenerService.publishEvent(OFFER_CANCELED_EVENT, offer));
    }
  }

  private DeedTenantOffer saveOffer(DeedTenantOffer deedTenantOffer) {
    return deedTenantOfferRepository.save(deedTenantOffer);
  }

  private String getNftNotExistsMessage(long nftId) {
    return "Deed Tenant with id " + nftId + " doesn't exists";
  }

  private String getNotOwnerMessage(String walletAddress, long nftId) {
    return walletAddress + " isn't owner of Deed NFT #" + nftId;
  }

  private Criteria addAndCriteria(Criteria criteria, Criteria ownerCriteria) {
    if (criteria == null) {
      criteria = ownerCriteria;
    } else {
      criteria.and(ownerCriteria);
    }
    return criteria;
  }

}
