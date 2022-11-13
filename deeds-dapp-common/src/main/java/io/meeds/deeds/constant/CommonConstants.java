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
package io.meeds.deeds.constant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CommonConstants {

  private CommonConstants() {
    // Static elements only, not instantiable
  }

  public static final String DEED_OWNERSHIP_TRANSFERRED_BLOCKCHAIN_EVENT = "deed.event.transferOwnershipFromBlockchain";

  public static final String DEED_TENANT_OWNERSHIP_TRANSFERRED_EVENT     = "deed.event.transferOwnershipFromDeedTenant";

  @NoArgsConstructor
  @AllArgsConstructor
  @Data
  public static class DeedOwnershipTransferEvent {

    private long   nftId;

    private String from;

    private String to;

  }
}
