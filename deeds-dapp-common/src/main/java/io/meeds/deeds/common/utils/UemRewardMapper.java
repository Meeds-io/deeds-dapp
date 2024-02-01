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
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import io.meeds.deeds.common.elasticsearch.model.UemRewardEntity;
import io.meeds.wom.api.model.UemReward;

public class UemRewardMapper {

  private UemRewardMapper() {
    // Utils class
  }

  public static UemRewardEntity toEntity(UemReward reward) {
    return new UemRewardEntity(reward.getRewardId(),
                               reward.getAmount(),
                               reward.getFixedGlobalIndex(),
                               reward.getFromDate(),
                               reward.getToDate(),
                               reward.getReportIds(),
                               lowerCase(reward.getHubAddresses()),
                               reward.getReportRewards() == null ? null :
                                                                 reward.getReportRewards()
                                                                       .entrySet()
                                                                       .stream()
                                                                       .collect(Collectors.toMap(e -> String.valueOf(e.getKey()),
                                                                                                 Entry::getValue)),
                               reward.getHubAchievementsCount(),
                               reward.getHubParticipantsCount(),
                               reward.getHubRewardsAmount(),
                               reward.getSumEd());
  }

  public static UemReward fromEntity(UemRewardEntity entity) {
    return new UemReward(entity.getRewardId(),
                         entity.getAmount(),
                         entity.getFixedGlobalIndex(),
                         entity.getFromDate(),
                         entity.getToDate(),
                         entity.getReportIds(),
                         entity.getHubAddresses(),
                         entity.getReportRewards() == null ? null :
                                                           entity.getReportRewards()
                                                                 .entrySet()
                                                                 .stream()
                                                                 .collect(Collectors.toMap(e -> Long.parseLong(e.getKey()),
                                                                                           Entry::getValue)),
                         entity.getHubAchievementsCount(),
                         entity.getHubParticipantsCount(),
                         entity.getHubRewardsAmount(),
                         entity.getSumEd());
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
