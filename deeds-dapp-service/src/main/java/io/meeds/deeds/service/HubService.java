/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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
package io.meeds.deeds.service;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Sign.SignatureData;
import org.web3j.utils.Numeric;

import io.meeds.deeds.constant.WomConnectionException;
import io.meeds.deeds.elasticsearch.model.DeedTenant;
import io.meeds.deeds.elasticsearch.model.DeedTenantHub;
import io.meeds.deeds.model.Hub;
import io.meeds.deeds.model.WomConnectionRequest;
import io.meeds.deeds.model.WomDisconnectionRequest;
import io.meeds.deeds.storage.HubRepository;

@Component
public class HubService {

  private static final Random RANDOM                    = new Random();

  /**
   * Maximum Tokens to generate to avoid having an out of memory
   */
  private static final int    MAX_GENERATED_TOKENS_SIZE = Integer.parseInt(System.getProperty("meeds.hub.maxTokens", "1000"));

  /**
   * 10 minutes by default for Token validity
   */
  private static final long   MAX_GENERATED_TOKENS_LT   = 1000l
      * Integer.parseInt(System.getProperty("meeds.hub.maxTokenLiveTimeSeconds",
                                            "600"));

  @Autowired
  private TenantService       tenantService;

  @Autowired
  private HubRepository       hubRepository;

  private Map<String, Long>   tokens                    = new ConcurrentHashMap<>();

  public boolean isDeedManager(String address, Long nftId) {
    return tenantService.isDeedManager(address, nftId);
  }

  public Page<Hub> getHubs(Pageable pageable) {
    Page<DeedTenantHub> page = hubRepository.findByEnabledIsTrue(pageable);
    return page.map(this::toHubPresentation);
  }

  public Hub getHub(Long nftId) {
    return hubRepository.findByNftIdAndEnabledIsTrue(nftId)
                        .map(this::toHubPresentation)
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
    return hubRepository.findByHubAddressAndEnabledIsTrue(StringUtils.lowerCase(hubAddress))
                        .map(this::toHubPresentation)
                        .orElse(null);
  }

  public String generateToken() {
    cleanInvalidTokens();
    if (tokens.size() >= MAX_GENERATED_TOKENS_SIZE) {
      return tokens.keySet().stream().max((k1, k2) -> (int) (tokens.get(k1) - tokens.get(k2))).orElse(null);
    }
    String token = RANDOM.nextLong() + "-" + RANDOM.nextLong() + "-" + RANDOM.nextLong();
    tokens.put(token, System.currentTimeMillis());
    return token;
  }

  public void connectToWoM(WomConnectionRequest hubConnectionRequest) throws WomConnectionException {
    validateHubCommunityConnectionRequest(hubConnectionRequest);
    saveDeedHubCommunity(hubConnectionRequest);
  }

  public void disconnectFromWoM(WomDisconnectionRequest hubConnectionRequest) throws WomConnectionException {
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
      throw new WomConnectionException("wom.alreadyDisconnected");
    }
  }

  public boolean isHubManagerValid(Hub hub) {
    // checks whether current manager of hub is still always the same
    return StringUtils.isBlank(hub.getDeedManagerAddress())
        || isDeedManager(hub.getDeedManagerAddress(), hub.getDeedId());
  }

  public void disableDeedHubCommunity(String hubAddress) {
    hubRepository.findById(StringUtils.lowerCase(hubAddress))
                 .ifPresent(deedTenantHub -> {
                   if (deedTenantHub.isEnabled()) {
                     deedTenantHub.setEnabled(false);
                     hubRepository.save(deedTenantHub);
                   }
                 });
  }

  private void validateHubCommunityConnectionRequest(WomConnectionRequest hubConnectionRequest) throws WomConnectionException {
    checkTokenInMessage(hubConnectionRequest.getToken());
    checkSignedMessage(hubConnectionRequest.getDeedManagerAddress(),
                       hubConnectionRequest.getSignedMessage(),
                       hubConnectionRequest.getRawMessage(),
                       hubConnectionRequest.getToken());
    checkDeedManager(hubConnectionRequest.getDeedManagerAddress(),
                     hubConnectionRequest.getDeedId());
    checkDeedNotUsed(hubConnectionRequest);
  }

  private void validateHubCommunityDisonnectionRequest(WomDisconnectionRequest hubConnectionRequest) throws WomConnectionException {
    checkTokenInMessage(hubConnectionRequest.getToken());
    checkSignedMessage(hubConnectionRequest.getDeedManagerAddress(),
                       hubConnectionRequest.getSignedMessage(),
                       hubConnectionRequest.getRawMessage(),
                       hubConnectionRequest.getToken());
  }

  private void saveDeedHubCommunity(WomConnectionRequest hubConnectionRequest) {
    DeedTenantHub deedTenantHub = hubRepository.findById(StringUtils.lowerCase(hubConnectionRequest.getHubAddress()))
                                               .orElseGet(DeedTenantHub::new);
    mapConnectionRequestToHub(deedTenantHub, hubConnectionRequest);
    deedTenantHub.setEnabled(true);
    hubRepository.save(deedTenantHub);
  }

  private void checkTokenInMessage(String token) throws WomConnectionException {
    if (StringUtils.isBlank(token)) {
      throw new WomConnectionException("wom.emptyTokenForSignedMessage");
    }
    cleanInvalidTokens();
    if (!tokens.containsKey(token)) {
      throw new WomConnectionException("wom.invalidTokenForSignedMessage");
    }
  }

  private void checkSignedMessage(String deedManagerAddress,
                                  String signedMessage,
                                  String rawMessage,
                                  String token) throws WomConnectionException {
    if (StringUtils.isBlank(deedManagerAddress)) {
      throw new WomConnectionException("wom.emptyDeedManagerAddress");
    } else if (StringUtils.isBlank(signedMessage) || StringUtils.isBlank(rawMessage)) {
      throw new WomConnectionException("wom.emptySignedMessage");
    } else if (!StringUtils.contains(rawMessage, token)) {
      throw new WomConnectionException("wom.invalidSignedMessage");
    }

    try {
      byte[] signatureBytes = Numeric.hexStringToByteArray(signedMessage);
      if (signatureBytes.length < 64) {
        throw new WomConnectionException("wom.invalidSignedMessage");
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
        throw new WomConnectionException("wom.invalidSignedMessage");
      }
    } catch (Exception e) {
      throw new WomConnectionException("wom.invalidSignedMessage", e);
    }
  }

  private void mapConnectionRequestToHub(DeedTenantHub deedTenantHub, WomConnectionRequest hubConnectionRequest) {
    deedTenantHub.setNftId(hubConnectionRequest.getDeedId());
    DeedTenant deedTenant = tenantService.getDeedTenant(hubConnectionRequest.getDeedId());
    if (deedTenant != null) {
      deedTenantHub.setCity(deedTenant.getCityIndex());
      deedTenantHub.setType(deedTenant.getCardType());
    }
    deedTenantHub.setHubAddress(hubConnectionRequest.getHubAddress());
    deedTenantHub.setHubName(hubConnectionRequest.getHubName());
    deedTenantHub.setHubDescription(hubConnectionRequest.getHubDescription());
    deedTenantHub.setHubUrl(hubConnectionRequest.getHubUrl());
    deedTenantHub.setHubLogoUrl(hubConnectionRequest.getHubLogoUrl());
    deedTenantHub.setDeedManagerAddress(hubConnectionRequest.getDeedManagerAddress());
    deedTenantHub.setEarnerAddress(hubConnectionRequest.getEarnerAddress());
    deedTenantHub.setColor(hubConnectionRequest.getColor());
  }

  private void checkDeedNotUsed(WomConnectionRequest hubConnectionRequest) throws WomConnectionException {
    Hub hub = getHub(hubConnectionRequest.getDeedId());
    if (hub != null) {
      if (!isHubManagerValid(hub)) {
        disableDeedHubCommunity(hub.getAddress());
      }
      if (hubRepository.existsByNftIdAndHubAddressNotAndEnabledIsTrue(hubConnectionRequest.getDeedId(),
                                                                      StringUtils.lowerCase(hubConnectionRequest.getHubAddress()))) {
        throw new WomConnectionException("wom.deedAlreadyUsedByAHub");
      }
    }
  }

  private void checkDeedManager(String deedManagerAddress, long deedId) throws WomConnectionException {
    if (!isDeedManager(deedManagerAddress, deedId)) {
      throw new WomConnectionException("wom.notDeedManager");
    }
  }

  private void cleanInvalidTokens() {
    tokens.entrySet().removeIf(entry -> (entry.getValue() - System.currentTimeMillis()) > MAX_GENERATED_TOKENS_LT);
  }

  private Hub toHubPresentation(DeedTenantHub deedTenantHub) {
    if (deedTenantHub == null) {
      return null;
    }
    Hub deedTenantHubPresentation = new Hub();
    deedTenantHubPresentation.setDeedId(deedTenantHub.getNftId());
    deedTenantHubPresentation.setCity(deedTenantHub.getCity());
    deedTenantHubPresentation.setType(deedTenantHub.getType());
    deedTenantHubPresentation.setDeedManagerAddress(deedTenantHub.getDeedManagerAddress());
    deedTenantHubPresentation.setAddress(deedTenantHub.getHubAddress());
    deedTenantHubPresentation.setName(deedTenantHub.getHubName());
    deedTenantHubPresentation.setDescription(deedTenantHub.getHubDescription());
    deedTenantHubPresentation.setUrl(deedTenantHub.getHubUrl());
    deedTenantHubPresentation.setLogoUrl(deedTenantHub.getHubLogoUrl());
    deedTenantHubPresentation.setColor(deedTenantHub.getColor());
    deedTenantHubPresentation.setEarnerAddress(deedTenantHub.getEarnerAddress());
    deedTenantHubPresentation.setCreatedDate(deedTenantHub.getCreatedDate());
    return deedTenantHubPresentation;
  }

}
