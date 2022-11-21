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
package io.meeds.deeds.utils;

import io.meeds.deeds.elasticsearch.model.UserProfile;
import io.meeds.deeds.model.UserProfileDTO;

public class UserProfileMapper {

  private UserProfileMapper() {
    // Class with Static methods
  }

  public static UserProfileDTO toDTO(UserProfile userProfile) {
    if (userProfile == null) {
      return null;
    }
    return new UserProfileDTO().withAddress(userProfile.getAddress())
                               .withEmail(userProfile.getEmail());
  }

  public static UserProfile fromDTO(UserProfileDTO userProfileDTO) {
    if (userProfileDTO == null) {
      return null;
    }
    return new UserProfile().withAddress(userProfileDTO.getAddress().toLowerCase())
                            .withEmail(userProfileDTO.getEmail());
  }

}
