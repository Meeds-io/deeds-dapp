/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import io.meeds.deeds.common.constant.TrialStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "trial_contacts", createIndex = true)
@Setting(replicas = 0, shards = 1)
public class TrialContactInformation {

  @Id
  private Long                id;

  @Field(type = FieldType.Text)
  private String              fullName;

  @Field(type = FieldType.Keyword)
  private String              position;

  @Field(type = FieldType.Keyword)
  private String              organization;

  @Field(type = FieldType.Text)
  private String              motivation;

  @Field(type = FieldType.Keyword)
  private String              email;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
  @CreatedDate
  private LocalDateTime       createdDate;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
  @LastModifiedDate
  private LocalDateTime       lastModifiedDate;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
  private LocalDateTime       provisionedDate;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
  private LocalDateTime       deprovisionedDate;

  @Field(type = FieldType.Auto)
  private TrialStatus         status;

  private Map<String, String> properties;

}
