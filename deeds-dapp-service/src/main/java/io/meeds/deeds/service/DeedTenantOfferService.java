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
package io.meeds.deeds.service;

import java.time.Instant;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import io.meeds.deeds.constant.DeedCard;
import io.meeds.deeds.constant.DeedCity;
import io.meeds.deeds.constant.ObjectNotFoundException;
import io.meeds.deeds.constant.OfferType;
import io.meeds.deeds.constant.UnauthorizedOperationException;
import io.meeds.deeds.model.DeedTenant;
import io.meeds.deeds.model.DeedTenantOffer;
import io.meeds.deeds.model.DeedTenantOfferDTO;
import io.meeds.deeds.storage.DeedTenantOfferRepository;
import io.meeds.deeds.utils.Mapper;

@Component
public class DeedTenantOfferService {

  @Autowired
  private DeedTenantOfferRepository deedTenantOfferRepository;

  @Autowired
  private TenantService             tenantService;

  public Page<DeedTenantOfferDTO> getOffersList(long nftId, Pageable pageable) {
    Page<DeedTenantOffer> deedTenantOffers = deedTenantOfferRepository.findByNftIdAndEnabledTrue(nftId, pageable);
    return deedTenantOffers.map(Mapper::toDTO);
  }

  public Page<DeedTenantOfferDTO> getOffersList(Pageable pageable) {
    Page<DeedTenantOffer> deedTenantOffers = deedTenantOfferRepository.findByEnabledTrue(pageable);
    return deedTenantOffers.map(Mapper::toDTO);
  }

  public DeedTenantOfferDTO createRentingOffer(String walletAddress,
                                               DeedTenantOfferDTO deedTenantOfferDTO) throws ObjectNotFoundException,
                                                                                      UnauthorizedOperationException {
    long nftId = deedTenantOfferDTO.getNftId();
    if (!tenantService.isDeedOwner(walletAddress, nftId)) {
      throw new UnauthorizedOperationException(getNotOwnerMessage(walletAddress, nftId));
    }
    if (!tenantService.isDeedManager(walletAddress, nftId)) {
      throw new IllegalStateException("Deed Tenant is currently used by another manager, thus can't be rented");
    }
    DeedTenant deedTenant = tenantService.getDeedTenantOrImport(walletAddress, nftId);
    if (deedTenant == null) {
      throw new ObjectNotFoundException(getNftNotExistsMessage(nftId));
    }
    if (deedTenant.getTenantProvisioningStatus() != null && deedTenant.getTenantProvisioningStatus().isStart()) {
      throw new IllegalStateException("Deed Tenant is currently used, thus can't be rented");
    }
    DeedTenantOffer deedTenantOffer = Mapper.fromDTO(deedTenantOfferDTO);
    // When multiple offers will be supported, then make the id
    // auto incremented
    deedTenantOffer.setId(nftId);
    if (deedTenant.getCardType() >= 0) {
      DeedCard cardType = DeedCard.values()[deedTenant.getCardType()];
      deedTenantOffer.setCardType(cardType);
      deedTenantOffer.setMintingPower(cardType.getMintingPower());
    }
    if (deedTenant.getCityIndex() >= 0) {
      deedTenantOffer.setCity(DeedCity.values()[deedTenant.getCityIndex()]);
    }
    deedTenantOffer.setOwner(walletAddress.toLowerCase());
    Instant now = Instant.now();
    deedTenantOffer.setCreatedDate(now);
    deedTenantOffer.setModifiedDate(now);
    deedTenantOffer.setOfferType(OfferType.RENTING);
    if (deedTenantOffer.getExpirationDuration() != null) {
      Instant expirationDate = now.atZone(ZoneOffset.UTC).plus(deedTenantOffer.getExpirationDuration().getPeriod()).toInstant();
      deedTenantOffer.setExpirationDate(expirationDate);
    }
    deedTenantOffer.setEnabled(true);
    deedTenantOffer = saveOffer(deedTenantOffer);
    return Mapper.toDTO(deedTenantOffer);
  }

  public DeedTenantOfferDTO updateRentingOffer(String walletAddress,
                                               DeedTenantOfferDTO deedTenantOfferDTO) throws ObjectNotFoundException,
                                                                                      UnauthorizedOperationException {
    if (!tenantService.isDeedOwner(walletAddress, deedTenantOfferDTO.getNftId())) {
      throw new UnauthorizedOperationException(getNotOwnerMessage(walletAddress, deedTenantOfferDTO.getNftId()));
    }
    if (!tenantService.isDeedManager(walletAddress, deedTenantOfferDTO.getNftId())) {
      throw new IllegalStateException("Deed Tenant is currently used by another manager, thus can't be rented");
    }
    DeedTenantOffer existingDeedTenantOffer = deedTenantOfferRepository.findById(deedTenantOfferDTO.getId()).orElse(null);
    if (existingDeedTenantOffer == null) {
      throw new ObjectNotFoundException("Offer with id  " + deedTenantOfferDTO.getId() + " doesn't exist");
    }
    existingDeedTenantOffer.setOwner(walletAddress.toLowerCase());
    existingDeedTenantOffer.setModifiedDate(Instant.now());
    existingDeedTenantOffer.setDescription(deedTenantOfferDTO.getDescription());
    existingDeedTenantOffer.setAmount(deedTenantOfferDTO.getAmount());
    existingDeedTenantOffer.setDuration(deedTenantOfferDTO.getDuration());
    existingDeedTenantOffer.setExpirationDuration(deedTenantOfferDTO.getExpirationDuration());
    existingDeedTenantOffer.setPaymentPeriodicity(deedTenantOfferDTO.getPaymentPeriodicity());
    existingDeedTenantOffer.setOwnerMintingPercentage(deedTenantOfferDTO.getOwnerMintingPercentage());
    if (deedTenantOfferDTO.getExpirationDuration() == null) {
      existingDeedTenantOffer.setExpirationDate(null);
    } else {
      Instant expirationDate = Instant.now()
                                      .atZone(ZoneOffset.UTC)
                                      .plus(deedTenantOfferDTO.getExpirationDuration().getPeriod())
                                      .toInstant();
      existingDeedTenantOffer.setExpirationDate(expirationDate);
    }

    existingDeedTenantOffer = saveOffer(existingDeedTenantOffer);
    return Mapper.toDTO(existingDeedTenantOffer);
  }

  public void deleteRentingOffer(String walletAddress, Long id) throws ObjectNotFoundException,
                                                                UnauthorizedOperationException {
    DeedTenantOffer existingDeedTenantOffer = deedTenantOfferRepository.findById(id).orElse(null);
    if (existingDeedTenantOffer == null) {
      throw new ObjectNotFoundException("Offer with id  " + id + " doesn't exist");
    }
    if (!tenantService.isDeedOwner(walletAddress, existingDeedTenantOffer.getNftId())) {
      throw new UnauthorizedOperationException(getNotOwnerMessage(walletAddress, existingDeedTenantOffer.getNftId()));
    }
    deedTenantOfferRepository.deleteById(id);
  }

  private DeedTenantOffer saveOffer(DeedTenantOffer deedTenantOffer) {
    return deedTenantOfferRepository.save(deedTenantOffer);
  }

  private String getNftNotExistsMessage(long nftId) {
    return "Deed Tenant with id " + nftId + " doesn't exists";
  }

  private String getNotOwnerMessage(String walletAddress, long nftId) {
    return walletAddress + " isn't owner of Deed NFT #" + nftId;
  }

}
