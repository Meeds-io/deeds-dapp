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

import static io.meeds.dapp.service.LeaseService.LEASE_ACQUIRED_EVENT;
import static io.meeds.dapp.service.LeaseService.LEASE_END_EVENT;
import static io.meeds.dapp.service.LeaseService.LEASE_RENT_PAYED_EVENT;
import static io.meeds.dapp.service.LeaseService.LEASE_RENT_PAYMENT_CONFIRMED_EVENT;
import static io.meeds.dapp.service.LeaseService.LEASE_TENANT_EVICT_EVENT;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import java.math.BigInteger;
import java.time.Instant;
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
import io.meeds.dapp.elasticsearch.model.DeedTenantLease;
import io.meeds.dapp.model.DeedTenantLeaseDTO;
import io.meeds.dapp.model.DeedTenantOfferDTO;
import io.meeds.dapp.model.LeaseFilter;
import io.meeds.dapp.storage.LeaseRepository;
import io.meeds.dapp.utils.DeedTenantLeaseMapper;
import io.meeds.deeds.common.constant.BlockchainLeaseStatus;
import io.meeds.deeds.common.constant.DeedCard;
import io.meeds.deeds.common.constant.DeedCity;
import io.meeds.deeds.common.constant.TransactionStatus;
import io.meeds.deeds.common.constant.UnauthorizedOperationException;
import io.meeds.deeds.common.elasticsearch.model.DeedTenant;
import io.meeds.deeds.common.model.DeedLeaseBlockchainState;
import io.meeds.deeds.common.service.BlockchainService;
import io.meeds.deeds.common.service.ListenerService;
import io.meeds.deeds.common.service.TenantService;
import io.meeds.deeds.constant.ObjectNotFoundException;

@SpringBootTest(classes = {
    LeaseService.class,
})
class LeaseServiceTest {

  @MockBean
  private LeaseRepository         leaseRepository;

  @MockBean
  private ElasticsearchOperations elasticsearchOperations;

  @MockBean
  private BlockchainService       blockchainService;

  @MockBean
  private TenantService           tenantService;

  @MockBean
  private OfferService            offerService;

  @MockBean
  private ListenerService         listenerService;

  @Autowired
  private LeaseService            leaseService;

  @Test
  @SuppressWarnings({
      "unchecked"
  })
  void testGetLeasess() {
    long nftId = 2l;
    long leaseId = 3l;
    DeedTenantLease lease = newLease(leaseId, nftId);

    Pageable pageable = mock(Pageable.class);
    SearchHit<DeedTenantLease> searchHit = mock(SearchHit.class);
    when(searchHit.getContent()).thenReturn(lease);

    SearchHits<DeedTenantLease> searchHits = mock(SearchHits.class);
    when(searchHits.getSearchHits()).thenReturn(Collections.singletonList(searchHit));
    when(searchHits.getTotalHits()).thenReturn(1l);
    when(tenantService.isBlockchainNetworkValid(2l)).thenReturn(true);

    assertElasticSearchQuery(searchHits,
                             "nftId",
                             "enabled",
                             "endDate",
                             "viewAddresses",
                             "confirmed",
                             "cardType",
                             "transactionStatus",
                             "owner");

    LeaseFilter leaseFilter = new LeaseFilter();
    leaseFilter.setNftId(nftId);
    leaseFilter.setExcludeNotConfirmed(true);
    leaseFilter.setCardTypes(Collections.singletonList(DeedCard.COMMON));
    leaseFilter.setNetworkId(6l);
    leaseFilter.setTransactionStatus(Collections.singletonList(TransactionStatus.VALIDATED));
    leaseFilter.setOwner(true);
    Page<DeedTenantLeaseDTO> result = leaseService.getLeases(leaseFilter, pageable);
    assertNotNull(result);
    assertEquals(0, result.getSize());

    leaseFilter.setNetworkId(2l);
    result = leaseService.getLeases(leaseFilter, pageable);
    assertNotNull(result);
    assertEquals(1, result.getSize());
    assertEquals(1, result.getTotalElements());
    assertEquals(DeedTenantLeaseMapper.toDTO(lease, null, null), result.get().findFirst().orElse(null));
  }

  @Test
  void testGetLease() throws Exception {
    long nftId = 2l;
    long leaseId = 3l;

    assertThrows(ObjectNotFoundException.class, () -> leaseService.getLease(leaseId, null, false));

    DeedTenantLease lease = newLease(leaseId, nftId);
    when(leaseRepository.findById(nftId)).thenReturn(Optional.of(lease));

    DeedTenantLeaseDTO leaseDTO = leaseService.getLease(leaseId, null, false);
    assertNotNull(leaseDTO);
  }

  @Test
  void testGetLeaseWithBlockchainRefresh() throws Exception {
    long nftId = 2l;
    long leaseId = 3l;
    long lastBlockNumber = 12l;
    String tenant = "tenantAddress";
    String transactionHash = "transactionHash";

    DeedLeaseBlockchainState blockchainState = new DeedLeaseBlockchainState(BigInteger.valueOf(leaseId),
                                                                            BigInteger.valueOf(lastBlockNumber),
                                                                            BigInteger.valueOf(nftId),
                                                                            BigInteger.valueOf(1),
                                                                            BigInteger.valueOf(2),
                                                                            BigInteger.valueOf(3),
                                                                            BigInteger.valueOf(4),
                                                                            BigInteger.valueOf(5),
                                                                            tenant,
                                                                            transactionHash);
    when(blockchainService.getLeaseById(BigInteger.valueOf(leaseId),
                                        BigInteger.valueOf(lastBlockNumber),
                                        null)).thenReturn(blockchainState);

    when(blockchainService.getLastBlock()).thenReturn(lastBlockNumber);

    DeedTenantOfferDTO offer = newOffer("offerId", nftId, leaseId);
    when(offerService.getOfferByBlockchainId(leaseId)).thenReturn(offer);
    when(leaseRepository.save(any())).thenAnswer(invocation -> {
      DeedTenantLease lease = invocation.getArgument(0, DeedTenantLease.class);
      when(leaseRepository.findById(lease.getId())).thenReturn(Optional.of(lease));
      return lease;
    });

    DeedTenant deedTenant = new DeedTenant();
    deedTenant.setNftId(nftId);
    when(tenantService.getDeedTenantOrImport(tenant, nftId)).thenReturn(deedTenant);
    when(tenantService.getDeedTenantOrImport(argThat(new ArgumentMatcher<String>() {
      @Override
      public boolean matches(String argument) {
        return !StringUtils.equalsIgnoreCase(tenant, argument);
      }
    }), eq(nftId))).thenThrow(UnauthorizedOperationException.class);

    assertThrows(ObjectNotFoundException.class, () -> leaseService.getLease(leaseId, null, false));

    when(tenantService.saveDeedTenant(any())).thenAnswer(invocation -> invocation.getArgument(0));

    DeedTenantLeaseDTO leaseDTO = leaseService.getLease(leaseId, null, true);
    assertNotNull(leaseDTO);

    verify(tenantService, times(1)).saveDeedTenant(argThat(new ArgumentMatcher<DeedTenant>() {
      @Override
      public boolean matches(DeedTenant deedTenant) {
        return StringUtils.isNotBlank(deedTenant.getOwnerAddress());
      }
    }));
  }

  @Test
  void testCreateLease() throws Exception {
    long nftId = 2l;
    long leaseId = 3l;
    String tenant = "tenantAddress";
    String transactionHash = "transactionHash";
    DeedTenantOfferDTO offer = newOffer("offerId", nftId, leaseId);

    assertThrows(ObjectNotFoundException.class, () -> leaseService.createLease(tenant, null, offer.getId(), transactionHash));

    when(offerService.getOffer(offer.getId())).thenReturn(offer);
    assertThrows(IllegalArgumentException.class, () -> leaseService.createLease(null, null, offer.getId(), transactionHash));// NOSONAR

    when(offerService.getOffer(offer.getId())).thenReturn(offer);
    assertThrows(IllegalArgumentException.class, () -> leaseService.createLease(tenant, null, offer.getId(), null));// NOSONAR

    assertThrows(UnauthorizedOperationException.class,
                 () -> leaseService.createLease(tenant, null, offer.getId(), transactionHash));

    when(tenantService.isDeedOwner(offer.getOwner(), nftId)).thenReturn(true);
    DeedTenant deedTenant = new DeedTenant();
    deedTenant.setNftId(nftId);
    when(tenantService.saveDeedTenant(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(tenantService.getDeedTenantOrImport(tenant, nftId)).thenReturn(deedTenant);
    when(tenantService.getDeedTenantOrImport(argThat(new ArgumentMatcher<String>() {
      @Override
      public boolean matches(String argument) {
        return !StringUtils.equalsIgnoreCase(tenant, argument);
      }
    }), eq(nftId))).thenThrow(UnauthorizedOperationException.class);
    when(leaseRepository.save(any())).thenAnswer(invocation -> {
      DeedTenantLease lease = invocation.getArgument(0, DeedTenantLease.class);
      when(leaseRepository.findById(lease.getId())).thenReturn(Optional.of(lease));
      return lease;
    });

    DeedTenantLeaseDTO createdLease = leaseService.createLease(tenant, null, offer.getId(), transactionHash);
    assertNotNull(createdLease);

    verify(tenantService, times(1)).saveDeedTenant(argThat(new ArgumentMatcher<DeedTenant>() {
      @Override
      public boolean matches(DeedTenant deedTenant) {
        return StringUtils.isNotBlank(deedTenant.getOwnerAddress());
      }
    }));

    verify(listenerService, times(1)).publishEvent(eq(LEASE_ACQUIRED_EVENT), any());
  }

  @Test
  void testPayRents() throws Exception {
    long nftId = 2l;
    long leaseId = 3l;
    String tenant = "tenantAddress";
    String owner = "ownerAddress";
    String transactionHash = "transactionHash";

    assertThrows(ObjectNotFoundException.class, () -> leaseService.payRents(tenant, owner, leaseId, 5, transactionHash));

    DeedTenantLease lease = newLease(leaseId, nftId);
    lease.setManager(tenant);
    when(leaseRepository.findById(lease.getId())).thenReturn(Optional.of(lease));
    assertThrows(UnauthorizedOperationException.class,
                 () -> leaseService.payRents("otherTenant", owner, leaseId, 5, transactionHash));
    assertThrows(IllegalArgumentException.class, () -> leaseService.payRents(tenant, owner, leaseId, 5, null));

    when(leaseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(tenantService.isDeedOwner(owner, lease.getNftId())).thenReturn(true);
    DeedTenantLeaseDTO changedLease = leaseService.payRents(tenant, owner, leaseId, 5, transactionHash);
    assertNotNull(changedLease);
    assertEquals(5, changedLease.getMonthPaymentInProgress());
    assertEquals(TransactionStatus.IN_PROGRESS, changedLease.getTransactionStatus());
    assertEquals(owner, changedLease.getOwnerAddress());

    verify(listenerService, times(1)).publishEvent(eq(LEASE_RENT_PAYED_EVENT), any());
  }

  @Test
  void testEndLeaseByOwner() throws Exception {
    long nftId = 2l;
    long leaseId = 3l;
    String tenant = "tenantAddress";
    String owner = "ownerAddress";
    String transactionHash = "transactionHash";

    assertThrows(ObjectNotFoundException.class, () -> leaseService.endLease(tenant, leaseId, transactionHash));

    DeedTenantLease lease = newLease(leaseId, nftId);
    lease.setManager(tenant);
    when(leaseRepository.findById(lease.getId())).thenReturn(Optional.of(lease));
    assertThrows(UnauthorizedOperationException.class, () -> leaseService.endLease(tenant, leaseId, transactionHash));
    assertThrows(UnauthorizedOperationException.class, () -> leaseService.endLease(owner, leaseId, transactionHash));

    when(tenantService.isDeedManager(tenant, nftId)).thenReturn(true);
    assertThrows(UnauthorizedOperationException.class, () -> leaseService.endLease(owner, leaseId, transactionHash));

    when(tenantService.isDeedOwner(owner, nftId)).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> leaseService.endLease(owner, leaseId, null));

    when(leaseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    DeedTenantLeaseDTO changedLease = leaseService.endLease(owner, leaseId, transactionHash);
    assertNotNull(changedLease);
    assertEquals(StringUtils.lowerCase(owner), changedLease.getEndingLeaseAddress());
    assertEquals(StringUtils.lowerCase(owner), changedLease.getOwnerAddress());
    assertTrue(changedLease.isEndingLease());

    verify(listenerService, times(1)).publishEvent(eq(LEASE_TENANT_EVICT_EVENT), any());
  }

  @Test
  void testEndLeaseByManager() throws Exception {
    long nftId = 2l;
    long leaseId = 3l;
    String tenant = "tenantAddress";
    String owner = "ownerAddress";
    String transactionHash = "transactionHash";

    assertThrows(ObjectNotFoundException.class, () -> leaseService.endLease(tenant, leaseId, transactionHash));

    DeedTenantLease lease = newLease(leaseId, nftId);
    lease.setManager(tenant);
    when(leaseRepository.findById(lease.getId())).thenReturn(Optional.of(lease));
    assertThrows(UnauthorizedOperationException.class, () -> leaseService.endLease(tenant, leaseId, transactionHash));
    assertThrows(UnauthorizedOperationException.class, () -> leaseService.endLease(owner, leaseId, transactionHash));

    when(tenantService.isDeedManager(tenant, nftId)).thenReturn(true);
    assertThrows(UnauthorizedOperationException.class, () -> leaseService.endLease(owner, leaseId, transactionHash));

    when(tenantService.isDeedOwner(owner, nftId)).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> leaseService.endLease(owner, leaseId, null));

    when(leaseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    DeedTenantLeaseDTO changedLease = leaseService.endLease(tenant, leaseId, transactionHash);
    assertNotNull(changedLease);
    assertEquals(StringUtils.lowerCase(tenant), changedLease.getEndingLeaseAddress());
    assertTrue(changedLease.isEndingLease());

    verify(listenerService, times(1)).publishEvent(eq(LEASE_END_EVENT), any());
  }

  @Test
  void testGetPendingTransactions() throws Exception {
    long nftId = 2l;
    long leaseId = 3l;
    DeedTenantLease lease = newLease(leaseId, nftId);
    when(leaseRepository.findByTransactionStatusInOrderByCreatedDateAsc(Arrays.asList(TransactionStatus.IN_PROGRESS))).thenReturn(Collections.singletonList(lease));

    List<DeedTenantLease> pendingTransactions = leaseService.getPendingTransactions();
    assertNotNull(pendingTransactions);
    assertEquals(1, pendingTransactions.size());

    DeedTenantLease pendingLease = pendingTransactions.get(0);
    assertEquals(lease.getId(), pendingLease.getId());
    assertEquals(lease.getNftId(), pendingLease.getNftId());
  }

  @Test
  void testUpdateLeaseStatusFromBlockchain() throws Exception {
    long nftId = 2l;
    long leaseId = 3l;
    String transactionHash = "transactionHash";
    long lastBlockNumber = 12l;
    String tenant = "tenantAddress";

    assertThrows(ObjectNotFoundException.class,
                 () -> leaseService.updateLeaseStatusFromBlockchain(leaseId, transactionHash, Collections.emptyMap()));
    when(leaseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    DeedTenantLease lease = newLease(leaseId, nftId);
    leaseService.updateLeaseStatusFromBlockchain(leaseId, transactionHash, Collections.emptyMap());
    verify(leaseRepository, times(1)).save(argThat(new ArgumentMatcher<DeedTenantLease>() {
      @Override
      public boolean matches(DeedTenantLease persistedLease) {
        if (lease.getId() == persistedLease.getId()) {
          assertEquals(0, persistedLease.getPendingTransactions().size());
          assertFalse(persistedLease.isEndingLease());
          assertEquals(0, persistedLease.getMonthPaymentInProgress());
          assertFalse(persistedLease.isEnabled());
          assertEquals(TransactionStatus.ERROR, persistedLease.getTransactionStatus());
          return true;
        }
        return false;
      }
    }));

    long enabledLeaseId = 4l;
    DeedTenantLease enabledLease = newLease(enabledLeaseId, nftId);
    enabledLease.setEndingLease(true);
    enabledLease.setMonthPaymentInProgress(5);
    enabledLease.setConfirmed(true);
    DeedLeaseBlockchainState blockchainState = new DeedLeaseBlockchainState(BigInteger.valueOf(enabledLeaseId),
                                                                            BigInteger.valueOf(lastBlockNumber),
                                                                            BigInteger.valueOf(nftId),
                                                                            BigInteger.valueOf(1),
                                                                            BigInteger.valueOf(2),
                                                                            BigInteger.valueOf(3),
                                                                            BigInteger.valueOf(4),
                                                                            BigInteger.valueOf(5),
                                                                            tenant,
                                                                            transactionHash);

    leaseService.updateLeaseStatusFromBlockchain(enabledLeaseId,
                                                 transactionHash,
                                                 Collections.singletonMap(BlockchainLeaseStatus.LEASE_PAYED, blockchainState));
    verify(leaseRepository, times(1)).save(argThat(new ArgumentMatcher<DeedTenantLease>() {
      @Override
      public boolean matches(DeedTenantLease persistedLease) {
        if (enabledLeaseId == persistedLease.getId()) {
          assertEquals(0, persistedLease.getPendingTransactions().size());
          assertFalse(persistedLease.isEndingLease());
          assertEquals(0, persistedLease.getMonthPaymentInProgress());
          assertTrue(persistedLease.isEnabled());
          assertEquals(TransactionStatus.VALIDATED, persistedLease.getTransactionStatus());
          return true;
        }
        return false;
      }
    }));

    verify(listenerService, times(1)).publishEvent(eq(LEASE_RENT_PAYMENT_CONFIRMED_EVENT), any());
  }

  private DeedTenantLease newLease(long leaseId, long nftId) {
    DeedTenantLease lease = new DeedTenantLease();
    lease.setId(leaseId);
    lease.setNftId(nftId);
    lease.setCardType(DeedCard.UNCOMMON);
    lease.setCity(DeedCity.MELQART);
    lease.setOwner("otherWallet");
    lease.setAmount(12);
    lease.setMonths(6);
    lease.setPaymentPeriodicity(RentalPaymentPeriodicity.ONE_MONTH);
    lease.setOwnerMintingPercentage(50);
    lease.setMintingPower(DeedCard.UNCOMMON.getMintingPower());
    lease.setCreatedDate(Instant.now());
    lease.setPendingTransactions(Collections.singletonList(StringUtils.lowerCase("TransactionHash")));
    lease.setEnabled(true);
    lenient().when(leaseRepository.findById(lease.getId())).thenReturn(Optional.of(lease));
    return lease;
  }

  private DeedTenantOfferDTO newOffer(String id, long nftId, long offerId) {
    return new DeedTenantOfferDTO(id,
                                  offerId,
                                  nftId,
                                  DeedCity.ASHTARTE,
                                  DeedCard.UNCOMMON,
                                  "owner",
                                  null,
                                  "description",
                                  5d,
                                  10d,
                                  OfferType.RENTING,
                                  ExpirationDuration.ONE_DAY,
                                  ExpirationDuration.ONE_DAY.getDays(),
                                  RentalDuration.ONE_MONTH,
                                  RentalDuration.ONE_MONTH.getMonths(),
                                  NoticePeriod.ONE_MONTH,
                                  NoticePeriod.ONE_MONTH.getMonths(),
                                  RentalPaymentPeriodicity.ONE_YEAR,
                                  1,
                                  1.1d,
                                  "0xTransaction",
                                  TransactionStatus.IN_PROGRESS,
                                  Instant.now(),
                                  Instant.now(),
                                  Instant.now(),
                                  Instant.now(),
                                  false,
                                  null,
                                  null,
                                  null,
                                  null);
  }

  private void assertElasticSearchQuery(SearchHits<DeedTenantLease> searchHits, String... filedNames) {
    when(elasticsearchOperations.search(argThat(new ArgumentMatcher<Query>() {
      @Override
      public boolean matches(Query query) {
        Arrays.stream(filedNames).forEach(filedName -> {
          assertQueryCriteriaNotNull(query, filedName);
        });
        return true;
      }
    }), eq(DeedTenantLease.class))).thenReturn(searchHits);
  }

  private void assertQueryCriteriaNotNull(Query query, String filedName) {
    CriteriaQuery criteriaQuery = (CriteriaQuery) query;
    if (criteriaQuery == null) {
      fail("Criteria for Field " + filedName + " not found");
    }
    Criteria criteria = criteriaQuery.getCriteria();
    List<Criteria> criteriaChain = criteria.getCriteriaChain();
    assertNotNull(criteriaChain);
    for (Criteria subCriteria : criteriaChain) {
      assertNotNull(subCriteria);
      if (StringUtils.equals(filedName, subCriteria.getField().getName())) {
        return;
      }
    }
    fail("Field " + filedName + " not found");
  }
}
