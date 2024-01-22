/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.dapp.listener;

import static io.meeds.deeds.common.service.LeaseService.LEASE_ACQUISITION_CONFIRMED_EVENT;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.meeds.deeds.common.constant.UnauthorizedOperationException;
import io.meeds.deeds.common.listener.EventListener;
import io.meeds.deeds.common.model.DeedTenantLeaseDTO;
import io.meeds.deeds.common.service.OfferService;
import io.meeds.deeds.common.service.TenantService;

@Service
public class OfferAcquiredListener implements EventListener<DeedTenantLeaseDTO> {

  private static final Logger       LOG              = LoggerFactory.getLogger(OfferAcquiredListener.class);

  public static final String        LISTENER_NAME    = "OfferAcquiredListener";

  private static final List<String> SUPPORTED_EVENTS = Collections.singletonList(LEASE_ACQUISITION_CONFIRMED_EVENT);

  @Autowired
  private OfferService              offerService;

  @Autowired
  private TenantService             tenantService;

  @Override
  public String getName() {
    return LISTENER_NAME;
  }

  @Override
  public List<String> getSupportedEvents() {
    return SUPPORTED_EVENTS;
  }

  @Override
  public void onEvent(String eventName, DeedTenantLeaseDTO lease) {
    if (!lease.isConfirmed()) {
      LOG.warn("Seems a bug, the lease change has been triggered as lease acquisition confirmed while it's not!");
      return;
    }
    if (lease.getStartDate() != null && lease.getStartDate().isBefore(Instant.now())) {
      try {
        tenantService.markDeedAsAcquired(lease.getNftId(), lease.getManagerAddress());
      } catch (UnauthorizedOperationException e) {
        LOG.warn("It seems that Hub Manager {} isn't Provisioning Manager of Deed {} anymore. Ignore assigning Tenant to this manager",
                 lease.getManagerAddress(),
                 lease.getNftId());
      } catch (Exception e) {
        LOG.warn("Error Marking Tenant as acquired for lease with id {} and nft id {}",
                 lease.getId(),
                 lease.getNftId(),
                 e);
      }
    } else {
      LOG.info("Acquired Lease {} by {} of nft {} seems to be in the future, thus Tenant Management will not be delegated immediatly",
               lease.getId(),
               lease.getManagerAddress(),
               lease.getNftId());
    }
    try {
      offerService.markOfferAsAcquired(lease.getId(), lease.getEndDate());
    } catch (Exception e) {
      LOG.warn("Error Marking Offers as acquired for lease with id {} and end date {}",
               lease.getId(),
               Date.from(lease.getEndDate()),
               e);
    }
  }

}
