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

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

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

  @Id
  @Field(type = FieldType.Long)
  private long              reportId;

  @Field(type = FieldType.Keyword)
  private long              rewardId;

  @Field(type = FieldType.Keyword)
  private String            hubAddress;

  @Field(type = FieldType.Keyword)
  private String            deedManagerAddress;

  @Field(type = FieldType.Keyword)
  private String            ownerAddress;

  @Field(type = FieldType.Integer)
  private int               ownerMintingPercentage;

  @Field(type = FieldType.Long)
  private long              deedId;

  @Field(type = FieldType.Short)
  private short             city;

  @Field(type = FieldType.Short)
  private short             cardType;

  @Field(type = FieldType.Short)
  private short             mintingPower;

  @Field(type = FieldType.Long)
  private long              maxUsers;

  @Field(type = FieldType.Date, format = DateFormat.basic_date_time, storeNullValue = true)
  private Instant           fromDate;

  @Field(type = FieldType.Date, format = DateFormat.basic_date_time, storeNullValue = true)
  private Instant           toDate;

  @Field(type = FieldType.Date, format = DateFormat.basic_date_time, storeNullValue = true)
  private Instant           sentDate;

  @Field(type = FieldType.Keyword)
  private String            periodType;

  @Field(type = FieldType.Long)
  private long              usersCount;

  @Field(type = FieldType.Long)
  private long              participantsCount;

  @Field(type = FieldType.Long)
  private long              recipientsCount;

  @Field(type = FieldType.Long)
  private long              achievementsCount;

  @Field(type = FieldType.Text)
  private String            rewardTokenAddress;

  @Field(type = FieldType.Long)
  private long              rewardTokenNetworkId;

  @Field(type = FieldType.Text)
  private SortedSet<String> transactions;

  @Field(type = FieldType.Double)
  private double            hubRewardAmount;

  @Field(type = FieldType.Double)
  private double            fixedRewardIndex;

  @Field(type = FieldType.Double)
  private double            ownerFixedIndex;

  @Field(type = FieldType.Double)
  private double            tenantFixedIndex;

  @Field(type = FieldType.Boolean)
  private boolean           fraud;

  @Field(type = FieldType.Double)
  private double            lastPeriodUemRewardAmount;

  @Field(type = FieldType.Double)
  private double            uemRewardAmount;

  @Field(type = FieldType.Date, format = DateFormat.basic_date_time, storeNullValue = true)
  @LastModifiedDate
  private Instant           updatedDate = Instant.now();

}
