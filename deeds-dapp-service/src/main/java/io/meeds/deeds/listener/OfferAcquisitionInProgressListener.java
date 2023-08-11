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
package io.meeds.deeds.listener;

import static io.meeds.deeds.service.LeaseService.LEASE_ACQUIRED_EVENT;

import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.meeds.deeds.common.listener.EventListener;
import io.meeds.deeds.common.model.UserProfileDTO;
import io.meeds.deeds.common.service.UserProfileService;
import io.meeds.deeds.constant.ObjectAlreadyExistsException;
import io.meeds.deeds.elasticsearch.model.DeedTenantLease;
import io.meeds.deeds.service.OfferService;

@Service
public class OfferAcquisitionInProgressListener implements EventListener<DeedTenantLease> {

  private static final Logger       LOG              = LoggerFactory.getLogger(OfferAcquisitionInProgressListener.class);

  public static final String        LISTENER_NAME    = "OfferAcquisitionInProgressListener";

  private static final List<String> SUPPORTED_EVENTS = Collections.singletonList(LEASE_ACQUIRED_EVENT);

  @Autowired
  private UserProfileService        userProfileService;

  @Autowired
  private OfferService              offerService;

  @Override
  public String getName() {
    return LISTENER_NAME;
  }

  @Override
  public List<String> getSupportedEvents() {
    return SUPPORTED_EVENTS;
  }

  @Override
  public void onEvent(String eventName, DeedTenantLease lease) {
    if (StringUtils.isNotBlank(lease.getManagerEmail())) {
      UserProfileDTO userProfileDTO = new UserProfileDTO(lease.getManager(), lease.getManagerEmail());
      userProfileService.saveUserProfile(userProfileDTO);
    }

    if (lease.isConfirmed()) {
      LOG.warn("Seems a bug, the lease change has been triggered as lease acquisition in progress while it's already confirmed!");
      return;
    }

    if (CollectionUtils.isEmpty(lease.getPendingTransactions())) {
      LOG.warn("Seems a bug, the lease change a transaction in progress!");
      return;
    }

    ZonedDateTime leaseEnDate = (ZonedDateTime) Period.ofMonths(lease.getMonths())
                                                      .addTo(ZonedDateTime.ofInstant(lease.getCreatedDate(),
                                                                                     ZoneId.systemDefault()));
    String transactionHash = lease.getPendingTransactions().get(0);
    try {
      offerService.markOfferAcquisitionInProgress(lease.getNftId(), transactionHash, leaseEnDate.toInstant());
    } catch (ObjectAlreadyExistsException e) {
      LOG.warn("Seems a bug, the transaction hash {} already exists in offers as acquisition in progress! Ignore adding the transaction hash.",
               transactionHash);
    }
  }

}
