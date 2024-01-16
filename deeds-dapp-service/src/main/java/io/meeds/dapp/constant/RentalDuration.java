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
package io.meeds.dapp.constant;

import java.time.Period;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum RentalDuration {

  ONE_MONTH(Period.ofMonths(1), 1),
  THREE_MONTHS(Period.ofMonths(3), 3),
  SIX_MONTHS(Period.ofMonths(6), 6),
  ONE_YEAR(Period.ofYears(1), 12),
  OTHER(null, 0);

  @Getter
  private Period period;

  @Getter
  private int    months;

  public static RentalDuration fromMonths(int months) {
    switch (months) {
    case 1:
      return ONE_MONTH;
    case 3:
      return THREE_MONTHS;
    case 6:
      return SIX_MONTHS;
    case 12:
      return ONE_YEAR;
    default:
      return OTHER;
    }
  }

}
