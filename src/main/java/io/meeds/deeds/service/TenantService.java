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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.meeds.deeds.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.meeds.deeds.constant.TenantCommand;
import io.meeds.deeds.constant.TenantStatus;
import io.meeds.deeds.constant.UnauthorizedOperationException;

@Component
public class TenantService {

  private Map<Long, TenantStatus> tenantStatus = new HashMap<>();

  /**
   * Retrieves deed status from SaaS hosting platform
   * 
   * @param networkId Etheureum chain Id
   * @param deedAddress DEED contract address
   * @param deedId DEED NFT Id
   * @return {@link TenantStatus}
   */
  public TenantStatus getStatus(long networkId,
                                String deedAddress,
                                long deedId) {
    // TODO retrieve status from Rancher
    return tenantStatus.computeIfAbsent(deedId, id -> TenantStatus.STOPPED);
  }

  /**
   * Changes the status of DEED NFT corresponding host. This will send a command
   * to SaaS hosting platform to stop or start the host.
   * 
   * @param networkId Etheureum chain Id
   * @param deedAddress DEED contract address
   * @param deedId DEED NFT Id
   * @param status Tenant Status to change to
   * @param walletAddress wallet Address used to change Tenant Status on SaaS
   *          platform
   * @throws UnauthorizedOperationException when wallet address isn't allowed to
   *           change host status
   */
  public void changeStatus(long networkId,
                           String deedAddress,
                           long deedId,
                           TenantCommand status,
                           String walletAddress) throws UnauthorizedOperationException {
    if (!isManager(networkId, deedAddress, deedId, walletAddress)) {
      throw new UnauthorizedOperationException();
    }

    // TODO change status on Rancher
    TenantStatus newStatus = status == TenantCommand.START ? TenantStatus.STARTED : TenantStatus.STOPPED;
    tenantStatus.put(deedId, newStatus);
  }

  /**
   * Check if user having corresponding wallet address has the role manager on
   * designated Tenant Host
   * 
   * @param networkId Etheureum chain Id
   * @param deedAddress DEED contract address
   * @param deedId DEED NFT Id
   * @param walletAddress wallet Address (wallet public key)
   * @return true is the user is a manager
   */
  public boolean isManager(long networkId,
                           String deedAddress,
                           long deedId,
                           String walletAddress) {
    // TODO check
    return true;
  }

}
