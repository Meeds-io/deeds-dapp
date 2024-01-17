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

import static io.meeds.deeds.common.utils.UEMRewardMapper.toEntity;
import static io.meeds.wom.api.constant.UEMRewardStatusType.NONE;
import static io.meeds.wom.api.constant.UEMRewardStatusType.PENDING_REWARD;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import io.meeds.deeds.common.elasticsearch.model.UEMRewardEntity;
import io.meeds.deeds.common.elasticsearch.storage.UEMRewardRepository;
import io.meeds.deeds.common.model.RewardPeriod;
import io.meeds.deeds.common.utils.UEMRewardMapper;
import io.meeds.wom.api.constant.UEMRewardStatusType;
import io.meeds.wom.api.model.HubReport;
import io.meeds.wom.api.model.UEMReward;

@Component
public class UEMRewardService {

  public static final String                     UEM_REWARD_SAVED             = "uem.reward.saved";

  public static final String                     UEM_REWARD_STATUS_CHANGED    = "uem.reward.status.changed";

  public static final String                     UEM_REWARD_TRANSACTION_SENT  = "uem.reward.status.transactionSent";

  public static final String                     UEM_REWARD_COMPUTED          = "uem.reward.computed";

  private static final List<UEMRewardStatusType> NOT_SENT_STATUSES            = Stream.of(NONE).toList();

  private static final List<UEMRewardStatusType> PENDING_TRANSACTION_STATUSES = Stream.of(PENDING_REWARD).toList();

  @Autowired
  private HubReportService                       reportService;

  @Autowired
  private ListenerService                        listenerService;

  @Autowired
  private UEMRewardRepository                    rewardRepository;

  public Page<UEMReward> getRewards(String hubAddress, Pageable pageable) {
    pageable = pageable.isUnpaged() ? pageable
                                    : PageRequest.of(pageable.getPageNumber(),
                                                     pageable.getPageSize(),
                                                     pageable.getSortOr(Sort.by(Direction.DESC, "fromDate")));
    if (StringUtils.isBlank(hubAddress)) {
      return rewardRepository.findAll(pageable)
                             .map(this::fromEntity);
    } else {
      return rewardRepository.findByHubAddresses(hubAddress, pageable)
                             .map(this::fromEntity);
    }
  }

  public Stream<UEMReward> getPendingRewards() {
    return rewardRepository.findByStatusIn(NOT_SENT_STATUSES)
                           .map(this::fromEntity);
  }

  public List<UEMReward> getPendingTransactionRewards() {
    return rewardRepository.findByStatusIn(PENDING_TRANSACTION_STATUSES)
                           .map(this::fromEntity)
                           .toList();
  }

  public UEMReward saveReward(UEMReward reward) {
    UEMRewardEntity rewardEntity = toEntity(reward);
    rewardEntity = rewardRepository.save(rewardEntity);
    listenerService.publishEvent(UEM_REWARD_SAVED, rewardEntity.getId());
    return fromEntity(rewardEntity);
  }

  public UEMReward getReward(RewardPeriod period) {
    return rewardRepository.findByFromDateIsAndToDateIs(period.getFrom(), period.getTo())
                           .map(this::fromEntity)
                           .orElse(null);
  }

  public UEMReward getRewardById(String rewardId) {
    return rewardRepository.findById(rewardId)
                           .map(this::fromEntity)
                           .orElse(null);
  }

  public void saveRewardStatus(String rewardId, UEMRewardStatusType rewardStatus) {
    UEMReward reward = getRewardById(rewardId);
    reward.setStatus(rewardStatus);
    UEMRewardEntity rewardEntity = toEntity(reward);
    rewardEntity = rewardRepository.save(rewardEntity);
    listenerService.publishEvent(UEM_REWARD_STATUS_CHANGED, rewardEntity.getId());
  }

  public void saveRewardTransactionHash(String rewardId, String transactionHash) {
    UEMReward reward = getRewardById(rewardId);
    reward.addTransactionHash(transactionHash);
    reward.setStatus(UEMRewardStatusType.PENDING_REWARD);
    UEMRewardEntity rewardEntity = toEntity(reward);
    rewardEntity = rewardRepository.save(rewardEntity);
    listenerService.publishEvent(UEM_REWARD_TRANSACTION_SENT, rewardEntity.getId());
  }

  private UEMReward fromEntity(UEMRewardEntity rewardEntity) {
    List<HubReport> reports = rewardEntity.getReportHashes()
                                          .stream()
                                          .map(hash -> reportService.getReport(hash))
                                          .filter(Objects::nonNull)
                                          .toList();
    return UEMRewardMapper.fromEntity(rewardEntity, reports);
  }

}
