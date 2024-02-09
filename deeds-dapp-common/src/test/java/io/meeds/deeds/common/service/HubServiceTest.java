/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.deeds.common.service;

import static io.meeds.deeds.common.service.HubService.HUB_CONNECTED;
import static io.meeds.deeds.common.service.HubService.HUB_DISCONNECTED;
import static io.meeds.deeds.common.service.HubService.HUB_SAVED;
import static io.meeds.deeds.common.service.HubService.HUB_STATUS_CHANGED;
import static io.meeds.deeds.common.service.HubService.WOM_INVALID_SIGNED_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import io.meeds.deeds.common.constant.TenantProvisioningStatus;
import io.meeds.deeds.common.constant.TenantStatus;
import io.meeds.deeds.common.elasticsearch.model.DeedFileBinary;
import io.meeds.deeds.common.elasticsearch.model.DeedTenant;
import io.meeds.deeds.common.elasticsearch.model.HubEntity;
import io.meeds.deeds.common.elasticsearch.model.UemRewardEntity;
import io.meeds.deeds.common.elasticsearch.storage.HubRepository;
import io.meeds.deeds.common.elasticsearch.storage.UemRewardRepository;
import io.meeds.deeds.common.model.DeedTenantLeaseDTO;
import io.meeds.deeds.common.model.FileBinary;
import io.meeds.deeds.common.model.LeaseFilter;
import io.meeds.deeds.common.model.ManagedDeed;
import io.meeds.deeds.common.model.WomDeed;
import io.meeds.deeds.common.model.WomHub;
import io.meeds.wom.api.constant.ObjectNotFoundException;
import io.meeds.wom.api.constant.WomAuthorizationException;
import io.meeds.wom.api.constant.WomException;
import io.meeds.wom.api.constant.WomRequestException;
import io.meeds.wom.api.model.Hub;
import io.meeds.wom.api.model.HubReport;
import io.meeds.wom.api.model.HubUpdateRequest;
import io.meeds.wom.api.model.WomConnectionRequest;
import io.meeds.wom.api.model.WomDisconnectionRequest;

@SpringBootTest(classes = {
  HubService.class,
})
@ExtendWith(MockitoExtension.class)
class HubServiceTest {

  private static final String RAW_MESSAGE            = "rawMessage";

  @MockBean
  private TenantService       tenantService;

  @MockBean
  private ListenerService     listenerService;

  @MockBean
  private FileService         fileService;

  @MockBean
  private BlockchainService   blockchainService;

  @MockBean
  private LeaseService        leaseService;

  @MockBean
  private HubRepository       hubRepository;

  @MockBean
  private UemRewardRepository rewardRepository;

  @Autowired
  private HubService          hubService;

  private long                periodStartTime        = ZonedDateTime.now()
                                                                    .with(DayOfWeek.MONDAY)
                                                                    .minusWeeks(3)
                                                                    .toLocalDate()
                                                                    .atStartOfDay(ZoneOffset.UTC)
                                                                    .toEpochSecond();

  private long                periodEndTime          = ZonedDateTime.now()
                                                                    .with(DayOfWeek.MONDAY)
                                                                    .minusWeeks(2)
                                                                    .toLocalDate()
                                                                    .atStartOfDay(ZoneOffset.UTC)
                                                                    .toEpochSecond();

  private Credentials         hubCredentials         =
                                             Credentials.create("0x1da4ef21b864d2cc526dbdb2a120bd2874c36c9d0a1fb7f8c63d7f7a8b41de8f");

  private String              hubAddress             = hubCredentials.getAddress();

  private Set<String>         hubAddresses           = Collections.singleton(hubAddress.toLowerCase());

  private long                deedId                 = 3l;

  private short               city                   = 1;

  private short               cardType               = 2;

  private short               ownerPercentage        = 60;

  private short               mintingPower           = 130;

  private long                maxUsers               = 10000;

  private String              deedOwnerAddress       = "0x530417D6909834f9Ebfe5d98b649433B616Efb38";

  private Credentials         deedManagerCredentials =
                                                     Credentials.create("0x3ba4ef21b864d2cc526dbdb2a120bd2874c36c9d0a1fb7f8c63d7f7a8b41de8f");

  private String              deedManagerAddress     = deedManagerCredentials.getAddress();

  private String              hubOwnerAddress        = deedManagerAddress;

  private String              deedManagerEmail       = "managerEmail";

  private String              txHash                 =
                                     "0xef4e9db309b5dd7020ce463ae726b4d0759e1de0635661de91d8d98e83ae2862";

  private Map<String, String> name                   = Collections.singletonMap("en", "name");

  private Map<String, String> description            = Collections.singletonMap("en", "description");

  private String              url                    = "url";

  private String              color                  = "#144523";

  private String              avatarId               = "avatarId";

  private String              bannerId               = "bannerId";

  private long                usersCount             = 56l;

  private String              rewardsPeriodType      = "WEEK";

  private double              rewardsPerPeriod       = 566.54d;

  private boolean             enabled                = true;

  private double              ownerClaimableAmount   = 25.6d;

  private double              managerClaimableAmount = 12.9d;

  private double              uemRewardAmount        = 2008.9d;

  private double              fixedGlobalIndex       = 3000.6554879d;

  private double              sumEd                  = 800.6554879d;

  private long                rewardId               = 328746l;

  private long                reportId               = 25546l;

  private List<Long>          reportIds              = Collections.singletonList(reportId);

  private Instant             createdDate            = Instant.now().minusSeconds(16);

  private Instant             untilDate              = Instant.now().plusSeconds(28);

  private Instant             joinDate               = Instant.now().minusSeconds(10);

  private Instant             updatedDate            = Instant.now();

  @Test
  void getHubs() {
    HubEntity hubEntity = newHubEntity();
    Page<HubEntity> hubEntitiesPage = new PageImpl<>(Collections.singletonList(hubEntity));
    when(hubRepository.findByEnabledIsTrue(any())).thenReturn(hubEntitiesPage);
    Page<Hub> hubsPage = hubService.getHubs(Pageable.unpaged());
    assertNotNull(hubsPage);
    assertEquals(1, hubsPage.getSize());
    Hub hub = hubsPage.getContent().get(0);
    assertEquals(hubEntity.getAddress().toLowerCase(), hub.getAddress().toLowerCase());
    assertEquals(hubEntity.getNftId(), hub.getDeedId());
    assertEquals(hubEntity.getCity(), hub.getCity());
    assertEquals(hubEntity.getType(), hub.getType());
    assertEquals(hubEntity.getHubOwnerAddress().toLowerCase(), hub.getHubOwnerAddress().toLowerCase());
    assertEquals(hubEntity.getDeedOwnerAddress().toLowerCase(), hub.getDeedOwnerAddress().toLowerCase());
    assertEquals(hubEntity.getDeedManagerAddress().toLowerCase(), hub.getDeedManagerAddress().toLowerCase());
    assertEquals(hubEntity.getName(), hub.getName());
    assertEquals(hubEntity.getDescription(), hub.getDescription());
    assertEquals(hubEntity.getUrl(), hub.getUrl());
    assertEquals(hubEntity.getColor(), hub.getColor());
    assertEquals(hubEntity.getRewardsPeriodType(), hub.getRewardsPeriodType());
    assertEquals(hubEntity.getRewardsPerPeriod(), hub.getRewardsPerPeriod());
    assertEquals(hubEntity.isEnabled(), hub.isConnected());
    assertEquals(hubEntity.getOwnerClaimableAmount(), hub.getOwnerClaimableAmount());
    assertEquals(hubEntity.getManagerClaimableAmount(), hub.getManagerClaimableAmount());
    assertEquals(hubEntity.getCreatedDate().toEpochMilli(), hub.getCreatedDate().toEpochMilli());
    assertEquals(hubEntity.getUntilDate().toEpochMilli(), hub.getUntilDate().toEpochMilli());
    assertEquals(hubEntity.getJoinDate().toEpochMilli(), hub.getJoinDate().toEpochMilli());
    assertEquals(hubEntity.getUpdatedDate().toEpochMilli(), hub.getUpdatedDate().toEpochMilli());
  }

  @Test
  void getHubsByRewardIdWhenEmpty() {
    when(rewardRepository.findById(rewardId)).thenReturn(Optional.empty());
    Page<Hub> hubsPage = hubService.getHubs(rewardId, Pageable.unpaged());
    assertNotNull(hubsPage);
    assertEquals(0, hubsPage.getSize());
  }

  @Test
  void getHubsByRewardIdWhenNoReports() {
    Pageable pageable = Pageable.unpaged();
    UemRewardEntity rewardEntity = newUemRewardEntity();
    when(rewardRepository.findById(rewardId)).thenReturn(Optional.of(rewardEntity));
    when(hubRepository.findByAddressInAndEnabledIsTrue(hubAddresses,
                                                       pageable)).thenReturn(new PageImpl<>(Collections.emptyList()));

    Page<Hub> hubsPage = hubService.getHubs(rewardId, pageable);
    assertNotNull(hubsPage);
    assertEquals(0, hubsPage.getSize());
  }

  @Test
  void getHubsByRewardId() {
    Pageable pageable = Pageable.unpaged();
    UemRewardEntity rewardEntity = newUemRewardEntity();
    HubEntity hubEntity = newHubEntity();
    when(rewardRepository.findById(rewardId)).thenReturn(Optional.of(rewardEntity));
    when(hubRepository.findByAddressInAndEnabledIsTrue(hubAddresses,
                                                       pageable)).thenReturn(new PageImpl<>(Collections.singletonList(hubEntity)));

    Page<Hub> hubsPage = hubService.getHubs(rewardId, pageable);
    assertNotNull(hubsPage);
    assertEquals(1, hubsPage.getSize());
  }

  @Test
  void getHubByNftWhenNotExists() throws ObjectNotFoundException {
    when(hubRepository.findByNftId(deedId)).thenReturn(Optional.empty());
    when(tenantService.getDeedTenantOrImport(deedId)).thenThrow(ObjectNotFoundException.class);
    assertNull(hubService.getHub(deedId));
  }

  @Test
  void getHubByNftWhenExistsOnBlockchain() throws ObjectNotFoundException {
    when(hubRepository.findByNftId(deedId)).thenReturn(Optional.empty());
    when(tenantService.getDeedTenantOrImport(deedId)).thenReturn(newDeedTenant());
    Hub hub = hubService.getHub(deedId);
    assertNotNull(hub);
    assertEquals(deedId, hub.getDeedId());
    assertEquals(city, hub.getCity());
    assertEquals(cardType, hub.getType());
    assertNull(hub.getColor());
    assertNull(hub.getName());
    assertNull(hub.getDescription());
    assertNull(hub.getUrl());
    assertNull(hub.getJoinDate());
    assertNull(hub.getUntilDate());
    assertNull(hub.getCreatedDate());
    assertNull(hub.getUpdatedDate());
    assertEquals(0d, hub.getOwnerClaimableAmount());
    assertEquals(0d, hub.getManagerClaimableAmount());
    assertNull(hub.getRewardsPeriodType());
    assertEquals(0d, hub.getRewardsPerPeriod());
    assertEquals(0l, hub.getUsersCount());
  }

  @Test
  void getHubByAddressWhenNoRefreshAndNotFound()  {
    when(hubRepository.findById(StringUtils.lowerCase(hubAddress))).thenReturn(Optional.empty());
    Hub hub = hubService.getHub(hubAddress);
    assertNull(hub);
  }

  @Test
  void getHubByAddressWhenNoRefresh()  {
    when(hubRepository.findById(StringUtils.lowerCase(hubAddress))).thenReturn(Optional.of(newHubEntity()));
    Hub hub = hubService.getHub(hubAddress);
    assertNotNull(hub);
  }

  @Test
  void getHubByAddressWhenRefreshAndNotFound()  {
    when(hubRepository.findById(StringUtils.lowerCase(hubAddress))).thenReturn(Optional.empty());
    Hub hub = hubService.getHub(hubAddress, true);
    assertNull(hub);
  }

  @Test
  void getHubByAddressWhenRefreshAndDisconnected()  {
    when(hubRepository.findById(StringUtils.lowerCase(hubAddress))).thenReturn(Optional.of(newHubEntity()));
    Hub hub = hubService.getHub(hubAddress, true);
    assertNotNull(hub);
    verify(listenerService).publishEvent(HUB_DISCONNECTED, hubAddress.toLowerCase());
    verify(hubRepository, atLeast(1)).save(argThat(entity -> !entity.isEnabled()));
  }

  @Test
  void getHubByAddressWhenRefreshAndExistsInBlockchainNotDisconnectWhenChangeOwner() throws ObjectNotFoundException, WomException  {
    when(hubRepository.findById(StringUtils.lowerCase(hubAddress))).thenReturn(Optional.of(newHubEntity()));
    when(hubRepository.findByNftId(deedId)).thenReturn(Optional.of(newHubEntity()));
    when(hubRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    WomHub womHub = new WomHub(deedId, hubOwnerAddress, enabled, Instant.now().minusSeconds(56).getEpochSecond());
    when(blockchainService.getHub(hubAddress)).thenReturn(womHub);
    WomDeed womDeed = new WomDeed(city, cardType, mintingPower, maxUsers, deedOwnerAddress.replace("1", "2"), deedManagerAddress, hubAddress, ownerPercentage);
    when(blockchainService.getWomDeed(deedId)).thenReturn(womDeed);
    when(tenantService.getDeedTenantOrImport(deedId)).thenReturn(newDeedTenant());
    
    assertThrows(WomAuthorizationException.class, () -> hubService.getHub(hubAddress, true));
    
    when(blockchainService.getDeedOwner(deedId)).thenReturn(deedOwnerAddress);
    when(blockchainService.isDeedOwner(deedOwnerAddress, deedId)).thenReturn(true);
    when(blockchainService.getDeedManager(deedId)).thenReturn(deedManagerAddress);
    when(blockchainService.isDeedProvisioningManager(deedManagerAddress, deedId)).thenReturn(true);
    when(blockchainService.getDeedCityIndex(deedId)).thenReturn(city);
    when(blockchainService.getDeedCardType(deedId)).thenReturn(cardType);
    DeedTenantLeaseDTO lease = mock(DeedTenantLeaseDTO.class);
    when(leaseService.getCurrentLease(deedId)).thenReturn(lease);
    when(lease.getOwnerMintingPercentage()).thenReturn((int) ownerPercentage);

    Hub hub = hubService.getHub(hubAddress, true);
    assertNotNull(hub);
    verify(listenerService).publishEvent(HUB_SAVED, hubAddress.toLowerCase());
    verify(blockchainService).updateWomDeed(deedId,
                                            city,
                                            cardType,
                                            mintingPower,
                                            maxUsers,
                                            deedOwnerAddress,
                                            deedManagerAddress,
                                            ownerPercentage);
    verify(listenerService, never()).publishEvent(HUB_CONNECTED, hubAddress.toLowerCase());
    verify(listenerService, never()).publishEvent(HUB_DISCONNECTED, hubAddress.toLowerCase());
  }

  @Test
  void getHubByAddressWhenRefreshAndExistsInBlockchainAndDisconnectWhenChangeManager() throws ObjectNotFoundException,
                                                                                       WomException {
    String newDeedManagerAddress = deedManagerAddress.replace("1", "2");

    ThreadLocal<WomDeed> womDeedTL = new ThreadLocal<>();
    womDeedTL.set(new WomDeed(city,
                              cardType,
                              mintingPower,
                              maxUsers,
                              deedOwnerAddress,
                              deedManagerAddress,
                              hubAddress,
                              ownerPercentage));
    doAnswer(invocation -> {
      womDeedTL.set(new WomDeed(invocation.getArgument(1),
                                invocation.getArgument(2),
                                invocation.getArgument(3, Short.class) / 100d,
                                invocation.getArgument(4),
                                invocation.getArgument(5),
                                invocation.getArgument(6),
                                hubAddress,
                                invocation.getArgument(7)));
      return null;
    }).when(blockchainService)
      .updateWomDeed(eq(deedId),
                     anyShort(),
                     anyShort(),
                     anyShort(),
                     anyLong(),
                     anyString(),
                     anyString(),
                     anyShort());

    ThreadLocal<HubEntity> hubEntityTL = new ThreadLocal<>();
    HubEntity hubEntity = newHubEntity();
    hubEntityTL.set(hubEntity);
    when(hubRepository.findById(StringUtils.lowerCase(hubAddress))).thenAnswer(invocation -> Optional.of(hubEntityTL.get()));
    when(hubRepository.findByNftId(deedId)).thenAnswer(invocation -> Optional.of(hubEntityTL.get()));
    when(hubRepository.save(any())).thenAnswer(invocation -> {
      HubEntity savedHubEntity = invocation.getArgument(0);
      hubEntityTL.set(savedHubEntity);
      return savedHubEntity;
    });

    WomHub womHub = new WomHub(deedId, hubOwnerAddress, enabled, Instant.now().minusSeconds(56).getEpochSecond());
    when(blockchainService.getHub(hubAddress)).thenReturn(womHub);
    when(blockchainService.getWomDeed(deedId)).thenAnswer(invocation -> womDeedTL.get());

    when(tenantService.getDeedTenantOrImport(deedId)).thenReturn(newDeedTenant());
    when(tenantService.getDeedTenant(deedId)).thenReturn(newDeedTenant());
    when(blockchainService.getDeedOwner(deedId)).thenReturn(deedOwnerAddress);
    when(blockchainService.isDeedOwner(deedOwnerAddress, deedId)).thenReturn(true);
    when(blockchainService.getDeedManager(deedId)).thenReturn(newDeedManagerAddress);
    when(blockchainService.isDeedProvisioningManager(newDeedManagerAddress, deedId)).thenReturn(true);
    when(blockchainService.getDeedCityIndex(deedId)).thenReturn(city);
    when(blockchainService.getDeedCardType(deedId)).thenReturn(cardType);
    DeedTenantLeaseDTO lease = mock(DeedTenantLeaseDTO.class);
    when(leaseService.getCurrentLease(deedId)).thenReturn(lease);
    when(lease.getOwnerMintingPercentage()).thenReturn((int) ownerPercentage);

    Hub hub = hubService.getHub(hubAddress, true);
    assertNotNull(hub);
    verify(listenerService).publishEvent(HUB_DISCONNECTED, hubAddress.toLowerCase());
    verify(blockchainService).updateWomDeed(deedId,
                                            city,
                                            cardType,
                                            mintingPower,
                                            maxUsers,
                                            deedOwnerAddress,
                                            newDeedManagerAddress,
                                            ownerPercentage);
  }

  @Test
  void getHubByAddressWhenRefreshAndExistsInBlockchainAsConnected() throws ObjectNotFoundException, WomException {
    String previousDeedManagerAddress = deedManagerAddress.replace("1", "2");

    ThreadLocal<WomDeed> womDeedTL = new ThreadLocal<>();
    womDeedTL.set(new WomDeed(city,
                              cardType,
                              mintingPower,
                              maxUsers,
                              deedOwnerAddress,
                              previousDeedManagerAddress,
                              hubAddress,
                              ownerPercentage));
    doAnswer(invocation -> {
      womDeedTL.set(new WomDeed(invocation.getArgument(1),
                                invocation.getArgument(2),
                                invocation.getArgument(3, Short.class) / 100d,
                                invocation.getArgument(4),
                                invocation.getArgument(5),
                                invocation.getArgument(6),
                                hubAddress,
                                invocation.getArgument(7)));
      return null;
    }).when(blockchainService)
      .updateWomDeed(eq(deedId),
                     anyShort(),
                     anyShort(),
                     anyShort(),
                     anyLong(),
                     anyString(),
                     anyString(),
                     anyShort());

    ThreadLocal<HubEntity> hubEntityTL = new ThreadLocal<>();
    HubEntity hubEntity = newHubEntity();
    hubEntity.setDeedManagerAddress(previousDeedManagerAddress);
    hubEntityTL.set(hubEntity);
    when(hubRepository.findById(StringUtils.lowerCase(hubAddress))).thenAnswer(invocation -> Optional.of(hubEntityTL.get()));
    when(hubRepository.findByNftId(deedId)).thenAnswer(invocation -> Optional.of(hubEntityTL.get()));
    when(hubRepository.save(any())).thenAnswer(invocation -> {
      HubEntity savedHubEntity = invocation.getArgument(0);
      hubEntityTL.set(savedHubEntity);
      return savedHubEntity;
    });

    WomHub womHub = new WomHub(deedId, hubOwnerAddress, enabled, Instant.now().minusSeconds(56).getEpochSecond());
    when(blockchainService.getHub(hubAddress)).thenReturn(womHub);
    when(blockchainService.getWomDeed(deedId)).thenAnswer(invocation -> womDeedTL.get());

    when(tenantService.getDeedTenantOrImport(deedId)).thenReturn(newDeedTenant());
    when(tenantService.getDeedTenant(deedId)).thenReturn(newDeedTenant());
    when(blockchainService.getDeedOwner(deedId)).thenReturn(deedOwnerAddress);
    when(blockchainService.isDeedOwner(deedOwnerAddress, deedId)).thenReturn(true);
    when(blockchainService.getDeedManager(deedId)).thenReturn(deedManagerAddress);
    when(blockchainService.isDeedProvisioningManager(deedManagerAddress, deedId)).thenReturn(true);
    when(blockchainService.getDeedCityIndex(deedId)).thenReturn(city);
    when(blockchainService.getDeedCardType(deedId)).thenReturn(cardType);
    DeedTenantLeaseDTO lease = mock(DeedTenantLeaseDTO.class);
    when(leaseService.getCurrentLease(deedId)).thenReturn(lease);
    when(lease.getOwnerMintingPercentage()).thenReturn((int) ownerPercentage);

    Hub hub = hubService.getHub(hubAddress, true);
    assertNotNull(hub);
    verify(listenerService).publishEvent(HUB_CONNECTED, hubAddress.toLowerCase());
    verify(blockchainService).updateWomDeed(deedId,
                                            city,
                                            cardType,
                                            mintingPower,
                                            maxUsers,
                                            deedOwnerAddress,
                                            deedManagerAddress,
                                            ownerPercentage);
  }

  @Test
  void refreshClaimableAmount() {
    HubEntity entity = newHubEntity();
    when(hubRepository.findByDeedOwnerAddress(StringUtils.lowerCase(deedOwnerAddress))).thenReturn(Stream.of(entity));
    when(hubRepository.findByDeedManagerAddress(StringUtils.lowerCase(deedManagerAddress))).thenReturn(Stream.of(entity));
    when(hubRepository.findById(StringUtils.lowerCase(hubAddress))).thenReturn(Optional.of(entity));

    double amount1 = 5.22d;
    double amount2 = 6.77d;
    when(blockchainService.getPendingRewards(deedManagerAddress)).thenReturn(amount1);
    when(blockchainService.getPendingRewards(deedOwnerAddress)).thenReturn(amount2);

    hubService.refreshClaimableAmount(deedManagerAddress);

    verify(hubRepository).save(any());
    verify(hubRepository).save(argThat(hubEntity -> hubEntity.getOwnerClaimableAmount() == amount2
                                                    && hubEntity.getManagerClaimableAmount() == amount1));
  }

  @Test
  void generateToken() {
    String token = hubService.generateToken();
    assertNotNull(token);
    String token2 = hubService.generateToken();
    assertNotNull(token);
    assertNotEquals(token, token2);
  }

  @Test
  void connectToWomWithSignatureErrors() {
    String fakeDeedManagerSignedMessage = "fakeSignedMessage";
    String fakeHubSignedMessage = "fakeHubSignedMessage";
    String rawMessage = RAW_MESSAGE;
    WomException exception = assertThrows(WomException.class,
                                          () -> {
                                            WomConnectionRequest hubConnectionRequest =
                                                                                      new WomConnectionRequest(fakeDeedManagerSignedMessage,
                                                                                                               fakeHubSignedMessage,
                                                                                                               rawMessage,
                                                                                                               null);
                                            hubService.connectToWom(hubConnectionRequest);
                                          });
    assertEquals("wom.emptyTokenForSignedMessage", exception.getMessage());
    exception = assertThrows(WomException.class,
                             () -> {
                               WomConnectionRequest hubConnectionRequest = new WomConnectionRequest(fakeDeedManagerSignedMessage,
                                                                                                    fakeHubSignedMessage,
                                                                                                    rawMessage,
                                                                                                    "token");
                               hubService.connectToWom(hubConnectionRequest);
                             });
    assertEquals("wom.invalidTokenForSignedMessage", exception.getMessage());

    String token = hubService.generateToken();
    exception = assertThrows(WomException.class,
                             () -> hubService.connectToWom(new WomConnectionRequest(fakeDeedManagerSignedMessage,
                                                                                    fakeHubSignedMessage,
                                                                                    rawMessage,
                                                                                    token)));
    assertEquals("wom.emptyDeedManagerAddress", exception.getMessage());

    exception = assertThrows(WomException.class,
                             () -> {
                               WomConnectionRequest hubConnectionRequest =
                                                                         new WomConnectionRequest(signHubMessage(rawMessage,
                                                                                                                 deedManagerCredentials.getEcKeyPair()),
                                                                                                  signHubMessage(rawMessage,
                                                                                                                 deedManagerCredentials.getEcKeyPair()),
                                                                                                  null,
                                                                                                  token);
                               hubConnectionRequest.setAddress(hubAddress);
                               hubConnectionRequest.setDeedManagerAddress(deedManagerAddress);
                               hubService.connectToWom(hubConnectionRequest);
                             });
    assertEquals("wom.emptySignedMessage", exception.getMessage());

    exception = assertThrows(WomException.class,
                             () -> {
                               WomConnectionRequest hubConnectionRequest =
                                                                         new WomConnectionRequest(signHubMessage(rawMessage,
                                                                                                                 deedManagerCredentials.getEcKeyPair()),
                                                                                                  signHubMessage(rawMessage,
                                                                                                                 deedManagerCredentials.getEcKeyPair()),
                                                                                                  rawMessage,
                                                                                                  token);
                               hubConnectionRequest.setAddress(hubAddress);
                               hubConnectionRequest.setDeedManagerAddress(deedManagerAddress);
                               hubService.connectToWom(hubConnectionRequest);
                             });
    assertEquals(WOM_INVALID_SIGNED_MESSAGE, exception.getMessage());
  }

  @Test
  void connectToWom() throws WomException, ObjectNotFoundException {
    String previousDeedManagerAddress = deedManagerAddress.replace("1", "2");

    ThreadLocal<WomDeed> womDeedTL = new ThreadLocal<>();
    womDeedTL.set(new WomDeed(city,
                              cardType,
                              mintingPower,
                              maxUsers,
                              deedOwnerAddress,
                              previousDeedManagerAddress,
                              hubAddress,
                              ownerPercentage));
    doAnswer(invocation -> {
      womDeedTL.set(new WomDeed(invocation.getArgument(1),
                                invocation.getArgument(2),
                                invocation.getArgument(3, Short.class) / 100d,
                                invocation.getArgument(4),
                                invocation.getArgument(5),
                                invocation.getArgument(6),
                                hubAddress,
                                invocation.getArgument(7)));
      return null;
    }).when(blockchainService)
      .updateWomDeed(eq(deedId),
                     anyShort(),
                     anyShort(),
                     anyShort(),
                     anyLong(),
                     anyString(),
                     anyString(),
                     anyShort());

    ThreadLocal<HubEntity> hubEntityTL = new ThreadLocal<>();
    HubEntity hubEntity = newHubEntity();
    hubEntity.setDeedManagerAddress(previousDeedManagerAddress);
    hubEntityTL.set(hubEntity);
    when(hubRepository.findById(StringUtils.lowerCase(hubAddress))).thenAnswer(invocation -> Optional.of(hubEntityTL.get()));
    when(hubRepository.findByNftId(deedId)).thenAnswer(invocation -> Optional.of(hubEntityTL.get()));
    when(hubRepository.save(any())).thenAnswer(invocation -> {
      HubEntity savedHubEntity = invocation.getArgument(0);
      hubEntityTL.set(savedHubEntity);
      return savedHubEntity;
    });

    WomHub womHub = new WomHub(deedId, hubOwnerAddress, enabled, Instant.now().minusSeconds(56).getEpochSecond());
    when(blockchainService.getHub(hubAddress)).thenReturn(womHub);
    when(blockchainService.getWomDeed(deedId)).thenAnswer(invocation -> womDeedTL.get());

    when(tenantService.getDeedTenantOrImport(deedId)).thenReturn(newDeedTenant());
    when(tenantService.getDeedTenant(deedId)).thenReturn(newDeedTenant());
    when(blockchainService.getDeedOwner(deedId)).thenReturn(deedOwnerAddress);
    when(blockchainService.isDeedOwner(deedOwnerAddress, deedId)).thenReturn(true);
    when(blockchainService.getDeedManager(deedId)).thenReturn(deedManagerAddress);
    when(blockchainService.isDeedProvisioningManager(deedManagerAddress, deedId)).thenReturn(true);
    when(blockchainService.getDeedCityIndex(deedId)).thenReturn(city);
    when(blockchainService.getDeedCardType(deedId)).thenReturn(cardType);
    DeedTenantLeaseDTO lease = mock(DeedTenantLeaseDTO.class);
    when(leaseService.getCurrentLease(deedId)).thenReturn(lease);
    when(lease.getOwnerMintingPercentage()).thenReturn((int) ownerPercentage);

    String token = hubService.generateToken();
    String rawMessage = RAW_MESSAGE + token;

    WomConnectionRequest hubConnectionRequest =
                                              new WomConnectionRequest(signHubMessage(rawMessage,
                                                                                      deedManagerCredentials.getEcKeyPair()),
                                                                       signHubMessage(rawMessage,
                                                                                      hubCredentials.getEcKeyPair()),
                                                                       rawMessage,
                                                                       token);
    hubConnectionRequest.setAddress(hubAddress);
    hubConnectionRequest.setDeedManagerAddress(deedManagerAddress);
    hubConnectionRequest.setDeedId(deedId);
    hubService.connectToWom(hubConnectionRequest);
    verify(listenerService).publishEvent(HUB_CONNECTED, hubAddress.toLowerCase());
    verify(blockchainService).updateWomDeed(deedId,
                                            city,
                                            cardType,
                                            mintingPower,
                                            maxUsers,
                                            deedOwnerAddress,
                                            deedManagerAddress,
                                            ownerPercentage);
  }

  @Test
  void disconnectToWomWithSignatureErrors() {
    String fakeDeedManagerSignedMessage = "fakeSignedMessage";
    String fakeHubSignedMessage = "fakeHubSignedMessage";
    String rawMessage = RAW_MESSAGE;
    WomException exception = assertThrows(WomException.class,
                                          () -> {
                                            WomDisconnectionRequest disconnectionRequest =
                                                                                         new WomDisconnectionRequest(hubAddress,
                                                                                                                     fakeHubSignedMessage,
                                                                                                                     deedManagerAddress,
                                                                                                                     fakeDeedManagerSignedMessage,
                                                                                                                     rawMessage,
                                                                                                                     null);
                                            hubService.disconnectFromWom(disconnectionRequest);
                                          });
    assertEquals("wom.emptyTokenForSignedMessage", exception.getMessage());
    exception = assertThrows(WomException.class,
                             () -> {
                               WomDisconnectionRequest disconnectionRequest =
                                                                            new WomDisconnectionRequest(hubAddress,
                                                                                                        fakeHubSignedMessage,
                                                                                                        deedManagerAddress,
                                                                                                        fakeDeedManagerSignedMessage,
                                                                                                        rawMessage,
                                                                                                        "token");
                               hubService.disconnectFromWom(disconnectionRequest);
                             });
    assertEquals("wom.invalidTokenForSignedMessage", exception.getMessage());

    String token = hubService.generateToken();
    exception = assertThrows(WomException.class,
                             () -> hubService.disconnectFromWom(new WomDisconnectionRequest(hubAddress,
                                                                                            fakeHubSignedMessage,
                                                                                            deedManagerAddress,
                                                                                            fakeDeedManagerSignedMessage,
                                                                                            rawMessage,
                                                                                            token)));
    assertEquals(WOM_INVALID_SIGNED_MESSAGE, exception.getMessage());

    exception = assertThrows(WomException.class,
                             () -> hubService.disconnectFromWom(new WomDisconnectionRequest(hubAddress,
                                                                                            fakeHubSignedMessage,
                                                                                            deedManagerAddress,
                                                                                            null,
                                                                                            rawMessage,
                                                                                            token)));
    assertEquals("wom.emptySignedMessage", exception.getMessage());

    exception = assertThrows(WomException.class,
                             () -> hubService.disconnectFromWom(new WomDisconnectionRequest(hubAddress,
                                                                                            signHubMessage(rawMessage,
                                                                                                           deedManagerCredentials.getEcKeyPair()),
                                                                                            deedManagerAddress,
                                                                                            signHubMessage(rawMessage,
                                                                                                           deedManagerCredentials.getEcKeyPair()),
                                                                                            rawMessage,
                                                                                            token)));
    assertEquals(WOM_INVALID_SIGNED_MESSAGE, exception.getMessage());
  }

  @Test
  void disconnectToWom() throws WomException, ObjectNotFoundException {
    ThreadLocal<WomDeed> womDeedTL = new ThreadLocal<>();
    womDeedTL.set(new WomDeed(city,
                              cardType,
                              mintingPower,
                              maxUsers,
                              deedOwnerAddress,
                              deedManagerAddress,
                              hubAddress,
                              ownerPercentage));
    doAnswer(invocation -> {
      womDeedTL.set(new WomDeed(invocation.getArgument(1),
                                invocation.getArgument(2),
                                invocation.getArgument(3, Short.class) / 100d,
                                invocation.getArgument(4),
                                invocation.getArgument(5),
                                invocation.getArgument(6),
                                hubAddress,
                                invocation.getArgument(7)));
      return null;
    }).when(blockchainService)
      .updateWomDeed(eq(deedId),
                     anyShort(),
                     anyShort(),
                     anyShort(),
                     anyLong(),
                     anyString(),
                     anyString(),
                     anyShort());

    ThreadLocal<HubEntity> hubEntityTL = new ThreadLocal<>();
    HubEntity hubEntity = newHubEntity();
    hubEntityTL.set(hubEntity);
    when(hubRepository.findById(StringUtils.lowerCase(hubAddress))).thenAnswer(invocation -> Optional.of(hubEntityTL.get()));
    when(hubRepository.findByNftId(deedId)).thenAnswer(invocation -> Optional.of(hubEntityTL.get()));
    when(hubRepository.save(any())).thenAnswer(invocation -> {
      HubEntity savedHubEntity = invocation.getArgument(0);
      hubEntityTL.set(savedHubEntity);
      return savedHubEntity;
    });

    WomHub womHub = new WomHub(deedId, hubOwnerAddress, false, Instant.now().minusSeconds(56).getEpochSecond());
    when(blockchainService.getHub(hubAddress)).thenReturn(womHub);
    when(blockchainService.getWomDeed(deedId)).thenAnswer(invocation -> womDeedTL.get());
    when(tenantService.getDeedTenantOrImport(deedId)).thenReturn(newDeedTenant());
    when(tenantService.getDeedTenant(deedId)).thenReturn(newDeedTenant());
    when(blockchainService.getDeedOwner(deedId)).thenReturn(deedOwnerAddress);
    when(blockchainService.isDeedOwner(deedOwnerAddress, deedId)).thenReturn(true);
    when(blockchainService.getDeedManager(deedId)).thenReturn(deedManagerAddress);
    when(blockchainService.isDeedProvisioningManager(deedManagerAddress, deedId)).thenReturn(true);
    when(blockchainService.getDeedCityIndex(deedId)).thenReturn(city);
    when(blockchainService.getDeedCardType(deedId)).thenReturn(cardType);
    DeedTenantLeaseDTO lease = mock(DeedTenantLeaseDTO.class);
    when(leaseService.getCurrentLease(deedId)).thenReturn(lease);
    when(lease.getOwnerMintingPercentage()).thenReturn((int) ownerPercentage);

    String token = hubService.generateToken();
    String rawMessage = RAW_MESSAGE + token;

    hubService.disconnectFromWom(new WomDisconnectionRequest(hubAddress,
                                                             signHubMessage(rawMessage,
                                                                            hubCredentials.getEcKeyPair()),
                                                             deedManagerAddress,
                                                             signHubMessage(rawMessage,
                                                                            deedManagerCredentials.getEcKeyPair()),
                                                             rawMessage,
                                                             token));
    verify(listenerService).publishEvent(HUB_DISCONNECTED, hubAddress.toLowerCase());
  }

  @Test
  void updateHubWhenNotFound() {
    assertThrows(IllegalArgumentException.class,
                 () -> {
                   String token = hubService.generateToken();
                   hubService.updateHub(new HubUpdateRequest(hubAddress,
                                                             name,
                                                             description,
                                                             url,
                                                             color,
                                                             signHubMessage(token, hubCredentials.getEcKeyPair()),
                                                             token));
                 });
  }

  @Test
  void updateHubWhenNotConnected() {
    HubEntity hubEntity = newHubEntity();
    hubEntity.setUntilDate(Instant.now().minusSeconds(5));
    when(hubRepository.findById(StringUtils.lowerCase(hubAddress))).thenReturn(Optional.of(hubEntity));
    when(hubRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    String token = hubService.generateToken();
    assertThrows(WomException.class,
                 () -> hubService.updateHub(new HubUpdateRequest(hubAddress,
                                                                 name,
                                                                 description,
                                                                 url,
                                                                 color,
                                                                 signHubMessage(token, hubCredentials.getEcKeyPair()),
                                                                 token)));
  }

  @Test
  void updateHub() throws WomException, ObjectNotFoundException {
    HubEntity oldHubEntity = newHubEntity();
    oldHubEntity.setName(null);
    oldHubEntity.setDescription(null);
    oldHubEntity.setUrl(null);
    oldHubEntity.setColor(null);
    when(hubRepository.findById(StringUtils.lowerCase(hubAddress))).thenReturn(Optional.of(oldHubEntity));
    when(hubRepository.save(any())).thenAnswer(invocation -> new HubEntity(invocation.getArgument(0, HubEntity.class)
                                                                                     .getAddress(),
                                                                           invocation.getArgument(0, HubEntity.class).getNftId(),
                                                                           invocation.getArgument(0, HubEntity.class).getCity(),
                                                                           invocation.getArgument(0, HubEntity.class).getType(),
                                                                           invocation.getArgument(0, HubEntity.class)
                                                                                     .getHubOwnerAddress(),
                                                                           invocation.getArgument(0, HubEntity.class)
                                                                                     .getDeedOwnerAddress(),
                                                                           invocation.getArgument(0, HubEntity.class)
                                                                                     .getDeedManagerAddress(),
                                                                           invocation.getArgument(0, HubEntity.class).getName(),
                                                                           invocation.getArgument(0, HubEntity.class)
                                                                                     .getDescription(),
                                                                           invocation.getArgument(0, HubEntity.class).getUrl(),
                                                                           invocation.getArgument(0, HubEntity.class).getColor(),
                                                                           invocation.getArgument(0, HubEntity.class)
                                                                                     .getAvatarId(),
                                                                           invocation.getArgument(0, HubEntity.class)
                                                                                     .getBannerId(),
                                                                           invocation.getArgument(0, HubEntity.class)
                                                                                     .getUsersCount(),
                                                                           invocation.getArgument(0, HubEntity.class)
                                                                                     .getRewardsPeriodType(),
                                                                           invocation.getArgument(0, HubEntity.class)
                                                                                     .getRewardsPerPeriod(),
                                                                           invocation.getArgument(0, HubEntity.class).isEnabled(),
                                                                           invocation.getArgument(0, HubEntity.class)
                                                                                     .getOwnerClaimableAmount(),
                                                                           invocation.getArgument(0, HubEntity.class)
                                                                                     .getManagerClaimableAmount(),
                                                                           invocation.getArgument(0, HubEntity.class)
                                                                                     .getCreatedDate(),
                                                                           invocation.getArgument(0, HubEntity.class)
                                                                                     .getUntilDate(),
                                                                           invocation.getArgument(0, HubEntity.class)
                                                                                     .getJoinDate(),
                                                                           invocation.getArgument(0, HubEntity.class)
                                                                                     .getUpdatedDate()));

    String token = hubService.generateToken();
    hubService.updateHub(new HubUpdateRequest(hubAddress,
                                              name,
                                              description,
                                              url,
                                              color,
                                              signHubMessage(token, hubCredentials.getEcKeyPair()),
                                              token));
    verify(hubRepository, atLeast(1)).save(argThat(entity -> name.equals(entity.getName())
                                                             && description.equals(entity.getDescription())
                                                             && url.equals(entity.getUrl())
                                                             && color.equals(entity.getColor())));
  }

  @Test
  void saveHubAvatarWhenInvalidSignature() {
    String token = hubService.generateToken();
    MultipartFile file = mock(MultipartFile.class);
    assertThrows(Exception.class,
                 () -> hubService.saveHubAvatar(hubAddress, token, "signature", token, file));
  }

  @Test
  void saveHubAvatarWhenNotFound() {
    String token = hubService.generateToken();
    MultipartFile file = mock(MultipartFile.class);
    assertThrows(Exception.class,
                 () -> hubService.saveHubAvatar(hubAddress,
                                                token,
                                                signHubMessage(token, hubCredentials.getEcKeyPair()),
                                                token,
                                                file));
  }

  @Test
  void saveHubAvatarWhenNoFile() {
    when(hubRepository.findById(StringUtils.lowerCase(hubAddress))).thenReturn(Optional.of(newHubEntity()));
    when(hubRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    
    String token = hubService.generateToken();
    assertThrows(IllegalArgumentException.class, () -> hubService.saveHubAvatar(hubAddress,
                             signHubMessage(token, hubCredentials.getEcKeyPair()),
                             token,
                             token,
                             null));
  }

  @Test
  void saveHubAvatarWhenSizeExceedsAllowed() {
    MultipartFile file = mock(MultipartFile.class);
    when(hubRepository.findById(StringUtils.lowerCase(hubAddress))).thenReturn(Optional.of(newHubEntity()));
    when(hubRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(file.getSize()).thenReturn(DeedFileBinary.MAX_FILE_LENGTH + 1l);

    String token = hubService.generateToken();
    assertThrows(WomRequestException.class,
                 () -> hubService.saveHubAvatar(hubAddress,
                                                signHubMessage(token, hubCredentials.getEcKeyPair()),
                                                token,
                                                token,
                                                file));
  }

  @Test
  void saveHubAvatar() throws WomException, ObjectNotFoundException, IOException {
    MultipartFile file = mock(MultipartFile.class);
    when(hubRepository.findById(StringUtils.lowerCase(hubAddress))).thenReturn(Optional.of(newHubEntity()));
    when(hubRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(fileService.saveFile(any())).thenReturn(avatarId);

    String token = hubService.generateToken();
    hubService.saveHubAvatar(hubAddress,
                             signHubMessage(token, hubCredentials.getEcKeyPair()),
                             token,
                             token,
                             file);
    verify(fileService).saveFile(any());
    verify(hubRepository).save(any());
    verify(listenerService).publishEvent(HUB_SAVED, hubAddress.toLowerCase());
  }

  @Test
  void getHubAvatarWhenNoHub() {
    when(hubRepository.findById(StringUtils.lowerCase(hubAddress))).thenReturn(Optional.empty());
    assertThrows(Exception.class, () -> hubService.getHubAvatar(hubAddress));
  }

  @Test
  void getHubAvatarWhenNoHubAvatar() {
    when(hubRepository.findById(StringUtils.lowerCase(hubAddress))).thenReturn(Optional.of(newHubEntity()));
    assertNull(hubService.getHubAvatar(hubAddress));
  }

  @Test
  void getHubAvatarWhenNoHubAvatarFile() {
    HubEntity hubEntity = newHubEntity();
    hubEntity.setAvatarId(avatarId);
    when(hubRepository.findById(StringUtils.lowerCase(hubAddress))).thenReturn(Optional.of(hubEntity));
    assertNull(hubService.getHubAvatar(hubAddress));
  }

  @Test
  void getHubAvatar() {
    HubEntity hubEntity = newHubEntity();
    hubEntity.setAvatarId(avatarId);
    when(hubRepository.findById(StringUtils.lowerCase(hubAddress))).thenReturn(Optional.of(hubEntity));
    when(fileService.getFile(avatarId)).thenReturn(mock(FileBinary.class));
    assertNotNull(hubService.getHubAvatar(hubAddress));
  }

  @Test
  void saveHubUEMProperties() {
    HubEntity hubEntity = newHubEntity();
    when(hubRepository.findById(StringUtils.lowerCase(hubAddress))).thenReturn(Optional.of(hubEntity));
    HubReport report = new HubReport();
    report.setHubAddress(hubAddress);
    report.setUsersCount(usersCount);
    report.setPeriodType("WEEK");
    report.setHubRewardAmount(2d);

    hubService.saveHubUEMProperties(report);

    verify(hubRepository).save(hubEntity);
    verify(listenerService).publishEvent(HUB_STATUS_CHANGED, StringUtils.lowerCase(report.getHubAddress()));

    assertEquals(report.getUsersCount(), hubEntity.getUsersCount());
    assertEquals(report.getPeriodType(), hubEntity.getRewardsPeriodType());
    assertEquals(report.getHubRewardAmount(), hubEntity.getRewardsPerPeriod());
  }

  @Test
  void getManagedDeeds() throws ObjectNotFoundException {
    BigInteger ownedDeedId = BigInteger.TWO;
    BigInteger ownedLeasedDeedId = BigInteger.ONE;
    BigInteger leasedDeedId = BigInteger.TEN;

    when(blockchainService.getDeedsOwnedBy(deedManagerAddress)).thenReturn(Arrays.asList(ownedDeedId, ownedLeasedDeedId));
    when(tenantService.getDeedTenantOrImport(deedManagerAddress, ownedDeedId.longValue())).thenReturn(newDeedTenant());
    when(tenantService.getDeedTenantOrImport(deedManagerAddress, ownedLeasedDeedId.longValue())).thenReturn(newDeedTenant());
    when(tenantService.getDeedTenantOrImport(deedManagerAddress, leasedDeedId.longValue())).thenReturn(newDeedTenant());
    when(leaseService.getCurrentLease(ownedLeasedDeedId.longValue())).thenReturn(mock(DeedTenantLeaseDTO.class));

    DeedTenantLeaseDTO lease = new DeedTenantLeaseDTO();
    lease.setNftId(leasedDeedId.longValue());
    when(leaseService.getLeases(any(LeaseFilter.class),
                                any())).thenReturn(new PageImpl<>(Collections.singletonList(lease)));

    List<ManagedDeed> managedDeeds = hubService.getManagedDeeds(deedManagerAddress);
    assertNotNull(managedDeeds);
    assertEquals(2, managedDeeds.size());
    assertTrue(managedDeeds.stream().map(ManagedDeed::getNftId).toList().contains(ownedDeedId.longValue()));
    assertFalse(managedDeeds.stream().map(ManagedDeed::getNftId).toList().contains(ownedLeasedDeedId.longValue()));
    assertTrue(managedDeeds.stream().map(ManagedDeed::getNftId).toList().contains(leasedDeedId.longValue()));
  }

  @Test
  void autoConnectToWomWhenDeedNotFound() throws ObjectNotFoundException {
    when(tenantService.getDeedTenantOrImport(deedId)).thenThrow(ObjectNotFoundException.class);
    assertThrows(ObjectNotFoundException.class, () -> hubService.autoConnectHubToWom(hubAddress, deedId));
  }

  @Test
  void autoConnectToWom() throws ObjectNotFoundException, WomException {
    when(tenantService.getDeedTenantOrImport(deedId)).thenReturn(newDeedTenant());

    ThreadLocal<WomDeed> womDeedTL = new ThreadLocal<>();
    womDeedTL.set(new WomDeed());
    doAnswer(invocation -> {
      womDeedTL.set(new WomDeed(invocation.getArgument(1),
                                invocation.getArgument(2),
                                invocation.getArgument(3, Short.class) / 100d,
                                invocation.getArgument(4),
                                invocation.getArgument(5),
                                invocation.getArgument(6),
                                invocation.getArgument(7),
                                invocation.getArgument(8)));
      return null;
    }).when(blockchainService)
      .autoConnectToWom(eq(deedId),
                        anyShort(),
                        anyShort(),
                        anyShort(),
                        anyLong(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyShort());

    DeedTenantLeaseDTO lease = new DeedTenantLeaseDTO();
    lease.setNftId(deedId);
    lease.setOwnerMintingPercentage(ownerPercentage);
    when(leaseService.getCurrentLease(deedId)).thenReturn(lease);

    ThreadLocal<HubEntity> hubEntityTL = new ThreadLocal<>();
    when(hubRepository.findById(StringUtils.lowerCase(hubAddress))).thenAnswer(invocation ->hubEntityTL.get() == null ? Optional.empty() :  Optional.of(hubEntityTL.get()));
    when(hubRepository.findByNftId(deedId)).thenAnswer(invocation -> hubEntityTL.get() == null ? Optional.empty() : Optional.of(hubEntityTL.get()));
    when(hubRepository.save(any())).thenAnswer(invocation -> {
      HubEntity savedHubEntity = invocation.getArgument(0);
      hubEntityTL.set(savedHubEntity);
      return savedHubEntity;
    });

    WomHub womHub = new WomHub(deedId, hubOwnerAddress, enabled, Instant.now().minusSeconds(56).getEpochSecond());
    when(blockchainService.getHub(hubAddress)).thenReturn(womHub);
    when(blockchainService.getWomDeed(deedId)).thenAnswer(invocation -> womDeedTL.get());

    when(tenantService.getDeedTenantOrImport(deedId)).thenReturn(newDeedTenant());
    when(tenantService.getDeedTenant(deedId)).thenReturn(newDeedTenant());
    when(blockchainService.getDeedOwner(deedId)).thenReturn(deedOwnerAddress);
    when(blockchainService.isDeedOwner(deedOwnerAddress, deedId)).thenReturn(true);
    when(blockchainService.getDeedManager(deedId)).thenReturn(deedManagerAddress);
    when(blockchainService.isDeedProvisioningManager(deedManagerAddress, deedId)).thenReturn(true);
    when(blockchainService.getDeedCityIndex(deedId)).thenReturn(city);
    when(blockchainService.getDeedCardType(deedId)).thenReturn(cardType);

    hubService.autoConnectHubToWom(hubAddress, deedId);

    verify(listenerService).publishEvent(HUB_CONNECTED, hubAddress.toLowerCase());
    verify(blockchainService).autoConnectToWom(deedId,
                                               city,
                                               cardType,
                                               mintingPower,
                                               maxUsers,
                                               deedOwnerAddress,
                                               deedManagerAddress,
                                               hubAddress,
                                               ownerPercentage);
  }

  private DeedTenant newDeedTenant() {
    return new DeedTenant(deedId,
                          city,
                          cardType,
                          deedOwnerAddress,
                          deedManagerAddress,
                          deedManagerEmail,
                          txHash,
                          txHash,
                          TenantProvisioningStatus.START_CONFIRMED,
                          TenantStatus.DEPLOYED,
                          true,
                          null,
                          null);
  }

  private UemRewardEntity newUemRewardEntity() {
    return new UemRewardEntity(rewardId,
                               uemRewardAmount,
                               fixedGlobalIndex,
                               fromDate(),
                               toDate(),
                               reportIds,
                               hubAddresses,
                               sumEd);
  }

  private HubEntity newHubEntity() {
    return new HubEntity(hubAddress,
                         deedId,
                         city,
                         cardType,
                         hubOwnerAddress,
                         deedOwnerAddress,
                         deedManagerAddress,
                         name,
                         description,
                         url,
                         color,
                         null,
                         bannerId,
                         usersCount,
                         rewardsPeriodType,
                         rewardsPerPeriod,
                         enabled,
                         ownerClaimableAmount,
                         managerClaimableAmount,
                         createdDate,
                         untilDate,
                         joinDate,
                         updatedDate);
  }

  private Instant toDate() {
    return Instant.ofEpochSecond(periodEndTime);
  }

  private Instant fromDate() {
    return Instant.ofEpochSecond(periodStartTime);
  }

  public String signHubMessage(String rawRequest, ECKeyPair ecKeyPair) {
    byte[] encodedRequest = rawRequest.getBytes(StandardCharsets.UTF_8);
    Sign.SignatureData signatureData = Sign.signPrefixedMessage(encodedRequest, ecKeyPair);
    byte[] retval = new byte[65];
    System.arraycopy(signatureData.getR(), 0, retval, 0, 32);
    System.arraycopy(signatureData.getS(), 0, retval, 32, 32);
    System.arraycopy(signatureData.getV(), 0, retval, 64, 1);
    return Numeric.toHexString(retval);
  }

}
