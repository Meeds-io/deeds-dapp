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

import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@EnableWebMvc
public class StaticResourceFilter extends HttpFilter {

  private static final long serialVersionUID = -1870848677869804113L;

  private static final long LAST_MODIFIED    = System.currentTimeMillis();

  private static final long MAX_AGE          = 31536000l;

  private static final long EXPIRES          = LAST_MODIFIED + MAX_AGE * 1000;

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;
    response.setDateHeader("Last-Modified", LAST_MODIFIED);
    response.setDateHeader("Expires", EXPIRES);
    response.setHeader("Cache-Control", "public," + MAX_AGE);
    chain.doFilter(request, response);
  }

}
