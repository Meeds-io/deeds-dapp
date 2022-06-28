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
package io.meeds.deeds.blockchain;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.gas.StaticGasProvider;

import io.meeds.deeds.contract.Deed;
import io.meeds.deeds.contract.MeedsToken;
import io.meeds.deeds.contract.TenantProvisioningStrategy;

@Configuration
public class BlockchainConfiguration {

  @Autowired
  private BlockchainConfigurationProperties properties;

  @Bean("ethereumNetwork")
  public Web3j getMainnetNetworkWeb3j() {
    return Web3j.build(new HttpService(properties.getNetworkUrl()));
  }

  @Bean("polygonNetwork")
  public Web3j getPolygonNetworkWeb3j() {
    return Web3j.build(new HttpService(properties.getPolygonNetworkUrl()));
  }

  @Bean
  public TenantProvisioningStrategy getTenantProvisioningStrategy(
                                                                  @Qualifier("ethereumNetwork")
                                                                  Web3j web3j) {
    BigInteger gasPrice = BigInteger.valueOf(20000000000l);
    BigInteger gasLimit = BigInteger.valueOf(300000l);
    return TenantProvisioningStrategy.load(properties.getTenantProvisioningAddress(),
                                           web3j,
                                           new ReadonlyTransactionManager(web3j,
                                                                          Address.DEFAULT.toString()),
                                           new StaticGasProvider(gasPrice, gasLimit));
  }

  @Bean
  public Deed getDeed(
                      @Qualifier("ethereumNetwork")
                      Web3j web3j) {
    BigInteger gasPrice = BigInteger.valueOf(20000000000l);
    BigInteger gasLimit = BigInteger.valueOf(300000l);
    return Deed.load(properties.getDeedAddress(),
                     web3j,
                     new ReadonlyTransactionManager(web3j,
                                                    Address.DEFAULT.toString()),
                     new StaticGasProvider(gasPrice, gasLimit));
  }

  @Bean("ethereumMeedToken")
  public MeedsToken getMainnetMeedToken(
                                        @Qualifier("ethereumNetwork")
                                        Web3j web3j) {
    BigInteger gasPrice = BigInteger.valueOf(20000000000l);
    BigInteger gasLimit = BigInteger.valueOf(300000l);
    return MeedsToken.load(properties.getMainnetMeedAddress(),
                           web3j,
                           new ReadonlyTransactionManager(web3j,
                                                          Address.DEFAULT.toString()),
                           new StaticGasProvider(gasPrice, gasLimit));
  }

  @Bean("polygonMeedToken")
  public MeedsToken getPolygonMeedToken(
                                        @Qualifier("polygonNetwork")
                                        Web3j web3j) {
    BigInteger gasPrice = BigInteger.valueOf(20000000000l);
    BigInteger gasLimit = BigInteger.valueOf(300000l);
    return MeedsToken.load(properties.getPolygonMeedAddress(),
                           web3j,
                           new ReadonlyTransactionManager(web3j,
                                                          Address.DEFAULT.toString()),
                           new StaticGasProvider(gasPrice, gasLimit));
  }

}
