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

import java.time.Instant;
import java.util.Set;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_EMPTY)
@Relation(collectionRelation = "rewards", itemRelation = "reward")
public class UEMRewardData {

  private String      previousHash;

  private Instant     fromDate;

  private Instant     toDate;

  private String      periodType;

  private Set<String> hubAddresses;

  private Set<String> reportHashes;

  /**
   * Total internal hub achievements
   */
  private long        hubAchievementsCount;

  /**
   * Total internal hub rewards sent to hub users
   */
  private double      hubRewardsAmount;

  /**
   * Total internal hub reward indices
   */
  private double      uemRewardIndex;

  /**
   * UEM Reward amount sent to Hub earner address
   */
  private double      uemRewardAmount;

  private long        tokenNetworkId;

  private String      tokenAddress;

}
