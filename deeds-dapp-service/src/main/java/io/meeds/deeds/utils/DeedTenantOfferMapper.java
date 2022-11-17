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

import java.time.Instant;

import io.meeds.deeds.constant.ExpirationDuration;
import io.meeds.deeds.constant.NoticePeriod;
import io.meeds.deeds.constant.SecurityDepositPeriod;
import io.meeds.deeds.model.DeedTenantOffer;
import io.meeds.deeds.model.DeedTenantOfferDTO;

public class DeedTenantOfferMapper {

  public static final Instant MAX_DATE_VALUE = Instant.ofEpochSecond(165241780471l);

  private DeedTenantOfferMapper() {
    // Class with Static methods
  }

  public static DeedTenantOfferDTO toDTO(DeedTenantOffer deedTenantOffer) {
    if (deedTenantOffer == null) {
      return null;
    }
    ExpirationDuration expirationDuration = deedTenantOffer.getExpirationDuration();
    Instant expirationDate = expirationDuration == null ? null : deedTenantOffer.getExpirationDate();
    return new DeedTenantOfferDTO(deedTenantOffer.getId(),
                                  deedTenantOffer.getNftId(),
                                  deedTenantOffer.getCity(),
                                  deedTenantOffer.getCardType(),
                                  deedTenantOffer.getOwner(),
                                  deedTenantOffer.getDescription(),
                                  deedTenantOffer.getAmount(),
                                  deedTenantOffer.getOfferType(),
                                  expirationDuration,
                                  deedTenantOffer.getDuration(),
                                  deedTenantOffer.getPaymentPeriodicity(),
                                  deedTenantOffer.getSecurityDepositPeriod(),
                                  deedTenantOffer.getNoticePeriod(),
                                  deedTenantOffer.getOwnerMintingPercentage(),
                                  deedTenantOffer.getMintingPower(),
                                  expirationDate,
                                  deedTenantOffer.getCreatedDate(),
                                  deedTenantOffer.getModifiedDate(),
                                  deedTenantOffer.isEnabled());
  }

  public static DeedTenantOffer fromDTO(DeedTenantOfferDTO deedTenantOfferDTO) {
    if (deedTenantOfferDTO == null) {
      return null;
    }
    ExpirationDuration expirationDuration = deedTenantOfferDTO.getExpirationDuration();
    Instant expirationDate = expirationDuration == null ? MAX_DATE_VALUE : deedTenantOfferDTO.getExpirationDate();
    SecurityDepositPeriod securityDepositPeriod = deedTenantOfferDTO.getSecurityDepositPeriod();
    NoticePeriod noticePeriod = deedTenantOfferDTO.getNoticePeriod();
    return new DeedTenantOffer(deedTenantOfferDTO.getId(),
                               deedTenantOfferDTO.getNftId(),
                               deedTenantOfferDTO.getCity(),
                               deedTenantOfferDTO.getCardType(),
                               deedTenantOfferDTO.getOwner(),
                               null,
                               deedTenantOfferDTO.getDescription(),
                               deedTenantOfferDTO.getAmount(),
                               deedTenantOfferDTO.getOfferType(),
                               expirationDuration,
                               deedTenantOfferDTO.getDuration(),
                               deedTenantOfferDTO.getPaymentPeriodicity(),
                               securityDepositPeriod == null ? SecurityDepositPeriod.NO_PERIOD : securityDepositPeriod,
                               noticePeriod == null ? NoticePeriod.NO_PERIOD : noticePeriod,
                               deedTenantOfferDTO.getOwnerMintingPercentage(),
                               deedTenantOfferDTO.getMintingPower(),
                               expirationDate,
                               deedTenantOfferDTO.getCreatedDate(),
                               deedTenantOfferDTO.getModifiedDate(),
                               deedTenantOfferDTO.isEnabled());
  }

}
