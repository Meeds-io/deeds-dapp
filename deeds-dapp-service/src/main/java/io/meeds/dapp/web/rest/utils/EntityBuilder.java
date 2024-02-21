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
package io.meeds.dapp.web.rest.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;

import io.meeds.dapp.web.rest.model.DeedMetadataPresentation;
import io.meeds.dapp.web.rest.model.DeedTenantPresentation;
import io.meeds.dapp.web.rest.model.HubWithReward;
import io.meeds.deeds.common.constant.TenantProvisioningStatus;
import io.meeds.deeds.common.constant.TenantStatus;
import io.meeds.deeds.common.elasticsearch.model.DeedMetadata;
import io.meeds.deeds.common.elasticsearch.model.DeedTenant;
import io.meeds.deeds.common.model.DeedMetadataAttribute;
import io.meeds.deeds.common.service.HubReportService;
import io.meeds.wom.api.model.Hub;
import io.meeds.wom.api.model.HubReport;

public class EntityBuilder {

  private EntityBuilder() {
    // Static util methods only
  }

  public static ResponseEntity<DeedTenantPresentation> getDeedTenantResponse(DeedTenant deedTenant) {
    if (deedTenant == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok().body(build(deedTenant));
  }

  public static ResponseEntity<DeedMetadataPresentation> getDeedMetadataResponse(DeedMetadata deedMetadata) {
    if (deedMetadata == null) {
      return ResponseEntity.notFound().build();
    }
    BodyBuilder response = ResponseEntity.ok();
    applyCache(response);
    allowOtherOrigins(response);
    return response.body(build(deedMetadata));
  }

  public static DeedMetadataPresentation build(DeedMetadata deedMetadata) {
    if (deedMetadata == null) {
      return null;
    }
    Set<DeedMetadataAttribute> attributes = deedMetadata.getAttributes() == null ? new HashSet<>() :
                                                                                 new HashSet<>(deedMetadata.getAttributes());
    return new DeedMetadataPresentation(deedMetadata.getName(),
                                        deedMetadata.getDescription(),
                                        deedMetadata.getImageUrl(),
                                        deedMetadata.getBackgroundColor(),
                                        deedMetadata.getExternalUrl(),
                                        deedMetadata.getExternalLink(),
                                        deedMetadata.getSellerFeeBasisPoints(),
                                        deedMetadata.getFeeRecipient(),
                                        attributes);
  }

  public static DeedTenantPresentation build(DeedTenant deedTenant) {
    LocalDateTime date = deedTenant.getDate();
    long epochSecond = date == null ? 0 : date.toEpochSecond(ZoneOffset.UTC);
    return new DeedTenantPresentation(deedTenant.getNftId(),
                                      deedTenant.getManagerAddress(),
                                      Objects.requireNonNullElse(deedTenant.getTenantProvisioningStatus(),
                                                                 TenantProvisioningStatus.STOP_CONFIRMED),
                                      Objects.requireNonNullElse(deedTenant.getTenantStatus(), TenantStatus.UNDEPLOYED),
                                      epochSecond);
  }

  public static HubWithReward decorateHubWithReward(Hub hub, HubReportService hubReportService) {
    Page<HubReport> reports = hubReportService.getReportsByHub(hub.getAddress(),
                                                               PageRequest.of(0, 3, Sort.by(Direction.DESC, "sentDate")));
    HubReport lastSentReport = reports.stream()
                                      .findFirst()
                                      .orElse(null);
    HubReport lastRewardedReport = reports.stream()
                                          .filter(r -> hubReportService.computeEngagementScore(r.getReportId()) > 0)
                                          .findFirst()
                                          .orElse(null);
    return new HubWithReward(hub,
                             lastSentReport == null ? 0l : lastSentReport.getActionsCount(),
                             lastSentReport == null ? 0d : lastSentReport.getHubTopRewardedAmount(),
                             lastRewardedReport == null ? 0d : lastRewardedReport.getEngagementScore(),
                             lastRewardedReport != null);
  }

  private static void applyCache(BodyBuilder response) {
    response.cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic()).lastModified(ZonedDateTime.now());
  }

  private static void allowOtherOrigins(BodyBuilder response) {
    response.headers(headers -> {
      headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");

      headers.addAll(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                     Arrays.asList("Content-Type", "Range", "User-Agent", "X-Requested-With"));

      headers.addAll(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                     Arrays.asList("Content-Range", "X-Chunked-Output", "X-Stream-Output"));

      headers.addAll(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, Arrays.asList("GET", "HEAD"));
    });
  }

}
