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
package io.meeds.deeds.common.scheduling.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.meeds.deeds.common.service.ListenerService;

@Component
public class ListenerEventCleanupTask {

  private static final Logger    LOG = LoggerFactory.getLogger(ListenerEventCleanupTask.class);

  @Autowired(required = false)
  private ListenerService listenerService;

  @Scheduled(cron = "0 0 0/12 * * *")
  public synchronized void triggerCleanupEvents() {
    if (listenerService == null) {
      return;
    }
    try {
      listenerService.cleanupElasticsearchEvents();
    } catch (Exception e) {
      LOG.warn("An error occurred while cleaning events on elasticsearch", e);
    }
  }

}
