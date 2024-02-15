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
import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthGetCode;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;

import lombok.Setter;
import lombok.SneakyThrows;

public class PolygonTransactionManagerProxy extends TransactionManager {

  private static final Logger       LOG = LoggerFactory.getLogger(PolygonTransactionManagerProxy.class);

  @Setter
  private static TransactionManager transactionManager;

  @Setter
  private static Web3j              web3j;

  public PolygonTransactionManagerProxy() {
    super(new PollingTransactionReceiptProcessor(web3j,
                                                 DEFAULT_POLLING_FREQUENCY,
                                                 DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH),
          null);
  }

  @SneakyThrows
  public static void initTransactionManager(ECKeyPair ecKeyPair) {
    transactionManager = new PolygonWomRawTransactionManager(web3j,
                                                             Credentials.create(ecKeyPair),
                                                             getPolygonNetworkId());
  }

  @Override
  public EthSendTransaction sendTransaction(BigInteger gasPrice,
                                            BigInteger gasLimit,
                                            String to,
                                            String data,
                                            BigInteger value,
                                            boolean constructor) throws IOException {
    return transactionManager.sendTransaction(gasPrice, gasLimit, to, data, value, constructor);
  }

  @Override
  public EthSendTransaction sendEIP1559Transaction(long chainId,
                                                   BigInteger maxPriorityFeePerGas,
                                                   BigInteger maxFeePerGas,
                                                   BigInteger gasLimit,
                                                   String to,
                                                   String data,
                                                   BigInteger value,
                                                   boolean constructor) throws IOException {
    return transactionManager.sendEIP1559Transaction(chainId,
                                                     maxPriorityFeePerGas,
                                                     maxFeePerGas,
                                                     gasLimit,
                                                     to,
                                                     data,
                                                     value,
                                                     constructor);
  }

  @Override
  public String sendCall(String to, String data, DefaultBlockParameter defaultBlockParameter) throws IOException {
    return transactionManager.sendCall(to, data, defaultBlockParameter);
  }

  @Override
  public EthGetCode getCode(String contractAddress, DefaultBlockParameter defaultBlockParameter) throws IOException {
    return transactionManager.getCode(contractAddress, defaultBlockParameter);
  }

  @Override
  public EthSendTransaction sendTransaction(
                                            BigInteger gasPrice,
                                            BigInteger gasLimit,
                                            String to,
                                            String data,
                                            BigInteger value)
                                                              throws IOException {
    return transactionManager.sendTransaction(gasPrice, gasLimit, to, data, value);
  }

  @Override
  public EthSendTransaction sendEIP1559Transaction(long chainId,
                                                   BigInteger maxPriorityFeePerGas,
                                                   BigInteger maxFeePerGas,
                                                   BigInteger gasLimit,
                                                   String to,
                                                   String data,
                                                   BigInteger value)
                                                                     throws IOException {
    return transactionManager.sendEIP1559Transaction(chainId, maxPriorityFeePerGas, maxFeePerGas, gasLimit, to, data, value);
  }

  @Override
  public String getFromAddress() {
    return transactionManager.getFromAddress();
  }

  private static long getPolygonNetworkId() {
    try {
      return web3j.ethChainId().send().getChainId().longValue();
    } catch (IOException e) {
      LOG.warn("Error retrieving Network Identifier", e);
      return 137l; // Polygon Mainnet in case it fails on production
    }
  }

}
