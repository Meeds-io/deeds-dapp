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
import io.meeds.deeds.contract.DeedTenantProvisioning;
import io.meeds.deeds.contract.ERC20;
import io.meeds.deeds.contract.MeedsToken;
import io.meeds.deeds.contract.TokenFactory;
import io.meeds.deeds.contract.XMeedsNFTRewarding;

@Configuration
public class BlockchainConfiguration {

  private static final BigInteger           GAS_PRICE             = BigInteger.valueOf(20000000000l);

  private static final BigInteger           GAS_LIMIT             = BigInteger.valueOf(300000l);

  private static final StaticGasProvider    CONTRACT_GAS_PROVIDER = new StaticGasProvider(GAS_PRICE, GAS_LIMIT);

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
  public DeedTenantProvisioning getDeedTenantProvisioning(
                                                          @Qualifier("ethereumNetwork")
                                                          Web3j web3j) {
    return DeedTenantProvisioning.load(properties.getTenantProvisioningAddress(),
                                       web3j,
                                       getTransactionManager(web3j),
                                       CONTRACT_GAS_PROVIDER);
  }

  @Bean
  public Deed getDeed(
                      @Qualifier("ethereumNetwork")
                      Web3j web3j) {
    return Deed.load(properties.getDeedAddress(),
                     web3j,
                     getTransactionManager(web3j),
                     CONTRACT_GAS_PROVIDER);
  }

  @Bean
  public TokenFactory getTokenFactory(
                                      @Qualifier("ethereumNetwork")
                                      Web3j web3j) {
    return TokenFactory.load(properties.getTokenFactoryAddress(),
                             web3j,
                             getTransactionManager(web3j),
                             CONTRACT_GAS_PROVIDER);
  }

  @Bean
  public XMeedsNFTRewarding getXMeedsNFTRewarding(
                                                  @Qualifier("ethereumNetwork")
                                                  Web3j web3j) {
    return XMeedsNFTRewarding.load(properties.getXMeedAddress(),
                                   web3j,
                                   getTransactionManager(web3j),
                                   CONTRACT_GAS_PROVIDER);
  }

  @Bean("sushiPairToken")
  public ERC20 getSushiPairToken(
                                 @Qualifier("ethereumNetwork")
                                 Web3j web3j) {
    return ERC20.load(properties.getSushiPairAddress(),
                      web3j,
                      getTransactionManager(web3j),
                      CONTRACT_GAS_PROVIDER);
  }

  @Bean("ethereumMeedToken")
  public MeedsToken getMainnetMeedToken(
                                        @Qualifier("ethereumNetwork")
                                        Web3j web3j) {
    return MeedsToken.load(properties.getMeedAddress(),
                           web3j,
                           getTransactionManager(web3j),
                           CONTRACT_GAS_PROVIDER);
  }

  @Bean("polygonMeedToken")
  public MeedsToken getPolygonMeedToken(
                                        @Qualifier("polygonNetwork")
                                        Web3j web3j) {
    return MeedsToken.load(properties.getPolygonMeedAddress(),
                           web3j,
                           getTransactionManager(web3j),
                           CONTRACT_GAS_PROVIDER);
  }

  private ReadonlyTransactionManager getTransactionManager(Web3j web3j) {
    return new ReadonlyTransactionManager(web3j, Address.DEFAULT.toString());
  }

}
