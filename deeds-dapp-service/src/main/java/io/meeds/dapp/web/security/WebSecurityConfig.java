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
package io.meeds.dapp.web.security;

import java.io.IOException;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.ContentTypeOptionsConfig;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.XXssConfig;
import org.springframework.security.config.annotation.web.configurers.JeeConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.context.ServletContextAware;

import io.meeds.dapp.web.utils.Utils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true,
    securedEnabled = true,
    jsr250Enabled = true)
public class WebSecurityConfig implements ServletContextAware {

  private static final Logger LOG = LoggerFactory.getLogger(WebSecurityConfig.class);

  @Setter
  private ServletContext      servletContext;

  @Bean
  public static GrantedAuthorityDefaults grantedAuthorityDefaults() {
    // Reset prefix to be empty. By default it adds "ROLE_" prefix
    return new GrantedAuthorityDefaults();
  }

  @SuppressWarnings("removal")
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
                                         DeedAuthenticationProvider authProvider,
                                         DeedAccessDeniedHandler deedAccessDeniedHandler) throws Exception {
    http
        .authenticationProvider(authProvider)
        .jee(JeeConfigurer::and) // NOSONAR no method replacement
        .csrf(CsrfConfigurer::disable)
        .headers(headers -> {
          headers.frameOptions(FrameOptionsConfig::disable);
          headers.xssProtection(XXssConfig::disable);
          headers.contentTypeOptions(ContentTypeOptionsConfig::disable);
        })
        .authorizeHttpRequests(customizer -> {
          try {
            customizer.requestMatchers(staticResourcesRequestMatcher())
                      .permitAll()
                      .requestMatchers(apiRequestMatcher())
                      .access(requestAuthorizationManager())
                      .anyRequest()
                      .permitAll();
          } catch (Exception e) {
            LOG.error("Error configuring REST endpoints security manager", e);
          }
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
                                .logoutSuccessHandler((request, response, authentication) -> handleLogout(request, response)))
        .exceptionHandling(exceptionHandling -> exceptionHandling.accessDeniedHandler(deedAccessDeniedHandler));
    return http.build();
  }

  private AuthorizationManager<RequestAuthorizationContext> requestAuthorizationManager() {
    return (Supplier<Authentication> authentication, RequestAuthorizationContext context) -> {
      Authentication userAuthentication = authentication.get();
      // Permit anonymous and authentication users to access
      // the REST endpoints and rely on jee & secured permission
      // management
      return userAuthentication.isAuthenticated() ? new AuthorizationDecision(true) : new AuthorizationDecision(false);
    };
  }

  private RequestMatcher apiRequestMatcher() {
    return request -> StringUtils.startsWith(request.getRequestURI(), servletContext.getContextPath() + "/api/");
  }

  private RequestMatcher staticResourcesRequestMatcher() {
    return request -> !StringUtils.startsWith(request.getRequestURI(), servletContext.getContextPath() + "/api/")
                      || StringUtils.startsWith(request.getRequestURI(), servletContext.getContextPath() + "/api/deeds/")
                      || StringUtils.startsWith(request.getRequestURI(), servletContext.getContextPath() + "/api/hubs/");
  }

  private void handleLogout(HttpServletRequest request, HttpServletResponse response) {
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
