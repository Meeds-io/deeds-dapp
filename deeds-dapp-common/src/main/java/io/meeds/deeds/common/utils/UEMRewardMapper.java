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

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import io.meeds.deeds.api.constant.UEMRewardStatusType;
import io.meeds.deeds.api.model.UEMReward;
import io.meeds.deeds.common.elasticsearch.model.UEMRewardEntity;

public class UEMRewardMapper {

  private UEMRewardMapper() {
    // Utils class
  }

  public static UEMReward fromEntity(UEMRewardEntity entity) {
    return new UEMReward(entity.getId(),
                         StringUtils.lowerCase(entity.getHash()),
                         StringUtils.lowerCase(entity.getPreviousHash()),
                         entity.getFromDate(),
                         entity.getToDate(),
                         entity.getPeriodType(),
                         lowerCase(entity.getHubAddress()),
                         lowerCase(entity.getReportHash()),
                         entity.getHubAchievementsCount(),
                         entity.getHubRewardsAmount(),
                         entity.getUemRewardIndex(),
                         entity.getUemRewardAmount(),
                         entity.getGlobalEngagementRate(),
                         entity.getTokenNetworkId(),
                         StringUtils.lowerCase(entity.getTokenAddress()),
                         entity.getStatus(),
                         entity.getCreatedDate());
  }

  public static UEMRewardEntity toEntity(UEMReward reward) {
    return new UEMRewardEntity(reward.getId(),
                               StringUtils.lowerCase(reward.getHash()),
                               StringUtils.lowerCase(reward.getPreviousHash()),
                               reward.getFromDate(),
                               reward.getToDate(),
                               reward.getPeriodType(),
                               lowerCase(reward.getHubAddress()),
                               lowerCase(reward.getReportHash()),
                               reward.getHubAchievementsCount(),
                               reward.getHubRewardsAmount(),
                               reward.getUemRewardIndex(),
                               reward.getUemRewardAmount(),
                               reward.getGlobalEngagementRate(),
                               reward.getTokenNetworkId(),
                               StringUtils.lowerCase(reward.getTokenAddress()),
                               reward.getStatus() == null ? UEMRewardStatusType.NONE : reward.getStatus(),
                               reward.getCreatedDate());
  }

  public static Set<String> lowerCase(Set<String> hubAddresses) {
    if (CollectionUtils.isEmpty(hubAddresses)) {
      return Collections.emptySet();
    } else {
      return hubAddresses.stream()
                         .map(StringUtils::lowerCase)
                         .collect(Collectors.toSet());
    }
  }
}
