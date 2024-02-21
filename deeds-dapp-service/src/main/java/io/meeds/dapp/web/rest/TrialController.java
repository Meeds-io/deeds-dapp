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

import static io.meeds.deeds.common.constant.CommonConstants.CODE_VERIFICATION_HTTP_HEADER;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.deeds.common.elasticsearch.model.TrialContactInformation;
import io.meeds.deeds.common.service.AuthorizationCodeService;
import io.meeds.deeds.common.service.TrialService;

@RestController
@RequestMapping("/api/trials")
public class TrialController {

  @Autowired
  private TrialService trialService;

  @Autowired
  private AuthorizationCodeService authorizationCodeService;

  @PostMapping(value="/contact", consumes =MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public TrialContactInformation saveTrial(@RequestParam(name = "fullname")
                                           String fullname,
                                           @RequestParam(name = "position")
                                           String position,
                                           @RequestParam(name = "organization")
                                           String organization,
                                           @RequestParam(name = "motivation")
                                           String motivation,
                                           @RequestParam(name = "email")
                                           String email,
                                           @RequestHeader(name = CODE_VERIFICATION_HTTP_HEADER, required = true)
                                           int code) {
    if (email == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    try {
      authorizationCodeService.validateAndGetData(email, code);
      return trialService.saveTrial(fullname, position, organization, motivation, email);
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
  }

}
