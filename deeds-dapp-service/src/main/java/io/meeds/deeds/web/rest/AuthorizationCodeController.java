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

import static io.meeds.deeds.common.constant.CommonConstants.CODE_VERIFICATION_HTTP_HEADER;

import java.security.Principal;

import javax.annotation.security.RolesAllowed;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.deeds.common.service.AuthorizationCodeService;
import io.meeds.deeds.web.security.DeedAuthenticationProvider;

@RestController
@RequestMapping("/api/authorization")
public class AuthorizationCodeController {

  @Autowired
  private AuthorizationCodeService authorizationCodeService;

  @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @RolesAllowed(DeedAuthenticationProvider.USER_ROLE_NAME)
  public void generateCode(Principal principal,
                           @RequestParam("email")
                           String email) {
    if (principal == null || StringUtils.isBlank(principal.getName()) || StringUtils.isBlank(email)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    try {
      String walletAddress = principal.getName();
      authorizationCodeService.generateCode(StringUtils.lowerCase(walletAddress), email, email);
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Max authorization code usage reached");
    }
  }

  @GetMapping
  @RolesAllowed(DeedAuthenticationProvider.USER_ROLE_NAME)
  @ResponseStatus(value = HttpStatus.NO_CONTENT)
  public void checkValidity(Principal principal,
                            @RequestHeader(name = CODE_VERIFICATION_HTTP_HEADER, required = true)
                            int code) {
    if (principal == null || StringUtils.isBlank(principal.getName())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    String walletAddress = StringUtils.lowerCase(principal.getName());
    try {
      authorizationCodeService.checkValidity(walletAddress, code);
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid code");
    }
  }

}
