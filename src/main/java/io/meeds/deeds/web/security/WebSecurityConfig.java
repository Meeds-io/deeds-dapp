/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.meeds.deeds.web.security;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private CookieCsrfTokenRepository  csrfTokenRepository = new CookieCsrfTokenRepository();

  @Autowired
  private DeedAuthenticationProvider authProvider;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> {
          csrfTokenRepository.setCookiePath("/");
          csrfTokenRepository.setCookieHttpOnly(false);
          csrf.csrfTokenRepository(csrfTokenRepository);
        })
        .headers(headers -> headers.referrerPolicy(referrerPolicy -> referrerPolicy.policy(ReferrerPolicy.SAME_ORIGIN)))
        .formLogin(formLogin -> formLogin
                                         .loginProcessingUrl("/login")
                                         .successHandler((request,
                                                          response,
                                                          authentication) -> response.setStatus(HttpServletResponse.SC_OK)))
        .logout(logout -> logout
                                .logoutUrl("/logout")
                                .logoutSuccessHandler((request, response, authentication) -> {
                                  // Regenerate CSRF Token for anonymous user to
                                  // allow login again
                                  // Using a valid CSRF Token
                                  CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
                                  csrfTokenRepository.saveToken(csrfToken, request, response);
                                  // Disable logout redirection
                                  response.setStatus(HttpServletResponse.SC_OK);
                                }));
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.authenticationProvider(authProvider);
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers("/static/**");
  }

}
