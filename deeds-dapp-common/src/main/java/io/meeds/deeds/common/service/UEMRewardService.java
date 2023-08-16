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
package io.meeds.deeds.common.service;

import static io.meeds.deeds.api.constant.UEMRewardStatusType.NONE;
import static io.meeds.deeds.common.utils.UEMRewardMapper.fromEntity;
import static io.meeds.deeds.common.utils.UEMRewardMapper.toEntity;

import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import io.meeds.deeds.api.constant.UEMRewardStatusType;
import io.meeds.deeds.api.model.UEMReward;
import io.meeds.deeds.common.elasticsearch.model.UEMRewardEntity;
import io.meeds.deeds.common.elasticsearch.storage.UEMRewardRepository;
import io.meeds.deeds.common.model.RewardPeriod;
import io.meeds.deeds.common.utils.UEMRewardMapper;

@Component
public class UEMRewardService {

  private static final List<UEMRewardStatusType> NOT_SENT_STATUSES = Stream.of(NONE).toList();

  @Autowired
  private UEMRewardRepository                    rewardRepository;

  public Page<UEMReward> getRewards(String hubAddress, Pageable pageable) {
    pageable = PageRequest.of(pageable.getPageNumber(),
                              pageable.getPageSize(),
                              pageable.getSortOr(Sort.by(Direction.DESC, "fromDate")));
    if (StringUtils.isBlank(hubAddress)) {
      return rewardRepository.findAll(pageable)
                             .map(UEMRewardMapper::fromEntity);
    } else {
      return rewardRepository.findByHubAddresses(hubAddress, pageable)
                             .map(UEMRewardMapper::fromEntity);
    }
  }

  public Stream<UEMReward> getPendingRewards() {
    return rewardRepository.findByStatusIn(NOT_SENT_STATUSES)
                           .map(UEMRewardMapper::fromEntity);
  }

  public UEMReward saveReward(UEMReward reward) {
    UEMRewardEntity rewardEntity = toEntity(reward);
    rewardRepository.save(rewardEntity);
    return fromEntity(rewardEntity);
  }

  public UEMReward getReward(RewardPeriod period) {
    return rewardRepository.findByFromDateIsAndToDateIs(period.getFrom(), period.getTo())
                           .map(UEMRewardMapper::fromEntity)
                           .orElse(null);
  }

  public UEMReward getRewardById(String rewardId) {
    return rewardRepository.findById(rewardId)
                           .map(UEMRewardMapper::fromEntity)
                           .orElse(null);
  }

}
