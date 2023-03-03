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

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.meeds.deeds.constant.BlockchainLeaseStatus;
import io.meeds.deeds.constant.BlockchainOfferStatus;
import io.meeds.deeds.model.DeedLeaseBlockchainState;
import io.meeds.deeds.model.DeedOfferBlockchainState;
import io.meeds.deeds.service.BlockchainService;
import io.meeds.deeds.service.LeaseService;
import io.meeds.deeds.service.OfferService;
import io.meeds.deeds.service.SettingService;

@Component
public class RentingBlockchainMinedEventsCheckTask {

  private static final Logger LOG                         =
                                  LoggerFactory.getLogger(RentingBlockchainMinedEventsCheckTask.class);

  private static final String SETTING_LAST_TIME_CHECK_KEY = "minedProvisioningTransactionsCheck";

  @Autowired
  private LeaseService        leaseService;

  @Autowired
  private OfferService        offerService;

  @Autowired
  private BlockchainService   blockchainService;

  @Autowired
  private SettingService      settingService;

  @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES, initialDelay = 2)
  @SuppressWarnings("unchecked")
  public void checkMinedRentingEvents() {
    long lastBlock = blockchainService.getLastBlock();
    long lastCheckedBlock = getLastCheckedBlock();

    long start = System.currentTimeMillis();
    LOG.debug("Checking Renting events happened on Blockchain contract.");
    List<Map<?, ?>> minedEvents = blockchainService.getMinedRentingTransactions(lastCheckedBlock, lastBlock);
    if (CollectionUtils.isEmpty(minedEvents)) {
      LOG.debug("End retrieving renting events in {}ms. No events for now",
                System.currentTimeMillis() - start);
      return;
    }
    LOG.debug("End retrieving renting events happened on Blockchain contract {} in {}ms.",
              minedEvents.size(),
              System.currentTimeMillis() - start);

    start = System.currentTimeMillis();
    LOG.debug("Check Renting events happened on Blockchain contract {} .", minedEvents.size());
    minedEvents.forEach(event -> {
      if (!event.isEmpty() && event instanceof EnumMap) {
        Object keyType = event.keySet().iterator().next();
        if (keyType instanceof BlockchainLeaseStatus status) {
          Map<BlockchainLeaseStatus, DeedLeaseBlockchainState> events =
                                                                      (Map<BlockchainLeaseStatus, DeedLeaseBlockchainState>) event;
          DeedLeaseBlockchainState deedLease = events.get(status);
          LOG.debug("Check Lease event {} happened on Blockchain contract {} .", status, deedLease);
          updateLeaseStatusFromBlockchain(status, deedLease);
        } else if (keyType instanceof BlockchainOfferStatus status) {
          Map<BlockchainOfferStatus, DeedOfferBlockchainState> events =
                                                                      (Map<BlockchainOfferStatus, DeedOfferBlockchainState>) event;
          DeedOfferBlockchainState blockchainOffer = events.get(status);
          LOG.debug("Check Offer event {} happened on Blockchain contract {} .", status, blockchainOffer);
          updateOfferFromBlockchain(blockchainOffer);
        }
      }
    });

    saveLastCheckedBlock(lastBlock);

    LOG.debug("End checking renting events happened on Blockchain contract {} in {}ms.",
              minedEvents.size(),
              System.currentTimeMillis() - start);
  }

  private void updateOfferFromBlockchain(DeedOfferBlockchainState blockchainOffer) {
    try {
      offerService.updateOfferFromBlockchain(blockchainOffer, true);
    } catch (Exception e) {
      LOG.warn("Error updating blockchain information of blockchain change for an offer: {}", blockchainOffer, e);
    }
  }

  private void updateLeaseStatusFromBlockchain(BlockchainLeaseStatus status, DeedLeaseBlockchainState deedLease) {
    try {
      leaseService.updateLeaseStatusFromBlockchain(deedLease, status);
    } catch (Exception e) {
      LOG.warn("Error updating blockchain information of blockchain change for a lease: {}", deedLease, e);
    }
  }

  private long getLastCheckedBlock() {
    String settingValue = settingService.get(SETTING_LAST_TIME_CHECK_KEY);
    return settingValue == null ? 0 : Long.parseLong(settingValue);
  }

  private void saveLastCheckedBlock(long lastBlock) {
    settingService.save(SETTING_LAST_TIME_CHECK_KEY, String.valueOf(lastBlock));
  }

}
