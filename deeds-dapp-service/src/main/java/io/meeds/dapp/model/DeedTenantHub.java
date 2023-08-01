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
package io.meeds.dapp.model;

import java.time.Instant;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@JsonInclude(value = Include.NON_EMPTY)
@Document(indexName = "deed_tenant_hub", createIndex = true)
@Setting(replicas = 0, shards = 1)
public class DeedTenantHub {

  @Getter
  @Setter
  @Field(type = FieldType.Long)
  private long    nftId;

  @Getter
  @Setter
  @Field(type = FieldType.Short)
  private short   city;

  @Getter
  @Setter
  @Field(type = FieldType.Short)
  private short   type;

  @Id
  @Getter
  @Field(type = FieldType.Keyword)
  private String  hubAddress;

  @Getter
  @Field(type = FieldType.Keyword)
  private String  deedManagerAddress;

  @Getter
  @Setter
  @Field(type = FieldType.Text)
  private String  hubName;

  @Getter
  @Setter
  @Field(type = FieldType.Text)
  private String  hubDescription;

  @Getter
  @Setter
  @Field(type = FieldType.Auto)
  private String  hubUrl;

  @Getter
  @Setter
  @Field(type = FieldType.Auto)
  private String  hubLogoUrl;

  @Getter
  @Setter
  @Field(type = FieldType.Keyword)
  private String  color;

  @Getter
  @Field(type = FieldType.Keyword)
  private String  earnerAddress;

  @Getter
  @Setter
  @Field(type = FieldType.Boolean)
  private boolean enabled;

  @Field(type = FieldType.Date, format = DateFormat.basic_date_time, storeNullValue = true)
  @Getter
  @Setter
  private Instant createdDate = Instant.now();

  public void setHubAddress(String hubAddress) {
    this.hubAddress = StringUtils.lowerCase(hubAddress);
  }

  public void setDeedManagerAddress(String deedManagerAddress) {
    this.deedManagerAddress = StringUtils.lowerCase(deedManagerAddress);
  }

  public void setEarnerAddress(String earnerAddress) {
    this.earnerAddress = StringUtils.lowerCase(earnerAddress);
  }

}
