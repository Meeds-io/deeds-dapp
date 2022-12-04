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

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import io.meeds.deeds.constant.TenantProvisioningStatus;
import io.meeds.deeds.constant.TenantStatus;
import io.meeds.deeds.elasticsearch.model.DeedTenant;
import io.meeds.deeds.service.TenantService;
import io.meeds.deeds.web.rest.TenantController;
import io.meeds.deeds.web.security.DeedAuthenticationProvider;
import io.meeds.deeds.web.security.WebSecurityConfig;

@SpringBootTest(classes = {
    TenantController.class, DeedAuthenticationProvider.class, WebSecurityConfig.class
})
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
class TenantControllerTest {

  private static final long     NFT_ID   = 3;

  private static final String   USERNAME = "testuser";

  @MockBean
  private TenantService         tenantService;

  @Autowired
  private WebApplicationContext context;

  private MockMvc               mockMvc;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  @WithAnonymousUser
  void whenAnonymousGetDeedTenant_thenNOk() throws Exception {
    mockMvc.perform(get("/api/tenants/" + NFT_ID)).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(username = USERNAME, roles = DeedAuthenticationProvider.USER_ROLE_NAME)
  void whenAuthenticatedGetDeedTenant_thenNOk() throws Exception {
    when(tenantService.getDeedTenantOrImport(USERNAME, NFT_ID, false)).thenReturn(new DeedTenant());
    mockMvc.perform(get("/api/tenants/" + NFT_ID)).andExpect(status().isOk());
    mockMvc.perform(get("/api/tenants/" + (NFT_ID + 1))).andExpect(status().isNotFound());
  }

  @Test
  @WithAnonymousUser
  void whenAnonymousGetDeedTenantStartTile_thenNOk() throws Exception {
    mockMvc.perform(get("/api/tenants/" + NFT_ID + "/startDate"))
           .andExpect(status().isOk())
           .andExpect(content().string(""));

    DeedTenant deedTenant = new DeedTenant();
    deedTenant.setTenantStatus(TenantStatus.DEPLOYED);
    deedTenant.setTenantProvisioningStatus(TenantProvisioningStatus.START_CONFIRMED);
    LocalDateTime date = LocalDateTime.now(ZoneOffset.UTC);
    deedTenant.setDate(date);

    when(tenantService.getDeedTenant(NFT_ID)).thenReturn(deedTenant);

    mockMvc.perform(get("/api/tenants/" + NFT_ID + "/startDate"))
           .andExpect(status().isOk())
           .andExpect(content().string(containsString(String.valueOf(date.toEpochSecond(ZoneOffset.UTC)))));

    deedTenant.setTenantStatus(TenantStatus.UNDEPLOYED);
    mockMvc.perform(get("/api/tenants/" + NFT_ID + "/startDate"))
           .andExpect(status().isOk())
           .andExpect(content().string(""));

    deedTenant.setTenantStatus(TenantStatus.DEPLOYED);
    deedTenant.setTenantProvisioningStatus(TenantProvisioningStatus.STOP_CONFIRMED);
    mockMvc.perform(get("/api/tenants/" + NFT_ID + "/startDate"))
           .andExpect(status().isOk())
           .andExpect(content().string(""));
  }

}
