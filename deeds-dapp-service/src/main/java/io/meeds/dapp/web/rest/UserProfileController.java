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

import java.security.Principal;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.meeds.dapp.web.security.DeedAuthenticationProvider;
import io.meeds.deeds.common.model.UserProfileDTO;
import io.meeds.deeds.common.service.UserProfileService;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

  @Autowired
  private UserProfileService userProfileService;

  @GetMapping(path = "/email", produces = MediaType.TEXT_PLAIN_VALUE)
  @Secured(DeedAuthenticationProvider.USER_ROLE_NAME)
  @ResponseStatus(value = HttpStatus.OK)
  public String getEmail(Principal principal) {
    if (principal == null || StringUtils.isBlank(principal.getName())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    String walletAddress = principal.getName();
    UserProfileDTO userProfile = userProfileService.getUserProfile(walletAddress);
    return userProfile == null ? null : userProfile.getEmail();
  }

}
