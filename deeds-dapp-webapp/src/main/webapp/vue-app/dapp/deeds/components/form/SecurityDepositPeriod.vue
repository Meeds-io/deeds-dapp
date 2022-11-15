<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <v-btn-toggle
    v-model="period"
    class="flex-grow-1 d-flex justify-space-between"
    mandatory
    outlined
    dense
    group>
    <deeds-button-group-item
      :selected-value="period"
      value="NO_PERIOD"
      color="primary">
      {{ $t('deedRentingDurationNoSecurityDeposit') }}
    </deeds-button-group-item>
    <deeds-button-group-item
      :selected-value="period"
      value="ONE_MONTH"
      color="primary">
      {{ $t('deedRentingDurationOneMonth') }}
    </deeds-button-group-item>
    <deeds-button-group-item
      :selected-value="period"
      value="TWO_MONTHS"
      color="primary">
      {{ $t('deedRentingDurationTwoMonths') }}
    </deeds-button-group-item>
    <deeds-button-group-item
      :selected-value="period"
      value="THREE_MONTHS"
      color="primary">
      {{ $t('deedRentingDurationThreeMonths') }}
    </deeds-button-group-item>
  </v-btn-toggle>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    }
  },
  data: () => ({
    period: null,
  }),
  watch: {
    period() {
      this.$emit('input', this.period);
    },
  },
  created() {
    this.period = this.value;
  },
};
</script>