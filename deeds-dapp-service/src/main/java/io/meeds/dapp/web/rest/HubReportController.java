/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.dapp.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.meeds.deeds.common.service.HubReportService;
import io.meeds.wom.api.constant.WomAuthorizationException;
import io.meeds.wom.api.constant.WomException;
import io.meeds.wom.api.constant.WomParsingException;
import io.meeds.wom.api.constant.WomRequestException;
import io.meeds.wom.api.model.HubReport;
import io.meeds.wom.api.model.HubReportVerifiableData;

@RestController
@RequestMapping("/api/hub/reports")
public class HubReportController {

  @Autowired
  private HubReportService reportService;

  @GetMapping
  public ResponseEntity<PagedModel<EntityModel<HubReport>>> getReports(Pageable pageable,
                                                                       PagedResourcesAssembler<HubReport> assembler,
                                                                       @RequestParam(name = "hubAddress", required = false)
                                                                       String hubAddress,
                                                                       @RequestParam(name = "rewardId", required = false, defaultValue = "0")
                                                                       long rewardId) {
    Page<HubReport> reports = reportService.getReports(hubAddress, rewardId, pageable);
    return ResponseEntity.ok()
                         .cacheControl(CacheControl.noStore())
                         .body(assembler.toModel(reports));
  }

  @GetMapping("/{reportId}")
  public ResponseEntity<Object> getReport(
                                          @PathVariable(name = "reportId")
                                          long reportId,
                                          @RequestParam(name = "forceRefresh", required = false)
                                          boolean forceRefresh) {
    HubReport hubReport = forceRefresh ? reportService.refreshReport(reportId) : reportService.getReport(reportId);
    if (hubReport == null) {
      return ResponseEntity.notFound()
                           .cacheControl(CacheControl.noStore())
                           .build();
    } else {
      return ResponseEntity.ok()
                           .cacheControl(CacheControl.noStore())
                           .body(hubReport);
    }
  }

  @GetMapping("/{rewardId}/{hubAddress}")
  public ResponseEntity<Object> getReport(
                                          @PathVariable(name = "rewardId")
                                          long rewardId,
                                          @PathVariable(name = "hubAddress")
                                          String hubAddress) {
    HubReport hubReport = reportService.getReport(rewardId, hubAddress);
    if (hubReport == null) {
      return ResponseEntity.notFound()
                           .cacheControl(CacheControl.noStore())
                           .build();
    } else {
      return ResponseEntity.ok()
                           .cacheControl(CacheControl.noStore())
                           .body(hubReport);
    }
  }

  @PostMapping
  public ResponseEntity<Object> saveReport(
                                           @RequestBody
                                           HubReportVerifiableData reportRequest) {
    try {
      HubReport report = reportService.saveReport(reportRequest);
      return ResponseEntity.ok(report);
    } catch (WomRequestException | WomParsingException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getErrorCode());
    } catch (WomAuthorizationException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getErrorCode());
    } catch (WomException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getErrorCode());
    }
  }

}
