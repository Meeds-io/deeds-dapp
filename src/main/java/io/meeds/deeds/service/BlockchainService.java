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

import java.lang.reflect.Method;
import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.gas.StaticGasProvider;

import io.meeds.deeds.contract.TenantProvisioningStrategy;

@Component
public class BlockchainService {

  @Value("${meeds.networkUrl}")
  private String                     networkUrl;

  @Value("${meeds.tenantProvisioningAddress}")
  private String                     tenantProvisioningAddress;

  private Web3j                      web3j;

  private TenantProvisioningStrategy tenantProvisioningStrategy;

  public boolean isTenantProvisioningManager(String address, long nftId) {
    Object result = executeReadOperation(tenantProvisioningAddress,
                                         TenantProvisioningStrategy.FUNC_ISPROVISIONINGMANAGER,
                                         address,
                                         BigInteger.valueOf(nftId));
    return result != null && Boolean.parseBoolean(result.toString());
  }

  public Object executeReadOperation(final String contractAddress,
                                     final String methodName,
                                     final Object... arguments) {
    TenantProvisioningStrategy contractInstance = getContractInstance(contractAddress);
    Method methodToInvoke = getMethod(methodName);
    if (methodToInvoke == null) {
      throw new IllegalStateException("Can't find method " + methodName + " in Token instance");
    }
    try {
      RemoteCall<?> response = (RemoteCall<?>) methodToInvoke.invoke(contractInstance, arguments);
      return response.send();
    } catch (Exception e) {
      throw new IllegalStateException("Error retrieving information from Blockchain", e);
    }
  }

  private TenantProvisioningStrategy getContractInstance(String contractAddress) {
    if (this.tenantProvisioningStrategy == null) {
      BigInteger gasPrice = BigInteger.valueOf(20000000000l);
      BigInteger gasLimit = BigInteger.valueOf(300000l);
      this.tenantProvisioningStrategy = TenantProvisioningStrategy.load(contractAddress,
                                                                        getWeb3j(),
                                                                        new ReadonlyTransactionManager(web3j,
                                                                                                       Address.DEFAULT.toString()),
                                                                        new StaticGasProvider(gasPrice, gasLimit));
    }
    return this.tenantProvisioningStrategy;
  }

  private Method getMethod(String methodName) {
    Method methodToInvoke = null;
    Method[] methods = TenantProvisioningStrategy.class.getDeclaredMethods();
    for (Method method : methods) {
      if (StringUtils.equals(methodName, method.getName())) {
        methodToInvoke = method;
      }
    }
    return methodToInvoke;
  }

  public Web3j getWeb3j() {
    if (web3j == null) {
      web3j = Web3j.build(new HttpService(networkUrl));
    }
    return web3j;
  }

}
