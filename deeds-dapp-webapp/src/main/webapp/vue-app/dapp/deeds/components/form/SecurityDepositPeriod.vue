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
      :disabled="maxValueIndex < 1"
      value="ONE_MONTH"
      color="primary">
      {{ $t('deedRentingDurationOneMonth') }}
    </deeds-button-group-item>
    <deeds-button-group-item
      :selected-value="period"
      :disabled="maxValueIndex < 2"
      value="TWO_MONTHS"
      color="primary">
      {{ $t('deedRentingDurationTwoMonths') }}
    </deeds-button-group-item>
    <deeds-button-group-item
      :selected-value="period"
      :disabled="maxValueIndex < 3"
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
    },
    maxValue: {
      type: String,
      default: null,
    },
    maxValueExclusive: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    period: null,
    maxValueIndex: 1000,
  }),
  computed: {
    selectedPeriodIndex() {
      return this.getValueIndex(this.period);
    },
  },
  watch: {
    period() {
      this.$emit('input', this.period);
    },
    maxValue() {
      this.adjustValue();
    },
    maxValueIndex() {
      if (this.selectedPeriodIndex && this.selectedPeriodIndex > this.maxValueIndex) {
        switch (this.maxValue) {
        case 'THREE_MONTHS': this.period = 'TWO_MONTHS'; break;
        case 'TWO_MONTHS': this.period = 'ONE_MONTH'; break;
        case 'ONE_MONTH': this.period = 'NO_PERIOD'; break;
        case 'NO_PERIOD': this.period = 'NO_PERIOD'; break;
        }
      }
    },
  },
  created() {
    this.period = this.value;
    this.$nextTick().then(() => this.adjustValue());
  },
  methods: {
    adjustValue() {
      if (this.maxValue) {
        let months = this.getValueIndex(this.maxValue);
        if (this.maxValueExclusive) {
          months--;
        }
        this.maxValueIndex = months;
      }
    },
    getValueIndex(value) {
      switch (value) {
      case 'NO_PERIOD': return 0;
      case 'ONE_MONTH': return 1;
      case 'TWO_MONTHS': return 2;
      case 'THREE_MONTHS': return 3;
      case 'SIX_MONTHS': return 6;
      case 'ONE_YEAR': return 12;
      }
      return 1000;
    },
  },
};
</script>