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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.meeds.deeds.common.constant.BlockchainOfferStatus;
import io.meeds.deeds.common.model.DeedOfferBlockchainState;
import io.meeds.deeds.common.service.BlockchainService;
import io.meeds.deeds.model.DeedTenantOfferDTO;
import io.meeds.deeds.service.OfferService;

@Component
public class OfferBlockchainTransactionCheckTask {

  private static final Logger LOG = LoggerFactory.getLogger(OfferBlockchainTransactionCheckTask.class);

  @Autowired
  private OfferService        offerService;

  @Autowired
  private BlockchainService   blockchainService;

  @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES, initialDelay = 1)
  public void checkPendingOffers() {
    List<DeedTenantOfferDTO> pendingOffers = offerService.getPendingTransactions();
    int pendingOffersSize = pendingOffers.size();
    if (pendingOffersSize == 0) {
      return;
    }
    long start = System.currentTimeMillis();
    LOG.info("Check {} pending transactions for Deed Offers.", pendingOffersSize);
    try {
      pendingOffers.forEach(deedOffer -> {
        String transactionHash = deedOffer.getOfferTransactionHash();
        try {
          if (StringUtils.isBlank(transactionHash)) {
            offerService.updateRentingOfferStatusFromBlockchain(deedOffer.getId(), Collections.emptyMap());
          } else if (blockchainService.isTransactionMined(transactionHash)) {
            Map<BlockchainOfferStatus, DeedOfferBlockchainState> minedEvents = blockchainService.getOfferTransactionEvents(transactionHash);
            offerService.updateRentingOfferStatusFromBlockchain(deedOffer.getId(), minedEvents);
          }
        } catch (Exception e) {
          LOG.warn("Can't update offer pending transaction {} status. It will be reattempted later.",
                   transactionHash,
                   e);
        }
      });
    } finally {
      LOG.info("End checking {} pending transactions for Deed Offers in {}ms.",
               pendingOffersSize,
               System.currentTimeMillis() - start);
    }
  }

}
