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
package io.meeds.deeds.web.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;

import io.meeds.deeds.web.utils.Utils;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

  private static final Logger       LOG                 = LoggerFactory.getLogger(WebSecurityConfig.class);

  private CookieCsrfTokenRepository csrfTokenRepository = new CookieCsrfTokenRepository();

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, DeedAuthenticationProvider authProvider) throws Exception {
    http
        .authorizeRequests(authorizeRequests -> authorizeRequests.antMatchers("/static/**", "/api/deeds/**").permitAll())
        .authenticationProvider(authProvider)
        .csrf(csrf -> {
          csrfTokenRepository.setCookiePath("/");
          csrfTokenRepository.setCookieHttpOnly(false);
          csrf.csrfTokenRepository(csrfTokenRepository);
          csrf.ignoringAntMatchers("/static/**", "/api/deeds/**");
        })
        .headers(headers -> {
          headers.frameOptions().disable();
          headers.xssProtection().disable();
          headers.contentTypeOptions().disable();
        })
        .formLogin(formLogin -> formLogin
                                         .loginProcessingUrl("/login")
                                         .successHandler((request,
                                                          response,
                                                          authentication) -> response.setStatus(HttpServletResponse.SC_OK))
                                         .failureHandler((request,
                                                          response,
                                                          authentication) -> response.setStatus(HttpServletResponse.SC_FORBIDDEN)))
        .logout(logout -> logout
                                .logoutUrl("/logout")
                                .logoutSuccessHandler((request, response, authentication) -> handleLogout(request, response)));
    return http.build();
  }

  private void handleLogout(HttpServletRequest request, HttpServletResponse response) {
    // Regenerate CSRF Token for anonymous user to
    // allow login again using a valid CSRF Token
    CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
    csrfTokenRepository.saveToken(csrfToken, request, response);
    // Disable logout redirection
    String loginMessage = Utils.generateLoginMessage(request.getSession(true));
    try {
      response.setContentType("text/plain; charset=UTF-8");
      response.getWriter().append(loginMessage);
      response.getWriter().close();
      response.setStatus(HttpServletResponse.SC_OK);
    } catch (IOException e) {
      LOG.warn("Error while generating Login Password for user to sign by his wallet", e);
    }
  }

}
