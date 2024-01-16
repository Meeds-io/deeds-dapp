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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.deeds.api.constant;

import lombok.Getter;

public enum HubReportStatusType {

  // Not sent yet
  NONE(true, true),
  // Not valid to be sent
  INVALID(false, false),
  // Sent to WoM
  SENT(true, false),
  // Error while sending to WoM
  ERROR_SENDING(true, true),
  // Pending for rewards to be sent
  PENDING_REWARD(true, false),
  // Pending for rewards to be sent
  REWARD_TRANSACTION_ERROR(true, false),
  // Rewards sent
  REWARDED(false, false),
  // Rejected by WoM
  REJECTED(false, false);

  @Getter
  private final boolean canRefresh;

  @Getter
  private final boolean canSend;

  private HubReportStatusType(boolean canRefresh, boolean canSend) {
    this.canRefresh = canRefresh;
    this.canSend = canSend;
  }
}
