/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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

import static io.meeds.deeds.common.constant.CommonConstants.CODE_REFRESH_HTTP_HEADER;
import static io.meeds.deeds.common.constant.CommonConstants.CODE_VERIFICATION_HTTP_HEADER;

import java.security.Principal;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import io.meeds.dapp.web.security.DeedAuthenticationProvider;
import io.meeds.deeds.common.constant.DeedCard;
import io.meeds.deeds.common.constant.UnauthorizedOperationException;
import io.meeds.deeds.common.model.DeedTenantLeaseDTO;
import io.meeds.deeds.common.model.LeaseFilter;
import io.meeds.deeds.common.service.AuthorizationCodeService;
import io.meeds.deeds.common.service.LeaseService;
import io.meeds.wom.api.constant.ObjectNotFoundException;

@RestController
@RequestMapping("/api/leases")
public class LeaseController {

  private static final Logger      LOG = LoggerFactory.getLogger(LeaseController.class);

  @Autowired
  private LeaseService             leaseService;

  @Autowired
  private AuthorizationCodeService authorizationCodeService;

  @GetMapping
  public PagedModel<EntityModel<DeedTenantLeaseDTO>> getLeases(Pageable pageable,
                                                               PagedResourcesAssembler<DeedTenantLeaseDTO> assembler,
                                                               @RequestParam(name = "nftId", required = false)
                                                               Long nftId,
                                                               @RequestParam(name = "cardType", required = false)
                                                               List<DeedCard> cardTypes,
                                                               @RequestParam(name = "onlyConfirmed", required = false)
                                                               boolean onlyConfirmed,
                                                               @RequestParam(name = "address", required = true)
                                                               String address,
                                                               @RequestParam(name = "owner", required = true)
                                                               boolean owner) {
    LeaseFilter leaseFilter = new LeaseFilter();
    if (nftId != null && nftId > 0) {
      leaseFilter.setNftId(nftId);
    }
    leaseFilter.setExcludeNotConfirmed(onlyConfirmed);
    leaseFilter.setCardTypes(cardTypes);
    leaseFilter.setCurrentAddress(StringUtils.lowerCase(address));
    leaseFilter.setOwner(owner);
    Page<DeedTenantLeaseDTO> leases = leaseService.getLeases(leaseFilter, pageable);
    return assembler.toModel(leases);
  }

  @GetMapping("/{leaseId}")
  public DeedTenantLeaseDTO getLease(
                                     Principal principal,
                                     @RequestHeader(name = CODE_REFRESH_HTTP_HEADER, required = false)
                                     boolean refreshFromBlockchain,
                                     @PathVariable(name = "leaseId", required = true)
                                     long leaseId) {
    String walletAddress = principal == null ? null : principal.getName();
    try {
      DeedTenantLeaseDTO lease = leaseService.getLease(leaseId, walletAddress, refreshFromBlockchain);
      if (lease == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
      }
      return lease;
    } catch (ObjectNotFoundException | UnauthorizedOperationException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      LOG.warn("Error retrieving lease by id {} and blockchainStateRefresh = {}",
               leaseId,
               refreshFromBlockchain,
               e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @Secured(DeedAuthenticationProvider.USER_ROLE_NAME)
  public DeedTenantLeaseDTO createLease(Principal principal,
                                        @RequestHeader(name = CODE_VERIFICATION_HTTP_HEADER, required = true)
                                        int code,
                                        @RequestParam(name = "offerId", required = true)
                                        String offerId,
                                        @RequestParam(name = "transactionHash", required = true)
                                        String transactionHash) {
    if (principal == null || StringUtils.isBlank(principal.getName())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    String walletAddress = StringUtils.lowerCase(principal.getName());
    try {
      String email = (String) authorizationCodeService.validateAndGetData(walletAddress, code);
      return leaseService.createLease(walletAddress, email, offerId, transactionHash);
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    } catch (UnauthorizedOperationException | IllegalAccessException e) {
      LOG.warn("[SECURITY ALERT] {} attempts to create Deed tenant lease while not allowed based on offerId {} and transactionHash {}",
               walletAddress,
               offerId,
               transactionHash,
               e);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }

  @PatchMapping(path = "/{leaseId}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @Secured(DeedAuthenticationProvider.USER_ROLE_NAME)
  public DeedTenantLeaseDTO payRent(Principal principal,
                                    @PathVariable(name = "leaseId", required = true)
                                    long leaseId,
                                    @RequestParam(name = "ownerAddress", required = true)
                                    String ownerAddress,
                                    @RequestParam(name = "paidMonths", required = true)
                                    int paidMonths,
                                    @RequestParam(name = "transactionHash", required = true)
                                    String transactionHash) {
    if (principal == null || StringUtils.isBlank(principal.getName())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    if (leaseId <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lease id is mandatory");
    }
    if (StringUtils.isBlank(ownerAddress)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner Address is mandatory");
    }
    if (StringUtils.isBlank(transactionHash)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction hash is mandatory");
    }
    if (paidMonths == 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paid months is mandatory");
    }
    String walletAddress = StringUtils.lowerCase(principal.getName());
    try {
      return leaseService.payRents(walletAddress,
                                   ownerAddress,
                                   leaseId,
                                   paidMonths,
                                   transactionHash);
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    } catch (UnauthorizedOperationException e) {
      LOG.warn("[SECURITY ALERT] {} attempts to indicate a rent pay for a tenant lease while not allowed based on leaseId {} and transactionHash {}",
               walletAddress,
               leaseId,
               transactionHash,
               e);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }

  @DeleteMapping(path = "/{leaseId}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @Secured(DeedAuthenticationProvider.USER_ROLE_NAME)
  public DeedTenantLeaseDTO endLease(Principal principal,
                                     @PathVariable(name = "leaseId", required = true)
                                     long leaseId,
                                     @RequestParam(name = "transactionHash", required = true)
                                     String transactionHash) {
    if (principal == null || StringUtils.isBlank(principal.getName())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    if (leaseId <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lease id is mandatory");
    }
    if (StringUtils.isBlank(transactionHash)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction hash is mandatory");
    }
    String walletAddress = StringUtils.lowerCase(principal.getName());
    try {
      return leaseService.endLease(walletAddress,
                                   leaseId,
                                   transactionHash);
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    } catch (UnauthorizedOperationException e) {
      LOG.warn("[SECURITY ALERT] {} attempts to indicate a lease end while not allowed based on leaseId {} and transactionHash {}",
               walletAddress,
               leaseId,
               transactionHash,
               e);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }

}
