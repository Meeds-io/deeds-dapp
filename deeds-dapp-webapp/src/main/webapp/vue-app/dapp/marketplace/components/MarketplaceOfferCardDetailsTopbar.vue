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
    <v-list-item-action class="me-2">
      <v-btn
        small
        icon
        @click="close">
        <v-icon>fas fa-arrow-left</v-icon>
      </v-btn>
    </v-list-item-action>
    <v-list-item-content>
      <v-list-item-title>
        {{ $t('deedsMarketPlaceBackToList') }}
      </v-list-item-title>
    </v-list-item-content>
    <v-list-item-action v-if="isAuthenticatedOwner" class="me-2 ms-0">
      <v-btn
        :title="$t('deedRentEditButton')"
        icon
        @click="$root.$emit('deeds-rent-drawer', null, offer)">
        <v-icon>fas fa-edit</v-icon>
      </v-btn>
    </v-list-item-action>
    <v-list-item-action v-if="offer" class="me-2 ms-0">
      <v-btn
        :title="$t('deedsMarketPlaceCopyOfferLink')"
        icon
        @click="copyLink">
        <v-icon>fas fa-clone</v-icon>
      </v-btn>
    </v-list-item-action>
    <v-list-item-action class="ms-0">
      <v-btn
        :title="$t('deedsMarketPlaceBackToList')"
        icon
        @click="close">
        <v-icon>fas fa-times</v-icon>
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
    authenticated: state => state.authenticated,
    isOwner() {
      return this.address && this.offer?.owner?.toLowerCase() === this.address?.toLowerCase();
    },
    isAuthenticatedOwner() {
      return this.authenticated && this.isOwner;
    },
  }),
  methods: {
    close() {
      this.$store.commit('setStandaloneOfferId', null);
    },
    copyLink() {
      try {
        navigator.clipboard.writeText(window.location.href);
        this.$root.$emit('alert-message', this.$t('deedsOfferPermanentLinkCopied'), 'success');
      } catch (e) {
        this.$root.$emit('alert-message', this.$t('navigatorDoesntAllowCopy'), 'warning');
      }
    },
  },
};
</script>