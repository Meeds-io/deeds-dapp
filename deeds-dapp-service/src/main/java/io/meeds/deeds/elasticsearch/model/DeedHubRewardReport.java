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
package io.meeds.deeds.elasticsearch.model;

import java.time.Instant;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(value = Include.NON_EMPTY)
@Document(indexName = "deed_tenant_hub_reward_report", createIndex = true)
@Setting(replicas = 0, shards = 1)
public class DeedHubRewardReport {

  @Id
  @Field(type = FieldType.Keyword)
  private String      hash;

  @Field(type = FieldType.Keyword)
  private String      hubAddress;

  @Field(type = FieldType.Long)
  private long        deedId;

  @Field(type = FieldType.Date, format = DateFormat.basic_date_time, storeNullValue = true)
  private Instant     fromDate;

  @Field(type = FieldType.Date, format = DateFormat.basic_date_time, storeNullValue = true)
  private Instant     toDate;

  @Field(type = FieldType.Date, format = DateFormat.basic_date_time, storeNullValue = true)
  private Instant     sentRewardsDate;

  @Field(type = FieldType.Keyword)
  private String      periodType;

  @Field(type = FieldType.Long)
  private long        participantsCount;

  @Field(type = FieldType.Long)
  private long        recipientsCount;

  @Field(type = FieldType.Long)
  private long        achievementsCount;

  @Field(type = FieldType.Double)
  private double      rewardAmount;

  @Field(type = FieldType.Text)
  private String      rewardTokenAddress;

  @Field(type = FieldType.Long)
  private long        rewardTokenNetworkId;

  @Field(type = FieldType.Text)
  private Set<String> transactions;

  @Field(type = FieldType.Keyword)
  private String      signature;

  @Field(type = FieldType.Date, format = DateFormat.basic_date_time, storeNullValue = true)
  private Instant     createdDate = Instant.now();

}
