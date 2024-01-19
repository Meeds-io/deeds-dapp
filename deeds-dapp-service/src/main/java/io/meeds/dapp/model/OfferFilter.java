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
package io.meeds.dapp.model;

import java.util.List;

import io.meeds.dapp.constant.OfferType;
import io.meeds.deeds.common.constant.DeedCard;
import io.meeds.deeds.common.constant.TransactionStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferFilter {

  @With
  private long                    nftId = -1;

  @With
  private String                  ownerAddress;

  @With
  private String                  currentAddress;

  @With
  private List<DeedCard>          cardTypes;

  @With
  private List<OfferType>         offerTypes;

  @With
  private boolean                 excludeExpired;

  @With
  private boolean                 excludeNotStarted;

  @With
  private boolean                 excludeDisabled;

  @With
  private List<TransactionStatus> transactionStatus;

  public static OfferFilter ofNftId(long nftId) {
    return new OfferFilter().withNftId(nftId);
  }
}
