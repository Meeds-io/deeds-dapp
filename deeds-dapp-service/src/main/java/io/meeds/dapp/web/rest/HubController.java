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
package io.meeds.dapp.web.rest;

import java.io.IOException;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.deeds.api.constant.ObjectNotFoundException;
import io.meeds.deeds.api.constant.WomAuthorizationException;
import io.meeds.deeds.api.constant.WomException;
import io.meeds.deeds.api.constant.WomParsingException;
import io.meeds.deeds.api.constant.WomRequestException;
import io.meeds.deeds.api.model.Hub;
import io.meeds.deeds.api.model.WomConnectionRequest;
import io.meeds.deeds.api.model.WomDisconnectionRequest;
import io.meeds.deeds.common.model.FileBinary;
import io.meeds.deeds.common.service.HubService;

@RestController
@RequestMapping("/api/hubs")
public class HubController {

  private static final Logger LOG = LoggerFactory.getLogger(HubController.class);

  @Autowired
  private HubService          hubService;

  @GetMapping
  public ResponseEntity<PagedModel<EntityModel<Hub>>> getHubs(Pageable pageable,
                                                              PagedResourcesAssembler<Hub> assembler,
                                                              @RequestParam(name = "rewardId", required = false)
                                                              String rewardId) {
    Page<Hub> hubs = hubService.getHubs(rewardId, pageable);
    return ResponseEntity.ok()
                         .cacheControl(CacheControl.noStore())
                         .body(assembler.toModel(hubs));
  }

  @GetMapping("/{hubAddress}")
  public ResponseEntity<Hub> getHub(
                                    @PathVariable(name = "hubAddress")
                                    String hubAddress) {
    Hub hub = hubService.getHub(hubAddress);
    if (hub == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok()
                         .cacheControl(CacheControl.noStore())
                         .body(hub);
  }

  @PostMapping("/{hubAddress}/avatar")
  public void saveHubAvatar(
                            @PathVariable(name = "hubAddress")
                            String hubAddress,
                            @RequestParam("signedMessage")
                            String signedMessage,
                            @RequestParam("rawMessage")
                            String rawMessage,
                            @RequestParam("token")
                            String token,
                            @RequestParam("file")
                            MultipartFile file) {
    try {
      hubService.saveHubAvatar(hubAddress,
                               signedMessage,
                               rawMessage,
                               token,
                               file);
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (WomException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "wom.errorReadingFile:" + e.getMessage());
    }
  }

  @PostMapping("/{hubAddress}/banner")
  public void saveHubBanner(
                            @PathVariable(name = "hubAddress")
                            String hubAddress,
                            @RequestParam("signedMessage")
                            String signedMessage,
                            @RequestParam("rawMessage")
                            String rawMessage,
                            @RequestParam("token")
                            String token,
                            @RequestParam("file")
                            MultipartFile file) {
    try {
      hubService.saveHubBanner(hubAddress,
                               signedMessage,
                               rawMessage,
                               token,
                               file);
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (WomException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "wom.errorReadingFile:" + e.getMessage());
    }
  }

  @GetMapping("/{hubAddress}/avatar")
  public ResponseEntity<InputStreamResource> getHubAvatar(
                                                          @PathVariable(name = "hubAddress")
                                                          String hubAddress,
                                                          @RequestParam(name = "v")
                                                          String lastUpdated) {
    return getFileResponse(hubService.getHubAvatar(hubAddress));
  }

  @GetMapping("/{hubAddress}/banner")
  public ResponseEntity<InputStreamResource> getHubBanner(
                                                          @PathVariable(name = "hubAddress")
                                                          String hubAddress,
                                                          @RequestParam(name = "v")
                                                          String lastUpdated) {
    return getFileResponse(hubService.getHubBanner(hubAddress));
  }

  @PostMapping
  public ResponseEntity<Object> connectToWoM(
                                             @RequestBody
                                             WomConnectionRequest hubConnectionRequest) {
    try {
      hubService.connectToWoM(hubConnectionRequest);
      return ResponseEntity.noContent().build();
    } catch (WomRequestException | WomParsingException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getErrorCode());
    } catch (WomAuthorizationException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getErrorCode());
    } catch (WomException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getErrorCode());
    } catch (Exception e) {
      LOG.warn("An unkown error happened when trying to process the request", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("wom.unknownError:" + e.getMessage());
    }
  }

  @DeleteMapping
  public ResponseEntity<Object> disconnectFromWoM(
                                                  @RequestBody
                                                  WomDisconnectionRequest disconnectionRequest) {
    try {
      hubService.disconnectFromWoM(disconnectionRequest);
      return ResponseEntity.noContent().build();
    } catch (WomRequestException | WomParsingException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getErrorCode());
    } catch (WomAuthorizationException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getErrorCode());
    } catch (WomException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getErrorCode());
    } catch (Exception e) {
      LOG.warn("An unkown error happened when trying to process the request", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("wom.unknownError:" + e.getMessage());
    }
  }

  @GetMapping("/byNftId/{nftId}")
  public ResponseEntity<Hub> getHubByNftId(
                                           @PathVariable(name = "nftId")
                                           long nftId) {
    Hub hub = hubService.getHub(nftId);
    if (hub == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(hub);
  }

  @GetMapping("/manager")
  public String isDeedTenantManager(
                                    @RequestParam(name = "nftId")
                                    Long nftId,
                                    @RequestParam(name = "address")
                                    String address) {
    boolean isManager = hubService.isDeedManager(address, nftId);
    return String.valueOf(isManager);
  }

  @GetMapping("/token")
  public String generateToken() {
    return hubService.generateToken();
  }

  private ResponseEntity<InputStreamResource> getFileResponse(FileBinary file) {
    if (file == null) {
      return ResponseEntity.notFound()
                           .build();
    }
    return ResponseEntity.ok()
                         .cacheControl(CacheControl.maxAge(Duration.ofDays(365))
                                                   .cachePublic())
                         .lastModified(file.getUpdatedDate())
                         .contentType(MediaType.valueOf(file.getMimeType()))
                         .body(new InputStreamResource(file.getBinary()));
  }

}
