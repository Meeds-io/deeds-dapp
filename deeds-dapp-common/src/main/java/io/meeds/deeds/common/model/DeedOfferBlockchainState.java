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
package io.meeds.deeds.common.model;

import java.math.BigInteger;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeedOfferBlockchainState {

  private BigInteger id;

  private BigInteger blockNumber;

  private BigInteger deedId;

  private String     creator;

  private BigInteger months;

  private BigInteger noticePeriod;

  private BigInteger price;

  private BigInteger allDurationPrice;

  private BigInteger offerStartDate;

  private BigInteger offerExpirationDate;

  private BigInteger offerExpirationDays;

  private String     authorizedTenant;

  private BigInteger ownerMintingPercentage;

  private String     transactionHash;
}
