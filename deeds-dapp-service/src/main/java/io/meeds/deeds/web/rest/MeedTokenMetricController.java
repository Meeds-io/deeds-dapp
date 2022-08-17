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
package io.meeds.deeds.web.rest;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.meeds.deeds.model.MeedTokenMetric;
import io.meeds.deeds.service.MeedTokenMetricService;

@RestController
@RequestMapping("/api/token/meed")
public class MeedTokenMetricController {

  @Autowired
  private MeedTokenMetricService meedTokenMetricService;

  @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<MeedTokenMetric> getMetrics() {
    return ResponseEntity.ok(meedTokenMetricService.getLastMetric());
  }

  @GetMapping("/circ")
  public ResponseEntity<BigDecimal> getCirculatingSupply() {
    BigDecimal circulatingSupply = meedTokenMetricService.getCirculatingSupply();
    return ResponseEntity.ok()
                         .cacheControl(CacheControl.noCache().cachePublic())
                         .body(circulatingSupply);
  }

  @GetMapping("/mcap")
  public ResponseEntity<BigDecimal> getMarketCapitalization() {
    BigDecimal marketCapitalization = meedTokenMetricService.getMarketCapitalization();
    return ResponseEntity.ok()
                         .cacheControl(CacheControl.noCache().cachePublic())
                         .body(marketCapitalization);
  }

  @GetMapping("/tvl")
  public ResponseEntity<BigDecimal> getTotalLockedValue() {
    BigDecimal totalValueLocked = meedTokenMetricService.getTotalValueLocked();
    return ResponseEntity.ok()
                         .cacheControl(CacheControl.noCache().cachePublic())
                         .body(totalValueLocked);
  }

  @GetMapping("/supply")
  public ResponseEntity<BigDecimal> getTotalSupply() {
    BigDecimal totalSupply = meedTokenMetricService.getTotalSupply();
    return ResponseEntity.ok()
                         .cacheControl(CacheControl.noCache().cachePublic())
                         .body(totalSupply);
  }

}
