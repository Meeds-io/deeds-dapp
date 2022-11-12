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
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.deeds.constant.DeedCard;
import io.meeds.deeds.constant.ObjectNotFoundException;
import io.meeds.deeds.constant.OfferType;
import io.meeds.deeds.constant.UnauthorizedOperationException;
import io.meeds.deeds.model.DeedTenantOfferDTO;
import io.meeds.deeds.service.DeedTenantOfferService;
import io.meeds.deeds.web.security.DeedAuthenticationProvider;

@RestController
@RequestMapping("/api/offers")
public class DeedTenantOfferController {

  private static final String    NFT_DOESN_T_EXIST_MESSAGE = "NFT doesn't exist";

  private static final Logger    LOG                       = LoggerFactory.getLogger(DeedTenantOfferController.class);

  @Autowired
  private DeedTenantOfferService deedTenantOfferService;

  @GetMapping
  public PagedModel<EntityModel<DeedTenantOfferDTO>> getOffers(Pageable pageable,
                                                               PagedResourcesAssembler<DeedTenantOfferDTO> assembler,
                                                               @RequestParam(name = "nftId", required = false)
                                                               Long nftId,
                                                               @RequestParam(name = "cardType", required = false)
                                                               List<DeedCard> cardTypes,
                                                               @RequestParam(name = "offerType", required = false)
                                                               List<OfferType> offerTypes) {
    Page<DeedTenantOfferDTO> offers;
    if (nftId != null && nftId > 0) {
      offers = deedTenantOfferService.getOffersList(nftId, pageable);
    } else {
      offers = deedTenantOfferService.getOffersList(cardTypes, offerTypes, pageable);
    }
    return assembler.toModel(offers);
  }

  @GetMapping("/{id}")
  public DeedTenantOfferDTO getOffer(
                                     @PathVariable(name = "id", required = true)
                                     Long offerId) {
    DeedTenantOfferDTO offer = deedTenantOfferService.getOffer(offerId);
    if (offer == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    return offer;
  }

  @PostMapping
  @Secured(DeedAuthenticationProvider.USER_ROLE_NAME)
  public DeedTenantOfferDTO createRentingOffer(Principal principal,
                                               @RequestBody
                                               DeedTenantOfferDTO deedTenantOfferDTO) {
    if (principal == null || StringUtils.isBlank(principal.getName())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    if (deedTenantOfferDTO == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Object is missing");
    }
    String walletAddress = principal.getName();
    try {
      return deedTenantOfferService.createRentingOffer(walletAddress, deedTenantOfferDTO);
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, NFT_DOESN_T_EXIST_MESSAGE);
    } catch (UnauthorizedOperationException e) {
      LOG.warn("[SECURITY ALERT] {} attempts to create Deed tenant offer while not owner {}",
               walletAddress,
               deedTenantOfferDTO,
               e);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }

  @PutMapping("/{id}")
  @Secured(DeedAuthenticationProvider.USER_ROLE_NAME)
  public DeedTenantOfferDTO updateRentingOffer(Principal principal,
                                               @PathVariable("id")
                                               Long id,
                                               @RequestBody
                                               DeedTenantOfferDTO deedTenantOfferDTO) {
    if (principal == null || StringUtils.isBlank(principal.getName())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    if (id == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Object id is missing");
    }
    if (deedTenantOfferDTO == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Object is missing");
    }
    String walletAddress = principal.getName();
    try {
      deedTenantOfferDTO.setId(id);
      return deedTenantOfferService.updateRentingOffer(walletAddress, deedTenantOfferDTO);
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, NFT_DOESN_T_EXIST_MESSAGE);
    } catch (UnauthorizedOperationException e) {
      LOG.warn("[SECURITY ALERT] {} attempts to update Deed tenant offer while not owner {}",
               walletAddress,
               deedTenantOfferDTO,
               e);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }

  @DeleteMapping("/{id}")
  @Secured(DeedAuthenticationProvider.USER_ROLE_NAME)
  public void deleteRentingOffer(Principal principal,
                                 @PathVariable("id")
                                 Long id) {
    if (principal == null || StringUtils.isBlank(principal.getName())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    if (id == null || id == 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Object id is missing");
    }
    String walletAddress = principal.getName();
    try {
      deedTenantOfferService.deleteRentingOffer(walletAddress, id);
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, NFT_DOESN_T_EXIST_MESSAGE);
    } catch (UnauthorizedOperationException e) {
      LOG.warn("[SECURITY ALERT] {} attempts to delete Deed tenant offer while not owner of offer with id {}",
               walletAddress,
               id,
               e);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }

}
