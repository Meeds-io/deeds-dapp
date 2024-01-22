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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.meeds.deeds.common.elasticsearch.model.DeedTenant;
import io.meeds.deeds.common.elasticsearch.model.HubEntity;
import io.meeds.deeds.common.model.DeedTenantLeaseDTO;
import io.meeds.wom.api.model.Hub;

public class HubMapper {

  private static final Logger LOG = LoggerFactory.getLogger(HubMapper.class);

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
                   hubEntity.getUntilDate(),
                   hubEntity.getUpdatedDate(),
                   hubEntity.getUsersCount(),
                   hubEntity.getRewardsPeriodType(),
                   hubEntity.getRewardsPerPeriod(),
                   hubEntity.isEnabled() && (hubEntity.getUntilDate() == null
                                             || hubEntity.getUntilDate().isAfter(Instant.now())));
  }

  public static HubEntity toEntity(Hub hub, // NOSONAR
                                   DeedTenant deedTenant,
                                   DeedTenantLeaseDTO lease,
                                   HubEntity existingEntity) {
    boolean enabled = existingEntity != null && existingEntity.isEnabled();
    Instant untilDate = existingEntity == null ? lease.getEndDate() : existingEntity.getUntilDate();
    if (enabled) {
      if (lease != null && StringUtils.equalsIgnoreCase(hub.getHubOwnerAddress(), lease.getManagerAddress())) {
        untilDate = lease.getEndDate();// The Deed tenant owns the hub, thus the
                                       // connection becomes outdated when the
                                       // lease ends
      } else if ((lease != null && StringUtils.equalsIgnoreCase(hub.getHubOwnerAddress(), lease.getOwnerAddress()))
                 || StringUtils.equalsIgnoreCase(hub.getHubOwnerAddress(), deedTenant.getOwnerAddress())) {
        untilDate = null; // The Deed owner owns the hub, thus no end date for
                          // WoM connection
      } else {
        LOG.warn("Can't know whether the Hub {} is owned by the Deed '#{}' owner '{}' or tenant '{}'",
                 hub.getAddress(),
                 hub.getDeedId(),
                 deedTenant.getOwnerAddress(),
                 deedTenant.getManagerAddress());
      }
      if (untilDate != null && untilDate.isBefore(Instant.now())) {
        enabled = false; // Change to disabled if outdated
      }
    }

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
                         enabled,
                         existingEntity == null ? Instant.now() : existingEntity.getCreatedDate(),
                         untilDate,
                         existingEntity == null ? Instant.now() : existingEntity.getUpdatedDate());
  }

}
