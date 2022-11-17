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

import static io.meeds.deeds.constant.CommonConstants.DEED_EMAIL_CODE_CONFIRMATION_TEMPLATE;
import static io.meeds.deeds.constant.CommonConstants.DEED_EMAIL_CODE_PARAM_NAME;
import static io.meeds.deeds.constant.CommonConstants.DEED_EMAIL_SEND_COMMAND_EVENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import io.meeds.deeds.model.EmailSendingCommand;

@SpringBootTest(classes = {
    AuthorizationCodeService.class,
})
@TestPropertySource(properties = {
    "meeds.authorizationCode.maxCodeValidityInMinutes=1",
    "meeds.authorizationCode.maxCodeSending=2",
    "meeds.authorizationCode.maxCodeVerification=3",
})
class AuthorizationCodeServiceTest {

  private static final String      EMAIL = "email";

  private static final String      DATA  = "data";

  private String                   key;

  @MockBean
  private ListenerService          listenerService;

  @Autowired
  private AuthorizationCodeService authorizationCodeService;

  @BeforeEach
  void setup() throws NoSuchAlgorithmException {
    this.key = String.valueOf(SecureRandom.getInstanceStrong().nextInt(1000000));
  }

  @Test
  void testParameters() throws IllegalAccessException {
    assertEquals(1, authorizationCodeService.getMaxCodeValidityInMinutes());
    assertEquals(2, authorizationCodeService.getMaxCodeSending());
    assertEquals(3, authorizationCodeService.getMaxCodeVerification());
  }

  @Test
  void testGenerateNewCode() throws IllegalAccessException {
    authorizationCodeService.generateCode(key, EMAIL, DATA);
    verifyEmailSending();
  }

  @Test
  void testGenerateCodeExceptionally() throws IllegalAccessException {
    authorizationCodeService.generateCode(key, EMAIL, DATA);
    authorizationCodeService.generateCode(key, EMAIL, DATA);
    assertThrows(IllegalAccessException.class, () -> authorizationCodeService.generateCode(key, EMAIL, DATA));
  }

  @Test
  void testCheckValidity() throws IllegalAccessException {
    authorizationCodeService.generateCode(key, EMAIL, DATA);
    int generatedCode = getGeneratedCode();
    authorizationCodeService.checkValidity(key, generatedCode);
    assertThrows(IllegalAccessException.class, () -> authorizationCodeService.checkValidity(key, generatedCode + 1));
  }

  @Test
  void testCheckValidityExceptionally() throws IllegalAccessException {
    authorizationCodeService.generateCode(key, EMAIL, DATA);
    int generatedCode = getGeneratedCode();
    authorizationCodeService.checkValidity(key, generatedCode);
    authorizationCodeService.checkValidity(key, generatedCode);
    authorizationCodeService.checkValidity(key, generatedCode);
    assertThrows(IllegalAccessException.class, () -> authorizationCodeService.checkValidity(key, generatedCode));
  }

  @Test
  void testValidateAndGetData() throws IllegalAccessException {
    authorizationCodeService.generateCode(key, EMAIL, DATA);
    int generatedCode = getGeneratedCode();
    Object data = authorizationCodeService.validateAndGetData(key, generatedCode);
    assertEquals(DATA, data);
  }

  @Test
  void testValidateAndGetDataWhenVerificationChecksReachedMax() throws IllegalAccessException {
    authorizationCodeService.generateCode(key, EMAIL, DATA);
    int generatedCode = getGeneratedCode();
    authorizationCodeService.checkValidity(key, generatedCode);
    authorizationCodeService.checkValidity(key, generatedCode);
    authorizationCodeService.checkValidity(key, generatedCode);
    Object data = authorizationCodeService.validateAndGetData(key, generatedCode);
    assertEquals(DATA, data);
  }

  @Test
  void testValidateAndGetDataWhenVerificationChecksReachedMaxPlusOne() throws IllegalAccessException {
    authorizationCodeService.generateCode(key, EMAIL, DATA);
    int generatedCode = getGeneratedCode();
    authorizationCodeService.checkValidity(key, generatedCode);
    authorizationCodeService.checkValidity(key, generatedCode);
    authorizationCodeService.checkValidity(key, generatedCode);
    assertThrows(IllegalAccessException.class, () -> authorizationCodeService.checkValidity(key, generatedCode));
    assertThrows(IllegalAccessException.class, () -> authorizationCodeService.validateAndGetData(key, generatedCode));
  }

  @Test
  void testValidateAndGetDataWhenVerificationCodeNotValid() throws IllegalAccessException {
    authorizationCodeService.generateCode(key, EMAIL, DATA);
    int generatedCode = getGeneratedCode();
    assertThrows(IllegalAccessException.class, () -> authorizationCodeService.validateAndGetData(key, generatedCode + 1));
  }

  @Test
  void testReinitializeCodeVerificationProcessAfterValidCheck() throws IllegalAccessException {
    authorizationCodeService.generateCode(key, EMAIL, DATA);
    int generatedCode = getGeneratedCode();
    authorizationCodeService.checkValidity(key, generatedCode);
    authorizationCodeService.checkValidity(key, generatedCode);
    authorizationCodeService.checkValidity(key, generatedCode);
    Object data = authorizationCodeService.validateAndGetData(key, generatedCode);
    assertEquals(DATA, data);
    authorizationCodeService.generateCode(key, EMAIL, DATA);
    int newlyGeneratedCode = getGeneratedCode(2);
    authorizationCodeService.checkValidity(key, newlyGeneratedCode);
    authorizationCodeService.checkValidity(key, newlyGeneratedCode);
    authorizationCodeService.checkValidity(key, newlyGeneratedCode);
    assertNotEquals(generatedCode, newlyGeneratedCode);
  }

  @Test
  void testCantGenerateCodeWhenMaxCheckReached() throws IllegalAccessException {
    authorizationCodeService.generateCode(key, EMAIL, DATA);
    int generatedCode = getGeneratedCode();
    authorizationCodeService.checkValidity(key, generatedCode);
    authorizationCodeService.checkValidity(key, generatedCode);
    authorizationCodeService.checkValidity(key, generatedCode);
    assertThrows(IllegalAccessException.class, () -> authorizationCodeService.generateCode(key, EMAIL, DATA));
  }

  @Test
  void testCanGenerateCodeWhenNotMaxCheckReached() throws IllegalAccessException {
    authorizationCodeService.generateCode(key, EMAIL, DATA);
    int generatedCode = getGeneratedCode();
    authorizationCodeService.checkValidity(key, generatedCode);
    authorizationCodeService.checkValidity(key, generatedCode);
    authorizationCodeService.generateCode(key, EMAIL, DATA);
    int newlyGeneratedCode = getGeneratedCode(2);
    assertEquals(generatedCode, newlyGeneratedCode);
  }

  private int getGeneratedCode() {
    return getGeneratedCode(1);
  }

  private int getGeneratedCode(int times) {
    AtomicInteger code = new AtomicInteger();
    verify(listenerService, times(times)).publishEvent(eq(DEED_EMAIL_SEND_COMMAND_EVENT), argThat(new ArgumentMatcher<>() {
      @Override
      public boolean matches(Object argument) {
        assertNotNull(argument);
        assertEquals(EmailSendingCommand.class, argument.getClass());
        EmailSendingCommand emailSendingCommand = (EmailSendingCommand) argument;
        assertEquals(EMAIL, emailSendingCommand.getEmail());
        assertEquals(DEED_EMAIL_CODE_CONFIRMATION_TEMPLATE, emailSendingCommand.getTemplate());
        assertNotNull(emailSendingCommand.getParameters());
        assertEquals(1, emailSendingCommand.getParameters().size());
        code.set(Integer.parseInt(emailSendingCommand.getParameters().get(DEED_EMAIL_CODE_PARAM_NAME)));
        return true;
      }
    }));
    return code.get();
  }

  private void verifyEmailSending() {
    verify(listenerService, times(1)).publishEvent(eq(DEED_EMAIL_SEND_COMMAND_EVENT), argThat(new ArgumentMatcher<>() {
      @Override
      public boolean matches(Object argument) {
        assertNotNull(argument);
        assertEquals(EmailSendingCommand.class, argument.getClass());
        EmailSendingCommand emailSendingCommand = (EmailSendingCommand) argument;
        assertEquals(EMAIL, emailSendingCommand.getEmail());
        assertEquals(DEED_EMAIL_CODE_CONFIRMATION_TEMPLATE, emailSendingCommand.getTemplate());
        assertNotNull(emailSendingCommand.getParameters());
        assertEquals(1, emailSendingCommand.getParameters().size());
        assertTrue(emailSendingCommand.getParameters().containsKey(DEED_EMAIL_CODE_PARAM_NAME));
        return true;
      }
    }));
  }

}
