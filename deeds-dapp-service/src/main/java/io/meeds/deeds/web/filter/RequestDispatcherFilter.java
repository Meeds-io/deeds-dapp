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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.util.CollectionUtils;

import io.meeds.deeds.web.utils.Utils;

public class RequestDispatcherFilter extends HttpFilter {

  protected static final String              PREFERRED_LANGUAGE_COOKIE_NAME = "preferred-language";

  protected static final String              DEFAULT_PAGE_FILE_NAME         = "/home";

  protected static final long                serialVersionUID               = -4145074746513311839L;

  protected static final List<String>        SUPPORTED_LANGUAGES            = Arrays.asList("en",
                                                                                            "fr");

  protected static final List<String>        STATIC_PATHS_EN                = Arrays.asList("/home",
                                                                                            "/whitepaper",
                                                                                            "/about-us",
                                                                                            "/legals",
                                                                                            "/tour");

  protected static final List<String>        STATIC_PATHS_FR                = Arrays.asList("/accueil",
                                                                                            "/livre-blanc",
                                                                                            "/qui-sommes-nous",
                                                                                            "/mentions-legales",
                                                                                            "/visite-guidee");

  protected static final List<String>        STATIC_PATHS                   = CollectionUtils.concatLists(STATIC_PATHS_EN, STATIC_PATHS_FR);

  protected static final List<String>        DAPP_PATHS_EN                  = Arrays.asList("/marketplace",
                                                                                            "/tenants",
                                                                                            "/owners",
                                                                                            "/portfolio",
                                                                                            "/stake",
                                                                                            "/deeds",
                                                                                            "/farm",
                                                                                            "/tokenomics");

  protected static final List<String>        DAPP_PATHS_FR                  = Arrays.asList("/place-de-marche",
                                                                                            "/locataires",
                                                                                            "/proprietaires",
                                                                                            "/portefeuille",
                                                                                            "/rejoindre-dao",
                                                                                            "/deeds-fr",
                                                                                            "/farm-fr",
                                                                                            "/tokenomics-fr");

  protected static final List<String>        DAPP_PATHS                   = CollectionUtils.concatLists(DAPP_PATHS_EN, DAPP_PATHS_FR);

  protected static final List<String>        METADATA_LABELS                = Arrays.asList("pageDescription",
                                                                                            "imageAlt",
                                                                                            "twitterTitle",
                                                                                            "pageTitle");

  protected static final long                LAST_MODIFIED                  = System.currentTimeMillis();

  protected static final String              VERSION                        = String.valueOf(LAST_MODIFIED);

  protected static final Map<String, String> PAGE_METADATAS                 = new HashMap<>();

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException { // NOSONAR
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;
    String servletPath = request.getServletPath();
    if (StringUtils.contains(servletPath, "api")) { // REST API
      chain.doFilter(request, response);
    } else {
      if (servletPath.endsWith("/")) {
        String servletPathCanonical = servletPath.substring(0, servletPath.length() - 1);
        if (DAPP_PATHS.contains(servletPathCanonical) || STATIC_PATHS.contains(servletPathCanonical)) {
          response.setHeader("Location", servletPathCanonical);
          response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
          return;
        }
      }
      String eTagHeader = request.getHeader("If-None-Match");
      String eTagValue = getETagValue(request);
      if (StringUtils.equals(eTagHeader, eTagValue)) {
        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        return;
      } else if (StringUtils.isBlank(servletPath) || StringUtils.equals(servletPath, "/")) {
        servletPath = DEFAULT_PAGE_FILE_NAME;
      }
      boolean isStaticPath = STATIC_PATHS.contains(servletPath);
      if (isStaticPath || DAPP_PATHS.contains(servletPath)) {
        response.setContentType("text/html; charset=UTF-8");
        response.setDateHeader("Last-Modified", LAST_MODIFIED);
        response.setHeader("Cache-Control", "public,must-revalidate");
        response.setHeader("etag", eTagValue);
        request.setAttribute("isStaticPath", isStaticPath);
        request.setAttribute("servletPath", servletPath);
        buildPageMetadata(request, servletPath);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/dapp.jsp");
        dispatcher.include(request, response);// NOSONAR
      } else {
        chain.doFilter(request, response);
      }
    }
  }

  private void buildPageMetadata(HttpServletRequest request, String servletPath) throws IOException {
    if (Utils.isProductionEnvironment()) {
      String pageName_EN = servletPath;
      if (DAPP_PATHS_FR.contains(servletPath)) {
        pageName_EN = DAPP_PATHS_EN.get(DAPP_PATHS_FR.indexOf(servletPath));
      } else if (STATIC_PATHS_FR.contains(servletPath)) {
        pageName_EN = STATIC_PATHS_EN.get(STATIC_PATHS_FR.indexOf(servletPath));
      }
      String pageContent = getPageHeaderMetadataContent(request, servletPath, pageName_EN);
      request.setAttribute("pageHeaderMetadatas", pageContent);
    } else {
      request.setAttribute("pageHeaderMetadatas", "");
    }
  }

  private String getPageHeaderMetadataContent(HttpServletRequest request, String servletPath, String pageName_EN) throws IOException {
    String lang = getLanguage(request);
    String pageName = pageName_EN.substring(1);

    String key = pageName + "_" + lang;
    String pageContent = PAGE_METADATAS.get(key);

    request.setAttribute("lang", lang);
    if (StringUtils.isBlank(pageContent)) {
      ResourceBundle resourceBundle = null;
      try (InputStream is = request.getServletContext().getResourceAsStream("/static/i18n/messages_" + lang + ".properties")) {
        resourceBundle = new PropertyResourceBundle(is);
      }

      try (InputStream is = request.getServletContext().getResourceAsStream("/WEB-INF/metadata" + pageName_EN + ".html")) {
        pageContent = IOUtils.toString(is, StandardCharsets.UTF_8);
        for (String label : METADATA_LABELS) {
          String i18nKey = "metadata." + pageName + "." + label;
          pageContent = pageContent.replace("#{" + i18nKey + "}", resourceBundle.getString(i18nKey));
        }
        pageContent = pageContent.replace("${lang}", lang);
        pageContent = pageContent.replace("${servletPath}", servletPath);
      }

      PAGE_METADATAS.put(key, pageContent);
    }
    return pageContent;
  }

  private String getLanguage(HttpServletRequest request) {
    String path = request.getServletPath();
    return DAPP_PATHS_FR.contains(path) || STATIC_PATHS_FR.contains(path) ? "fr" : "en";
  }

  private String getETagValue(HttpServletRequest request) {
    HttpSession session = request.getSession();
    return VERSION + Objects.hash(request.getRemoteUser(), session == null ? null : session.getId());
  }

}
