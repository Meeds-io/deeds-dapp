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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.deeds.service;

import static org.assertj.core.api.Assertions.assertWith;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.math.BigInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.CollectionUtils;

import io.meeds.deeds.model.DeedCity;
import io.meeds.deeds.model.FundInfo;

@SpringBootTest(classes = { MeedAssetsMetricsService.class })
class MeedAssetsMetricsServiceTest {

  @MockBean
  private BlockchainService        blockchainService;

  @Autowired
  private MeedAssetsMetricsService meedAssetsMetricsService;

  @BeforeEach
  void init() {
    reset(blockchainService);
    meedAssetsMetricsService.meedAssetsMetrics = null;
  }

  @Test
  void testGetMeedAssetsMetrics() {
    testEmptyMetrics();

    BigInteger expectedTotalAllocationPoints = BigInteger.ONE;
    BigInteger expectedTotalFixedPercentage = BigInteger.TWO;
    DeedCity expectedCity = new DeedCity(BigInteger.valueOf(3),
                                         "Tanit",
                                         BigInteger.valueOf(4),
                                         BigInteger.valueOf(5),
                                         BigInteger.valueOf(6));
    FundInfo expectedXMeedFund = new FundInfo("address",
                                              BigInteger.valueOf(3),
                                              BigInteger.valueOf(4),
                                              BigInteger.valueOf(5),
                                              BigInteger.valueOf(6),
                                              false);
    FundInfo expectedSushiPool = new FundInfo("poolAddress",
                                              BigInteger.valueOf(30),
                                              BigInteger.valueOf(40),
                                              BigInteger.valueOf(50),
                                              BigInteger.valueOf(60),
                                              true);

    when(blockchainService.totalAllocationPoints()).thenReturn(expectedTotalAllocationPoints);
    when(blockchainService.totalFixedPercentage()).thenReturn(expectedTotalFixedPercentage);
    when(blockchainService.getCurrentCity()).thenReturn(expectedCity);
    when(blockchainService.getXMeedFundInfo()).thenReturn(expectedXMeedFund);
    when(blockchainService.getSushiPairFundInfo()).thenReturn(expectedSushiPool);

    testEmptyMetrics();

    assertWith(meedAssetsMetricsService.getMeedAssetsMetrics(true), meedAssetsMetrics -> {
      assertNotNull(meedAssetsMetrics);
      assertEquals(expectedCity, meedAssetsMetrics.getCurrentCity());
      assertEquals(expectedTotalAllocationPoints, meedAssetsMetrics.getTotalAllocationPoints());
      assertEquals(expectedTotalFixedPercentage, meedAssetsMetrics.getTotalFixedPercentage());
      assertEquals(2, meedAssetsMetrics.getPools().size());
      assertTrue(meedAssetsMetrics.getPools().stream().anyMatch(fund -> fund.equals(expectedXMeedFund)));
      assertTrue(meedAssetsMetrics.getPools().stream().anyMatch(fund -> fund.equals(expectedSushiPool)));
    });
  }

  private void testEmptyMetrics() {
    assertWith(meedAssetsMetricsService.getMeedAssetsMetrics(false), meedAssetsMetrics -> {
      assertNotNull(meedAssetsMetrics);
      assertTrue(CollectionUtils.isEmpty(meedAssetsMetrics.getPools()));
      assertNull(meedAssetsMetrics.getCurrentCity());
      assertNull(meedAssetsMetrics.getTotalAllocationPoints());
      assertNull(meedAssetsMetrics.getTotalFixedPercentage());
    });
  }

}
