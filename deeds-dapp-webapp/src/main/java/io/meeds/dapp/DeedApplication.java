/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.dapp;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

@SpringBootApplication(scanBasePackages = {
    "io.meeds.deeds.common",
    "io.meeds.dapp",
  }, exclude = {
    LiquibaseAutoConfiguration.class,
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@EnableCaching
@EnableSpringDataWebSupport
@EnableElasticsearchRepositories(basePackages = {
  "io.meeds.deeds.common",
  "io.meeds.dapp",
})
@PropertySource("classpath:application.properties")
@PropertySource("classpath:dApp.properties")
public class DeedApplication extends SpringBootServletInitializer {

  @Override
  public void onStartup(ServletContext servletContext) throws ServletException {
    // Used to disable LogBack initialization in WebApp context after having
    // initialized it already in Meeds Server globally
    System.setProperty("org.springframework.boot.logging.LoggingSystem", "none");
    super.onStartup(servletContext);
  }

}
