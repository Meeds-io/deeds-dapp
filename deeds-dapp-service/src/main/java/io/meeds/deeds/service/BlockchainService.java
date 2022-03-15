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
package io.meeds.deeds.service;

import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.meeds.deeds.constant.ObjectNotFoundException;
import io.meeds.deeds.contract.Deed;
import io.meeds.deeds.contract.TenantProvisioningStrategy;

@Component
public class BlockchainService {

  @Autowired
  private TenantProvisioningStrategy tenantProvisioningStrategy;

  @Autowired
  private Deed                       deed;

  /**
   * Retrieves from blockchain whether an address is the provisioning manager of
   * the deed or not
   * 
   * @param address Ethereum address to check
   * @param nftId Deed NFT identifier
   * @return true if is manager else false
   */
  public boolean isDeedProvisioningManager(String address, long nftId) {
    try {
      return tenantProvisioningStrategy.isProvisioningManager(address, BigInteger.valueOf(nftId)).send();
    } catch (Exception e) {
      throw new IllegalStateException("Error retrieving information 'isTenantProvisioningManager' from Blockchain", e);
    }
  }

  /**
   * Retrieves from Blockchain DEED card type:
   * - 0 : Common
   * - 1 : Uncommon
   * - 2 : Rare
   * - 3 : Legendary
   * 
   * @param nftId Deed NFT identifier
   * @return card type index
   * @throws ObjectNotFoundException when NFT with selected identifier doesn't
   *           exists
   */
  public short getDeedCardType(long nftId) throws ObjectNotFoundException {
    try {
      return deed.cardType(BigInteger.valueOf(nftId)).send().shortValue();
    } catch (Exception e) {
      if (StringUtils.contains(e.getMessage(), "execution reverted")) {
        throw new ObjectNotFoundException(e.getMessage());
      } else {
        throw new IllegalStateException("Error retrieving information 'getDeedCardType' from Blockchain", e);
      }
    }
  }

  /**
   * Retrieves from Blockchain DEED city index:
   * - 0 : Tanit
   * - 1 : Reshef
   * - 2 : Ashtarte
   * - 3 : Melqart
   * - 4 : Eshmun
   * - 5 : Kushor
   * - 6 : Hammon
   * 
   * @param nftId Deed NFT identifier
   * @return card city index
   * @throws ObjectNotFoundException when NFT with selected identifier doesn't
   *           exists
   */
  public short getDeedCityIndex(long nftId) throws ObjectNotFoundException {
    try {
      return deed.cityIndex(BigInteger.valueOf(nftId)).send().shortValue();
    } catch (Exception e) {
      if (StringUtils.contains(e.getMessage(), "execution reverted")) {
        throw new ObjectNotFoundException(e.getMessage());
      } else {
        throw new IllegalStateException("Error retrieving information 'getDeedCityIndex' from Blockchain", e);
      }
    }
  }

}
