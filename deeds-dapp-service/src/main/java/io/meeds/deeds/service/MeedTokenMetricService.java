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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.meeds.deeds.model.MeedTokenMetric;
import io.meeds.deeds.storage.MeedTokenMetricsRepository;

@Component
public class MeedTokenMetricService {

  @Value(
    "#{'${meeds.blockchain.reserveValueEthereumAddresses:0xBa5e4D55CA96bf25c35Fc65D9251355Dcd120655,0x8f4660498E79c771f93316f09da98E1eBF94c576,0x70CAd5d439591Ea7f496B69DcB22521685015853}'.split(',')}"
  )
  private List<String>               reserveEthereumAddresses;

  @Value(
    "#{'${meeds.blockchain.reserveValuePolygonAddresses:}'.split(',')}"
  )
  private List<String>               reservePolygonAddresses;

  @Value(
    "#{'${meeds.blockchain.lockedValueEthereumAddresses:0x44D6d6aB50401Dd846336e9C706A492f06E1Bcd4,0x960Bd61D0b960B107fF5309A2DCceD4705567070}'.split(',')}"
  )
  private List<String>               lockedEthereumAddresses;

  @Value("#{'${meeds.blockchain.lockedValuePolygonAddresses:0x6acA77CF3BaB0C4E8210A09B57B07854a995289a}'.split(',')}")
  private List<String>               lockedPolygonAddresses;

  @Autowired(required = false)
  private BlockchainService          blockchainService;

  @Autowired
  private MeedTokenMetricsRepository meedTokenMetricsRepository;

  private MeedTokenMetric            recentMetric;

  /**
   * Retrieves list of total circulating Meeds supply by using this formula:
   * - Total supply of Meeds - total Meeds reserves - total locked Meeds
   * 
   * @return {@link BigDecimal} for most recent computed circulating supply
   *           value
   */
  public BigDecimal getCirculatingSupply() {
    if (recentMetric == null) {
      recentMetric = getTodayMetric();
      if (recentMetric == null) {
        computeTokenMetrics();
      }
    }
    return recentMetric.getCirculatingSupply();
  }

  public void computeTokenMetrics() {
    MeedTokenMetric metric = getTodayMetric();
    if (metric == null) {
      metric = new MeedTokenMetric(getTodayId());
    }
    BigDecimal totalSupply = blockchainService.totalSupply();
    metric.setTotalSupply(totalSupply);

    Map<String, BigDecimal> reserveBalances = getReserveBalances();
    metric.setReserveBalances(reserveBalances);

    Map<String, BigDecimal> lockedBalances = getLockedBalances();
    metric.setLockedBalances(lockedBalances);

    BigDecimal reserveValue = reserveBalances.values().stream().reduce(BigDecimal::add).orElse(BigDecimal.valueOf(0));
    BigDecimal lockedValue = lockedBalances.values().stream().reduce(BigDecimal::add).orElse(BigDecimal.valueOf(0));
    BigDecimal circulatingSupply = totalSupply.subtract(reserveValue).subtract(lockedValue);
    metric.setCirculatingSupply(circulatingSupply);

    meedTokenMetricsRepository.save(metric);

    // Cache most recent collected Metric
    this.recentMetric = metric;
  }

  private Map<String, BigDecimal> getReserveBalances() {
    Map<String, BigDecimal> reserveBalances = new HashMap<>();
    reserveEthereumAddresses.stream().forEach(address -> {
      BigDecimal balance = blockchainService.balanceOfOnEthereum(address);
      reserveBalances.put(address.toLowerCase(), balance);
    });
    reservePolygonAddresses.stream().forEach(address -> {
      BigDecimal balance = blockchainService.balanceOfOnPolygon(address);
      reserveBalances.put(address.toLowerCase(), balance);
    });
    return reserveBalances;
  }

  private Map<String, BigDecimal> getLockedBalances() {
    Map<String, BigDecimal> lockedBalances = new HashMap<>();
    lockedEthereumAddresses.stream().forEach(address -> {
      BigDecimal balance = blockchainService.balanceOfOnEthereum(address);
      lockedBalances.put(address.toLowerCase(), balance);
    });
    lockedPolygonAddresses.stream().forEach(address -> {
      BigDecimal balance = blockchainService.balanceOfOnPolygon(address);
      lockedBalances.put(address.toLowerCase(), balance);
    });
    return lockedBalances;
  }

  private MeedTokenMetric getTodayMetric() {
    return meedTokenMetricsRepository.findById(getTodayId()).orElse(null);
  }

  private LocalDate getTodayId() {
    return LocalDate.now(ZoneOffset.UTC);
  }

}
