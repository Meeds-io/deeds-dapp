/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.dapp.web.rest.model;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.meeds.wom.api.model.Hub;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonInclude(value = Include.NON_EMPTY)
@Relation(collectionRelation = "hubs", itemRelation = "hub")
public class HubWithReward extends Hub {

  private long    actionsCount;

  private double  topHubReceiverAmount;

  private double  engagementScore;

  private boolean hasReports;

  public HubWithReward(Hub hub,
                       long actionsCount,
                       double topHubReceiverAmount,
                       double engagementScore,
                       boolean hasReports) {
    super(hub.getDeedId(),
          hub.getCity(),
          hub.getType(),
          hub.getAddress(),
          hub.getName(),
          hub.getDescription(),
          hub.getUrl(),
          hub.getColor(),
          hub.getHubOwnerAddress(),
          hub.getDeedOwnerAddress(),
          hub.getDeedManagerAddress(),
          hub.getCreatedDate(),
          hub.getUntilDate(),
          hub.getJoinDate(),
          hub.getUpdatedDate(),
          hub.getUsersCount(),
          hub.getRewardsPeriodType(),
          hub.getRewardsPerPeriod(),
          hub.isConnected(),
          hub.getOwnerClaimableAmount(),
          hub.getManagerClaimableAmount());
    this.actionsCount = actionsCount;
    this.topHubReceiverAmount = topHubReceiverAmount;
    this.engagementScore = engagementScore;
    this.hasReports = hasReports;
  }

}
