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

    MeedTokenMetric result = new MeedTokenMetric(LocalDate.now(),
                                                 new BigDecimal("1"),
                                                 Collections.singletonMap("", new BigDecimal("2")),
                                                 Collections.singletonMap("", new BigDecimal("3")),
                                                 new BigDecimal("4"),
                                                 new BigDecimal("5"),
                                                 new BigDecimal("6"),
                                                 new BigDecimal("7"));

    when(meedTokenMetricService.getLastMetric()).thenReturn(result);

    ResultActions response = mockMvc.perform(get("/api/token/meed/"));
    response.andExpect(status().isOk());
    // TODO on all attributes .andExpect(jsonPath(null, null))
  }

}
