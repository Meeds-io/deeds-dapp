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
package io.meeds.deeds.utils;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.meeds.deeds.constant.NoticePeriod;
import io.meeds.deeds.constant.TenantProvisioningStatus;
import io.meeds.deeds.constant.TenantStatus;
import io.meeds.deeds.constant.TransactionStatus;
import io.meeds.deeds.elasticsearch.model.DeedTenant;
import io.meeds.deeds.elasticsearch.model.DeedTenantLease;
import io.meeds.deeds.model.DeedTenantLeaseDTO;
import io.meeds.deeds.model.DeedTenantOfferDTO;

public class DeedTenantLeaseMapper {

  public static final Instant MAX_DATE_VALUE = Instant.ofEpochSecond(165241780471l);

  public static final String  EVERYONE       = "ALL";

  private DeedTenantLeaseMapper() {
    // Class with Static methods
  }

  public static DeedTenantLeaseDTO toDTO(DeedTenantLease deedTenantLease,
                                         TenantStatus tenantStatus,
                                         TenantProvisioningStatus provisioningStatus) {
    if (deedTenantLease == null) {
      return null;
    }
    boolean confirmed = deedTenantLease.isConfirmed();
    Instant endDate = confirmed ? deedTenantLease.getEndDate() : null;
    Instant startDate = confirmed ? deedTenantLease.getStartDate() : null;

    int noticePeriod = deedTenantLease.getNoticePeriod();
    return new DeedTenantLeaseDTO(deedTenantLease.getId(),
                                  deedTenantLease.getNftId(),
                                  deedTenantLease.getCity(),
                                  deedTenantLease.getCardType(),
                                  tenantStatus,
                                  provisioningStatus,
                                  deedTenantLease.getMonths(),
                                  deedTenantLease.getPaidMonths(),
                                  deedTenantLease.getMonthPaymentInProgress(),
                                  deedTenantLease.getOwner(),
                                  deedTenantLease.getManager(),
                                  deedTenantLease.getPaymentPeriodicity(),
                                  deedTenantLease.getAmount(),
                                  deedTenantLease.getAllDurationAmount(),
                                  deedTenantLease.getDistributedAmount(),
                                  NoticePeriod.fromMonths(noticePeriod),
                                  deedTenantLease.getOwnerMintingPercentage(),
                                  deedTenantLease.getMintingPower(),
                                  deedTenantLease.getTransactionStatus(),
                                  startDate,
                                  endDate,
                                  deedTenantLease.getNoticeDate(),
                                  deedTenantLease.getPaidRentsDate(),
                                  confirmed,
                                  deedTenantLease.isEndingLease(),
                                  deedTenantLease.getEndingLeaseAddress());
  }

  public static DeedTenantLease fromOffer(DeedTenantOfferDTO deedTenantOffer,
                                          DeedTenant deedTenant,
                                          String managerAddress,
                                          String managerEmail,
                                          String transactionHash) {
    if (deedTenantOffer == null || deedTenant == null) {
      return null;
    }
    List<String> pendingTransactions = StringUtils.isEmpty(transactionHash) ? Collections.emptyList()
                                                                            : Collections.singletonList(transactionHash.toLowerCase());
    TransactionStatus transactionStatus = StringUtils.isEmpty(transactionHash) ? TransactionStatus.VALIDATED
                                                                               : TransactionStatus.IN_PROGRESS;
    NoticePeriod noticePeriod = deedTenantOffer.getNoticePeriod();
    return new DeedTenantLease(deedTenantOffer.getOfferId(),
                               deedTenantOffer.getNftId(),
                               deedTenantOffer.getCity(),
                               deedTenantOffer.getCardType(),
                               (int) deedTenantOffer.getDuration().getPeriod().toTotalMonths(),
                               0,
                               noticePeriod == null ? 0 : noticePeriod.getMonths(),
                               0,
                               deedTenantOffer.getOwner(),
                               managerAddress,
                               managerEmail,
                               Arrays.asList(managerAddress, deedTenant.getOwnerAddress()),
                               deedTenantOffer.getPaymentPeriodicity(),
                               deedTenantOffer.getAmount(),
                               deedTenantOffer.getAllDurationAmount(),
                               0d,
                               deedTenantOffer.getOwnerMintingPercentage(),
                               deedTenantOffer.getMintingPower(),
                               pendingTransactions,
                               transactionStatus,
                               Instant.now(),
                               MAX_DATE_VALUE,
                               null,
                               null,
                               Instant.now(),
                               false,
                               true,
                               false,
                               null,
                               0);
  }

}
