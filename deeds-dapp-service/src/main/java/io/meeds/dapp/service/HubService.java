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
package io.meeds.dapp.service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.meeds.dapp.web.rest.model.DeedTenantHub;
import io.meeds.deeds.constant.ObjectNotFoundException;
import io.meeds.deeds.elasticsearch.model.DeedTenant;
import io.meeds.deeds.service.TenantService;

@Component
public class HubService {

  private static final Random RANDOM                    = new Random();

  private static final int    MAX_GENERATED_TOKENS_SIZE = Integer.parseInt(System.getProperty("meeds.hub.maxTokens", "1000"));

  private static final long   MAX_GENERATED_TOKENS_LT   = 1000l
      * Integer.parseInt(System.getProperty("meeds.hub.maxTokenLiveTimeSeconds",
                                            "600"));

  @Autowired
  private TenantService       tenantService;

  private Map<String, Long>   tokens                    = new ConcurrentHashMap<>();

  public boolean isDeedManager(String address, Long nftId) {
    return tenantService.isDeedManager(address, nftId);
  }

  public DeedTenantHub getDeedTenant(Long nftId) throws ObjectNotFoundException {
    DeedTenant deedTenant = tenantService.getDeedTenantOrImport(nftId);
    return new DeedTenantHub(deedTenant.getNftId(),
                             deedTenant.getCityIndex(),
                             deedTenant.getCardType(),
                             deedTenant.getManagerAddress());
  }

  public String generateToken() {
    tokens.entrySet().removeIf(entry -> (entry.getValue() - System.currentTimeMillis()) > MAX_GENERATED_TOKENS_LT);
    if (tokens.size() >= MAX_GENERATED_TOKENS_SIZE) {
      return tokens.keySet().stream().max((k1, k2) -> (int) (tokens.get(k1) - tokens.get(k2))).orElse(null);
    }
    String token = RANDOM.nextLong() + "-" + RANDOM.nextLong() + "-" + RANDOM.nextLong();
    tokens.put(token, System.currentTimeMillis());
    return token;
  }

}
