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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.gas.StaticGasProvider;

import io.meeds.deeds.contract.Deed;
import io.meeds.deeds.contract.TenantProvisioningStrategy;

@Component
public class BlockchainService {

  @Value("${meeds.networkUrl}")
  private String                     networkUrl;

  @Value("${meeds.tenantProvisioningAddress}")
  private String                     tenantProvisioningAddress;

  @Value("${meeds.deedAddress}")
  private String                     deedAddress;

  private TenantProvisioningStrategy tenantProvisioningStrategy;

  private Deed                       deed;

  private Web3j                      web3j;

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
      TenantProvisioningStrategy contractInstance = getTenantProvisioningStrategy();
      return contractInstance.isProvisioningManager(address, BigInteger.valueOf(nftId)).send();
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
   */
  public short getDeedCardType(long nftId) {
    try {
      Deed contractInstance = getDeed();
      return contractInstance.cardType(BigInteger.valueOf(nftId)).send().shortValue();
    } catch (Exception e) {
      throw new IllegalStateException("Error retrieving information 'getDeedCardType' from Blockchain", e);
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
   */
  public short getDeedCityIndex(long nftId) {
    try {
      Deed contractInstance = getDeed();
      return contractInstance.cityIndex(BigInteger.valueOf(nftId)).send().shortValue();
    } catch (Exception e) {
      throw new IllegalStateException("Error retrieving information 'getDeedCityIndex' from Blockchain", e);
    }
  }

  private TenantProvisioningStrategy getTenantProvisioningStrategy() {
    if (this.tenantProvisioningStrategy == null) {
      BigInteger gasPrice = BigInteger.valueOf(20000000000l);
      BigInteger gasLimit = BigInteger.valueOf(300000l);
      this.tenantProvisioningStrategy = TenantProvisioningStrategy.load(tenantProvisioningAddress,
                                                                        getWeb3j(),
                                                                        new ReadonlyTransactionManager(getWeb3j(),
                                                                                                       Address.DEFAULT.toString()),
                                                                        new StaticGasProvider(gasPrice, gasLimit));
    }
    return this.tenantProvisioningStrategy;
  }

  private Deed getDeed() {
    if (this.deed == null) {
      BigInteger gasPrice = BigInteger.valueOf(20000000000l);
      BigInteger gasLimit = BigInteger.valueOf(300000l);
      this.deed = Deed.load(deedAddress,
                            getWeb3j(),
                            new ReadonlyTransactionManager(getWeb3j(),
                                                           Address.DEFAULT.toString()),
                            new StaticGasProvider(gasPrice, gasLimit));
    }
    return this.deed;
  }

  private Web3j getWeb3j() {
    if (web3j == null) {
      web3j = Web3j.build(new HttpService(networkUrl));
    }
    return web3j;
  }

}
