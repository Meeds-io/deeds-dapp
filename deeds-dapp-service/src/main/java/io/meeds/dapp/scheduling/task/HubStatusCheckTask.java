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
package io.meeds.dapp.scheduling.task;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.meeds.deeds.api.model.Hub;
import io.meeds.deeds.common.service.HubService;

@Component
public class HubStatusCheckTask {

  private static final int PAGE_SIZE = 10;

  @Autowired
  private HubService       hubService;

  @Scheduled(cron = "${meeds.hub.checkStatus.cron:0 0 0/3 * * *}")
  public void checkHubsStatus() {
    Page<Hub> hubs = hubService.getHubs(PageRequest.of(0, PAGE_SIZE));
    while (hubs.getSize() > 0) {
      for (Hub hub : hubs) {
        if (StringUtils.isNotBlank(hub.getAddress())
            && !hubService.isDeedManager(hub.getDeedManagerAddress(), hub.getDeedId())) {
          hubService.disableDeedHubCommunity(hub.getAddress());
        }
      }
      hubs = hubService.getHubs(hubs.nextPageable());
    }
  }

}
