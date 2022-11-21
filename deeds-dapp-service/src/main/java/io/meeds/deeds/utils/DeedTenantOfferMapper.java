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
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import io.meeds.deeds.constant.ExpirationDuration;
import io.meeds.deeds.constant.NoticePeriod;
import io.meeds.deeds.constant.OfferType;
import io.meeds.deeds.constant.TransactionStatus;
import io.meeds.deeds.model.DeedTenantOffer;
import io.meeds.deeds.model.DeedTenantOfferDTO;

public class DeedTenantOfferMapper {

  public static final Instant MAX_DATE_VALUE = Instant.ofEpochSecond(165241780471l);

  public static final String  EVERYONE       = "ALL";

  private DeedTenantOfferMapper() {
    // Class with Static methods
  }

  public static DeedTenantOfferDTO toDTO(DeedTenantOffer deedTenantOffer) {
    if (deedTenantOffer == null) {
      return null;
    }
    ExpirationDuration expirationDuration = deedTenantOffer.getExpirationDuration();
    Instant expirationDate = expirationDuration == null
        || MAX_DATE_VALUE.equals(deedTenantOffer.getExpirationDate()) ? null : deedTenantOffer.getExpirationDate();
    return new DeedTenantOfferDTO(deedTenantOffer.getId(),
                                  deedTenantOffer.getOfferId(),
                                  deedTenantOffer.getNftId(),
                                  deedTenantOffer.getCity(),
                                  deedTenantOffer.getCardType(),
                                  deedTenantOffer.getOwner(),
                                  deedTenantOffer.getHostAddress(),
                                  deedTenantOffer.getDescription(),
                                  deedTenantOffer.getAmount(),
                                  deedTenantOffer.getAllDurationAmount(),
                                  deedTenantOffer.getOfferType(),
                                  expirationDuration,
                                  deedTenantOffer.getDuration(),
                                  deedTenantOffer.getPaymentPeriodicity(),
                                  deedTenantOffer.getNoticePeriod(),
                                  deedTenantOffer.getOwnerMintingPercentage(),
                                  deedTenantOffer.getMintingPower(),
                                  deedTenantOffer.getOfferTransactionHash(),
                                  deedTenantOffer.getOfferTransactionStatus(),
                                  deedTenantOffer.getStartDate(),
                                  expirationDate,
                                  deedTenantOffer.getCreatedDate(),
                                  deedTenantOffer.getModifiedDate(),
                                  deedTenantOffer.isEnabled(),
                                  deedTenantOffer.getParentId(),
                                  deedTenantOffer.getAcquisitionIds(),
                                  deedTenantOffer.getUpdateId(),
                                  deedTenantOffer.getDeleteId());
  }

  public static DeedTenantOffer fromDTO(DeedTenantOfferDTO deedTenantOfferDTO) {
    if (deedTenantOfferDTO == null) {
      return null;
    }
    NoticePeriod noticePeriod = deedTenantOfferDTO.getNoticePeriod();
    if (noticePeriod == null) {
      noticePeriod = NoticePeriod.NO_PERIOD;
    }
    Instant now = Instant.now();
    DeedTenantOffer newOffer = new DeedTenantOffer();
    newOffer.setOfferId(deedTenantOfferDTO.getOfferId());
    newOffer.setNftId(deedTenantOfferDTO.getNftId());
    newOffer.setCity(deedTenantOfferDTO.getCity());
    newOffer.setCardType(deedTenantOfferDTO.getCardType());
    newOffer.setMintingPower(deedTenantOfferDTO.getMintingPower());
    newOffer.setOfferType(OfferType.RENTING);
    newOffer.setHostAddress(StringUtils.lowerCase(deedTenantOfferDTO.getHostAddress()));
    newOffer.setOwner(StringUtils.lowerCase(deedTenantOfferDTO.getOwner()));
    newOffer.setViewAddresses(Collections.singletonList(StringUtils.lowerCase(deedTenantOfferDTO.getOwner())));
    newOffer.setOfferTransactionHash(StringUtils.lowerCase(deedTenantOfferDTO.getOfferTransactionHash()));
    newOffer.setOfferTransactionStatus(TransactionStatus.IN_PROGRESS);
    newOffer.setDescription(deedTenantOfferDTO.getDescription());
    newOffer.setAmount(deedTenantOfferDTO.getAmount());
    newOffer.setAllDurationAmount(deedTenantOfferDTO.getAllDurationAmount());
    newOffer.setDuration(deedTenantOfferDTO.getDuration());
    newOffer.setExpirationDuration(deedTenantOfferDTO.getExpirationDuration());
    newOffer.setPaymentPeriodicity(deedTenantOfferDTO.getPaymentPeriodicity());
    newOffer.setNoticePeriod(noticePeriod);
    newOffer.setOwnerMintingPercentage(deedTenantOfferDTO.getOwnerMintingPercentage());
    newOffer.setExpirationDate(MAX_DATE_VALUE);
    newOffer.setCreatedDate(now);
    newOffer.setModifiedDate(now);
    newOffer.setEnabled(true);
    return newOffer;
  }

  public static DeedTenantOffer toOfferUpdateChangeLog(DeedTenantOfferDTO deedTenantOfferDTO,
                                                       DeedTenantOffer existingDeedTenantOffer) {
    if (deedTenantOfferDTO == null) {
      return null;
    }
    DeedTenantOffer changeLogOffer = toOfferChangeLog(existingDeedTenantOffer, deedTenantOfferDTO.getOfferTransactionHash());
    changeLogOffer.setHostAddress(StringUtils.lowerCase(deedTenantOfferDTO.getHostAddress()));
    changeLogOffer.setDescription(deedTenantOfferDTO.getDescription());
    changeLogOffer.setAmount(deedTenantOfferDTO.getAmount());
    changeLogOffer.setAllDurationAmount(deedTenantOfferDTO.getAllDurationAmount());
    changeLogOffer.setDuration(deedTenantOfferDTO.getDuration());
    changeLogOffer.setExpirationDuration(deedTenantOfferDTO.getExpirationDuration());
    changeLogOffer.setPaymentPeriodicity(deedTenantOfferDTO.getPaymentPeriodicity());
    changeLogOffer.setNoticePeriod(deedTenantOfferDTO.getNoticePeriod());
    changeLogOffer.setOwnerMintingPercentage(deedTenantOfferDTO.getOwnerMintingPercentage());
    return changeLogOffer;
  }

  public static DeedTenantOffer toOfferChangeLog(DeedTenantOffer existingDeedTenantOffer, String transactionHash) {
    DeedTenantOffer changeLogOffer = existingDeedTenantOffer.clone();
    changeLogOffer.setId(null);
    changeLogOffer.setViewAddresses(Collections.emptyList());
    changeLogOffer.setOfferTransactionHash(StringUtils.lowerCase(transactionHash));
    changeLogOffer.setOfferTransactionStatus(TransactionStatus.IN_PROGRESS);
    changeLogOffer.setParentId(existingDeedTenantOffer.getId());
    changeLogOffer.setDeleteId(null);
    changeLogOffer.setUpdateId(null);
    changeLogOffer.setAcquisitionIds(Collections.emptySet());
    changeLogOffer.setCreatedDate(Instant.now());
    changeLogOffer.setModifiedDate(changeLogOffer.getCreatedDate());
    return changeLogOffer;
  }

}
