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
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.meeds.deeds.constant.BlockchainLeaseStatus;
import io.meeds.deeds.model.DeedLeaseBlockchainState;
import io.meeds.deeds.model.DeedTenantLease;
import io.meeds.deeds.service.BlockchainService;
import io.meeds.deeds.service.LeaseService;

@Component
public class LeaseBlockchainTransactionCheckTask {

  private static final Logger LOG = LoggerFactory.getLogger(LeaseBlockchainTransactionCheckTask.class);

  @Autowired
  private LeaseService        leaseService;

  @Autowired
  private BlockchainService   blockchainService;

  @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
  public void checkPendingLeases() {
    List<DeedTenantLease> pendingLeases = leaseService.getPendingTransactions();
    int pendingLeasesSize = pendingLeases.size();
    if (pendingLeasesSize == 0) {
      return;
    }
    long start = System.currentTimeMillis();
    LOG.info("Check {} pending Deed Lease pending transaction.", pendingLeasesSize);
    try {
      pendingLeases.forEach(lease -> {
        List<String> pendingTransactions = lease.getPendingTransactions();
        pendingTransactions.forEach(transactionHash -> {
          try {
            if (StringUtils.isBlank(transactionHash)) {
              leaseService.saveLeaseTransactionAsError(lease.getId(), transactionHash);
            } else if (blockchainService.isTransactionMined(transactionHash)) {
              Map<BlockchainLeaseStatus, DeedLeaseBlockchainState> minedEvents =
                                                                               blockchainService.getLeaseTransactionEvents(transactionHash);
              leaseService.updateLeaseStatusFromBlockchain(lease.getId(), transactionHash, minedEvents);
            }
          } catch (Exception e) {
            LOG.warn("Can't update lease pending transaction {} status. It will be reattempted later.",
                     transactionHash,
                     e);
          }
        });
      });
    } finally {
      LOG.info("End checking {} pending Deed Lease pending transaction in {}ms.",
               pendingLeasesSize,
               System.currentTimeMillis() - start);
    }
  }

}
