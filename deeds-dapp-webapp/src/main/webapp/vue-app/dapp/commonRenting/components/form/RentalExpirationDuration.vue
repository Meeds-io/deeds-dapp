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
    v-model="expirationDuration"
    class="flex-grow-1 d-flex flex-wrap justify-space-between"
    mandatory
    outlined
    dense
    group>
    <deeds-button-group-item
      :selected-value="expirationDuration"
      value="NO_EXPIRATION"
      color="primary"
      class="col-6 d-flex justify-center">
      {{ $t('deedRentingExpirationDurationNoExpiration') }}
    </deeds-button-group-item>
    <deeds-button-group-item
      :selected-value="expirationDuration"
      value="THREE_DAYS"
      color="primary"
      class="col-6 d-flex justify-center">
      {{ $t('deedRentingExpirationDurationThreeDays') }}
    </deeds-button-group-item>
    <deeds-button-group-item
      :selected-value="expirationDuration"
      value="ONE_WEEK"
      color="primary"
      class="col-6 d-flex justify-center">
      {{ $t('deedRentingExpirationDurationOneWeek') }}
    </deeds-button-group-item>
    <deeds-button-group-item
      :selected-value="expirationDuration"
      value="ONE_MONTH"
      color="primary"
      class="col-6 d-flex justify-center">
      {{ $t('deedRentingExpirationDurationOneMonth') }}
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
      if (this.expirationDuration === 'NO_EXPIRATION') {
        this.$emit('input', null);
      } else {
        this.$emit('input', this.expirationDuration);
      }
    },
  },
  created() {
    if (!this.value) {
      this.expirationDuration = 'NO_EXPIRATION';
    } else {
      this.expirationDuration = this.value;
    }
  },
};
</script>