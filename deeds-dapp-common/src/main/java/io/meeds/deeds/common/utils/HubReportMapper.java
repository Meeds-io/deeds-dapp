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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import io.meeds.deeds.common.elasticsearch.model.HubReportEntity;
import io.meeds.wom.api.model.HubReport;

public class HubReportMapper {

  private HubReportMapper() {
    // Utils class
  }

  public static HubReportEntity toEntity(HubReport report) {
    return new HubReportEntity(report.getReportId(),
                               StringUtils.lowerCase(report.getHubAddress()),
                               StringUtils.lowerCase(report.getEarnerAddress()),
                               StringUtils.lowerCase(report.getDeedManagerAddress()),
                               StringUtils.lowerCase(report.getOwnerAddress()),
                               report.getOwnerMintingPercentage(),
                               report.getDeedId(),
                               report.getFromDate(),
                               report.getToDate(),
                               report.getSentDate(),
                               report.getPeriodType(),
                               report.getUsersCount(),
                               report.getParticipantsCount(),
                               report.getRecipientsCount(),
                               report.getAchievementsCount(),
                               StringUtils.lowerCase(report.getRewardTokenAddress()),
                               report.getRewardTokenNetworkId(),
                               lowerCase(report.getTransactions()),
                               report.getSignature(),
                               report.getStatus(),
                               report.getError(),
                               report.getRewardId(),
                               report.getHubRewardAmount(),
                               report.getUemRewardIndex(),
                               report.getUemRewardAmount(),
                               report.getLastPeriodUemRewardAmount(),
                               report.getLastPeriodUemDiff(),
                               report.getHubRewardAmountPerPeriod(),
                               report.getHubRewardLastPeriodDiff(),
                               report.getLastPeriodUemRewardAmountPerPeriod(),
                               report.getMp(),
                               Instant.now(),
                               Instant.now());
  }

  public static HubReport fromEntity(HubReportEntity entity) {
    return new HubReport(entity.getReportId(),
                         null,
                         null,
                         entity.getHubAddress(),
                         entity.getDeedId(),
                         entity.getFromDate(),
                         entity.getToDate(),
                         entity.getSentDate(),
                         entity.getPeriodType(),
                         entity.getUsersCount(),
                         entity.getParticipantsCount(),
                         entity.getRecipientsCount(),
                         entity.getAchievementsCount(),
                         entity.getRewardTokenAddress(),
                         entity.getRewardTokenNetworkId(),
                         entity.getHubRewardAmount(),
                         entity.getTransactions(),
                         entity.getEarnerAddress(),
                         entity.getDeedManagerAddress(),
                         StringUtils.lowerCase(entity.getOwnerAddress()),
                         entity.getOwnerMintingPercentage(),
                         entity.getStatus(),
                         entity.getError(),
                         entity.getUemRewardIndex(),
                         entity.getUemRewardAmount(),
                         entity.getLastPeriodUemRewardAmount(),
                         entity.getLastPeriodUemDiff(),
                         entity.getHubRewardAmountPerPeriod(),
                         entity.getHubRewardLastPeriodDiff(),
                         entity.getLastPeriodUemRewardAmountPerPeriod(),
                         entity.getMp(),
                         entity.getRewardId());
  }

  public static SortedSet<String> lowerCase(SortedSet<String> hashes) {
    if (CollectionUtils.isEmpty(hashes)) {
      return new TreeSet<>();
    } else {
      return hashes.stream()
                   .map(StringUtils::lowerCase)
                   .collect(Collectors.toCollection(TreeSet::new));
    }
  }

}
