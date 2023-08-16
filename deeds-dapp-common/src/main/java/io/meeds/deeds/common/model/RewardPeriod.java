/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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
package io.meeds.deeds.common.model;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardPeriod {

  public static final DayOfWeek FIRST_DAY_OF_WEEK = DayOfWeek.MONDAY;

  public static final ZoneId    ZONE_ID           = ZoneOffset.UTC;

  private Instant               from;

  private Instant               to;

  public static RewardPeriod getCurrentPeriod() {
    return getPeriod(LocalDate.now(ZONE_ID));
  }

  public static RewardPeriod getPeriod(Instant localDate) {
    return getPeriod(getLocalDate(localDate));
  }

  public static RewardPeriod getPeriod(LocalDate localDate) {
    ZonedDateTime firstDayOfThisWeek = localDate.atStartOfDay(ZONE_ID)
                                                .with(FIRST_DAY_OF_WEEK);
    ZonedDateTime firstDayOfNextWeek = firstDayOfThisWeek.plusWeeks(1);
    return new RewardPeriod(firstDayOfThisWeek.toInstant(),
                            firstDayOfNextWeek.toInstant());
  }

  public static List<RewardPeriod> getPreviousPeriods(long offset, long limit) {
    List<RewardPeriod> periods = new ArrayList<>();
    RewardPeriod currentPeriod = RewardPeriod.getCurrentPeriod();
    for (int i = 0; i < limit; i++) {
      periods.add(new RewardPeriod(ZonedDateTime.ofInstant(currentPeriod.getFrom(), ZONE_ID)
                                                .minusWeeks(offset + i)
                                                .toInstant(),
                                   ZonedDateTime.ofInstant(currentPeriod.getTo(), ZONE_ID)
                                                .minusWeeks(offset + i)
                                                .toInstant()));
    }
    return periods;
  }

  public static LocalDate getLocalDate(Instant date) {
    return ZonedDateTime.ofInstant(date, ZONE_ID).toLocalDate();
  }

  public RewardPeriod getPreviousPeriod() {
    return new RewardPeriod(ZonedDateTime.ofInstant(from, ZONE_ID).minusWeeks(1).toInstant(),
                            ZonedDateTime.ofInstant(to, ZONE_ID).minusWeeks(1).toInstant());
  }

  public LocalDate getMedian() {
    return from.atZone(ZONE_ID).plusDays(3).toLocalDate();
  }

}
