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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.meeds.deeds.constant.Currency;
import io.meeds.deeds.elasticsearch.model.MeedTokenMetric;
import io.meeds.deeds.storage.MeedTokenMetricsRepository;
import lombok.*;

@Component
public class MeedTokenMetricService {

  @Value("#{'${meeds.blockchain.reserveValueEthereumAddresses:0xBa5e4D55CA96bf25c35Fc65D9251355Dcd120655,0x8f4660498E79c771f93316f09da98E1eBF94c576,0x70CAd5d439591Ea7f496B69DcB22521685015853}'.split(',')}")
  @Getter
  private List<String>               reserveEthereumAddresses;

  @Value("#{'${meeds.blockchain.reserveValuePolygonAddresses:}'.split(',')}")
  @Getter
  private List<String>               reservePolygonAddresses;

  @Value("#{'${meeds.blockchain.lockedValueEthereumAddresses:0x44D6d6aB50401Dd846336e9C706A492f06E1Bcd4,0x960Bd61D0b960B107fF5309A2DCceD4705567070}'.split(',')}")
  @Getter
  private List<String>               lockedEthereumAddresses;

  @Value("#{'${meeds.blockchain.lockedValuePolygonAddresses:0x6acA77CF3BaB0C4E8210A09B57B07854a995289a}'.split(',')}")
  @Getter
  private List<String>               lockedPolygonAddresses;

  @Autowired
  private BlockchainService          blockchainService;

  @Autowired
  private MeedTokenMetricsRepository meedTokenMetricsRepository;

  @Getter
  @Setter
  private MeedTokenMetric            recentMetric;

  @Autowired
  private ExchangeService            exchangeService;

  /**
   * Retrieves total circulating Meeds supply by using this formula: - Total
   * supply of Meeds - total Meeds reserves - total locked Meeds
   *
   * @return {@link BigDecimal} for most recent computed circulating supply
   *         value
   */
  public BigDecimal getCirculatingSupply() {
    MeedTokenMetric lastMetric = getLastMetric(null);
    return lastMetric.getCirculatingSupply();
  }

  /**
   * Retrieves total Meeds supply, reading the value of the ERC20.totalSupply()
   * Meeds contract
   *
   * @return {@link BigDecimal} for most recent computed total supply value
   */
  public BigDecimal getTotalSupply() {
    MeedTokenMetric lastMetric = getLastMetric(null);
    return lastMetric.getTotalSupply();
  }

  /**
   * Retrieves Market Capitalization by using this formula: - Circulating Supply
   * of Meeds * Meed USD Token price
   * 
   * @param  currency used {@link Currency} for currency prices calculation
   * @return          {@link BigDecimal} for most recent computed market
   *                  capitalization value
   */
  public BigDecimal getMarketCapitalization(Currency currency) {
    MeedTokenMetric lastMetric = getLastMetric(currency);
    return lastMetric.getMarketCapitalization();
  }

  /**
   * Retrieves Total Locked Value by using this formula: - Total Locked Meed
   * Tokens * Meed USD Token price
   * 
   * @param  currency used {@link Currency} for currency prices calculation
   * @return          {@link BigDecimal} for most recent computed Total Locked
   *                  Value value
   */
  public BigDecimal getTotalValueLocked(Currency currency) {
    MeedTokenMetric lastMetric = getLastMetric(currency);
    return lastMetric.getTotalValuelocked();
  }

  /**
   * Retrieves Metrics of Meed Token from Blockchain and other external sources.
   * Once retrieved, it will be saved. The metrics are collected by day, in
   * other terms, there will be only one metric entity collected by a day.
   */
  public void computeTokenMetrics() {
    MeedTokenMetric metric = getTodayMetric();
    if (metric == null) {
      metric = new MeedTokenMetric(getTodayId());
    }
    BigDecimal totalSupply = blockchainService.meedsTotalSupplyNoDecimals();
    metric.setTotalSupply(totalSupply);

    Map<String, BigDecimal> reserveBalances = getReserveBalances();
    metric.setReserveBalances(reserveBalances);

    Map<String, BigDecimal> lockedBalances = getLockedBalances();
    metric.setLockedBalances(lockedBalances);

    BigDecimal meedUsdValue = exchangeService.getMeedUsdPrice();
    metric.setMeedUsdPrice(meedUsdValue);

    BigDecimal totalLockedBalance = getTotalLockedBalance(lockedBalances);
    BigDecimal totalLockedValue = meedUsdValue.multiply(totalLockedBalance);
    metric.setTotalValuelocked(totalLockedValue);

    BigDecimal totalReserveBalance = getTotalReserveBalance(reserveBalances);

    BigDecimal circulatingSupply = totalSupply.subtract(totalReserveBalance).subtract(totalLockedBalance);
    metric.setCirculatingSupply(circulatingSupply);

    BigDecimal marketCapitalization = meedUsdValue.multiply(circulatingSupply);
    metric.setMarketCapitalization(marketCapitalization);

    meedTokenMetricsRepository.save(metric);

    // Cache most recent collected Metric
    this.recentMetric = metric;
  }

  /**
   * @return {@link Map} of {@link String} and {@link BigDecimal}. This will
   *         retrieve from Ethereum and Polygon Blockchains a Map of Token
   *         balances for each configured reserveBalanceAddress.
   */
  public Map<String, BigDecimal> getReserveBalances() {
    Map<String, BigDecimal> reserveBalances = new HashMap<>();
    reserveEthereumAddresses.stream()
                            .filter(StringUtils::isNotBlank)
                            .forEach(address -> {
                              BigDecimal balance = blockchainService.meedBalanceOfNoDecimals(address);
                              reserveBalances.put(address.toLowerCase(), balance);
                            });
    reservePolygonAddresses.stream()
                           .filter(StringUtils::isNotBlank)
                           .forEach(address -> {
                             BigDecimal balance = blockchainService.meedBalanceOfOnPolygon(address);
                             reserveBalances.put(address.toLowerCase(), balance);
                           });
    return reserveBalances;
  }

  /**
   * @return {@link Map} of {@link String} and {@link BigDecimal}. This will
   *         retrieve from Ethereum and Polygon Blockchains a Map of Token
   *         balances for each configured lockedBalanceAddress.
   */
  public Map<String, BigDecimal> getLockedBalances() {
    Map<String, BigDecimal> lockedBalances = new HashMap<>();
    lockedEthereumAddresses.stream()
                           .filter(StringUtils::isNotBlank)
                           .forEach(address -> {
                             BigDecimal balance = blockchainService.meedBalanceOfNoDecimals(address);
                             lockedBalances.put(address.toLowerCase(), balance);
                           });
    lockedPolygonAddresses.stream()
                          .filter(StringUtils::isNotBlank)
                          .forEach(address -> {
                            BigDecimal balance = blockchainService.meedBalanceOfOnPolygon(address);
                            lockedBalances.put(address.toLowerCase(), balance);
                          });
    return lockedBalances;
  }

  private BigDecimal getTotalLockedBalance(Map<String, BigDecimal> lockedBalances) {
    return lockedBalances.values()
                         .stream()
                         .filter(Objects::nonNull)
                         .reduce(BigDecimal::add)
                         .orElse(BigDecimal.valueOf(0));
  }

  private BigDecimal getTotalReserveBalance(Map<String, BigDecimal> reserveBalances) {
    return reserveBalances.values()
                          .stream()
                          .filter(Objects::nonNull)
                          .reduce(BigDecimal::add)
                          .orElse(BigDecimal.valueOf(0));
  }

  /**
   * Retrieves the latest Metrics of Meed Token
   * 
   * @param  currency used {@link Currency} for currency prices calculation
   * @return          {@link MeedTokenMetric} representing the Meed Token
   *                  Metrics
   */
  public MeedTokenMetric getLastMetric(Currency currency) {
    if (recentMetric == null) {
      recentMetric = getTodayMetric();
      if (recentMetric == null) {
        computeTokenMetrics();
      }
    }
    return toCurrency(recentMetric, currency);
  }

  private MeedTokenMetric getTodayMetric() {
    return meedTokenMetricsRepository.findById(getTodayId()).orElse(null);
  }

  private LocalDate getTodayId() {
    return LocalDate.now(ZoneOffset.UTC);
  }

  private MeedTokenMetric toCurrency(MeedTokenMetric metric, Currency currency) {
    BigDecimal usdRate = exchangeService.getExchangeRate(currency);
    if (BigDecimal.ONE.equals(usdRate)) {
      return metric;
    } else {
      return new MeedTokenMetric(metric.getDate(),
                                 metric.getTotalSupply(),
                                 metric.getLockedBalances() == null ? Collections.emptyMap()
                                                                    : new HashMap<>(metric.getLockedBalances()),
                                 metric.getReserveBalances() == null ? Collections.emptyMap()
                                                                     : new HashMap<>(metric.getReserveBalances()),
                                 metric.getCirculatingSupply(),
                                 metric.getMarketCapitalization().multiply(usdRate),
                                 metric.getTotalValuelocked().multiply(usdRate),
                                 metric.getMeedUsdPrice().multiply(usdRate));
    }
  }

}
