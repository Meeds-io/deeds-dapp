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
package io.meeds.deeds.web.rest.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;

import io.meeds.deeds.constant.TenantProvisioningStatus;
import io.meeds.deeds.constant.TenantStatus;
import io.meeds.deeds.elasticsearch.model.DeedMetadata;
import io.meeds.deeds.elasticsearch.model.DeedMetadataAttribute;
import io.meeds.deeds.elasticsearch.model.DeedTenant;
import io.meeds.deeds.web.rest.model.DeedMetadataPresentation;
import io.meeds.deeds.web.rest.model.DeedTenantPresentation;

public class EntityMapper {

  private EntityMapper() {
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
    Set<DeedMetadataAttribute> attributes = deedMetadata.getAttributes() == null ? new HashSet<>()
                                                                                 : new HashSet<>(deedMetadata.getAttributes());
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

  private static DeedTenantPresentation build(DeedTenant deedTenant) {
    LocalDateTime date = deedTenant.getDate();
    long epochSecond = date == null ? 0 : date.toEpochSecond(ZoneOffset.UTC);
    return new DeedTenantPresentation(deedTenant.getNftId(),
                                      deedTenant.getManagerAddress(),
                                      Objects.requireNonNullElse(deedTenant.getTenantProvisioningStatus(),
                                                                 TenantProvisioningStatus.STOP_CONFIRMED),
                                      Objects.requireNonNullElse(deedTenant.getTenantStatus(), TenantStatus.UNDEPLOYED),
                                      epochSecond);
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
