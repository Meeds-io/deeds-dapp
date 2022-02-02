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
package io.meeds.deeds;

import javax.servlet.ServletContext;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.context.ServletContextAware;

import io.meeds.deeds.task.CurrencyExchangeTask;
import io.meeds.deeds.task.MeedsExchangeTask;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableWebSecurity
public class DeedApplication extends SpringBootServletInitializer implements ServletContextAware {

  private ServletContext servletContext;

  @Bean
  public CurrencyExchangeTask currencyExchangeTask() {
    return new CurrencyExchangeTask(servletContext);
  }

  @Bean
  public MeedsExchangeTask meedsExchangeTask() {
    return new MeedsExchangeTask(servletContext);
  }

  @Override
  public void setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

}
