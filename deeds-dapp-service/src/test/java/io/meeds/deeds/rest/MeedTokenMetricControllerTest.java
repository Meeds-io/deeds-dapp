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
package io.meeds.deeds.rest;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import io.meeds.deeds.model.MeedTokenMetric;
import io.meeds.deeds.service.MeedTokenMetricService;
import io.meeds.deeds.web.rest.MeedTokenMetricController;

@SpringBootTest(
    classes = {
        MeedTokenMetricController.class
    }
)
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
class MeedTokenMetricControllerTest {

  @MockBean
  private MeedTokenMetricService meedTokenMetricService;

  @Autowired
  private MockMvc                mockMvc;

  @Test
  void testGetLastMetric() throws Exception {

    LocalDate today = LocalDate.now();

    MeedTokenMetric result = new MeedTokenMetric(today,
                                                 new BigDecimal("1"),
                                                 Collections.singletonMap("", new BigDecimal("2")),
                                                 Collections.singletonMap("", new BigDecimal("3")),
                                                 new BigDecimal("4"),
                                                 new BigDecimal("5"),
                                                 new BigDecimal("6"),
                                                 new BigDecimal("7"));

    when(meedTokenMetricService.getLastMetric()).thenReturn(result);

    ResultActions response = mockMvc.perform(get("/api/token/meed/"));
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$.date",is(today.toString())))
            .andExpect(jsonPath("$.totalSupply",is(1)))
            .andExpect(jsonPath("$.lockedBalances",aMapWithSize(1)))
            .andExpect(jsonPath("$.reserveBalances",aMapWithSize(1)))
            .andExpect(jsonPath("$.circulatingSupply",is(4)))
            .andExpect(jsonPath("$.marketCapitalization",is(5)))
            .andExpect(jsonPath("$.totalValuelocked",is(6)))
            .andExpect(jsonPath("$.meedUsdPrice",is(7)));
  }

  @Test
  void testGetCirculatingSupply() throws Exception {

    BigDecimal circulatingSupply = new BigDecimal("2");

    when(meedTokenMetricService.getCirculatingSupply()).thenReturn(circulatingSupply);

    ResultActions response = mockMvc.perform(get("/api/token/meed/circ"));
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$", is(2)));
  }

  @Test
  void testGetMarketCapitalization() throws Exception {

    BigDecimal marketCapitalization = new BigDecimal("5");

    when(meedTokenMetricService.getMarketCapitalization()).thenReturn(marketCapitalization);

    ResultActions response = mockMvc.perform(get("/api/token/meed/mcap"));
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$", is(5)));
  }

  @Test
  void testGetTotalLockedValue() throws Exception {

    BigDecimal totalValuelocked = new BigDecimal("4");

    when(meedTokenMetricService.getTotalValueLocked()).thenReturn(totalValuelocked);

    ResultActions response = mockMvc.perform(get("/api/token/meed/tvl"));
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$", is(4)));
  }

  @Test
  void testGetTotalSupply() throws Exception {

    BigDecimal totalSupply = new BigDecimal("3");

    when(meedTokenMetricService.getTotalSupply()).thenReturn(totalSupply);

    ResultActions response = mockMvc.perform(get("/api/token/meed/supply"));
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$", is(3)));
  }

}
