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
package io.meeds.deeds.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.meeds.deeds.constant.WomAuthorizationException;
import io.meeds.deeds.constant.WomException;
import io.meeds.deeds.constant.WomParsingException;
import io.meeds.deeds.constant.WomRequestException;
import io.meeds.deeds.model.HubRewardReportRequest;
import io.meeds.deeds.model.HubRewardReportStatus;
import io.meeds.deeds.service.HubReportService;

@RestController
@RequestMapping("/api/hub/reports")
public class HubReportController {

  @Autowired
  private HubReportService hubReportService;

  @GetMapping
  public PagedModel<EntityModel<HubRewardReportStatus>> getHubReports(
                                                                      @RequestParam(name = "hubAddress", required = true)
                                                                      String hubAddress,
                                                                      Pageable pageable,
                                                                      PagedResourcesAssembler<HubRewardReportStatus> assembler) {
    Page<HubRewardReportStatus> reports = hubReportService.getRewardReports(hubAddress, pageable);
    return assembler.toModel(reports);
  }

  @GetMapping("/{hash}")
  public ResponseEntity<Object> getRewardReportByHash(
                                                      @PathVariable(name = "hash")
                                                      String hash) {
    HubRewardReportStatus hubRewardReportStatus = hubReportService.getRewardReport(hash);
    if (hubRewardReportStatus == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok(hubRewardReportStatus);
    }
  }

  @PostMapping
  public ResponseEntity<Object> saveRewardReport(
                                                 @RequestBody
                                                 HubRewardReportRequest hubRewardReportRequest) {
    try {
      HubRewardReportStatus hubRewardReportStatus = hubReportService.saveRewardReport(hubRewardReportRequest);
      return ResponseEntity.ok(hubRewardReportStatus);
    } catch (WomRequestException | WomParsingException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getErrorCode());
    } catch (WomAuthorizationException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getErrorCode());
    } catch (WomException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getErrorCode());
    }
  }

}
