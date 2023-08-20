/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.util.SortedSet;

import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.meeds.deeds.api.constant.HubReportStatusType;
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
public class HubReport extends HubReportVerifiableData {

  @Getter
  private String              earnerAddress;

  @Getter
  private String              deedManagerAddress;

  @Getter
  private String              ownerAddress;

  @Getter
  @Setter
  private int                 ownerMintingPercentage;

  @Getter
  @Setter
  private HubReportStatusType status;

  @Getter
  @Setter
  private String              error;

  @Getter
  @Setter
  private double              uemRewardIndex;

  @Getter
  @Setter
  private double              uemRewardAmount;

  @Getter
  @Setter
  private double              lastPeriodUemRewardAmount;

  @Getter
  @Setter
  private double              lastPeriodUemDiff;

  @Getter
  @Setter
  private double              hubRewardAmountPerPeriod;

  @Getter
  @Setter
  private double              hubRewardLastPeriodDiff;

  @Getter
  @Setter
  private double              lastPeriodUemRewardAmountPerPeriod;

  @Getter
  @Setter
  private double              mp;

  @Getter
  @Setter
  private String              rewardId;

  @Getter
  private String              rewardHash;

  @Getter
  private String              rewardTransactionHash;

  public HubReport(String hash, // NOSONAR
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
                   SortedSet<String> transactions,
                   String earnerAddress,
                   String deedManagerAddress,
                   String ownerAddress,
                   int ownerMintingPercentage,
                   HubReportStatusType status,
                   String error,
                   double uemRewardIndex,
                   double uemRewardAmount,
                   double lastPeriodUemRewardAmount,
                   double lastPeriodUemDiff,
                   double hubRewardAmountPerPeriod,
                   double hubRewardLastPeriodDiff,
                   double lastPeriodUemRewardAmountPerPeriod,
                   double mp,
                   String rewardId,
                   String rewardHash,
                   String rewardTransactionHash) {
    super(StringUtils.lowerCase(hash),
          signature,
          StringUtils.lowerCase(hubAddress),
          deedId,
          fromDate,
          toDate,
          sentDate,
          periodType,
          usersCount,
          participantsCount,
          recipientsCount,
          achievementsCount,
          StringUtils.lowerCase(rewardTokenAddress),
          rewardTokenNetworkId,
          hubRewardAmount,
          lowerCase(transactions));
    this.earnerAddress = StringUtils.lowerCase(earnerAddress);
    this.deedManagerAddress = StringUtils.lowerCase(deedManagerAddress);
    this.ownerAddress = StringUtils.lowerCase(ownerAddress);
    this.ownerMintingPercentage = ownerMintingPercentage;
    this.status = status;
    this.error = error;
    this.uemRewardIndex = uemRewardIndex;
    this.uemRewardAmount = uemRewardAmount;
    this.lastPeriodUemRewardAmount = lastPeriodUemRewardAmount;
    this.lastPeriodUemDiff = lastPeriodUemDiff;
    this.hubRewardAmountPerPeriod = hubRewardAmountPerPeriod;
    this.hubRewardLastPeriodDiff = hubRewardLastPeriodDiff;
    this.lastPeriodUemRewardAmountPerPeriod = lastPeriodUemRewardAmountPerPeriod;
    this.mp = mp;
    this.rewardId = rewardId;
    this.rewardHash = StringUtils.lowerCase(rewardHash);
    this.rewardTransactionHash = StringUtils.lowerCase(rewardTransactionHash);
  }

  public void setEarnerAddress(String earnerAddress) {
    this.earnerAddress = StringUtils.lowerCase(earnerAddress);
  }

  public void setDeedManagerAddress(String deedManagerAddress) {
    this.deedManagerAddress = StringUtils.lowerCase(deedManagerAddress);
  }

  public void setOwnerAddress(String ownerAddress) {
    this.ownerAddress = StringUtils.lowerCase(ownerAddress);
  }

  public void setRewardHash(String rewardHash) {
    this.rewardHash = StringUtils.lowerCase(rewardHash);
  }

  public void setRewardTransactionHash(String rewardTransactionHash) {
    this.rewardTransactionHash = StringUtils.lowerCase(rewardTransactionHash);
  }

  public double getEd() {
    double achievementsCount = getAchievementsCount();
    double participantsCount = getParticipantsCount();
    if (participantsCount == 0 || achievementsCount == 0) {
      return 0;
    } else {
      return BigDecimal.valueOf(achievementsCount)
                       .divide(BigDecimal.valueOf(participantsCount), MathContext.DECIMAL128)
                       .doubleValue();
    }
  }

  public double getDr() {
    return lastPeriodUemRewardAmount == 0 ? 1d
                                          : BigDecimal.valueOf(getHubRewardAmount())
                                                      .divide(BigDecimal.valueOf(lastPeriodUemRewardAmount),
                                                              MathContext.DECIMAL128)
                                                      .doubleValue();
  }

  public double getDs() {
    return getUsersCount() == 0 ? 0d
                                : BigDecimal.valueOf(getRecipientsCount())
                                            .divide(BigDecimal.valueOf(getUsersCount()), MathContext.DECIMAL128)
                                            .doubleValue();
  }

}
