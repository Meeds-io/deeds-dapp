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
package io.meeds.dapp.web.rest;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.meeds.dapp.constant.Currency;
import io.meeds.dapp.elasticsearch.model.MeedTokenMetric;
import io.meeds.dapp.service.MeedTokenMetricService;

@RestController
@RequestMapping("/api/token/meed")
public class MeedTokenMetricController {

  @Autowired
  private MeedTokenMetricService meedTokenMetricService;

  @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<MeedTokenMetric> getMetrics(
                                                    @RequestParam(name = "currency", required = false)
                                                    Currency currency) {
    return ResponseEntity.ok(meedTokenMetricService.getLastMetric(currency));
  }

  @GetMapping("/circ")
  public ResponseEntity<BigDecimal> getCirculatingSupply() {
    BigDecimal circulatingSupply = meedTokenMetricService.getCirculatingSupply();
    return ResponseEntity.ok()
                         .cacheControl(CacheControl.noStore())
                         .body(circulatingSupply);
  }

  @GetMapping("/mcap")
  public ResponseEntity<BigDecimal> getMarketCapitalization(
                                                            @RequestParam(name = "currency", required = false)
                                                            Currency currency) {
    BigDecimal marketCapitalization = meedTokenMetricService.getMarketCapitalization(currency);
    return ResponseEntity.ok()
                         .cacheControl(CacheControl.noStore())
                         .body(marketCapitalization);
  }

  @GetMapping("/tvl")
  public ResponseEntity<BigDecimal> getTotalLockedValue(
                                                        @RequestParam(name = "currency", required = false)
                                                        Currency currency) {
    BigDecimal totalValueLocked = meedTokenMetricService.getTotalValueLocked(currency);
    return ResponseEntity.ok()
                         .cacheControl(CacheControl.noStore())
                         .body(totalValueLocked);
  }

  @GetMapping("/supply")
  public ResponseEntity<BigDecimal> getTotalSupply() {
    BigDecimal totalSupply = meedTokenMetricService.getTotalSupply();
    return ResponseEntity.ok()
                         .cacheControl(CacheControl.noStore())
                         .body(totalSupply);
  }

}
