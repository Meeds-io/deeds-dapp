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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.meeds.deeds.model.MeedTokenMetric;
import io.meeds.deeds.storage.MeedTokenMetricsRepository;
import lombok.Getter;

import javax.net.ssl.HttpsURLConnection;

@Component
public class MeedTokenMetricService {

  @Value(
    "#{'${meeds.blockchain.reserveValueEthereumAddresses:0xBa5e4D55CA96bf25c35Fc65D9251355Dcd120655,0x8f4660498E79c771f93316f09da98E1eBF94c576,0x70CAd5d439591Ea7f496B69DcB22521685015853}'.split(',')}"
  )
  @Getter
  private List<String>               reserveEthereumAddresses;

  @Value(
    "#{'${meeds.blockchain.reserveValuePolygonAddresses:}'.split(',')}"
  )
  @Getter
  private List<String>               reservePolygonAddresses;

  @Value(
    "#{'${meeds.blockchain.lockedValueEthereumAddresses:0x44D6d6aB50401Dd846336e9C706A492f06E1Bcd4,0x960Bd61D0b960B107fF5309A2DCceD4705567070}'.split(',')}"
  )
  @Getter
  private List<String>               lockedEthereumAddresses;

  @Value("#{'${meeds.blockchain.lockedValuePolygonAddresses:0x6acA77CF3BaB0C4E8210A09B57B07854a995289a}'.split(',')}")
  @Getter
  private List<String>               lockedPolygonAddresses;

  @Value("${meeds.exchange.Coingecko.UsdPrice:https://api.coingecko.com/api/v3/simple/price?ids=meeds-dao&vs_currencies=USD}")
  private String InstantMeedUsdPrice;

  @Autowired(required = false)
  private BlockchainService          blockchainService;

  @Autowired
  private MeedTokenMetricsRepository meedTokenMetricsRepository;

  @Getter
  private MeedTokenMetric            recentMetric;

  @Autowired(required = false)
  private ExchangeService exchangeService;

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
    BigDecimal totalSupply = blockchainService.totalSupply();
    metric.setTotalSupply(totalSupply);

    Map<String, BigDecimal> reserveBalances = getReserveBalances();
    metric.setReserveBalances(reserveBalances);

    Map<String, BigDecimal> lockedBalances = getLockedBalances();
    metric.setLockedBalances(lockedBalances);

    BigDecimal marketCap = getMarketCapitalization();
    metric.setMarketCapitalization(marketCap);

    BigDecimal reserveValue = reserveBalances.values().stream().reduce(BigDecimal::add).orElse(BigDecimal.valueOf(0));
    BigDecimal lockedValue = lockedBalances.values().stream().reduce(BigDecimal::add).orElse(BigDecimal.valueOf(0));
    BigDecimal circulatingSupply = totalSupply.subtract(reserveValue).subtract(lockedValue);
    metric.setCirculatingSupply(circulatingSupply);

    meedTokenMetricsRepository.save(metric);

    // Cache most recent collected Metric
    this.recentMetric = metric;
  }

  /**
   * @return {@link Map} of {@link String} and {@link BigDecimal}. This will
   *           retrieve from Ethereum and Polygon Blockchains a Map of Token
   *           balances for each configured reserveBalanceAddress.
   */
  public Map<String, BigDecimal> getReserveBalances() {
    Map<String, BigDecimal> reserveBalances = new HashMap<>();
    reserveEthereumAddresses.stream()
                            .filter(StringUtils::isNotBlank)
                            .forEach(address -> {
                              BigDecimal balance = blockchainService.balanceOfOnEthereum(address);
                              reserveBalances.put(address.toLowerCase(), balance);
                            });
    reservePolygonAddresses.stream()
                           .filter(StringUtils::isNotBlank)
                           .forEach(address -> {
                             BigDecimal balance = blockchainService.balanceOfOnPolygon(address);
                             reserveBalances.put(address.toLowerCase(), balance);
                           });
    return reserveBalances;
  }

  /**
   * @return {@link Map} of {@link String} and {@link BigDecimal}. This will
   *           retrieve from Ethereum and Polygon Blockchains a Map of Token
   *           balances for each configured lockedBalanceAddress.
   */
  public Map<String, BigDecimal> getLockedBalances() {
    Map<String, BigDecimal> lockedBalances = new HashMap<>();
    lockedEthereumAddresses.stream()
                           .filter(StringUtils::isNotBlank)
                           .forEach(address -> {
                             BigDecimal balance = blockchainService.balanceOfOnEthereum(address);
                             lockedBalances.put(address.toLowerCase(), balance);
                           });
    lockedPolygonAddresses.stream()
                          .filter(StringUtils::isNotBlank)
                          .forEach(address -> {
                            BigDecimal balance = blockchainService.balanceOfOnPolygon(address);
                            lockedBalances.put(address.toLowerCase(), balance);
                          });
    return lockedBalances;
  }

  /**
   * @return {@link BigDecimal} . This will return the Makert Capitalization value of the Meeds Token
   * throughout the formula of MarketCap = TotalSupply * Meeds price
   */
  public BigDecimal getMarketCapitalization() {
    BigDecimal MarketCap = null;
    try {
      HttpsURLConnection con = (HttpsURLConnection) new URL(InstantMeedUsdPrice).openConnection();
      int responseCode = con.getResponseCode();
      if (responseCode == 200) {
        try (InputStream inputStream = con.getInputStream()) {
          String meedUsdValue = findDecimalNums(IOUtils.toString(inputStream, StandardCharsets.UTF_8)).get(0);
          return new BigDecimal(meedUsdValue).multiply(getCirculatingSupply());
        }
      }

    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    return MarketCap;
  }

  List<String> findDecimalNums(String stringToSearch) {
    Pattern decimalNumPattern = Pattern.compile("-?\\d+(\\.\\d+)?");
    Matcher matcher = decimalNumPattern.matcher(stringToSearch);

    List<String> decimalNumList = new ArrayList<>();
    while (matcher.find()) {
      decimalNumList.add(matcher.group());
    }

    return decimalNumList;
  }



  private MeedTokenMetric getTodayMetric() {
    return meedTokenMetricsRepository.findById(getTodayId()).orElse(null);
  }

  private LocalDate getTodayId() {
    return LocalDate.now(ZoneOffset.UTC);
  }

}
