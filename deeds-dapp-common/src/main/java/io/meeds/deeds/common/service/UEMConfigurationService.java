/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.deeds.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.meeds.deeds.common.model.UEMConfiguration;

@Component
public class UEMConfigurationService {

  @Autowired
  private BlockchainService blockchainService;

  @Value("${meeds.wom.url:https://wom.meeds.io}")
  private String            womUrl;

  private UEMConfiguration  uemConfiguration;

  public UEMConfiguration getConfiguration() {
    if (uemConfiguration == null) {
      uemConfiguration = new UEMConfiguration(blockchainService.getNetworkId(),
                                              blockchainService.getPolygonNetworkId(),
                                              blockchainService.getEthereumMeedTokenAddress(),
                                              blockchainService.getPolygonMeedTokenAddress(),
                                              blockchainService.getUemAddress(),
                                              womUrl,
                                              blockchainService.getUemPeriodicRewardAmount());
    }
    return uemConfiguration;
  }

  public void resetUemRewardAmount() {
    this.uemConfiguration = null;
  }

}
