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

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestDispatcherFilter implements Filter {

  private static final List<String> PATHS = Arrays.asList("/", "/marketplace", "/tenants", "/overview", "/stake", "/deeds", "/farm");

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Nothing to start
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;
    if (PATHS.contains(request.getServletPath())) {
      response.setContentType("text/html; charset=UTF-8");
      response.setCharacterEncoding("UTF-8");
      RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/view.jsp");
      dispatcher.include(request, response);// NOSONAR
    } else {
      chain.doFilter(request, response);
    }
  }

}
