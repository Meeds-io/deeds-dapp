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

import static io.meeds.deeds.common.service.HubReportService.HUB_REPORT_RECEIVED;
import static io.meeds.deeds.common.service.HubReportService.HUB_REPORT_SAVED;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.meeds.dapp.model.DeedTenantLeaseDTO;
import io.meeds.dapp.service.LeaseService;
import io.meeds.deeds.common.listener.EventListener;
import io.meeds.deeds.common.service.HubReportService;
import io.meeds.deeds.common.service.UEMRewardComputingService;
import io.meeds.wom.api.model.HubReport;

@Service
public class HubReportSavedListener implements EventListener<String> {

  public static final String        LISTENER_NAME    = "HubReportSaved";

  private static final List<String> SUPPORTED_EVENTS = Arrays.asList(HUB_REPORT_SAVED,
                                                                     HUB_REPORT_RECEIVED);

  @Autowired
  private UEMRewardComputingService rewardComputingService;

  @Autowired
  private HubReportService          reportService;

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
  public void onEvent(String eventName, String hash) {
    HubReport report = reportService.getReport(hash);
    if (StringUtils.equals(HUB_REPORT_RECEIVED, eventName)) {
      computeLeaseProperties(report);
    }
    rewardComputingService.computeUEMReward(report.getSentDate());
  }

  private void computeLeaseProperties(HubReport report) {
    DeedTenantLeaseDTO lease = leaseService.getCurrentLease(report.getDeedId());
    if (lease != null && lease.isConfirmed()) {
      report.setDeedManagerAddress(lease.getManagerAddress());
      report.setOwnerAddress(lease.getOwnerAddress());
      report.setOwnerMintingPercentage(lease.getOwnerMintingPercentage());
      reportService.saveReportLeaseStatus(report);
    }
  }

}