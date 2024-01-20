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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.deeds.common.utils;

import java.time.Instant;

import io.meeds.deeds.common.elasticsearch.model.DeedTenant;
import io.meeds.deeds.common.elasticsearch.model.HubEntity;
import io.meeds.wom.api.model.Hub;

public class HubMapper {

  private HubMapper() {
    // Utils class
  }

  public static Hub fromEntity(HubEntity hubEntity) {
    if (hubEntity == null) {
      return null;
    }
    return new Hub(hubEntity.getNftId(),
                   hubEntity.getCity(),
                   hubEntity.getType(),
                   hubEntity.getAddress(),
                   hubEntity.getName(),
                   hubEntity.getDescription(),
                   hubEntity.getUrl(),
                   hubEntity.getColor(),
                   hubEntity.getHubOwnerAddress(),
                   hubEntity.getDeedOwnerAddress(),
                   hubEntity.getDeedManagerAddress(),
                   hubEntity.getEarnerAddress(),
                   hubEntity.getCreatedDate(),
                   hubEntity.getUpdatedDate(),
                   hubEntity.getUsersCount(),
                   hubEntity.getRewardsPeriodType(),
                   hubEntity.getRewardsPerPeriod(),
                   hubEntity.isEnabled());
  }

  public static HubEntity toEntity(Hub hub,
                                   DeedTenant deedTenant,
                                   HubEntity existingEntity) {
    return new HubEntity(hub.getAddress(),
                         deedTenant.getNftId(),
                         deedTenant.getCityIndex(),
                         deedTenant.getCardType(),
                         hub.getEarnerAddress(),
                         hub.getHubOwnerAddress(),
                         deedTenant.getOwnerAddress(),
                         deedTenant.getManagerAddress(),
                         hub.getName(),
                         hub.getDescription(),
                         hub.getUrl(),
                         hub.getColor(),
                         existingEntity == null ? null : existingEntity.getAvatarId(),
                         existingEntity == null ? null : existingEntity.getBannerId(),
                         hub.getUsersCount(),
                         hub.getRewardsPeriodType(),
                         hub.getRewardsPerPeriod(),
                         hub.isEnabled(),
                         existingEntity == null ? Instant.now() : existingEntity.getCreatedDate(),
                         existingEntity == null ? Instant.now() : existingEntity.getUpdatedDate());
  }

}
