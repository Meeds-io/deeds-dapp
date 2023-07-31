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
package io.meeds.deeds.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.deeds.constant.ObjectNotFoundException;
import io.meeds.deeds.service.HubService;
import io.meeds.deeds.web.rest.model.DeedTenantHub;

@RestController
@RequestMapping("/api/hubs")
public class HubController {

  @Autowired
  private HubService hubService;

  @GetMapping("/{nftId}")
  public ResponseEntity<DeedTenantHub> getDeedTenantHub(
                                                        @PathVariable(name = "nftId")
                                                        Long nftId) {
    try {
      DeedTenantHub deedTenantHub = hubService.getDeedTenant(nftId);
      if (deedTenantHub == null) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.ok(deedTenantHub);
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping("/{nftId}/{address}/manager")
  public String isDeedTenantManager(
                                    @PathVariable(name = "nftId")
                                    Long nftId,
                                    @PathVariable(name = "address")
                                    String walletAddress) {
    boolean isManager = hubService.isDeedManager(walletAddress, nftId);
    return String.valueOf(isManager);
  }

  @GetMapping("/token")
  public String generateToken() {
    return hubService.generateToken();
  }

}
