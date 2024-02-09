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

import static io.meeds.wom.api.utils.JsonUtils.toJsonStringNoCheckedEx;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import io.meeds.dapp.web.rest.HubReportController;
import io.meeds.dapp.web.security.DeedAccessDeniedHandler;
import io.meeds.dapp.web.security.DeedAuthenticationProvider;
import io.meeds.dapp.web.security.WebSecurityConfig;
import io.meeds.deeds.common.service.HubReportService;
import io.meeds.wom.api.constant.WomAuthorizationException;
import io.meeds.wom.api.constant.WomException;
import io.meeds.wom.api.constant.WomParsingException;
import io.meeds.wom.api.constant.WomRequestException;
import io.meeds.wom.api.model.HubReport;
import io.meeds.wom.api.model.HubReportVerifiableData;

@SpringBootTest(classes = {
                            HubReportController.class,
                            DeedAuthenticationProvider.class,
                            WebSecurityConfig.class,
                            DeedAccessDeniedHandler.class,
})
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
public class HubReportControllerTest {

  private static final String   HUB_ADDRESS_PARAM         = "hubAddress";

  private static final String   REWARD_ID_PARAM           = "rewardId";

  private static final String   API_HUB_REPORTS           = "/api/hub/reports";

  @MockBean
  private HubReportService      reportService;

  @Autowired
  private WebApplicationContext context;

  private MockMvc               mockMvc;

  private String                hubAddress                = "0x27d282d1e7e790df596f50a234602d9e761d22aa";

  private long                  rewardId                  = 56648l;

  private long                  deedId                    = 3l;

  private long                  reportId                  = 56684l;

  private long                  periodStartTime           = ZonedDateTime.now()
                                                                         .with(DayOfWeek.MONDAY)
                                                                         .minusWeeks(3)
                                                                         .toLocalDate()
                                                                         .atStartOfDay(ZoneOffset.UTC)
                                                                         .toEpochSecond();

  private long                  periodEndTime             = ZonedDateTime.now()
                                                                         .with(DayOfWeek.MONDAY)
                                                                         .minusWeeks(2)
                                                                         .toLocalDate()
                                                                         .atStartOfDay(ZoneOffset.UTC)
                                                                         .toEpochSecond();

  private short                 city                      = 1;

  private short                 cardType                  = 3;

  private short                 mintingPower              = 120;

  private long                  maxUsers                  = Long.MAX_VALUE;

  private int                   ownerMintingPercentage    = 60;

  private double                fixedRewardIndex          = 0.005446d;

  private double                ownerFixedIndex           = 0.0032676d;

  private double                tenantFixedIndex          = 0.0021784d;

  private double                lastPeriodUemRewardAmount = 84d;

  private double                uemRewardAmount           = 90d;

  private double                engagementScore           = 1.3d;

  private double                hubRewardAmount           = 150d;

  private double                hubTopRewardedAmount      = 39d;

  private Instant               updatedDate               = Instant.now();

  private boolean               fraud                     = false;

  private long                  usersCount                = 125l;

  private long                  participantsCount         = 85l;

  private long                  recipientsCount           = 65l;

  private int                   achievementsCount         = 55698;

  private int                   actionsCount              = 569;

  private String                rewardTokenAddress        = "0x334d85047da64738c065d36e10b2adeb965000d0";

  private long                  rewardTokenNetworkId      = 1l;

  private String                deedManagerAddress        = "0x609a6f01b7976439603356e41d5456b42df957b7";

  private String                ownerAddress              = "0x27d282d1e7e790df596f50a234602d9e761d22aa";

  private String                periodType                = "WEEK";

  private String                txHash                    =
                                       "0xef4e9db309b5dd7020ce463ae726b4d0759e1de0635661de91d8d98e83ae2862";

  private Instant               sentDate                  = Instant.now();

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .apply(springSecurity())
                             .build();
  }

  @Test
  @WithAnonymousUser
  void getReports() throws Exception {
    when(reportService.getReports(eq(hubAddress), eq(rewardId), any())).thenReturn(new PageImpl<>(Arrays.asList(newHubReport())));
    ResultActions response = mockMvc.perform(get(API_HUB_REPORTS)
                                             .param(HUB_ADDRESS_PARAM, hubAddress)
                                             .param(REWARD_ID_PARAM, String.valueOf(rewardId)));
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$.page.totalPages",is(1)));
  }

  @Test
  @WithAnonymousUser
  void getReportByHubAndRewardIdWhenNotFound() throws Exception {
    ResultActions response = mockMvc.perform(get(API_HUB_REPORTS + "/" + rewardId + "/" + hubAddress));
    response.andExpect(status().isNotFound());
  }

  @Test
  @WithAnonymousUser
  void getReportByHubAndRewardId() throws Exception {
    when(reportService.getReport(rewardId, hubAddress)).thenReturn(newHubReport());
    ResultActions response = mockMvc.perform(get(API_HUB_REPORTS + "/" + rewardId + "/" + hubAddress));
    response.andExpect(status().isOk())
    .andExpect(jsonPath("$.deedId",is((int) deedId)))
    .andExpect(jsonPath("$.rewardId",is((int) rewardId)))
    .andExpect(jsonPath("$.city",is((int) city)))
    .andExpect(jsonPath("$.cardType",is((int) cardType)))
    .andExpect(jsonPath("$.mintingPower",is((int) mintingPower)))
    .andExpect(jsonPath("$.maxUsers",is(maxUsers)))
    .andExpect(jsonPath("$.deedManagerAddress",is(deedManagerAddress)))
    .andExpect(jsonPath("$.ownerAddress",is(ownerAddress)))
    .andExpect(jsonPath("$.ownerMintingPercentage",is(ownerMintingPercentage)))
    .andExpect(jsonPath("$.fixedRewardIndex",is(fixedRewardIndex)))
    .andExpect(jsonPath("$.ownerFixedIndex",is(ownerFixedIndex)))
    .andExpect(jsonPath("$.tenantFixedIndex",is(tenantFixedIndex)))
    .andExpect(jsonPath("$.fraud",is(fraud)))
    .andExpect(jsonPath("$.uemRewardAmount",is(uemRewardAmount)))
    .andExpect(jsonPath("$.reportId",is((int) reportId)))
    .andExpect(jsonPath("$.hubAddress",is(hubAddress)))
    .andExpect(jsonPath("$.usersCount",is((int) usersCount)))
    .andExpect(jsonPath("$.participantsCount",is((int) participantsCount)))
    .andExpect(jsonPath("$.recipientsCount",is((int) recipientsCount)))
    .andExpect(jsonPath("$.achievementsCount",is(achievementsCount)))
    .andExpect(jsonPath("$.actionsCount",is(actionsCount)))
    .andExpect(jsonPath("$.rewardTokenNetworkId",is((int) rewardTokenNetworkId)))
    .andExpect(jsonPath("$.hubRewardAmount",is(hubRewardAmount)))
    .andExpect(jsonPath("$.rewardTokenAddress",is(rewardTokenAddress)));
  }

  @Test
  @WithAnonymousUser
  void getReportWhenNotFound() throws Exception {
    ResultActions response = mockMvc.perform(get(API_HUB_REPORTS + "/" + reportId));
    response.andExpect(status().isNotFound());
  }

  @Test
  @WithAnonymousUser
  void getReport() throws Exception {
    when(reportService.getReport(reportId)).thenReturn(newHubReport());
    ResultActions response = mockMvc.perform(get(API_HUB_REPORTS + "/" + reportId));
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$.deedId",is((int) deedId)))
            .andExpect(jsonPath("$.rewardId",is((int) rewardId)))
            .andExpect(jsonPath("$.city",is((int) city)))
            .andExpect(jsonPath("$.cardType",is((int) cardType)))
            .andExpect(jsonPath("$.mintingPower",is((int) mintingPower)))
            .andExpect(jsonPath("$.maxUsers",is(maxUsers)))
            .andExpect(jsonPath("$.deedManagerAddress",is(deedManagerAddress)))
            .andExpect(jsonPath("$.ownerAddress",is(ownerAddress)))
            .andExpect(jsonPath("$.ownerMintingPercentage",is(ownerMintingPercentage)))
            .andExpect(jsonPath("$.fixedRewardIndex",is(fixedRewardIndex)))
            .andExpect(jsonPath("$.ownerFixedIndex",is(ownerFixedIndex)))
            .andExpect(jsonPath("$.tenantFixedIndex",is(tenantFixedIndex)))
            .andExpect(jsonPath("$.fraud",is(fraud)))
            .andExpect(jsonPath("$.uemRewardAmount",is(uemRewardAmount)))
            .andExpect(jsonPath("$.reportId",is((int) reportId)))
            .andExpect(jsonPath("$.hubAddress",is(hubAddress)))
            .andExpect(jsonPath("$.usersCount",is((int) usersCount)))
            .andExpect(jsonPath("$.participantsCount",is((int) participantsCount)))
            .andExpect(jsonPath("$.recipientsCount",is((int) recipientsCount)))
            .andExpect(jsonPath("$.achievementsCount",is(achievementsCount)))
            .andExpect(jsonPath("$.rewardTokenNetworkId",is((int) rewardTokenNetworkId)))
            .andExpect(jsonPath("$.hubRewardAmount",is(hubRewardAmount)))
            .andExpect(jsonPath("$.engagementScore",is(engagementScore)))
            .andExpect(jsonPath("$.rewardTokenAddress",is(rewardTokenAddress)));
  }

  @Test
  @WithAnonymousUser
  void saveReportWhenWomRequestException() throws Exception {
    doThrow(WomRequestException.class).when(reportService).saveReport(any());
    ResultActions response = mockMvc.perform(post(API_HUB_REPORTS).contentType(MediaType.APPLICATION_JSON)
                                                                  .content(toJsonStringNoCheckedEx(new HubReportVerifiableData())));
    response.andExpect(status().isBadRequest());
  }

  @Test
  @WithAnonymousUser
  void saveReportWhenWomParsingException() throws Exception {
    doThrow(WomParsingException.class).when(reportService).saveReport(any());
    ResultActions response = mockMvc.perform(post(API_HUB_REPORTS).contentType(MediaType.APPLICATION_JSON)
                                                                  .content(toJsonStringNoCheckedEx(new HubReportVerifiableData())));
    response.andExpect(status().isBadRequest());
  }

  @Test
  @WithAnonymousUser
  void saveReportWhenWomAuthorizationException() throws Exception {
    doThrow(WomAuthorizationException.class).when(reportService).saveReport(any());
    ResultActions response = mockMvc.perform(post(API_HUB_REPORTS).contentType(MediaType.APPLICATION_JSON)
                                                                  .content(toJsonStringNoCheckedEx(new HubReportVerifiableData())));
    response.andExpect(status().isUnauthorized());
  }

  @Test
  @WithAnonymousUser
  void saveReportWhenWomException() throws Exception {
    doThrow(WomException.class).when(reportService).saveReport(any());
    ResultActions response = mockMvc.perform(post(API_HUB_REPORTS).contentType(MediaType.APPLICATION_JSON)
                                                                  .content(toJsonStringNoCheckedEx(new HubReportVerifiableData())));
    response.andExpect(status().isInternalServerError());
  }

  @Test
  @WithAnonymousUser
  void saveReport() throws Exception {
    ResultActions response = mockMvc.perform(post(API_HUB_REPORTS).contentType(MediaType.APPLICATION_JSON)
                                                                  .content(toJsonStringNoCheckedEx(new HubReportVerifiableData())));
    response.andExpect(status().isOk());
  }

  private HubReport newHubReport() {
    return new HubReport(reportId,
                         StringUtils.lowerCase(hubAddress),
                         deedId,
                         fromDate(),
                         toDate(),
                         sentDate,
                         periodType,
                         usersCount,
                         participantsCount,
                         recipientsCount,
                         achievementsCount,
                         actionsCount,
                         rewardTokenAddress,
                         rewardTokenNetworkId,
                         hubRewardAmount,
                         hubTopRewardedAmount,
                         transactions(),
                         rewardId,
                         city,
                         cardType,
                         mintingPower,
                         maxUsers,
                         StringUtils.lowerCase(deedManagerAddress),
                         StringUtils.lowerCase(ownerAddress),
                         ownerMintingPercentage,
                         fixedRewardIndex,
                         ownerFixedIndex,
                         tenantFixedIndex,
                         fraud,
                         lastPeriodUemRewardAmount,
                         uemRewardAmount,
                         updatedDate,
                         engagementScore);
  }

  private Instant toDate() {
    return Instant.ofEpochSecond(periodEndTime);
  }

  private Instant fromDate() {
    return Instant.ofEpochSecond(periodStartTime);
  }

  private TreeSet<String> transactions() {
    TreeSet<String> transactions = new TreeSet<>();
    transactions.add(txHash);
    return transactions;
  }

}
