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
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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

public class CurrencyExchangeServlet extends HttpServlet {

  private static final long             serialVersionUID         = 3406095035407422332L;

  private static final Logger           LOG                      = LoggerFactory.getLogger(CurrencyExchangeServlet.class);

  public static final DateTimeFormatter DATE_FORMATTER           = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                                                                    .withResolverStyle(ResolverStyle.LENIENT);

  private static final LocalDate        MEEDS_TOKEN_FIRST_DATE   = LocalDate.of(2021, 11, 6);

  private String                        apiKey                   = System.getProperty("m3o.apiKey");

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
      URL exchangeRate = getServletContext().getResource("/json/exchangeRate-eur.json");
      if (exchangeRate != null) {
        String exchangeRateObjectString = IOUtils.toString(exchangeRate, StandardCharsets.UTF_8);
        JSONObject exchangeRateObject = new JSONObject(exchangeRateObjectString);
        LocalDate localDate = MEEDS_TOKEN_FIRST_DATE;
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        boolean ratesChanged = false;
        while (localDate.isBefore(tomorrow)) {
          String dateString = DATE_FORMATTER.format(localDate);
          if (!exchangeRateObject.has(dateString)) {
            String exchangeRateResultString = retrieveCurrencyExchangeRate(localDate);
            if (StringUtils.isBlank(exchangeRateResultString)) {
              break;
            }
            JSONObject exchangeRateResult = new JSONObject(exchangeRateResultString);
            BigDecimal euroExchangeRate = exchangeRateResult.getJSONObject("rates").getBigDecimal("EUR");
            exchangeRateObject.put(dateString, euroExchangeRate);
            ratesChanged = true;
          }
          localDate = localDate.plusDays(1);
        }
        if (ratesChanged) {
          try (FileOutputStream outputStream = new FileOutputStream(exchangeRate.getPath())) {
            IOUtils.write(exchangeRateObject.toString(), outputStream, StandardCharsets.UTF_8);
          }
        }
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
