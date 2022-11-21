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
package io.meeds.deeds.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.meeds.deeds.constant.DeedCard;
import io.meeds.deeds.constant.DeedCity;
import io.meeds.deeds.constant.ExpirationDuration;
import io.meeds.deeds.constant.NoticePeriod;
import io.meeds.deeds.constant.OfferType;
import io.meeds.deeds.constant.RentalDuration;
import io.meeds.deeds.constant.RentalPaymentPeriodicity;
import io.meeds.deeds.constant.SecurityDepositPeriod;
import io.meeds.deeds.constant.TransactionStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonInclude(value = Include.NON_EMPTY)
public class DeedTenantOfferUpdateDTO extends DeedTenantOfferDTO {

  public DeedTenantOfferUpdateDTO(String id, // NOSONAR
                                  long offerId,
                                  long nftId,
                                  DeedCity city,
                                  DeedCard cardType,
                                  String owner,
                                  String hostAddress,
                                  String description,
                                  double amount,
                                  OfferType offerType,
                                  ExpirationDuration expirationDuration,
                                  RentalDuration duration,
                                  RentalPaymentPeriodicity paymentPeriodicity,
                                  SecurityDepositPeriod securityDepositPeriod,
                                  NoticePeriod noticePeriod,
                                  int ownerMintingPercentage,
                                  double mintingPower,
                                  String transactionHash,
                                  TransactionStatus transactionStatus,
                                  Instant expirationDate,
                                  Instant createdDate,
                                  Instant modifiedDate,
                                  boolean enabled,
                                  boolean updateExpirationDate) {
    super(id,
          offerId,
          nftId,
          city,
          cardType,
          owner,
          hostAddress,
          description,
          amount,
          offerType,
          expirationDuration,
          duration,
          paymentPeriodicity,
          securityDepositPeriod,
          noticePeriod,
          ownerMintingPercentage,
          mintingPower,
          transactionHash,
          transactionStatus,
          expirationDate,
          createdDate,
          modifiedDate,
          enabled);
    this.updateExpirationDate = updateExpirationDate;
  }

  private boolean updateExpirationDate;

}
