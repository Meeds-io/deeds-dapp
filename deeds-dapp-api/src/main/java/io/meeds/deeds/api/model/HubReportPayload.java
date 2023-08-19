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

import java.time.Instant;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_EMPTY)
@Relation(collectionRelation = "reports", itemRelation = "report")
public class HubReportPayload implements DataPayload, Cloneable {

  @Getter
  private String            hubAddress;

  @Getter
  @Setter
  private long              deedId;

  @Getter
  @Setter
  private Instant           fromDate;

  @Getter
  @Setter
  private Instant           toDate;

  @Getter
  @Setter
  private Instant           sentDate;

  @Getter
  @Setter
  private String            periodType;

  @Getter
  @Setter
  private long              usersCount;

  @Getter
  @Setter
  private long              participantsCount;

  @Getter
  @Setter
  private long              recipientsCount;

  @Getter
  @Setter
  private long              achievementsCount;

  @Getter
  private String            rewardTokenAddress;

  @Getter
  @Setter
  private long              rewardTokenNetworkId;

  @Getter
  @Setter
  private double            hubRewardAmount;

  @Getter
  private SortedSet<String> transactions;

  public void setHubAddress(String hubAddress) {
    this.hubAddress = StringUtils.lowerCase(hubAddress);
  }

  public void setRewardTokenAddress(String rewardTokenAddress) {
    this.rewardTokenAddress = StringUtils.lowerCase(rewardTokenAddress);
  }

  public void setTransactions(SortedSet<String> transactions) {
    this.transactions = lowerCase(transactions);
  }

  @Override
  public final String generateRawMessage() {
    if (Objects.equals(this.getClass(), HubReportPayload.class)) {
      return DataPayload.super.generateRawMessage();
    } else {
      return clone().generateRawMessage();
    }
  }

  protected static SortedSet<String> lowerCase(SortedSet<String> transactions) {
    return CollectionUtils.isEmpty(transactions) ? transactions
                                                 : transactions.stream()
                                                               .map(StringUtils::lowerCase)
                                                               .filter(StringUtils::isNotBlank)
                                                               .collect(Collectors.toCollection(TreeSet::new));
  }

  @Override
  protected HubReportPayload clone() { // NOSONAR
    return new HubReportPayload(hubAddress,
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
  }
}
