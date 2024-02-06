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
package io.meeds.dapp.rest;

import static io.meeds.deeds.common.constant.CommonConstants.CODE_VERIFICATION_HTTP_HEADER;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import io.meeds.dapp.web.rest.AuthorizationCodeController;
import io.meeds.dapp.web.security.DeedAccessDeniedHandler;
import io.meeds.dapp.web.security.DeedAuthenticationProvider;
import io.meeds.dapp.web.security.WebSecurityConfig;
import io.meeds.deeds.common.service.AuthorizationCodeService;

import jakarta.servlet.Filter;

@SpringBootTest(classes = {
                            AuthorizationCodeController.class,
                            DeedAuthenticationProvider.class,
                            DeedAccessDeniedHandler.class,
})
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {
                                  WebSecurityConfig.class
})
class AuthorizationCodeControllerTest {

  private static final String      TEST_PASSWORD = "testPassword";

  private static final String      TEST_USER     = "testUser";

  private static final String      EMAIL         = "email";

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
    String clientIp = "clientIp";
    ResultActions response = mockMvc.perform(post("/api/authorization/generateCode")
                                                                                    .with(remoteAddr(clientIp))
                                                                                    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                                                    .param("email", EMAIL)
                                                                                    .with(csrf()));
    response.andExpect(status().isOk());
  }

  @Test
  void testGenerateCodeWhenNotAuthenticatedAndRemoteAddrNotValid() throws Exception {
    ResultActions response = mockMvc.perform(post("/api/authorization/generateCode")
                                                                                    .with(remoteAddr(""))
                                                                                    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                                                    .param("email", EMAIL)
                                                                                    .with(csrf()));
    response.andExpect(status().isForbidden());
  }

  @Test
  void testGenerateCodeWhenAuthenticated() throws Exception {
    String clientIp = "clientIp";
    ResultActions response = mockMvc.perform(post("/api/authorization/generateCode")
                                                                                    .with(remoteAddr(clientIp))
                                                                                    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                                                    .param("email", EMAIL)
                                                                                    .with(testUser())
                                                                                    .with(csrf()));
    response.andExpect(status().isOk());
    verify(authorizationCodeService, times(1)).generateCode(TEST_USER.toLowerCase(), EMAIL, EMAIL, clientIp);
  }

  @Test
  void testGenerateCodeWhenMaxMailSendingReached() throws Exception {
    String clientIp = "clientIp";
    doThrow(IllegalAccessException.class).when(authorizationCodeService)
                                         .generateCode(TEST_USER.toLowerCase(), EMAIL, EMAIL, clientIp);
    ResultActions response = mockMvc.perform(post("/api/authorization/generateCode")
                                                                                    .with(remoteAddr(clientIp))
                                                                                    .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                                                    .param("email", EMAIL)
                                                                                    .with(testUser())
                                                                                    .with(csrf()));
    response.andExpect(status().isUnauthorized());
  }

  @Test
  void testCheckValidityWhenNotAuthenticatedAndNoCode() throws Exception {
    ResultActions response = mockMvc.perform(post("/api/authorization/checkValidity")
                                                                                     .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                                                     .param("email", EMAIL)
                                                                                     .with(csrf()));
    response.andExpect(status().isBadRequest());
  }

  @Test
  void testCheckValidityWhenNotAuthenticated() throws Exception {
    int code = 56422;
    ResultActions response = mockMvc.perform(post("/api/authorization/checkValidity")
                                                                                     .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                                                     .param("email", EMAIL)
                                                                                     .with(csrf())
                                                                                     .header(CODE_VERIFICATION_HTTP_HEADER,
                                                                                             String.valueOf(code)));
    response.andExpect(status().isOk());
  }

  @Test
  void testCheckValidityWhenNotAuthenticatedAndNoEmail() throws Exception {
    int code = 56422;
    ResultActions response = mockMvc.perform(post("/api/authorization/checkValidity")
                                                                                     .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                                                     .param("email", "")
                                                                                     .with(csrf())
                                                                                     .header(CODE_VERIFICATION_HTTP_HEADER,
                                                                                             String.valueOf(code)));
    response.andExpect(status().isForbidden());
  }

  @Test
  void testCheckValidityWhenAuthenticated() throws Exception {
    String email = "email";
    int code = 56422;
    ResultActions response = mockMvc.perform(post("/api/authorization/checkValidity")
                                                                                     .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                                                     .param("email", email)
                                                                                     .with(testUser())
                                                                                     .with(csrf())
                                                                                     .header(CODE_VERIFICATION_HTTP_HEADER,
                                                                                             String.valueOf(code)));
    response.andExpect(status().isOk());
    verify(authorizationCodeService, times(1)).checkValidity(TEST_USER.toLowerCase(), code);
  }

  @Test
  void testCheckValidityWhenMaxVerificationReached() throws Exception {
    String email = "email";
    int code = 56422;
    doThrow(IllegalAccessException.class).when(authorizationCodeService).checkValidity(TEST_USER.toLowerCase(), code);
    ResultActions response = mockMvc.perform(post("/api/authorization/checkValidity")
                                                                                     .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                                                                                     .param("email", email)
                                                                                     .with(testUser())
                                                                                     .with(csrf())
                                                                                     .header(CODE_VERIFICATION_HTTP_HEADER,
                                                                                             String.valueOf(code)));
    response.andExpect(status().isUnauthorized());
  }

  private RequestPostProcessor testUser() {
    return user(TEST_USER).password(TEST_PASSWORD)
                          .authorities(new SimpleGrantedAuthority(DeedAuthenticationProvider.USER_ROLE_NAME));
  }

  private static RequestPostProcessor remoteAddr(final String remoteAddr) { // it's
                                                                            // nice
                                                                            // to
                                                                            // extract
                                                                            // into
                                                                            // a
                                                                            // helper
    return new RequestPostProcessor() {
      @Override
      public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
        request.setRemoteAddr(remoteAddr);
        return request;
      }
    };
  }

}
