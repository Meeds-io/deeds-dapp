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
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import io.meeds.deeds.api.constant.UEMRewardStatusType;
import io.meeds.deeds.api.model.HubReport;
import io.meeds.deeds.api.model.UEMReward;
import io.meeds.deeds.common.elasticsearch.model.UEMRewardEntity;

public class UEMRewardMapper {

  private UEMRewardMapper() {
    // Utils class
  }

  public static UEMReward fromEntity(UEMRewardEntity entity, List<HubReport> reports) {
    return new UEMReward(entity.getId(),
                         StringUtils.lowerCase(entity.getHash()),
                         StringUtils.lowerCase(entity.getReportsMerkleRoot()),
                         entity.getFromDate(),
                         entity.getToDate(),
                         entity.getPeriodType(),
                         lowerCase(entity.getHubAddresses()),
                         lowerCase(entity.getReportHashes()),
                         lowerCase(entity.getTransactionHashes()),
                         reports.stream()
                                .collect(Collectors.toMap(HubReport::getHash,
                                                          HubReport::getUemRewardAmount,
                                                          (v1, v2) -> {
                                                            throw new IllegalStateException(String.format("Duplicate key for values %s and %s",
                                                                                                          v1,
                                                                                                          v2));
                                                          },
                                                          TreeMap::new)),
                         entity.getHubAchievementsCount(),
                         entity.getHubRewardsAmount(),
                         entity.getUemRewardIndex(),
                         entity.getUemRewardAmount(),
                         entity.getTokenNetworkId(),
                         StringUtils.lowerCase(entity.getTokenAddress()),
                         entity.getGlobalEngagementRate(),
                         entity.getStatus(),
                         entity.getCreatedDate());
  }

  public static UEMRewardEntity toEntity(UEMReward reward) {
    return new UEMRewardEntity(reward.getId(),
                               StringUtils.lowerCase(reward.getHash()),
                               StringUtils.lowerCase(reward.getReportsMerkleRoot()),
                               reward.getFromDate(),
                               reward.getToDate(),
                               reward.getPeriodType(),
                               lowerCase(reward.getHubAddresses()),
                               lowerCase(reward.getReportHashes()),
                               lowerCase(reward.getTransactionHashes()),
                               reward.getHubAchievementsCount(),
                               reward.getHubRewardsAmount(),
                               reward.getUemRewardIndex(),
                               reward.getUemRewardAmount(),
                               reward.getTokenNetworkId(),
                               StringUtils.lowerCase(reward.getTokenAddress()),
                               reward.getGlobalEngagementRate(),
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
