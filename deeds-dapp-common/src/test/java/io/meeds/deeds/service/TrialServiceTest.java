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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import io.meeds.deeds.constant.TrialStatus;
import io.meeds.deeds.elasticsearch.model.TrialContactInformation;
import io.meeds.deeds.storage.TrialRepository;

@SpringBootTest(classes = {
  TrialService.class
})
class TrialServiceTest {

  private static final Long   id           = 1L; 

  private static final String firstname    = "firstname";

  private static final String lastname     = "lastname";

  private static final String position     = "position";

  private static final String organization = "organization";

  private static final String email        = "email";

  @Autowired
  private TrialService        trialService;

  @MockBean
  private TrialRepository     trialRepository;
    
  @Test
  void testSaveWithEmailTrial() {
    trialService.saveTrial(firstname, lastname, position, organization, email);
    verify(trialRepository, times(1)).save(argThat(new ArgumentMatcher<TrialContactInformation>() {
      @Override
      public boolean matches(TrialContactInformation trial) {
        assertNotNull(trial);
        assertNotNull(trial.getEmail());
        assertEquals(trial.getStatus(), TrialStatus.OPEN);
        assertEquals(trial.getCreatedDate(), trial.getLastModifiedDate());
        return true;
      }
    }));
  }

  @Test
  void testGetTrialByEmail() {
    assertNull(trialService.getTrialByEmail(email));
    TrialContactInformation trial = new TrialContactInformation();
    trial.setId(id);
    trial.setFirstName(firstname);
    trial.setLastName(lastname);
    trial.setPosition(position);
    trial.setOrganization(organization);
    trial.setEmail(email);
    trial.setCreatedDate(LocalDateTime.now());
    when(trialService.getTrialByEmail(email)).thenReturn(trial);
  }
}
