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

import static io.meeds.deeds.constant.CommonConstants.CODE_VERIFICATION_HTTP_HEADER;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.servlet.Filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import io.meeds.deeds.service.AuthorizationCodeService;
import io.meeds.deeds.web.rest.AuthorizationCodeController;
import io.meeds.deeds.web.security.DeedAuthenticationProvider;
import io.meeds.deeds.web.security.WebSecurityConfig;

@SpringBootTest(classes = {
    AuthorizationCodeController.class,
    DeedAuthenticationProvider.class
})
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {
    WebSecurityConfig.class
})
class AuthorizationCodeControllerTest {

  private static final String      TEST_PASSWORD = "testPassword";

  private static final String      TEST_USER     = "testUser";

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
  void testGenerateCodeWhenNotAuthenticated() throws Exception {
    String email = "email";
    ResultActions response = mockMvc.perform(post("/api/authorization")
                                                                       .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                                       .param("email", email)
                                                                       .with(csrf()));
    response.andExpect(status().isForbidden());
  }

  @Test
  void testGenerateCodeWhenAuthenticated() throws Exception {
    String email = "email";
    ResultActions response = mockMvc.perform(post("/api/authorization")
                                                                       .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                                       .param("email", email)
                                                                       .with(testUser())
                                                                       .with(csrf()));
    response.andExpect(status().isOk());
    verify(authorizationCodeService, times(1)).generateCode(TEST_USER.toLowerCase(), email, email);
  }

  @Test
  void testGenerateCodeWhenMaxMailSendingReached() throws Exception {
    String email = "email";
    doThrow(IllegalAccessException.class).when(authorizationCodeService).generateCode(TEST_USER.toLowerCase(), email, email);
    ResultActions response = mockMvc.perform(post("/api/authorization")
                                                                       .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                                       .param("email", email)
                                                                       .with(testUser())
                                                                       .with(csrf()));
    response.andExpect(status().isUnauthorized());
  }

  @Test
  void testCheckValidityWhenNotAuthenticatedAndNoCode() throws Exception {
    ResultActions response = mockMvc.perform(get("/api/authorization").with(csrf()));
    response.andExpect(status().isBadRequest());
  }

  @Test
  void testCheckValidityWhenNotAuthenticated() throws Exception {
    int code = 56422;
    ResultActions response = mockMvc.perform(get("/api/authorization").with(csrf())
                                                                      .header(CODE_VERIFICATION_HTTP_HEADER,
                                                                              String.valueOf(code)));
    response.andExpect(status().isForbidden());
  }

  @Test
  void testCheckValidityWhenAuthenticated() throws Exception {
    int code = 56422;
    ResultActions response = mockMvc.perform(get("/api/authorization")
                                                                      .with(testUser())
                                                                      .with(csrf())
                                                                      .header(CODE_VERIFICATION_HTTP_HEADER,
                                                                              String.valueOf(code)));
    response.andExpect(status().isNoContent());
    verify(authorizationCodeService, times(1)).checkValidity(TEST_USER.toLowerCase(), code);
  }

  @Test
  void testCheckValidityWhenMaxVerificationReached() throws Exception {
    int code = 56422;
    doThrow(IllegalAccessException.class).when(authorizationCodeService).checkValidity(TEST_USER.toLowerCase(), code);
    ResultActions response = mockMvc.perform(get("/api/authorization")
                                                                      .with(testUser())
                                                                      .with(csrf())
                                                                      .header(CODE_VERIFICATION_HTTP_HEADER,
                                                                              String.valueOf(code)));
    response.andExpect(status().isUnauthorized());
  }

  private RequestPostProcessor testUser() {
    return user(TEST_USER).password(TEST_PASSWORD).roles(DeedAuthenticationProvider.USER_ROLE_NAME);
  }

}
