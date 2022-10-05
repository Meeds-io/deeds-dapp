/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
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
package io.meeds.deeds.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.meeds.deeds.model.FundInfo;
import io.meeds.deeds.model.MeedAssetsMetrics;

@Component
public class MeedAssetsMetricsService {

  @Autowired
  private BlockchainService   blockchainService;

  protected MeedAssetsMetrics meedAssetsMetrics;

  public MeedAssetsMetrics getMeedAssetsMetrics(boolean forceCompute) {
    if (meedAssetsMetrics == null || forceCompute) {
      computeMeedAssetsMetrics();
    }
    return meedAssetsMetrics;
  }

  public synchronized void computeMeedAssetsMetrics() {
    MeedAssetsMetrics metrics = new MeedAssetsMetrics();
    addPools(metrics);
    metrics.setTotalAllocationPoints(blockchainService.totalAllocationPoints());
    metrics.setTotalFixedPercentage(blockchainService.totalFixedPercentage());
    metrics.setCurrentCity(blockchainService.getCurrentCity());
    this.meedAssetsMetrics = metrics;
  }

  private void addPools(MeedAssetsMetrics metrics) {
    ArrayList<FundInfo> pools = new ArrayList<>();
    FundInfo xMeedFundInfo = blockchainService.getXMeedFundInfo();
    if (xMeedFundInfo != null) {
      pools.add(xMeedFundInfo);
    }
    FundInfo sushiPairFundInfo = blockchainService.getSushiPairFundInfo();
    if (sushiPairFundInfo != null) {
      pools.add(sushiPairFundInfo);
    }
    metrics.setPools(pools);
  }

}
