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
import java.util.stream.Stream;

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
import io.meeds.deeds.common.elasticsearch.model.HubReportEntity;
import io.meeds.deeds.common.elasticsearch.model.UemRewardEntity;
import io.meeds.deeds.common.elasticsearch.storage.HubReportRepository;
import io.meeds.deeds.common.elasticsearch.storage.HubRepository;
import io.meeds.deeds.common.elasticsearch.storage.UemRewardRepository;
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
import io.meeds.wom.api.model.HubUpdateRequest;
import io.meeds.wom.api.model.WomConnectionRequest;
import io.meeds.wom.api.model.WomConnectionResponse;
import io.meeds.wom.api.model.WomDisconnectionRequest;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;

@Component
public class HubService {

  private static final Logger LOG                        = LoggerFactory.getLogger(HubService.class);

  public static final String  HUB_SAVED                  = "uem.hub.saved";

  public static final String  HUB_CONNECTED              = "uem.hub.connectedToWom";

  public static final String  HUB_DISCONNECTED           = "uem.hub.disconnectedFromWom";

  public static final String  HUB_STATUS_CHANGED         = "uem.hub.status.changed";

  public static final String  WOM_INVALID_SIGNED_MESSAGE = "wom.invalidSignedMessage";

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
  private BlockchainService   blockchainService;

  @Autowired
  private LeaseService        leaseService;

  @Autowired
  private HubRepository       hubRepository;

  @Autowired
  private HubReportRepository reportRepository;

  @Autowired
  private UemRewardRepository rewardRepository;

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
    return getHubs(0, pageable);
  }

  public Page<Hub> getHubs(long rewardId, Pageable pageable) {
    pageable = pageable.isUnpaged() ? pageable :
                                    PageRequest.of(pageable.getPageNumber(),
                                                   pageable.getPageSize(),
                                                   pageable.getSortOr(Sort.by(Direction.DESC, "createdDate")));
    Page<HubEntity> page;
    if (rewardId == 0) {
      page = hubRepository.findByEnabledIsTrue(pageable);
    } else {
      UemRewardEntity rewardEntity = rewardRepository.findById(rewardId).orElse(null);
      if (rewardEntity == null || CollectionUtils.isEmpty(rewardEntity.getHubAddresses())) {
        return Page.empty(pageable);
      } else {
        page = hubRepository.findByAddressInAndEnabledIsTrue(rewardEntity.getHubAddresses(), pageable);
      }
    }
    return page.map(HubMapper::fromEntity);
  }

  public Hub getHub(Long nftId) {
    return hubRepository.findByNftId(nftId)
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
  public Hub getHub(String hubAddress, boolean forceRefresh) {
    if (forceRefresh) {
      Hub hub = refreshHubFromWom(hubAddress);
      if (hub != null) {
        refreshHubClaimableAmount(hubAddress);
      }
    }
    HubEntity hubEntity = hubRepository.findById(StringUtils.lowerCase(hubAddress)).orElse(null);
    return HubMapper.fromEntity(hubEntity);
  }

  @SneakyThrows
  public Hub refreshHubFromWom(String hubAddress) { // NOSONAR
    LOG.info("Update Hub {} from the WoM (Polygon Blockchain)", hubAddress);
    HubEntity hubEntity = hubRepository.findById(StringUtils.lowerCase(hubAddress)).orElse(null);
    boolean previouslyConnected = HubMapper.isConnected(hubEntity);
    WomHub hubFromWom = blockchainService.getHub(hubAddress);
    boolean hubExistsInWom = hubFromWom != null && !StringUtils.equals(hubFromWom.getOwner(), EnsUtils.EMPTY_ADDRESS);
    if (hubExistsInWom) {
      long deedId = hubFromWom.getDeedId();
      WomDeed womDeed = blockchainService.getWomDeed(deedId);
      if (StringUtils.isBlank(womDeed.getManagerAddress())
          || StringUtils.isBlank(womDeed.getOwnerAddress())
          || StringUtils.equals(EnsUtils.EMPTY_ADDRESS, womDeed.getManagerAddress())
          || StringUtils.equals(EnsUtils.EMPTY_ADDRESS, womDeed.getOwnerAddress())
          || !blockchainService.isDeedProvisioningManager(womDeed.getManagerAddress(), deedId)
          || !blockchainService.isDeedOwner(womDeed.getOwnerAddress(), deedId)) {
        boolean refreshed = updateDeedOnWom(deedId);
        if (refreshed) {
          womDeed = blockchainService.getWomDeed(deedId);
        }
      }

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
      hubEntity.setDeedOwnerAddress(womDeed.getOwnerAddress());
      hubEntity.setDeedManagerAddress(womDeed.getManagerAddress());
      hubEntity.setCity(womDeed.getCity());
      hubEntity.setType(womDeed.getCardType());
      hubEntity.setJoinDate(Instant.ofEpochSecond(hubFromWom.getJoinDate()));
      DeedTenantLeaseDTO lease = leaseService.getCurrentLease(deedId);
      if (lease == null) {
        hubEntity.setUntilDate(null);
      } else {
        hubEntity.setUntilDate(lease.getEndDate());
      }
      hubEntity = hubRepository.save(hubEntity);
      listenerService.publishEvent(HUB_SAVED, StringUtils.lowerCase(hubEntity.getAddress()));
    } else if (hubEntity != null) {
      hubEntity.setEnabled(false);
      hubEntity = hubRepository.save(hubEntity);
    }
    boolean connected = HubMapper.isConnected(hubEntity);
    if (connected != previouslyConnected) {
      if (connected) {
        listenerService.publishEvent(HUB_CONNECTED, StringUtils.lowerCase(hubAddress));
      } else {
        listenerService.publishEvent(HUB_DISCONNECTED, StringUtils.lowerCase(hubAddress));
      }
    }
    return HubMapper.fromEntity(hubEntity);
  }

  public void refreshClaimableAmount(String address) {
    Stream.of(hubRepository.findByDeedOwnerAddress(StringUtils.lowerCase(address)),
              hubRepository.findByDeedManagerAddress(StringUtils.lowerCase(address)))
          .flatMap(s -> s)
          .map(HubEntity::getAddress)
          .distinct()
          .forEach(this::refreshHubClaimableAmount);
  }

  public String generateToken() {
    cleanInvalidTokens();
    if (tokens.size() >= MAX_GENERATED_TOKENS_SIZE) {
      throw new IllegalStateException("Too much Tokens generated in a small time");
    }
    String token = String.format("%s-%s-%s",
                                 secureRandomCodeGenerator.nextLong(),
                                 secureRandomCodeGenerator.nextLong(),
                                 secureRandomCodeGenerator.nextLong());
    tokens.put(token, System.currentTimeMillis());
    return token;
  }

  public WomConnectionResponse connectToWom(WomConnectionRequest hubConnectionRequest) throws WomException {
    LOG.info("Hub connection {} initiation with Deed Id {} to the WoM",
             hubConnectionRequest.getAddress(),
             hubConnectionRequest.getDeedId());
    validateHubCommunityConnectionRequest(hubConnectionRequest);
    updateDeedOnWom(hubConnectionRequest.getDeedId(),
                    null,
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
    updateDeedOnWom(hub.getDeedId());
    refreshHubFromWom(hubDisconnectionRequest.getHubAddress());
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

    Hub savedHub = getHub(hubAddress);
    if (savedHub == null) {
      throw new WomException("wom.hubNotConnectedToWoM");
    } else if (!savedHub.isConnected()) {
      savedHub = getHub(hubAddress, true);
      if (savedHub == null || !savedHub.isConnected()) {
        throw new WomException("wom.hubNotConnectedToWoM");
      }
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

  public FileBinary getHubAvatar(String hubAddress) {
    String avatarId = getAvatarId(hubAddress);
    if (StringUtils.isNotBlank(avatarId)) {
      return fileService.getFile(avatarId);
    } else {
      return null;
    }
  }

  public void refreshHubUemProperties(String hubAddress) {
    hubAddress = StringUtils.lowerCase(hubAddress);
    Page<HubReportEntity> page = reportRepository.findByHubAddress(hubAddress,
                                                                   PageRequest.of(0, 1, Sort.by(Direction.DESC, "sentDate")));
    HubReportEntity reportEntity = page.stream()
                                       .findFirst()
                                       .orElse(null);
    if (reportEntity != null) {
      hubRepository.findById(hubAddress)
                   .ifPresent(hubEntity -> {
                     hubEntity.setUsersCount(reportEntity.getUsersCount());
                     hubEntity.setRewardsPeriodType(reportEntity.getPeriodType());
                     hubEntity.setRewardsPerPeriod(reportEntity.getHubRewardAmount());
                     hubEntity = hubRepository.save(hubEntity);
                     listenerService.publishEvent(HUB_STATUS_CHANGED, StringUtils.lowerCase(reportEntity.getHubAddress()));
                   });
    }
  }

  public List<ManagedDeed> getManagedDeeds(String address) {
    List<ManagedDeed> result = new ArrayList<>();
    addOwnedDeeds(address, result);
    addLeasedDeeds(address, result);
    return result;
  }

  public boolean updateDeedOnWom(long deedId) throws ObjectNotFoundException, WomException {
    return updateDeedOnWom(deedId, false);
  }

  public boolean updateDeedOnWom(long deedId, boolean forceRefresh) throws ObjectNotFoundException, WomException {
    DeedTenant deedTenant = tenantService.getDeedTenantOrImport(deedId);
    return updateDeedOnWom(deedId,
                           null,
                           deedTenant.getOwnerAddress(),
                           deedTenant.getManagerAddress(),
                           forceRefresh);
  }

  public void autoConnectHubToWom(String hubAddress, long deedId) throws ObjectNotFoundException, WomException {
    DeedTenant deedTenant = tenantService.getDeedTenantOrImport(deedId);
    updateDeedOnWom(deedId,
                    hubAddress,
                    deedTenant.getOwnerAddress(),
                    deedTenant.getManagerAddress());
    refreshHubFromWom(hubAddress);
  }

  public void transferDeedOwner(long deedId, String previousOwner, String newOwner) throws WomException {
    if (blockchainService.isDeedOwner(newOwner, deedId)) {
      updateDeedOnWom(deedId,
                      null,
                      newOwner,
                      previousOwner);
      Hub hub = getHub(deedId);
      if (hub != null
          && StringUtils.isNotBlank(hub.getAddress())
          && !StringUtils.equals(hub.getAddress(), EnsUtils.EMPTY_ADDRESS)) {
        refreshHubFromWom(hub.getAddress());
        refreshHubClaimableAmount(hub.getAddress());
      }
    }
  }

  public void transferDeedManager(long deedId, String previousManager, String newManager) throws WomException {
    if (blockchainService.isDeedProvisioningManager(newManager, deedId)) {
      updateDeedOnWom(deedId, null, previousManager, newManager);
      Hub hub = getHub(deedId);
      if (hub != null
          && StringUtils.isNotBlank(hub.getAddress())
          && !StringUtils.equals(hub.getAddress(), EnsUtils.EMPTY_ADDRESS)) {
        refreshHubFromWom(hub.getAddress());
        refreshHubClaimableAmount(hub.getAddress());
      }
    }
  }

  private void saveHubAttachment(String hubAddress, MultipartFile file, AttachmentType attachmentType) throws IOException,
                                                                                                       WomException {
    if (file == null) {
      throw new IllegalArgumentException("wom.fileIsMandatory");
    }
    if (file.getSize() > DeedFileBinary.MAX_FILE_LENGTH) {
      throw new WomRequestException("wom.fileTooBig");
    }
    String fileId = null;
    if (attachmentType == AttachmentType.AVATAR) { // NOSONAR
      fileId = getAvatarId(hubAddress);
    }
    String contentType =
                       StringUtils.contains(file.getContentType(), "image/") ? file.getContentType() : MediaType.IMAGE_PNG_VALUE;
    FileBinary fileBinary = new FileBinary(fileId,
                                           file.getName(),
                                           contentType,
                                           file.getInputStream(),
                                           Instant.now());
    String savedFileId = fileService.saveFile(fileBinary);
    HubEntity hubEntity = hubRepository.findById(StringUtils.lowerCase(hubAddress)).orElseThrow();
    if (attachmentType == AttachmentType.AVATAR) { // NOSONAR
      hubEntity.setAvatarId(savedFileId);
    }
    hubEntity.setUpdatedDate(Instant.now());
    hubEntity = hubRepository.save(hubEntity);
    listenerService.publishEvent(HUB_SAVED, StringUtils.lowerCase(hubEntity.getAddress()));
  }

  private String getAvatarId(String hubAddress) {
    HubEntity deedHub = hubRepository.findById(StringUtils.lowerCase(hubAddress)).orElseThrow();
    return deedHub.getAvatarId();
  }

  private void refreshHubClaimableAmount(String hubAddress) {
    HubEntity hubEntity = hubRepository.findById(StringUtils.lowerCase(hubAddress)).orElseThrow();
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
    hubRepository.save(hubEntity);
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
  }

  private void validateHubCommunityDisonnectionRequest(WomDisconnectionRequest hubConnectionRequest) throws WomException {
    checkTokenInMessage(hubConnectionRequest.getToken());
    checkSignedMessage(hubConnectionRequest.getDeedManagerAddress(),
                       hubConnectionRequest.getSignedMessage(),
                       hubConnectionRequest.getRawMessage(),
                       hubConnectionRequest.getToken());
    checkSignedMessage(hubConnectionRequest.getHubAddress(),
                       hubConnectionRequest.getHubSignedMessage(),
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
  private boolean updateDeedOnWom(long deedId, // NOSONAR
                                  String hubAddress,
                                  String potentialOwnerAddress,
                                  String potentialManagerAddress) throws WomException {
    return updateDeedOnWom(deedId, hubAddress, potentialOwnerAddress, potentialManagerAddress, false);
  }

  @SneakyThrows
  private boolean updateDeedOnWom(long deedId, // NOSONAR
                                  String hubAddress,
                                  String potentialOwnerAddress,
                                  String potentialManagerAddress,
                                  boolean forceRefresh) throws WomException {
    DeedTenantLeaseDTO lease = leaseService.getCurrentLease(deedId);
    short ownerMintingPercentage = lease == null ? 100 : (short) lease.getOwnerMintingPercentage();
    String ownerAddress = getDeedOwnerAddress(deedId, potentialOwnerAddress);
    String managerAddress = getDeedManagerAddress(deedId, potentialManagerAddress, ownerAddress);

    long start = System.currentTimeMillis();
    validateDeedCharacteristics(deedId, ownerAddress, managerAddress);
    if (StringUtils.isBlank(hubAddress) || StringUtils.equals(hubAddress, EnsUtils.EMPTY_ADDRESS)) {
      WomDeed woMDeed = blockchainService.getWomDeed(deedId);
      if (forceRefresh
          || woMDeed == null
          || !StringUtils.equalsIgnoreCase(woMDeed.getOwnerAddress(), ownerAddress)
          || !StringUtils.equalsIgnoreCase(woMDeed.getManagerAddress(), managerAddress)
          || woMDeed.getOwnerPercentage() != ownerMintingPercentage
          || StringUtils.isBlank(hubAddress)) {
        LOG.info("Sending Deed Update Transaction on Blockchain for Deed NFT #{} owner {} and manager {}",
                 deedId,
                 ownerAddress,
                 managerAddress);
        short city = blockchainService.getDeedCityIndex(deedId);
        short cardType = blockchainService.getDeedCardType(deedId);
        DeedCard deedCard = DeedCard.values()[cardType];
        short mintingPower = (short) (deedCard.getMintingPower() * 100);
        long maxUsers = deedCard.getMaxUsers();
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
        return true;
      }
    } else {
      LOG.info("Sending Deed Auto Connect Transaction for Hub {} on Blockchain using Deed NFT #{} with owner {} and manager {}",
               hubAddress,
               deedId,
               ownerAddress,
               managerAddress);
      short city = blockchainService.getDeedCityIndex(deedId);
      short cardType = blockchainService.getDeedCardType(deedId);
      DeedCard deedCard = DeedCard.values()[cardType];
      short mintingPower = (short) (deedCard.getMintingPower() * 100);
      long maxUsers = deedCard.getMaxUsers();

      blockchainService.autoConnectToWom(deedId,
                                         city,
                                         cardType,
                                         mintingPower,
                                         maxUsers,
                                         ownerAddress,
                                         managerAddress,
                                         hubAddress,
                                         ownerMintingPercentage);
      LOG.info("Sent Deed Auto Connect Transaction on Blockchain for Hub {}, Deed NFT #{}, owner {} and manager {} in {}ms",
               hubAddress,
               deedId,
               ownerAddress,
               managerAddress,
               System.currentTimeMillis() - start);
      return true;
    }
    return false;
  }

  private void validateDeedCharacteristics(long deedId,
                                           String deedOwnerAddress,
                                           String deedManagerAddress) throws WomException {
    checkDeedOwner(deedOwnerAddress, deedId);
    checkDeedManager(deedManagerAddress, deedId);
  }

  @SneakyThrows
  private String getDeedOwnerAddress(long deedId, String potentialDeedOwner) { // NOSONAR
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
    if (ownerAddress != null
        && !StringUtils.equalsIgnoreCase(ownerAddress, EnsUtils.EMPTY_ADDRESS)
        && !StringUtils.equalsIgnoreCase(ownerAddress, deedTenant.getOwnerAddress())) {
      deedTenant.setOwnerAddress(ownerAddress);
      tenantService.saveDeedTenant(deedTenant);
    }
    return ownerAddress;
  }

  @SneakyThrows
  private String getDeedManagerAddress(long deedId, // NOSONAR
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
    if (managerAddress != null
        && !StringUtils.equalsIgnoreCase(managerAddress, EnsUtils.EMPTY_ADDRESS)
        && !StringUtils.equalsIgnoreCase(managerAddress, deedTenant.getManagerAddress())) {
      deedTenant.setManagerAddress(managerAddress);
      tenantService.saveDeedTenant(deedTenant);
    }
    return managerAddress;
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
             if (leaseService.getCurrentLease(deedId) != null) {
               return null;
             }
             return new ManagedDeed(deedId,
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
    leasePage.get()
             .map(lease -> {
               long deedId = lease.getNftId();
               DeedTenant deedTenant = getDeedTenant(address, deedId);
               return new ManagedDeed(deedId,
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

  @SneakyThrows
  private DeedTenant getDeedTenant(String address, long deedId) {
    return tenantService.getDeedTenantOrImport(address, deedId);
  }

  private boolean isConnected(long deedId) {
    return hubRepository.findByNftId(deedId)
                        .map(HubMapper::isConnected)
                        .orElse(false);
  }

}
