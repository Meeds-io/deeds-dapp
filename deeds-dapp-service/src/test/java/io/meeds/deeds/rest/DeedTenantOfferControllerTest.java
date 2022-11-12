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

import static io.meeds.deeds.redis.model.EventSerialization.OBJECT_MAPPER;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
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
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import io.meeds.deeds.constant.DeedCard;
import io.meeds.deeds.constant.DeedCity;
import io.meeds.deeds.constant.ExpirationDuration;
import io.meeds.deeds.constant.OfferType;
import io.meeds.deeds.constant.RentalDuration;
import io.meeds.deeds.constant.RentalPaymentPeriodicity;
import io.meeds.deeds.model.DeedTenantOfferDTO;
import io.meeds.deeds.service.DeedTenantOfferService;
import io.meeds.deeds.web.rest.DeedTenantOfferController;

@SpringBootTest(classes = {
    DeedTenantOfferController.class
})
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
class DeedTenantOfferControllerTest {

  @MockBean
  private DeedTenantOfferService deedTenantOfferService;

  @Autowired
  private WebApplicationContext  context;

  private MockMvc                mockMvc;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .build();
  }

  @Test
  void testGetOffersByNftId() throws Exception {
    long nftId = 2l;
    DeedTenantOfferDTO deedTenantOfferDTO = getTenantOfferDTO(nftId);

    when(deedTenantOfferService.getOffersList(eq(nftId), any(Pageable.class))).thenAnswer(invocation -> {
      Pageable pageable = invocation.getArgument(1, Pageable.class);
      return new PageImpl<>(Collections.singletonList(deedTenantOfferDTO), pageable, 1);
    });

    ResultActions response = mockMvc.perform(get("/api/offers?nftId=" + nftId));
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded", notNullValue()))
            .andExpect(jsonPath("$._embedded.deedTenantOfferDTOList", notNullValue()))
            .andExpect(jsonPath("$.page", notNullValue()))
            .andExpect(jsonPath("$.page.totalElements", is(1)));
  }

  @Test
  void testGetOffersByOfferTypesAndCardTypes() throws Exception {
    long nftId = 2l;
    DeedTenantOfferDTO deedTenantOfferDTO = getTenantOfferDTO(nftId);
    List<DeedCard> cardTypes = Arrays.asList(DeedCard.COMMON, DeedCard.UNCOMMON);
    List<OfferType> offerTypes = Arrays.asList(OfferType.RENTING, OfferType.SALE);

    when(deedTenantOfferService.getOffersList(eq(cardTypes), eq(offerTypes), any(Pageable.class))).thenAnswer(invocation -> {
      Pageable pageable = invocation.getArgument(2, Pageable.class);
      return new PageImpl<>(Collections.singletonList(deedTenantOfferDTO), pageable, 1);
    });

    String link = "/api/offers?";
    link += StringUtils.join(cardTypes.stream().map(cardType -> "cardType=" + cardType.name()).collect(Collectors.toList()), '&');
    link += "&";
    link += StringUtils.join(offerTypes.stream().map(offerType -> "offerType=" + offerType.name()).collect(Collectors.toList()),
                             '&');
    ResultActions response = mockMvc.perform(get(link));
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded", notNullValue()))
            .andExpect(jsonPath("$._embedded.deedTenantOfferDTOList", notNullValue()))
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
    long offerId = 2l;
    long nftId = 3l;
    DeedTenantOfferDTO deedTenantOfferDTO = getTenantOfferDTO(nftId);
    when(deedTenantOfferService.getOffer(offerId)).thenReturn(deedTenantOfferDTO);

    ResultActions response = mockMvc.perform(get("/api/offers/" + offerId));
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$", notNullValue()));
  }

  @Test
  void testCreateOfferWithAnonymousUser() throws Exception {
    long nftId = 3l;
    DeedTenantOfferDTO deedTenantOfferDTO = getTenantOfferDTO(nftId);
    ResultActions response = mockMvc.perform(post("/api/offers/").content(asJsonString(deedTenantOfferDTO))
                                                                 .contentType(MediaType.APPLICATION_JSON)
                                                                 .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isForbidden());
  }

  @Test
  @WithAnonymousUser
  void testUpdateOfferWithAnonymousUser() throws Exception {
    long offerId = 2l;
    long nftId = 3l;
    DeedTenantOfferDTO deedTenantOfferDTO = getTenantOfferDTO(nftId);
    ResultActions response = mockMvc.perform(put("/api/offers/" + offerId).content(asJsonString(deedTenantOfferDTO))
                                                                          .contentType(MediaType.APPLICATION_JSON)
                                                                          .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isForbidden());
  }

  @Test
  @WithAnonymousUser
  void testDeleteOfferWithAnonymousUser() throws Exception {
    long offerId = 2l;
    ResultActions response = mockMvc.perform(delete("/api/offers/" + offerId));
    response.andExpect(status().isForbidden());
  }

  private DeedTenantOfferDTO getTenantOfferDTO(long nftId) {
    return new DeedTenantOfferDTO(1l,
                                  nftId,
                                  DeedCity.ASHTARTE,
                                  DeedCard.UNCOMMON,
                                  "owner",
                                  "description",
                                  5d,
                                  OfferType.RENTING,
                                  ExpirationDuration.ONE_DAY,
                                  RentalDuration.ONE_MONTH,
                                  RentalPaymentPeriodicity.ONE_YEAR,
                                  1,
                                  1.1d,
                                  Instant.now(),
                                  Instant.now(),
                                  Instant.now(),
                                  false);
  }

  public static String asJsonString(final Object obj) {
    try {
      return OBJECT_MAPPER.writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
