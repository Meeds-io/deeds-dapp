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
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.web3j.utils.Numeric;

import io.meeds.deeds.common.constant.AttachmentType;
import io.meeds.deeds.common.elasticsearch.model.DeedFileBinary;
import io.meeds.deeds.common.elasticsearch.model.DeedTenant;
import io.meeds.deeds.common.elasticsearch.model.HubEntity;
import io.meeds.deeds.common.elasticsearch.model.UEMRewardEntity;
import io.meeds.deeds.common.elasticsearch.storage.HubRepository;
import io.meeds.deeds.common.elasticsearch.storage.UEMRewardRepository;
import io.meeds.deeds.common.model.FileBinary;
import io.meeds.deeds.common.utils.HubMapper;
import io.meeds.wom.api.constant.ObjectNotFoundException;
import io.meeds.wom.api.constant.WomAuthorizationException;
import io.meeds.wom.api.constant.WomException;
import io.meeds.wom.api.constant.WomRequestException;
import io.meeds.wom.api.model.Hub;
import io.meeds.wom.api.model.HubReport;
import io.meeds.wom.api.model.WomConnectionRequest;
import io.meeds.wom.api.model.WomDisconnectionRequest;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;

@Component
public class HubService {

  public static final String  HUB_SAVED                  = "uem.hub.saved";

  public static final String  HUB_DISABLED               = "uem.hub.diabled";

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

  public boolean isDeedManager(String address, Long nftId) {
    return tenantService.isDeedManager(address, nftId);
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
                          DeedTenant deedTenant = tenantService.getDeedTenant(nftId);
                          if (deedTenant == null) {
                            return null;
                          } else {
                            Hub hub = new Hub();
                            hub.setDeedId(nftId);
                            hub.setCity(deedTenant.getCityIndex());
                            hub.setType(deedTenant.getCardType());
                            return hub;
                          }
                        });
  }

  public Hub getHub(String hubAddress) {
    return hubRepository.findByAddressAndEnabledIsTrue(StringUtils.lowerCase(hubAddress))
                        .map(HubMapper::fromEntity)
                        .orElse(null);
  }

  public String generateToken() {
    cleanInvalidTokens();
    if (tokens.size() >= MAX_GENERATED_TOKENS_SIZE) {
      return tokens.keySet().stream().max((k1, k2) -> (int) (tokens.get(k1) - tokens.get(k2))).orElse(null);
    }
    String token = secureRandomCodeGenerator.nextLong() + "-" + secureRandomCodeGenerator.nextLong() + "-" + secureRandomCodeGenerator.nextLong();
    tokens.put(token, System.currentTimeMillis());
    return token;
  }

  public void connectToWoM(WomConnectionRequest hubConnectionRequest) throws WomException {
    validateHubCommunityConnectionRequest(hubConnectionRequest);
    saveHub(hubConnectionRequest);
  }

  public void disconnectFromWoM(WomDisconnectionRequest hubConnectionRequest) throws WomException {
    validateHubCommunityDisonnectionRequest(hubConnectionRequest);
    Hub hub = getHub(hubConnectionRequest.getHubAddress());
    if (hub != null) {
      if (isHubManagerValid(hub)) {
        checkDeedManager(hubConnectionRequest.getDeedManagerAddress(), hub.getDeedId());
      } else {
        // Disconnect anyway, due to the fact that the previous manager isn't
        // connected, thus the earner address has to change as well
      }
      disableDeedHubCommunity(hubConnectionRequest.getHubAddress());
    } else {
      throw new WomRequestException("wom.alreadyDisconnected");
    }
  }

  public void saveHubAvatar(String hubAddress,
                            String signedMessage,
                            String rawMessage,
                            String token,
                            MultipartFile file) throws ObjectNotFoundException,
                                                WomException,
                                                IOException {
    validateSignedHubManagerMessage(hubAddress,
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
    validateSignedHubManagerMessage(hubAddress,
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

  public boolean isHubManagerValid(Hub hub) {
    // checks whether current manager of hub is still always the same
    return StringUtils.isBlank(hub.getDeedManagerAddress())
           || isDeedManager(hub.getDeedManagerAddress(), hub.getDeedId());
  }

  public void disableDeedHubCommunity(String hubAddress) {
    hubRepository.findById(StringUtils.lowerCase(hubAddress))
                 .ifPresent(hubEntity -> {
                   if (hubEntity.isEnabled()) {
                     String avatarId = hubEntity.getAvatarId();
                     String bannerId = hubEntity.getBannerId();

                     hubEntity.setEnabled(false);
                     hubEntity.setAvatarId(null);
                     hubEntity.setBannerId(null);
                     hubEntity = hubRepository.save(hubEntity);

                     if (StringUtils.isNotBlank(avatarId)) {
                       fileService.removeFile(avatarId);
                     }
                     if (StringUtils.isNotBlank(bannerId)) {
                       fileService.removeFile(bannerId);
                     }
                     listenerService.publishEvent(HUB_DISABLED, hubEntity.getAddress());
                   }
                 });
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

  public void saveHubManagerAddress(String hubAddress, String managerAddress) {
    hubRepository.findById(StringUtils.lowerCase(hubAddress))
                 .ifPresent(hubEntity -> {
                   hubEntity.setDeedManagerAddress(managerAddress);
                   hubEntity = hubRepository.save(hubEntity);
                   listenerService.publishEvent(HUB_SAVED, hubEntity.getAddress());
                 });
  }

  public void saveHubOwnerAddress(String hubAddress, String ownerAddress) {
    hubRepository.findById(StringUtils.lowerCase(hubAddress))
                 .ifPresent(hubEntity -> {
                   hubEntity.setOwnerAddress(ownerAddress);
                   hubEntity = hubRepository.save(hubEntity);
                   listenerService.publishEvent(HUB_SAVED, hubEntity.getAddress());
                 });
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

  private void validateSignedHubManagerMessage(String hubAddress,
                                               String signedMessage,
                                               String rawMessage,
                                               String token) throws ObjectNotFoundException, WomException {
    Hub hub = getHub(hubAddress);
    if (hub == null) {
      throw new ObjectNotFoundException("wom.hubDoesNotExist");
    }
    checkTokenInMessage(token);
    checkSignedMessage(hub.getDeedManagerAddress(),
                       signedMessage,
                       rawMessage,
                       token);

    checkDeedManager(hub.getDeedManagerAddress(),
                     hub.getDeedId());
  }

  private void validateHubCommunityConnectionRequest(WomConnectionRequest hubConnectionRequest) throws WomException {
    checkTokenInMessage(hubConnectionRequest.getToken());
    checkDeedManager(hubConnectionRequest.getDeedManagerAddress(),
                     hubConnectionRequest.getDeedId());
    checkDeedNotUsed(hubConnectionRequest);
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
  }

  private void saveHub(Hub hub) {
    HubEntity existingDeedHub = hubRepository.findById(StringUtils.lowerCase(hub.getAddress()))
                                             .orElseGet(HubEntity::new);
    DeedTenant deedTenant = tenantService.getDeedTenant(hub.getDeedId());
    HubEntity hubEntity = toEntity(hub,
                                   deedTenant,
                                   existingDeedHub);
    hubEntity.setUpdatedDate(Instant.now());
    hubEntity.setEnabled(true);
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

  private void checkSignedMessage(String deedManagerAddress,
                                  String signedMessage,
                                  String rawMessage,
                                  String token) throws WomException {
    if (StringUtils.isBlank(deedManagerAddress)) {
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
      if (!recoveredAddress.equalsIgnoreCase(deedManagerAddress)) {
        throw new WomAuthorizationException(WOM_INVALID_SIGNED_MESSAGE);
      }
    } catch (WomException e) {
      throw e;
    } catch (Exception e) {
      throw new WomAuthorizationException(WOM_INVALID_SIGNED_MESSAGE, e);
    }
  }

  private void checkDeedNotUsed(WomConnectionRequest hubConnectionRequest) throws WomException {
    Hub hub = getHub(hubConnectionRequest.getDeedId());
    if (hub != null) {
      if (!isHubManagerValid(hub)) {
        disableDeedHubCommunity(hub.getAddress());
      }
      if (hubRepository.existsByNftIdAndAddressNotAndEnabledIsTrue(hubConnectionRequest.getDeedId(),
                                                                   StringUtils.lowerCase(hubConnectionRequest.getAddress()))) {
        throw new WomRequestException("wom.deedAlreadyUsedByAHub");
      }
    }
  }

  private void checkDeedManager(String deedManagerAddress, long deedId) throws WomException {
    if (!isDeedManager(deedManagerAddress, deedId)) {
      throw new WomAuthorizationException("wom.notDeedManager");
    }
  }

  private void cleanInvalidTokens() {
    tokens.entrySet().removeIf(entry -> (entry.getValue() - System.currentTimeMillis()) > MAX_GENERATED_TOKENS_LT);
  }

}
