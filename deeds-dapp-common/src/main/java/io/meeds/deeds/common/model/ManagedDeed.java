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
 *
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
import io.meeds.deeds.common.constant.TenantProvisioningStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_EMPTY)
@Relation(collectionRelation = "deeds", itemRelation = "deed")
public class ManagedDeed {

  private long                                    nftId;

  private io.meeds.deeds.common.constant.DeedCity city;

  private DeedCard                                cardType;

  private TenantProvisioningStatus                provisioningStatus;

  private String                                  ownerAddress;

  private String                                  managerAddress;

  private Instant                                 startDate;

  private Instant                                 endDate;

  private boolean                                 connected;

  public long getMaxUsers() {
    return cardType == null ? 0l : cardType.getMaxUsers();
  }

  public double getMintingPower() {
    return cardType == null ? 0d : cardType.getMintingPower();
  }

}
