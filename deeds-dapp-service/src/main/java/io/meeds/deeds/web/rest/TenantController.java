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

import static io.meeds.deeds.web.rest.utils.EntityMapper.getDeedTenantResponse;

import java.security.Principal;
import java.time.ZoneOffset;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.deeds.constant.ObjectNotFoundException;
import io.meeds.deeds.constant.TenantStatus;
import io.meeds.deeds.constant.UnauthorizedOperationException;
import io.meeds.deeds.model.DeedTenant;
import io.meeds.deeds.service.TenantService;
import io.meeds.deeds.web.rest.model.DeedTenantPresentation;
import io.meeds.deeds.web.security.DeedAuthenticationProvider;

@RestController
@RequestMapping("/api/tenant")
public class TenantController {

  private static final Logger LOG = LoggerFactory.getLogger(TenantController.class);

  @Autowired
  private TenantService       tenantService;

  @GetMapping("/{nftId}")
  @RolesAllowed(DeedAuthenticationProvider.USER_ROLE_NAME)
  public ResponseEntity<DeedTenantPresentation> getDeedTenant(
                                                              Principal principal,
                                                              @PathVariable(name = "nftId")
                                                              Long nftId) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    String walletAddress = principal.getName();
    try {
      DeedTenant deedTenant = tenantService.getDeedTenant(walletAddress, nftId);
      return getDeedTenantResponse(deedTenant);
    } catch (UnauthorizedOperationException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }

  @GetMapping("/{nftId}/startDate")
  public ResponseEntity<String> getDeedTenantStartTime(
                                                       @PathVariable(name = "nftId")
                                                       Long nftId) {
    DeedTenant deedTenant = tenantService.getDeedTenant(nftId);
    if (deedTenant == null || deedTenant.getDate() == null || deedTenant.getTenantProvisioningStatus() == null
        || deedTenant.getTenantProvisioningStatus().isStop()
        || deedTenant.getTenantStatus() != TenantStatus.DEPLOYED) {
      return ResponseEntity.ok("");
    } else {
      return ResponseEntity.ok(String.valueOf(deedTenant.getDate().toEpochSecond(ZoneOffset.UTC)));
    }
  }

  @PostMapping("/{nftId}")
  @RolesAllowed(DeedAuthenticationProvider.USER_ROLE_NAME)
  public void startTenant(
                          @PathVariable(name = "nftId")
                          long nftId,
                          @RequestParam(name = "email", required = false)
                          String email,
                          @RequestParam(name = "transactionHash", required = true)
                          String transactionHash,
                          Principal principal) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    String walletAddress = principal.getName();
    try {
      tenantService.startTenant(walletAddress, transactionHash, nftId, email);
    } catch (ObjectNotFoundException e) {
      throwNftNotExistsError();
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
                         @RequestParam(name = "transactionHash", required = true)
                         String transactionHash,
                         Principal principal) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    String walletAddress = principal.getName();
    try {
      tenantService.stopTenant(walletAddress, transactionHash, nftId);
    } catch (ObjectNotFoundException e) {
      throwNftNotExistsError();
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
                          @RequestParam(name = "email", required = true)
                          String email,
                          Principal principal) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    String walletAddress = principal.getName();
    try {
      tenantService.saveEmail(walletAddress, nftId, email);
    } catch (ObjectNotFoundException e) {
      throwNftNotExistsError();
    } catch (UnauthorizedOperationException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }

  private void throwNftNotExistsError() {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NFT doesn't exist");
  }

}
