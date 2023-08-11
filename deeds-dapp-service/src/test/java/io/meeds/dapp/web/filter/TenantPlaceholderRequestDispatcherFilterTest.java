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
package io.meeds.dapp.web.filter;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import io.meeds.deeds.common.elasticsearch.model.DeedTenant;
import io.meeds.deeds.common.service.TenantService;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SpringBootTest(classes = { TenantPlaceholderRequestDispatcherFilter.class })
class TenantPlaceholderRequestDispatcherFilterTest {

  @MockBean
  private TenantService                            tenantServiceMock;

  @Mock
  private HttpServletRequest                       request;

  @Mock
  private HttpServletResponse                      response;

  @Mock
  private RequestDispatcher                        dispatcher;

  private TenantPlaceholderRequestDispatcherFilter dispatcherFilter;

  @BeforeEach
  void before() throws ServletException {
    dispatcherFilter = new TenantPlaceholderRequestDispatcherFilter() {
      private static final long serialVersionUID = 4367082767921658504L;

      @Override
      public void init(FilterConfig filterConfig) throws ServletException {
        this.tenantService = tenantServiceMock;
      }
    };
    dispatcherFilter.init(null);

    when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
  }

  @Test
  void testReturnNotFoundResponseWhenNoDeedIdIdentified() throws Exception {
    verifyNotFound();
  }

  @Test
  void testReturnNotFoundResponseWhenNoDeedIdIdentifiedWithSimilarURL() throws Exception {
    when(request.getRequestURL()).thenReturn(new StringBuffer("http://tanit-test.wom.meeds.io/test"));
    verifyNotFound();
  }

  @Test
  void testReturnNotFoundResponseWhenDeedIdInHostNameNotStopped() throws Exception {
    long nftId = 1;
    when(request.getRequestURL()).thenReturn(new StringBuffer("http://tanit-").append(nftId).append(".wom.meeds.io/test"));
    when(tenantServiceMock.isTenantCommandStop(nftId)).thenReturn(true);
    verifyNotFound();
  }

  @Test
  void testReturnNotFoundResponseWhenDeedIdInParamNotStopped() throws Exception {
    long nftId = 1;
    when(request.getRequestURL()).thenReturn(new StringBuffer("http://test/test?nftId=").append(nftId));
    when(tenantServiceMock.isTenantCommandStop(nftId)).thenReturn(true);
    verifyNotFound();
  }

  @Test
  void testForwardResponseWhenDeedIdInHostNameNotStopped() throws Exception {
    long nftId = 1;
    when(request.getRequestURL()).thenReturn(new StringBuffer("http://tanit-").append(nftId).append(".wom.meeds.io/test"));
    verifyForwarded();
  }

  @Test
  void testForwardResponseWhenDeedIdInParamNotStopped() throws Exception {
    long nftId = 1;
    when(request.getRequestURL()).thenReturn(new StringBuffer("http://test/test"));
    when(request.getParameter("nftId")).thenReturn(String.valueOf(nftId));
    verifyForwarded();
  }

  @Test
  void testNotFoundResponseWhenDeedCityInHostNameNotMatching() throws Exception {
    long nftId = 1;
    when(request.getRequestURL()).thenReturn(new StringBuffer("http://tanit-").append(nftId).append(".wom.meeds.io/test"));
    DeedTenant deedTenant = new DeedTenant();
    deedTenant.setCityIndex((short) 1);
    when(tenantServiceMock.getDeedTenant(nftId)).thenReturn(deedTenant);
    verifyNotFound();
  }

  @Test
  void testNotFoundResponseWhenDeedCityInParameterNameNotMatching() throws Exception {
    long nftId = 1;
    when(request.getRequestURL()).thenReturn(new StringBuffer("http://test/test"));
    when(request.getParameter("nftId")).thenReturn(String.valueOf(nftId));
    when(request.getParameter("cityName")).thenReturn("tanit");
    DeedTenant deedTenant = new DeedTenant();
    deedTenant.setCityIndex((short) 1);
    when(tenantServiceMock.getDeedTenant(nftId)).thenReturn(deedTenant);
    verifyNotFound();
  }

  private void verifyForwarded() throws IOException, ServletException {
    dispatcherFilter.doFilter(request, response, null);
    verify(dispatcher, times(1)).include(request, response);
    verify(response, never()).setStatus(anyInt());
  }

  private void verifyNotFound() throws IOException, ServletException {
    dispatcherFilter.doFilter(request, response, null);
    verify(response, times(1)).setStatus(404);
    verify(dispatcher, never()).include(request, response);
  }

}
