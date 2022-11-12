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
package io.meeds.deeds.utils;

import io.meeds.deeds.model.DeedTenantOffer;
import io.meeds.deeds.model.DeedTenantOfferDTO;

public class Mapper {

  private Mapper() {
    // Class with Static methods
  }

  public static DeedTenantOfferDTO toDTO(DeedTenantOffer deedTenantOffer) {
    if (deedTenantOffer == null) {
      return null;
    }
    return new DeedTenantOfferDTO(deedTenantOffer.getId(),
                                  deedTenantOffer.getNftId(),
                                  deedTenantOffer.getCity(),
                                  deedTenantOffer.getCardType(),
                                  deedTenantOffer.getOwner(),
                                  deedTenantOffer.getDescription(),
                                  deedTenantOffer.getAmount(),
                                  deedTenantOffer.getOfferType(),
                                  deedTenantOffer.getExpirationDuration(),
                                  deedTenantOffer.getDuration(),
                                  deedTenantOffer.getPaymentPeriodicity(),
                                  deedTenantOffer.getOwnerMintingPercentage(),
                                  deedTenantOffer.getMintingPower(),
                                  deedTenantOffer.getExpirationDate(),
                                  deedTenantOffer.getCreatedDate(),
                                  deedTenantOffer.getModifiedDate(),
                                  deedTenantOffer.isEnabled());
  }

  public static DeedTenantOffer fromDTO(DeedTenantOfferDTO deedTenantOfferDTO) {
    if (deedTenantOfferDTO == null) {
      return null;
    }
    return new DeedTenantOffer(deedTenantOfferDTO.getId(),
                               deedTenantOfferDTO.getNftId(),
                               deedTenantOfferDTO.getCity(),
                               deedTenantOfferDTO.getCardType(),
                               deedTenantOfferDTO.getOwner(),
                               deedTenantOfferDTO.getDescription(),
                               deedTenantOfferDTO.getAmount(),
                               deedTenantOfferDTO.getOfferType(),
                               deedTenantOfferDTO.getExpirationDuration(),
                               deedTenantOfferDTO.getDuration(),
                               deedTenantOfferDTO.getPaymentPeriodicity(),
                               deedTenantOfferDTO.getOwnerMintingPercentage(),
                               deedTenantOfferDTO.getMintingPower(),
                               deedTenantOfferDTO.getExpirationDate(),
                               deedTenantOfferDTO.getCreatedDate(),
                               deedTenantOfferDTO.getModifiedDate(),
                               deedTenantOfferDTO.isEnabled());
  }
}
