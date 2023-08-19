/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
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
package io.meeds.deeds.scheduling.task;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.meeds.deeds.api.model.UEMReward;
import io.meeds.deeds.common.service.BlockchainService;
import io.meeds.deeds.common.service.UEMRewardComputingService;
import io.meeds.deeds.common.service.UEMRewardService;

@Component
public class UEMRewardBlockchainTransactionCheckTask {

  private static final Logger       LOG = LoggerFactory.getLogger(UEMRewardBlockchainTransactionCheckTask.class);

  @Autowired
  private UEMRewardService          rewardService;

  @Autowired
  private UEMRewardComputingService rewardComputingService;

  @Autowired
  private BlockchainService         blockchainService;

  @Scheduled(cron = "${meeds.deed.uem.checkPendingTransactions.cron:0 0/1 * * * *}")
  public void checkPendingTransactions() {
    List<UEMReward> pendingRewards = rewardService.getPendingTransactionRewards();
    long pendingSize = pendingRewards.size();
    if (pendingSize == 0) {
      return;
    }
    long start = System.currentTimeMillis();
    LOG.info("Check {} pending UEM Reward pending transaction.", pendingSize);
    try {
      pendingRewards.forEach(this::saveRewardTransactionStatus);
    } finally {
      LOG.info("End checking {} pending UEM Reward transaction in {}ms.",
               pendingSize,
               System.currentTimeMillis() - start);
    }
  }

  private void saveRewardTransactionStatus(UEMReward reward) {
    Map<String, Boolean> minedTransactions = getMinedTransactionStatuses(reward);
    rewardComputingService.saveRewardTransactionStatus(reward.getId(), minedTransactions);
  }

  private Map<String, Boolean> getMinedTransactionStatuses(UEMReward reward) {
    return reward.getTransactionHashes()
                 .stream()
                 .filter(blockchainService::isPolygonTransactionMined)
                 .collect(Collectors.toMap(Function.identity(),
                                           blockchainService::isPolygonTransactionConfirmed));
  }

}
