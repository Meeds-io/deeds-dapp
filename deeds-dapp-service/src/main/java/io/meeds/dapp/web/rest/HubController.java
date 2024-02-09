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

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.dapp.web.rest.model.HubWithReward;
import io.meeds.dapp.web.rest.utils.EntityBuilder;
import io.meeds.deeds.common.model.FileBinary;
import io.meeds.deeds.common.model.ManagedDeed;
import io.meeds.deeds.common.service.HubReportService;
import io.meeds.deeds.common.service.HubService;
import io.meeds.wom.api.constant.ObjectNotFoundException;
import io.meeds.wom.api.constant.WomAuthorizationException;
import io.meeds.wom.api.constant.WomException;
import io.meeds.wom.api.constant.WomParsingException;
import io.meeds.wom.api.constant.WomRequestException;
import io.meeds.wom.api.model.Hub;
import io.meeds.wom.api.model.HubUpdateRequest;
import io.meeds.wom.api.model.WomConnectionRequest;
import io.meeds.wom.api.model.WomConnectionResponse;
import io.meeds.wom.api.model.WomDisconnectionRequest;

@RestController
@RequestMapping("/api/hubs")
public class HubController {

  private static final String WOM_UNKNOWN_ERROR_MESSAGE     = "wom.unknownError:";

  private static final Logger LOG                           = LoggerFactory.getLogger(HubController.class);

  private static final String WOM_CONNECTION_LOG_MESSAGE    =
                                                         "WOM-CONNECTION-FAIL: {}: An error happened when trying to connect to the WoM";

  private static final String WOM_DISCONNECTION_LOG_MESSAGE =
                                                            "WOM-DISCONNECTION-FAIL: {}: An error happened when trying to disconnect from the WoM";

  @Autowired
  private HubService          hubService;

  @Autowired
  private HubReportService    hubReportService;

  @GetMapping
  public ResponseEntity<PagedModel<EntityModel<HubWithReward>>> getHubs(Pageable pageable,
                                                                        PagedResourcesAssembler<HubWithReward> assembler,
                                                                        @RequestParam(name = "rewardId", required = false,
                                                                                      defaultValue = "0")
                                                                        long rewardId) {
    Page<Hub> hubs = hubService.getHubs(rewardId, pageable);
    return ResponseEntity.ok()
                         .cacheControl(CacheControl.noStore())
                         .body(assembler.toModel(hubs.map(hub -> EntityBuilder.decorateHubWithReward(hub, hubReportService))));
  }

  @GetMapping("/{hubAddress}")
  public ResponseEntity<Hub> getHub(
                                    @PathVariable(name = "hubAddress")
                                    String hubAddress,
                                    @RequestParam(name = "forceRefresh", required = false)
                                    boolean forceRefresh) {
    Hub hub = hubService.getHub(hubAddress, forceRefresh);
    if (hub == null) {
      return ResponseEntity.notFound().build();
    } else {
      return ResponseEntity.ok()
                           .cacheControl(CacheControl.noStore())
                           .body(EntityBuilder.decorateHubWithReward(hub, hubReportService));
    }
  }

  @GetMapping("/managed-deeds/{managerAddress}")
  public ResponseEntity<Object> getManagedDeeds(
                                                @PathVariable(name = "managerAddress")
                                                String managerAddress) {
    List<ManagedDeed> deeds = hubService.getManagedDeeds(managerAddress);
    return ResponseEntity.ok()
                         .cacheControl(CacheControl.noStore())
                         .body(deeds);
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

  @GetMapping("/{hubAddress}/avatar")
  public ResponseEntity<InputStreamResource> getHubAvatar(
                                                          @PathVariable(name = "hubAddress")
                                                          String hubAddress,
                                                          @RequestParam(name = "v", required = false)
                                                          String lastUpdated) {
    return getFileResponse(hubService.getHubAvatar(hubAddress), lastUpdated);
  }

  @PostMapping
  public ResponseEntity<Object> connectToWom(
                                             @RequestBody
                                             WomConnectionRequest hubConnectionRequest) {
    try {
      WomConnectionResponse connectionResponse = hubService.connectToWom(hubConnectionRequest);
      return ResponseEntity.ok(connectionResponse);
    } catch (WomRequestException | WomParsingException e) {
      LOG.info(WOM_CONNECTION_LOG_MESSAGE, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getErrorCode());
    } catch (WomAuthorizationException e) {
      LOG.info(WOM_CONNECTION_LOG_MESSAGE, e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getErrorCode());
    } catch (WomException e) {
      LOG.info(WOM_CONNECTION_LOG_MESSAGE, e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getErrorCode());
    } catch (Exception e) {
      LOG.warn("An unkown error happened when trying to process the connection request", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(WOM_UNKNOWN_ERROR_MESSAGE + e.getMessage());
    }
  }

  @PutMapping
  public ResponseEntity<Object> updateHub(
                                          @RequestBody
                                          HubUpdateRequest hubUpdateRequest) {
    try {
      hubService.updateHub(hubUpdateRequest);
      return ResponseEntity.noContent().build();
    } catch (WomRequestException | WomParsingException e) {
      LOG.info(WOM_CONNECTION_LOG_MESSAGE, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getErrorCode());
    } catch (WomAuthorizationException e) {
      LOG.info(WOM_CONNECTION_LOG_MESSAGE, e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getErrorCode());
    } catch (WomException e) {
      LOG.info(WOM_CONNECTION_LOG_MESSAGE, e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getErrorCode());
    } catch (Exception e) {
      LOG.warn("An unkown error happened when trying to process the refresh Hub request", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(WOM_UNKNOWN_ERROR_MESSAGE + e.getMessage());
    }
  }

  @DeleteMapping
  public ResponseEntity<Object> disconnectFromWom(
                                                  @RequestBody
                                                  WomDisconnectionRequest disconnectionRequest) {
    try {
      String womAddress = hubService.disconnectFromWom(disconnectionRequest);
      return ResponseEntity.ok(womAddress);
    } catch (WomRequestException | WomParsingException e) {
      LOG.info(WOM_DISCONNECTION_LOG_MESSAGE, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getErrorCode());
    } catch (WomAuthorizationException e) {
      LOG.info(WOM_DISCONNECTION_LOG_MESSAGE, e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getErrorCode());
    } catch (WomException e) {
      LOG.info(WOM_DISCONNECTION_LOG_MESSAGE, e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getErrorCode());
    } catch (Exception e) {
      LOG.warn("An unkown error happened when trying to process the disconnection request", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(WOM_UNKNOWN_ERROR_MESSAGE + e.getMessage());
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

  @GetMapping("/token")
  public String generateToken() {
    return hubService.generateToken();
  }

  private ResponseEntity<InputStreamResource> getFileResponse(FileBinary file, String lastUpdated) {
    if (file == null) {
      return ResponseEntity.notFound().build();
    }
    BodyBuilder builder = ResponseEntity.ok();
    if (StringUtils.isBlank(lastUpdated)) {
      builder.cacheControl(CacheControl.noStore());
    } else {
      builder.cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic());
    }
    return builder.lastModified(file.getUpdatedDate())
                  .contentType(MediaType.valueOf(file.getMimeType()))
                  .body(new InputStreamResource(file.getBinary()));
  }

}
