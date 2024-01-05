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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import io.meeds.deeds.elasticsearch.model.TrialContactInformation;
import io.meeds.deeds.storage.TrialRepository;

@SpringBootTest(classes = {
                            TrialService.class,
})
class TrialServiceTest {

  private static final Long   ID           = 1L;

  private static final String FIRSTNAME    = "firstname";

  private static final String LASTNAME     = "lastname";

  private static final String POSITION     = "position";

  private static final String ORGANIZATION = "organization";

  private static final String EMAIL        = "email@test.com";

  @Autowired
  private TrialService        trialService;

  @MockBean
  private TrialRepository     trialRepository;

  @MockBean
  private ListenerService     listenerService;

  @Test
  void testSaveWithEmailTrial() {
    when(trialRepository.save(any())).thenAnswer(arg -> {
      TrialContactInformation trial = arg.getArgument(0);
      trial.setId(ID);
      return trial;
    });

    assertThrows(IllegalArgumentException.class, () -> trialService.saveTrial(null, LASTNAME, POSITION, ORGANIZATION, EMAIL));
    assertThrows(IllegalArgumentException.class, () -> trialService.saveTrial(FIRSTNAME, null, POSITION, ORGANIZATION, EMAIL));
    assertThrows(IllegalArgumentException.class, () -> trialService.saveTrial(FIRSTNAME, LASTNAME, POSITION, ORGANIZATION, null));

    TrialContactInformation savedTrial = trialService.saveTrial(FIRSTNAME, LASTNAME, POSITION, ORGANIZATION, EMAIL);
    assertNotNull(savedTrial);
    assertEquals(ID, savedTrial.getId());
    verify(listenerService, times(1)).publishEvent(eq(TRIAL_CREATE_COMMAND_EVENT), eq(savedTrial));
  }

  @Test
  void testGetTrialByEmail() {
    TrialContactInformation trialContactInformation = new TrialContactInformation(ID,
                                                                                  FIRSTNAME,
                                                                                  LASTNAME,
                                                                                  POSITION,
                                                                                  ORGANIZATION,
                                                                                  EMAIL,
                                                                                  null,
                                                                                  null,
                                                                                  null,
                                                                                  null,
                                                                                  null,
                                                                                  null);
    when(trialRepository.findByEmail(EMAIL)).thenReturn(trialContactInformation);
    TrialContactInformation trialByEmail = trialService.getTrialByEmail(EMAIL);
    assertEquals(trialContactInformation, trialByEmail);
  }

}
