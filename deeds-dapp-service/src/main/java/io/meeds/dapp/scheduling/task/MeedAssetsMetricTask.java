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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.meeds.dapp.service.MeedAssetsMetricsService;

@Component
public class MeedAssetsMetricTask {

  private static final Logger      LOG = LoggerFactory.getLogger(MeedAssetsMetricTask.class);

  @Autowired
  private MeedAssetsMetricsService meedAssetsMetricsService;

  @Scheduled(cron = "${meeds.token.assets.cron:0 0/15 * * * *}")
  public void computeMeedAssetsMetrics() {
    LOG.info("Start Computing Meed Assets Metrics for Investors app offline access");
    long start = System.currentTimeMillis();
    try {
      meedAssetsMetricsService.computeMeedAssetsMetrics();
      LOG.info("End Computing Meed Assets Metrics in {}ms", System.currentTimeMillis() - start);
    } catch (Exception e) {
      LOG.warn("An error occurred while computing Meed Assets Metrics", e);
    }
  }

}
