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

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.meeds.deeds.api.constant.UEMRewardStatusType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_EMPTY)
@Relation(collectionRelation = "rewards", itemRelation = "reward")
public class UEMReward extends UEMRewardVerifiableData {

  private String              id;

  private Set<String>         hubAddresses;

  /**
   * Report Hashes
   */
  private Set<String>         reportHashes;

  private Set<String>         transactionHashes;

  /**
   * Total internal hub achievements
   */
  private long                hubAchievementsCount;

  /**
   * Total internal hub rewards sent to hub users
   */
  private double              hubRewardsAmount;

  /**
   * Total internal hub reward indices
   */
  private double              uemRewardIndex;

  private double              globalEngagementRate;

  private String              periodType;

  private long                tokenNetworkId;

  private String              tokenAddress;

  private UEMRewardStatusType status;

  private Instant             createdDate;

  public UEMReward(String id, // NOSONAR
                   String hash,
                   String reportMerkleRoot,
                   Instant fromDate,
                   Instant toDate,
                   String periodType,
                   Set<String> hubAddresses,
                   Set<String> reportHashes,
                   Set<String> transactionHashes,
                   SortedMap<String, Double> reportRewards,
                   long hubAchievementsCount,
                   double hubRewardsAmount,
                   double uemRewardIndex,
                   double uemRewardAmount,
                   long tokenNetworkId,
                   String tokenAddress,
                   double globalEngagementRate,
                   UEMRewardStatusType status,
                   Instant createdDate) {
    super(hash,
          reportMerkleRoot,
          fromDate,
          toDate,
          uemRewardAmount,
          reportRewards);
    this.id = id;
    this.periodType = periodType;
    this.hubAddresses = hubAddresses;
    this.reportHashes = reportHashes;
    this.transactionHashes = transactionHashes;
    this.hubAchievementsCount = hubAchievementsCount;
    this.hubRewardsAmount = hubRewardsAmount;
    this.uemRewardIndex = uemRewardIndex;
    this.tokenNetworkId = tokenNetworkId;
    this.tokenAddress = tokenAddress;
    this.globalEngagementRate = globalEngagementRate;
    this.status = status;
    this.createdDate = createdDate;
  }

  public long getHubsCount() {
    return getHubAddresses() == null ? 0 : getHubAddresses().size();
  }

  public double getEw() {
    long hubsCount = reportHashes == null ? 0 : reportHashes.size();
    return hubsCount == 0 ? 0d
                          : BigDecimal.valueOf(globalEngagementRate)
                                      .divide(BigDecimal.valueOf(hubsCount), MathContext.DECIMAL128)
                                      .doubleValue();
  }

  public void addTransactionHash(String transactionHash) {
    if (transactionHashes == null) {
      transactionHashes = new HashSet<>();
    } else {
      transactionHashes = new HashSet<>(transactionHashes);
    }
    transactionHashes.add(transactionHash);
  }

}
