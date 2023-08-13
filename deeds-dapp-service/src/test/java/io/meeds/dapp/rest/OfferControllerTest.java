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
package io.meeds.dapp.rest;

import static io.meeds.deeds.common.constant.CommonConstants.CODE_VERIFICATION_HTTP_HEADER;
import static io.meeds.deeds.common.listerner.model.EventSerialization.OBJECT_MAPPER;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.tomcat.util.buf.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import io.meeds.dapp.constant.ExpirationDuration;
import io.meeds.dapp.constant.NoticePeriod;
import io.meeds.dapp.constant.OfferType;
import io.meeds.dapp.constant.RentalDuration;
import io.meeds.dapp.constant.RentalPaymentPeriodicity;
import io.meeds.dapp.model.DeedTenantOfferDTO;
import io.meeds.dapp.service.OfferService;
import io.meeds.dapp.web.rest.OfferController;
import io.meeds.dapp.web.security.DeedAccessDeniedHandler;
import io.meeds.dapp.web.security.DeedAuthenticationProvider;
import io.meeds.dapp.web.security.WebSecurityConfig;
import io.meeds.deeds.api.constant.ObjectNotFoundException;
import io.meeds.deeds.common.constant.DeedCard;
import io.meeds.deeds.common.constant.DeedCity;
import io.meeds.deeds.common.constant.TransactionStatus;
import io.meeds.deeds.common.constant.UnauthorizedOperationException;
import io.meeds.deeds.common.service.AuthorizationCodeService;

import jakarta.servlet.Filter;

@SpringBootTest(classes = {
                            OfferController.class,
                            DeedAuthenticationProvider.class,
                            DeedAccessDeniedHandler.class,
})
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {
                                  WebSecurityConfig.class
})
class OfferControllerTest {

  private static final String      TEST_PASSWORD = "testPassword";

  private static final String      TEST_USER     = "testUser";

  @MockBean
  private OfferService             deedTenantOfferService;

  @MockBean
  private AuthorizationCodeService authorizationCodeService;

  @Autowired
  private SecurityFilterChain      filterChain;

  @Autowired
  private WebApplicationContext    context;

  private MockMvc                  mockMvc;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .addFilters(filterChain.getFilters().toArray(new Filter[0]))
                             .build();
  }

  @Test
  void testGetOffersByNftId() throws Exception {
    String offerId = "offerId";
    long nftId = 2l;
    DeedTenantOfferDTO deedTenantOfferDTO = newOffer(offerId, nftId);

    when(deedTenantOfferService.getOffers(any(), any(Pageable.class))).thenAnswer(invocation -> {
      Pageable pageable = invocation.getArgument(1, Pageable.class);
      return new PageImpl<>(Collections.singletonList(deedTenantOfferDTO), pageable, 1);
    });

    ResultActions response = mockMvc.perform(get("/api/offers?networkId=1&nftId=" + nftId + "&networkId=1&address=0xAdr"));
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded", notNullValue()))
            .andExpect(jsonPath("$._embedded.offers", notNullValue()))
            .andExpect(jsonPath("$.page", notNullValue()))
            .andExpect(jsonPath("$.page.totalElements", is(1)));
  }

  @Test
  void testGetOffersByOfferTypesAndCardTypes() throws Exception {
    String offerId = "offerId";
    long nftId = 2l;
    DeedTenantOfferDTO deedTenantOfferDTO = newOffer(offerId, nftId);
    List<DeedCard> cardTypes = Arrays.asList(DeedCard.COMMON, DeedCard.UNCOMMON);
    List<OfferType> offerTypes = Arrays.asList(OfferType.RENTING, OfferType.SALE);

    when(deedTenantOfferService.getOffers(any(), any(Pageable.class))).thenAnswer(invocation -> {
      Pageable pageable = invocation.getArgument(1, Pageable.class);
      return new PageImpl<>(Collections.singletonList(deedTenantOfferDTO), pageable, 1);
    });

    String link = "/api/offers?networkId=1&address=0xAdr&";
    link += StringUtils.join(cardTypes.stream().map(cardType -> "cardType=" + cardType.name()).collect(Collectors.toList()), '&');
    link += "&";
    link += StringUtils.join(offerTypes.stream().map(offerType -> "offerType=" + offerType.name()).collect(Collectors.toList()),
                             '&');
    ResultActions response = mockMvc.perform(get(link));
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded", notNullValue()))
            .andExpect(jsonPath("$._embedded.offers", notNullValue()))
            .andExpect(jsonPath("$.page", notNullValue()))
            .andExpect(jsonPath("$.page.totalElements", is(1)));
  }

  @Test
  void testGetOfferNotFound() throws Exception {
    long offerId = 2l;
    ResultActions response = mockMvc.perform(get("/api/offers/" + offerId));
    response.andExpect(status().isNotFound());
  }

  @Test
  void testGetOffer() throws Exception {
    String offerId = "offerId";
    long nftId = 3l;
    DeedTenantOfferDTO deedTenantOfferDTO = newOffer(offerId, nftId);
    when(deedTenantOfferService.getOffer(eq(offerId), any(), eq(false))).thenReturn(deedTenantOfferDTO);

    ResultActions response = mockMvc.perform(get("/api/offers/" + offerId));
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$", notNullValue()));
  }

  @Test
  void testGetOfferWhenMeantToTenantAnonymously() throws Exception {
    String offerId = "offerId";
    long nftId = 3l;
    DeedTenantOfferDTO deedTenantOfferDTO = newOffer(offerId, nftId);
    deedTenantOfferDTO.setHostAddress("tenant");
    when(deedTenantOfferService.getOffer(offerId, null, false)).thenThrow(ObjectNotFoundException.class);

    ResultActions response = mockMvc.perform(get("/api/offers/" + offerId));
    response.andExpect(status().isNotFound());
  }

  @Test
  void testGetProtectedOfferAnonymously() throws Exception {
    String offerId = "offerId";
    long nftId = 3l;
    DeedTenantOfferDTO deedTenantOfferDTO = newOffer(offerId, nftId);
    deedTenantOfferDTO.setHostAddress("tenant");
    when(deedTenantOfferService.getOffer(offerId, null, false)).thenThrow(UnauthorizedOperationException.class);

    ResultActions response = mockMvc.perform(get("/api/offers/" + offerId));
    response.andExpect(status().isNotFound());
  }

  @Test
  void testGetOfferWhenMeantToTenant() throws Exception {
    long nftId = 3l;
    String offerId = "offerId";
    DeedTenantOfferDTO deedTenantOfferDTO = newOffer(offerId, nftId);
    deedTenantOfferDTO.setHostAddress(TEST_USER);
    when(deedTenantOfferService.getOffer(eq(offerId), any(), eq(false))).thenReturn(deedTenantOfferDTO);

    ResultActions response = mockMvc.perform(get("/api/offers/" + offerId).accept(MediaType.APPLICATION_JSON)
                                                                          .with(testUser())
                                                                          .with(csrf()));
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$", notNullValue()));
  }

  @Test
  void testCreateOfferWithAnonymousUser() throws Exception {
    long nftId = 3l;
    String offerId = "offerId";
    DeedTenantOfferDTO deedTenantOfferDTO = newOffer(offerId, nftId);
    ResultActions response = mockMvc.perform(post("/api/offers").content(asJsonString(deedTenantOfferDTO))
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .accept(MediaType.APPLICATION_JSON)
                                                                .header(CODE_VERIFICATION_HTTP_HEADER,
                                                                        String.valueOf(1234l)));
    response.andExpect(status().is3xxRedirection());
    verify(deedTenantOfferService, never()).createRentingOffer(any(), any(), any());
  }

  @Test
  void testCreateOfferWithUserWithoutVerificationCode() throws Exception {
    long nftId = 3l;
    String offerId = "offerId";
    DeedTenantOfferDTO deedTenantOfferDTO = newOffer(offerId, nftId);
    ResultActions response = mockMvc.perform(post("/api/offers").content(asJsonString(deedTenantOfferDTO))
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .accept(MediaType.APPLICATION_JSON)
                                                                .with(testUser())
                                                                .with(csrf()));
    response.andExpect(status().isBadRequest());
    verify(deedTenantOfferService, never()).createRentingOffer(any(), any(), any());
  }

  @Test
  void testCreateOfferWithUserWithInvalidVerificationCode() throws Exception {
    long nftId = 3l;
    int code = 588865;
    String offerId = "offerId";
    DeedTenantOfferDTO deedTenantOfferDTO = newOffer(offerId, nftId);

    when(authorizationCodeService.validateAndGetData(TEST_USER.toLowerCase(), code)).thenThrow(IllegalAccessException.class);
    ResultActions response = mockMvc.perform(post("/api/offers").content(asJsonString(deedTenantOfferDTO))
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .accept(MediaType.APPLICATION_JSON)
                                                                .with(testUser())
                                                                .with(csrf())
                                                                .header(CODE_VERIFICATION_HTTP_HEADER,
                                                                        String.valueOf(code)));
    response.andExpect(status().isForbidden());
    verify(deedTenantOfferService, never()).createRentingOffer(any(), any(), any());
  }

  @Test
  void testCreateOfferWithUserWithValidVerificationCode() throws Exception {
    long nftId = 3l;
    String offerId = "offerId";
    String email = "email";
    int code = 588865;
    DeedTenantOfferDTO deedTenantOfferDTO = newOffer(offerId, nftId);
    when(authorizationCodeService.validateAndGetData(TEST_USER.toLowerCase(), code)).thenReturn(email);
    ResultActions response = mockMvc.perform(post("/api/offers").content(asJsonString(deedTenantOfferDTO))
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .accept(MediaType.APPLICATION_JSON)
                                                                .with(testUser())
                                                                .with(csrf())
                                                                .header(CODE_VERIFICATION_HTTP_HEADER,
                                                                        String.valueOf(code)));
    response.andExpect(status().isOk());
    verify(deedTenantOfferService, times(1)).createRentingOffer(TEST_USER.toLowerCase(), email, deedTenantOfferDTO);
  }

  @Test
  void testUpdateOfferWithAnonymousUser() throws Exception {
    String offerId = "offerId";
    long nftId = 3l;
    DeedTenantOfferDTO deedTenantOfferDTO = newOffer(offerId, nftId);
    ResultActions response = mockMvc.perform(put("/api/offers/" + offerId).content(asJsonString(deedTenantOfferDTO))
                                                                          .contentType(MediaType.APPLICATION_JSON)
                                                                          .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().is3xxRedirection());
    verify(deedTenantOfferService, never()).updateRentingOffer(any(), any());
  }

  @Test
  void testUpdateOfferWithUser() throws Exception {
    String offerId = "offerId";
    long nftId = 3l;
    DeedTenantOfferDTO deedTenantOfferDTO = newOffer(offerId, nftId);
    deedTenantOfferDTO.setId(offerId);
    ResultActions response = mockMvc.perform(put("/api/offers/" + offerId).content(asJsonString(deedTenantOfferDTO))
                                                                          .contentType(MediaType.APPLICATION_JSON)
                                                                          .accept(MediaType.APPLICATION_JSON)
                                                                          .with(testUser())
                                                                          .with(csrf()));
    response.andExpect(status().isOk());
    verify(deedTenantOfferService, times(1)).updateRentingOffer(TEST_USER, deedTenantOfferDTO);
  }

  @Test
  void testDeleteOfferWithAnonymousUser() throws Exception {
    String offerId = "offerId";
    ResultActions response = mockMvc.perform(delete("/api/offers/" + offerId));
    response.andExpect(status().is3xxRedirection());
    verify(deedTenantOfferService, never()).deleteRentingOffer(any(), any(), any());
  }

  @Test
  void testDeleteOfferWithUser() throws Exception {
    String offerId = "offerId";
    String transactionHash = "transactionHash";
    ResultActions response = mockMvc.perform(delete("/api/offers/" + offerId + "?transactionHash=" +
        transactionHash).with(testUser())
                        .with(csrf()));
    response.andExpect(status().isOk());
    verify(deedTenantOfferService, times(1)).deleteRentingOffer(TEST_USER, offerId, transactionHash);
  }

  private DeedTenantOfferDTO newOffer(String offerId, long nftId) {
    return new DeedTenantOfferDTO(offerId,
                                  nftId,
                                  nftId,
                                  DeedCity.ASHTARTE,
                                  DeedCard.UNCOMMON,
                                  "owner",
                                  null,
                                  "description",
                                  5d,
                                  10d,
                                  OfferType.RENTING,
                                  ExpirationDuration.ONE_DAY,
                                  ExpirationDuration.ONE_DAY.getDays(),
                                  RentalDuration.ONE_MONTH,
                                  RentalDuration.ONE_MONTH.getMonths(),
                                  NoticePeriod.ONE_MONTH,
                                  NoticePeriod.ONE_MONTH.getMonths(),
                                  RentalPaymentPeriodicity.ONE_YEAR,
                                  1,
                                  1.1d,
                                  "0xTransaction",
                                  TransactionStatus.IN_PROGRESS,
                                  Instant.now(),
                                  Instant.now(),
                                  Instant.now(),
                                  Instant.now(),
                                  false,
                                  null,
                                  null,
                                  null,
                                  null);
  }

  public static String asJsonString(final Object obj) {
    try {
      return OBJECT_MAPPER.writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private RequestPostProcessor testUser() {
    return user(TEST_USER).password(TEST_PASSWORD)
                          .authorities(new SimpleGrantedAuthority(DeedAuthenticationProvider.USER_ROLE_NAME));
  }

}
