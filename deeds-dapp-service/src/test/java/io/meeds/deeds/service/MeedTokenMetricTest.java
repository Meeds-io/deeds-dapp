
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import io.meeds.deeds.contract.MeedsToken;
import io.meeds.deeds.model.MeedTokenMetric;
import io.meeds.deeds.storage.MeedTokenMetricsRepository;

@SpringBootTest(
    classes = {
        MeedTokenMetricService.class,
    }
)
@TestPropertySource(
    properties = {
        "meeds.blockchain.reserveValueEthereumAddresses=",
        "meeds.blockchain.reserveValuePolygonAddresses=",
        "meeds.blockchain.lockedValueEthereumAddresses=0x44D6d6aB50401Dd846336e9C706A492f06E1Bcd4,0x960Bd61D0b960B107fF5309A2DCceD4705567070",
        "meeds.blockchain.lockedValuePolygonAddresses=0x6acA77CF3BaB0C4E8210A09B57B07854a995289a"
    }
)
//0x44D6d6aB50401Dd846336e9C706A492f06E1Bee4
//0xBa5e4D55CA96bf25c35Fc65D9251355Dcd120655,0x8f4660498E79c771f93316f09da98E1eBF94c576,0x70CAd5d439591Ea7f496B69DcB22521685015853
class MeedTokenMetricTest {

  @MockBean
  private BlockchainService          blockchainService;

  @MockBean
  private MeedTokenMetric            metric;

  @MockBean
  private MeedTokenMetricsRepository meedTokenMetricsRepository;

  @MockBean(name = "ethereumMeedToken")
  private MeedsToken                 ethereumToken;

  @MockBean(name = "polygonMeedToken")
  private MeedsToken                 polygonToken;

  @Autowired
  private MeedTokenMetricService     meedTokenMetricService;

  @BeforeEach
  void init() {
    reset(meedTokenMetricsRepository);
  }

  @Test
  void testGetReserveBalances() {
    Map<String, BigDecimal> expectedReserveBalances = new HashMap<>();
    BigDecimal totalReserves = mockReserveBalances(expectedReserveBalances);
    Map<String, BigDecimal> reserveBalances = meedTokenMetricService.getReserveBalances();
    assertEquals(expectedReserveBalances, reserveBalances);

    BigDecimal result = reserveBalances.values().stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    assertEquals(totalReserves, result);
  }

  @Test
  void testGetLockedBalances() {
    Map<String, BigDecimal> expectedLockedBalances = new HashMap<>();
    BigDecimal totallocked = mockLockedBalances(expectedLockedBalances);
    Map<String, BigDecimal> lockedBalances = meedTokenMetricService.getLockedBalances();
    assertEquals(expectedLockedBalances, lockedBalances);

    BigDecimal result = lockedBalances.values().stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    assertEquals(totallocked, result);
  }

  @Test
  void testComputeTokenMetrics() {
    // Given
    BigDecimal expectedTotalSupply = BigDecimal.valueOf(100);
    when(blockchainService.totalSupply()).thenReturn(expectedTotalSupply);

    HashMap<String, BigDecimal> expectedReserveBalances = new HashMap<>();
    BigDecimal expectedTotalReserves = mockReserveBalances(expectedReserveBalances);
    HashMap<String, BigDecimal> expectedLockedBalances = new HashMap<>();
    BigDecimal expectedTotalLocked = mockLockedBalances(expectedLockedBalances);

    BigDecimal expectedCirculatingSupply = expectedTotalSupply.subtract(expectedTotalReserves).subtract(expectedTotalLocked);

    // When
    meedTokenMetricService.computeTokenMetrics();

    // Then
    MeedTokenMetric recentMetric = meedTokenMetricService.getRecentMetric();
    assertNotNull(recentMetric);
    assertEquals(expectedTotalSupply, recentMetric.getTotalSupply());
    assertEquals(expectedReserveBalances, recentMetric.getReserveBalances());
    assertEquals(expectedLockedBalances, recentMetric.getLockedBalances());
    assertEquals(expectedCirculatingSupply, recentMetric.getCirculatingSupply());

    verify(meedTokenMetricsRepository, times(1)).save(recentMetric);
  }

  @Test
  void testGetCirculatingSupply() {
    // Given
    BigDecimal expectedTotalSupply = BigDecimal.valueOf(100);
    when(blockchainService.totalSupply()).thenReturn(expectedTotalSupply);

    BigDecimal expectedTotalReserves = mockReserveBalances(new HashMap<>());
    BigDecimal expectedTotalLocked = mockLockedBalances(new HashMap<>());
    BigDecimal expectedCirculatingSupply = expectedTotalSupply.subtract(expectedTotalReserves).subtract(expectedTotalLocked);

    // When
    meedTokenMetricService.computeTokenMetrics();

    // Then
    BigDecimal circulatingSupply = meedTokenMetricService.getCirculatingSupply();
    assertEquals(expectedCirculatingSupply, circulatingSupply);
  }

  @Test
  void testGetMarketCapitalization(){
    when(meedTokenMetricService.getCirculatingSupply()).thenReturn(BigDecimal.valueOf(100));
    BigDecimal marketCap = meedTokenMetricService.getMarketCapitalization();
    assertEquals(100,marketCap);
  }

  private BigDecimal mockReserveBalances(Map<String, BigDecimal> expectedReserveBalances) {
    BigDecimal totalReserves = new BigDecimal(0);
    for (String address : meedTokenMetricService.getReserveEthereumAddresses()) {
      double value = Math.random() * 1000;
      BigDecimal valueBigDecimal = BigDecimal.valueOf(value);
      when(blockchainService.balanceOfOnEthereum(address)).thenReturn(valueBigDecimal);
      expectedReserveBalances.put(address.toLowerCase(), valueBigDecimal);
      totalReserves = totalReserves.add(valueBigDecimal);
    }
    for (String address : meedTokenMetricService.getReservePolygonAddresses()) {
      double value = Math.random() * 1000;
      BigDecimal valueBigDecimal = BigDecimal.valueOf(value);
      when(blockchainService.balanceOfOnPolygon(address)).thenReturn(valueBigDecimal);
      expectedReserveBalances.put(address.toLowerCase(), valueBigDecimal);
      totalReserves = totalReserves.add(valueBigDecimal);
    }
    return totalReserves;
  }

  private BigDecimal mockLockedBalances(Map<String, BigDecimal> expectedLockedBalances) {
    BigDecimal totallocked = new BigDecimal(0);
    for (String address : meedTokenMetricService.getLockedEthereumAddresses()) {
      double value = Math.random() * 1000;
      BigDecimal valueBigDecimal = BigDecimal.valueOf(value);
      when(blockchainService.balanceOfOnEthereum(address)).thenReturn(valueBigDecimal);
      expectedLockedBalances.put(address.toLowerCase(), valueBigDecimal);
      totallocked = totallocked.add(valueBigDecimal);
    }
    for (String address : meedTokenMetricService.getLockedPolygonAddresses()) {
      double value = Math.random() * 1000;
      BigDecimal valueBigDecimal = BigDecimal.valueOf(value);
      when(blockchainService.balanceOfOnPolygon(address)).thenReturn(valueBigDecimal);
      expectedLockedBalances.put(address.toLowerCase(), valueBigDecimal);
      totallocked = totallocked.add(valueBigDecimal);
    }
    return totallocked;
  }

}
