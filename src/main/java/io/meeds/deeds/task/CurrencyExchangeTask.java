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
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.concurrent.TimeUnit;

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

public class CurrencyExchangeTask {

  private static final String           EXCHANGE_RATE_FILE_PATH = "/static/json/exchangeRate-eur.json";

  private static final Logger           LOG                     = LoggerFactory.getLogger(CurrencyExchangeTask.class);

  public static final DateTimeFormatter DATE_FORMATTER          = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                                                                   .withResolverStyle(ResolverStyle.LENIENT);

  private static final LocalDate        MEEDS_TOKEN_FIRST_DATE  = LocalDate.of(2021, 11, 6);

  private String                        apiKey                  = System.getProperty("m3o.apiKey");

  private ServletContext                servletContext;

  public CurrencyExchangeTask(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  @Scheduled(fixedDelay = 15, timeUnit = TimeUnit.MINUTES)
  public synchronized void computeExchangeRate() {
    LOG.info("Start Computing EURO exchange rate");
    long start = System.currentTimeMillis();
    try {
      URL exchangeRate = servletContext.getResource(EXCHANGE_RATE_FILE_PATH);
      if (exchangeRate != null) {
        String exchangeRateObjectString = IOUtils.toString(exchangeRate, StandardCharsets.UTF_8);
        JsonObject exchangeRateObject = Json.createReader(new StringReader(exchangeRateObjectString)).readObject();
        JsonObjectBuilder exchangeRateObjectBuilder = Json.createObjectBuilder(exchangeRateObject);
        LocalDate localDate = MEEDS_TOKEN_FIRST_DATE;
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        boolean ratesChanged = false;
        while (localDate.isBefore(tomorrow)) {
          String dateString = DATE_FORMATTER.format(localDate);
          if (!exchangeRateObject.containsKey(dateString)) {
            String exchangeRateResultString = retrieveCurrencyExchangeRate(localDate);
            if (StringUtils.isBlank(exchangeRateResultString)) {
              break;
            }
            JsonObject exchangeRateResult = Json.createReader(new StringReader(exchangeRateResultString)).readObject();
            BigDecimal euroExchangeRate = exchangeRateResult.getJsonObject("rates").getJsonNumber("EUR").bigDecimalValue();
            exchangeRateObjectBuilder.add(dateString, euroExchangeRate);
            ratesChanged = true;
          }
          localDate = localDate.plusDays(1);
        }
        if (ratesChanged) {
          try (FileOutputStream outputStream = new FileOutputStream(exchangeRate.getPath())) {
            JsonWriter writer = Json.createWriter(outputStream);
            writer.writeObject(exchangeRateObjectBuilder.build());
          }
        }
        LOG.info("End Computing EURO exchange rate in {}ms", System.currentTimeMillis() - start);
      } else {
        LOG.warn("Can't compute EURO exchange rate because file '{}' doesn't exists", EXCHANGE_RATE_FILE_PATH);
      }
    } catch (Exception e) {
      LOG.error("An error occurred while computing EURO exchange rates", e);
    }
  }

  private String retrieveCurrencyExchangeRate(LocalDate date) {
    if (apiKey == null) {
      throw new IllegalStateException("API Key is mandatory");
    }
    String dateString = DATE_FORMATTER.format(date);
    try {
      URL apiUrl = new URL("https://api.m3o.com/v1/currency/Rates");
      HttpsURLConnection con = (HttpsURLConnection) apiUrl.openConnection();

      // add reuqest header
      con.setRequestMethod("GET");
      con.setRequestProperty("Content-Type", "application/json");
      con.setRequestProperty("Authorization", "Bearer " + apiKey);
      String parameters = "{\"code\": \"USD\"}";

      // Send post request
      con.setDoOutput(true);
      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
      wr.writeBytes(parameters);
      wr.flush();
      wr.close();

      int responseCode = con.getResponseCode();
      if (responseCode == 200) {
        try (InputStream inputStream = con.getInputStream()) {
          return IOUtils.toString(con.getInputStream(), StandardCharsets.UTF_8);
        }
      }
      return null;
    } catch (Exception e) {
      LOG.warn("An error occurred while retrieving EURO exchange rate of date {}", dateString, e);
      return null;
    }
  }

}
