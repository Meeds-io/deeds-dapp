/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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
package io.meeds.deeds.api.model;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.util.Set;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.meeds.deeds.api.constant.UEMRewardStatusType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_EMPTY)
@Relation(collectionRelation = "rewards", itemRelation = "reward")
public class UEMReward {

  private String              id;

  private String              hash;

  private String              previousHash;

  private Instant             fromDate;

  private Instant             toDate;

  private String              periodType;

  private Set<String>         hubAddress;

  private Set<String>         reportHash;

  /**
   * Total internal hub achievements
   */
  private long                hubAchievementsCount;

  /**
   * Total internal hub rewards sent to hub users
   */
  private double              hubRewardsAmount;

  /**
   * Total internal hub reward indices
   */
  private double              uemRewardIndex;

  /**
   * Total internal rewarded amount to hubs sent to hub users
   */
  private double              uemRewardAmount;

  private double              globalEngagementRate;

  private long                tokenNetworkId;

  private String              tokenAddress;

  private UEMRewardStatusType status;

  private Instant             createdDate;

  @JsonIgnore
  public long getHubsCount() {
    return hubAddress == null ? 0 : hubAddress.size();
  }

  public double getEw() {
    double hubsCount = getHubsCount();
    return hubsCount == 0 ? 0d
                          : BigDecimal.valueOf(globalEngagementRate)
                                      .divide(BigDecimal.valueOf(hubsCount), MathContext.DECIMAL128)
                                      .doubleValue();
  }

}
