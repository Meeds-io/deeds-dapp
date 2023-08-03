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
package io.meeds.deeds.model;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(value = Include.NON_EMPTY)
@Relation(collectionRelation = "hubRewardReports", itemRelation = "hubRewardReport")
public class HubRewardReport {

  @Getter
  private String      hubAddress;

  @Getter
  @Setter
  private long        deedId;

  @Getter
  @Setter
  @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Instant     fromDate;

  @Getter
  @Setter
  @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Instant     toDate;

  @Getter
  @Setter
  @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Instant     sentRewardsDate;

  @Getter
  @Setter
  private String      periodType;

  @Getter
  @Setter
  private long        participantsCount;

  @Getter
  @Setter
  private long        recipientsCount;

  @Getter
  @Setter
  private long        achievementsCount;

  @Getter
  @Setter
  private double      rewardAmount;

  @Getter
  private String      rewardTokenAddress;

  @Getter
  @Setter
  private long        rewardTokenNetworkId;

  @Getter
  private Set<String> transactions;

  public void setHubAddress(String hubAddress) {
    this.hubAddress = StringUtils.lowerCase(hubAddress);
  }

  public void setRewardTokenAddress(String rewardTokenAddress) {
    this.rewardTokenAddress = StringUtils.lowerCase(rewardTokenAddress);
  }

  public void setTransactions(Set<String> transactions) {
    this.transactions = CollectionUtils.isEmpty(transactions) ? transactions
                                                              : transactions.stream()
                                                                            .map(StringUtils::lowerCase)
                                                                            .filter(StringUtils::isNotBlank)
                                                                            .collect(Collectors.toSet());
  }
}
