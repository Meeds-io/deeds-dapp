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
package io.meeds.deeds.service;

import static io.meeds.deeds.constant.CommonConstants.TRIAL_CREATE_COMMAND_EVENT;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.meeds.deeds.constant.TrialStatus;
import io.meeds.deeds.elasticsearch.model.TrialContactInformation;
import io.meeds.deeds.storage.TrialRepository;

@Service
public class TrialService {

  @Autowired
  private TrialRepository trialRepository;

  @Autowired
  private ListenerService listenerService;

  public TrialContactInformation getTrialByEmail(String email) {
    return trialRepository.findByEmail(email);
  }

  public TrialContactInformation saveTrial(String firstname,
                                           String lastname,
                                           String position,
                                           String organization,
                                           String email) {

    if (StringUtils.isBlank(firstname)) {
      throw new IllegalArgumentException("firstname is mandatory");
    }
    if (StringUtils.isBlank(lastname)) {
      throw new IllegalArgumentException("lastname is mandatory");
    }
    if (StringUtils.isBlank(email)) {
      throw new IllegalArgumentException("email is mandatory");
    }
    TrialContactInformation trial = new TrialContactInformation();
    trial.setFirstName(firstname);
    trial.setLastName(lastname);
    trial.setPosition(position);
    trial.setOrganization(organization);
    trial.setEmail(email);
    trial.setCreatedDate(LocalDateTime.now());
    trial.setLastModifiedDate(trial.getCreatedDate());
    trial.setStatus(TrialStatus.OPEN);
    TrialContactInformation savedTrial = trialRepository.save(trial);

    listenerService.publishEvent(TRIAL_CREATE_COMMAND_EVENT, savedTrial);

    return savedTrial;
  }

}
