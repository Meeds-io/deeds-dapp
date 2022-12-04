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
  <v-list-item class="px-0">
    <v-list-item-action class="me-2" @click="close">
      <v-tooltip
        z-index="4"
        bottom>
        <template #activator="{on, attrs}">
          <v-btn
            class="mr-n2px"
            icon
            v-bind="attrs"
            v-on="on"
            @click="close">
            <v-icon>fas fa-arrow-left</v-icon>
          </v-btn>
        </template>
        <span class="text-no-wrap">{{ $t('deedsMarketPlaceBackToList') }}</span>
      </v-tooltip>
    </v-list-item-action>
    <v-list-item-content>
      <v-list-item-title class="d-flex">
        <v-tooltip
          z-index="4"
          bottom>
          <template #activator="{on, attrs}">
            <v-card
              class="flex-grow-0"
              color="transparent"
              flat
              v-bind="attrs"
              v-on="on"
              @click="close">
              {{ $t('deedsMarketPlaceBackToList') }}
            </v-card>
          </template>
          <span class="text-no-wrap">{{ $t('deedsMarketPlaceBackToList') }}</span>
        </v-tooltip>
      </v-list-item-title>
    </v-list-item-content>
    <template v-if="!invalidOffer">
      <v-list-item-action v-if="isOwner" class="me-2 ms-0 d-flex flex-row align-center">
        <v-tooltip
          z-index="4"
          bottom>
          <template #activator="{on, attrs}">
            <v-btn
              color="amber lighten-2"
              class="me-2"
              icon
              v-on="on"
              v-bind="attrs">
              <v-icon class="mt-1" size="22">fa-crown</v-icon>
            </v-btn>
          </template>
          <span>{{ $t('deedsOfferOwner') }}</span>
        </v-tooltip>
        <v-tooltip
          v-if="!isOfferChangeLog"
          z-index="4"
          max-width="300px"
          bottom>
          <template #activator="{on, attrs}">
            <v-card
              color="transparent"
              flat
              v-bind="attrs"
              v-on="on">
              <v-badge
                :value="showEditOfferBadge"
                :icon="editOfferBadgeIcon"
                :color="editOfferBadgeIconColor"
                class="full-width"
                bottom
                left
                overlap>
                <v-btn
                  :disabled="disabledEdit"
                  color="primary"
                  icon
                  @click="$root.$emit('deeds-rent-drawer', null, offer)">
                  <v-icon>fas fa-edit</v-icon>
                </v-btn>
              </v-badge>
            </v-card>
          </template>
          <span>{{ rentingEditTooltip }}</span>
        </v-tooltip>
      </v-list-item-action>
      <v-list-item-action class="me-2 ms-0">
        <v-tooltip
          z-index="4"
          max-width="300px"
          bottom>
          <template #activator="{on, attrs}">
            <v-btn
              icon
              v-bind="attrs"
              v-on="on"
              @click="copyLink">
              <v-icon>fas fa-clone</v-icon>
            </v-btn>
          </template>
          <span>{{ $t('deedsMarketPlaceCopyOfferLink') }}</span>
        </v-tooltip>
      </v-list-item-action>
    </template>
    <v-list-item-action class="ms-0">
      <v-tooltip
        z-index="4"
        max-width="300px"
        bottom>
        <template #activator="{on, attrs}">
          <v-btn
            class="mr-n2px"
            icon
            v-bind="attrs"
            v-on="on"
            @click="close">
            <v-icon>fas fa-times</v-icon>
          </v-btn>
        </template>
        <span>{{ $t('deedsMarketPlaceBackToList') }}</span>
      </v-tooltip>
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
    invalidOffer: {
      type: Boolean,
      default: false,
    },
  },
  computed: Vuex.mapState({
    address: state => state.address,
    isOwner() {
      return this.address && this.offer?.owner?.toLowerCase() === this.address?.toLowerCase();
    },
    offerId() {
      return this.offer?.offerId;
    },
    isDeleteInProgress() {
      return this.offer?.deleteId;
    },
    isUpdateInProgress() {
      return this.offer?.updateId;
    },
    isCreateInProgress() {
      return this.offer && !this.offerId || false;
    },
    isOfferChangeLog() {
      return this.offer && this.offer.parentId || false;
    },
    acquisitionsCount() {
      return this.offer?.acquisitionIds?.length || 0;
    },
    isAcquisitionInProgress() {
      return this.acquisitionsCount > 0;
    },
    showEditOfferBadge() {
      return this.isAcquisitionInProgress || this.disabledEdit;
    },
    editOfferBadgeIcon() {
      if (this.isCreateInProgress) {
        return 'fas fa-info mt-n2px';
      }
      return this.isAcquisitionInProgress
        && 'fas fa-cart-shopping mt-n2px'
        || 'fas fa-lock mt-n2px';
    },
    editOfferBadgeIconColor() {
      if (this.isCreateInProgress) {
        return 'info';
      }
      return this.isAcquisitionInProgress
        && 'warning'
        || 'error';
    },
    disabledEdit() {
      return !this.isOwner
        || this.isCreateInProgress
        || this.isUpdateInProgress
        || this.isDeleteInProgress
        || this.isAcquisitionInProgress
        || this.isOfferChangeLog;
    },
    rentingEditTooltip() {
      if (this.isCreateInProgress) {
        return this.$t('deedRentingOfferCreationInProgressTooltip');
      }
      if (this.isAcquisitionInProgress) {
        return this.$t('deedOfferAcquisitionInProgress', {0: this.acquisitionsCount});
      }
      if (this.isDeleteInProgress) {
        return this.$t('deedRentingOfferDeletionInProgress');
      }
      if (this.isUpdateInProgress) {
        return this.$t('deedRentingOfferUpdateInProgress');
      }
      return this.$t('deedRentEditDescription');
    },
  }),
  methods: {
    close() {
      this.$store.commit('setStandaloneOfferId', null);
    },
    copyLink() {
      this.$utils.copyToClipboard(window.location.href);
    },
  },
};
</script>