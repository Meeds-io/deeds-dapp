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
                               report.getRewardId(),
                               StringUtils.lowerCase(report.getHubAddress()),
                               StringUtils.lowerCase(report.getDeedManagerAddress()),
                               StringUtils.lowerCase(report.getOwnerAddress()),
                               report.getOwnerMintingPercentage(),
                               report.getDeedId(),
                               report.getCity(),
                               report.getCardType(),
                               report.getMintingPower(),
                               report.getMaxUsers(),
                               report.getFromDate(),
                               report.getToDate(),
                               report.getSentDate(),
                               report.getPeriodType(),
                               report.getUsersCount(),
                               report.getParticipantsCount(),
                               report.getRecipientsCount(),
                               report.getAchievementsCount(),
                               report.getActionsCount(),
                               StringUtils.lowerCase(report.getRewardTokenAddress()),
                               report.getRewardTokenNetworkId(),
                               lowerCase(report.getTransactions()),
                               report.getHubRewardAmount(),
                               report.getHubTopRewardedAmount(),
                               report.getFixedRewardIndex(),
                               report.getOwnerFixedIndex(),
                               report.getTenantFixedIndex(),
                               report.isFraud(),
                               report.getLastPeriodUemRewardAmount(),
                               report.getUemRewardAmount(),
                               report.getUpdatedDate(),
                               report.getEngagementScore());
  }

  public static HubReport fromEntity(HubReportEntity entity) {
    return new HubReport(entity.getReportId(),
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
                         entity.getActionsCount(),
                         entity.getRewardTokenAddress(),
                         entity.getRewardTokenNetworkId(),
                         entity.getHubRewardAmount(),
                         entity.getHubTopRewardedAmount(),
                         entity.getTransactions(),
                         entity.getRewardId(),
                         entity.getCity(),
                         entity.getCardType(),
                         entity.getMintingPower(),
                         entity.getMaxUsers(),
                         entity.getDeedManagerAddress(),
                         entity.getOwnerAddress(),
                         entity.getOwnerMintingPercentage(),
                         entity.getFixedRewardIndex(),
                         entity.getOwnerFixedIndex(),
                         entity.getTenantFixedIndex(),
                         entity.isFraud(),
                         entity.getLastPeriodUemRewardAmount(),
                         entity.getUemRewardAmount(),
                         entity.getUpdatedDate(),
                         entity.getEngagementScore());
  }

  public static SortedSet<String> lowerCase(SortedSet<String> transactions) {
    if (CollectionUtils.isEmpty(transactions)) {
      return new TreeSet<>();
    } else {
      return transactions.stream()
                         .map(StringUtils::lowerCase)
                         .collect(Collectors.toCollection(TreeSet::new));
    }
  }

}
