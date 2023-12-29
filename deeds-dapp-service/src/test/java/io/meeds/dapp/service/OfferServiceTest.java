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

import static io.meeds.dapp.service.OfferService.OFFER_ACQUISITION_PROGRESS_EVENT;
import static io.meeds.dapp.service.OfferService.OFFER_CANCELED_EVENT;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;

import io.meeds.dapp.constant.ExpirationDuration;
import io.meeds.dapp.constant.NoticePeriod;
import io.meeds.dapp.constant.OfferType;
import io.meeds.dapp.constant.RentalDuration;
import io.meeds.dapp.constant.RentalPaymentPeriodicity;
import io.meeds.dapp.model.DeedTenantOffer;
import io.meeds.dapp.model.DeedTenantOfferDTO;
import io.meeds.dapp.model.OfferFilter;
import io.meeds.dapp.service.OfferService;
import io.meeds.dapp.storage.OfferRepository;
import io.meeds.dapp.utils.DeedTenantOfferMapper;
import io.meeds.deeds.constant.BlockchainOfferStatus;
import io.meeds.deeds.constant.DeedCard;
import io.meeds.deeds.constant.DeedCity;
import io.meeds.deeds.constant.ObjectAlreadyExistsException;
import io.meeds.deeds.constant.ObjectNotFoundException;
import io.meeds.deeds.constant.TenantProvisioningStatus;
import io.meeds.deeds.constant.TransactionStatus;
import io.meeds.deeds.constant.UnauthorizedOperationException;
import io.meeds.deeds.elasticsearch.model.DeedTenant;
import io.meeds.deeds.model.DeedOfferBlockchainState;
import io.meeds.deeds.service.BlockchainService;
import io.meeds.deeds.service.ListenerService;
import io.meeds.deeds.service.TenantService;

@SpringBootTest(classes = {
    OfferService.class,
})
class OfferServiceTest {

  private static final String     ADDRESS = "address";

  private static final String     EMAIL   = "email";

  @MockBean
  private OfferRepository         offerRepository;

  @MockBean
  private ElasticsearchOperations elasticsearchOperations;

  @MockBean
  private BlockchainService       blockchainService;

  @MockBean
  private TenantService           tenantService;

  @MockBean
  private ListenerService         listenerService;

  @Autowired
  private OfferService            offerService;

  @Test
  @SuppressWarnings({
      "unchecked"
  })
  void testGetOffersListByNftId() {
    long nftId = 2l;
    String offerId = "offerId";
    DeedTenantOffer offer = newOffer(offerId, nftId);

    Pageable pageable = mock(Pageable.class);
    SearchHit<DeedTenantOffer> searchHit = mock(SearchHit.class);
    when(searchHit.getContent()).thenReturn(offer);

    SearchHits<DeedTenantOffer> searchHits = mock(SearchHits.class);
    when(searchHits.getSearchHits()).thenReturn(Collections.singletonList(searchHit));
    when(searchHits.getTotalHits()).thenReturn(1l);

    assertElasticSearchQuery(searchHits, "nftId");

    OfferFilter offerFilter = new OfferFilter();
    offerFilter.setNftId(nftId);
    Page<DeedTenantOfferDTO> result = offerService.getOffers(offerFilter, pageable);
    assertNotNull(result);
    assertEquals(1, result.getSize());
    assertEquals(1, result.getTotalElements());
    assertEquals(DeedTenantOfferMapper.toDTO(offer), result.get().findFirst().orElse(null));
  }

  @Test
  @SuppressWarnings({
      "unchecked"
  })
  void testGetOffersListWithInvalidNetworkId() {
    long nftId = 2l;
    String offerId = "offerId";
    DeedTenantOffer offer = newOffer(offerId, nftId);

    Pageable pageable = mock(Pageable.class);
    SearchHit<DeedTenantOffer> searchHit = mock(SearchHit.class);
    when(searchHit.getContent()).thenReturn(offer);

    SearchHits<DeedTenantOffer> searchHits = mock(SearchHits.class);
    when(searchHits.getSearchHits()).thenReturn(Collections.singletonList(searchHit));
    when(searchHits.getTotalHits()).thenReturn(1l);
    when(tenantService.isBlockchainNetworkValid(2l)).thenReturn(true);

    assertElasticSearchQuery(searchHits, "nftId");

    OfferFilter offerFilter = new OfferFilter();
    offerFilter.setNftId(nftId);
    offerFilter.setNetworkId(5l);
    Page<DeedTenantOfferDTO> result = offerService.getOffers(offerFilter, pageable);
    assertNotNull(result);
    assertEquals(0, result.getSize());

    offerFilter.setNetworkId(2l);
    result = offerService.getOffers(offerFilter, pageable);
    assertNotNull(result);
    assertEquals(1, result.getSize());
    assertEquals(1, result.getTotalElements());
    assertEquals(DeedTenantOfferMapper.toDTO(offer), result.get().findFirst().orElse(null));
  }

  @Test
  @SuppressWarnings({
      "unchecked"
  })
  void testGetOffersListByOwned() {
    long nftId = 2l;
    String offerId = "offerId";
    String ownerAddress = "ownerAddress";
    DeedTenantOffer offer = newOffer(offerId, nftId);

    Pageable pageable = mock(Pageable.class);
    SearchHit<DeedTenantOffer> searchHit = mock(SearchHit.class);
    when(searchHit.getContent()).thenReturn(offer);

    SearchHits<DeedTenantOffer> searchHits = mock(SearchHits.class);
    when(searchHits.getSearchHits()).thenReturn(Collections.singletonList(searchHit));
    when(searchHits.getTotalHits()).thenReturn(1l);

    assertElasticSearchQuery(searchHits, "owner");

    OfferFilter offerFilter = new OfferFilter();
    offerFilter.setOwnerAddress(ownerAddress);

    Page<DeedTenantOfferDTO> result = offerService.getOffers(offerFilter, pageable);
    assertNotNull(result);
    assertEquals(1, result.getSize());
    assertEquals(1, result.getTotalElements());
    assertEquals(DeedTenantOfferMapper.toDTO(offer), result.get().findFirst().orElse(null));
  }

  @Test
  @SuppressWarnings({
      "unchecked"
  })
  void testGetOffersListByCardTypes() {
    long nftId = 2l;
    String offerId = "offerId";
    List<DeedCard> cardTypes = Arrays.asList(DeedCard.COMMON, DeedCard.RARE);
    DeedTenantOffer offer = newOffer(offerId, nftId);

    Pageable pageable = mock(Pageable.class);
    SearchHit<DeedTenantOffer> searchHit = mock(SearchHit.class);
    when(searchHit.getContent()).thenReturn(offer);

    SearchHits<DeedTenantOffer> searchHits = mock(SearchHits.class);
    when(searchHits.getSearchHits()).thenReturn(Collections.singletonList(searchHit));
    when(searchHits.getTotalHits()).thenReturn(1l);

    assertElasticSearchQuery(searchHits, "cardType");

    OfferFilter offerFilter = new OfferFilter();
    offerFilter.setCardTypes(cardTypes);

    Page<DeedTenantOfferDTO> result = offerService.getOffers(offerFilter, pageable);
    assertNotNull(result);
    assertEquals(1, result.getSize());
    assertEquals(1, result.getTotalElements());
    assertEquals(DeedTenantOfferMapper.toDTO(offer), result.get().findFirst().orElse(null));
  }

  @Test
  @SuppressWarnings({
      "unchecked"
  })
  void testGetOffersListWithCurrentAddress() {
    long nftId = 2l;
    String offerId = "offerId";
    DeedTenantOffer offer = newOffer(offerId, nftId);

    Pageable pageable = mock(Pageable.class);
    SearchHit<DeedTenantOffer> searchHit = mock(SearchHit.class);
    when(searchHit.getContent()).thenReturn(offer);

    SearchHits<DeedTenantOffer> searchHits = mock(SearchHits.class);
    when(searchHits.getSearchHits()).thenReturn(Collections.singletonList(searchHit));
    when(searchHits.getTotalHits()).thenReturn(1l);

    String currentAddress = "address";
    assertElasticSearchQuery(searchHits, "viewAddresses");

    OfferFilter offerFilter = new OfferFilter();

    offerFilter.setCurrentAddress(currentAddress);
    Page<DeedTenantOfferDTO> result = offerService.getOffers(offerFilter, pageable);
    assertNotNull(result);
    assertEquals(1, result.getSize());
    assertEquals(1, result.getTotalElements());
    assertEquals(DeedTenantOfferMapper.toDTO(offer), result.get().findFirst().orElse(null));
  }

  @Test
  @SuppressWarnings({
      "unchecked"
  })
  void testGetOffersListByOfferTypes() {
    long nftId = 2l;
    String offerId = "offerId";
    List<OfferType> offerTypes = Arrays.asList(OfferType.RENTING, OfferType.SALE);
    DeedTenantOffer offer = newOffer(offerId, nftId);

    Pageable pageable = mock(Pageable.class);
    SearchHit<DeedTenantOffer> searchHit = mock(SearchHit.class);
    when(searchHit.getContent()).thenReturn(offer);

    SearchHits<DeedTenantOffer> searchHits = mock(SearchHits.class);
    when(searchHits.getSearchHits()).thenReturn(Collections.singletonList(searchHit));
    when(searchHits.getTotalHits()).thenReturn(1l);

    assertElasticSearchQuery(searchHits, "offerType");

    OfferFilter offerFilter = new OfferFilter();
    offerFilter.setOfferTypes(offerTypes);

    Page<DeedTenantOfferDTO> result = offerService.getOffers(offerFilter, pageable);
    assertNotNull(result);
    assertEquals(1, result.getSize());
    assertEquals(1, result.getTotalElements());
    assertEquals(DeedTenantOfferMapper.toDTO(offer), result.get().findFirst().orElse(null));
  }

  @Test
  @SuppressWarnings({
      "unchecked"
  })
  void testGetOffersListByEnabled() {
    long nftId = 2l;
    String offerId = "offerId";
    DeedTenantOffer offer = newOffer(offerId, nftId);

    Pageable pageable = mock(Pageable.class);
    SearchHit<DeedTenantOffer> searchHit = mock(SearchHit.class);
    when(searchHit.getContent()).thenReturn(offer);

    SearchHits<DeedTenantOffer> searchHits = mock(SearchHits.class);
    when(searchHits.getSearchHits()).thenReturn(Collections.singletonList(searchHit));
    when(searchHits.getTotalHits()).thenReturn(1l);

    assertElasticSearchQuery(searchHits, "enabled");

    OfferFilter offerFilter = new OfferFilter();
    offerFilter.setExcludeDisabled(true);

    Page<DeedTenantOfferDTO> result = offerService.getOffers(offerFilter, pageable);
    assertNotNull(result);
    assertEquals(1, result.getSize());
    assertEquals(1, result.getTotalElements());
    assertEquals(DeedTenantOfferMapper.toDTO(offer), result.get().findFirst().orElse(null));
  }

  @Test
  @SuppressWarnings({
      "unchecked"
  })
  void testGetOffersListByDateExpiration() {
    long nftId = 2l;
    String offerId = "offerId";
    DeedTenantOffer offer = newOffer(offerId, nftId);

    Pageable pageable = mock(Pageable.class);
    SearchHit<DeedTenantOffer> searchHit = mock(SearchHit.class);
    when(searchHit.getContent()).thenReturn(offer);

    SearchHits<DeedTenantOffer> searchHits = mock(SearchHits.class);
    when(searchHits.getSearchHits()).thenReturn(Collections.singletonList(searchHit));
    when(searchHits.getTotalHits()).thenReturn(1l);

    assertElasticSearchQuery(searchHits, "expirationDate");

    OfferFilter offerFilter = new OfferFilter();
    offerFilter.setExcludeExpired(true);

    Page<DeedTenantOfferDTO> result = offerService.getOffers(offerFilter, pageable);
    assertNotNull(result);
    assertEquals(1, result.getSize());
    assertEquals(1, result.getTotalElements());
    assertEquals(DeedTenantOfferMapper.toDTO(offer), result.get().findFirst().orElse(null));
  }

  @Test
  void testGetOffer() throws Exception {
    String offerId = "offerId";
    DeedTenantOffer offer = new DeedTenantOffer();
    offer.setId(offerId);
    offer.setViewAddresses(Collections.singletonList(DeedTenantOfferMapper.EVERYONE));
    offer.setEnabled(true);

    when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));
    DeedTenantOfferDTO deedTenantOfferDTO = offerService.getOffer(offerId, ADDRESS, false);
    assertNotNull(deedTenantOfferDTO);
    assertEquals(offerId, deedTenantOfferDTO.getId());
  }

  @Test
  void testCreateRentingOfferBySimpleUser() {
    assertThrows(UnauthorizedOperationException.class,
                 () -> offerService.createRentingOffer(ADDRESS, EMAIL, new DeedTenantOfferDTO()));
  }

  @Test
  void testCreateRentingOfferByOwnerButNotManager() {
    long nftId = 2l;
    String walletAddress = ADDRESS;
    DeedTenantOfferDTO deedTenantOfferDTO = new DeedTenantOfferDTO();
    deedTenantOfferDTO.setNftId(nftId);

    when(tenantService.isDeedOwner(walletAddress, nftId)).thenReturn(true);
    assertThrows(IllegalStateException.class,
                 () -> offerService.createRentingOffer(walletAddress, EMAIL, deedTenantOfferDTO));
  }

  @Test
  void testCreateRentingOfferByOwnerWhenNftNotExists() throws Exception {
    long nftId = 2l;
    String walletAddress = ADDRESS;
    DeedTenantOfferDTO deedTenantOfferDTO = new DeedTenantOfferDTO();
    deedTenantOfferDTO.setNftId(nftId);
    deedTenantOfferDTO.setOfferTransactionHash("transactionHash");

    when(tenantService.isDeedOwner(walletAddress, nftId)).thenReturn(true);
    when(tenantService.isDeedManager(walletAddress, nftId)).thenReturn(true);
    assertThrows(ObjectNotFoundException.class,
                 () -> offerService.createRentingOffer(walletAddress, EMAIL, deedTenantOfferDTO));
  }

  @Test
  void testCreateRentingOfferByOwnerWhenTenantIsUsed() throws Exception {
    long nftId = 2l;
    String walletAddress = ADDRESS;
    DeedTenantOfferDTO deedTenantOfferDTO = new DeedTenantOfferDTO();
    deedTenantOfferDTO.setNftId(nftId);
    DeedTenant deedTenant = new DeedTenant();
    deedTenant.setNftId(nftId);
    deedTenant.setTenantProvisioningStatus(TenantProvisioningStatus.START_IN_PROGRESS);

    when(tenantService.isDeedOwner(walletAddress, nftId)).thenReturn(true);
    when(tenantService.isDeedManager(walletAddress, nftId)).thenReturn(true);
    when(tenantService.getDeedTenantOrImport(walletAddress, nftId)).thenReturn(deedTenant);
    assertThrows(IllegalStateException.class,
                 () -> offerService.createRentingOffer(walletAddress, EMAIL, deedTenantOfferDTO));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testCreateRentingOfferByOwnerWhenTenantNotUsed() throws Exception {
    long nftId = 2l;
    String offerId = "offerId";
    String walletAddress = ADDRESS;
    DeedTenantOfferDTO deedTenantOfferDTO = new DeedTenantOfferDTO();
    deedTenantOfferDTO.setNftId(nftId);
    deedTenantOfferDTO.setCardType(DeedCard.LEGENDARY);
    deedTenantOfferDTO.setCity(DeedCity.ASHTARTE);
    deedTenantOfferDTO.setOwner("otherWallet");
    deedTenantOfferDTO.setDescription("description");
    deedTenantOfferDTO.setAmount(12);
    deedTenantOfferDTO.setOfferType(OfferType.SALE);
    deedTenantOfferDTO.setExpirationDuration(ExpirationDuration.ONE_MONTH);
    deedTenantOfferDTO.setDuration(RentalDuration.SIX_MONTHS);
    deedTenantOfferDTO.setPaymentPeriodicity(RentalPaymentPeriodicity.ONE_MONTH);
    deedTenantOfferDTO.setOwnerMintingPercentage(50);
    deedTenantOfferDTO.setMintingPower(50);
    deedTenantOfferDTO.setExpirationDate(Instant.now().plus(500, ChronoUnit.DAYS));
    deedTenantOfferDTO.setCreatedDate(Instant.now().plus(500, ChronoUnit.DAYS));
    deedTenantOfferDTO.setModifiedDate(Instant.now().plus(700, ChronoUnit.DAYS));
    deedTenantOfferDTO.setEnabled(false);
    deedTenantOfferDTO.setOfferTransactionHash("transactionHash");

    DeedTenant deedTenant = new DeedTenant();
    deedTenant.setNftId(nftId);
    deedTenant.setCardType((short) DeedCard.UNCOMMON.ordinal());
    deedTenant.setCityIndex((short) DeedCity.MELQART.ordinal());

    when(tenantService.isDeedOwner(walletAddress, nftId)).thenReturn(true);
    when(tenantService.isDeedManager(walletAddress, nftId)).thenReturn(true);
    when(tenantService.getDeedTenantOrImport(walletAddress, nftId)).thenReturn(deedTenant);
    when(offerRepository.save(any())).thenAnswer(invocation -> {
      DeedTenantOffer offer = invocation.getArgument(0, DeedTenantOffer.class);
      offer.setId(offerId);
      return offer;
    });

    SearchHits<DeedTenantOffer> searchHits = mock(SearchHits.class);
    when(searchHits.getSearchHits()).thenReturn(Collections.emptyList());
    when(searchHits.getTotalHits()).thenReturn(0l);
    when(elasticsearchOperations.search(any(Query.class), eq(DeedTenantOffer.class))).thenReturn(searchHits);

    DeedTenantOfferDTO createdRentingOffer = offerService.createRentingOffer(walletAddress, EMAIL, deedTenantOfferDTO);
    assertNotNull(createdRentingOffer);
    assertEquals(offerId, createdRentingOffer.getId());
    assertEquals(OfferType.RENTING, createdRentingOffer.getOfferType());
    assertEquals(DeedCity.MELQART, createdRentingOffer.getCity());
    assertEquals(DeedCard.UNCOMMON, createdRentingOffer.getCardType());
    assertEquals(DeedCard.UNCOMMON.getMintingPower(), createdRentingOffer.getMintingPower());
    assertNotNull(createdRentingOffer.getCreatedDate());
    assertEquals(createdRentingOffer.getCreatedDate(), createdRentingOffer.getModifiedDate());
    assertTrue(createdRentingOffer.isEnabled());
    assertEquals(walletAddress, createdRentingOffer.getOwner());
    assertEquals(deedTenantOfferDTO.getExpirationDuration(), createdRentingOffer.getExpirationDuration());
    assertEquals(deedTenantOfferDTO.getDuration(), createdRentingOffer.getDuration());
    assertEquals(deedTenantOfferDTO.getOwnerMintingPercentage(), createdRentingOffer.getOwnerMintingPercentage());
    assertEquals(deedTenantOfferDTO.getPaymentPeriodicity(), createdRentingOffer.getPaymentPeriodicity());
    assertEquals(deedTenantOfferDTO.getAmount(), createdRentingOffer.getAmount());
    assertNotEquals(deedTenantOfferDTO.getExpirationDate(), createdRentingOffer.getExpirationDate());
    assertNotEquals(deedTenantOfferDTO.getCreatedDate(), createdRentingOffer.getCreatedDate());
    assertNotEquals(deedTenantOfferDTO.getModifiedDate(), createdRentingOffer.getModifiedDate());
  }

  @Test
  void testUpdateRentingOfferBySimpleUser() {
    assertThrows(UnauthorizedOperationException.class,
                 () -> offerService.updateRentingOffer(ADDRESS, new DeedTenantOfferDTO()));
  }

  @Test
  void testUpdateRentingOfferByOwnerWhenNftNotExists() throws Exception {
    long nftId = 2l;
    String walletAddress = ADDRESS;
    DeedTenantOfferDTO deedTenantOfferDTO = new DeedTenantOfferDTO();
    deedTenantOfferDTO.setNftId(nftId);
    deedTenantOfferDTO.setOfferTransactionHash("transactionHash");
    deedTenantOfferDTO.setEnabled(true);

    when(tenantService.isDeedOwner(walletAddress, nftId)).thenReturn(true);
    when(tenantService.isDeedManager(walletAddress, nftId)).thenReturn(true);
    assertThrows(ObjectNotFoundException.class,
                 () -> offerService.updateRentingOffer(walletAddress, deedTenantOfferDTO));
  }

  @Test
  void testUpdateRentingOfferByOwnerWhenTenantNotUsed() throws Exception {
    long nftId = 2l;
    String offerId = "offerId";
    String walletAddress = ADDRESS;
    DeedTenantOfferDTO deedTenantOfferDTO = new DeedTenantOfferDTO();
    deedTenantOfferDTO.setId(offerId);
    deedTenantOfferDTO.setNftId(nftId);
    deedTenantOfferDTO.setCardType(DeedCard.LEGENDARY);
    deedTenantOfferDTO.setCity(DeedCity.ASHTARTE);
    deedTenantOfferDTO.setOwner("otherWallet");
    deedTenantOfferDTO.setDescription("description updated");
    deedTenantOfferDTO.setAmount(15);
    deedTenantOfferDTO.setOfferType(OfferType.SALE);
    deedTenantOfferDTO.setExpirationDuration(ExpirationDuration.ONE_WEEK);
    deedTenantOfferDTO.setNoticePeriod(NoticePeriod.ONE_MONTH);
    deedTenantOfferDTO.setDuration(RentalDuration.ONE_YEAR);
    deedTenantOfferDTO.setPaymentPeriodicity(RentalPaymentPeriodicity.ONE_YEAR);
    deedTenantOfferDTO.setOwnerMintingPercentage(85);
    deedTenantOfferDTO.setMintingPower(50);
    deedTenantOfferDTO.setExpirationDate(Instant.now().plus(500, ChronoUnit.DAYS));
    deedTenantOfferDTO.setCreatedDate(Instant.now().plus(500, ChronoUnit.DAYS));
    deedTenantOfferDTO.setModifiedDate(Instant.now().plus(700, ChronoUnit.DAYS));
    deedTenantOfferDTO.setEnabled(false);
    deedTenantOfferDTO.setOfferTransactionHash("TransactionHash");

    when(tenantService.isDeedOwner(walletAddress, nftId)).thenReturn(true);
    when(tenantService.isDeedManager(walletAddress, nftId)).thenReturn(true);

    DeedTenantOffer existingDeedTenantOffer = newOffer(offerId, nftId);
    when(offerRepository.findById(offerId)).thenReturn(Optional.of(existingDeedTenantOffer));

    assertThrows(UnauthorizedOperationException.class,
                 () -> offerService.updateRentingOffer(walletAddress, deedTenantOfferDTO));
    deedTenantOfferDTO.setEnabled(true);

    when(offerRepository.save(any())).thenAnswer(invocation -> {
      DeedTenantOffer offer = invocation.getArgument(0, DeedTenantOffer.class);
      offer.setId(offerId);
      return offer;
    });
    DeedTenantOfferDTO updatedRentingOffer = offerService.updateRentingOffer(walletAddress, deedTenantOfferDTO);
    assertNotNull(updatedRentingOffer);
    assertNotNull(updatedRentingOffer.getUpdateId());
    assertEquals(OfferType.RENTING, updatedRentingOffer.getOfferType());
    assertEquals(existingDeedTenantOffer.getCity(), updatedRentingOffer.getCity());
    assertEquals(existingDeedTenantOffer.getCardType(), updatedRentingOffer.getCardType());
    assertEquals(existingDeedTenantOffer.getCardType().getMintingPower(), updatedRentingOffer.getMintingPower());
    assertTrue(updatedRentingOffer.isEnabled());
    assertEquals(existingDeedTenantOffer.getOwner(), updatedRentingOffer.getOwner());
    assertEquals(existingDeedTenantOffer.getExpirationDays(), updatedRentingOffer.getExpirationDuration().getDays());
    assertEquals(existingDeedTenantOffer.getMonths(), updatedRentingOffer.getDuration().getMonths());
    assertEquals(existingDeedTenantOffer.getOwnerMintingPercentage(), updatedRentingOffer.getOwnerMintingPercentage());
    assertEquals(existingDeedTenantOffer.getPaymentPeriodicity(), updatedRentingOffer.getPaymentPeriodicity());
    assertEquals(existingDeedTenantOffer.getAmount(), updatedRentingOffer.getAmount());
    assertEquals(existingDeedTenantOffer.getAllDurationAmount(), updatedRentingOffer.getAllDurationAmount());
    assertEquals(existingDeedTenantOffer.getNoticePeriod(), updatedRentingOffer.getNoticePeriod().getMonths());
  }

  @Test
  void testUpdateRentingOfferByOwnerWhenNeverExpires() throws Exception {
    long nftId = 2l;
    String offerId = "offerId";
    String walletAddress = ADDRESS;
    DeedTenantOfferDTO deedTenantOfferDTO = new DeedTenantOfferDTO();
    deedTenantOfferDTO.setId(offerId);
    deedTenantOfferDTO.setNftId(nftId);
    deedTenantOfferDTO.setCardType(DeedCard.LEGENDARY);
    deedTenantOfferDTO.setCity(DeedCity.ASHTARTE);
    deedTenantOfferDTO.setOwner("otherWallet");
    deedTenantOfferDTO.setDescription("description updated");
    deedTenantOfferDTO.setAmount(15);
    deedTenantOfferDTO.setOfferType(OfferType.SALE);
    deedTenantOfferDTO.setDuration(RentalDuration.ONE_YEAR);
    deedTenantOfferDTO.setPaymentPeriodicity(RentalPaymentPeriodicity.ONE_YEAR);
    deedTenantOfferDTO.setOwnerMintingPercentage(85);
    deedTenantOfferDTO.setMintingPower(50);
    deedTenantOfferDTO.setExpirationDate(Instant.now().plus(500, ChronoUnit.DAYS));
    deedTenantOfferDTO.setCreatedDate(Instant.now().plus(500, ChronoUnit.DAYS));
    deedTenantOfferDTO.setModifiedDate(Instant.now().plus(700, ChronoUnit.DAYS));
    deedTenantOfferDTO.setEnabled(true);
    deedTenantOfferDTO.setOfferTransactionHash("transactionHash");

    when(tenantService.isDeedOwner(walletAddress, nftId)).thenReturn(true);
    when(tenantService.isDeedManager(walletAddress, nftId)).thenReturn(true);
    when(offerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0, DeedTenantOffer.class));

    DeedTenantOffer existingDeedTenantOffer = new DeedTenantOffer();
    existingDeedTenantOffer.setId(offerId);
    existingDeedTenantOffer.setNftId(nftId);
    existingDeedTenantOffer.setCardType(DeedCard.UNCOMMON);
    existingDeedTenantOffer.setCity(DeedCity.MELQART);
    existingDeedTenantOffer.setOwner("otherWallet");
    existingDeedTenantOffer.setDescription("description");
    existingDeedTenantOffer.setAmount(12);
    existingDeedTenantOffer.setOfferType(OfferType.RENTING);
    existingDeedTenantOffer.setExpirationDays(30);
    existingDeedTenantOffer.setMonths(6);
    existingDeedTenantOffer.setPaymentPeriodicity(RentalPaymentPeriodicity.ONE_MONTH);
    existingDeedTenantOffer.setOwnerMintingPercentage(50);
    existingDeedTenantOffer.setMintingPower(DeedCard.UNCOMMON.getMintingPower());
    existingDeedTenantOffer.setExpirationDate(Instant.now().plus(5, ChronoUnit.DAYS));
    existingDeedTenantOffer.setCreatedDate(Instant.now());
    existingDeedTenantOffer.setModifiedDate(Instant.now());
    existingDeedTenantOffer.setEnabled(true);
    existingDeedTenantOffer.setOfferTransactionHash("TransactionHash");
    when(offerRepository.findById(offerId)).thenReturn(Optional.of(existingDeedTenantOffer));

    DeedTenantOfferDTO updatedRentingOffer = offerService.updateRentingOffer(walletAddress, deedTenantOfferDTO);
    assertNotNull(updatedRentingOffer);
    assertEquals(OfferType.RENTING, updatedRentingOffer.getOfferType());
    assertEquals(existingDeedTenantOffer.getCity(), updatedRentingOffer.getCity());
    assertEquals(existingDeedTenantOffer.getCardType(), updatedRentingOffer.getCardType());
    assertEquals(existingDeedTenantOffer.getCardType().getMintingPower(), updatedRentingOffer.getMintingPower());
    assertEquals(existingDeedTenantOffer.getOwner(), updatedRentingOffer.getOwner());
    assertEquals(existingDeedTenantOffer.getMonths(), updatedRentingOffer.getDuration().getPeriod().getMonths());
    assertEquals(existingDeedTenantOffer.getOwnerMintingPercentage(), updatedRentingOffer.getOwnerMintingPercentage());
    assertEquals(existingDeedTenantOffer.getPaymentPeriodicity(), updatedRentingOffer.getPaymentPeriodicity());
    assertEquals(existingDeedTenantOffer.getAmount(), updatedRentingOffer.getAmount());
  }

  @Test
  void testDeleteRentingOfferWhenNotExists() throws Exception {
    String offerId = "offerId";
    String walletAddress = ADDRESS;
    assertThrows(ObjectNotFoundException.class,
                 () -> offerService.deleteRentingOffer(walletAddress, offerId, ""));
  }

  @Test
  void testDeleteRentingOfferWhenExistsAndNotOwner() throws Exception {
    long nftId = 2l;
    String offerId = "offerId";
    String walletAddress = ADDRESS;
    DeedTenantOffer offer = new DeedTenantOffer();
    offer.setNftId(nftId);

    lenient().when(tenantService.isDeedManager(walletAddress, nftId)).thenReturn(true);
    when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));
    assertThrows(UnauthorizedOperationException.class,
                 () -> offerService.deleteRentingOffer(walletAddress, offerId, null),
                 "even Deed Manager shouldn't be able to delete Offer");
  }

  @Test
  void testDeleteRentingOfferWhenExistsAndOwner() throws Exception {
    long nftId = 2l;
    String offerId = "offerId";
    String walletAddress = ADDRESS;
    DeedTenantOffer offer = new DeedTenantOffer();
    offer.setNftId(nftId);
    offer.setEnabled(true);
    offer.setOfferTransactionHash("TransactionHash");

    when(tenantService.isDeedOwner(walletAddress, nftId)).thenReturn(true);
    when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));
    when(offerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    assertThrows(IllegalStateException.class, () -> offerService.deleteRentingOffer(walletAddress, offerId, null));

    offerService.deleteRentingOffer(walletAddress, offerId, "transactionHash");
    verify(offerRepository, never()).deleteById(any());
    verify(offerRepository, atLeast(1)).save(any());
  }

  @Test
  void testCancelRentingOfferWhenExists() throws Exception {
    long nftId = 2l;
    String walletAddress = ADDRESS;
    DeedTenantOffer offer = new DeedTenantOffer();
    offer.setNftId(nftId);
    offer.setOfferTransactionHash("TransactionHash");

    List<DeedTenantOffer> offers = Collections.singletonList(offer);
    when(offerRepository.findByOwnerNotAndNftIdAndEnabledTrue(walletAddress, nftId)).thenReturn(offers);
    when(offerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    offerService.cancelOffers(walletAddress, nftId);
    verify(offerRepository, never()).deleteById(any());
    verify(offerRepository, times(1)).save(any());
  }

  @Test
  void testGetPendingTransactions() throws Exception {
    DeedTenantOffer offer = newOffer("1", 5);
    when(offerRepository.findByOfferTransactionStatusInOrderByCreatedDateAsc(Arrays.asList(TransactionStatus.IN_PROGRESS)))
                                                                                                                           .thenReturn(Collections.singletonList(offer));

    List<DeedTenantOfferDTO> pendingTransactions = offerService.getPendingTransactions();
    assertNotNull(pendingTransactions);
    assertEquals(1, pendingTransactions.size());

    DeedTenantOfferDTO deedTenantOfferDTO = pendingTransactions.get(0);
    assertEquals(offer.getId(), deedTenantOfferDTO.getId());
    assertEquals(offer.getNftId(), deedTenantOfferDTO.getNftId());
  }

  @Test
  void testUpdateRentingOfferStatusFromBlockchain() throws Exception {
    String id = "offerId";
    assertThrows(IllegalArgumentException.class, // NOSONAR
                 () -> offerService.updateRentingOfferStatusFromBlockchain(id,
                                                                           Collections.singletonMap(BlockchainOfferStatus.OFFER_ACQUIRED,
                                                                                                    mock(DeedOfferBlockchainState.class))));

    DeedTenantOffer offer = newOffer(id, 5);
    when(offerRepository.findById(id)).thenReturn(Optional.of(offer));
    offer.setOfferId(10);

    when(blockchainService.isOfferEnabled(10)).thenReturn(true);
    offerService.updateRentingOfferStatusFromBlockchain(id, Collections.emptyMap());
    verify(offerRepository, never()).delete(any());

    when(blockchainService.isOfferEnabled(10)).thenReturn(false);
    offerService.updateRentingOfferStatusFromBlockchain(id, Collections.emptyMap());
    verify(offerRepository, times(1)).delete(any());

  }

  @Test
  void testMarkOfferAcquisitionInProgress() throws Exception {
    long nftId = 2l;
    long blockchainOfferId = 6l;
    String transactionHash = "transactionHash";
    Instant validStartDate = Instant.now();

    offerService.markOfferAcquisitionInProgress(nftId, null, validStartDate);
    verify(offerRepository, never()).save(any());

    DeedTenantOffer anotherDeedTenant = newOffer("anotherId", nftId);
    when(offerRepository.findByOfferTransactionHash(StringUtils.lowerCase(transactionHash))).thenReturn(anotherDeedTenant);

    assertThrows(ObjectAlreadyExistsException.class,
                 () -> offerService.markOfferAcquisitionInProgress(nftId, transactionHash, validStartDate));

    DeedTenantOffer acquiredOffer = newOffer("acquiredOfferId", nftId);
    when(offerRepository.findByOfferIdAndParentIdIsNull(blockchainOfferId)).thenReturn(Collections.singletonList(acquiredOffer));

    String anotherTransactionHash = "transactionHash2";
    offerService.markOfferAcquisitionInProgress(nftId, anotherTransactionHash, validStartDate);

    verify(offerRepository, never()).save(any());

    String offerId = "offerId";
    DeedTenantOffer existingDeedTenant = newOffer(offerId, nftId);
    existingDeedTenant.setStartDate(validStartDate.minusSeconds(5));

    List<DeedTenantOffer> offers = Collections.singletonList(existingDeedTenant);
    when(offerRepository.findByNftId(nftId)).thenReturn(offers);

    existingDeedTenant.setAcquired(true);
    offerService.markOfferAcquisitionInProgress(nftId, anotherTransactionHash, validStartDate);
    verify(offerRepository, never()).save(any());
    existingDeedTenant.setAcquired(false);

    existingDeedTenant.setEnabled(false);
    offerService.markOfferAcquisitionInProgress(nftId, anotherTransactionHash, validStartDate);
    verify(offerRepository, never()).save(any());
    existingDeedTenant.setEnabled(true);

    existingDeedTenant.setParentId("parentOfferId");
    offerService.markOfferAcquisitionInProgress(nftId, anotherTransactionHash, validStartDate);
    verify(offerRepository, never()).save(any());
    existingDeedTenant.setParentId(null);

    existingDeedTenant.setOfferTransactionStatus(TransactionStatus.ERROR);
    offerService.markOfferAcquisitionInProgress(nftId, anotherTransactionHash, validStartDate);
    verify(offerRepository, never()).save(any());
    existingDeedTenant.setOfferTransactionStatus(TransactionStatus.VALIDATED);

    existingDeedTenant.setStartDate(validStartDate);
    offerService.markOfferAcquisitionInProgress(nftId, anotherTransactionHash, validStartDate);
    verify(offerRepository, never()).save(any());
    existingDeedTenant.setStartDate(validStartDate.minusSeconds(5));

    mockOfferSaving();

    offerService.markOfferAcquisitionInProgress(nftId, anotherTransactionHash, validStartDate);
    verify(offerRepository, atLeast(1)).save(argThat(new ArgumentMatcher<DeedTenantOffer>() {
      public boolean matches(DeedTenantOffer offer) {
        return StringUtils.equals(offer.getId(), existingDeedTenant.getId()) && offer.getAcquisitionIds().size() == 1;
      }
    }));
    verify(offerRepository, atLeast(1)).save(argThat(new ArgumentMatcher<DeedTenantOffer>() {
      public boolean matches(DeedTenantOffer offer) {
        return !StringUtils.equals(offer.getId(), existingDeedTenant.getId())
            && StringUtils.equals(offer.getParentId(), existingDeedTenant.getId());
      }
    }));

    verify(listenerService, times(1)).publishEvent(OFFER_ACQUISITION_PROGRESS_EVENT, existingDeedTenant);
  }

  @Test
  void testGetOfferWithBlockchainRefresh() throws Exception {
    long nftId = 2l;
    long blockchainOfferId = 6l;
    String offerId = "offerId";
    String walletAddress = "walletAddress";
    String authorizedAddress = "authorizedAddress";

    assertThrows(ObjectNotFoundException.class, () -> offerService.getOffer(offerId, null, false));

    DeedTenantOffer offer = newOffer(offerId, nftId);
    offer.setOwner(walletAddress);
    offer.setHostAddress(authorizedAddress);
    when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));

    offer.setEnabled(false);
    assertThrows(UnauthorizedOperationException.class, () -> offerService.getOffer(offerId, null, false));
    offer.setEnabled(true);

    DeedTenantOfferDTO offerDTO = offerService.getOffer(offerId, null, false);
    assertNotNull(offerDTO);
    assertEquals(offer.getId(), offerDTO.getId());

    offer.setOfferId(0);// offer not created yet

    offer.setOfferTransactionStatus(TransactionStatus.VALIDATED);
    offerDTO = offerService.getOffer(offerId, walletAddress, true);
    assertNotNull(offerDTO);
    assertEquals(offer.getId(), offerDTO.getId());
    verify(offerRepository, never()).save(any());

    offerService.releaseExplicitOfferRefreshLock(offerId);
    offerService.releaseExplicitOfferRefreshLock(offerId);// Shouldn't throw any
                                                          // exception when
                                                          // called twice
    offer.setOfferTransactionStatus(TransactionStatus.IN_PROGRESS);
    offerDTO = offerService.getOffer(offerId, walletAddress, true);
    assertNotNull(offerDTO);
    assertEquals(offer.getId(), offerDTO.getId());
    verify(offerRepository, never()).save(any());

    BigInteger price = BigInteger.valueOf(12);
    BigInteger allDurationPrice = BigInteger.valueOf(7);
    DeedOfferBlockchainState blockchainState = newOfferBlockchainState(nftId,
                                                                       blockchainOfferId,
                                                                       walletAddress,
                                                                       authorizedAddress,
                                                                       offer.getOfferTransactionHash(),
                                                                       price,
                                                                       allDurationPrice);
    when(blockchainService.getOfferTransactionEvents(offer.getOfferTransactionHash())).thenReturn(Collections.singletonMap(BlockchainOfferStatus.OFFER_CREATED,
                                                                                                                           blockchainState));

    mockOfferSaving();

    offerService.releaseExplicitOfferRefreshLock(offerId);
    when(blockchainService.isOfferEnabled(blockchainOfferId)).thenReturn(true);
    offerDTO = offerService.getOffer(offerId, walletAddress, true);
    assertNotNull(offerDTO);
    assertEquals(offer.getId(), offerDTO.getId());

    verify(offerRepository, times(1)).save(argThat(new ArgumentMatcher<DeedTenantOffer>() {
      @Override
      public boolean matches(DeedTenantOffer persistedOffer) {
        assertEquals(blockchainState.getId().longValue(), persistedOffer.getOfferId());
        assertEquals(blockchainState.getDeedId().longValue(), persistedOffer.getNftId());
        assertEquals(blockchainState.getBlockNumber().longValue(), persistedOffer.getLastCheckedBlock());
        assertEquals(StringUtils.lowerCase(blockchainState.getCreator()), persistedOffer.getOwner());
        assertEquals(blockchainState.getMonths().longValue(), persistedOffer.getMonths());
        assertEquals(blockchainState.getNoticePeriod().longValue(), persistedOffer.getNoticePeriod());
        assertEquals(price.doubleValue(), persistedOffer.getAmount());
        assertEquals(allDurationPrice.doubleValue(), persistedOffer.getAllDurationAmount());
        assertEquals(blockchainState.getOfferStartDate().longValue(), persistedOffer.getStartDate().toEpochMilli() / 1000);
        assertEquals(blockchainState.getOfferExpirationDate().longValue(),
                     persistedOffer.getExpirationDate().toEpochMilli() / 1000);
        assertEquals(blockchainState.getOfferExpirationDays().longValue(), persistedOffer.getExpirationDays());
        assertEquals(StringUtils.lowerCase(blockchainState.getAuthorizedTenant()), persistedOffer.getHostAddress());
        return true;
      }
    }));
  }

  @Test
  void testGetOfferByBlockchainId() throws Exception {
    long nftId = 2l;
    long blockchainOfferId = 6l;
    String offerId = "offerId";
    String walletAddress = "walletAddress";
    String authorizedAddress = "authorizedAddress";
    String transactionHash = "transactionHash";
    BigInteger price = BigInteger.valueOf(12);
    BigInteger allDurationPrice = BigInteger.valueOf(7);

    assertNull(offerService.getOfferByBlockchainId(blockchainOfferId));

    DeedOfferBlockchainState blockchainState = newOfferBlockchainState(nftId,
                                                                       blockchainOfferId,
                                                                       walletAddress,
                                                                       authorizedAddress,
                                                                       transactionHash,
                                                                       price,
                                                                       allDurationPrice);
    when(blockchainService.getOfferById(BigInteger.valueOf(blockchainOfferId), null, null)).thenReturn(blockchainState);

    DeedTenant deedTenant = new DeedTenant();
    deedTenant.setNftId(nftId);
    when(tenantService.getDeedTenantOrImport(blockchainState.getCreator(), nftId)).thenReturn(deedTenant);
    mockOfferSaving();

    DeedTenantOfferDTO offerByBlockchainId = offerService.getOfferByBlockchainId(blockchainOfferId);

    verify(offerRepository, times(1)).save(argThat(new ArgumentMatcher<DeedTenantOffer>() {
      @Override
      public boolean matches(DeedTenantOffer persistedOffer) {
        assertEquals(blockchainState.getId().longValue(), persistedOffer.getOfferId());
        assertEquals(blockchainState.getDeedId().longValue(), persistedOffer.getNftId());
        assertEquals(blockchainState.getBlockNumber().longValue(), persistedOffer.getLastCheckedBlock());
        assertEquals(StringUtils.lowerCase(blockchainState.getCreator()), persistedOffer.getOwner());
        assertEquals(blockchainState.getMonths().longValue(), persistedOffer.getMonths());
        assertEquals(blockchainState.getNoticePeriod().longValue(), persistedOffer.getNoticePeriod());
        assertEquals(price.doubleValue(), persistedOffer.getAmount());
        assertEquals(allDurationPrice.doubleValue(), persistedOffer.getAllDurationAmount());
        assertEquals(blockchainState.getOfferStartDate().longValue(), persistedOffer.getStartDate().toEpochMilli() / 1000);
        assertEquals(blockchainState.getOfferExpirationDate().longValue(),
                     persistedOffer.getExpirationDate().toEpochMilli() / 1000);
        assertEquals(blockchainState.getOfferExpirationDays().longValue(), persistedOffer.getExpirationDays());
        assertEquals(StringUtils.lowerCase(blockchainState.getAuthorizedTenant()), persistedOffer.getHostAddress());
        return true;
      }
    }));

    DeedTenantOffer offer = newOffer(offerId, nftId);
    List<DeedTenantOffer> offers = Arrays.asList(offer,
                                                 newOffer("otherOfferId", nftId));
    when(offerRepository.findByOfferIdAndParentIdIsNull(blockchainOfferId)).thenReturn(offers);

    offerByBlockchainId = offerService.getOfferByBlockchainId(blockchainOfferId);
    assertNotNull(offerByBlockchainId);
    assertEquals(offer.getId(), offerByBlockchainId.getId());
  }

  @Test
  void testUpdateOfferFromBlockchain() throws Exception {
    long nftId = 2l;
    long blockchainOfferId = 6l;
    String offerId = "offerId";
    String walletAddress = "walletAddress";
    String authorizedAddress = "authorizedAddress";
    String transactionHash = "transactionHash";
    BigInteger price = BigInteger.valueOf(12);
    BigInteger allDurationPrice = BigInteger.valueOf(7);

    mockOfferSaving();

    DeedOfferBlockchainState blockchainState = newOfferBlockchainState(nftId,
                                                                       blockchainOfferId,
                                                                       walletAddress,
                                                                       authorizedAddress,
                                                                       transactionHash,
                                                                       price,
                                                                       allDurationPrice);
    DeedTenant deedTenant = new DeedTenant();
    deedTenant.setNftId(nftId);
    when(tenantService.getDeedTenantOrImport(blockchainState.getCreator(), nftId)).thenReturn(deedTenant);

    DeedTenantOffer existingOffer = newOffer(offerId, nftId);
    when(offerRepository.findByOfferIdAndParentIdIsNull(blockchainOfferId)).thenReturn(Collections.singletonList(existingOffer));

    DeedTenantOffer createdOffer = offerService.updateOfferFromBlockchain(blockchainState, true);
    assertNotNull(createdOffer);

    verify(offerRepository, times(1)).save(argThat(new ArgumentMatcher<DeedTenantOffer>() {
      @Override
      public boolean matches(DeedTenantOffer persistedOffer) {
        assertEquals(blockchainState.getId().longValue(), persistedOffer.getOfferId());
        assertEquals(blockchainState.getDeedId().longValue(), persistedOffer.getNftId());
        assertEquals(blockchainState.getBlockNumber().longValue(), persistedOffer.getLastCheckedBlock());
        assertEquals(StringUtils.lowerCase(blockchainState.getCreator()), persistedOffer.getOwner());
        assertEquals(blockchainState.getMonths().longValue(), persistedOffer.getMonths());
        assertEquals(blockchainState.getNoticePeriod().longValue(), persistedOffer.getNoticePeriod());
        assertEquals(price.doubleValue(), persistedOffer.getAmount());
        assertEquals(allDurationPrice.doubleValue(), persistedOffer.getAllDurationAmount());
        assertEquals(blockchainState.getOfferStartDate().longValue(), persistedOffer.getStartDate().toEpochMilli() / 1000);
        assertEquals(blockchainState.getOfferExpirationDate().longValue(),
                     persistedOffer.getExpirationDate().toEpochMilli() / 1000);
        assertEquals(blockchainState.getOfferExpirationDays().longValue(), persistedOffer.getExpirationDays());
        assertEquals(StringUtils.lowerCase(blockchainState.getAuthorizedTenant()), persistedOffer.getHostAddress());
        assertEquals(StringUtils.lowerCase(transactionHash), StringUtils.lowerCase(persistedOffer.getOfferTransactionHash()));
        return true;
      }
    }));
  }

  @Test
  void testMarkOfferAsAcquired() throws Exception {
    long nftId = 2l;
    long blockchainOfferId = 6l;
    String offerId = "offerId";
    String parentOfferId = "parentOfferId";
    String walletAddress = "walletAddress";
    String authorizedAddress = "authorizedAddress";
    Instant leaseEndDate = Instant.now();

    mockOfferSaving();

    DeedTenantOffer parentOffer = newOffer(parentOfferId, nftId);
    parentOffer.setOwner(walletAddress);
    parentOffer.setHostAddress(authorizedAddress);
    parentOffer.setAcquisitionIds(Collections.singleton(offerId));
    parentOffer.setOfferId(blockchainOfferId);

    DeedTenantOffer offer = newOffer(offerId, nftId);
    offer.setOwner(walletAddress);
    offer.setHostAddress(authorizedAddress);
    offer.setParentId(parentOfferId);
    offer.setOfferId(blockchainOfferId);

    when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));
    when(offerRepository.findById(parentOfferId)).thenReturn(Optional.of(parentOffer));

    DeedTenantOffer existingOffer = newOffer("existingOfferId", nftId);
    existingOffer.setStartDate(leaseEndDate.minusSeconds(5));

    List<DeedTenantOffer> offers = Collections.singletonList(existingOffer);
    when(offerRepository.findByNftId(nftId)).thenReturn(offers);

    when(offerRepository.findByOfferIdAndParentIdIsNull(blockchainOfferId)).thenReturn(Collections.singletonList(parentOffer));

    offer.setAcquired(false);
    parentOffer.setAcquired(false);
    offerService.markOfferAsAcquired(offer.getOfferId(), leaseEndDate);

    assertTrue(parentOffer.isAcquired());
    verify(offerRepository, atLeast(1)).save(parentOffer);
    verify(offerRepository, times(1)).deleteByParentId(parentOfferId);

    verify(listenerService, times(1)).publishEvent(OFFER_CANCELED_EVENT, existingOffer);
  }

  private void mockOfferSaving() {
    when(offerRepository.save(any())).thenAnswer(invocation -> {
      DeedTenantOffer offerToPersist = invocation.getArgument(0, DeedTenantOffer.class);
      if (StringUtils.isBlank(offerToPersist.getId())) {
        offerToPersist.setId(String.valueOf(Math.random()));
        when(offerRepository.findById(offerToPersist.getId())).thenReturn(Optional.of(offerToPersist));
      }
      return offerToPersist;
    });
  }

  private DeedOfferBlockchainState newOfferBlockchainState(long nftId, long blockchainOfferId, String walletAddress,
                                                           String authorizedAddress, String transactionHash, BigInteger price,
                                                           BigInteger allDurationPrice) {
    return new DeedOfferBlockchainState(BigInteger.valueOf(blockchainOfferId),
                                        BigInteger.valueOf(3),
                                        BigInteger.valueOf(nftId),
                                        walletAddress,
                                        BigInteger.valueOf(4),
                                        BigInteger.valueOf(5),
                                        price.multiply(BigInteger.valueOf(10).pow(18)),
                                        allDurationPrice.multiply(BigInteger.valueOf(10)
                                                                            .pow(18)),
                                        BigInteger.valueOf(8),
                                        BigInteger.valueOf(9),
                                        BigInteger.valueOf(10),
                                        authorizedAddress,
                                        BigInteger.valueOf(11),
                                        transactionHash);
  }

  private DeedTenantOffer newOffer(String offerId, long nftId) {
    DeedTenantOffer offer = new DeedTenantOffer();
    offer.setId(offerId);
    offer.setNftId(nftId);
    offer.setCardType(DeedCard.UNCOMMON);
    offer.setCity(DeedCity.MELQART);
    offer.setOwner("otherWallet");
    offer.setDescription("description");
    offer.setAmount(12);
    offer.setOfferType(OfferType.RENTING);
    offer.setExpirationDays(30);
    offer.setMonths(6);
    offer.setPaymentPeriodicity(RentalPaymentPeriodicity.ONE_MONTH);
    offer.setOwnerMintingPercentage(50);
    offer.setMintingPower(DeedCard.UNCOMMON.getMintingPower());
    offer.setExpirationDate(Instant.now().plus(5, ChronoUnit.DAYS));
    offer.setCreatedDate(Instant.now());
    offer.setModifiedDate(Instant.now());
    offer.setOfferTransactionHash("TransactionHash");
    offer.setEnabled(true);
    lenient().when(offerRepository.findById(offer.getId())).thenReturn(Optional.of(offer));
    return offer;
  }

  private void assertElasticSearchQuery(SearchHits<DeedTenantOffer> searchHits, String filedName) {
    when(elasticsearchOperations.search(argThat(new ArgumentMatcher<Query>() {
      @Override
      public boolean matches(Query query) {
        assertQueryCriteriaNotNull(query, filedName);
        return true;
      }
    }), eq(DeedTenantOffer.class))).thenReturn(searchHits);
  }

  private void assertQueryCriteriaNotNull(Query query, String filedName) {
    CriteriaQuery criteriaQuery = (CriteriaQuery) query;
    Criteria criteria = criteriaQuery.getCriteria();
    List<Criteria> criteriaChain = criteria.getCriteriaChain();
    assertNotNull(criteriaChain);
    for (Criteria subCriteria : criteriaChain) {
      assertNotNull(subCriteria);
      if (StringUtils.equals(filedName, subCriteria.getField().getName())) {
        return;
      }
    }
    fail("Field not found");
  }
}
