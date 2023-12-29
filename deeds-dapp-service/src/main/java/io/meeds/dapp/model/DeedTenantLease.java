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
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import io.meeds.dapp.constant.RentalPaymentPeriodicity;
import io.meeds.deeds.constant.DeedCard;
import io.meeds.deeds.constant.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "deed_tenant_lease", createIndex = true)
@Setting(replicas = 0, shards = 1)
public class DeedTenantLease {

  @Id
  @Field(type = FieldType.Long)
  private long                             id;

  @Field(type = FieldType.Long)
  private long                             nftId;

  @Field(type = FieldType.Auto)
  private io.meeds.deeds.constant.DeedCity city;

  @Field(type = FieldType.Auto)
  private DeedCard                         cardType;

  @Field(type = FieldType.Long)
  private int                              months;

  @Field(type = FieldType.Long)
  private int                              paidMonths;

  @Field(type = FieldType.Integer)
  private int                              noticePeriod;

  @Field(type = FieldType.Long)
  private int                              monthPaymentInProgress;

  @Field(type = FieldType.Keyword, storeNullValue = true)
  private String                           owner;

  @Field(type = FieldType.Keyword, storeNullValue = true)
  private String                           manager;

  @Field(type = FieldType.Keyword)
  private String                           managerEmail;

  @Field(type = FieldType.Keyword)
  private List<String>                     viewAddresses;

  @Field(type = FieldType.Keyword)
  private RentalPaymentPeriodicity         paymentPeriodicity;

  @Field(type = FieldType.Double)
  private double                           amount;

  @Field(type = FieldType.Double)
  private double                           allDurationAmount;

  @Field(type = FieldType.Double)
  private double                           distributedAmount;

  @Field(type = FieldType.Integer)
  private int                              ownerMintingPercentage;

  @Field(type = FieldType.Double)
  private double                           mintingPower;

  @Field(type = FieldType.Keyword)
  private List<String>                     pendingTransactions;

  @Field(type = FieldType.Keyword)
  private TransactionStatus                transactionStatus;

  @Field(type = FieldType.Date, format = DateFormat.basic_date_time, storeNullValue = true)
  private Instant                          startDate;

  @Field(type = FieldType.Date, format = DateFormat.basic_date_time, storeNullValue = true)
  private Instant                          endDate;

  @Field(type = FieldType.Date, format = DateFormat.basic_date_time, storeNullValue = true)
  private Instant                          noticeDate;

  @Field(type = FieldType.Date, format = DateFormat.basic_date_time, storeNullValue = true)
  private Instant                          paidRentsDate;

  @Field(type = FieldType.Date, format = DateFormat.basic_date_time, storeNullValue = true)
  private Instant                          createdDate;

  @Field(type = FieldType.Boolean)
  private boolean                          confirmed;

  @Field(type = FieldType.Boolean)
  private boolean                          enabled;

  @Field(type = FieldType.Boolean)
  private boolean                          endingLease;

  @Field(type = FieldType.Keyword)
  private String                           endingLeaseAddress;

  @Field(type = FieldType.Long)
  private long                             lastCheckedBlock;

}
