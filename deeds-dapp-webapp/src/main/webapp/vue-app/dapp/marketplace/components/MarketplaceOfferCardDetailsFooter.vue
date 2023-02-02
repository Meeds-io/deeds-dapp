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
    <v-list-item-action-text v-if="isRestricted" class="d-flex py-0 me-0 me-sm-8 subtitle-1">
      <span class="error--text">{{ $t('deedsOfferIsRestricted') }}</span>
    </v-list-item-action-text>
    <v-list-item-action-text v-else-if="hasExpired" class="d-flex py-0 me-0 me-sm-8 subtitle-1">
      <span class="error--text">{{ $t('deedsOfferRentingExpired') }}</span>
    </v-list-item-action-text>
    <template v-else-if="hasExpirationTime">
      <v-list-item-action-text class="d-flex py-0 subtitle-1">
        <span class="mx-4">{{ $t('deedsOfferRentingExpiresWithin') }}</span>
      </v-list-item-action-text>
      <v-list-item-action-text class="d-flex py-0 me-0 me-sm-8 subtitle-1">
        <v-icon :color="blackThemeColor" size="16">fas fa-stopwatch</v-icon>
        <deeds-timer
          :end-time="expirationTime"
          class="ms-sm-1"
          text-color=""
          short-format />
      </v-list-item-action-text>
    </template>
    <v-list-item-action v-if="!hasExpired" class="mx-0 align-self-center align-self-sm-end">
      <div v-if="metamaskOffline" class="d-flex flex-grow-1 flex-shrink-0">
        <deeds-metamask-button class="ma-auto" />
      </div>
      <v-btn
        v-else
        :disabled="disabledRentButton"
        color="primary"
        @click="openOfferRentingDrawer">
        <span class="subtitle-1 font-weight-medium">{{ $t('deedsOfferRentingButton') }}</span>
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
    address: state => state.address,
    metamaskOffline: state => state.metamaskOffline,
    now: state => state.now,
    blackThemeColor: state => state.blackThemeColor,
    hasExpirationTime() {
      return !!this.offer?.expirationDate;
    },
    expirationTime() {
      return this.offer?.expirationDate && new Date(this.offer.expirationDate).getTime() || 0;
    },
    hasExpired() {
      return this.hasExpirationTime && this.expirationTime < this.now;
    },
    isRestricted() {
      return this.offer?.hostAddress && this.offer?.hostAddress.toLowerCase() !== this.address?.toLowerCase();
    },
    isConfirmed() {
      return this.offer?.offerId;
    },
    isUpdating() {
      return this.offer?.updateId;
    },
    isDeleting() {
      return this.offer?.deleteId;
    },
    isOwner() {
      return this.address && this.offer?.owner?.toLowerCase() === this.address?.toLowerCase();
    },
    displayActions() {
      return this.metamaskOffline || !this.isOwner;
    },
    acquisitionInProgress() {
      return this.offer?.acquisitionIds?.length || 0;
    },
    isAcquisitionInProgress() {
      return this.acquisitionInProgress > 0;
    },
    disabledRentButton() {
      return this.isRestricted
        || !this.isConfirmed
        || this.isOwner
        || this.hasExpired
        || this.isAcquisitionInProgress
        || this.isDeleting;
    },
  }),
  methods: {
    openOfferRentingDrawer() {
      this.$root.$emit('deeds-rent-offer-drawer', this.offer);
    },
  },
};
</script>