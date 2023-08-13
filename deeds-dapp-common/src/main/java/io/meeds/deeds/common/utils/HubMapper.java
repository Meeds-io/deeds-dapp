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

  public static Hub fromEntity(HubEntity deedTenantHub) {
    if (deedTenantHub == null) {
      return null;
    }
    Hub deedTenantHubPresentation = new Hub();
    deedTenantHubPresentation.setDeedId(deedTenantHub.getNftId());
    deedTenantHubPresentation.setCity(deedTenantHub.getCity());
    deedTenantHubPresentation.setType(deedTenantHub.getType());
    deedTenantHubPresentation.setDeedManagerAddress(deedTenantHub.getDeedManagerAddress());
    deedTenantHubPresentation.setAddress(deedTenantHub.getAddress());
    deedTenantHubPresentation.setName(deedTenantHub.getName());
    deedTenantHubPresentation.setDescription(deedTenantHub.getDescription());
    deedTenantHubPresentation.setUrl(deedTenantHub.getUrl());
    deedTenantHubPresentation.setColor(deedTenantHub.getColor());
    deedTenantHubPresentation.setEarnerAddress(deedTenantHub.getEarnerAddress());
    deedTenantHubPresentation.setCreatedDate(deedTenantHub.getCreatedDate());
    deedTenantHubPresentation.setUpdatedDate(deedTenantHub.getUpdatedDate());
    deedTenantHubPresentation.setRewardsPeriodType(deedTenantHub.getRewardsPeriodType());
    deedTenantHubPresentation.setRewardsPerPeriod(deedTenantHub.getRewardsPerPeriod());
    deedTenantHubPresentation.setUsersCount(deedTenantHub.getUsersCount());
    return deedTenantHubPresentation;
  }

  public static HubEntity toEntity(Hub hub,
                                   DeedTenant deedTenant,
                                   HubEntity existingEntity) {
    HubEntity deedHub;
    if (existingEntity == null) {
      deedHub = new HubEntity();
      deedHub.setCreatedDate(Instant.now());
    } else {
      deedHub = existingEntity;
    }
    deedHub.setNftId(hub.getDeedId());
    if (deedTenant != null) {
      deedHub.setCity(deedTenant.getCityIndex());
      deedHub.setType(deedTenant.getCardType());
    }
    deedHub.setAddress(hub.getAddress());
    deedHub.setName(hub.getName());
    deedHub.setDescription(hub.getDescription());
    deedHub.setUrl(hub.getUrl());
    deedHub.setDeedManagerAddress(hub.getDeedManagerAddress());
    deedHub.setEarnerAddress(hub.getEarnerAddress());
    deedHub.setColor(hub.getColor());
    deedHub.setUsersCount(hub.getUsersCount());
    deedHub.setRewardsPerPeriod(existingEntity == null ? 0d : existingEntity.getRewardsPerPeriod());
    deedHub.setRewardsPeriodType(hub.getRewardsPeriodType());
    return deedHub;
  }

}
