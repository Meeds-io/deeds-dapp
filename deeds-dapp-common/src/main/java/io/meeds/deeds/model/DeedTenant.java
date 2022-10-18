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
package io.meeds.deeds.model;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.annotations.Setting.SortOrder;

import io.meeds.deeds.constant.TenantProvisioningStatus;
import io.meeds.deeds.constant.TenantStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "deed_tenant_manager", createIndex = true)
@Setting(sortFields = "date", sortOrders = SortOrder.desc, replicas = 0, shards = 1)
public class DeedTenant {

  @Id
  private long                     nftId;

  private short                    cityIndex = -1;

  private short                    cardType  = -1;

  private String                   managerAddress;

  private String                   managerEmail;

  private String                   startupTransactionHash;

  private String                   shutdownTransactionHash;

  private TenantProvisioningStatus tenantProvisioningStatus;

  private TenantStatus             tenantStatus;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
  private LocalDateTime            date;

  private Map<String, String>      properties;

}
