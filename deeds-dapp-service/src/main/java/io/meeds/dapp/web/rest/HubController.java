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
package io.meeds.dapp.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.meeds.dapp.service.HubService;
import io.meeds.deeds.constant.WomConnectionException;
import io.meeds.deeds.model.Hub;
import io.meeds.deeds.model.WomConnectionRequest;
import io.meeds.deeds.model.WomDisconnectionRequest;

@RestController
@RequestMapping("/api/hubs")
public class HubController {

  @Autowired
  private HubService hubService;

  @GetMapping
  public PagedModel<EntityModel<Hub>> getHubs(Pageable pageable,
                                              PagedResourcesAssembler<Hub> assembler) {
    Page<Hub> hubs = hubService.getHubs(pageable);
    return assembler.toModel(hubs);
  }

  @GetMapping("/{hubAddress}")
  public ResponseEntity<Hub> getHub(
                                    @PathVariable(name = "hubAddress")
                                    String hubAddress) {
    Hub hub = hubService.getHub(hubAddress);
    if (hub == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(hub);
  }

  @GetMapping("/byNftId/{nftId}")
  public ResponseEntity<Hub> getHubByNftId(
                                           @PathVariable(name = "nftId")
                                           long nftId) {
    Hub hub = hubService.getHub(nftId);
    if (hub == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(hub);
  }

  @GetMapping("/manager")
  public String isDeedTenantManager(
                                    @RequestParam(name = "nftId")
                                    Long nftId,
                                    @RequestParam(name = "address")
                                    String address) {
    boolean isManager = hubService.isDeedManager(address, nftId);
    return String.valueOf(isManager);
  }

  @GetMapping("/token")
  public String generateToken() {
    return hubService.generateToken();
  }

  @PostMapping("/connect")
  public ResponseEntity<Object> connectToWoM(
                                             @RequestBody
                                             WomConnectionRequest hubConnectionRequest) {
    try {
      hubService.connectToWoM(hubConnectionRequest);
      return ResponseEntity.noContent().build();
    } catch (WomConnectionException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }

  @PostMapping("/disconnect")
  public ResponseEntity<Object> disconnectFromWoM(
                                                  @RequestBody
                                                  WomDisconnectionRequest disconnectionRequest) {
    try {
      hubService.disconnectFromWoM(disconnectionRequest);
      return ResponseEntity.noContent().build();
    } catch (WomConnectionException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }

}
