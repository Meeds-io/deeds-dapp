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
package io.meeds.deeds.common.elasticsearch.model;

import java.time.Instant;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.meeds.deeds.api.constant.UEMRewardStatusType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_EMPTY)
@Document(indexName = "deed_uem_reward", createIndex = true)
@Setting(replicas = 0, shards = 1)
public class UEMRewardEntity {

  @Id
  private String              id;

  @Field(type = FieldType.Keyword)
  private String              hash;

  @Field(type = FieldType.Keyword)
  private String              reportsMerkleRoot;

  @Field(type = FieldType.Date, format = DateFormat.basic_date_time, storeNullValue = true)
  private Instant             fromDate;

  @Field(type = FieldType.Date, format = DateFormat.basic_date_time, storeNullValue = true)
  private Instant             toDate;

  @Field(type = FieldType.Keyword)
  private String              periodType;

  @Field(type = FieldType.Keyword)
  private Set<String>         hubAddresses;

  @Field(type = FieldType.Keyword)
  private Set<String>         reportHashes;

  @Field(type = FieldType.Keyword)
  private Set<String>         transactionHashes;

  /**
   * Total internal hub achievements
   */
  @Field(type = FieldType.Long)
  private long                hubAchievementsCount;

  /**
   * Total internal hub rewards sent to hub users
   */
  @Field(type = FieldType.Double)
  private double              hubRewardsAmount;

  /**
   * Total hubs computed reward index
   */
  @Field(type = FieldType.Double)
  private double              uemRewardIndex;

  /**
   * Total UEM reward budget
   */
  @Field(type = FieldType.Double)
  private double              uemRewardAmount;

  @Field(type = FieldType.Long)
  private long                tokenNetworkId;

  @Field(type = FieldType.Keyword)
  private String              tokenAddress;

  @Field(type = FieldType.Double)
  private double              globalEngagementRate;

  @Field(type = FieldType.Keyword)
  private UEMRewardStatusType status;

  @Field(type = FieldType.Date, format = DateFormat.basic_date_time, storeNullValue = true)
  @CreatedDate
  private Instant             createdDate = Instant.now();

}