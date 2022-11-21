/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
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
import java.util.List;

import org.springframework.data.annotation.Id;
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
@Document(indexName = "deed_tenant_events", createIndex = false)
@Setting(replicas = 0, shards = 1)
@JsonInclude(value = Include.NON_EMPTY)
public class DeedTenantEvent {

  @Id
  private String       id;

  @Field(type = FieldType.Keyword)
  private String       eventName;

  private String       objectJson;

  @Field(type = FieldType.Keyword)
  private List<String> consumers;

  @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
  private Instant      date;

  public DeedTenantEvent(String eventName, String objectJson, List<String> consumers, Instant date) {
    this.eventName = eventName;
    this.objectJson = objectJson;
    this.consumers = consumers;
    this.date = date;
  }

}
