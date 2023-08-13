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
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import io.meeds.deeds.api.model.HubReportData;
import io.meeds.deeds.api.model.HubReport;
import io.meeds.deeds.common.elasticsearch.model.HubReportEntity;

public class HubReportMapper {

  private HubReportMapper() {
    // Utils class
  }

  public static HubReportEntity toEntity(HubReport report) {
    return new HubReportEntity(StringUtils.lowerCase(report.getHash()),
                               StringUtils.lowerCase(report.getHubAddress()),
                               StringUtils.lowerCase(report.getEarnerAddress()),
                               StringUtils.lowerCase(report.getDeedManagerAddress()),
                               report.getDeedId(),
                               report.getFromDate(),
                               report.getToDate(),
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
                               report.getRewardId(),
                               StringUtils.lowerCase(report.getRewardHash()),
                               report.getHubRewardAmount(),
                               report.getUemRewardIndex(),
                               report.getUemRewardAmount(),
                               report.getLastPeriodUemRewardAmount(),
                               report.getHubRewardAmountPerPeriod(),
                               report.getLastPeriodUemRewardAmountPerPeriod(),
                               report.getSentDate(),
                               Instant.now());
  }

  public static HubReport fromEntity(HubReportEntity entity) {
    HubReport reportStatus = new HubReport(entity.getHash(),
                                           entity.getSignature(),
                                           entity.getEarnerAddress(),
                                           entity.getDeedManagerAddress(),
                                           entity.getStatus(),
                                           null,
                                           entity.getCreatedDate(),
                                           entity.getUemRewardIndex(),
                                           entity.getUemRewardAmount(),
                                           entity.getLastPeriodUemRewardAmount(),
                                           entity.getHubRewardAmountPerPeriod(),
                                           entity.getLastPeriodUemRewardAmountPerPeriod(),
                                           entity.getRewardId(),
                                           entity.getRewardHash());
    reportStatus.setReportData(new HubReportData(entity.getHubAddress(),
                                                 entity.getDeedId(),
                                                 entity.getFromDate(),
                                                 entity.getToDate(),
                                                 entity.getPeriodType(),
                                                 entity.getUsersCount(),
                                                 entity.getParticipantsCount(),
                                                 entity.getRecipientsCount(),
                                                 entity.getAchievementsCount(),
                                                 entity.getRewardTokenAddress(),
                                                 entity.getRewardTokenNetworkId(),
                                                 entity.getHubRewardAmount(),
                                                 entity.getTransactions()));
    return reportStatus;
  }

  public static Set<String> lowerCase(Set<String> hashes) {
    if (CollectionUtils.isEmpty(hashes)) {
      return Collections.emptySet();
    } else {
      return hashes.stream()
                   .map(StringUtils::lowerCase)
                   .collect(Collectors.toSet());
    }
  }
}
