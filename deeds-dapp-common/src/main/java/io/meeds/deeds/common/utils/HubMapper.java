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

import io.meeds.deeds.common.elasticsearch.model.DeedTenant;
import io.meeds.deeds.common.elasticsearch.model.HubEntity;
import io.meeds.deeds.common.model.DeedTenantLeaseDTO;
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
                   hubEntity.getCreatedDate(),
                   hubEntity.getUntilDate(),
                   hubEntity.getJoinDate(),
                   hubEntity.getUpdatedDate(),
                   hubEntity.getUsersCount(),
                   hubEntity.getRewardsPeriodType(),
                   hubEntity.getRewardsPerPeriod(),
                   hubEntity.isEnabled() && hubEntity.getJoinDate() != null
                                                    && (hubEntity.getUntilDate() == null
                                                        || hubEntity.getUntilDate().isAfter(Instant.now())),
                   hubEntity.getOwnerClaimableAmount(),
                   hubEntity.getManagerClaimableAmount());
  }

  public static HubEntity toEntity(Hub hub, // NOSONAR
                                   DeedTenant deedTenant,
                                   DeedTenantLeaseDTO lease,
                                   HubEntity existingEntity) {
    boolean connected = existingEntity != null && existingEntity.isEnabled();
    Instant untilDate = existingEntity == null ? lease.getEndDate() : existingEntity.getUntilDate();

    String ownerAddress = deedTenant.getOwnerAddress();
    String managerAddress = deedTenant.getManagerAddress();
    if (StringUtils.equalsIgnoreCase(ownerAddress, managerAddress)) {
      untilDate = null;
    } else if (connected
               && lease != null
               && StringUtils.equalsIgnoreCase(hub.getHubOwnerAddress(), lease.getManagerAddress())) {
      untilDate = lease.getEndDate();
    }

    if (connected
        && untilDate != null
        && untilDate.isBefore(Instant.now())) {
      connected = false;
    }
    return new HubEntity(hub.getAddress(),
                         deedTenant.getNftId(),
                         deedTenant.getCityIndex(),
                         deedTenant.getCardType(),
                         hub.getHubOwnerAddress(),
                         ownerAddress,
                         managerAddress,
                         hub.getName(),
                         hub.getDescription(),
                         hub.getUrl(),
                         hub.getColor(),
                         existingEntity == null ? null : existingEntity.getAvatarId(),
                         existingEntity == null ? null : existingEntity.getBannerId(),
                         hub.getUsersCount(),
                         hub.getRewardsPeriodType(),
                         hub.getRewardsPerPeriod(),
                         connected,
                         existingEntity == null ? 0d : existingEntity.getOwnerClaimableAmount(),
                         existingEntity == null ? 0d : existingEntity.getManagerClaimableAmount(),
                         existingEntity == null ? Instant.now() : existingEntity.getCreatedDate(),
                         untilDate,
                         existingEntity == null ? null : existingEntity.getJoinDate(),
                         existingEntity == null ? Instant.now() : existingEntity.getUpdatedDate());
  }

}
