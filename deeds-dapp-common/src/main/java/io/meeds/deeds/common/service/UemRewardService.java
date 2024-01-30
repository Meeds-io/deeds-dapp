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
package io.meeds.deeds.common.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import io.meeds.deeds.common.elasticsearch.model.UemRewardEntity;
import io.meeds.deeds.common.elasticsearch.storage.UEMRewardRepository;
import io.meeds.deeds.common.utils.UemRewardMapper;
import io.meeds.wom.api.model.HubReport;
import io.meeds.wom.api.model.UemReward;

@Component
public class UemRewardService {

  public static final String  UEM_REWARD_SAVED = "uem.reward.saved";

  @Autowired
  private HubReportService    hubReportService;

  @Autowired
  private BlockchainService   blockchainService;

  @Autowired
  private ListenerService     listenerService;

  @Autowired
  private UEMRewardRepository rewardRepository;

  public Page<UemReward> getRewards(String hubAddress, Pageable pageable) {
    pageable = pageable.isUnpaged() ? pageable :
                                    PageRequest.of(pageable.getPageNumber(),
                                                   pageable.getPageSize(),
                                                   pageable.getSortOr(Sort.by(Direction.DESC, "fromDate")));
    if (StringUtils.isBlank(hubAddress)) {
      return rewardRepository.findAll(pageable)
                             .map(UemRewardMapper::fromEntity);
    } else {
      return rewardRepository.findByHubAddresses(hubAddress, pageable)
                             .map(UemRewardMapper::fromEntity);
    }
  }

  public UemReward getRewardById(long rewardId) {
    return rewardRepository.findById(rewardId)
                           .map(UemRewardMapper::fromEntity)
                           .orElse(null);
  }

  public UemReward refreshRewardProperties(long rewardId) {
    UemReward reward = new UemReward();
    reward.setRewardId(rewardId);
    blockchainService.retrieveRewardProperties(reward);

    UemRewardEntity rewardEntity = UemRewardMapper.toEntity(reward);
    computeUemReward(rewardEntity);
    rewardEntity = rewardRepository.save(rewardEntity);
    listenerService.publishEvent(UEM_REWARD_SAVED, rewardEntity.getRewardId());
    return UemRewardMapper.fromEntity(rewardEntity);
  }

  private void computeUemReward(UemRewardEntity rewardEntity) {
    List<HubReport> reports = hubReportService.getReportsByRewardId(rewardEntity.getRewardId());
    if (CollectionUtils.isEmpty(reports)) {
      rewardEntity.setHubAchievementsCount(0);
      rewardEntity.setHubParticipantsCount(0);
      rewardEntity.setHubRewardsAmount(0);
      rewardEntity.setReportRewards(Collections.emptyMap());
      rewardEntity.setHubAddresses(Collections.emptySet());
    } else {
      reports.forEach(report -> hubReportService.computeUemReward(report,
                                                                  rewardEntity.getFixedGlobalIndex(),
                                                                  rewardEntity.getAmount()));
      rewardEntity.setHubAchievementsCount(reports.stream().mapToLong(HubReport::getAchievementsCount).sum());
      rewardEntity.setHubParticipantsCount(reports.stream().mapToLong(HubReport::getParticipantsCount).sum());
      rewardEntity.setHubRewardsAmount(reports.stream().mapToDouble(HubReport::getUemRewardAmount).sum());
      rewardEntity.setReportRewards(reports.stream()
                                           .collect(Collectors.toMap(r -> String.valueOf(r.getReportId()),
                                                                     HubReport::getUemRewardAmount)));
      rewardEntity.setHubAddresses(reports.stream()
                                          .map(HubReport::getHubAddress)
                                          .map(StringUtils::lowerCase)
                                          .collect(Collectors.toSet()));
    }
  }

}
