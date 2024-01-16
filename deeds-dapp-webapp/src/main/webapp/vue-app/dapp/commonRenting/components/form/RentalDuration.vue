<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
    v-model="duration"
    class="flex-grow-1 d-flex justify-space-between"
    mandatory
    outlined
    dense
    group>
    <deeds-button-group-item
      :selected-value="duration"
      value="ONE_MONTH"
      color="primary">
      {{ $t('deedRentingDurationOneMonth') }}
    </deeds-button-group-item>
    <deeds-button-group-item
      :selected-value="duration"
      value="THREE_MONTHS"
      color="primary">
      {{ $t('deedRentingDurationThreeMonths') }}
    </deeds-button-group-item>
    <deeds-button-group-item
      :selected-value="duration"
      value="SIX_MONTHS"
      color="primary">
      {{ $t('deedRentingDurationSixMonths') }}
    </deeds-button-group-item>
    <deeds-button-group-item
      :selected-value="duration"
      value="ONE_YEAR"
      color="primary">
      {{ $t('deedRentingDurationOneYear') }}
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
    duration: null,
  }),
  watch: {
    duration() {
      this.$emit('input', this.duration);
    },
  },
  created() {
    this.duration = this.value;
  },
};
</script>