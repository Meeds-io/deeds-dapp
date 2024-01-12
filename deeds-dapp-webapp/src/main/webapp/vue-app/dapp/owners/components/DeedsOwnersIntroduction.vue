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
        {{ $t('page.owners.title') }}
      </template>
      <template #subtitle>
        {{ $t('page.owners.subtitle') }}
      </template>
    </deeds-page-title-layout>
    <v-progress-linear
      v-if="loading"
      class="mt-8"
      indeterminate />
    <div v-else>
      <div v-if="hasTenants" class="d-flex justify-end pt-4 pt-md-11">
        <v-btn
          height="40px"
          class="px-4 mt-2"
          :color="collapsed && 'primary'"
          :class="!collapsed && 'primary'"
          :dark="!collapsed"
          outlined
          @click="changeCollapsedTextVisibility">
          <span v-if="collapsed">{{ $t('dapp.seeMore') }}</span>
          <span v-else>{{ $t('dapp.seeLess') }}</span>
        </v-btn>
      </div>
      <div v-if="!collapsed || !hasTenants">
        <div class="d-flex flex-column flex-md-row pb-6 mb-16 mt-4 mt-md-13">
          <div class="d-flex flex-column me-7 ps-4 ps-sm-2">
            <span class="headline font-weight-bold">{{ $t('dapp.owners.mintOrBuyDeed.title') }}</span>
            <span class="dark-grey--text mt-5 mb-5 mb-md-0 text-h6 font-weight-light">{{ $t('dapp.owners.mintOrBuyDeed.descriptionPart1') }}</span>
            <div class="mb-7">
              <v-btn
                id="oMint-button"
                :href="mintUrl"
                height="40px"
                class="px-4 mt-4"
                color="primary"
                outlined>
                <span>{{ $t('dapp.owners.mintDeed.button') }}</span>
              </v-btn>
            </div>
            <span class="dark-grey--text mb-5 mb-md-0 text-h6 font-weight-light">{{ $t('dapp.owners.mintOrBuyDeed.descriptionPart2') }}</span>
            <div class="mb-7">
              <v-btn
                id="oBuy-button"
                :href="`${openSeaLink}`"
                target="_blank"
                height="40px"
                class="px-4 mt-4"
                color="primary"
                outlined>
                <span>{{ $t('dapp.owners.buyDeed.button') }}</span>
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
            <source :src="`${mintDeedVideoLink}`" type="video/mp4">
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
            <source :src="`${rentDeedVideoLink}`" type="video/mp4">
          </video>
          <div class="d-flex flex-column mx-0 mx-md-14">
            <span class="headline font-weight-bold">{{ $t('dapp.owners.useOrRentDeed.title') }}</span>
            <span class="dark-grey--text text-h6 font-weight-light mt-10 mb-5 mb-md-0">{{ $t('dapp.owners.useOrRentDeed.descriptionPart1') }}</span>
            <span class="dark-grey--text text-h6 font-weight-light mt-10 mb-5 mb-md-0">{{ $t('dapp.owners.useOrRentDeed.descriptionPart2') }}</span>
            <div class="mb-7">
              <v-btn
                id="oOffers-button"
                :href="marketplaceURL"
                height="40px"
                class="px-4 mt-4"
                color="primary"
                outlined>
                <span>{{ $t('dapp.owners.useOrRentDeed.button') }}</span>
              </v-btn>
            </div>
          </div>
        </div>
      </div>
    </div>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    showIntroduction: false,
    hasTenants: false,
    loading: true,
    collapsed: true,
  }),
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    openSeaCollectionLink: state => state.openSeaCollectionLink,
    mintDeedVideoLink: state => state.mintDeedVideoLink,
    rentDeedVideoLink: state => state.rentDeedVideoLink,
    marketplaceURL: state => state.marketplaceURL,
    mintUrl: state => state.mintUrl,
    dark: state => state.dark,
    openSeaLink() {
      return this.openSeaCollectionLink || 'javascript:void(0)';
    },
    openSeaTarget() {
      return this.openSeaCollectionLink && '_blank' || '';
    },
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
      this.showIntroduction = true;
      window.setTimeout(() => this.loading = false, 500);
    },
    changeCollapsedTextVisibility() {
      this.collapsed= !this.collapsed;
    }
  },
};
</script>