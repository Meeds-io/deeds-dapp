/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
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

import static io.meeds.dapp.service.UEMRewardComputingService.UEM_HUB_REPORT_REWARD_COMPUTED;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.meeds.deeds.api.model.HubReport;
import io.meeds.deeds.common.listener.EventListener;
import io.meeds.deeds.common.service.HubReportService;
import io.meeds.deeds.common.service.HubService;

@Service
public class HubRewardComptedListener implements EventListener<String> {

  public static final String        LISTENER_NAME    = "HubReportComputed";

  private static final List<String> SUPPORTED_EVENTS = Arrays.asList(UEM_HUB_REPORT_REWARD_COMPUTED);

  @Autowired
  private HubReportService          reportService;

  @Autowired
  private HubService                hubService;

  @Override
  public String getName() {
    return LISTENER_NAME;
  }

  @Override
  public List<String> getSupportedEvents() {
    return SUPPORTED_EVENTS;
  }

  @Override
  public void onEvent(String eventName, String hash) {
    HubReport report = reportService.getReport(hash);
    hubService.saveHubUEMProperties(report.getHubAddress(), report);
  }

}
