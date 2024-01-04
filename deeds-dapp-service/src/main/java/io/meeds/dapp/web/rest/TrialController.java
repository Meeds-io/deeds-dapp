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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.deeds.elasticsearch.model.TrialContactInformation;
import io.meeds.deeds.service.TrialService;
import io.meeds.dapp.web.security.DeedAuthenticationProvider;

import jakarta.annotation.security.RolesAllowed;

@RestController
@RequestMapping("/api/trials")
public class TrialController {

  @Autowired
  private TrialService trialService;

  @PostMapping(value="/contact", consumes =MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @RolesAllowed(DeedAuthenticationProvider.USER_ROLE_NAME)
  public TrialContactInformation saveTrial(@RequestParam(name = "firstname")
                                           String firstname,
                                           @RequestParam(name = "lastname")
                                           String lastname,
                                           @RequestParam(name = "position")
                                           String position,
                                           @RequestParam(name = "organization")
                                           String organization,
                                           @RequestParam(name = "email")
                                           String email) {
    if (email == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    return trialService.saveTrial(firstname, lastname, position, organization, email);
  }
   
}
