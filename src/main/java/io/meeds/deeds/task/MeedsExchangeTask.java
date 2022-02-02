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
package io.meeds.deeds.task;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

public class MeedsExchangeTask {

  private static final Logger           LOG                               = LoggerFactory.getLogger(MeedsExchangeTask.class);

  private static final String           EXCHANGE_RATE_FILE_PATH           = "/static/json/exchangeRate-meeds.json";

  private static final String           PAIR_ADDRESS                      = "0x960bd61d0b960b107ff5309a2dcced4705567070";

  private static final Pattern          LATEST_BLOCK_NUMBER_ERROR_PATTERN = Pattern.compile(".+ up to block number ([0-9]+) .+");

  public static final DateTimeFormatter DATE_FORMATTER                    = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                                                                             .withResolverStyle(ResolverStyle.LENIENT);

  private static final LocalDate        MEEDS_TOKEN_FIRST_DATE            = LocalDate.of(2021, 11, 6);

  private ServletContext                servletContext;

  public MeedsExchangeTask(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  @Scheduled(fixedDelay = 15, timeUnit = TimeUnit.MINUTES)
  public synchronized void computeExchangeRate() {
    LOG.info("Start Computing MEED exchange rates");
    long start = System.currentTimeMillis();
    try {
      URL exchangeRate = servletContext.getResource(EXCHANGE_RATE_FILE_PATH);
      if (exchangeRate != null) {
        String exchangeRateObjectString = IOUtils.toString(exchangeRate, StandardCharsets.UTF_8);
        JsonObject exchangeRateObject = Json.createReader(new StringReader(exchangeRateObjectString)).readObject();
        JsonObjectBuilder exchangeRateObjectBuilder = Json.createObjectBuilder(exchangeRateObject);
        LocalDate localDate = MEEDS_TOKEN_FIRST_DATE;
        LocalDate today = LocalDate.now();
        while (localDate.isBefore(today)) {
          ZonedDateTime date = localDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).minusSeconds(1);
          addExchangeRateForDate(exchangeRateObjectBuilder, exchangeRateObject, date, false);
          localDate = localDate.plusDays(1);
        }
        addExchangeRateForDate(exchangeRateObjectBuilder, exchangeRateObject, ZonedDateTime.now(ZoneOffset.UTC), true);
        try (FileOutputStream outputStream = new FileOutputStream(exchangeRate.getPath())) {
          JsonWriter writer = Json.createWriter(outputStream);
          writer.writeObject(exchangeRateObjectBuilder.build());
        }
        LOG.info("End Computing MEED exchange rates in {}ms", System.currentTimeMillis() - start);
      } else {
        LOG.warn("Can't compute MEED exchange rates because file '{}' doesn't exists", EXCHANGE_RATE_FILE_PATH);
      }
    } catch (Exception e) {
      LOG.error("An error occurred while computing Meeds exchange rates", e);
    }
  }

  private void addExchangeRateForDate(JsonObjectBuilder exchangeRateObjectBuilder,
                                      JsonObject exchangeRateObject,
                                      ZonedDateTime date,
                                      boolean todayValue) {
    String dateString = DATE_FORMATTER.format(date);
    if (!exchangeRateObject.containsKey(dateString)
        || exchangeRateObject.getJsonObject(dateString).containsKey("volatile")
        || todayValue) {
      JsonObjectBuilder exchangeRateResultBuilder = getExchangeRateForDate(date, todayValue);
      exchangeRateObjectBuilder.add(dateString, exchangeRateResultBuilder);
    }
  }

  private JsonObjectBuilder getExchangeRateForDate(ZonedDateTime date, boolean todayValue) {
    JsonObjectBuilder exchangeRateResultBuilder = Json.createObjectBuilder();
    addEthPrice(exchangeRateResultBuilder, date, todayValue);
    addPairData(exchangeRateResultBuilder, date, todayValue);
    if (todayValue) {
      // Keep updating the date until
      exchangeRateResultBuilder.add("volatile", true);
    }
    return exchangeRateResultBuilder;
  }

  private void addEthPrice(JsonObjectBuilder exchangeRateObjectBuilder, ZonedDateTime date, boolean todayValue) {
    String blockNumber = todayValue ? getLastBlockNumber() : getBlockNumber(date);
    boolean added = addEthPrice(exchangeRateObjectBuilder, blockNumber);
    if (!added) {
      // If the computing is about the same day computing, attempt the previous
      // block
      if (ChronoUnit.DAYS.between(date, ZonedDateTime.now(ZoneOffset.UTC)) == 0) {
        int previousBlock = Integer.parseInt(blockNumber) - 10;
        added = addEthPrice(exchangeRateObjectBuilder, String.valueOf(previousBlock));
      }
    }
    if (!added) {
      // Force to recompute in case of error
      LOG.warn("Error computing Eth Price for date {}. Retrieve empty result.", DATE_FORMATTER.format(date));
      exchangeRateObjectBuilder.add("volatile", true);
    }
  }

  private boolean addEthPrice(JsonObjectBuilder exchangeRateObjectBuilder, String blockNumber) {
    if (StringUtils.isBlank(blockNumber)) {
      exchangeRateObjectBuilder.add("volatile", true);
    }
    String body = "{\"query\":\"{bundle(id: 1, block:{number:" + blockNumber + "}){ethPrice}}\"}";
    String ethPriceDataJsonString = executeQuery("https://api.thegraph.com/subgraphs/name/sushiswap/exchange", body);
    if (StringUtils.isNotBlank(ethPriceDataJsonString)) {
      JsonObject ethPriceDataJson = Json.createReader(new StringReader(ethPriceDataJsonString)).readObject();
      if (ethPriceDataJson.containsKey("data") && ethPriceDataJson.getJsonObject("data").containsKey("bundle")
          && !ethPriceDataJson.getJsonObject("data").isNull("bundle")) {
        String ethPrice = ethPriceDataJson.getJsonObject("data").getJsonObject("bundle").getString("ethPrice");
        exchangeRateObjectBuilder.add("ethPrice", ethPrice);
        return true;
      } else if (ethPriceDataJson.containsKey("errors")
          && !ethPriceDataJson.isNull("errors")
          && ethPriceDataJson.getJsonArray("errors").size() > 0
          && ethPriceDataJson.getJsonArray("errors").getJsonObject(0).containsKey("message")
          && !ethPriceDataJson.getJsonArray("errors").getJsonObject(0).isNull("message")) {
        String message = ethPriceDataJson.getJsonArray("errors").getJsonObject(0).getString("message");
        LOG.warn(message);
        if (message.contains("up to block number")) {
          Matcher matcher = LATEST_BLOCK_NUMBER_ERROR_PATTERN.matcher(message);
          if (matcher.find()) {
            blockNumber = matcher.group(1);
            LOG.info("Attempt to retrieve data with block number {}", blockNumber);
            return addEthPrice(exchangeRateObjectBuilder, blockNumber);
          }
        }
      }
    }
    return false;
  }

  private void addPairData(JsonObjectBuilder exchangeRateResultBuilder, ZonedDateTime date, boolean todayValue) {
    String blockNumber = todayValue ? getLastBlockNumber() : getBlockNumber(date);
    boolean added = addPairData(exchangeRateResultBuilder, blockNumber);
    if (!added) {
      // If the computing is about the same day computing, attempt the previous
      // block
      if (ChronoUnit.DAYS.between(date, ZonedDateTime.now(ZoneOffset.UTC)) == 0) {
        int previousBlock = Integer.parseInt(blockNumber) - 10;
        added = addPairData(exchangeRateResultBuilder, String.valueOf(previousBlock));
      }
    }
    if (!added) {
      // Force to recompute in case of error
      LOG.warn("Error computing Pair Data for date {}. Retrieve empty result.", DATE_FORMATTER.format(date));
      exchangeRateResultBuilder.add("volatile", true);
    }
  }

  private boolean addPairData(JsonObjectBuilder exchangeRateResultBuilder, String blockNumber) {
    if (StringUtils.isNotBlank(blockNumber)) {
      String body = "{\"query\":\"{pair(id: \\\""
          + PAIR_ADDRESS + "\\\", block: {number:" + blockNumber
          + "}) {id,token1Price,reserve0,reserve1}}\"}";
      String pairDataJsonString = executeQuery("https://api.thegraph.com/subgraphs/name/sushiswap/exchange", body);
      if (StringUtils.isNotBlank(pairDataJsonString)) {
        JsonObject pairDataJson = Json.createReader(new StringReader(pairDataJsonString)).readObject();
        if (pairDataJson.containsKey("data") && pairDataJson.getJsonObject("data").containsKey("pair")
            && !pairDataJson.getJsonObject("data").isNull("pair")) {
          JsonObject pairJsonObject = pairDataJson.getJsonObject("data").getJsonObject("pair");
          String meedsPrice = pairJsonObject.getString("token1Price");
          exchangeRateResultBuilder.add("meedsPrice", meedsPrice);
          String meedsReserve = pairJsonObject.getString("reserve1");
          exchangeRateResultBuilder.add("meedsReserve", meedsReserve);
          String wethReserve = pairJsonObject.getString("reserve0");
          exchangeRateResultBuilder.add("wethReserve", wethReserve);
          return true;
        } else if (pairDataJson.containsKey("errors")
            && !pairDataJson.isNull("errors")
            && pairDataJson.getJsonArray("errors").size() > 0
            && pairDataJson.getJsonArray("errors").getJsonObject(0).containsKey("message")
            && !pairDataJson.getJsonArray("errors").getJsonObject(0).isNull("message")) {
          String message = pairDataJson.getJsonArray("errors").getJsonObject(0).getString("message");
          LOG.warn(message);
          if (message.contains("up to block number")) {
            Matcher matcher = LATEST_BLOCK_NUMBER_ERROR_PATTERN.matcher(message);
            if (matcher.find()) {
              blockNumber = matcher.group(1);
              LOG.info("Attempt to retrieve data with block number {}", blockNumber);
              return addPairData(exchangeRateResultBuilder, blockNumber);
            }
          }
        }
      }
    }
    return false;
  }

  private String getBlockNumber(ZonedDateTime date) {
    String body = "{\"query\":\"{blocks(first:1,orderBy:timestamp,orderDirection: desc,where:{timestamp_lte:"
        + date.toEpochSecond() + "}){number}}\"}";
    String blockNumberDataJsonString = executeQuery("https://api.thegraph.com/subgraphs/name/blocklytics/ethereum-blocks", body);
    if (StringUtils.isNotBlank(blockNumberDataJsonString)) {
      JsonObject blockNumberDataJson = Json.createReader(new StringReader(blockNumberDataJsonString)).readObject();
      if (blockNumberDataJson.containsKey("data") && blockNumberDataJson.getJsonObject("data").containsKey("blocks")
          && !blockNumberDataJson.getJsonObject("data").isNull("blocks")) {
        return blockNumberDataJson.getJsonObject("data").getJsonArray("blocks").getJsonObject(0).getString("number");
      }
    }
    return null;
  }

  private String getLastBlockNumber() {
    String body = "{\"query\":\"{blocks(first:1,orderBy:number,orderDirection:desc){number}}\"}";
    String blockNumberDataJsonString = executeQuery("https://api.thegraph.com/subgraphs/name/blocklytics/ethereum-blocks", body);
    if (StringUtils.isNotBlank(blockNumberDataJsonString)) {
      JsonObject blockNumberDataJson = Json.createReader(new StringReader(blockNumberDataJsonString)).readObject();
      if (blockNumberDataJson.containsKey("data") && blockNumberDataJson.getJsonObject("data").containsKey("blocks")
          && !blockNumberDataJson.getJsonObject("data").isNull("blocks")) {
        return blockNumberDataJson.getJsonObject("data").getJsonArray("blocks").getJsonObject(0).getString("number");
      }
    }
    return null;
  }

  private String executeQuery(String url, String body) {
    try {
      URL apiUrl = new URL(url);
      HttpsURLConnection con = (HttpsURLConnection) apiUrl.openConnection();

      // add reuqest header
      con.setRequestMethod("GET");
      con.setRequestProperty("Content-Type", "application/json");

      // Send post request
      con.setDoOutput(true);
      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
      wr.writeBytes(body);
      wr.flush();
      wr.close();

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
}
