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

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.tx.RawTransactionManager;

/**
 * Override default Raw Transaction to ensure to use the latest mined nonce
 * instead of pending, in order to replace transactions
 */
public class PolygonWomRawTransactionManager extends RawTransactionManager {

  private final Web3j web3j;

  public PolygonWomRawTransactionManager(Web3j web3j,
                                         Credentials credentials,
                                         long polygonNetworkId) {
    super(web3j, credentials, polygonNetworkId);
    this.web3j = web3j;
  }

  @Override
  protected BigInteger getNonce() throws IOException {
    EthGetTransactionCount ethGetTransactionCount =
                                                  web3j.ethGetTransactionCount(this.getFromAddress(),
                                                                               DefaultBlockParameterName.LATEST)
                                                       .send();

    return ethGetTransactionCount.getTransactionCount();
  }

}
