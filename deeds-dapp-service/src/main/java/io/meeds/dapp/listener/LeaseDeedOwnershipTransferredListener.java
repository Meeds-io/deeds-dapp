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
package io.meeds.dapp.listener;

import static io.meeds.deeds.constant.CommonConstants.DEED_TENANT_OWNERSHIP_TRANSFERRED_EVENT;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.meeds.dapp.service.LeaseService;
import io.meeds.deeds.constant.CommonConstants.DeedOwnershipTransferEvent;
import io.meeds.deeds.listener.EventListener;

@Service
public class LeaseDeedOwnershipTransferredListener implements EventListener<DeedOwnershipTransferEvent> {

  private static final Logger       LOG              = LoggerFactory.getLogger(LeaseDeedOwnershipTransferredListener.class);

  public static final String        LISTENER_NAME    = "DeedOwnershipTransferredLeaseListener";

  private static final List<String> SUPPORTED_EVENTS = Collections.singletonList(DEED_TENANT_OWNERSHIP_TRANSFERRED_EVENT);

  @Autowired
  private LeaseService              leaseService;

  @Override
  public String getName() {
    return LISTENER_NAME;
  }

  @Override
  public List<String> getSupportedEvents() {
    return SUPPORTED_EVENTS;
  }

  @Override
  public void onEvent(String eventName, DeedOwnershipTransferEvent ownershipTransferEvent) {
    String oldOwner = ownershipTransferEvent.getFrom();
    String newOnwer = ownershipTransferEvent.getTo();
    if (StringUtils.equalsIgnoreCase(oldOwner, newOnwer)) {
      return;
    }
    long nftId = ownershipTransferEvent.getNftId();
    try {
      leaseService.transferDeedOwnership(newOnwer, nftId);
    } catch (Exception e) {
      LOG.warn("Owner modification on current leases for new owner {} had failed", newOnwer, e);
    }
  }

}