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
  <v-list-item class="d-flex flex-column flex-sm-row justify-end px-0 mt-4 mt-sm-auto">
    <v-list-item-action-text v-if="expirationDate" class="d-flex py-0 subtitle-1">
      <span class="mx-4">{{ $t('deedsOfferRentingExpiresWithin') }}</span>
    </v-list-item-action-text>
    <v-list-item-action-text v-if="expirationDate" class="d-flex py-0 subtitle-1">
      <v-icon color="black" size="16">fas fa-stopwatch</v-icon>
      <deeds-timer
        :end-time="expirationDate"
        class="ms-sm-1"
        text-color=""
        short-format />
    </v-list-item-action-text>
    <v-list-item-action class="ms-0 ms-sm-8 align-self-end">
      <v-btn
        color="primary"
        @click="close">
        {{ $t('deedsOfferRentingButton') }}
      </v-btn>
    </v-list-item-action>
  </v-list-item>
</template>
<script>
export default {
  props: {
    offer: {
      type: Object,
      default: null,
    },
  },
  computed: Vuex.mapState({
    expirationDate() {
      return this.offer?.expirationDate && new Date(this.offer.expirationDate).getTime() || 0;
    },
  }),
};
</script>