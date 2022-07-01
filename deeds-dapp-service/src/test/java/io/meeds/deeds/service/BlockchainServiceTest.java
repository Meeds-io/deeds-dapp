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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;

import io.meeds.deeds.blockchain.BlockchainConfiguration;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteFunctionCall;

import io.meeds.deeds.constant.ObjectNotFoundException;
import io.meeds.deeds.contract.Deed;
import io.meeds.deeds.contract.MeedsToken;
import io.meeds.deeds.contract.TenantProvisioningStrategy;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

@SpringBootTest(
    classes = {
        BlockchainService.class,
    }
)
class BlockchainServiceTest {

  @MockBean
  private TenantProvisioningStrategy tenantProvisioningStrategy;

  @Mock
  private MeedsToken meedsToken;

  @MockBean
  private Deed                       deed;

  @MockBean(name = "ethereumMeedToken")
  private MeedsToken                 ethereumToken;

  @MockBean(name = "polygonMeedToken")
  private MeedsToken                 polygonToken;
  @Autowired
  private BlockchainService          blockchainService;

  @Test
  void testGetDeedCardType() throws Exception {
    assertNotNull(blockchainService);

    when(deed.cardType(any())).thenAnswer(invocation -> {
      BigInteger argument = invocation.getArgument(0, BigInteger.class);
      if (argument == null || argument.shortValue() > 1) {
        throw new IllegalStateException("execution reverted: nftId doesn't exist");
      } else if (argument.shortValue() == 0) {
        throw new RuntimeException();
      } else {
        @SuppressWarnings("unchecked")
        RemoteFunctionCall<BigInteger> remoteFunctionCall = mock(RemoteFunctionCall.class);
        when(remoteFunctionCall.send()).thenReturn(BigInteger.TWO);
        return remoteFunctionCall;
      }
    });

    assertThrows(IllegalStateException.class, () -> blockchainService.getDeedCardType(0));
    assertThrows(ObjectNotFoundException.class, () -> blockchainService.getDeedCardType(2));
    assertThrows(ObjectNotFoundException.class, () -> blockchainService.getDeedCardType(500));
    assertEquals(2, blockchainService.getDeedCardType(1));
  }

  @Test
  void testGetDeedCityIndex() throws Exception {
    assertNotNull(blockchainService);

    when(deed.cityIndex(any())).thenAnswer(invocation -> {
      BigInteger argument = invocation.getArgument(0, BigInteger.class);
      if (argument == null || argument.shortValue() > 1) {
        throw new IllegalStateException("execution reverted: nftId doesn't exist");
      } else if (argument.shortValue() == 0) {
        throw new RuntimeException();
      } else {
        @SuppressWarnings("unchecked")
        RemoteFunctionCall<BigInteger> remoteFunctionCall = mock(RemoteFunctionCall.class);
        when(remoteFunctionCall.send()).thenReturn(BigInteger.TWO);
        return remoteFunctionCall;
      }
    });

    assertThrows(IllegalStateException.class, () -> blockchainService.getDeedCityIndex(0));
    assertThrows(ObjectNotFoundException.class, () -> blockchainService.getDeedCityIndex(2));
    assertThrows(ObjectNotFoundException.class, () -> blockchainService.getDeedCityIndex(500));
    assertEquals(2, blockchainService.getDeedCityIndex(1));
  }

  @Test
  void testIsDeedProvisioningManager() throws Exception {
    assertNotNull(blockchainService);

    String walletAddress = "walletAddress";

    when(tenantProvisioningStrategy.isProvisioningManager(any(), any())).thenAnswer(invocation -> {
      String address = invocation.getArgument(0, String.class);
      BigInteger nftId = invocation.getArgument(1, BigInteger.class);
      if (nftId == null || nftId.shortValue() == 0 || !StringUtils.equals(address, walletAddress)) {
        throw new RuntimeException();
      } else {
        @SuppressWarnings("unchecked")
        RemoteFunctionCall<Boolean> remoteFunctionCall = mock(RemoteFunctionCall.class);
        when(remoteFunctionCall.send()).thenReturn(nftId.shortValue() == 1);
        return remoteFunctionCall;
      }
    });

    assertThrows(RuntimeException.class, () -> blockchainService.isDeedProvisioningManager(walletAddress, 0));
    assertTrue(blockchainService.isDeedProvisioningManager(walletAddress, 1));
    assertFalse(blockchainService.isDeedProvisioningManager(walletAddress, 2));
  }

  @Test
  void TestTotalSupply() throws Exception {
    assertNotNull(ethereumToken);
    when(ethereumToken.totalSupply()).thenAnswer(invocation -> {
      RemoteFunctionCall<BigInteger> remoteFunctionCall = mock(RemoteFunctionCall.class);
      when(remoteFunctionCall.send()).thenReturn(BigInteger.valueOf((long) 5008548.684083333333333319));
      return remoteFunctionCall;
    });
    assertNotNull(ethereumToken.totalSupply().send());
  }
  @Test
  void TestBalanceOfOnPolygon() throws Exception {
    assertNotNull(polygonToken);
    when(polygonToken.balanceOf("0x6acA77CF3BaB0C4E8210A09B57B07854a995289a")).thenAnswer(invocation -> {
      RemoteFunctionCall<Boolean> remoteFunctionCall = mock(RemoteFunctionCall.class);
      when(remoteFunctionCall.send()).thenReturn(true);
      return remoteFunctionCall;
    });
    assertNotNull(polygonToken.balanceOf("0x6acA77CF3BaB0C4E8210A09B57B07854a995289a"));

  }
  @Test
  void TestBalanceOfOnEthereum() throws Exception {
    assertNotNull(polygonToken);
    when(ethereumToken.balanceOf("0x44D6d6aB50401Dd846336e9C706A492f06E1Bcd4")).thenAnswer(invocation -> {
    RemoteFunctionCall<Boolean> remoteFunctionCall = mock(RemoteFunctionCall.class);
    when(remoteFunctionCall.send()).thenReturn(true);
    return remoteFunctionCall;
  });
    assertNotNull(ethereumToken.balanceOf("0x44D6d6aB50401Dd846336e9C706A492f06E1Bcd4"));


  }

}
