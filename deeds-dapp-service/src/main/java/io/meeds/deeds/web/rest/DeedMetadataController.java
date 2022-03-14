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

import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.meeds.deeds.model.DeedMetadata;
import io.meeds.deeds.service.DeedMetadataService;
import io.meeds.deeds.web.rest.model.DeedMetadataPresentation;

@RestController
@RequestMapping("/api/deed/metadata/")
public class DeedMetadataController {

  private static final String DAPP_IMAGE_SERVER_BASE = "${DAPP_SERVER_BASE}";

  @Autowired
  private DeedMetadataService deedMetadataService;

  @Value("${meeds.deed.metadata.serverBase:}")
  private String              imageServerBase;

  @GetMapping("/{nftId}")
  @CrossOrigin(origins = "*")
  public ResponseEntity<DeedMetadataPresentation> getNftMetadata(
                                                                 @PathVariable(name = "nftId")
                                                                 Long nftId,
                                                                 HttpServletRequest request) {
    DeedMetadata deedMetadata = deedMetadataService.getDeedMetadata(nftId);
    if (deedMetadata == null) {
      return ResponseEntity.notFound().build();
    } else if (StringUtils.contains(deedMetadata.getImageUrl(), DAPP_IMAGE_SERVER_BASE)) {
      deedMetadata.setImageUrl(deedMetadata.getImageUrl()
                                           .replace(DAPP_IMAGE_SERVER_BASE,
                                                    getServerBase(request)));
    }

    return ResponseEntity.ok()
                         .cacheControl(CacheControl.maxAge(15, TimeUnit.MINUTES).cachePublic())
                         .lastModified(ZonedDateTime.now())
                         .body(DeedMetadataPresentation.build(deedMetadata));
  }

  @GetMapping("/type/{cityIndex}/{cardType}")
  public ResponseEntity<DeedMetadataPresentation> getNftMetadata(
                                                                 @PathVariable(name = "cityIndex")
                                                                 short cityIndex,
                                                                 @PathVariable(name = "cardType")
                                                                 short cardType,
                                                                 HttpServletRequest request) {
    DeedMetadata deedMetadata = deedMetadataService.getDeedMetadataOfCard(cityIndex, cardType);
    if (deedMetadata == null) {
      return ResponseEntity.notFound().build();
    } else if (StringUtils.contains(deedMetadata.getImageUrl(), DAPP_IMAGE_SERVER_BASE)) {
      deedMetadata.setImageUrl(deedMetadata.getImageUrl()
                                           .replace(DAPP_IMAGE_SERVER_BASE,
                                                    getServerBase(request)));
    }

    return ResponseEntity.ok()
                         .cacheControl(CacheControl.maxAge(15, TimeUnit.MINUTES).cachePublic())
                         .lastModified(ZonedDateTime.now())
                         .body(DeedMetadataPresentation.build(deedMetadata));
  }

  private String getServerBase(HttpServletRequest request) {
    if (StringUtils.isBlank(imageServerBase)) {
      return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + ""
          + request.getContextPath();
    } else {
      return imageServerBase;
    }
  }

}
