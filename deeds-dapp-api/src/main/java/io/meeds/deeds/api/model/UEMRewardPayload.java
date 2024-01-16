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

import java.time.Instant;
import java.util.Objects;
import java.util.SortedMap;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_EMPTY)
@Relation(collectionRelation = "rewards", itemRelation = "reward")
public class UEMRewardPayload implements DataPayload, Cloneable {

  private String                    reportsMerkleRoot;

  private Instant                   fromDate;

  private Instant                   toDate;

  /**
   * Total internal hub rewards sent to hub users
   */
  private double                    uemRewardAmount;

  /**
   * Report Hash => UEM Reward amount
   */
  private SortedMap<String, Double> reportRewards;

  @Override
  public final String generateRawMessage() {
    if (Objects.equals(this.getClass(), UEMRewardPayload.class)) {
      return DataPayload.super.generateRawMessage();
    } else {
      return clone().generateRawMessage();
    }
  }

  @Override
  protected UEMRewardPayload clone() { // NOSONAR
    return new UEMRewardPayload(reportsMerkleRoot,
                                fromDate,
                                toDate,
                                uemRewardAmount,
                                reportRewards);
  }

}
