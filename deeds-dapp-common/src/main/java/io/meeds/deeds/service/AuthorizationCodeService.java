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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.meeds.deeds.listerner.model.EmailSendingCommand;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Component
public class AuthorizationCodeService {

  private SecureRandom                   secureRandomCodeGenerator;

  @Getter
  @Value("${meeds.authorizationCode.maxCodeValidityInMinutes:120}")
  private int                            maxCodeValidityInMinutes;

  @Getter
  @Value("${meeds.authorizationCode.maxCodeSending:3}")
  private int                            maxCodeSending;

  @Getter
  @Value("${meeds.authorizationCode.maxCodeVerification:20}")
  private int                            maxCodeVerification;

  @Getter
  @Value("${meeds.generationCode.limit:10}")
  private int                            generationCodeLimit;

  private Duration                       maxCodeValidity;

  private Map<String, AuthorizationCode> authorizationCodes = new ConcurrentHashMap<>();

  private Map<String, Integer> codeGenerationCount          = new ConcurrentHashMap<>();

  @Autowired
  private ListenerService                listenerService;

  public AuthorizationCodeService() throws NoSuchAlgorithmException {
    try {
      secureRandomCodeGenerator = SecureRandom.getInstance("SHA1PRNG");
    } catch (NoSuchAlgorithmException e) {
      secureRandomCodeGenerator = SecureRandom.getInstanceStrong();
    }
  }

  @PostConstruct
  public void init() {
    maxCodeValidity = Duration.ofMinutes(maxCodeValidityInMinutes);
  }

  /**
   * Generates a new Verification Code for a designated key and with protecting
   * a data that will be retrieved only after providing a valid code. The
   * generated code will be held for the designated key until 2 hours at maximum
   * (default value of code valid time). At the period of code valid time, when
   * the user checks more than 'meeds.authorizationCode.maxCodeVerification',
   * then an exception is raised. Same thing, when the user attempts to generate
   * a code more than 'meeds.authorizationCode.maxCodeSending' at the period of
   * code validity, an exception is raised as well to limit the number of emails
   * that a user can send.
   * 
   * @param  key                    A key to designate the protected data
   *                                  resource with code
   * @param  email                  email to send to to verify access to data
   *                                  object
   * @param  data                   data to cache and protect by email code
   *                                  verification mechanism
   * @throws IllegalAccessException when maximum code verification sending is
   *                                  reached or maximum code verification has
   *                                  been reached
   */
  public void generateCode(String key, String email, Object data) throws IllegalAccessException {
    if (!hasValidDateCode(key)) {
      authorizationCodes.put(key, newAuthorizationCode());
    }
    String clientIp = request.getHeader("X-FORWARDED-FOR");  
    if (clientIp == null) {  
      clientIp = request.getRemoteAddr();  
    }
    int count = codeGenerationCount.get(clientIp);
    if (count >= generationCodeLimit) {
      throw new IllegalAccessException("Code generation limit exceeded");
    }
    AuthorizationCode authorizationCode = authorizationCodes.get(key);
    authorizationCode.incrementSendingCount();
    authorizationCode.setData(data);
    codeGenerationCount.put(clientIp, count + 1);
    sendEmailWithCode(email, authorizationCode);
  }

  /**
   * Checks a code validity for a designated key and throws an exception when
   * any verification condition hasn't been met.
   * 
   * @param  key                    A key to designate the protected data
   *                                  resource with code
   * @param  code                   the verification code sent by email to
   *                                  protect access to data object
   * @throws IllegalAccessException when the maximum verification checks has
   *                                  been reached or when the code isn' valid
   */
  public void checkValidity(String key, int code) throws IllegalAccessException {
    if (!isValidCode(key, code, false)) {
      throw new IllegalAccessException("Code " + code + " isn't valid for key " + key);
    }
  }

  /**
   * Validate the entered code for a designated key and return its associated
   * protected data if valid.
   * 
   * @param  key                    A key to designate the protected data
   *                                  resource with code
   * @param  code                   the verification code sent by email to
   *                                  protect access to data object
   * @return                        the protected data object if the provided
   *                                code is valid
   * @throws IllegalAccessException when the maximum verification checks has
   *                                  been reached or when the code isn' valid
   */
  public Object validateAndGetData(String key, int code) throws IllegalAccessException {
    if (isValidCode(key, code, true)) {
      AuthorizationCode authorizationCode = authorizationCodes.remove(key);
      return authorizationCode.getData();
    }
    throw new IllegalAccessException("No Code found for designated key " + key);
  }

  private boolean isValidCode(String key, int code, boolean ignoreMaxTentativesTopBorder) throws IllegalAccessException {
    return StringUtils.isNotBlank(key) && authorizationCodes.containsKey(key)
        && authorizationCodes.get(key).isValid(code, ignoreMaxTentativesTopBorder);
  }

  private boolean hasValidDateCode(String key) {
    return StringUtils.isNotBlank(key) && authorizationCodes.containsKey(key)
        && authorizationCodes.get(key).isDateValid();
  }

  private void sendEmailWithCode(String email, AuthorizationCode authorizationCode) {
    EmailSendingCommand emailCommand = new EmailSendingCommand(email,
                                                               DEED_EMAIL_CODE_CONFIRMATION_TEMPLATE,
                                                               Collections.singletonMap(DEED_EMAIL_CODE_PARAM_NAME,
                                                                                        String.format("%06d",
                                                                                                      authorizationCode.getCode())));
    listenerService.publishEvent(DEED_EMAIL_SEND_COMMAND_EVENT, emailCommand);
  }

  private synchronized AuthorizationCode newAuthorizationCode() {
    return new AuthorizationCode();
  }

  public class AuthorizationCode {

    private final Instant generationTime = Instant.now();

    private final Instant maxValidTime   = generationTime.plus(maxCodeValidity);

    @Getter(AccessLevel.PRIVATE)
    private final int     code           = secureRandomCodeGenerator.nextInt(1000000);

    private int           sentTentatives;

    private int           verificationTentatives;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private Object        data;

    public boolean isValid(int codeToVerify, boolean ignoreMaxTentativesTopBorder) throws IllegalAccessException {
      incrementVerificationCount(ignoreMaxTentativesTopBorder);
      return isDateValid() && codeToVerify == this.code;
    }

    private void incrementSendingCount() throws IllegalAccessException {
      checkVerificationCount();
      if (sentTentatives++ >= maxCodeSending) {
        throw new IllegalAccessException("Max sending tentatives of code sending reached");
      }
    }

    private void checkVerificationCount() throws IllegalAccessException {
      if (verificationTentatives >= maxCodeVerification) {
        throw new IllegalAccessException("Max verification tentatives of code sending reached");
      }
    }

    private void incrementVerificationCount(boolean ignoreMaxTentativesTopBorder) throws IllegalAccessException {
      // When checking 3 times the same code and at the 3rd tentative send the
      // real code, we should allow to get last time the code to verify, thus we
      // should ignore the top border of MaxTentatives only once
      if (verificationTentatives++ >= maxCodeVerification
          && (!ignoreMaxTentativesTopBorder || verificationTentatives > (maxCodeVerification + 1))) {
        throw new IllegalAccessException("Max verification tentatives of code sending reached");
      }
    }

    private boolean isDateValid() {
      return Instant.now().isBefore(maxValidTime);
    }
  }

}
