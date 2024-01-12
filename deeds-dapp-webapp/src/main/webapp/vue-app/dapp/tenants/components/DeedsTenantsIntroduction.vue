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
  <v-card class="d-flex flex-column" flat>
    <deeds-page-title-layout>
      <template #title>
        {{ $t('page.tenants.title') }}
      </template>
      <template #subtitle>
        {{ $t('page.tenants.subtitle') }}
      </template>
    </deeds-page-title-layout>
    <div v-if="hasTenants" class="align-self-end pt-4 pt-md-11">
      <v-btn
        height="40px"
        class="px-4 mt-4"
        :color="collapsed && 'primary'"
        :class="!collapsed && 'primary'"
        :dark="!collapsed"
        outlined
        @click="changeCollapsedTextVisibility">
        <span v-if="collapsed">{{ $t('dapp.seeMore') }}</span>
        <span v-else>{{ $t('dapp.seeLess') }}</span>
      </v-btn>
    </div>
    <div v-show="!collapsed || !hasTenants">
      <div class="d-flex flex-column flex-md-row pb-6 mb-16 mt-4 mt-md-13">
        <div class="d-flex flex-column my-auto me-7 ps-4 ps-sm-2">
          <span class="headline font-weight-bold">{{ $t('dapp.tenants.rentFromMarketplace.title') }}</span>
          <span class="dark-grey--text mt-10 mb-5 mb-md-0 text-h6 font-weight-light">{{ $t('dapp.tenants.rentFromMarketplace.description') }}</span>
          <div v-if="!hasTenants" class="mb-7">
            <v-btn
              id="teOffers-button"
              :href="marketplaceURL"
              height="40px"
              class="px-4 mt-4"
              color="primary"
              outlined>
              <span>{{ $t('dapp.tenants.rentFromMarketplace.button') }}</span>
            </v-btn>
          </div>
        </div>
        <video
          class="ms-0 ms-sm-auto me-0 me-sm-auto"
          height="350px"
          loop="true"
          autoplay
          muted
          controls>
          <source :src="`${browseOffersVideoLink}`" type="video/mp4">
        </video>
      </div>
      <div class="d-flex flex-column-reverse flex-md-row py-16 mb-16">
        <video
          class="ms-0 ms-sm-auto me-0 me-sm-auto ps-4 ps-sm-2"
          height="350px"
          loop="true"
          autoplay
          muted
          controls>
          <source :src="`${beTenantVideoLink}`" type="video/mp4">
        </video>
        <div class="d-flex flex-column my-auto mx-0 mx-md-14">
          <span class="headline font-weight-bold">{{ $t('dapp.tenants.manageYourHub.title') }}</span>
          <span class="dark-grey--text text-h6 font-weight-light mt-10 mb-5 mb-md-0">{{ $t('dapp.tenants.manageYourHub.description') }}</span>
          <div class="mb-7">
            <v-btn
              id="teDiscover-button"
              :href="tourURL"
              height="40px"
              class="px-4 mt-4"
              color="primary"
              outlined>
              <span>{{ $t('dapp.tenants.manageYourHub.button') }}</span>
            </v-btn>
          </div>
        </div>
      </div>
    </div>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    hasTenants: false,
    collapsed: true,
  }),
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    browseOffersVideoLink: state => state.browseOffersVideoLink,
    beTenantVideoLink: state => state.beTenantVideoLink,
    dark: state => state.dark,
    marketplaceURL: state => state.marketplaceURL,
    tourURL: state => state.tourURL,
  }),
  created() {
    this.$root.$on('deed-leases-loaded', this.computeLeasesLength);
  },
  beforeDestroy() {
    this.$root.$off('deed-leases-loaded', this.computeLeasesLength);
  },
  methods: {
    computeLeasesLength(_leases, totalSize) {
      this.hasTenants = totalSize > 0;
    },
    changeCollapsedTextVisibility() {
      this.collapsed= !this.collapsed;
    }
  },
};
</script>