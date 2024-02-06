/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.dapp.rest;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import io.meeds.dapp.web.rest.UemRewardController;
import io.meeds.dapp.web.security.DeedAccessDeniedHandler;
import io.meeds.dapp.web.security.DeedAuthenticationProvider;
import io.meeds.dapp.web.security.WebSecurityConfig;
import io.meeds.deeds.common.service.UemRewardService;
import io.meeds.wom.api.model.UemReward;

@SpringBootTest(classes = {
                            UemRewardController.class,
                            DeedAuthenticationProvider.class,
                            WebSecurityConfig.class,
                            DeedAccessDeniedHandler.class,
})
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
public class UemRewardControllerTest {

  private static final String   API_UEM_REWARDS  = "/api/uem/rewards";

  @MockBean
  private UemRewardService      rewardService;

  @Autowired
  private WebApplicationContext context;

  private MockMvc               mockMvc;

  private String                hubAddress       = "0x27d282d1e7e790df596f50a234602d9e761d22aa";

  private Set<String>           hubAddresses     = Collections.singleton(hubAddress);

  private long                  rewardId         = 255648;

  private double                amount           = 53364.23;

  private double                fixedGlobalIndex = 56.3669845;

  private Instant               fromDate         = Instant.now();

  private Instant               toDate           = Instant.now();

  private List<Long>            reportIds        = Collections.singletonList(2l);

  private double                sumEd            = 655893.25;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .apply(springSecurity())
                             .build();
  }

  @Test
  @WithAnonymousUser
  void getRewards() throws Exception {
    when(rewardService.getRewards(eq(hubAddress), any())).thenReturn(new PageImpl<>(Arrays.asList(newUemReward())));
    ResultActions response = mockMvc.perform(get(API_UEM_REWARDS).param("hubAddress", hubAddress));
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$.page.totalPages",is(1)));
  }

  @Test
  @WithAnonymousUser
  void getReward() throws Exception {
    when(rewardService.getRewardById(rewardId)).thenReturn(newUemReward());
    ResultActions response = mockMvc.perform(get(API_UEM_REWARDS + "/" + rewardId));
    response.andExpect(status().isOk())
    .andExpect(jsonPath("$.rewardId",is((int) rewardId)))
    .andExpect(jsonPath("$.amount",is(amount)))
    .andExpect(jsonPath("$.fixedGlobalIndex",is(fixedGlobalIndex)))
    .andExpect(jsonPath("$.reportsCount",is(1)))
    .andExpect(jsonPath("$.sumEd",is(sumEd)));
  }

  private UemReward newUemReward() {
    return new UemReward(rewardId,
                         amount,
                         fixedGlobalIndex,
                         fromDate,
                         toDate,
                         reportIds,
                         hubAddresses,
                         sumEd);
  }
}
