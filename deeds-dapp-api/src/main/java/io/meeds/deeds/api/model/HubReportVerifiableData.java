/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 *
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
package io.meeds.deeds.api.model;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Arrays;
import java.util.SortedSet;

import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.server.core.Relation;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Sign.SignatureData;
import org.web3j.utils.Numeric;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonInclude(value = Include.NON_EMPTY)
@Relation(collectionRelation = "reports", itemRelation = "report")
public class HubReportVerifiableData extends HubReportPayload implements VerifiableData {

  @Getter
  private String hash;

  @Getter
  @Setter
  private String signature;

  public HubReportVerifiableData(String hash, // NOSONAR
                                 String signature,
                                 String hubAddress,
                                 long deedId,
                                 Instant fromDate,
                                 Instant toDate,
                                 Instant sentDate,
                                 String periodType,
                                 long usersCount,
                                 long participantsCount,
                                 long recipientsCount,
                                 long achievementsCount,
                                 String rewardTokenAddress,
                                 long rewardTokenNetworkId,
                                 double hubRewardAmount,
                                 SortedSet<String> transactions) {
    super(hubAddress,
          deedId,
          fromDate,
          toDate,
          sentDate,
          periodType,
          usersCount,
          participantsCount,
          recipientsCount,
          achievementsCount,
          rewardTokenAddress,
          rewardTokenNetworkId,
          hubRewardAmount,
          transactions);
    this.hash = hash;
    this.signature = signature;
  }

  public HubReportVerifiableData(String hash,
                                 String signature,
                                 HubReportPayload reportData) {
    this(hash,
         signature,
         reportData.getHubAddress(),
         reportData.getDeedId(),
         reportData.getFromDate(),
         reportData.getToDate(),
         reportData.getSentDate(),
         reportData.getPeriodType(),
         reportData.getUsersCount(),
         reportData.getParticipantsCount(),
         reportData.getRecipientsCount(),
         reportData.getAchievementsCount(),
         reportData.getRewardTokenAddress(),
         reportData.getRewardTokenNetworkId(),
         reportData.getHubRewardAmount(),
         reportData.getTransactions());
  }

  public void setHash(String hash) {
    this.hash = StringUtils.lowerCase(hash);
  }

  @Override
  public String generateHash() {
    return StringUtils.lowerCase(Hash.sha3(signature));
  }

  @Override
  public boolean isValid() throws SignatureException {
    return isValidHash() && isValidMessageSignature();
  }

  private boolean isValidMessageSignature() throws SignatureException {
    String hubAddress = getHubAddress();
    String rawMessage = generateRawMessage();
    if (StringUtils.isBlank(hubAddress) || StringUtils.isBlank(signature) || StringUtils.isBlank(rawMessage)) {
      return false;
    }

    byte[] signatureBytes = Numeric.hexStringToByteArray(signature);
    if (signatureBytes.length < 64) {
      return false;
    }
    byte[] r = Arrays.copyOfRange(signatureBytes, 0, 32);
    byte[] s = Arrays.copyOfRange(signatureBytes, 32, 64);
    byte v = signatureBytes[64];
    if (v < 27) {
      v += 27;
    }

    BigInteger publicKey = Sign.signedPrefixedMessageToKey(rawMessage.getBytes(StandardCharsets.UTF_8),
                                                           new SignatureData(v, r, s));
    return StringUtils.equalsIgnoreCase(hubAddress, "0x" + Keys.getAddress(publicKey));
  }

}
