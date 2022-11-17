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
package io.meeds.deeds.listener;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.meeds.deeds.model.DeedTenantOffer;
import io.meeds.deeds.model.UserProfileDTO;
import io.meeds.deeds.service.DeedTenantOfferService;
import io.meeds.deeds.service.UserProfileService;

@Service
public class OfferCreatedListener implements EventListener<DeedTenantOffer> {

  public static final String        LISTENER_NAME    = "OfferCreatedListener";

  private static final List<String> SUPPORTED_EVENTS = Collections.singletonList(DeedTenantOfferService.OFFER_CREATED_EVENT);

  @Autowired
  private UserProfileService        userProfileService;

  @Override
  public String getName() {
    return LISTENER_NAME;
  }

  @Override
  public List<String> getSupportedEvents() {
    return SUPPORTED_EVENTS;
  }

  @Override
  public void onEvent(String eventName, DeedTenantOffer deedTenantOffer) {
    if (StringUtils.isNotBlank(deedTenantOffer.getOwnerEmail())) {
      UserProfileDTO userProfileDTO = new UserProfileDTO(deedTenantOffer.getOwner(), deedTenantOffer.getOwnerEmail());
      userProfileService.saveUserProfile(userProfileDTO);
    }
  }

}
