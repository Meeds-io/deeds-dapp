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
package io.meeds.deeds.web.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

public class RequestDispatcherFilter extends HttpFilter {

  private static final String       DEFAULT_PAGE_FILE_NAME = "/home";

  private static final long         serialVersionUID       = -4145074746513311839L;

  private static final List<String> STATIC_PATHS           = Arrays.asList("/home",
                                                                           "/whitepaper",
                                                                           "/about-us",
                                                                           "/legals",
                                                                           "/tour");

  private static final List<String> DAPP_PATHS             = Arrays.asList("/marketplace",
                                                                           "/tenants",
                                                                           "/owners",
                                                                           "/portfolio",
                                                                           "/stake",
                                                                           "/deeds",
                                                                           "/farm",
                                                                           "/tokenomics");

  private static final long         LAST_MODIFIED          = System.currentTimeMillis();

  private static final String       VERSION                = String.valueOf(LAST_MODIFIED);

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;
    String servletPath = request.getServletPath();
    if (StringUtils.contains(servletPath, "api")) { // REST API
      chain.doFilter(request, response);
    } else {
      if (servletPath.endsWith("/")) {
        String servletPathCanonical = servletPath.substring(0, servletPath.length() - 1);
        if (DAPP_PATHS.contains(servletPathCanonical) || STATIC_PATHS.contains(servletPathCanonical)) {
          String requestURL = request.getRequestURL().toString();
          if (StringUtils.isNotBlank(request.getHeader("x-forwarded-host"))) {
            requestURL = requestURL.substring(0, requestURL.length() - 1).replace(request.getContextPath(), "");
          } else {
            requestURL = requestURL.substring(0, requestURL.length() - 1);
          }
          response.setHeader("Location", requestURL);
          response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
          return;
        }
      }
      String eTagHeader = request.getHeader("If-None-Match");
      String eTagValue = getETagValue(request);
      if (StringUtils.equals(eTagHeader, eTagValue)) {
        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        return;
      }
      response.setHeader("Cache-Control", "public,must-revalidate");
      response.setHeader("etag", eTagValue);
      response.setDateHeader("Last-Modified", LAST_MODIFIED);
      response.setContentType("text/html; charset=UTF-8");
      if (StringUtils.isBlank(servletPath) || StringUtils.equals(servletPath, "/")) {
        servletPath = DEFAULT_PAGE_FILE_NAME;
      }
      boolean isStaticPath = STATIC_PATHS.contains(servletPath);
      if (isStaticPath || DAPP_PATHS.contains(servletPath)) {
        request.setAttribute("isStaticPath", isStaticPath);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/dapp.jsp");
        dispatcher.include(request, response);// NOSONAR
      } else {
        chain.doFilter(request, response);
      }
    }
  }

  private String getETagValue(HttpServletRequest request) {
    HttpSession session = request.getSession();
    return VERSION + Objects.hash(request.getRemoteUser(), session == null ? null : session.getId());
  }

}
