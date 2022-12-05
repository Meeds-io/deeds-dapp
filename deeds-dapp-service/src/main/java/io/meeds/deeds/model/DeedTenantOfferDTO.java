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

import java.time.Instant;
import java.util.Set;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.meeds.deeds.constant.DeedCard;
import io.meeds.deeds.constant.ExpirationDuration;
import io.meeds.deeds.constant.NoticePeriod;
import io.meeds.deeds.constant.OfferType;
import io.meeds.deeds.constant.RentalDuration;
import io.meeds.deeds.constant.RentalPaymentPeriodicity;
import io.meeds.deeds.constant.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_EMPTY)
@Relation(collectionRelation = "offers", itemRelation = "offer")
public class DeedTenantOfferDTO {

  private String                           id;

  private long                             offerId;

  private long                             nftId;

  private io.meeds.deeds.constant.DeedCity city;

  private DeedCard                         cardType;

  private String                           owner;

  private String                           hostAddress;

  private String                           description;

  private double                           amount;

  private double                           allDurationAmount;

  private OfferType                        offerType;

  private ExpirationDuration               expirationDuration;

  private int                              expirationDays;

  private RentalDuration                   duration;

  private int                              months;

  private NoticePeriod                     noticePeriod;

  private int                              noticePeriodMonths;

  private RentalPaymentPeriodicity         paymentPeriodicity;

  private int                              ownerMintingPercentage;

  private double                           mintingPower;

  private String                           offerTransactionHash;

  private TransactionStatus                offerTransactionStatus;

  private Instant                          startDate;

  private Instant                          expirationDate;

  private Instant                          createdDate;

  private Instant                          modifiedDate;

  private boolean                          enabled;

  private String                           parentId;

  private Set<String>                      acquisitionIds;

  private String                           updateId;

  private String                           deleteId;

}
