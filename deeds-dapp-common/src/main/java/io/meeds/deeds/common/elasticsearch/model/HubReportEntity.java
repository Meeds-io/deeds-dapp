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
package io.meeds.deeds.common.elasticsearch.model;

import java.time.Instant;
import java.util.SortedSet;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.meeds.wom.api.constant.HubReportStatusType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_EMPTY)
@Document(indexName = "hub_report", createIndex = true)
@Setting(replicas = 0, shards = 1)
public class HubReportEntity {

  @Field(type = FieldType.Long)
  private long                reportId;

  @Field(type = FieldType.Keyword)
  private String              hubAddress;

  @Field(type = FieldType.Keyword)
  private String              earnerAddress;

  @Field(type = FieldType.Keyword)
  private String              deedManagerAddress;

  @Field(type = FieldType.Keyword)
  private String              ownerAddress;

  @Field(type = FieldType.Integer)
  private int                 ownerMintingPercentage;

  @Field(type = FieldType.Long)
  private long                deedId;

  @Field(type = FieldType.Date, format = DateFormat.basic_date_time, storeNullValue = true)
  private Instant             fromDate;

  @Field(type = FieldType.Date, format = DateFormat.basic_date_time, storeNullValue = true)
  private Instant             toDate;

  @Field(type = FieldType.Date, format = DateFormat.basic_date_time, storeNullValue = true)
  private Instant             sentDate;

  @Field(type = FieldType.Keyword)
  private String              periodType;

  @Field(type = FieldType.Long)
  private long                usersCount;

  @Field(type = FieldType.Long)
  private long                participantsCount;

  @Field(type = FieldType.Long)
  private long                recipientsCount;

  @Field(type = FieldType.Long)
  private long                achievementsCount;

  @Field(type = FieldType.Text)
  private String              rewardTokenAddress;

  @Field(type = FieldType.Long)
  private long                rewardTokenNetworkId;

  @Field(type = FieldType.Text)
  private SortedSet<String>   transactions;

  @Field(type = FieldType.Keyword)
  private String              signature;

  // UEM computed field
  @Field(type = FieldType.Keyword)
  private HubReportStatusType status;

  // Report validation error code
  @Field(type = FieldType.Keyword)
  private String              error;

  @Field(type = FieldType.Keyword)
  private long                rewardId;

  @Field(type = FieldType.Double)
  private double              hubRewardAmount;

  // UEM computed field
  @Field(type = FieldType.Double)
  private double              uemRewardIndex;

  // UEM computed field
  @Field(type = FieldType.Double)
  private double              uemRewardAmount;

  // UEM computed field
  @Field(type = FieldType.Double)
  private double              lastPeriodUemRewardAmount;

  // UEM computed field
  @Field(type = FieldType.Double)
  private double              lastPeriodUemDiff;

  // UEM computed field
  @Field(type = FieldType.Double)
  private double              hubRewardAmountPerPeriod;

  // UEM computed field
  @Field(type = FieldType.Double)
  private double              hubRewardLastPeriodDiff;

  // UEM computed field
  @Field(type = FieldType.Double)
  private double              lastPeriodUemRewardAmountPerPeriod;

  // UEM computed field
  @Field(type = FieldType.Double)
  private double              mp;

  @Field(type = FieldType.Date, format = DateFormat.basic_date_time, storeNullValue = true)
  @CreatedDate
  private Instant             createdDate = Instant.now();

  @Field(type = FieldType.Date, format = DateFormat.basic_date_time, storeNullValue = true)
  @LastModifiedDate
  private Instant             updatedDate = Instant.now();

}
