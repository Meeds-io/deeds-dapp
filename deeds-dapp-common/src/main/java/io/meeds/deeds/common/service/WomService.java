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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.deeds.common.service;

import static io.meeds.deeds.common.utils.HubMapper.toEntity;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Sign.SignatureData;
import org.web3j.utils.EnsUtils;
import org.web3j.utils.Numeric;

import io.meeds.deeds.common.constant.AttachmentType;
import io.meeds.deeds.common.constant.DeedCard;
import io.meeds.deeds.common.constant.DeedCity;
import io.meeds.deeds.common.elasticsearch.model.DeedFileBinary;
import io.meeds.deeds.common.elasticsearch.model.DeedTenant;
import io.meeds.deeds.common.elasticsearch.model.HubEntity;
import io.meeds.deeds.common.elasticsearch.model.UEMRewardEntity;
import io.meeds.deeds.common.elasticsearch.storage.HubRepository;
import io.meeds.deeds.common.elasticsearch.storage.UEMRewardRepository;
import io.meeds.deeds.common.model.DeedTenantLeaseDTO;
import io.meeds.deeds.common.model.FileBinary;
import io.meeds.deeds.common.model.LeaseFilter;
import io.meeds.deeds.common.model.ManagedDeed;
import io.meeds.deeds.common.model.WomDeed;
import io.meeds.deeds.common.model.WomHub;
import io.meeds.deeds.common.utils.HubMapper;
import io.meeds.wom.api.constant.ObjectNotFoundException;
import io.meeds.wom.api.constant.WomAuthorizationException;
import io.meeds.wom.api.constant.WomException;
import io.meeds.wom.api.constant.WomRequestException;
import io.meeds.wom.api.model.Hub;
import io.meeds.wom.api.model.HubReport;
import io.meeds.wom.api.model.HubUpdateRequest;
import io.meeds.wom.api.model.WomConnectionRequest;
import io.meeds.wom.api.model.WomConnectionResponse;
import io.meeds.wom.api.model.WomDisconnectionRequest;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;

@Component
public class WomService {

  private static final Logger LOG                        = LoggerFactory.getLogger(WomService.class);

  public static final String  HUB_SAVED                  = "uem.hub.saved";

  public static final String  HUB_CONNECTED              = "uem.hub.connectedToWom";

  public static final String  HUB_DISCONNECTED           = "uem.hub.disconnectedFromWom";

  public static final String  HUB_STATUS_CHANGED         = "uem.hub.status.changed";

  private static final String WOM_INVALID_SIGNED_MESSAGE = "wom.invalidSignedMessage";

  private SecureRandom        secureRandomCodeGenerator;

  /**
   * Maximum Tokens to generate to avoid having an out of memory
   */
  private static final int    MAX_GENERATED_TOKENS_SIZE  = Integer.parseInt(System.getProperty("meeds.hub.maxTokens", "1000"));

  /**
   * 10 minutes by default for Token validity
   */
  private static final long   MAX_GENERATED_TOKENS_LT    = 1000l *
      Integer.parseInt(System.getProperty("meeds.hub.maxTokenLiveTimeSeconds",
                                          "600"));

  @Autowired
  private TenantService       tenantService;

  @Autowired
  private ListenerService     listenerService;

  @Autowired
  private FileService         fileService;

  @Autowired
  private UEMRewardRepository rewardRepository;

  @Autowired
  private BlockchainService   blockchainService;

  @Autowired
  private LeaseService        leaseService;

  @Autowired
  private HubRepository       hubRepository;

  private Map<String, Long>   tokens                     = new ConcurrentHashMap<>();

  @PostConstruct
  @SneakyThrows
  public void init() {
    try {
      secureRandomCodeGenerator = SecureRandom.getInstance("SHA1PRNG");
    } catch (NoSuchAlgorithmException e) {
      secureRandomCodeGenerator = SecureRandom.getInstanceStrong();
    }
  }

  public Page<Hub> getHubs(Pageable pageable) {
    return getHubs(null, pageable);
  }

  public Page<Hub> getHubs(String rewardId, Pageable pageable) {
    pageable = pageable.isUnpaged() ? pageable :
                                    PageRequest.of(pageable.getPageNumber(),
                                                   pageable.getPageSize(),
                                                   pageable.getSortOr(Sort.by(Direction.DESC, "createdDate")));
    Page<HubEntity> page;
    if (StringUtils.isBlank(rewardId)) {
      page = hubRepository.findByEnabledIsTrue(pageable);
    } else {
      UEMRewardEntity rewardEntity = rewardRepository.findById(rewardId).orElse(null);
      if (rewardEntity == null || CollectionUtils.isEmpty(rewardEntity.getHubAddresses())) {
        return Page.empty(pageable);
      } else {
        page = hubRepository.findByAddressInAndEnabledIsTrue(rewardEntity.getHubAddresses(), pageable);
      }
    }
    return page.map(HubMapper::fromEntity);
  }

  public Hub getHub(Long nftId) {
    return hubRepository.findByNftIdAndEnabledIsTrue(nftId)
                        .map(HubMapper::fromEntity)
                        .orElseGet(() -> {
                          try {
                            DeedTenant deedTenant = tenantService.getDeedTenantOrImport(nftId);
                            Hub hub = new Hub();
                            hub.setDeedId(nftId);
                            hub.setCity(deedTenant.getCityIndex());
                            hub.setType(deedTenant.getCardType());
                            hub.setDeedOwnerAddress(deedTenant.getOwnerAddress());
                            hub.setDeedManagerAddress(deedTenant.getManagerAddress());
                            hub.setHubOwnerAddress(StringUtils.isBlank(hub.getDeedManagerAddress()) ? hub.getDeedOwnerAddress() :
                                                                                                    hub.getDeedManagerAddress());
                            return hub;
                          } catch (ObjectNotFoundException e) {
                            return null;
                          }
                        });
  }

  public Hub getHub(String hubAddress) {
    return getHub(hubAddress, false);
  }

  @SneakyThrows
  public Hub getHub(String hubAddress, boolean forceRefresh) { // NOSONAR
    if (forceRefresh) {
      return refreshHubFromWom(hubAddress);
    } else {
      HubEntity hubEntity = hubRepository.findById(StringUtils.lowerCase(hubAddress)).orElse(null);
      return HubMapper.fromEntity(hubEntity);
    }
  }

  public Hub refreshHubFromWom(String hubAddress) { // NOSONAR
    LOG.info("Update Hub {} from the WoM (Polygon Blockchain)", hubAddress);
    HubEntity hubEntity = hubRepository.findById(StringUtils.lowerCase(hubAddress)).orElse(null);
    boolean previouslyEnabled = hubEntity != null && hubEntity.isEnabled()
                                && (hubEntity.getUntilDate() == null || hubEntity.getUntilDate().isAfter(Instant.now()));
    WomHub hubFromWom = blockchainService.getHub(hubAddress);
    boolean hubExistsInWom = hubFromWom != null && !StringUtils.equals(hubFromWom.getOwner(), EnsUtils.EMPTY_ADDRESS);
    if (hubExistsInWom) {
      long deedId = hubFromWom.getDeedId();
      DeedTenant deedTenant = getDeedTenant(hubFromWom.getOwner(), hubFromWom.getDeedId());

      if (hubEntity == null) {
        hubEntity = new HubEntity();
        hubEntity.setAddress(StringUtils.lowerCase(hubAddress));
        hubEntity.setCreatedDate(Instant.now());
        hubEntity.setUpdatedDate(hubEntity.getCreatedDate());
      } else {
        hubEntity.setUpdatedDate(Instant.now());
      }
      hubEntity.setNftId(deedId);
      hubEntity.setEnabled(hubFromWom.isEnabled());
      hubEntity.setHubOwnerAddress(hubFromWom.getOwner());
      hubEntity.setCity(deedTenant.getCityIndex()); // NOSONAR
      hubEntity.setType(deedTenant.getCardType());
      WomDeed womDeed = blockchainService.getWomDeed(deedId);
      if (womDeed != null
          && !StringUtils.equals(womDeed.getOwnerAddress(), EnsUtils.EMPTY_ADDRESS)) {
        hubEntity.setDeedOwnerAddress(womDeed.getOwnerAddress());
      } else if (!StringUtils.equalsIgnoreCase(deedTenant.getOwnerAddress(), hubEntity.getDeedOwnerAddress())
                 && blockchainService.isDeedOwner(deedTenant.getOwnerAddress(), deedId)) {
        hubEntity.setDeedOwnerAddress(deedTenant.getOwnerAddress());
      }
      if (womDeed != null
          && !StringUtils.equals(womDeed.getManagerAddress(), EnsUtils.EMPTY_ADDRESS)) {
        hubEntity.setDeedManagerAddress(womDeed.getManagerAddress());
      } else if (!StringUtils.equalsIgnoreCase(deedTenant.getManagerAddress(), hubEntity.getDeedManagerAddress())
                 && blockchainService.isDeedProvisioningManager(deedTenant.getManagerAddress(), deedId)) {
        hubEntity.setDeedManagerAddress(deedTenant.getManagerAddress());
      }
      if (!StringUtils.equalsIgnoreCase(hubEntity.getDeedManagerAddress(), hubEntity.getDeedOwnerAddress())) {
        DeedTenantLeaseDTO lease = leaseService.getCurrentLease(deedId);
        if (lease != null) {
          hubEntity.setUntilDate(lease.getEndDate());
        }
      }
      if (StringUtils.isNotBlank(hubEntity.getDeedOwnerAddress())
          && !StringUtils.equals(hubEntity.getDeedOwnerAddress(), EnsUtils.EMPTY_ADDRESS)) {
        double ownerClaimableAmount = blockchainService.getPendingRewards(hubEntity.getDeedOwnerAddress());
        hubEntity.setOwnerClaimableAmount(ownerClaimableAmount);
      }
      if (StringUtils.isNotBlank(hubEntity.getDeedManagerAddress())
          && !StringUtils.equals(hubEntity.getDeedManagerAddress(), EnsUtils.EMPTY_ADDRESS)
          && !StringUtils.equalsIgnoreCase(hubEntity.getDeedOwnerAddress(), hubEntity.getDeedManagerAddress())) {
        double managerClaimableAmount = blockchainService.getPendingRewards(hubEntity.getDeedManagerAddress());
        hubEntity.setManagerClaimableAmount(managerClaimableAmount);
      }
      hubEntity.setJoinDate(Instant.ofEpochSecond(hubFromWom.getJoinDate()));
      hubEntity = hubRepository.save(hubEntity);
      listenerService.publishEvent(HUB_SAVED, hubEntity.getAddress());
    } else if (hubEntity != null) {
      hubRepository.deleteById(hubAddress);
      hubEntity = null;
    }
    boolean enabled = hubEntity != null && hubEntity.isEnabled()
                      && (hubEntity.getUntilDate() == null || hubEntity.getUntilDate().isAfter(Instant.now()));
    if (enabled != previouslyEnabled) {
      if (enabled) {
        listenerService.publishEvent(HUB_CONNECTED, hubAddress);
      } else {
        listenerService.publishEvent(HUB_DISCONNECTED, hubAddress);
      }
    }
    return HubMapper.fromEntity(hubEntity);
  }

  public String generateToken() {
    cleanInvalidTokens();
    if (tokens.size() >= MAX_GENERATED_TOKENS_SIZE) {
      return tokens.keySet().stream().max((k1, k2) -> (int) (tokens.get(k1) - tokens.get(k2))).orElse(null);
    }
    String token = secureRandomCodeGenerator.nextLong() + "-" + secureRandomCodeGenerator.nextLong() + "-" +
        secureRandomCodeGenerator.nextLong();
    tokens.put(token, System.currentTimeMillis());
    return token;
  }

  public WomConnectionResponse connectToWom(WomConnectionRequest hubConnectionRequest) throws WomException {
    LOG.info("Hub connection {} initiation with Deed Id {} to the WoM",
             hubConnectionRequest.getAddress(),
             hubConnectionRequest.getDeedId());
    validateHubCommunityConnectionRequest(hubConnectionRequest);
    updateDeedOnWom(hubConnectionRequest.getDeedId(),
                    hubConnectionRequest.getDeedOwnerAddress(),
                    hubConnectionRequest.getDeedManagerAddress());
    saveHubProperties(hubConnectionRequest);
    return new WomConnectionResponse(hubConnectionRequest.getDeedId(),
                                     hubConnectionRequest.getAddress(),
                                     blockchainService.getWomAddress(),
                                     blockchainService.getUemAddress(),
                                     blockchainService.getPolygonNetworkId());
  }

  @SneakyThrows
  public String disconnectFromWom(WomDisconnectionRequest hubDisconnectionRequest) throws WomException {
    LOG.info("Hub disconnection {} initiation from the WoM",
             hubDisconnectionRequest.getHubAddress());
    validateHubCommunityDisonnectionRequest(hubDisconnectionRequest);
    Hub hub = getHub(hubDisconnectionRequest.getHubAddress());
    if (hub == null || !hub.isConnected()) {
      throw new WomException("wom.alreadyDisconnected");
    }
    boolean refreshed = updateDeedOnWom(hub.getDeedId());
    if (!refreshed) {
      refreshHubFromWom(hubDisconnectionRequest.getHubAddress());
    }
    return blockchainService.getWomAddress();
  }

  public void updateHub(HubUpdateRequest hubUpdateRequest) throws WomException, ObjectNotFoundException {
    Hub hub = getHub(hubUpdateRequest.getAddress());
    if (hub == null) {
      throw new IllegalArgumentException("wom.hubIsMandatory");
    }
    LOG.info("Update Hub Card {} for owner {} with Deed Id {} to the WoM",
             hub.getAddress(),
             hub.getHubOwnerAddress(),
             hub.getDeedId());
    String hubAddress = hub.getAddress();
    validateSignedHubMessage(hubAddress,
                             hubUpdateRequest.getHubSignedMessage(),
                             hubUpdateRequest.getToken(),
                             hubUpdateRequest.getToken());

    Hub savedHub = getHub(hubAddress, true);
    if (savedHub == null || !savedHub.isConnected()) {
      throw new WomException("wom.hubNotConnectedToWoM");
    }
    saveHubProperties(hubUpdateRequest);
  }

  public void saveHubAvatar(String hubAddress,
                            String signedMessage,
                            String rawMessage,
                            String token,
                            MultipartFile file) throws ObjectNotFoundException,
                                                WomException,
                                                IOException {
    validateSignedHubMessage(hubAddress,
                             signedMessage,
                             rawMessage,
                             token);
    saveHubAttachment(hubAddress, file, AttachmentType.AVATAR);
  }

  public void saveHubBanner(String hubAddress,
                            String signedMessage,
                            String rawMessage,
                            String token,
                            MultipartFile file) throws ObjectNotFoundException,
                                                WomException,
                                                IOException {
    validateSignedHubMessage(hubAddress,
                             signedMessage,
                             rawMessage,
                             token);
    saveHubAttachment(hubAddress, file, AttachmentType.BANNER);
  }

  public FileBinary getHubAvatar(String hubAddress) {
    return fileService.getFile(getAvatarId(hubAddress));
  }

  public FileBinary getHubBanner(String hubAddress) {
    return fileService.getFile(getBannerId(hubAddress));
  }

  public void saveHubUEMProperties(String hubAddress, HubReport report) {
    hubRepository.findById(StringUtils.lowerCase(hubAddress))
                 .ifPresent(hubEntity -> {
                   hubEntity.setUsersCount(report.getUsersCount());
                   hubEntity.setRewardsPeriodType(report.getPeriodType());
                   hubEntity.setRewardsPerPeriod(report.getHubRewardAmountPerPeriod());
                   hubEntity = hubRepository.save(hubEntity);
                   listenerService.publishEvent(HUB_STATUS_CHANGED, hubEntity.getAddress());
                 });
  }

  public List<ManagedDeed> getManagedDeeds(String address) {
    List<ManagedDeed> result = new ArrayList<>();
    addOwnedDeeds(address, result);
    addLeasedDeeds(address, result);
    return result;
  }

  public boolean updateDeedOnWom(long deedId) throws ObjectNotFoundException, WomException {
    DeedTenant deedTenant = tenantService.getDeedTenantOrImport(deedId);
    return updateDeedOnWom(deedId,
                           deedTenant.getOwnerAddress(),
                           deedTenant.getManagerAddress());
  }

  public void autoConnectHubToWom(String hubAddress, long deedId) throws ObjectNotFoundException, WomException {
    DeedTenant deedTenant = tenantService.getDeedTenantOrImport(deedId);
    boolean refreshed = updateDeedOnWom(deedId,
                                        hubAddress,
                                        deedTenant.getOwnerAddress(),
                                        deedTenant.getManagerAddress());
    if (!refreshed) {
      refreshHubFromWom(hubAddress);
    }
  }

  public void transferDeedOwner(long deedId, String oldOwner, String newOwner) throws WomException {
    if (blockchainService.isDeedOwner(newOwner, deedId)) {
      boolean refreshed = updateDeedOnWom(deedId, newOwner, oldOwner);
      if (!refreshed) {
        Hub hub = getHub(deedId);
        if (hub != null && !StringUtils.equals(hub.getAddress(), EnsUtils.EMPTY_ADDRESS)) {
          refreshHubFromWom(hub.getAddress());
        }
      }
    }
  }

  public void transferDeedManager(long deedId, String ownerAddress, String managerAddress) throws WomException {
    if (blockchainService.isDeedProvisioningManager(managerAddress, deedId)) {
      boolean refreshed = updateDeedOnWom(deedId, ownerAddress, managerAddress);
      if (!refreshed) {
        Hub hub = getHub(deedId);
        if (hub != null && !StringUtils.equals(hub.getAddress(), EnsUtils.EMPTY_ADDRESS)) {
          refreshHubFromWom(hub.getAddress());
        }
      }
    }
  }

  public String getDeedOwnerAddress(long deedId) {
    return getDeedOwnerAddress(deedId, true, null);
  }

  public String getDeedManagerAddress(long deedId) { // NOSONAR
    return getDeedManagerAddress(deedId, true, null, null);
  }

  @SneakyThrows
  public String getDeedOwnerAddress(long deedId, boolean updateTransaction, String potentialDeedOwner) { // NOSONAR
    DeedTenant deedTenant = tenantService.getDeedTenantOrImport(deedId);
    Hub hub = getHub(deedId);

    String ownerAddress = Arrays.asList(potentialDeedOwner,
                                        deedTenant.getOwnerAddress(),
                                        hub.getDeedOwnerAddress(),
                                        hub.getHubOwnerAddress())
                                .stream()
                                .filter(StringUtils::isNotBlank)
                                .map(StringUtils::lowerCase)
                                .collect(Collectors.toSet())
                                .stream()
                                .filter(address -> blockchainService.isDeedOwner(address, deedId))
                                .findFirst()
                                .orElse(null);
    if (ownerAddress == null) {
      ownerAddress = blockchainService.getDeedOwner(deedId);
    }
    if (ownerAddress != null) {
      if (!StringUtils.equalsIgnoreCase(ownerAddress, deedTenant.getOwnerAddress())) {
        deedTenant.setOwnerAddress(ownerAddress);
        tenantService.saveDeedTenant(deedTenant);
      }
      if (!StringUtils.equalsIgnoreCase(ownerAddress, hub.getDeedOwnerAddress())) {
        saveHub(hub);
        if (updateTransaction) {
          updateDeedOnWom(deedId, ownerAddress, null);
        }
      }
    }
    return ownerAddress;
  }

  @SneakyThrows
  public String getDeedManagerAddress(long deedId, // NOSONAR
                                      boolean updateTransaction,
                                      String potentialManagerAddress,
                                      String potentialOwnerAddress) {
    DeedTenantLeaseDTO lease = leaseService.getCurrentLease(deedId);
    DeedTenant deedTenant = tenantService.getDeedTenantOrImport(deedId);
    Hub hub = getHub(deedId);
    String managerAddress = Arrays.asList(potentialManagerAddress,
                                          potentialOwnerAddress,
                                          lease == null ? null : lease.getManagerAddress(),
                                          deedTenant.getManagerAddress(),
                                          hub.getDeedManagerAddress(),
                                          lease == null ? null : lease.getOwnerAddress(),
                                          deedTenant.getOwnerAddress(),
                                          hub.getDeedOwnerAddress(),
                                          hub.getHubOwnerAddress())
                                  .stream()
                                  .filter(StringUtils::isNotBlank)
                                  .map(StringUtils::lowerCase)
                                  .collect(Collectors.toSet())
                                  .stream()
                                  .filter(address -> blockchainService.isDeedProvisioningManager(address, deedId))
                                  .findFirst()
                                  .orElse(null);
    if (managerAddress == null) {
      managerAddress = blockchainService.getDeedManager(deedId);
    }
    if (managerAddress != null) {
      if (!StringUtils.equalsIgnoreCase(managerAddress, deedTenant.getManagerAddress())) {
        deedTenant.setManagerAddress(managerAddress);
        tenantService.saveDeedTenant(deedTenant);
      }
      if (StringUtils.isNotBlank(hub.getAddress())
          && !StringUtils.equalsIgnoreCase(managerAddress, hub.getDeedManagerAddress())) {
        saveHub(hub);
      }
      if (updateTransaction) {
        updateDeedOnWom(deedId, managerAddress, null);
      }
    }
    return managerAddress;
  }

  private void saveHubAttachment(String hubAddress, MultipartFile file, AttachmentType attachmentType) throws IOException,
                                                                                                       WomException {
    if (file == null) {
      throw new IllegalArgumentException("wom.fileIsMandatory");
    }
    if (file.getSize() > DeedFileBinary.MAX_FILE_LENGTH) {
      throw new WomRequestException("wom.fileTooBig");
    }
    if (attachmentType == null) {
      throw new IllegalArgumentException("wom.attachmentTypeIsMandatory");
    }
    String fileId = switch (attachmentType) {
    case AVATAR:
      yield getAvatarId(hubAddress);
    case BANNER:
      yield getBannerId(hubAddress);
    default:
      throw new IllegalArgumentException("wom.attachmentTypeIsUnsupported:" + attachmentType);
    };
    String contentType =
                       StringUtils.contains(file.getContentType(), "image/") ? file.getContentType() : MediaType.IMAGE_PNG_VALUE;
    FileBinary fileBinary = new FileBinary(fileId,
                                           file.getName(),
                                           contentType,
                                           file.getInputStream(),
                                           Instant.now());
    String savedFileId = fileService.saveFile(fileBinary);
    HubEntity hubEntity = hubRepository.findById(StringUtils.lowerCase(hubAddress)).orElseThrow();
    switch (attachmentType) {
    case AVATAR: {
      hubEntity.setAvatarId(savedFileId);
      break;
    }
    case BANNER: {
      hubEntity.setBannerId(savedFileId);
      break;
    }
    default:
      throw new IllegalArgumentException("wom.attachmentTypeIsUnsupported:" + attachmentType);
    }
    hubEntity.setUpdatedDate(Instant.now());
    hubEntity = hubRepository.save(hubEntity);
    listenerService.publishEvent(HUB_SAVED, hubEntity.getAddress());
  }

  private String getAvatarId(String hubAddress) {
    HubEntity deedHub = hubRepository.findByAddressAndEnabledIsTrue(StringUtils.lowerCase(hubAddress)).orElseThrow();
    return deedHub.getAvatarId();
  }

  private String getBannerId(String hubAddress) {
    HubEntity deedHub = hubRepository.findByAddressAndEnabledIsTrue(StringUtils.lowerCase(hubAddress)).orElseThrow();
    return deedHub.getBannerId();
  }

  private void validateSignedHubMessage(String hubAddress,
                                        String signedMessage,
                                        String rawMessage,
                                        String token) throws ObjectNotFoundException, WomException {
    Hub hub = getHub(hubAddress);
    if (hub == null) {
      throw new ObjectNotFoundException("wom.hubDoesNotExist");
    }
    checkTokenInMessage(token);
    checkSignedMessage(hubAddress,
                       signedMessage,
                       rawMessage,
                       token);
  }

  private void validateHubCommunityConnectionRequest(WomConnectionRequest hubConnectionRequest) throws WomException {
    checkTokenInMessage(hubConnectionRequest.getToken());
    checkSignedMessage(hubConnectionRequest.getDeedManagerAddress(),
                       hubConnectionRequest.getSignedMessage(),
                       hubConnectionRequest.getRawMessage(),
                       hubConnectionRequest.getToken());
    checkSignedMessage(hubConnectionRequest.getAddress(),
                       hubConnectionRequest.getHubSignedMessage(),
                       hubConnectionRequest.getRawMessage(),
                       hubConnectionRequest.getToken());
    checkDeedNotUsed(hubConnectionRequest.getAddress(),
                     hubConnectionRequest.getDeedId());
    validateHubOwner(hubConnectionRequest);
  }

  private void validateHubOwner(WomConnectionRequest hubConnectionRequest) throws WomException {
    String previousHubOwnerAddress = blockchainService.getHubOwner(hubConnectionRequest.getAddress());
    if (StringUtils.isNotBlank(previousHubOwnerAddress)) {
      checkHubOwner(previousHubOwnerAddress, hubConnectionRequest.getDeedManagerAddress());
      hubConnectionRequest.setHubOwnerAddress(previousHubOwnerAddress);
    } else {
      hubConnectionRequest.setHubOwnerAddress(hubConnectionRequest.getDeedManagerAddress());
    }
  }

  private void checkHubOwner(String hubOwnerAddress, String deedManagerAddress) throws WomException {
    if (StringUtils.isNotBlank(hubOwnerAddress)
        && !StringUtils.equalsIgnoreCase(hubOwnerAddress, deedManagerAddress)
        && !StringUtils.equalsIgnoreCase(hubOwnerAddress, EnsUtils.EMPTY_ADDRESS)) {
      throw new WomException("wom.onlyHubOwnerCanManageWoMConnection");
    }
  }

  private void validateHubCommunityDisonnectionRequest(WomDisconnectionRequest hubConnectionRequest) throws WomException {
    checkTokenInMessage(hubConnectionRequest.getToken());
    checkSignedMessage(hubConnectionRequest.getDeedManagerAddress(),
                       hubConnectionRequest.getSignedMessage(),
                       hubConnectionRequest.getRawMessage(),
                       hubConnectionRequest.getToken());
  }

  private void saveHubProperties(WomConnectionRequest hubConnectionRequest) {
    saveHubProperties(hubConnectionRequest.getAddress(),
                      hubConnectionRequest.getUrl(),
                      hubConnectionRequest.getColor(),
                      hubConnectionRequest.getName(),
                      hubConnectionRequest.getDescription());
  }

  private void saveHubProperties(HubUpdateRequest hubUpdateRequest) {
    saveHubProperties(hubUpdateRequest.getAddress(),
                      hubUpdateRequest.getUrl(),
                      hubUpdateRequest.getColor(),
                      hubUpdateRequest.getName(),
                      hubUpdateRequest.getDescription());
  }

  private void saveHubProperties(String address,
                                 String url,
                                 String color,
                                 Map<String, String> name,
                                 Map<String, String> description) {
    refreshHubFromWom(address);
    HubEntity existingHubEntity = hubRepository.findById(StringUtils.lowerCase(address))
                                               .orElseGet(() -> {
                                                 HubEntity hubEntity = new HubEntity();
                                                 hubEntity.setAddress(address);
                                                 return hubEntity;
                                               });
    existingHubEntity.setUrl(url);
    existingHubEntity.setColor(color);
    existingHubEntity.setName(name);
    existingHubEntity.setDescription(description);
    hubRepository.save(existingHubEntity);
  }

  private void saveHub(Hub hub) {
    refreshHubFromWom(hub.getAddress());
    HubEntity existingDeedHub = hubRepository.findById(StringUtils.lowerCase(hub.getAddress()))
                                             .orElseGet(HubEntity::new);
    DeedTenant deedTenant = tenantService.getDeedTenant(hub.getDeedId());
    DeedTenantLeaseDTO lease = leaseService.getCurrentLease(hub.getDeedId());
    HubEntity hubEntity = toEntity(hub,
                                   deedTenant,
                                   lease,
                                   existingDeedHub);
    hubEntity.setUpdatedDate(Instant.now());
    hubEntity = hubRepository.save(hubEntity);
    listenerService.publishEvent(HUB_SAVED, hubEntity.getAddress());
  }

  private void checkTokenInMessage(String token) throws WomException {
    if (StringUtils.isBlank(token)) {
      throw new WomAuthorizationException("wom.emptyTokenForSignedMessage");
    }
    cleanInvalidTokens();
    if (!tokens.containsKey(token)) {
      throw new WomAuthorizationException("wom.invalidTokenForSignedMessage");
    }
  }

  private void checkSignedMessage(String address,
                                  String signedMessage,
                                  String rawMessage,
                                  String token) throws WomException {
    if (StringUtils.isBlank(address)) {
      throw new WomAuthorizationException("wom.emptyDeedManagerAddress");
    } else if (StringUtils.isBlank(signedMessage) || StringUtils.isBlank(rawMessage)) {
      throw new WomAuthorizationException("wom.emptySignedMessage");
    } else if (!StringUtils.contains(rawMessage, token)) {
      throw new WomAuthorizationException(WOM_INVALID_SIGNED_MESSAGE);
    }

    try {
      byte[] signatureBytes = Numeric.hexStringToByteArray(signedMessage);
      if (signatureBytes.length < 64) {
        throw new WomAuthorizationException(WOM_INVALID_SIGNED_MESSAGE);
      }
      byte[] r = Arrays.copyOfRange(signatureBytes, 0, 32);
      byte[] s = Arrays.copyOfRange(signatureBytes, 32, 64);
      byte v = signatureBytes[64];
      if (v < 27) {
        v += 27;
      }

      BigInteger publicKey = Sign.signedPrefixedMessageToKey(rawMessage.getBytes(), new SignatureData(v, r, s));
      String recoveredAddress = "0x" + Keys.getAddress(publicKey);
      if (!recoveredAddress.equalsIgnoreCase(address)) {
        throw new WomAuthorizationException(WOM_INVALID_SIGNED_MESSAGE);
      }
    } catch (WomException e) {
      throw e;
    } catch (Exception e) {
      throw new WomAuthorizationException(WOM_INVALID_SIGNED_MESSAGE, e);
    }
  }

  @SneakyThrows
  private boolean updateDeedOnWom(long deedId,
                                  String potentialOwnerAddress,
                                  String potentialManagerAddress) throws WomException {
    return updateDeedOnWom(deedId, null, potentialOwnerAddress, potentialManagerAddress);
  }

  @SneakyThrows
  private boolean updateDeedOnWom(long deedId, // NOSONAR
                                  String hubAddress,
                                  String potentialOwnerAddress,
                                  String potentialManagerAddress) throws WomException {
    DeedTenantLeaseDTO lease = leaseService.getCurrentLease(deedId);
    short ownerMintingPercentage = lease == null ? 100 : (short) lease.getOwnerMintingPercentage();
    String ownerAddress = getDeedOwnerAddress(deedId, false, potentialOwnerAddress);
    String managerAddress = getDeedManagerAddress(deedId, false, potentialManagerAddress, ownerAddress);

    validateDeedCharacteristics(deedId, ownerAddress, managerAddress);
    WomDeed woMDeed = blockchainService.getWomDeed(deedId);
    if (woMDeed == null
        || !StringUtils.equals(woMDeed.getOwnerAddress(), ownerAddress)
        || !StringUtils.equals(woMDeed.getManagerAddress(), managerAddress)
        || woMDeed.getOwnerPercentage() != ownerMintingPercentage) {
      long start = System.currentTimeMillis();
      LOG.info("Sending Deed Update Transaction on Blockchain for Deed NFT #{} owner {} and manager {}",
               deedId,
               ownerAddress,
               managerAddress);
      short city = blockchainService.getDeedCityIndex(deedId);
      short cardType = blockchainService.getDeedCardType(deedId);
      DeedCard deedCard = DeedCard.values()[cardType];
      short mintingPower = (short) (deedCard.getMintingPower() * 100);
      long maxUsers = deedCard.getMaxUsers();

      if (StringUtils.isBlank(hubAddress)) {
        blockchainService.updateWomDeed(deedId,
                                        city,
                                        cardType,
                                        mintingPower,
                                        maxUsers,
                                        ownerAddress,
                                        managerAddress,
                                        ownerMintingPercentage);
        LOG.info("Sent Deed Update Transaction on Blockchain for Deed NFT #{} owner {} and manager {} in {}ms",
                 deedId,
                 ownerAddress,
                 managerAddress,
                 System.currentTimeMillis() - start);
      } else {
        blockchainService.autoConnectToWom(deedId,
                                           city,
                                           cardType,
                                           mintingPower,
                                           maxUsers,
                                           ownerAddress,
                                           managerAddress,
                                           hubAddress,
                                           ownerMintingPercentage);
        LOG.info("Sent Deed Auto Com Connection Transaction on Blockchain for Hub {}, Deed NFT #{}, owner {} and manager {} in {}ms",
                 hubAddress,
                 deedId,
                 ownerAddress,
                 managerAddress,
                 System.currentTimeMillis() - start);
      }

      Hub hub = getHub(deedId);
      if (StringUtils.isNotBlank(hubAddress)
          && !StringUtils.equals(hubAddress, EnsUtils.EMPTY_ADDRESS)) {
        if (StringUtils.isNotBlank(hub.getAddress())
            && !StringUtils.equalsIgnoreCase(hub.getAddress(), hubAddress)) {
          refreshHubFromWom(hub.getAddress()); // Changed community address,
                                               // thus refresh status from WoM
        }
        refreshHubFromWom(hubAddress);
        return true;
      } else if (hub.isConnected()) {
        refreshHubFromWom(hub.getAddress());
        return true;
      }
    }
    return false;
  }

  private void validateDeedCharacteristics(long deedId,
                                           String deedOwnerAddress,
                                           String deedManagerAddress) throws WomException {
    checkDeedOwner(deedOwnerAddress, deedId);
    checkDeedManager(deedManagerAddress, deedId);
  }

  private void checkDeedOwner(String deedOwnerAddress, long deedId) throws WomException {
    if (!blockchainService.isDeedOwner(deedOwnerAddress, deedId)) {
      throw new WomAuthorizationException("wom.notDeedOwner");
    }
  }

  private void checkDeedManager(String deedManagerAddress, long deedId) throws WomException {
    if (!blockchainService.isDeedProvisioningManager(deedManagerAddress, deedId)) {
      throw new WomAuthorizationException("wom.notDeedManager");
    }
  }

  private void checkDeedNotUsed(String hubAddress, long deedId) throws WomException {
    String previousHubAddress = blockchainService.getHubByDeedId(deedId);
    if (StringUtils.isNotBlank(previousHubAddress)
        && !StringUtils.equalsIgnoreCase(previousHubAddress, hubAddress)
        && !StringUtils.equalsIgnoreCase(previousHubAddress, EnsUtils.EMPTY_ADDRESS)) {
      throw new WomRequestException("wom.deedAlreadyUsedByAHub");
    }
  }

  private void cleanInvalidTokens() {
    tokens.entrySet().removeIf(entry -> (entry.getValue() - System.currentTimeMillis()) > MAX_GENERATED_TOKENS_LT);
  }

  private void addOwnedDeeds(String address, List<ManagedDeed> result) { // NOSONAR
    List<BigInteger> deeds = blockchainService.getDeedsOwnedBy(address);
    if (CollectionUtils.isNotEmpty(deeds)) {
      deeds.stream()
           .map(deedIdBN -> {
             long deedId = deedIdBN.longValue();
             DeedTenant deedTenant = getDeedTenant(address, deedId);
             if (deedTenant == null || leaseService.getCurrentLease(deedId) != null) {
               return null;
             }
             return new ManagedDeed(deedTenant.getNftId(),
                                    DeedCity.values()[deedTenant.getCityIndex()],
                                    DeedCard.values()[deedTenant.getCardType()],
                                    deedTenant.getTenantProvisioningStatus(),
                                    address,
                                    address,
                                    null,
                                    null,
                                    isConnected(deedId));
           })
           .filter(Objects::nonNull)
           .forEach(result::add);
    }
  }

  private void addLeasedDeeds(String address, List<ManagedDeed> result) {
    LeaseFilter leaseFilter = new LeaseFilter();
    leaseFilter.setCurrentAddress(address);
    leaseFilter.setOwner(false);
    Page<DeedTenantLeaseDTO> leasePage = leaseService.getLeases(leaseFilter, Pageable.unpaged());
    leasePage.get().map(lease -> {
      long deedId = lease.getNftId();
      DeedTenant deedTenant = getDeedTenant(address, deedId);
      if (deedTenant == null) {
        return null;
      }
      return new ManagedDeed(deedTenant.getNftId(),
                             DeedCity.values()[deedTenant.getCityIndex()],
                             DeedCard.values()[deedTenant.getCardType()],
                             deedTenant.getTenantProvisioningStatus(),
                             lease.getOwnerAddress(),
                             lease.getManagerAddress(),
                             lease.getStartDate(),
                             lease.getEndDate(),
                             isConnected(deedId));
    })
             .filter(Objects::nonNull)
             .forEach(result::add);
  }

  private DeedTenant getDeedTenant(String address, long deedId) {
    DeedTenant deedTenant;
    try {
      deedTenant = tenantService.getDeedTenantOrImport(address, deedId);
    } catch (ObjectNotFoundException e) {
      LOG.warn("Error retieving Deed NFT with id {}", deedId, e);
      return null;
    }
    return deedTenant;
  }

  private boolean isConnected(long deedId) {
    Hub hub = getHub(deedId);
    return hub != null && hub.isConnected();
  }

}
