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
package io.meeds.dapp.scheduling.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.meeds.dapp.service.ExchangeService;

@Component
public class CurrencyExchangeTask {

  private static final Logger LOG = LoggerFactory.getLogger(CurrencyExchangeTask.class);

  @Autowired
  private ExchangeService     exchangeService;

  @Scheduled(cron = "${meeds.currency.exchangeRate.cron:0 0 0/12 * * *}")
  public void computeExchangeRate() {
    LOG.info("Start Computing EURO exchange rate");
    long start = System.currentTimeMillis();
    try {
      exchangeService.computeTodayCurrencyExchangeRate();
      LOG.info("End Computing EURO exchange rate in {}ms", System.currentTimeMillis() - start);
    } catch (Exception e) {
      LOG.error("An error occurred while computing EURO exchange rates", e);
    }
  }

}
