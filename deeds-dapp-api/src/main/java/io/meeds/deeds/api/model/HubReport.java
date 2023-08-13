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

import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.meeds.deeds.api.constant.HubReportStatusType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(value = Include.NON_EMPTY)
@Relation(collectionRelation = "reports", itemRelation = "report")
public class HubReport extends HubReportData {

  @Getter
  private String              hash;

  @Getter
  @Setter
  private String              signature;

  @Getter
  private String              earnerAddress;

  @Getter
  private String              deedManagerAddress;

  @Getter
  @Setter
  private HubReportStatusType status;

  @Getter
  @Setter
  private String              error;

  @Getter
  @Setter
  private Instant             sentDate;

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
  private double              hubRewardAmountPerPeriod;

  @Getter
  @Setter
  private double              lastPeriodUemRewardAmountPerPeriod;

  @Getter
  @Setter
  private String              rewardId;

  @Getter
  private String              rewardHash;

  public HubReport(HubReportData report) {
    this.setReportData(report);
  }

  public void setHash(String hash) {
    this.hash = StringUtils.lowerCase(hash);
  }

  public void setEarnerAddress(String earnerAddress) {
    this.earnerAddress = StringUtils.lowerCase(earnerAddress);
  }

  public void setDeedManagerAddress(String deedManagerAddress) {
    this.deedManagerAddress = StringUtils.lowerCase(deedManagerAddress);
  }

  public void setRewardHash(String rewardHash) {
    this.rewardHash = StringUtils.lowerCase(rewardHash);
  }

  public void setReportData(HubReportData report) {
    this.setHubAddress(report.getHubAddress());
    this.setDeedId(report.getDeedId());
    this.setFromDate(report.getFromDate());
    this.setToDate(report.getToDate());
    this.setPeriodType(report.getPeriodType());
    this.setUsersCount(report.getUsersCount());
    this.setParticipantsCount(report.getParticipantsCount());
    this.setRecipientsCount(report.getRecipientsCount());
    this.setAchievementsCount(report.getAchievementsCount());
    this.setRewardTokenAddress(report.getRewardTokenAddress());
    this.setRewardTokenNetworkId(report.getRewardTokenNetworkId());
    this.setHubRewardAmount(report.getHubRewardAmount());
    this.setTransactions(report.getTransactions());
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

}
