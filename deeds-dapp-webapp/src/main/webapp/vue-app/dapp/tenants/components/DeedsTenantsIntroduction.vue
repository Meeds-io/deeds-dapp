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
    <v-card-title class="py-0 justify-center flex-nowrap">
      <span v-if="hasTenants" class="col-12 col-lg-8 col-md-7 ps-0 text-sm-h2 display-2 font-weight-bold text-center text-md-start">{{ $t('dapp.tenants.tenantsListTitle') }}</span>
      <span v-else class="col-12 col-lg-8 col-md-7 ps-0 text-sm-h2 display-2 font-weight-bold text-center text-md-start">{{ $t('dapp.tenants.tenantsListTitleWhenNoLeases') }}</span>
      <v-spacer />
      <v-img 
        :src="`${parentLocation}/static/images/tenants_banner.png`"
        max-width="326px"
        class="hidden-sm-and-down"
        alt=""
        contain
        eager />
    </v-card-title>
    <div v-if="hasTenants" class="align-self-end mt-6">
      <v-btn
        height="36px"
        class="px-4 mt-4 rounded-pill"
        :color="collapsed && 'primary'"
        :class="!collapsed && 'primary'"
        :dark="!collapsed"
        outlined
        @click="changeCollapsedTextVisibility">
        <span v-if="collapsed">{{ $t('dapp.tenants.seeMore') }}</span>
        <span v-else>{{ $t('dapp.tenants.seeLess') }}</span>
      </v-btn>
    </div>
    <div v-show="!collapsed || !hasTenants">
      <div class="d-flex flex-column flex-md-row pb-6 my-16">
        <div class="d-flex flex-column my-auto me-7">
          <span class="display-1 dark-grey-color font-weight-bold">{{ $t('dapp.tenants.rentFromMarketplace.title') }}</span>
          <span class="mt-10 mb-5 mb-md-0 text-h5 dark-grey-color font-weight-light">{{ $t('dapp.tenants.rentFromMarketplace.description') }}</span>
          <div v-if="!hasTenants" class="mb-7">
            <v-btn
              :href="`${parentLocation}/marketplace`"
              height="36px"
              class="px-4 mt-4 rounded-pill"
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
          class="ms-0 ms-sm-auto me-0 me-sm-auto"
          height="350px"
          loop="true"
          autoplay
          muted
          controls>
          <source :src="`${beTenantVideoLink}`" type="video/mp4">
        </video>
        <div class="d-flex flex-column my-auto mx-0 mx-md-14">
          <span class="display-1 dark-grey-color font-weight-bold">{{ $t('dapp.tenants.manageYourHub.title') }}</span>
          <span class="text-h5 dark-grey-color font-weight-light mt-10 mb-5 mb-md-0">{{ $t('dapp.tenants.manageYourHub.description') }}</span>
          <div class="mb-7">
            <v-btn
              :href="`${parentLocation}/tour`"
              height="36px"
              class="px-4 mt-4 rounded-pill"
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
    openMarketplace(event) {
      if (event?.target?.tagName?.toLowerCase() === 'a') {
        this.$store.commit('setStandaloneOfferId', null);
        this.$store.commit('setOfferId', null);
        this.$root.$emit('switch-page', 'marketplace');
      }
    },
    changeCollapsedTextVisibility() {
      this.collapsed= !this.collapsed;
    }
  },
};
</script>