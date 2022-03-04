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
package io.meeds.deeds.web.rest;

import java.time.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.deeds.constant.Currency;
import io.meeds.deeds.model.MeedPrice;
import io.meeds.deeds.service.ExchangeService;

@RestController
@RequestMapping("/api/exchange")
public class ExchangeController {

  @Autowired
  private ExchangeService exchangeService;

  @GetMapping("/{currency}")
  public ResponseEntity<List<MeedPrice>> getExchangeRates(
                                                          @PathVariable(name = "currency")
                                                          Currency currency,
                                                          @RequestParam(name = "from", required = true)
                                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                          LocalDate fromDate) {
    LocalDate today = LocalDate.now(ZoneOffset.UTC);
    if (fromDate.isAfter(today)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "From date must be before today");
    }
    List<MeedPrice> exchangeRates = exchangeService.getExchangeRates(currency, fromDate, today);
    return ResponseEntity.ok()
                         .cacheControl(CacheControl.maxAge(15, TimeUnit.MINUTES).cachePublic())
                         .lastModified(ZonedDateTime.now())
                         .body(exchangeRates);
  }

}
