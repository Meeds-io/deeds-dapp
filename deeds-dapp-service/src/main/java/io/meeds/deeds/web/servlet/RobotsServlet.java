/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
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
package io.meeds.deeds.web.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.meeds.deeds.web.utils.Utils;

public class RobotsServlet extends HttpServlet {

  private static final long   serialVersionUID = 2720033323076566476L;

  private static final Logger LOG              = LoggerFactory.getLogger(RobotsServlet.class);

  private static String       robotsContent;

  public RobotsServlet() {
    retrieveRobotsFileContent();
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      resp.getWriter().append(robotsContent);
      resp.setHeader("Content-Type", "text/plain");
      resp.setStatus(200);
    } catch (IOException e) {
      LOG.error("Error writing robots content", e);
      resp.setStatus(404);
    }
  }

  private static void retrieveRobotsFileContent() {
    String filePath;
    if (Utils.isProductionEnvironment()) {
      filePath = "/robots_prod_env.txt"; // NOSONAR
    } else {
      filePath = "/robots_test_env.txt"; // NOSONAR
    }
    try {
      try (InputStream fileIs = RobotsServlet.class.getClassLoader().getResourceAsStream(filePath)) {
        robotsContent = IOUtils.toString(fileIs, StandardCharsets.UTF_8);
      }
    } catch (IOException e) {
      LOG.error("Error retrieving file content with path '{}'", filePath, e);
      robotsContent = "";
    }
  }

}
