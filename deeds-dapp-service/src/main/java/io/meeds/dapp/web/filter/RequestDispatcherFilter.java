/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.meeds.dapp.web.utils.Utils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class RequestDispatcherFilter extends HttpFilter {

  protected static final String              PREFERRED_LANGUAGE_COOKIE_NAME = "preferred-language";

  protected static final String              DEFAULT_PAGE_FILE_NAME_EN      = "/home";

  protected static final String              DEFAULT_PAGE_FILE_NAME_FR      = "/accueil";

  protected static final long                serialVersionUID               = -4145074746513311839L;

  protected static final List<String>        SUPPORTED_LANGUAGES            = Arrays.asList("en",
                                                                                            "fr");

  protected static final List<String>        STATIC_PATHS_EN                = Arrays.asList("/why-meeds",
                                                                                            "/whitepaper",
                                                                                            "/about-us",
                                                                                            "/legals",
                                                                                            "/tour");

  protected static final List<String>        STATIC_PATHS_FR                = Arrays.asList("/pourquoi-meeds",
                                                                                            "/livre-blanc",
                                                                                            "/qui-sommes-nous",
                                                                                            "/mentions-legales",
                                                                                            "/visite-guidee");

  protected static final List<String>        STATIC_PATHS                   =
                                                          Stream.concat(STATIC_PATHS_EN.stream(), STATIC_PATHS_FR.stream())
                                                                .toList();

  protected static final List<String>        DAPP_PATHS_EN                  = Arrays.asList("/marketplace",
                                                                                            "/tenants",
                                                                                            "/owners",
                                                                                            "/portfolio",
                                                                                            "/stake",
                                                                                            "/home",
                                                                                            "/buy",
                                                                                            "/mint",
                                                                                            "/farm",
                                                                                            "/tokenomics",
                                                                                            "/hubs");

  protected static final List<String>        DAPP_PATHS_FR_UNCOM            = Arrays.asList("/place-de-marche",
                                                                                            "/locataires",
                                                                                            "/proprietaires",
                                                                                            "/portefeuille",
                                                                                            "/rejoindre-dao",
                                                                                            "/accueil",
                                                                                            "/acheter",
                                                                                            "/rejoindre-hubs");

  protected static final List<String>        DAPP_PATHS_FR_COMM             = Arrays.asList("/mint",
                                                                                            "/farm",
                                                                                            "/tokenomics");

  protected static final List<String>        DAPP_PATHS_FR                  =
                                                           Stream.concat(DAPP_PATHS_FR_UNCOM.stream(),
                                                                         DAPP_PATHS_FR_COMM.stream())
                                                                 .toList();

  protected static final List<String>        DAPP_PATHS                     =
                                                        Stream.concat(DAPP_PATHS_EN.stream(), DAPP_PATHS_FR.stream()).toList();

  protected static final List<String>        METADATA_LABELS                = Arrays.asList("pageDescription",
                                                                                            "imageAlt",
                                                                                            "twitterTitle",
                                                                                            "pageTitle");

  protected static final long                LAST_MODIFIED                  = System.currentTimeMillis();

  protected static final String              VERSION                        = String.valueOf(LAST_MODIFIED);

  protected static final Map<String, String> PAGE_METADATAS                 = new HashMap<>();

  private static final Logger                LOG                            = LoggerFactory.getLogger(RequestDispatcherFilter.class);

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException { // NOSONAR
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;
    String servletPath = request.getServletPath();
    if (StringUtils.contains(servletPath, "api")) { // REST API
      chain.doFilter(request, response);
    } else if (StringUtils.contains(servletPath, "/static/") && !StringUtils.startsWith(servletPath, "/static/")) { // STATIC
                                                                                                                    // URI
      response.setHeader("Location", servletPath.substring(servletPath.indexOf("/static/")));
      response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
    } else {
      if (servletPath.endsWith("/")) {
        String servletPathCanonical = servletPath.substring(0, servletPath.length() - 1);
        if (DAPP_PATHS.contains(servletPathCanonical) || STATIC_PATHS.contains(servletPathCanonical)) {
          response.setHeader("Location", servletPathCanonical);
          response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
          return;
        }
      } else if (servletPath.equals("/farm")
          || servletPath.equals("/tokenomics")
          || servletPath.equals("/portfolio")
          || servletPath.equals("/fr/tokenomics")
          || servletPath.equals("/fr/portefeuille")) {
        response.setHeader("Location", "/");
        response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        return;
      } else if (servletPath.equals("/home")) {
        response.setHeader("Location", "/");
        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        return;
      } else if (servletPath.equals("/fr/accueil")) {
        response.setHeader("Location", "/fr");
        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        return;
      } else if(servletPath.equals("/deeds")) {
        response.setHeader("Location", "/mint");
        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        return;
      }
      String eTagHeader = request.getHeader("If-None-Match");
      String eTagValue = getETagValue(request);
      if (StringUtils.equals(eTagHeader, eTagValue)) {
        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        return;
      } else if (StringUtils.isBlank(servletPath) || StringUtils.equals(servletPath, "/")) {
        servletPath = DEFAULT_PAGE_FILE_NAME_EN;
      } else if (StringUtils.equals(servletPath, "/fr")) {
        servletPath = servletPath + DEFAULT_PAGE_FILE_NAME_FR;
      }
      String uri = servletPath;
      if (uri.startsWith("/fr/")) {
        uri = uri.substring(3, uri.length());
      } else if (DAPP_PATHS_FR_UNCOM.contains(uri) || STATIC_PATHS_FR.contains(uri)) {
        uri = "/fr" + uri;
        response.setHeader("Location", uri);
        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        return;
      }
      boolean isStaticPath = STATIC_PATHS.contains(uri);
      if (isStaticPath || DAPP_PATHS.contains(uri)) {
        String lang = getLanguage(request);

        response.setContentType("text/html; charset=UTF-8");
        response.setDateHeader("Last-Modified", LAST_MODIFIED);
        response.setHeader("Cache-Control", "public,must-revalidate");
        response.setHeader("etag", eTagValue);
        request.setAttribute("isStaticPath", isStaticPath);
        request.setAttribute("lang", lang);

        String pageName = getNormalizedPageName(servletPath, uri);
        request.setAttribute("pageName", pageName.substring(1));
        buildPageMetadata(request, servletPath, pageName, lang);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/dapp.jsp");
        dispatcher.include(request, response);// NOSONAR
      } else {
        doFilter(chain, request, response);
      }
    }
  }

  private void doFilter(FilterChain chain, HttpServletRequest request, HttpServletResponse response) {
    try {
      chain.doFilter(request, response);
    } catch (Exception e) {
      if (LOG.isDebugEnabled()) {
      } else {
        LOG.warn(e.getMessage());
      }
      if (!response.isCommitted()) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      }
    }
  }

  private void buildPageMetadata(HttpServletRequest request, String servletPath, String defaultPageName, String lang) throws IOException {
    String pageContent = getPageHeaderMetadataContent(request, servletPath, defaultPageName, lang);
    if (Utils.isProductionEnvironment()) {
      request.setAttribute("pageHeaderMetadatas", pageContent);
    } else {
      request.setAttribute("pageHeaderMetadatas", "");
    }
  }

  private String getPageHeaderMetadataContent(HttpServletRequest request,
                                              String servletPath,
                                              String defaultPageName,
                                              String lang) throws IOException {
    String pageName = defaultPageName.substring(1);

    String key = pageName + "_" + lang;
    String pageContent = PAGE_METADATAS.get(key);

    if (StringUtils.isBlank(pageContent)) {
      ResourceBundle resourceBundle = null;
      try (InputStream is = request.getServletContext().getResourceAsStream("/static/i18n/messages_" + lang + ".properties")) {
        resourceBundle = new PropertyResourceBundle(is);
      }

      try (InputStream is = request.getServletContext().getResourceAsStream("/WEB-INF/metadata" + defaultPageName + ".html")) {
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

  private String getNormalizedPageName(String servletPath, String uri) {
    if (DAPP_PATHS_FR.contains(uri) && servletPath.startsWith("/fr/")) {
      return DAPP_PATHS_EN.get(DAPP_PATHS_FR.indexOf(uri));
    } else if (STATIC_PATHS_FR.contains(uri) && servletPath.startsWith("/fr/")) {
      return STATIC_PATHS_EN.get(STATIC_PATHS_FR.indexOf(uri));
    }
    return STATIC_PATHS_EN.contains(uri) || DAPP_PATHS_EN.contains(uri) ? uri : "/home";
  }

  private String getLanguage(HttpServletRequest request) {
    String path = request.getServletPath();
    return (StringUtils.equals(path, "/fr") || path.startsWith("/fr/")) ? "fr" : "en";
  }

  private String getETagValue(HttpServletRequest request) {
    HttpSession session = request.getSession();
    return VERSION + Objects.hash(request.getRemoteUser(), session == null ? null : session.getId());
  }

}
