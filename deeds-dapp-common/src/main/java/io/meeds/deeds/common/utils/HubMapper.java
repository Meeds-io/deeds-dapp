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
package io.meeds.deeds.common.utils;

import java.time.Instant;

import io.meeds.deeds.common.elasticsearch.model.HubEntity;
import io.meeds.deeds.api.model.Hub;
import io.meeds.deeds.common.elasticsearch.model.DeedTenant;

public class HubMapper {

  private HubMapper() {
    // Utils class
  }

  public static Hub fromEntity(HubEntity hubEntity) {
    if (hubEntity == null) {
      return null;
    }
    Hub hub = new Hub();
    hub.setDeedId(hubEntity.getNftId());
    hub.setCity(hubEntity.getCity());
    hub.setType(hubEntity.getType());
    hub.setDeedManagerAddress(hubEntity.getDeedManagerAddress());
    hub.setAddress(hubEntity.getAddress());
    hub.setName(hubEntity.getName());
    hub.setDescription(hubEntity.getDescription());
    hub.setUrl(hubEntity.getUrl());
    hub.setColor(hubEntity.getColor());
    hub.setEarnerAddress(hubEntity.getEarnerAddress());
    hub.setCreatedDate(hubEntity.getCreatedDate());
    hub.setUpdatedDate(hubEntity.getUpdatedDate());
    hub.setRewardsPeriodType(hubEntity.getRewardsPeriodType());
    hub.setRewardsPerPeriod(hubEntity.getRewardsPerPeriod());
    hub.setUsersCount(hubEntity.getUsersCount());
    return hub;
  }

  public static HubEntity toEntity(Hub hub,
                                   DeedTenant deedTenant,
                                   HubEntity existingEntity) {
    HubEntity hubEntity;
    if (existingEntity == null) {
      hubEntity = new HubEntity();
      hubEntity.setCreatedDate(Instant.now());
    } else {
      hubEntity = existingEntity;
    }
    hubEntity.setNftId(hub.getDeedId());
    if (deedTenant != null) {
      hubEntity.setCity(deedTenant.getCityIndex());
      hubEntity.setType(deedTenant.getCardType());
    }
    hubEntity.setAddress(hub.getAddress());
    hubEntity.setName(hub.getName());
    hubEntity.setDescription(hub.getDescription());
    hubEntity.setUrl(hub.getUrl());
    hubEntity.setDeedManagerAddress(hub.getDeedManagerAddress());
    hubEntity.setEarnerAddress(hub.getEarnerAddress());
    hubEntity.setColor(hub.getColor());
    hubEntity.setUsersCount(hub.getUsersCount());
    hubEntity.setRewardsPerPeriod(existingEntity == null ? 0d : existingEntity.getRewardsPerPeriod());
    hubEntity.setRewardsPeriodType(hub.getRewardsPeriodType());
    return hubEntity;
  }

}
