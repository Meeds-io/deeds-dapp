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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

import io.meeds.deeds.constant.DeedCard;
import io.meeds.deeds.constant.DeedCity;
import io.meeds.deeds.constant.ExpirationDuration;
import io.meeds.deeds.constant.ObjectNotFoundException;
import io.meeds.deeds.constant.OfferType;
import io.meeds.deeds.constant.RentalDuration;
import io.meeds.deeds.constant.RentalPaymentPeriodicity;
import io.meeds.deeds.constant.TenantProvisioningStatus;
import io.meeds.deeds.constant.UnauthorizedOperationException;
import io.meeds.deeds.model.DeedTenant;
import io.meeds.deeds.model.DeedTenantOffer;
import io.meeds.deeds.model.DeedTenantOfferDTO;
import io.meeds.deeds.model.DeedTenantOfferFilter;
import io.meeds.deeds.storage.DeedTenantOfferRepository;
import io.meeds.deeds.utils.Mapper;

@SpringBootTest(classes = {
    DeedTenantOfferService.class,
})
class DeedTenantOfferServiceTest {

  @MockBean
  private DeedTenantOfferRepository deedTenantOfferRepository;

  @MockBean
  private ElasticsearchOperations   elasticsearchOperations;

  @MockBean
  private TenantService             tenantService;

  @MockBean
  private ListenerService           listenerService;

  @Autowired
  private DeedTenantOfferService    deedTenantOfferService;

  @Test
  @SuppressWarnings({
      "unchecked"
  })
  void testGetOffersListByNftId() {
    long nftId = 2l;
    DeedTenantOffer deedTenantOffer = newDeedTenant(nftId);

    Pageable pageable = mock(Pageable.class);
    SearchHit<DeedTenantOffer> searchHit = mock(SearchHit.class);
    when(searchHit.getContent()).thenReturn(deedTenantOffer);

    SearchHits<DeedTenantOffer> searchHits = mock(SearchHits.class);
    when(searchHits.getSearchHits()).thenReturn(Collections.singletonList(searchHit));
    when(searchHits.getTotalHits()).thenReturn(1l);

    assertElasticSearchQuery(searchHits, "nftId", 2l);

    DeedTenantOfferFilter offerFilter = new DeedTenantOfferFilter();
    offerFilter.setNftId(nftId);
    Page<DeedTenantOfferDTO> result = deedTenantOfferService.getOffersList(offerFilter, pageable);
    assertNotNull(result);
    assertEquals(1, result.getSize());
    assertEquals(1, result.getTotalElements());
    assertEquals(Mapper.toDTO(deedTenantOffer), result.get().findFirst().orElse(null));
  }

  @Test
  @SuppressWarnings({
      "unchecked"
  })
  void testGetOffersListByOwned() {
    long nftId = 2l;
    String ownerAddress = "ownerAddress";
    DeedTenantOffer deedTenantOffer = newDeedTenant(nftId);

    Pageable pageable = mock(Pageable.class);
    SearchHit<DeedTenantOffer> searchHit = mock(SearchHit.class);
    when(searchHit.getContent()).thenReturn(deedTenantOffer);

    SearchHits<DeedTenantOffer> searchHits = mock(SearchHits.class);
    when(searchHits.getSearchHits()).thenReturn(Collections.singletonList(searchHit));
    when(searchHits.getTotalHits()).thenReturn(1l);

    assertElasticSearchQuery(searchHits, "owner", ownerAddress.toLowerCase());

    DeedTenantOfferFilter offerFilter = new DeedTenantOfferFilter();
    offerFilter.setOwnerAddress(ownerAddress);

    Page<DeedTenantOfferDTO> result = deedTenantOfferService.getOffersList(offerFilter, pageable);
    assertNotNull(result);
    assertEquals(1, result.getSize());
    assertEquals(1, result.getTotalElements());
    assertEquals(Mapper.toDTO(deedTenantOffer), result.get().findFirst().orElse(null));
  }

  @Test
  @SuppressWarnings({
      "unchecked"
  })
  void testGetOffersListByCardTypes() {
    long nftId = 2l;
    List<DeedCard> cardTypes = Arrays.asList(DeedCard.COMMON, DeedCard.RARE);
    DeedTenantOffer deedTenantOffer = newDeedTenant(nftId);

    Pageable pageable = mock(Pageable.class);
    SearchHit<DeedTenantOffer> searchHit = mock(SearchHit.class);
    when(searchHit.getContent()).thenReturn(deedTenantOffer);

    SearchHits<DeedTenantOffer> searchHits = mock(SearchHits.class);
    when(searchHits.getSearchHits()).thenReturn(Collections.singletonList(searchHit));
    when(searchHits.getTotalHits()).thenReturn(1l);

    assertElasticSearchQuery(searchHits, "cardType", cardTypes);

    DeedTenantOfferFilter offerFilter = new DeedTenantOfferFilter();
    offerFilter.setCardTypes(cardTypes);

    Page<DeedTenantOfferDTO> result = deedTenantOfferService.getOffersList(offerFilter, pageable);
    assertNotNull(result);
    assertEquals(1, result.getSize());
    assertEquals(1, result.getTotalElements());
    assertEquals(Mapper.toDTO(deedTenantOffer), result.get().findFirst().orElse(null));
  }

  @Test
  @SuppressWarnings({
      "unchecked"
  })
  void testGetOffersListByOfferTypes() {
    long nftId = 2l;
    List<OfferType> offerTypes = Arrays.asList(OfferType.RENTING, OfferType.SALE);
    DeedTenantOffer deedTenantOffer = newDeedTenant(nftId);

    Pageable pageable = mock(Pageable.class);
    SearchHit<DeedTenantOffer> searchHit = mock(SearchHit.class);
    when(searchHit.getContent()).thenReturn(deedTenantOffer);

    SearchHits<DeedTenantOffer> searchHits = mock(SearchHits.class);
    when(searchHits.getSearchHits()).thenReturn(Collections.singletonList(searchHit));
    when(searchHits.getTotalHits()).thenReturn(1l);

    assertElasticSearchQuery(searchHits, "offerType", offerTypes);

    DeedTenantOfferFilter offerFilter = new DeedTenantOfferFilter();
    offerFilter.setOfferTypes(offerTypes);

    Page<DeedTenantOfferDTO> result = deedTenantOfferService.getOffersList(offerFilter, pageable);
    assertNotNull(result);
    assertEquals(1, result.getSize());
    assertEquals(1, result.getTotalElements());
    assertEquals(Mapper.toDTO(deedTenantOffer), result.get().findFirst().orElse(null));
  }

  @Test
  @SuppressWarnings({
      "unchecked"
  })
  void testGetOffersListByEnabled() {
    long nftId = 2l;
    boolean enabled = true;
    DeedTenantOffer deedTenantOffer = newDeedTenant(nftId);

    Pageable pageable = mock(Pageable.class);
    SearchHit<DeedTenantOffer> searchHit = mock(SearchHit.class);
    when(searchHit.getContent()).thenReturn(deedTenantOffer);

    SearchHits<DeedTenantOffer> searchHits = mock(SearchHits.class);
    when(searchHits.getSearchHits()).thenReturn(Collections.singletonList(searchHit));
    when(searchHits.getTotalHits()).thenReturn(1l);

    assertElasticSearchQuery(searchHits, "enabled", enabled);

    DeedTenantOfferFilter offerFilter = new DeedTenantOfferFilter();
    offerFilter.setExcludeDisabled(true);

    Page<DeedTenantOfferDTO> result = deedTenantOfferService.getOffersList(offerFilter, pageable);
    assertNotNull(result);
    assertEquals(1, result.getSize());
    assertEquals(1, result.getTotalElements());
    assertEquals(Mapper.toDTO(deedTenantOffer), result.get().findFirst().orElse(null));
  }

  @Test
  void testCreateRentingOfferBySimpleUser() {
    assertThrows(UnauthorizedOperationException.class,
                 () -> deedTenantOfferService.createRentingOffer("address", new DeedTenantOfferDTO()));
  }

  @Test
  void testCreateRentingOfferByOwnerButNotManager() {
    long nftId = 2l;
    String walletAddress = "address";
    DeedTenantOfferDTO deedTenantOfferDTO = new DeedTenantOfferDTO();
    deedTenantOfferDTO.setNftId(nftId);

    when(tenantService.isDeedOwner(walletAddress, nftId)).thenReturn(true);
    assertThrows(IllegalStateException.class,
                 () -> deedTenantOfferService.createRentingOffer(walletAddress, deedTenantOfferDTO));
  }

  @Test
  void testCreateRentingOfferByOwnerWhenNftNotExists() throws Exception {
    long nftId = 2l;
    String walletAddress = "address";
    DeedTenantOfferDTO deedTenantOfferDTO = new DeedTenantOfferDTO();
    deedTenantOfferDTO.setNftId(nftId);

    when(tenantService.isDeedOwner(walletAddress, nftId)).thenReturn(true);
    when(tenantService.isDeedManager(walletAddress, nftId)).thenReturn(true);
    assertThrows(ObjectNotFoundException.class,
                 () -> deedTenantOfferService.createRentingOffer(walletAddress, deedTenantOfferDTO));
  }

  @Test
  void testCreateRentingOfferByOwnerWhenTenantIsUsed() throws Exception {
    long nftId = 2l;
    String walletAddress = "address";
    DeedTenantOfferDTO deedTenantOfferDTO = new DeedTenantOfferDTO();
    deedTenantOfferDTO.setNftId(nftId);
    DeedTenant deedTenant = new DeedTenant();
    deedTenant.setNftId(nftId);
    deedTenant.setTenantProvisioningStatus(TenantProvisioningStatus.START_IN_PROGRESS);

    when(tenantService.isDeedOwner(walletAddress, nftId)).thenReturn(true);
    when(tenantService.isDeedManager(walletAddress, nftId)).thenReturn(true);
    when(tenantService.getDeedTenantOrImport(walletAddress, nftId)).thenReturn(deedTenant);
    assertThrows(IllegalStateException.class, () -> deedTenantOfferService.createRentingOffer(walletAddress, deedTenantOfferDTO));
  }

  @Test
  void testCreateRentingOfferByOwnerWhenTenantNotUsed() throws Exception {
    long nftId = 2l;
    String walletAddress = "address";
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
    DeedTenant deedTenant = new DeedTenant();
    deedTenant.setNftId(nftId);
    deedTenant.setCardType((short) DeedCard.UNCOMMON.ordinal());
    deedTenant.setCityIndex((short) DeedCity.MELQART.ordinal());

    when(tenantService.isDeedOwner(walletAddress, nftId)).thenReturn(true);
    when(tenantService.isDeedManager(walletAddress, nftId)).thenReturn(true);
    when(tenantService.getDeedTenantOrImport(walletAddress, nftId)).thenReturn(deedTenant);
    when(deedTenantOfferRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0, DeedTenantOffer.class));

    DeedTenantOfferDTO createdRentingOffer = deedTenantOfferService.createRentingOffer(walletAddress, deedTenantOfferDTO);
    assertNotNull(createdRentingOffer);
    assertEquals(nftId,
                 createdRentingOffer.getId(),
                 "Knowing that one single offer is allowed for now for a Deed Tenant, the id must be the same as NFT Identifier");
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
                 () -> deedTenantOfferService.updateRentingOffer("address", new DeedTenantOfferDTO()));
  }

  @Test
  void testUpdateRentingOfferByOwnerButNotManager() {
    long nftId = 2l;
    String walletAddress = "address";
    DeedTenantOfferDTO deedTenantOfferDTO = new DeedTenantOfferDTO();
    deedTenantOfferDTO.setNftId(nftId);

    when(tenantService.isDeedOwner(walletAddress, nftId)).thenReturn(true);
    assertThrows(IllegalStateException.class,
                 () -> deedTenantOfferService.updateRentingOffer(walletAddress, deedTenantOfferDTO));
  }

  @Test
  void testUpdateRentingOfferByOwnerWhenNftNotExists() throws Exception {
    long nftId = 2l;
    String walletAddress = "address";
    DeedTenantOfferDTO deedTenantOfferDTO = new DeedTenantOfferDTO();
    deedTenantOfferDTO.setNftId(nftId);

    when(tenantService.isDeedOwner(walletAddress, nftId)).thenReturn(true);
    when(tenantService.isDeedManager(walletAddress, nftId)).thenReturn(true);
    assertThrows(ObjectNotFoundException.class,
                 () -> deedTenantOfferService.updateRentingOffer(walletAddress, deedTenantOfferDTO));
  }

  @Test
  void testUpdateRentingOfferByOwnerWhenTenantNotUsed() throws Exception {
    long nftId = 2l;
    String walletAddress = "address";
    DeedTenantOfferDTO deedTenantOfferDTO = new DeedTenantOfferDTO();
    deedTenantOfferDTO.setId(nftId);
    deedTenantOfferDTO.setNftId(nftId);
    deedTenantOfferDTO.setCardType(DeedCard.LEGENDARY);
    deedTenantOfferDTO.setCity(DeedCity.ASHTARTE);
    deedTenantOfferDTO.setOwner("otherWallet");
    deedTenantOfferDTO.setDescription("description updated");
    deedTenantOfferDTO.setAmount(15);
    deedTenantOfferDTO.setOfferType(OfferType.SALE);
    deedTenantOfferDTO.setExpirationDuration(ExpirationDuration.ONE_WEEK);
    deedTenantOfferDTO.setDuration(RentalDuration.ONE_YEAR);
    deedTenantOfferDTO.setPaymentPeriodicity(RentalPaymentPeriodicity.ONE_YEAR);
    deedTenantOfferDTO.setOwnerMintingPercentage(85);
    deedTenantOfferDTO.setMintingPower(50);
    deedTenantOfferDTO.setExpirationDate(Instant.now().plus(500, ChronoUnit.DAYS));
    deedTenantOfferDTO.setCreatedDate(Instant.now().plus(500, ChronoUnit.DAYS));
    deedTenantOfferDTO.setModifiedDate(Instant.now().plus(700, ChronoUnit.DAYS));
    deedTenantOfferDTO.setEnabled(false);

    when(tenantService.isDeedOwner(walletAddress, nftId)).thenReturn(true);
    when(tenantService.isDeedManager(walletAddress, nftId)).thenReturn(true);
    when(deedTenantOfferRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0, DeedTenantOffer.class));

    DeedTenantOffer existingDeedTenantOffer = newDeedTenant(nftId);
    when(deedTenantOfferRepository.findById(nftId)).thenReturn(Optional.of(existingDeedTenantOffer));

    DeedTenantOfferDTO updatedRentingOffer = deedTenantOfferService.updateRentingOffer(walletAddress, deedTenantOfferDTO);
    assertNotNull(updatedRentingOffer);
    assertEquals(nftId,
                 updatedRentingOffer.getId(),
                 "Knowing that one single offer is allowed for now for a Deed Tenant, the id must be the same as NFT Identifier");
    assertEquals(OfferType.RENTING, updatedRentingOffer.getOfferType());
    assertEquals(DeedCity.MELQART, updatedRentingOffer.getCity());
    assertEquals(DeedCard.UNCOMMON, updatedRentingOffer.getCardType());
    assertEquals(DeedCard.UNCOMMON.getMintingPower(), updatedRentingOffer.getMintingPower());
    assertNotNull(updatedRentingOffer.getCreatedDate());
    assertNotEquals(updatedRentingOffer.getCreatedDate(), updatedRentingOffer.getModifiedDate());
    assertTrue(updatedRentingOffer.isEnabled());
    assertEquals(walletAddress, updatedRentingOffer.getOwner());
    assertEquals(deedTenantOfferDTO.getExpirationDuration(), updatedRentingOffer.getExpirationDuration());
    assertEquals(deedTenantOfferDTO.getDuration(), updatedRentingOffer.getDuration());
    assertEquals(deedTenantOfferDTO.getOwnerMintingPercentage(), updatedRentingOffer.getOwnerMintingPercentage());
    assertEquals(deedTenantOfferDTO.getPaymentPeriodicity(), updatedRentingOffer.getPaymentPeriodicity());
    assertEquals(deedTenantOfferDTO.getAmount(), updatedRentingOffer.getAmount());
    assertNotEquals(deedTenantOfferDTO.getExpirationDate(), updatedRentingOffer.getExpirationDate());
    assertNotEquals(deedTenantOfferDTO.getCreatedDate(), updatedRentingOffer.getCreatedDate());
    assertNotEquals(deedTenantOfferDTO.getModifiedDate(), updatedRentingOffer.getModifiedDate());
  }

  @Test
  void testUpdateRentingOfferByOwnerWhenNeverExpires() throws Exception {
    long nftId = 2l;
    String walletAddress = "address";
    DeedTenantOfferDTO deedTenantOfferDTO = new DeedTenantOfferDTO();
    deedTenantOfferDTO.setId(nftId);
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
    deedTenantOfferDTO.setEnabled(false);

    when(tenantService.isDeedOwner(walletAddress, nftId)).thenReturn(true);
    when(tenantService.isDeedManager(walletAddress, nftId)).thenReturn(true);
    when(deedTenantOfferRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0, DeedTenantOffer.class));

    DeedTenantOffer existingDeedTenantOffer = new DeedTenantOffer();
    existingDeedTenantOffer.setId(nftId);
    existingDeedTenantOffer.setNftId(nftId);
    existingDeedTenantOffer.setCardType(DeedCard.UNCOMMON);
    existingDeedTenantOffer.setCity(DeedCity.MELQART);
    existingDeedTenantOffer.setOwner("otherWallet");
    existingDeedTenantOffer.setDescription("description");
    existingDeedTenantOffer.setAmount(12);
    existingDeedTenantOffer.setOfferType(OfferType.RENTING);
    existingDeedTenantOffer.setExpirationDuration(ExpirationDuration.ONE_MONTH);
    existingDeedTenantOffer.setDuration(RentalDuration.SIX_MONTHS);
    existingDeedTenantOffer.setPaymentPeriodicity(RentalPaymentPeriodicity.ONE_MONTH);
    existingDeedTenantOffer.setOwnerMintingPercentage(50);
    existingDeedTenantOffer.setMintingPower(DeedCard.UNCOMMON.getMintingPower());
    existingDeedTenantOffer.setExpirationDate(Instant.now().plus(5, ChronoUnit.DAYS));
    existingDeedTenantOffer.setCreatedDate(Instant.now());
    existingDeedTenantOffer.setModifiedDate(Instant.now());
    existingDeedTenantOffer.setEnabled(true);
    when(deedTenantOfferRepository.findById(nftId)).thenReturn(Optional.of(existingDeedTenantOffer));

    DeedTenantOfferDTO updatedRentingOffer = deedTenantOfferService.updateRentingOffer(walletAddress, deedTenantOfferDTO);
    assertNotNull(updatedRentingOffer);
    assertEquals(nftId,
                 updatedRentingOffer.getId(),
                 "Knowing that one single offer is allowed for now for a Deed Tenant, the id must be the same as NFT Identifier");
    assertEquals(OfferType.RENTING, updatedRentingOffer.getOfferType());
    assertEquals(DeedCity.MELQART, updatedRentingOffer.getCity());
    assertEquals(DeedCard.UNCOMMON, updatedRentingOffer.getCardType());
    assertEquals(DeedCard.UNCOMMON.getMintingPower(), updatedRentingOffer.getMintingPower());
    assertNotNull(updatedRentingOffer.getCreatedDate());
    assertNotEquals(updatedRentingOffer.getCreatedDate(), updatedRentingOffer.getModifiedDate());
    assertTrue(updatedRentingOffer.isEnabled());
    assertEquals(walletAddress, updatedRentingOffer.getOwner());
    assertEquals(deedTenantOfferDTO.getDuration(), updatedRentingOffer.getDuration());
    assertEquals(deedTenantOfferDTO.getOwnerMintingPercentage(), updatedRentingOffer.getOwnerMintingPercentage());
    assertEquals(deedTenantOfferDTO.getPaymentPeriodicity(), updatedRentingOffer.getPaymentPeriodicity());
    assertEquals(deedTenantOfferDTO.getAmount(), updatedRentingOffer.getAmount());
    assertNull(updatedRentingOffer.getExpirationDuration());
    assertNull(updatedRentingOffer.getExpirationDate());
    assertNotEquals(deedTenantOfferDTO.getCreatedDate(), updatedRentingOffer.getCreatedDate());
    assertNotEquals(deedTenantOfferDTO.getModifiedDate(), updatedRentingOffer.getModifiedDate());
  }

  @Test
  void testDeleteRentingOfferWhenNotExists() throws Exception {
    long nftId = 2l;
    String walletAddress = "address";
    assertThrows(ObjectNotFoundException.class,
                 () -> deedTenantOfferService.deleteRentingOffer(walletAddress, nftId));
  }

  @Test
  void testDeleteRentingOfferWhenExistsAndNotOwner() throws Exception {
    long nftId = 2l;
    String walletAddress = "address";
    DeedTenantOffer deedTenantOffer = new DeedTenantOffer();
    deedTenantOffer.setNftId(nftId);

    lenient().when(tenantService.isDeedManager(walletAddress, nftId)).thenReturn(true);
    when(deedTenantOfferRepository.findById(nftId)).thenReturn(Optional.of(deedTenantOffer));
    assertThrows(UnauthorizedOperationException.class,
                 () -> deedTenantOfferService.deleteRentingOffer(walletAddress, nftId),
                 "even Deed Manager shouldn't be able to delete Offer");
  }

  @Test
  void testDeleteRentingOfferWhenExistsAndOwner() throws Exception {
    long nftId = 2l;
    String walletAddress = "address";
    DeedTenantOffer deedTenantOffer = new DeedTenantOffer();
    deedTenantOffer.setNftId(nftId);

    when(tenantService.isDeedOwner(walletAddress, nftId)).thenReturn(true);
    when(deedTenantOfferRepository.findById(nftId)).thenReturn(Optional.of(deedTenantOffer));
    deedTenantOfferService.deleteRentingOffer(walletAddress, nftId);
    verify(deedTenantOfferRepository, times(1)).deleteById(nftId);
  }

  @Test
  void testCancelRentingOfferWhenExists() throws Exception {
    long nftId = 2l;
    String walletAddress = "address";
    DeedTenantOffer deedTenantOffer = new DeedTenantOffer();
    deedTenantOffer.setNftId(nftId);

    List<DeedTenantOffer> offers = Collections.singletonList(deedTenantOffer);
    when(deedTenantOfferRepository.findByOwnerNotAndNftIdAndEnabledTrue(walletAddress, nftId)).thenReturn(offers);

    deedTenantOfferService.cancelOffers(walletAddress, nftId);

    verify(deedTenantOfferRepository, times(1)).deleteAll(offers);
  }

  private DeedTenantOffer newDeedTenant(long nftId) {
    DeedTenantOffer deedTenantOffer = new DeedTenantOffer();
    deedTenantOffer.setId(nftId);
    deedTenantOffer.setNftId(nftId);
    deedTenantOffer.setCardType(DeedCard.UNCOMMON);
    deedTenantOffer.setCity(DeedCity.MELQART);
    deedTenantOffer.setOwner("otherWallet");
    deedTenantOffer.setDescription("description");
    deedTenantOffer.setAmount(12);
    deedTenantOffer.setOfferType(OfferType.RENTING);
    deedTenantOffer.setExpirationDuration(ExpirationDuration.ONE_MONTH);
    deedTenantOffer.setDuration(RentalDuration.SIX_MONTHS);
    deedTenantOffer.setPaymentPeriodicity(RentalPaymentPeriodicity.ONE_MONTH);
    deedTenantOffer.setOwnerMintingPercentage(50);
    deedTenantOffer.setMintingPower(DeedCard.UNCOMMON.getMintingPower());
    deedTenantOffer.setExpirationDate(Instant.now().plus(5, ChronoUnit.DAYS));
    deedTenantOffer.setCreatedDate(Instant.now());
    deedTenantOffer.setModifiedDate(Instant.now());
    deedTenantOffer.setEnabled(true);
    return deedTenantOffer;
  }

  private void assertElasticSearchQuery(SearchHits<DeedTenantOffer> searchHits, String filedName, Object fieldValue) {
    when(elasticsearchOperations.search(argThat(new ArgumentMatcher<Query>() {
      @Override
      public boolean matches(Query query) {
        assertQueryCriteriaIs(query, filedName, fieldValue);
        return true;
      }
    }), eq(DeedTenantOffer.class))).thenReturn(searchHits);
  }

  private void assertQueryCriteriaIs(Query query, String filedName, Object fieldValue) {
    CriteriaQuery criteriaQuery = (CriteriaQuery) query;
    Criteria criteria = criteriaQuery.getCriteria();
    List<Criteria> criteriaChain = criteria.getCriteriaChain();
    assertNotNull(criteriaChain);
    assertEquals(1, criteriaChain.size());
    Criteria nftIdCriteria = criteriaChain.get(0);
    assertNotNull(nftIdCriteria);
    assertEquals(filedName, nftIdCriteria.getField().getName());
    assertEquals(fieldValue, nftIdCriteria.getQueryCriteriaEntries().iterator().next().getValue());
  }
}
