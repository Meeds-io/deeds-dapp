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
package io.meeds.deeds.web.utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import jakarta.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

public class Utils {

  private static final String HOST_ENVIRONMENT_PARAM           = "meeds.deed.hostEnvironment";

  private static final String HOST_ENVIRONMENT_PRODUCTION      = "production";

  private static final String HOST_ENVIRONMENT_TEST            = "test";

  private static final String EXTENDED_HTML_CONTENT_PATH_PARAM = "meeds.deed.extendedHtmlContent.path";

  private static final String APPLICATION_BUILD_NUMBER_PARAM   = "application.buildNumber";

  private static final String LOGIN_MESSAGE_ATTRIBUTE_NAME     = "login_message";

  private static final Random RANDOM                           = new Random();

  private static String       buildNumber;

  private static String       hostEnvironment;

  private Utils() {
    // Utility class
  }

  public static String generateLoginMessage(HttpSession session) {
    String token = getLoginMessage(session);
    if (token == null) {
      token = RANDOM.nextLong() + "-" + RANDOM.nextLong() + "-" + RANDOM.nextLong();
      session.setAttribute(LOGIN_MESSAGE_ATTRIBUTE_NAME, token); // NOSONAR
    }
    return token;
  }

  public static String getLoginMessage(HttpSession session) {
    return session == null ? null : (String) session.getAttribute(LOGIN_MESSAGE_ATTRIBUTE_NAME);
  }

  public static String getApplicationBuildNumber() {
    if (StringUtils.isBlank(buildNumber)) {
      buildNumber = EnvironmentService.getEnvironment().getProperty(APPLICATION_BUILD_NUMBER_PARAM, "");
    }
    return buildNumber;
  }

  public static boolean isProductionEnvironment() {
    if (hostEnvironment == null) {
      hostEnvironment = EnvironmentService.getEnvironment().getProperty(HOST_ENVIRONMENT_PARAM, "");
      if (StringUtils.isBlank(hostEnvironment)) {
        String extFilePath = EnvironmentService.getEnvironment().getProperty(EXTENDED_HTML_CONTENT_PATH_PARAM, "");
        if (StringUtils.isNotBlank(extFilePath) && Files.exists(Paths.get(extFilePath))) {
          hostEnvironment = HOST_ENVIRONMENT_PRODUCTION;
        } else {
          hostEnvironment = HOST_ENVIRONMENT_TEST;
        }
      }
    }
    return StringUtils.equals(hostEnvironment, HOST_ENVIRONMENT_PRODUCTION);
  }

}
