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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.meeds.deeds.constant.UnauthorizedOperationException;
import io.meeds.deeds.elasticsearch.model.DeedTenantManager;
import io.meeds.deeds.elasticsearch.storage.DeedTenantManagerRepository;

@Component
public class TenantService {

  @Autowired
  private DeedTenantManagerRepository deedTenantManagerRepository;

  @Autowired
  private BlockchainService           blockchainService;

  /**
   * Stores User Email to allow support Team Contact him and notify him about
   * Tenant Status
   * 
   * @param nftId DEED NFT id in the blockchain
   * @param managerAddress DEED Provisioning Manager wallet address
   * @param email Email of the manager
   * @throws UnauthorizedOperationException when the wallet isn't the DEED
   *           manager
   */
  public void saveEmail(String managerAddress, long nftId, String email) throws UnauthorizedOperationException {
    if (!isDeedManager(managerAddress, nftId)) {
      throw new UnauthorizedOperationException("User with address " + managerAddress + " isn't the manager of deed " + nftId);
    }
    if (StringUtils.isBlank(email)) {
      deedTenantManagerRepository.deleteById(nftId);
    } else {
      DeedTenantManager deedTenantManager = new DeedTenantManager(nftId, StringUtils.lowerCase(managerAddress), email);
      deedTenantManagerRepository.save(deedTenantManager);
    }
  }

  /**
   * Checks if address is the provisioning manager of the DEED
   * 
   * @param nftId DEED NFT identifier
   * @param address Wallet or Contract Ethereum address
   * @return true if address is the provisioning manager of the DEED Tenant
   */
  public boolean isDeedManager(String address, long nftId) {
    return blockchainService.isTenantProvisioningManager(address, nftId);
  }

}
