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
import org.springframework.stereotype.Component;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Sign.SignatureData;
import org.web3j.utils.Numeric;

import io.meeds.deeds.constant.ObjectNotFoundException;
import io.meeds.deeds.constant.WoMConnectionRequestException;
import io.meeds.deeds.elasticsearch.model.DeedTenant;
import io.meeds.deeds.model.DeedTenantHub;
import io.meeds.deeds.model.DeedTenantNft;
import io.meeds.deeds.model.HubConnectionRequest;
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

  public DeedTenantNft getDeedTenant(Long nftId) throws ObjectNotFoundException {
    DeedTenant deedTenant = tenantService.getDeedTenantOrImport(nftId);
    return new DeedTenantNft(deedTenant.getNftId(),
                             deedTenant.getCityIndex(),
                             deedTenant.getCardType(),
                             deedTenant.getManagerAddress());
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

  public void connectToWoM(HubConnectionRequest hubConnectionRequest) throws WoMConnectionRequestException {
    validateHubCommunityConnectionRequest(hubConnectionRequest);
    disableDeedHubCommunityWithSameNft(hubConnectionRequest.getDeedId(), hubConnectionRequest.getHubAddress());
    saveDeedHubCommunity(hubConnectionRequest);
  }

  private void validateHubCommunityConnectionRequest(HubConnectionRequest hubConnectionRequest) throws WoMConnectionRequestException {
    checkTokenInMessage(hubConnectionRequest.getToken());
    checkSignedMessage(hubConnectionRequest.getDeedManagerAddress(),
                       hubConnectionRequest.getSignedMessage(),
                       hubConnectionRequest.getRawMessage(),
                       hubConnectionRequest.getToken());
    checkDeedManager(hubConnectionRequest.getDeedManagerAddress(),
                     hubConnectionRequest.getDeedId());
  }

  private void disableDeedHubCommunityWithSameNft(long nftId, String hubAddress) {
    hubRepository.findByNftIdAndHubAddressNotAndEnabledIsTrue(nftId,
                                                              StringUtils.lowerCase(hubAddress))
                 .ifPresent(existingDeedTenantHub -> {
                   existingDeedTenantHub.setEnabled(false);
                   hubRepository.save(existingDeedTenantHub);
                 });
  }

  private void saveDeedHubCommunity(HubConnectionRequest hubConnectionRequest) {
    DeedTenantHub deedTenantHub = hubRepository.findById(StringUtils.lowerCase(hubConnectionRequest.getHubAddress()))
                                               .orElseGet(DeedTenantHub::new);
    mapConnectionRequestToHub(deedTenantHub, hubConnectionRequest);
    deedTenantHub.setEnabled(true);
    hubRepository.save(deedTenantHub);
  }

  private void checkTokenInMessage(String token) throws WoMConnectionRequestException {
    if (StringUtils.isBlank(token)) {
      throw new WoMConnectionRequestException("wom.emptyTokenForSignedMessage");
    }
    cleanInvalidTokens();
    if (!tokens.containsKey(token)) {
      throw new WoMConnectionRequestException("wom.invalidTokenForSignedMessage");
    }
  }

  private void checkSignedMessage(String deedManagerAddress,
                                  String signedMessage,
                                  String rawMessage,
                                  String token) throws WoMConnectionRequestException {
    if (StringUtils.isBlank(deedManagerAddress)) {
      throw new WoMConnectionRequestException("wom.emptyDeedManagerAddress");
    } else if (StringUtils.isBlank(signedMessage) || StringUtils.isBlank(rawMessage)) {
      throw new WoMConnectionRequestException("wom.emptySignedMessage");
    } else if (!StringUtils.contains(rawMessage, token)) {
      throw new WoMConnectionRequestException("wom.invalidSignedMessage");
    }

    try {
      byte[] signatureBytes = Numeric.hexStringToByteArray(signedMessage);
      if (signatureBytes.length < 64) {
        throw new WoMConnectionRequestException("wom.invalidSignedMessage");
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
        throw new WoMConnectionRequestException("wom.invalidSignedMessage");
      }
    } catch (Exception e) {
      throw new WoMConnectionRequestException("wom.invalidSignedMessage", e);
    }
  }

  private void mapConnectionRequestToHub(DeedTenantHub deedTenantHub, HubConnectionRequest hubConnectionRequest) {
    deedTenantHub.setNftId(hubConnectionRequest.getDeedId());
    deedTenantHub.setHubAddress(hubConnectionRequest.getHubAddress());
    deedTenantHub.setHubName(hubConnectionRequest.getHubName());
    deedTenantHub.setHubDescription(hubConnectionRequest.getHubDescription());
    deedTenantHub.setHubUrl(hubConnectionRequest.getHubUrl());
    deedTenantHub.setHubLogoUrl(hubConnectionRequest.getHubLogoUrl());
    deedTenantHub.setDeedManagerAddress(hubConnectionRequest.getDeedManagerAddress());
    deedTenantHub.setEarnerAddress(hubConnectionRequest.getEarnerAddress());
    deedTenantHub.setColor(hubConnectionRequest.getColor());
  }

  private void checkDeedManager(String deedManagerAddress, long deedId) throws WoMConnectionRequestException {
    if (!tenantService.isDeedManager(deedManagerAddress, deedId)) {
      throw new WoMConnectionRequestException("wom.notDeedManager");
    }
  }

  private void cleanInvalidTokens() {
    tokens.entrySet().removeIf(entry -> (entry.getValue() - System.currentTimeMillis()) > MAX_GENERATED_TOKENS_LT);
  }

}
