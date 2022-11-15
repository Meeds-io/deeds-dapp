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

import io.meeds.deeds.constant.DeedCard;
import io.meeds.deeds.constant.ExpirationDuration;
import io.meeds.deeds.constant.OfferType;
import io.meeds.deeds.constant.RentalDuration;
import io.meeds.deeds.constant.RentalPaymentPeriodicity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeedTenantOfferDTO {

  private String                           id;

  private long                             nftId;

  private io.meeds.deeds.constant.DeedCity city;

  private DeedCard                         cardType;

  private String                           owner;

  private String                           description;

  private double                           amount;

  private OfferType                        offerType;

  private ExpirationDuration               expirationDuration;

  private RentalDuration                   duration;

  private RentalPaymentPeriodicity         paymentPeriodicity;

  private int                              ownerMintingPercentage;

  private double                           mintingPower;

  private Instant                          expirationDate;

  private Instant                          createdDate;

  private Instant                          modifiedDate;

  private boolean                          enabled;

}
