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
package io.meeds.dapp.web.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.meeds.deeds.common.constant.TenantProvisioningStatus;
import io.meeds.deeds.common.constant.TenantStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_EMPTY)
public class DeedTenantPresentation {

  @JsonProperty("nftId")
  @EqualsAndHashCode.Exclude
  private long                     nftId;

  @JsonProperty("managerAddress")
  @EqualsAndHashCode.Exclude
  private String                   managerAddress;

  @JsonProperty("provisioningStatus")
  @EqualsAndHashCode.Exclude
  private TenantProvisioningStatus provisioningStatus;

  @JsonProperty("status")
  @EqualsAndHashCode.Exclude
  private TenantStatus             status;

  @JsonProperty("provisioningDate")
  @EqualsAndHashCode.Exclude
  private long                     provisioningDate;

}
