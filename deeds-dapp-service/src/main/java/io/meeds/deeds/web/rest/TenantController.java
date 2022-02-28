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

import java.security.Principal;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.deeds.constant.UnauthorizedOperationException;
import io.meeds.deeds.service.TenantService;
import io.meeds.deeds.web.security.DeedAuthenticationProvider;

@RestController
@RequestMapping("/api/tenant")
public class TenantController {

  private static final Logger LOG = LoggerFactory.getLogger(TenantController.class);

  @Autowired
  private TenantService       tenantService;

  @GetMapping("/{nftId}/lastCommand")
  @RolesAllowed(DeedAuthenticationProvider.USER_ROLE_NAME)
  public String lastCommand(
                            @PathVariable(name = "nftId")
                            long nftId,
                            Principal principal) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    String walletAddress = principal.getName();
    try {
      return tenantService.getLastTenantCommand(walletAddress, nftId);
    } catch (UnauthorizedOperationException e) {
      LOG.warn("[SECURITY ALERT] {} attempts to get tenant status for Deed with id {}", walletAddress, nftId, e);
      return "";
    }
  }

  @PostMapping("/{nftId}")
  @RolesAllowed(DeedAuthenticationProvider.USER_ROLE_NAME)
  public void startTenant(
                          @PathVariable(name = "nftId")
                          long nftId,
                          @RequestParam(name = "email")
                          String email,
                          @RequestParam(name = "transactionHash")
                          String transactionHash,
                          Principal principal) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    String walletAddress = principal.getName();
    try {
      tenantService.startTenant(walletAddress, transactionHash, nftId, email);
    } catch (UnauthorizedOperationException e) {
      LOG.warn("[SECURITY ALERT] {} attempts to send start tenant query for Deed with id {}", walletAddress, nftId, e);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }

  @DeleteMapping("/{nftId}")
  @RolesAllowed(DeedAuthenticationProvider.USER_ROLE_NAME)
  public void stopTenant(
                         @PathVariable(name = "nftId")
                         long nftId,
                         @RequestParam(name = "transactionHash")
                         String transactionHash,
                         Principal principal) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    String walletAddress = principal.getName();
    try {
      tenantService.stopTenant(walletAddress, transactionHash, nftId);
    } catch (UnauthorizedOperationException e) {
      LOG.warn("[SECURITY ALERT] {} attempts to send stop tenant query for Deed with id {}", walletAddress, nftId, e);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }

  @PatchMapping(path = "/{nftId}")
  @RolesAllowed(DeedAuthenticationProvider.USER_ROLE_NAME)
  public void updateEmail(
                          @PathVariable(name = "nftId")
                          long nftId,
                          @RequestParam(name = "email")
                          String email,
                          Principal principal) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    String walletAddress = principal.getName();
    try {
      tenantService.saveEmail(walletAddress, nftId, email);
    } catch (UnauthorizedOperationException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }

}
