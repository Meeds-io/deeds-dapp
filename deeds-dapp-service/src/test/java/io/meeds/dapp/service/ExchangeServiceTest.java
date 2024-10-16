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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;

import io.meeds.dapp.constant.Currency;
import io.meeds.dapp.elasticsearch.model.CurrencyExchangeRate;
import io.meeds.dapp.elasticsearch.model.MeedExchangeRate;
import io.meeds.dapp.model.MeedPrice;
import io.meeds.dapp.service.ExchangeServiceTest.ExchangeServiceNoInit;
import io.meeds.dapp.storage.CurrencyExchangeRateRepository;
import io.meeds.dapp.storage.MeedExchangeRateRepository;

@SpringBootTest(
                classes = {
                            ExchangeServiceNoInit.class,
                })
@TestPropertySource(
                    properties = {
                                   "meeds.exchange.currencyApiUrl=CurrencyApiUrl",
                                   "meeds.exchange.currencyApiKey=CurrencyApiKey",
                                   "meeds.exchange.blockchainApiUrl=BlockchainApiUrl",
                                   "meeds.exchange.lpTokenApiUrl=LpTokenApiUrl",
                                   "meeds.exchange.lpTokenAddress=LpTokenAddress",
                    })
class ExchangeServiceTest {

  @MockBean
  private CurrencyExchangeRateRepository currencyExchangeRateRepository;

  @MockBean
  private MeedExchangeRateRepository     meedExchangeRateRepository;

  @Autowired
  private ExchangeServiceNoInit          exchangeService;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void init() {
    reset(currencyExchangeRateRepository, meedExchangeRateRepository);
  }

  @Test
  void testComputeTodayCurrencyExchangeRate() {
    assertNotNull(exchangeService);

    exchangeService.computeTodayCurrencyExchangeRate();
    verify(currencyExchangeRateRepository, times(1)).save(any());
  }

  @Test
  void testComputeCurrencyExchangeRate() {
    assertNotNull(exchangeService);

    exchangeService.computeCurrencyExchangeRate();
    Duration between = Duration.between(exchangeService.firstMeedTokenDate().atStartOfDay(), LocalDate.now().atStartOfDay());
    verify(currencyExchangeRateRepository,
           times((int) between.toDays() + 2)).save(argThat(rate -> BigDecimal.valueOf(1.8d).equals(rate.getRate()) && rate.getCurrency() == Currency.EUR));

    verify(currencyExchangeRateRepository,
           times(1)).save(new CurrencyExchangeRate(exchangeService.firstMeedTokenDate(), Currency.EUR, BigDecimal.valueOf(1.8d)));
  }

  @Test
  void testComputeMeedExchangeRate() {
    assertNotNull(exchangeService);

    exchangeService.computeMeedExchangeRate();
    Duration between = Duration.between(exchangeService.firstMeedTokenDate().atStartOfDay(), LocalDate.now().atStartOfDay());
    verify(meedExchangeRateRepository,
           times((int) between.toDays() + 1)).save(
                                                   argThat(rate -> rate != null && rate.getDate() != null
                                                                   && BigDecimal.valueOf(300).equals(rate.getEthReserve())
                                                                   && BigDecimal.valueOf(200).equals(rate.getMeedReserve())
                                                                   && BigDecimal.valueOf(2).equals(rate.getMeedEthPrice())
                                                                   && BigDecimal.valueOf(3).equals(rate.getEthUsdPrice())));

    verify(meedExchangeRateRepository, times(1)).save(new MeedExchangeRate(exchangeService.firstMeedTokenDate(),
                                                                           BigDecimal.valueOf(3),
                                                                           BigDecimal.valueOf(2),
                                                                           BigDecimal.valueOf(200),
                                                                           BigDecimal.valueOf(300),
                                                                           true));
    verify(meedExchangeRateRepository, times(1)).save(new MeedExchangeRate(LocalDate.now(),
                                                                           BigDecimal.valueOf(3),
                                                                           BigDecimal.valueOf(2),
                                                                           BigDecimal.valueOf(200),
                                                                           BigDecimal.valueOf(300),
                                                                           false));
  }

  @Test
  void testGetExchangeRates() {
    assertNotNull(exchangeService);

    mockExchangeRates();

    List<MeedPrice> exchangeRates = exchangeService.getExchangeRates(Currency.ETH, LocalDate.now().minusDays(2), LocalDate.now());
    assertNotNull(exchangeRates);
    assertEquals(3, exchangeRates.size());
    MeedPrice meedPrice = exchangeRates.get(0);
    assertNotNull(meedPrice);
    assertEquals(LocalDate.now().minusDays(2), meedPrice.getDate());
    assertEquals(BigDecimal.valueOf(2), meedPrice.getEthPrice());
    assertEquals(BigDecimal.valueOf(2), meedPrice.getCurrencyPrice());

    exchangeRates = exchangeService.getExchangeRates(Currency.USD, LocalDate.now().minusDays(2), LocalDate.now());
    assertNotNull(exchangeRates);
    assertEquals(3, exchangeRates.size());
    meedPrice = exchangeRates.get(0);
    assertEquals(new MeedPrice(LocalDate.now().minusDays(2), BigDecimal.valueOf(2), BigDecimal.valueOf(6)), meedPrice);

    exchangeRates = exchangeService.getExchangeRates(Currency.EUR, LocalDate.now().minusDays(2), LocalDate.now());
    assertNotNull(exchangeRates);
    assertEquals(3, exchangeRates.size());
    meedPrice = exchangeRates.get(0);
    assertEquals(new MeedPrice(LocalDate.now().minusDays(2), BigDecimal.valueOf(2), BigDecimal.valueOf(10.8)), meedPrice);
    assertEquals(new MeedPrice(LocalDate.now().minusDays(2), BigDecimal.valueOf(2), BigDecimal.valueOf(10.8)).hashCode(),
                 meedPrice.hashCode());
  }

  @Test
  void testGetExchangeRate() {
    assertNotNull(exchangeService);

    mockExchangeRates();

    BigDecimal usdExchangeRate = exchangeService.getExchangeRate(Currency.USD);
    assertEquals(BigDecimal.ONE, usdExchangeRate);

    usdExchangeRate = exchangeService.getExchangeRate(null);
    assertEquals(BigDecimal.ONE, usdExchangeRate);

    BigDecimal ethExchangeRate = exchangeService.getExchangeRate(Currency.ETH);
    assertEquals(new BigDecimal("0.333333333333333333"), ethExchangeRate);

    BigDecimal eurExchangeRate = exchangeService.getExchangeRate(Currency.EUR);
    assertEquals(BigDecimal.valueOf(1.8), eurExchangeRate);
  }

  @Test
  void testGetMeedUsdPrice() {
    assertNotNull(exchangeService);

    List<MeedExchangeRate> meedExchangeRates = new ArrayList<>();

    when(meedExchangeRateRepository.save(any())).thenAnswer(invocation -> {
      MeedExchangeRate exchangeRate = invocation.getArgument(0, MeedExchangeRate.class);
      meedExchangeRates.add(exchangeRate);
      return exchangeRate;
    });

    exchangeService.computeMeedExchangeRate();

    when(meedExchangeRateRepository.findByDateBetween(any(), any())).thenAnswer(invocation -> {
      LocalDate fromDate = invocation.getArgument(0, LocalDate.class);
      LocalDate toDate = invocation.getArgument(1, LocalDate.class);
      return meedExchangeRates.stream()
                              .filter(meedExchangeRate -> (meedExchangeRate.getDate().isAfter(fromDate)
                                                           || meedExchangeRate.getDate().isEqual(fromDate))
                                                          && (meedExchangeRate.getDate().isBefore(toDate)
                                                              || meedExchangeRate.getDate().isEqual(toDate)))
                              .toList();
    });

    BigDecimal price = exchangeService.getMeedUsdPrice();
    assertEquals(BigDecimal.valueOf(6), price);
  }

  private void mockExchangeRates() {
    List<CurrencyExchangeRate> currencyExchangeRates = new ArrayList<>();
    List<MeedExchangeRate> meedExchangeRates = new ArrayList<>();

    when(meedExchangeRateRepository.save(any())).thenAnswer(invocation -> {
      MeedExchangeRate exchangeRate = invocation.getArgument(0, MeedExchangeRate.class);
      meedExchangeRates.add(exchangeRate);
      return exchangeRate;
    });
    when(currencyExchangeRateRepository.save(any())).thenAnswer(invocation -> {
      CurrencyExchangeRate exchangeRate = invocation.getArgument(0, CurrencyExchangeRate.class);
      currencyExchangeRates.add(exchangeRate);
      return exchangeRate;
    });

    exchangeService.computeMeedExchangeRate();
    exchangeService.computeCurrencyExchangeRate();

    when(currencyExchangeRateRepository.findByCurrencyAndDateBetween(any(), any(), any())).thenAnswer(invocation -> {
      LocalDate fromDate = invocation.getArgument(1, LocalDate.class);
      LocalDate toDate = invocation.getArgument(2, LocalDate.class);
      return currencyExchangeRates.stream()
                                  .filter(currencyExchangeRate -> (currencyExchangeRate.getDate().isAfter(fromDate)
                                                                   || currencyExchangeRate.getDate().isEqual(fromDate))
                                                                  && (currencyExchangeRate.getDate().isBefore(toDate)
                                                                      || currencyExchangeRate.getDate().isEqual(toDate)))
                                  .toList();
    });

    when(meedExchangeRateRepository.findByDateBetween(any(), any())).thenAnswer(invocation -> {
      LocalDate fromDate = invocation.getArgument(0, LocalDate.class);
      LocalDate toDate = invocation.getArgument(1, LocalDate.class);
      return meedExchangeRates.stream()
                              .filter(meedExchangeRate -> (meedExchangeRate.getDate().isAfter(fromDate)
                                                           || meedExchangeRate.getDate().isEqual(fromDate))
                                                          && (meedExchangeRate.getDate().isBefore(toDate)
                                                              || meedExchangeRate.getDate().isEqual(toDate)))
                              .toList();
    });
  }

  @Component
  public static class ExchangeServiceNoInit extends ExchangeService {
    private static final String LP_TOKEN_API_URL = "LpTokenApiUrl";

    private AtomicBoolean ethPriceRequest      = new AtomicBoolean(false);

    private AtomicBoolean simulateErrorRequest = new AtomicBoolean(false);

    @Override
    protected String executeQuery(String url, String body) {
      ethPriceRequest.set(StringUtils.contains(body, "ethPrice"));
      if (StringUtils.contains(body, "block:")) {
        simulateErrorRequest.set(!simulateErrorRequest.get());
      }
      return super.executeQuery(url, body);
    }

    @Override
    protected HttpsURLConnection newURLConnection(String apiUrl) throws IOException {
      if (StringUtils.contains(apiUrl, "CurrencyApiUrl")) {
        HttpsURLConnection connection = mock(HttpsURLConnection.class);
        when(connection.getResponseCode()).thenReturn(200);
        when(connection.getInputStream()).thenReturn(new ByteArrayInputStream("{\"data\": {\"EUR\": {\"value\": 1.8}}}".getBytes()));
        return connection;
      } else if (StringUtils.contains(apiUrl, "BlockchainApiUrl")) {
        HttpsURLConnection connection = mock(HttpsURLConnection.class);
        when(connection.getResponseCode()).thenReturn(200);
        when(connection.getOutputStream()).thenReturn(mock(OutputStream.class));
        when(connection.getInputStream()).thenReturn(new ByteArrayInputStream("{\"data\": {\"blocks\": [{\"number\": \"1111\"}]}}".getBytes()));
        return connection;
      } else if (StringUtils.contains(apiUrl, LP_TOKEN_API_URL) && simulateErrorRequest.get()) {
        HttpsURLConnection connection = mock(HttpsURLConnection.class);
        when(connection.getResponseCode()).thenReturn(200);
        when(connection.getOutputStream()).thenReturn(mock(OutputStream.class));
        when(connection.getInputStream()).thenReturn(new ByteArrayInputStream("{\"errors\": [{\"message\": \"Error: up to block number 1000 .\"}]}".getBytes()));
        return connection;
      } else if (StringUtils.contains(apiUrl, LP_TOKEN_API_URL) && ethPriceRequest.get()) {
        HttpsURLConnection connection = mock(HttpsURLConnection.class);
        when(connection.getResponseCode()).thenReturn(200);
        when(connection.getOutputStream()).thenReturn(mock(OutputStream.class));
        when(connection.getInputStream()).thenReturn(new ByteArrayInputStream("{\"data\": {\"bundle\": {\"ethPrice\": \"3\"}}}".getBytes()));
        return connection;
      } else if (StringUtils.contains(apiUrl, LP_TOKEN_API_URL)) {
        HttpsURLConnection connection = mock(HttpsURLConnection.class);
        when(connection.getResponseCode()).thenReturn(200);
        when(connection.getOutputStream()).thenReturn(mock(OutputStream.class));
        when(connection.getInputStream()).thenReturn(new ByteArrayInputStream("{\"data\": {\"pair\": {\"token1Price\": \"2\", \"reserve0\": \"200\", \"reserve1\": \"300\"}}}".getBytes()));
        return connection;
      } else {
        return super.newURLConnection(apiUrl);
      }
    }

    @Override
    protected LocalDate firstMeedTokenDate() {
      return LocalDate.now().minusDays(3);
    }
  }
}
