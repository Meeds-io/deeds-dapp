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

import static io.meeds.deeds.common.constant.CommonConstants.DEED_EVENT_TENANT_EMAIL_UPDATED;
import static io.meeds.deeds.common.constant.CommonConstants.TENANT_COMMAND_START_EVENT;
import static io.meeds.deeds.common.constant.CommonConstants.TENANT_COMMAND_STOP_EVENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import io.meeds.deeds.common.constant.TenantProvisioningStatus;
import io.meeds.deeds.common.constant.UnauthorizedOperationException;
import io.meeds.deeds.common.elasticsearch.model.DeedTenant;
import io.meeds.deeds.common.elasticsearch.storage.DeedTenantManagerRepository;

@SpringBootTest(classes = {
    TenantService.class
})
class TenantServiceTest {

  @MockBean
  private BlockchainService           blockchainService;

  @MockBean
  private DeedTenantManagerRepository deedTenantManagerRepository;

  @MockBean
  private ListenerService             listenerService;

  @Autowired
  private TenantService               tenantService;

  @Test
  void testGetDeedTenant() throws Exception {
    long nftId = 1l;

    DeedTenant deedTenantMock = new DeedTenant();
    deedTenantMock.setNftId(nftId);

    when(deedTenantManagerRepository.findById(nftId)).thenReturn(Optional.of(deedTenantMock));

    DeedTenant deedTenant = tenantService.getDeedTenant(nftId);
    assertNotNull(deedTenant);
    assertEquals(deedTenantMock, deedTenant);
  }

  @Test
  void testGetDeedTenantByAddress() throws Exception {
    long nftId = 1l;
    String address = "address";

    assertThrows(UnauthorizedOperationException.class, () -> tenantService.getDeedTenant(address, nftId));
  }

  @Test
  void testGetDeedTenantByManager() throws Exception {
    long nftId = 1l;
    String address = "address";
    DeedTenant deedTenantMock = new DeedTenant();
    deedTenantMock.setNftId(nftId);

    when(deedTenantManagerRepository.findById(nftId)).thenReturn(Optional.of(deedTenantMock));
    when(blockchainService.isDeedProvisioningManager(address, nftId)).thenReturn(true);
    DeedTenant deedTenant = tenantService.getDeedTenant(address, nftId);
    assertEquals(deedTenantMock, deedTenant);
  }

  @Test
  void testGetDeedTenantByOwner() throws Exception {
    long nftId = 1l;
    String address = "address";
    DeedTenant deedTenantMock = new DeedTenant();
    deedTenantMock.setNftId(nftId);

    when(deedTenantManagerRepository.findById(nftId)).thenReturn(Optional.of(deedTenantMock));
    when(blockchainService.isDeedOwner(address, nftId)).thenReturn(true);
    DeedTenant deedTenant = tenantService.getDeedTenant(address, nftId);
    assertEquals(deedTenantMock, deedTenant);
  }

  @Test
  void testGetDeedTenantOrImportByManagerWhenExists() throws Exception {
    long nftId = 1l;
    String address = "address";
    DeedTenant deedTenantMock = new DeedTenant();
    deedTenantMock.setNftId(nftId);

    when(deedTenantManagerRepository.findById(nftId)).thenReturn(Optional.of(deedTenantMock));
    when(deedTenantManagerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0, DeedTenant.class));
    when(blockchainService.isDeedProvisioningManager(address, nftId)).thenReturn(true);
    DeedTenant deedTenant = tenantService.getDeedTenantOrImport(address, nftId);
    assertEquals(deedTenantMock, deedTenant);
    assertEquals(address, deedTenant.getManagerAddress());
    assertNull(deedTenant.getOwnerAddress());
  }

  void testGetDeedTenantOrImportByOwnerWhenExists() throws Exception {
    long nftId = 1l;
    String address = "address";
    DeedTenant deedTenantMock = new DeedTenant();
    deedTenantMock.setNftId(nftId);

    when(deedTenantManagerRepository.findById(nftId)).thenReturn(Optional.of(deedTenantMock));
    when(deedTenantManagerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0, DeedTenant.class));
    when(blockchainService.isDeedOwner(address, nftId)).thenReturn(true);
    DeedTenant deedTenant = tenantService.getDeedTenantOrImport(address, nftId);
    assertEquals(deedTenantMock, deedTenant);
    assertEquals(address, deedTenant.getOwnerAddress());
    assertNull(deedTenant.getManagerAddress());
  }

  @Test
  void testGetDeedTenantOrImportByManagerWhenNotExists() throws Exception {
    long nftId = 1l;
    String address = "address";
    short cardType = 2;
    short cityIndex = 3;
    DeedTenant deedTenantMock = new DeedTenant();
    deedTenantMock.setNftId(nftId);
    deedTenantMock.setManagerAddress(address);
    deedTenantMock.setCardType(cardType);
    deedTenantMock.setCityIndex(cityIndex);

    when(blockchainService.getDeedCardType(nftId)).thenReturn(cardType);
    when(blockchainService.getDeedCityIndex(nftId)).thenReturn(cityIndex);
    when(blockchainService.isDeedProvisioningManager(address, nftId)).thenReturn(true);
    when(deedTenantManagerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0, DeedTenant.class));

    DeedTenant deedTenant = tenantService.getDeedTenantOrImport(address, nftId);

    verify(deedTenantManagerRepository, times(1)).save(argThat(new ArgumentMatcher<DeedTenant>() {
      @Override
      public boolean matches(DeedTenant deedTenant) {
        assertNotNull(deedTenant);
        assertEquals(nftId, deedTenant.getNftId());
        assertEquals(cardType, deedTenant.getCardType());
        assertEquals(cityIndex, deedTenant.getCityIndex());
        assertNull(deedTenant.getManagerEmail());
        assertNull(deedTenant.getStartupTransactionHash());
        assertNull(deedTenant.getShutdownTransactionHash());
        assertNull(deedTenant.getDate());
        return true;
      }
    }));
    assertNotNull(deedTenant);
    assertEquals(address, deedTenant.getManagerAddress());
    assertNull(deedTenant.getOwnerAddress());
  }

  @Test
  void testGetDeedTenantOrImportByOwnerWhenNotExists() throws Exception {
    long nftId = 1l;
    String address = "address";
    short cardType = 2;
    short cityIndex = 3;
    DeedTenant deedTenantMock = new DeedTenant();
    deedTenantMock.setNftId(nftId);
    deedTenantMock.setCardType(cardType);
    deedTenantMock.setCityIndex(cityIndex);

    when(blockchainService.getDeedCardType(nftId)).thenReturn(cardType);
    when(blockchainService.getDeedCityIndex(nftId)).thenReturn(cityIndex);
    when(blockchainService.isDeedOwner(address, nftId)).thenReturn(true);
    when(deedTenantManagerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0, DeedTenant.class));

    DeedTenant deedTenant = tenantService.getDeedTenantOrImport(address, nftId);

    verify(deedTenantManagerRepository, times(1)).save(argThat(new ArgumentMatcher<DeedTenant>() {
      @Override
      public boolean matches(DeedTenant deedTenant) {
        assertNotNull(deedTenant);
        assertEquals(nftId, deedTenant.getNftId());
        assertEquals(cardType, deedTenant.getCardType());
        assertEquals(cityIndex, deedTenant.getCityIndex());
        assertNull(deedTenant.getManagerEmail());
        assertNull(deedTenant.getStartupTransactionHash());
        assertNull(deedTenant.getShutdownTransactionHash());
        assertNull(deedTenant.getDate());
        return true;
      }
    }));
    assertNotNull(deedTenant);
    assertEquals(address, deedTenant.getOwnerAddress());
    assertNull(deedTenant.getManagerAddress());
  }

  @Test
  void testGetDeedTenantOrImportByChangedOwner() throws Exception {
    long nftId = 1l;
    String address = "address";
    String otherAddress = "anotherAddress";
    short cardType = 2;
    short cityIndex = 3;
    when(blockchainService.getDeedCardType(nftId)).thenReturn(cardType);
    when(blockchainService.getDeedCityIndex(nftId)).thenReturn(cityIndex);
    DeedTenant deedTenantMock = new DeedTenant();
    deedTenantMock.setNftId(nftId);
    deedTenantMock.setManagerAddress(otherAddress);

    when(deedTenantManagerRepository.findById(nftId)).thenReturn(Optional.of(deedTenantMock));
    when(blockchainService.isDeedProvisioningManager(address, nftId)).thenReturn(true);
    when(deedTenantManagerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0, DeedTenant.class));

    DeedTenant deedTenant = tenantService.getDeedTenantOrImport(address, nftId);
    assertNotNull(deedTenant);
    assertEquals(address, deedTenant.getManagerAddress());
    verify(deedTenantManagerRepository, times(1)).save(deedTenant);
  }

  @Test
  void testGetDeedTenantOrImportByChangedManager() throws Exception {
    long nftId = 1l;
    String address = "address";
    String otherAddress = "anotherAddress";
    short cardType = 2;
    short cityIndex = 3;
    when(blockchainService.getDeedCardType(nftId)).thenReturn(cardType);
    when(blockchainService.getDeedCityIndex(nftId)).thenReturn(cityIndex);
    DeedTenant deedTenantMock = new DeedTenant();
    deedTenantMock.setNftId(nftId);
    deedTenantMock.setOwnerAddress(otherAddress);

    when(deedTenantManagerRepository.findById(nftId)).thenReturn(Optional.of(deedTenantMock));
    when(blockchainService.isDeedOwner(address, nftId)).thenReturn(true);
    when(deedTenantManagerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0, DeedTenant.class));

    DeedTenant deedTenant = tenantService.getDeedTenantOrImport(address, nftId);
    assertNotNull(deedTenant);
    assertEquals(address, deedTenant.getOwnerAddress());
    verify(deedTenantManagerRepository, times(1)).save(deedTenant);
  }

  @Test
  void testStartTenantByOwnerNotManager() throws Exception {
    long nftId = 1l;
    String address = "address";
    String transactionHash = "transactionHash";
    String email = "email";

    // Made lenient on purpose to double check even when implementation is
    // modified
    lenient().when(blockchainService.isDeedOwner(address, nftId)).thenReturn(true);
    assertThrows(UnauthorizedOperationException.class, () -> tenantService.startTenant(address, transactionHash, nftId, email));
  }

  @Test
  void testStartTenantByManagerWithExistingTenant() throws Exception {
    long nftId = 1l;
    short cardType = 2;
    short cityIndex = 3;
    String managerAddress = "managerAddress";
    String transactionHash = "transactionHash";
    String email = "email";
    DeedTenant deedTenantMock = new DeedTenant();
    deedTenantMock.setNftId(nftId);
    deedTenantMock.setManagerAddress(managerAddress);
    deedTenantMock.setManagerEmail(email);
    deedTenantMock.setStartupTransactionHash(transactionHash);
    deedTenantMock.setShutdownTransactionHash(transactionHash);
    deedTenantMock.setDate(LocalDateTime.now(ZoneOffset.UTC));

    when(blockchainService.isDeedProvisioningManager(managerAddress, nftId)).thenReturn(true);
    when(blockchainService.getDeedCardType(nftId)).thenReturn(cardType);
    when(blockchainService.getDeedCityIndex(nftId)).thenReturn(cityIndex);

    when(deedTenantManagerRepository.findById(nftId)).thenReturn(Optional.of(deedTenantMock));
    tenantService.startTenant(managerAddress, transactionHash, nftId, email);

    verify(deedTenantManagerRepository, times(1)).save(argThat(new ArgumentMatcher<DeedTenant>() {
      @Override
      public boolean matches(DeedTenant deedTenant) {
        assertNotNull(deedTenant);
        assertNotNull(deedTenant.getDate());
        assertTrue(Duration.between(deedTenant.getDate(), deedTenantMock.getDate()).getSeconds() <= 5);
        assertEquals(deedTenantMock.getNftId(), deedTenant.getNftId());
        assertEquals(deedTenantMock.getManagerAddress().toLowerCase(), deedTenant.getManagerAddress().toLowerCase());
        assertEquals(cardType, deedTenant.getCardType());
        assertEquals(cityIndex, deedTenant.getCityIndex());
        assertEquals(deedTenantMock.getManagerEmail(), deedTenant.getManagerEmail());
        assertEquals(deedTenantMock.getStartupTransactionHash().toLowerCase(),
                     deedTenant.getStartupTransactionHash().toLowerCase());
        assertNull(deedTenant.getShutdownTransactionHash());
        return true;
      }
    }));

    verify(listenerService, times(1)).publishEvent(eq(TENANT_COMMAND_START_EVENT), any());
  }

  @Test
  void testStartTenantByManager() throws Exception {
    long nftId = 1l;
    short cardType = 2;
    short cityIndex = 3;
    String managerAddress = "managerAddress";
    String transactionHash = "transactionHash";
    String email = "email";
    DeedTenant deedTenantMock = new DeedTenant();
    deedTenantMock.setNftId(nftId);
    deedTenantMock.setManagerAddress(managerAddress);
    deedTenantMock.setCardType(cardType);
    deedTenantMock.setCityIndex(cityIndex);
    deedTenantMock.setManagerEmail(email);
    deedTenantMock.setStartupTransactionHash(transactionHash);
    deedTenantMock.setShutdownTransactionHash(transactionHash);
    deedTenantMock.setDate(LocalDateTime.now(ZoneOffset.UTC));

    when(blockchainService.isDeedProvisioningManager(managerAddress, nftId)).thenReturn(true);
    when(blockchainService.getDeedCardType(nftId)).thenReturn(cardType);
    when(blockchainService.getDeedCityIndex(nftId)).thenReturn(cityIndex);

    tenantService.startTenant(managerAddress, transactionHash, nftId, email);

    verify(deedTenantManagerRepository, times(1)).save(argThat(new ArgumentMatcher<DeedTenant>() {
      @Override
      public boolean matches(DeedTenant deedTenant) {
        assertNotNull(deedTenant);
        assertNotNull(deedTenant.getDate());
        assertTrue(Duration.between(deedTenant.getDate(), deedTenantMock.getDate()).getSeconds() <= 5);
        assertEquals(deedTenantMock.getNftId(), deedTenant.getNftId());
        assertEquals(deedTenantMock.getManagerAddress().toLowerCase(), deedTenant.getManagerAddress().toLowerCase());
        assertEquals(deedTenantMock.getCardType(), deedTenant.getCardType());
        assertEquals(deedTenantMock.getCityIndex(), deedTenant.getCityIndex());
        assertEquals(deedTenantMock.getManagerEmail(), deedTenant.getManagerEmail());
        assertEquals(deedTenantMock.getStartupTransactionHash().toLowerCase(),
                     deedTenant.getStartupTransactionHash().toLowerCase());
        assertNull(deedTenant.getShutdownTransactionHash());
        return true;
      }
    }));

    verify(listenerService, times(1)).publishEvent(eq(TENANT_COMMAND_START_EVENT), any());
  }

  @Test
  void testStopTenantByOwnerNotManager() throws Exception {
    long nftId = 1l;
    String address = "address";
    String transactionHash = "transactionHash";

    lenient().when(blockchainService.isDeedOwner(address, nftId)).thenReturn(true);
    assertThrows(UnauthorizedOperationException.class, () -> tenantService.stopTenant(address, transactionHash, nftId));
  }

  @Test
  void testStopTenantByManagerWithExistingTenant() throws Exception {
    long nftId = 1l;
    short cardType = 2;
    short cityIndex = 3;
    String managerAddress = "managerAddress";
    String transactionHash = "transactionHash";
    DeedTenant deedTenantMock = new DeedTenant();
    deedTenantMock.setNftId(nftId);
    deedTenantMock.setManagerAddress(managerAddress);
    deedTenantMock.setStartupTransactionHash(transactionHash);
    deedTenantMock.setShutdownTransactionHash(transactionHash);
    deedTenantMock.setDate(LocalDateTime.now(ZoneOffset.UTC));

    when(blockchainService.isDeedProvisioningManager(managerAddress, nftId)).thenReturn(true);
    when(blockchainService.getDeedCardType(nftId)).thenReturn(cardType);
    when(blockchainService.getDeedCityIndex(nftId)).thenReturn(cityIndex);

    when(deedTenantManagerRepository.findById(nftId)).thenReturn(Optional.of(deedTenantMock));
    tenantService.stopTenant(managerAddress, transactionHash, nftId);

    verify(deedTenantManagerRepository, times(1)).save(argThat(new ArgumentMatcher<DeedTenant>() {
      @Override
      public boolean matches(DeedTenant deedTenant) {
        assertNotNull(deedTenant);
        assertNotNull(deedTenant.getDate());
        assertTrue(Duration.between(deedTenant.getDate(), deedTenantMock.getDate()).getSeconds() <= 5);
        assertEquals(deedTenantMock.getNftId(), deedTenant.getNftId());
        assertEquals(deedTenantMock.getManagerAddress().toLowerCase(), deedTenant.getManagerAddress().toLowerCase());
        assertEquals(cardType, deedTenant.getCardType());
        assertEquals(cityIndex, deedTenant.getCityIndex());
        assertEquals(deedTenantMock.getManagerEmail(), deedTenant.getManagerEmail());
        assertEquals(deedTenantMock.getShutdownTransactionHash().toLowerCase(),
                     deedTenant.getShutdownTransactionHash().toLowerCase());
        assertNull(deedTenant.getStartupTransactionHash());
        return true;
      }
    }));

    verify(listenerService, times(1)).publishEvent(eq(TENANT_COMMAND_STOP_EVENT), any());
  }

  @Test
  void testStopTenantByManager() throws Exception {
    long nftId = 1l;
    short cardType = 2;
    short cityIndex = 3;
    String managerAddress = "managerAddress";
    String transactionHash = "transactionHash";
    DeedTenant deedTenantMock = new DeedTenant();
    deedTenantMock.setNftId(nftId);
    deedTenantMock.setManagerAddress(managerAddress);
    deedTenantMock.setCardType(cardType);
    deedTenantMock.setCityIndex(cityIndex);
    deedTenantMock.setStartupTransactionHash(transactionHash);
    deedTenantMock.setShutdownTransactionHash(transactionHash);
    deedTenantMock.setDate(LocalDateTime.now(ZoneOffset.UTC));

    when(blockchainService.isDeedProvisioningManager(managerAddress, nftId)).thenReturn(true);
    when(blockchainService.getDeedCardType(nftId)).thenReturn(cardType);
    when(blockchainService.getDeedCityIndex(nftId)).thenReturn(cityIndex);

    tenantService.stopTenant(managerAddress, transactionHash, nftId);

    verify(deedTenantManagerRepository, times(1)).save(argThat(new ArgumentMatcher<DeedTenant>() {
      @Override
      public boolean matches(DeedTenant deedTenant) {
        assertNotNull(deedTenant);
        assertNotNull(deedTenant.getDate());
        assertTrue(Duration.between(deedTenant.getDate(), deedTenantMock.getDate()).getSeconds() <= 5);
        assertEquals(deedTenantMock.getNftId(), deedTenant.getNftId());
        assertEquals(deedTenantMock.getManagerAddress().toLowerCase(), deedTenant.getManagerAddress().toLowerCase());
        assertEquals(deedTenantMock.getCardType(), deedTenant.getCardType());
        assertEquals(deedTenantMock.getCityIndex(), deedTenant.getCityIndex());
        assertEquals(deedTenantMock.getManagerEmail(), deedTenant.getManagerEmail());
        assertEquals(deedTenantMock.getShutdownTransactionHash().toLowerCase(),
                     deedTenant.getShutdownTransactionHash().toLowerCase());
        assertNull(deedTenant.getStartupTransactionHash());
        return true;
      }
    }));

    verify(listenerService, times(1)).publishEvent(eq(TENANT_COMMAND_STOP_EVENT), any());
  }

  @Test
  void testSaveEmailByTenantOwnerNotManager() throws Exception {
    long nftId = 1l;
    String address = "address";
    String email = "email";

    lenient().when(blockchainService.isDeedOwner(address, nftId)).thenReturn(true);
    assertThrows(UnauthorizedOperationException.class, () -> tenantService.saveEmail(address, nftId, email));
  }

  @Test
  void testIsTenantCommandStop() {
    long nftId = 1l;

    assertTrue(tenantService.isTenantCommandStop(nftId));

    DeedTenant deedTenantMock = mock(DeedTenant.class);

    when(deedTenantManagerRepository.findById(nftId)).thenReturn(Optional.of(deedTenantMock));
    assertTrue(tenantService.isTenantCommandStop(nftId));

    when(deedTenantMock.getTenantProvisioningStatus()).thenReturn(TenantProvisioningStatus.START_CONFIRMED);
    assertFalse(tenantService.isTenantCommandStop(nftId));

    when(deedTenantMock.getTenantProvisioningStatus()).thenReturn(TenantProvisioningStatus.START_IN_PROGRESS);
    assertFalse(tenantService.isTenantCommandStop(nftId));

    when(deedTenantMock.getTenantProvisioningStatus()).thenReturn(TenantProvisioningStatus.STOP_IN_PROGRESS);
    assertFalse(tenantService.isTenantCommandStop(nftId));

    when(deedTenantMock.getTenantProvisioningStatus()).thenReturn(TenantProvisioningStatus.STOP_CONFIRMED);
    assertTrue(tenantService.isTenantCommandStop(nftId));
  }

  @Test
  void testSaveEmailByTenantManager() throws Exception {
    long nftId = 1l;
    short cardType = 2;
    short cityIndex = 3;
    String managerAddress = "managerAddress";
    String transactionHash = "transactionHash";
    String email = "email";
    DeedTenant deedTenantMock = new DeedTenant();
    deedTenantMock.setNftId(nftId);
    deedTenantMock.setManagerAddress(managerAddress);
    deedTenantMock.setCardType(cardType);
    deedTenantMock.setCityIndex(cityIndex);
    deedTenantMock.setStartupTransactionHash(transactionHash);
    deedTenantMock.setShutdownTransactionHash(transactionHash);
    deedTenantMock.setDate(LocalDateTime.now(ZoneOffset.UTC));

    when(deedTenantManagerRepository.findById(nftId)).thenReturn(Optional.of(deedTenantMock));
    when(blockchainService.isDeedProvisioningManager(managerAddress, nftId)).thenReturn(true);
    when(blockchainService.getDeedCardType(nftId)).thenReturn(cardType);
    when(blockchainService.getDeedCityIndex(nftId)).thenReturn(cityIndex);

    tenantService.saveEmail(managerAddress, nftId, email);

    verify(deedTenantManagerRepository, times(1)).save(argThat(new ArgumentMatcher<DeedTenant>() {
      @Override
      public boolean matches(DeedTenant deedTenant) {
        assertNotNull(deedTenant);
        assertNotNull(deedTenant.getDate());
        assertTrue(Duration.between(deedTenant.getDate(), deedTenantMock.getDate()).getSeconds() <= 5);
        assertEquals(nftId, deedTenant.getNftId());
        assertEquals(managerAddress.toLowerCase(), deedTenant.getManagerAddress().toLowerCase());
        assertEquals(cardType, deedTenant.getCardType());
        assertEquals(cityIndex, deedTenant.getCityIndex());
        assertEquals(email, deedTenant.getManagerEmail());
        assertEquals(transactionHash, deedTenant.getStartupTransactionHash());
        assertEquals(transactionHash, deedTenant.getShutdownTransactionHash());
        return true;
      }
    }));

    verify(listenerService, times(1)).publishEvent(eq(DEED_EVENT_TENANT_EMAIL_UPDATED), any());
  }

}
