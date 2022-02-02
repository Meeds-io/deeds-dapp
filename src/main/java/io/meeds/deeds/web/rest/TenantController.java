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

import java.security.Principal;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.deeds.constant.TenantCommand;
import io.meeds.deeds.constant.TenantStatus;
import io.meeds.deeds.constant.UnauthorizedOperationException;
import io.meeds.deeds.service.TenantService;
import io.meeds.deeds.web.security.DeedAuthenticationProvider;

@RestController
@RequestMapping("/api/tenant")
public class TenantController {

  @Autowired
  private TenantService tenantService;

  @GetMapping("/{networkId}/{deedAddress}/{id}")
  public String status(
                       @PathVariable(name = "networkId")
                       long networkId,
                       @PathVariable(name = "deedAddress")
                       String deedAddress,
                       @PathVariable(value = "id")
                       long deedId) {
    TenantStatus status = tenantService.getStatus(networkId, deedAddress, deedId);
    return status == null ? "" : status.name();
  }

  @PostMapping("/{networkId}/{deedAddress}/{id}")
  @RolesAllowed(DeedAuthenticationProvider.USER_ROLE_NAME)
  public void changeStatus(
                           @PathVariable(name = "networkId")
                           long networkId,
                           @PathVariable(name = "deedAddress")
                           String deedAddress,
                           @PathVariable(name = "id")
                           long deedId,
                           @RequestParam(name = "status", required = true)
                           TenantCommand status,
                           Principal principal) {
    if (principal == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    String wallet = principal.getName();
    try {
      tenantService.changeStatus(networkId, deedAddress, deedId, status, wallet);
    } catch (UnauthorizedOperationException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }

}
