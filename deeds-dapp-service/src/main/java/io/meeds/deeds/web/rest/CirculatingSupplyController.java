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


import io.meeds.deeds.service.CirculatingSupplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/rest/meed")
public class CirculatingSupplyController {

  @Autowired
  private CirculatingSupplyService circulatingSupplyService;

  @GetMapping("/circ")
  public ResponseEntity<BigDecimal> getCirculatingSupply() {
    BigDecimal circulatingSupply = circulatingSupplyService.getCircualtingSupply();
    return ResponseEntity.ok()
                         .cacheControl(CacheControl.maxAge(15, TimeUnit.MINUTES).cachePublic())
                         .lastModified(ZonedDateTime.now())
                         .body(circulatingSupply);
  }

}
