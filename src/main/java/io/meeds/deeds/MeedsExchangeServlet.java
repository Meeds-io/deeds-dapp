/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
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
package io.meeds.deeds;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.concurrent.*;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeedsExchangeServlet extends HttpServlet {

  private static final long             serialVersionUID         = -4130223673549168957L;

  private static final Logger           LOG                      = LoggerFactory.getLogger(MeedsExchangeServlet.class);

  private static final String           PAIR_ADDRESS             = "0x960bd61d0b960b107ff5309a2dcced4705567070";

  public static final DateTimeFormatter DATE_FORMATTER           = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                                                                    .withResolverStyle(ResolverStyle.LENIENT);

  private static final LocalDate        MEEDS_TOKEN_FIRST_DATE   = LocalDate.of(2021, 11, 6);

  private ScheduledExecutorService      scheduledExecutorService = Executors.newScheduledThreadPool(1);

  @Override
  public void init() throws ServletException {
    scheduledExecutorService.scheduleWithFixedDelay(() -> computeExchangeRate(), 0, 1, TimeUnit.HOURS);
  }

  @Override
  public void destroy() {
    scheduledExecutorService.shutdownNow();
    super.destroy();
  }

  private void computeExchangeRate() {
    try {
      URL exchangeRate = getServletContext().getResource("/json/exchangeRate-meeds.json");
      if (exchangeRate != null) {
        String exchangeRateObjectString = IOUtils.toString(exchangeRate, StandardCharsets.UTF_8);
        JSONObject exchangeRateObject = new JSONObject(exchangeRateObjectString);
        LocalDate localDate = MEEDS_TOKEN_FIRST_DATE;
        LocalDate today = LocalDate.now().plusDays(1);
        while (localDate.isBefore(today)) {
          ZonedDateTime date = localDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).minusSeconds(1);
          addExchangeRateForDate(exchangeRateObject, date, false);
          localDate = localDate.plusDays(1);
        }
        addExchangeRateForDate(exchangeRateObject, ZonedDateTime.now(ZoneOffset.UTC), true);
        try (FileOutputStream outputStream = new FileOutputStream(exchangeRate.getPath())) {
          IOUtils.write(exchangeRateObject.toString(), outputStream, StandardCharsets.UTF_8);
        }
      }
    } catch (Exception e) {
      LOG.error("An error occurred while computing Meeds exchange rates", e);
    }
  }

  private void addExchangeRateForDate(JSONObject exchangeRateObject, ZonedDateTime date, boolean volatileValue) {
    String dateString = DATE_FORMATTER.format(date);
    if (!exchangeRateObject.has(dateString)
        || exchangeRateObject.getJSONObject(dateString).has("volatile")
        || volatileValue) {
      JSONObject exchangeRateResult = getExchangeRateForDate(date);
      if (exchangeRateResult == null) {
        LOG.warn("ETH and Meeds prices for date {} are empty, stop computing.", dateString);
        return;
      }
      if (volatileValue) {
        // Keep updating the date until
        exchangeRateResult.put("volatile", true);
      }
      exchangeRateObject.put(dateString, exchangeRateResult);
    }
  }

  private JSONObject getExchangeRateForDate(ZonedDateTime date) {
    JSONObject exchangeRateResult = new JSONObject();
    addEthPrice(exchangeRateResult, date);
    addPairData(exchangeRateResult, date);
    return exchangeRateResult;
  }

  private void addEthPrice(JSONObject exchangeRateResult, ZonedDateTime date) {
    String blockNumber = getBlockNumber(date);
    if (StringUtils.isBlank(blockNumber)) {
      exchangeRateResult.put("volatile", true);
    }
    String body = "{\"query\":\"{bundle(id: 1, block:{number:" + blockNumber + "}){ethPrice}}\"}";
    String ethPriceDataJsonString = executeQuery("https://api.thegraph.com/subgraphs/name/sushiswap/exchange", body);
    if (StringUtils.isNotBlank(ethPriceDataJsonString)) {
      JSONObject ethPriceDataJson = new JSONObject(ethPriceDataJsonString);
      if (ethPriceDataJson.has("data") && ethPriceDataJson.getJSONObject("data").has("bundle")
          && !ethPriceDataJson.getJSONObject("data").isNull("bundle")) {
        String ethPrice = ethPriceDataJson.getJSONObject("data").getJSONObject("bundle").getString("ethPrice");
        exchangeRateResult.put("ethPrice", ethPrice);
        return;
      }
    }
    // Force to recompute in case of error
    LOG.warn("Error computing Eth Price for date {}. Retrieve empty result.", DATE_FORMATTER.format(date));
    exchangeRateResult.put("volatile", true);
  }

  private void addPairData(JSONObject exchangeRateResult, ZonedDateTime date) {
    String blockNumber = getBlockNumber(date);
    if (StringUtils.isNotBlank(blockNumber)) {
      String body = "{\"query\":\"{pair(id: \\\""
          + PAIR_ADDRESS + "\\\", block: {number:" + blockNumber + "}) {id,token1Price,reserve0,reserve1}}\"}";
      String pairDataJsonString = executeQuery("https://api.thegraph.com/subgraphs/name/sushiswap/exchange", body);
      if (StringUtils.isNotBlank(pairDataJsonString)) {
        JSONObject pairDataJson = new JSONObject(pairDataJsonString);
        if (pairDataJson.has("data") && pairDataJson.getJSONObject("data").has("pair")
            && !pairDataJson.getJSONObject("data").isNull("pair")) {
          JSONObject pairJsonObject = pairDataJson.getJSONObject("data").getJSONObject("pair");
          String meedsPrice = pairJsonObject.getString("token1Price");
          exchangeRateResult.put("meedsPrice", meedsPrice);
          String meedsReserve = pairJsonObject.getString("reserve1");
          exchangeRateResult.put("meedsReserve", meedsReserve);
          String wethReserve = pairJsonObject.getString("reserve0");
          exchangeRateResult.put("wethReserve", wethReserve);
          return;
        }
      }
    }
    // Force to recompute in case of error
    LOG.warn("Error computing Pair Data for date {}. Retrieve empty result.", DATE_FORMATTER.format(date));
    exchangeRateResult.put("volatile", true);
  }

  private String getBlockNumber(ZonedDateTime date) {
    String body = "{\"query\":\"{blocks(first:1,orderBy:timestamp,orderDirection: desc,where:{timestamp_lte:"
        + date.toEpochSecond() + "}){number}}\"}";
    String blockNumberDataJsonString = executeQuery("https://api.thegraph.com/subgraphs/name/blocklytics/ethereum-blocks", body);
    if (StringUtils.isNotBlank(blockNumberDataJsonString)) {
      JSONObject blockNumberDataJson = new JSONObject(blockNumberDataJsonString);
      if (blockNumberDataJson.has("data") && blockNumberDataJson.getJSONObject("data").has("blocks")
          && !blockNumberDataJson.getJSONObject("data").isNull("blocks")) {
        return blockNumberDataJson.getJSONObject("data").getJSONArray("blocks").getJSONObject(0).getString("number");
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
