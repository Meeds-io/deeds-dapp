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
package io.meeds.dapp.web.filter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import io.meeds.deeds.constant.DeedCity;
import io.meeds.deeds.elasticsearch.model.DeedTenant;
import io.meeds.deeds.service.TenantService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TenantPlaceholderRequestDispatcherFilter extends HttpFilter {

  private static final long    serialVersionUID    = 8971410534107824567L;

  private static final Pattern SERVER_HOST_PATTERN =
                                                   Pattern.compile("(tanit|reshef|ashtarte|melqart|eshmun|kushor|hammon)-([\\d]+)\\.wom\\.meeds\\.io");

  protected TenantService      tenantService;                                                                                                          // NOSONAR

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(filterConfig.getServletContext());
    this.tenantService = ctx.getBean(TenantService.class);
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    long nftId = getDeedId(request);
    if (nftId > 0 && !tenantService.isTenantCommandStop(nftId) && isDeedCity(request, nftId)) {
      response.setContentType("text/html; charset=UTF-8");
      response.setCharacterEncoding("UTF-8");
      RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/placeholder.jsp");
      dispatcher.include(request, response);
    } else {
      response.setStatus(404);
    }
  }

  private long getDeedId(HttpServletRequest request) {
    String nftId = request.getParameter("nftId");
    if (StringUtils.isBlank(nftId) && StringUtils.isNotBlank(request.getRequestURL())) {
      Matcher matcher = SERVER_HOST_PATTERN.matcher(request.getRequestURL());
      if (matcher.find()) {
        nftId = matcher.group(2);
      }
    }
    if (StringUtils.isNotBlank(nftId)) {
      return Long.parseLong(nftId);
    } else {
      return -1l;
    }
  }

  private boolean isDeedCity(HttpServletRequest request, long nftId) {
    int deedCityIndex = getDeedCityIndex(request);
    if (deedCityIndex >= 0) {
      DeedTenant deedTenant = tenantService.getDeedTenant(nftId);
      return deedTenant == null || deedTenant.getCityIndex() == deedCityIndex;
    } else {
      return true;
    }
  }

  private int getDeedCityIndex(HttpServletRequest request) {
    String cityName = request.getParameter("cityName");
    if (StringUtils.isBlank(cityName)) {
      Matcher matcher = SERVER_HOST_PATTERN.matcher(request.getRequestURL());
      if (matcher.find()) {
        return DeedCity.valueOf(matcher.group(1).toUpperCase()).ordinal();
      } else {
        return -1;
      }
    } else {
      return DeedCity.valueOf(cityName.toUpperCase()).ordinal();
    }
  }

}
