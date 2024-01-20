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
package io.meeds.deeds.common.model;

import java.time.Instant;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.meeds.deeds.common.constant.DeedCard;
import io.meeds.deeds.common.constant.NoticePeriod;
import io.meeds.deeds.common.constant.RentalPaymentPeriodicity;
import io.meeds.deeds.common.constant.TenantProvisioningStatus;
import io.meeds.deeds.common.constant.TenantStatus;
import io.meeds.deeds.common.constant.TransactionStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_EMPTY)
@Relation(collectionRelation = "leases", itemRelation = "lease")
public class DeedTenantLeaseDTO {

  private long                             id;

  private long                             nftId;

  private io.meeds.deeds.common.constant.DeedCity city;

  private DeedCard                         cardType;

  private TenantStatus                     tenantStatus;

  private TenantProvisioningStatus         provisioningStatus;

  private int                              months;

  private int                              paidMonths;

  private int                              monthPaymentInProgress;

  private String                           ownerAddress;

  private String                           managerAddress;

  private RentalPaymentPeriodicity         paymentPeriodicity;

  private double                           amount;

  private double                           allDurationAmount;

  private double                           distributedAmount;

  private NoticePeriod                     noticePeriod;

  private int                              ownerMintingPercentage;

  private double                           mintingPower;

  private TransactionStatus                transactionStatus;

  private Instant                          startDate;

  private Instant                          endDate;

  private Instant                          noticeDate;

  private Instant                          paidRentsDate;

  private boolean                          confirmed;

  private boolean                          endingLease;

  private String                           endingLeaseAddress;

}
