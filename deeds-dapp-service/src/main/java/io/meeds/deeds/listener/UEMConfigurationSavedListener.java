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

import static io.meeds.deeds.common.service.UEMConfigurationService.UEM_CONFIGURATION_SAVED;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.meeds.deeds.common.listener.EventListener;
import io.meeds.deeds.common.service.UEMRewardComputingService;

@Service
public class UEMConfigurationSavedListener implements EventListener<Object> {

  public static final String        LISTENER_NAME    = "UEMConfigurationSaved";

  private static final List<String> SUPPORTED_EVENTS = Arrays.asList(UEM_CONFIGURATION_SAVED);

  @Autowired
  private UEMRewardComputingService rewardComputingService;

  @Override
  public String getName() {
    return LISTENER_NAME;
  }

  @Override
  public List<String> getSupportedEvents() {
    return SUPPORTED_EVENTS;
  }

  @Override
  public void onEvent(String eventName, Object config) {
    rewardComputingService.computePendingRewards();
  }

}
