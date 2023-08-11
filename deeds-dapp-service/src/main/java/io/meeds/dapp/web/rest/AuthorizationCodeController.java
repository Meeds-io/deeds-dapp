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

import static io.meeds.deeds.common.constant.CommonConstants.CODE_VERIFICATION_HTTP_HEADER;

import java.security.Principal;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.deeds.common.service.AuthorizationCodeService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/authorization")
public class AuthorizationCodeController {

  @Autowired
  private AuthorizationCodeService authorizationCodeService;

  @PostMapping(value="/generateCode", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public void generateCode(Principal principal,
                           @RequestParam("email")
                           String email,
                           HttpServletRequest request) {
    String clientIp = request.getHeader("X-FORWARDED-FOR");  
    if (clientIp == null) {  
      clientIp = request.getRemoteAddr();  
    }
    if (principal == null && StringUtils.isBlank(clientIp)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    try {
      if (principal == null) {
        authorizationCodeService.generateCode(email, email, email, clientIp);
      } else {
        String walletAddress = principal.getName();
        authorizationCodeService.generateCode(StringUtils.lowerCase(walletAddress), email, email, clientIp);
      }
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Max authorization code usage reached");
    }
  }

  @PostMapping(value="/checkValidity", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public void checkValidity(Principal principal,
                            @RequestHeader(name = CODE_VERIFICATION_HTTP_HEADER, required = true)
                            int code,
                            @RequestParam("email")
                            String email) {
    if (principal == null && StringUtils.isBlank(email)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    try {
      if (principal == null) {
        authorizationCodeService.checkValidity(email, code);
      } else {
        String walletAddress = StringUtils.lowerCase(principal.getName());
        authorizationCodeService.checkValidity(walletAddress, code);
      }
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid code");
    }
  }

}
