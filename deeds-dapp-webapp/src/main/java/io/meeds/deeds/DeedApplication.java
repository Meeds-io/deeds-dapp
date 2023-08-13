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
package io.meeds.deeds;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication(exclude = RedisAutoConfiguration.class)
@EnableCaching
@EnableSpringDataWebSupport
@PropertySource("classpath:dApp.properties")
@PropertySource("classpath:application.properties")
public class DeedApplication extends SpringBootServletInitializer {

  @Override
  public void onStartup(ServletContext servletContext) throws ServletException {
    // Used to disable LogBack initialization in WebApp context after having
    // initialized it already in Meeds Server globally
    System.setProperty("org.springframework.boot.logging.LoggingSystem", "none");
    // Allow creating Deed Tenant Indexes
    System.setProperty("meeds.elasticsearch.autoCreateIndex", "true");
    super.onStartup(servletContext);
  }

}
