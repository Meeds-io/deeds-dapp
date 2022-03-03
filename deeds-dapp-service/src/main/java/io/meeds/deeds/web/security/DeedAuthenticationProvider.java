/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.meeds.deeds.web.security;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Sign.SignatureData;
import org.web3j.utils.Numeric;

import io.meeds.deeds.web.utils.Utils;

@Component
public class DeedAuthenticationProvider implements AuthenticationProvider {

  public static final String USER_ROLE_NAME = "USER";

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    String walletAddress = authentication.getName();
    String encryptedMessage = authentication.getCredentials().toString();

    ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();

    HttpServletRequest request = requestAttributes.getRequest();
    String message = request.getParameter("message");

    HttpSession session = request.getSession(false);
    String loginMessage = Utils.getLoginMessage(session);

    try {
      if (!StringUtils.contains(message, loginMessage)) {
        throw new BadCredentialsException("Flat message signed by user doesn't have the corresponding Token");
      } else if (validateSignedMessage(walletAddress, message, encryptedMessage)) {
        return new UsernamePasswordAuthenticationToken(walletAddress,
                                                       null,
                                                       Collections.singleton(new SimpleGrantedAuthority(USER_ROLE_NAME)));
      } else {
        throw new BadCredentialsException("Can't decrypt wallet message");
      }
    } catch (Exception e) {
      throw new AuthenticationServiceException("An unknown error is encountered while authenticating user", e);
    }
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return authentication.equals(UsernamePasswordAuthenticationToken.class);
  }

  /**
   * @param walletAddress wallet Address (wallet public key)
   * @param rawMessage raw signed message
   * @param signedMessage encrypted message
   * @return true if the message has been decrypted successfully, else false
   * @throws UnsupportedEncodingException when UTF-8 isn't recognized as
   *           Encoding Charset
   * @throws SignatureException when an error occurs while decrypting signed
   *           message
   */
  public boolean validateSignedMessage(String walletAddress,
                                       String rawMessage,
                                       String signedMessage) throws UnsupportedEncodingException, SignatureException {
    if (StringUtils.isBlank(walletAddress) || StringUtils.isBlank(rawMessage) || StringUtils.isBlank(signedMessage)) {
      return false;
    }

    byte[] signatureBytes = Numeric.hexStringToByteArray(signedMessage);
    byte[] r = Arrays.copyOfRange(signatureBytes, 0, 32);
    byte[] s = Arrays.copyOfRange(signatureBytes, 32, 64);
    byte v = signatureBytes[64];
    if (v < 27) {
      v += 27;
    }

    BigInteger publicKey = Sign.signedPrefixedMessageToKey(rawMessage.getBytes(), new SignatureData(v, r, s));
    if (publicKey != null) {
      String recoveredAddress = "0x" + Keys.getAddress(publicKey);
      if (recoveredAddress.equalsIgnoreCase(walletAddress)) {
        return true;
      }
    }
    return false;
  }

}
