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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import io.meeds.deeds.common.elasticsearch.model.UEMRewardEntity;
import io.meeds.wom.api.model.HubReport;
import io.meeds.wom.api.model.UEMReward;

public class UEMRewardMapper {

  private UEMRewardMapper() {
    // Utils class
  }

  public static UEMReward fromEntity(UEMRewardEntity entity, List<HubReport> reports) {
    return new UEMReward(entity.getRewardId(),
                         entity.getFromDate(),
                         entity.getToDate(),
                         entity.getUemRewardAmount(),
                         entity.getReportIds(),
                         reports.stream()
                                .collect(Collectors.toMap(HubReport::getReportId,
                                                          HubReport::getUemRewardAmount,
                                                          (v1, v2) -> {
                                                            throw new IllegalStateException(String.format("Duplicate key for values %s and %s",
                                                                                                          v1,
                                                                                                          v2));
                                                          },
                                                          TreeMap::new)),
                         lowerCase(entity.getHubAddresses()),
                         entity.getHubAchievementsCount(),
                         entity.getHubRewardsAmount(),
                         entity.getUemRewardIndex(),
                         entity.getGlobalEngagementRate(),
                         entity.getPeriodType());
  }

  public static UEMRewardEntity toEntity(UEMReward reward) {
    return new UEMRewardEntity(reward.getRewardId(),
                               reward.getFromDate(),
                               reward.getToDate(),
                               lowerCase(reward.getHubAddresses()),
                               reward.getReportIds(),
                               reward.getHubAchievementsCount(),
                               reward.getHubRewardsAmount(),
                               reward.getUemRewardIndex(),
                               reward.getUemRewardAmount(),
                               reward.getGlobalEngagementRate(),
                               reward.getPeriodType());
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
