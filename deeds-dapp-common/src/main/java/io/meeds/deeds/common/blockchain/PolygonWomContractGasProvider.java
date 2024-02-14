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
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.deeds.common.blockchain;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.tx.gas.ContractEIP1559GasProvider;

import lombok.SneakyThrows;

public class PolygonWomContractGasProvider implements ContractEIP1559GasProvider {

  private static final Logger     LOG       = LoggerFactory.getLogger(PolygonWomContractGasProvider.class);

  private static final BigInteger GAS_LIMIT = BigInteger.valueOf(300000);

  private Web3j                   web3j;

  private long                    chainId;

  public PolygonWomContractGasProvider(Web3j web3j) {
    this.web3j = web3j;
  }

  @Override
  public long getChainId() {
    if (chainId == 0) {
      try {
        chainId = web3j.ethChainId().send().getChainId().longValue();
      } catch (IOException e) {
        LOG.warn("Error retrieving Network Identifier", e);
        return 137l; // Polygon Mainnet in case it fails on production
      }
    }
    return chainId;
  }

  @Override
  @SneakyThrows
  public BigInteger getGasPrice() {
    BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
    return new BigDecimal(gasPrice).multiply(BigDecimal.valueOf(1.2))
                                   .setScale(0, RoundingMode.HALF_EVEN)
                                   .toBigInteger();
  }

  @Override
  @SneakyThrows
  public BigInteger getMaxFeePerGas(String contractFunc) {
    return getGasPrice().add(getMaxPriorityFeePerGas(contractFunc));
  }

  @Override
  @SneakyThrows
  public BigInteger getMaxPriorityFeePerGas(String contractFunc) {
    return web3j.ethMaxPriorityFeePerGas().send().getMaxPriorityFeePerGas();
  }

  @Override
  public BigInteger getGasPrice(String contractFunc) {
    return getGasPrice();
  }

  @Override
  public BigInteger getGasLimit(String contractFunc) {
    return getGasLimit();
  }

  @Override
  public BigInteger getGasLimit() {
    return GAS_LIMIT;
  }

  @Override
  public boolean isEIP1559Enabled() {
    return true;
  }

}
