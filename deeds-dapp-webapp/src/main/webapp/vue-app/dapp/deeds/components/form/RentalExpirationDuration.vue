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
    v-model="expirationDuration"
    class="flex-grow-1 d-flex justify-space-between"
    outlined
    dense
    group>
    <deeds-button-group-item
      :selected-value="expirationDuration"
      value="ONE_DAY"
      color="secondary">
      <span class="text-ordinary-capitalize">{{ $t('deedRentingExpirationDurationOneDay') }}</span>
    </deeds-button-group-item>
    <deeds-button-group-item
      :selected-value="expirationDuration"
      value="THREE_DAYS"
      color="secondary">
      <span class="text-ordinary-capitalize">{{ $t('deedRentingExpirationDurationThreeDays') }}</span>
    </deeds-button-group-item>
    <deeds-button-group-item
      :selected-value="expirationDuration"
      value="ONE_WEEK"
      color="secondary">
      <span class="text-ordinary-capitalize">{{ $t('deedRentingExpirationDurationOneWeek') }}</span>
    </deeds-button-group-item>
    <deeds-button-group-item
      :selected-value="expirationDuration"
      value="ONE_MONTH"
      color="secondary">
      <span class="text-ordinary-capitalize">{{ $t('deedRentingExpirationDurationOneMonth') }}</span>
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
    expirationDuration: null,
  }),
  watch: {
    expirationDuration() {
      this.$emit('input', this.expirationDuration);
    },
  },
  created() {
    this.expirationDuration = this.value;
  },
};
</script>