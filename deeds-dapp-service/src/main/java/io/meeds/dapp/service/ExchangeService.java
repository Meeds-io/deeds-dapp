/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.dapp.service;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.meeds.dapp.constant.Currency;
import io.meeds.dapp.elasticsearch.model.CurrencyExchangeRate;
import io.meeds.dapp.elasticsearch.model.MeedExchangeRate;
import io.meeds.dapp.model.MeedPrice;
import io.meeds.dapp.storage.CurrencyExchangeRateRepository;
import io.meeds.dapp.storage.MeedExchangeRateRepository;

import jakarta.annotation.PostConstruct;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

@Component
public class ExchangeService {

  private static final Logger            LOG                               = LoggerFactory.getLogger(ExchangeService.class);

  private static final LocalDate         MEEDS_TOKEN_FIRST_DATE            = LocalDate.of(2021, 11, 6);

  private static final Pattern           LATEST_BLOCK_NUMBER_ERROR_PATTERN = Pattern.compile(".+ up to block number (\\d+) .+");

  private static final String            PAIR_PARAM_NAME                   = "pair";

  private static final String            MESSAGE_PARAM_NAME                = "message";

  private static final String            BUNDLE_PARAM_NAME                 = "bundle";

  private static final String            DATA_PARAM_NAME                   = "data";

  private static final String            BLOCKS_PARAM_NAME                 = "blocks";

  private static final String            ERRORS_PARAM_NAME                 = "errors";

  @Value("${meeds.exchange.currencyApiKey:}")
  private String                         currencyApiKey;

  @Value("${meeds.exchange.currencyApiUrl:https://api.currencyapi.com/v3/latest}")
  private String                         currencyApiUrl;

  @Value("${meeds.exchange.blockchainApiUrl:https://api.thegraph.com/subgraphs/name/blocklytics/ethereum-blocks}")
  private String                         blockchainApiUrl;

  @Value("${meeds.exchange.lpTokenApiUrl:https://api.thegraph.com/subgraphs/name/sushiswap/exchange}")
  private String                         lpTokenApiUrl;

  @Value("${meeds.exchange.lpTokenAddress:0x960bd61d0b960b107ff5309a2dcced4705567070}")
  private String                         lpTokenAddress;

  @Autowired
  private CurrencyExchangeRateRepository currencyExchangeRateRepository;

  @Autowired
  private MeedExchangeRateRepository     meedExchangeRateRepository;

  /**
   * Retrieves list of {@link MeedPrice} from selected date until a dedicated
   * date
   * 
   * @param  currency {@link Currency}
   * @param  fromDate {@link LocalDate}
   * @param  toDate   {@link LocalDate}
   * @return          {@link List} of {@link MeedPrice} for selected dates
   */
  public List<MeedPrice> getExchangeRates(Currency currency, LocalDate fromDate, LocalDate toDate) {
    if (fromDate == null) {
      throw new IllegalArgumentException("fromDate mandatory");
    }
    if (toDate == null) {
      throw new IllegalArgumentException("toDate mandatory");
    }
    if (toDate.isBefore(fromDate)) {
      throw new IllegalArgumentException("toDate must be after mandatory");
    }
    List<MeedExchangeRate> exchangeRates = meedExchangeRateRepository.findByDateBetween(fromDate, toDate);
    if (exchangeRates == null || exchangeRates.isEmpty()) {
      return Collections.emptyList();
    }
    List<CurrencyExchangeRate> currencyExchangeRates;
    if (currency != null && currency != Currency.USD && currency != Currency.ETH) {
      currencyExchangeRates = currencyExchangeRateRepository.findByCurrencyAndDateBetween(currency, fromDate, toDate);
    } else {
      currencyExchangeRates = Collections.emptyList();
    }
    return exchangeRates.stream()
                        .map(exchangeRate -> toMeedPrice(exchangeRate, currencyExchangeRates, currency))
                        .toList();
  }

  public BigDecimal getExchangeRate(Currency currency) {
    if (currency == null || currency == Currency.USD) {
      return BigDecimal.ONE;
    }
    if (currency == Currency.ETH) {
      List<MeedExchangeRate> meedExchangeRates = meedExchangeRateRepository.findByDateBetween(LocalDate.now().minusDays(2),
                                                                                              LocalDate.now());
      if (CollectionUtils.isEmpty(meedExchangeRates)) {
        return BigDecimal.ZERO;
      } else {
        return meedExchangeRates.stream()
                                .max(Comparator.comparing(MeedExchangeRate::getDate))
                                .map(rate -> BigDecimal.ONE.divide(rate.getEthUsdPrice(), 18, RoundingMode.HALF_UP))
                                .orElse(BigDecimal.ZERO);
      }
    } else {
      List<CurrencyExchangeRate> currencyExchangeRates =
                                                       currencyExchangeRateRepository.findByCurrencyAndDateBetween(currency,
                                                                                                                   LocalDate.now()
                                                                                                                            .minusDays(2),
                                                                                                                   LocalDate.now());
      if (CollectionUtils.isEmpty(currencyExchangeRates)) {
        return BigDecimal.ZERO;
      } else {
        return currencyExchangeRates.stream()
                                    .max(Comparator.comparing(CurrencyExchangeRate::getDate))
                                    .map(CurrencyExchangeRate::getRate)
                                    .orElse(BigDecimal.ZERO);
      }
    }
  }

  public void computeTodayCurrencyExchangeRate() {
    computeCurrencyExchangeRateOfDay(LocalDate.now(ZoneOffset.UTC));
  }

  /**
   * Compute and store EURO Currency Exchange rate from first date when MEED
   * Contract has been created until today
   */
  @PostConstruct
  public void computeRates() {
    new Thread(this::computeCurrencyExchangeRate).start();
    new Thread(this::computeMeedExchangeRate).start();
  }

  /**
   * Compute and store MEED Token Exchange rates from first date when MEED
   * Contract has been created until today
   */
  public void computeMeedExchangeRate() {
    LocalDate indexDate = firstMeedTokenDate();
    LocalDate today = LocalDate.now(ZoneOffset.UTC);
    LocalDate untilDate = today.plusDays(1);
    while (indexDate.isBefore(untilDate)) {
      MeedExchangeRate rate = meedExchangeRateRepository.findById(indexDate)
                                                        .orElse(new MeedExchangeRate(indexDate));
      if (!rate.isFinalRate()) {
        boolean todayValue = indexDate.isEqual(today);
        rate.setFinalRate(!todayValue);
        ZonedDateTime time = todayValue ? ZonedDateTime.now(ZoneOffset.UTC)
                                        : indexDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).minusSeconds(1);
        computePrice(rate, time, todayValue, this::addEthPrice);
        computePrice(rate, time, todayValue, this::addPairData);
        meedExchangeRateRepository.save(rate);
      }
      indexDate = indexDate.plusDays(1);
    }
  }

  public BigDecimal getMeedUsdPrice() {
    List<MeedPrice> meedExchangeRates = getExchangeRates(Currency.USD, LocalDate.now().minusDays(8), LocalDate.now());
    return meedExchangeRates.stream()
                            .filter(object -> object.getDate() != null)
                            .max(Comparator.comparing(MeedPrice::getDate))
                            .map(MeedPrice::getCurrencyPrice)
                            .orElse(BigDecimal.ZERO);
  }

  protected void computeCurrencyExchangeRate() {
    LocalDate today = LocalDate.now(ZoneOffset.UTC);
    CurrencyExchangeRate todayExchangeRate = currencyExchangeRateRepository.findById(today).orElse(null);
    if (todayExchangeRate == null) {
      todayExchangeRate = computeCurrencyExchangeRateOfDay(today);
      if (todayExchangeRate == null) {
        LOG.warn("Can't find Currency Exchange rate for today, give up update rates");
        return;
      }
    }

    LocalDate indexDate = firstMeedTokenDate();
    LocalDate untilDate = LocalDate.now(ZoneOffset.UTC).plusDays(1);
    while (indexDate.isBefore(untilDate)) {
      CurrencyExchangeRate rate = currencyExchangeRateRepository.findById(indexDate).orElse(null);
      if (rate == null) {
        rate = new CurrencyExchangeRate(indexDate,
                                        todayExchangeRate.getCurrency(),
                                        todayExchangeRate.getRate());
        currencyExchangeRateRepository.save(rate);
      }
      indexDate = indexDate.plusDays(1);
    }
  }

  private CurrencyExchangeRate computeCurrencyExchangeRateOfDay(LocalDate date) {
    CurrencyExchangeRate rate = null;
    String exchangeRateResultString = retrieveCurrencyExchangeRate();
    if (StringUtils.isNotBlank(exchangeRateResultString)) {
      try (JsonReader reader = Json.createReader(new StringReader(exchangeRateResultString))) {
        JsonObject exchangeRateResult = reader.readObject();
        BigDecimal euroExchangeRate = exchangeRateResult.getJsonObject("data")
                                                        .getJsonObject("EUR")
                                                        .getJsonNumber("value")
                                                        .bigDecimalValue();
        rate = new CurrencyExchangeRate(date, Currency.EUR, euroExchangeRate);
        currencyExchangeRateRepository.save(rate);
      }
    }
    return rate;
  }

  private void computePrice(MeedExchangeRate rate,
                            ZonedDateTime date,
                            boolean todayValue,
                            BiPredicate<MeedExchangeRate, String> computeFunction) {
    String blockNumber = getBlockNumber(date, todayValue);
    boolean added = computeFunction.test(rate, blockNumber);
    // If the computing is about the same day computing, attempt the previous
    // block
    if (!added && todayValue) {
      int previousBlock = Integer.parseInt(blockNumber) - 10;
      added = computeFunction.test(rate, String.valueOf(previousBlock));
    }
    if (!added) {
      // Force to recompute in case of error
      if (LOG.isWarnEnabled()) {
        LOG.warn("Error computing Eth Price for date {}. Retrieve empty result.", date.toLocalDate());
      }
      rate.setFinalRate(false);
    }
  }

  private boolean addEthPrice(MeedExchangeRate rate, String blockNumber) {
    String body = "{\"query\":\"{bundle(id: 1, block:{number:" + blockNumber + "}){ethPrice}}\"}";
    String ethPriceDataJsonString = executeQuery(lpTokenApiUrl, body);
    if (StringUtils.isNotBlank(ethPriceDataJsonString)) {
      try (JsonReader reader = Json.createReader(new StringReader(ethPriceDataJsonString))) {
        JsonObject ethPriceDataJson = reader.readObject();
        if (ethPriceDataJson.containsKey(DATA_PARAM_NAME)
            && ethPriceDataJson.getJsonObject(DATA_PARAM_NAME).containsKey(BUNDLE_PARAM_NAME)
            && !ethPriceDataJson.getJsonObject(DATA_PARAM_NAME).isNull(BUNDLE_PARAM_NAME)) {
          JsonObject bundleObject = ethPriceDataJson.getJsonObject(DATA_PARAM_NAME).getJsonObject(BUNDLE_PARAM_NAME);
          BigDecimal ethPrice = new BigDecimal(bundleObject.getJsonString("ethPrice").getString());
          rate.setEthUsdPrice(ethPrice);
          return true;
        } else {
          String lastKnownBlock = getLastBlockFromErrorMessage(ethPriceDataJson);
          if (StringUtils.isNotBlank(lastKnownBlock)) {
            return addEthPrice(rate, lastKnownBlock);
          }
        }
      }
    }
    return false;
  }

  private boolean addPairData(MeedExchangeRate rate, String blockNumber) {
    if (StringUtils.isNotBlank(blockNumber)) {
      String body = "{\"query\":\"{pair(id: \\\""
          + lpTokenAddress + "\\\", block: {number:" + blockNumber
          + "}) {id,token1Price,reserve0,reserve1}}\"}";
      String pairDataJsonString = executeQuery(lpTokenApiUrl, body);
      if (StringUtils.isNotBlank(pairDataJsonString)) {
        try (JsonReader reader = Json.createReader(new StringReader(pairDataJsonString))) {
          JsonObject pairDataJson = reader.readObject();
          if (hasPairParam(pairDataJson)) {
            JsonObject pairJsonObject = pairDataJson.getJsonObject(DATA_PARAM_NAME).getJsonObject(PAIR_PARAM_NAME);

            BigDecimal meedEthPrice = new BigDecimal(pairJsonObject.getJsonString("token1Price").getString());
            rate.setMeedEthPrice(meedEthPrice);
            BigDecimal meedsReserve = new BigDecimal(pairJsonObject.getJsonString("reserve0").getString());
            rate.setMeedReserve(meedsReserve);
            BigDecimal ethReserve = new BigDecimal(pairJsonObject.getJsonString("reserve1").getString());
            rate.setEthReserve(ethReserve);
            return true;
          } else {
            String lastKnownBlock = getLastBlockFromErrorMessage(pairDataJson);
            if (StringUtils.isNotBlank(lastKnownBlock)) {
              return addPairData(rate, lastKnownBlock);
            }
          }
        }
      }
    }
    return false;
  }

  private String getLastBlockFromErrorMessage(JsonObject pairDataJson) {
    if (pairDataJson.containsKey(ERRORS_PARAM_NAME)
        && !pairDataJson.isNull(ERRORS_PARAM_NAME)
        && !pairDataJson.getJsonArray(ERRORS_PARAM_NAME).isEmpty()
        && pairDataJson.getJsonArray(ERRORS_PARAM_NAME).getJsonObject(0).containsKey(MESSAGE_PARAM_NAME)
        && !pairDataJson.getJsonArray(ERRORS_PARAM_NAME).getJsonObject(0).isNull(MESSAGE_PARAM_NAME)) {
      String message = pairDataJson.getJsonArray(ERRORS_PARAM_NAME).getJsonObject(0).getString(MESSAGE_PARAM_NAME);
      if (message.contains("up to block number")) {
        Matcher matcher = LATEST_BLOCK_NUMBER_ERROR_PATTERN.matcher(message);
        if (matcher.find()) {
          String blockNumber = matcher.group(1);
          LOG.debug("{}. Attempt to retrieve data with block number {}", message, blockNumber);
          return blockNumber;
        }
      }
    }
    return null;
  }

  private String getBlockNumber(ZonedDateTime date, boolean lastBlock) {
    String body;
    if (lastBlock) {
      body = "{\"query\":\"{blocks(first:1,orderBy:number,orderDirection:desc){number}}\"}";
    } else {
      body = "{\"query\":\"{blocks(first:1,orderBy:timestamp,orderDirection: desc,where:{timestamp_lte:"
          + date.toEpochSecond() + "}){number}}\"}";
    }
    String blockNumberDataJsonString = executeQuery(blockchainApiUrl, body);
    if (StringUtils.isNotBlank(blockNumberDataJsonString)) {
      try (JsonReader reader = Json.createReader(new StringReader(blockNumberDataJsonString))) {
        JsonObject blockNumberDataJson = reader.readObject();
        if (blockNumberDataJson.containsKey(DATA_PARAM_NAME)
            && blockNumberDataJson.getJsonObject(DATA_PARAM_NAME).containsKey(BLOCKS_PARAM_NAME)
            && !blockNumberDataJson.getJsonObject(DATA_PARAM_NAME).isNull(BLOCKS_PARAM_NAME)) {
          return blockNumberDataJson.getJsonObject(DATA_PARAM_NAME)
                                    .getJsonArray(BLOCKS_PARAM_NAME)
                                    .getJsonObject(0)
                                    .getString("number");
        }
      }
    }
    return null;
  }

  protected String executeQuery(String url, String body) {
    try {
      HttpsURLConnection con = newURLConnection(url);

      // add reuqest header
      con.setRequestMethod("GET");
      con.setRequestProperty("Content-Type", "application/json");

      if (StringUtils.isNotBlank(body)) {
        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(body);
        wr.flush();
        wr.close();
      }

      int responseCode = con.getResponseCode();
      if (responseCode == 200) {
        try (InputStream inputStream = con.getInputStream()) {
          return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        }
      }
      return null;
    } catch (Exception e) {
      LOG.warn("An error occurred while retrieving data from URL '{}' with data '{}'",
               url,
               body,
               e);
      return null;
    }
  }

  private String retrieveCurrencyExchangeRate() {
    if (StringUtils.isBlank(currencyApiKey)) {
      throw new IllegalStateException("API Key is mandatory");
    }
    try {
      HttpsURLConnection con = newURLConnection(getCurrencyApiUrl());
      int responseCode = con.getResponseCode();
      if (responseCode == 200) {
        try (InputStream inputStream = con.getInputStream()) {
          return IOUtils.toString(con.getInputStream(), StandardCharsets.UTF_8);
        }
      }
      return null;
    } catch (Exception e) {
      LOG.warn("An error occurred while retrieving EURO exchange rate", e);
      return null;
    }
  }

  private String getCurrencyApiUrl() {
    return currencyApiUrl + "?apikey=" + currencyApiKey + "&base_currency=USD&currencies=EUR";
  }

  private MeedPrice toMeedPrice(MeedExchangeRate exchangeRate,
                                List<CurrencyExchangeRate> currencyExchangeRates,
                                Currency currency) {
    LocalDate date = exchangeRate.getDate();
    BigDecimal ethPrice = exchangeRate.getMeedEthPrice();
    BigDecimal ethUsdPrice = exchangeRate.getEthUsdPrice();
    BigDecimal currencyPrice = getMeedPriceInCurrency(ethPrice, ethUsdPrice, currency, currencyExchangeRates, date);
    return new MeedPrice(date, ethPrice, currencyPrice);
  }

  private BigDecimal getMeedPriceInCurrency(BigDecimal meedEthPrice,
                                            BigDecimal ethUsdPrice,
                                            Currency currency,
                                            List<CurrencyExchangeRate> currencyExchangeRates,
                                            LocalDate date) {
    BigDecimal currencyPrice = meedEthPrice;
    if (currency == Currency.USD) {
      currencyPrice = currencyPrice.multiply(ethUsdPrice);
    } else if (currency != Currency.ETH) {
      BigDecimal currencyRate = currencyExchangeRates.stream()
                                                     .filter(rate -> rate.getDate().isEqual(date))
                                                     .findAny()
                                                     .map(CurrencyExchangeRate::getRate)
                                                     .orElse(BigDecimal.valueOf(1));

      currencyPrice = currencyPrice.multiply(ethUsdPrice)
                                   .multiply(currencyRate);
    }
    return currencyPrice;
  }

  private boolean hasPairParam(JsonObject pairDataJson) {
    return pairDataJson.containsKey(DATA_PARAM_NAME)
        && pairDataJson.getJsonObject(DATA_PARAM_NAME).containsKey(PAIR_PARAM_NAME)
        && !pairDataJson.getJsonObject(DATA_PARAM_NAME).isNull(PAIR_PARAM_NAME);
  }

  protected HttpsURLConnection newURLConnection(String url) throws IOException {
    return (HttpsURLConnection) new URL(url).openConnection();
  }

  protected LocalDate firstMeedTokenDate() {
    return MEEDS_TOKEN_FIRST_DATE;
  }

}
