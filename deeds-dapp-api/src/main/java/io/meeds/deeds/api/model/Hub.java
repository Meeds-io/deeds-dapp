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
package io.meeds.deeds.api.model;

import java.time.Instant;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@JsonInclude(value = Include.NON_EMPTY)
@Relation(collectionRelation = "hubs", itemRelation = "hub")
public class Hub {

  @Getter
  @Setter
  private long                deedId = -1;

  @Getter
  @Setter
  private short               city   = -1;

  @Getter
  @Setter
  private short               type   = -1;

  @Getter
  private String              address;

  @Getter
  @Setter
  private Map<String, String> name;

  @Getter
  @Setter
  private Map<String, String> description;

  @Getter
  @Setter
  private String              url;

  @Getter
  @Setter
  private String              color;

  @Getter
  private String              ownerAddress;

  @Getter
  private String              deedManagerAddress;

  @Getter
  private String              earnerAddress;

  @Getter
  @Setter
  private Instant             createdDate;

  @Getter
  @Setter
  private Instant             updatedDate;

  // Changed by Sent Report in UEM computing engine
  @Getter
  @Setter
  private long                usersCount;

  // Changed by Sent Report in UEM computing engine
  @Getter
  @Setter
  private String              rewardsPeriodType;

  // Changed by Sent Report in UEM computing engine
  @Getter
  @Setter
  private double              rewardsPerPeriod;

  public Hub(long deedId, // NOSONAR
             short city,
             short type,
             String address,
             Map<String, String> name,
             Map<String, String> description,
             String url,
             String color,
             String ownerAddress,
             String deedManagerAddress,
             String earnerAddress,
             Instant createdDate,
             Instant updatedDate,
             long usersCount,
             String rewardsPeriodType,
             double rewardsPerPeriod) {
    this.deedId = deedId;
    this.city = city;
    this.type = type;
    this.address = StringUtils.lowerCase(address);
    this.name = name;
    this.description = description;
    this.url = url;
    this.color = color;
    this.ownerAddress = StringUtils.lowerCase(ownerAddress);
    this.deedManagerAddress = StringUtils.lowerCase(deedManagerAddress);
    this.earnerAddress = StringUtils.lowerCase(earnerAddress);
    this.createdDate = createdDate;
    this.updatedDate = updatedDate;
    this.usersCount = usersCount;
    this.rewardsPeriodType = rewardsPeriodType;
    this.rewardsPerPeriod = rewardsPerPeriod;
  }

  public void setAddress(String address) {
    this.address = StringUtils.lowerCase(address);
  }

  public void setOwnerAddress(String ownerAddress) {
    this.ownerAddress = StringUtils.lowerCase(ownerAddress);
  }

  public void setDeedManagerAddress(String deedManagerAddress) {
    this.deedManagerAddress = StringUtils.lowerCase(deedManagerAddress);
  }

  public void setEarnerAddress(String earnerAddress) {
    this.earnerAddress = StringUtils.lowerCase(earnerAddress);
  }

}
