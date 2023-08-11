/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 * 
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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode
public class FundInfo {

  @Getter
  private String     address;

  @Getter
  @JsonFormat(shape=Shape.STRING)
  private BigInteger fixedPercentage;

  @Getter
  @JsonFormat(shape=Shape.STRING)
  private BigInteger allocationPoint;

  @Getter
  @JsonFormat(shape=Shape.STRING)
  private BigInteger lastRewardTime;

  @Getter
  @JsonFormat(shape=Shape.STRING)
  private BigInteger accMeedPerShare;

  @Getter
  @JsonProperty("isLPToken")
  private boolean    isLpToken;

  @Getter
  @Setter
  @JsonFormat(shape=Shape.STRING)
  private BigInteger meedsBalance;

  @Getter
  @Setter
  @JsonFormat(shape=Shape.STRING)
  private BigInteger totalSupply;

  @Getter
  @Setter
  @JsonFormat(shape=Shape.STRING)
  private BigInteger xMeedPendingReward;

  @Getter
  @Setter
  @JsonFormat(shape=Shape.STRING)
  private BigInteger lpBalanceOfTokenFactory;

  @Getter
  @Setter
  @JsonFormat(shape=Shape.STRING)
  private String     symbol;

  public FundInfo(String address,
                  BigInteger fixedPercentage,
                  BigInteger allocationPoint,
                  BigInteger lastRewardTime,
                  BigInteger accMeedPerShare,
                  Boolean isLPToken) {
    this.address = address;
    this.fixedPercentage = fixedPercentage;
    this.allocationPoint = allocationPoint;
    this.lastRewardTime = lastRewardTime;
    this.accMeedPerShare = accMeedPerShare;
    this.isLpToken = isLPToken != null && isLPToken.booleanValue();
  }

}
