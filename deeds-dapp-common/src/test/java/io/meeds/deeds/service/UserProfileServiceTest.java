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
package io.meeds.deeds.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import io.meeds.deeds.model.UserProfile;
import io.meeds.deeds.model.UserProfileDTO;
import io.meeds.deeds.storage.UserProfileRepository;

@SpringBootTest(classes = {
    UserProfileService.class,
})
class UserProfileServiceTest {

  private static final String   ADDRESS = "address";

  private static final String   EMAIL   = "email";

  private static final String   ID      = "@id";

  @MockBean
  private UserProfileRepository userProfileRepository;

  @MockBean
  private ListenerService       listenerService;

  @Autowired
  private UserProfileService    userProfileService;

  @Test
  void testGetUserProfileNull() {
    UserProfileDTO userProfileDTO = userProfileService.getUserProfile(ADDRESS);
    assertNull(userProfileDTO);
  }

  @Test
  void testGetUserProfile() {
    when(userProfileRepository.findByAddress(ADDRESS)).thenReturn(new UserProfile(ID, ADDRESS, EMAIL));
    UserProfileDTO userProfileDTO = userProfileService.getUserProfile(ADDRESS);
    assertNotNull(userProfileDTO);
    assertEquals(new UserProfileDTO(ADDRESS, EMAIL), userProfileDTO);
  }

  @Test
  void testSaveUserProfileWhenNotExists() {
    userProfileService.saveUserProfile(new UserProfileDTO(ADDRESS, EMAIL));
    verify(userProfileRepository, times(1)).save(new UserProfile().withAddress(ADDRESS).withEmail(EMAIL));
  }

  @Test
  void testSaveUserProfileWhenExists() {
    when(userProfileRepository.findByAddress(ADDRESS)).thenReturn(new UserProfile(ID, ADDRESS, EMAIL));
    userProfileService.saveUserProfile(new UserProfileDTO(ADDRESS, EMAIL));
    verify(userProfileRepository, times(1)).save(new UserProfile(ID, ADDRESS, EMAIL));
  }

}
