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

import static io.meeds.dapp.web.rest.utils.EntityMapper.getDeedTenantResponse;
import static io.meeds.deeds.constant.CommonConstants.CODE_REFRESH_HTTP_HEADER;

import java.security.Principal;
import java.time.ZoneOffset;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.dapp.web.rest.model.DeedTenantPresentation;
import io.meeds.dapp.web.rest.utils.EntityMapper;
import io.meeds.dapp.web.security.DeedAuthenticationProvider;
import io.meeds.deeds.constant.ObjectNotFoundException;
import io.meeds.deeds.constant.TenantStatus;
import io.meeds.deeds.constant.UnauthorizedOperationException;
import io.meeds.deeds.elasticsearch.model.DeedTenant;
import io.meeds.deeds.service.TenantService;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

  private static final Logger LOG = LoggerFactory.getLogger(TenantController.class);

  @Autowired
  private TenantService       tenantService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DeedTenantPresentation> getTenants(Principal principal) {
    if (principal == null || StringUtils.isBlank(principal.getName())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    String walletAddress = principal.getName();
    List<DeedTenant> deedTenants = tenantService.getDeedTenants(walletAddress);
    return deedTenants.stream().map(EntityMapper::build).toList();
  }

  @GetMapping("/{nftId}")
  @Secured(DeedAuthenticationProvider.USER_ROLE_NAME)
  public ResponseEntity<DeedTenantPresentation> getDeedTenant(
                                                              Principal principal,
                                                              @RequestHeader(name = CODE_REFRESH_HTTP_HEADER, required = false)
                                                              boolean refreshFromBlockchain,
                                                              @PathVariable(name = "nftId")
                                                              Long nftId) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    String walletAddress = principal.getName();
    try {
      DeedTenant deedTenant = tenantService.getDeedTenantOrImport(walletAddress, nftId, refreshFromBlockchain);
      return getDeedTenantResponse(deedTenant);
    } catch (UnauthorizedOperationException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
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
  @Secured(DeedAuthenticationProvider.USER_ROLE_NAME)
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
  @Secured(DeedAuthenticationProvider.USER_ROLE_NAME)
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
  @Secured(DeedAuthenticationProvider.USER_ROLE_NAME)
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
