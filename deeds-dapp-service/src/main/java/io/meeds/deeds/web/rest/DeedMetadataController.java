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

import static io.meeds.deeds.web.rest.utils.EntityMapper.getDeedMetadataResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.meeds.deeds.common.elasticsearch.model.DeedMetadata;
import io.meeds.deeds.service.DeedMetadataService;
import io.meeds.deeds.web.rest.model.DeedMetadataPresentation;

@RestController
@RequestMapping("/api/deeds")
public class DeedMetadataController {

  @Autowired
  private DeedMetadataService deedMetadataService;

  @GetMapping
  public ResponseEntity<DeedMetadataPresentation> getContractMetadata() {
    DeedMetadata deedMetadata = deedMetadataService.getContractMetadata();
    return getDeedMetadataResponse(deedMetadata);
  }

  @GetMapping("/{nftId}")
  public ResponseEntity<DeedMetadataPresentation> getNftMetadata(
                                                                 @PathVariable(name = "nftId")
                                                                 Long nftId) {
    DeedMetadata deedMetadata = deedMetadataService.getDeedMetadata(nftId);
    return getDeedMetadataResponse(deedMetadata);
  }

  @GetMapping("/type/{cityIndex}/{cardType}")
  public ResponseEntity<DeedMetadataPresentation> getNftMetadata(
                                                                 @PathVariable(name = "cityIndex")
                                                                 short cityIndex,
                                                                 @PathVariable(name = "cardType")
                                                                 short cardType) {
    DeedMetadata deedMetadata = deedMetadataService.getDeedMetadataOfCard(cityIndex, cardType);
    return getDeedMetadataResponse(deedMetadata);
  }

}
