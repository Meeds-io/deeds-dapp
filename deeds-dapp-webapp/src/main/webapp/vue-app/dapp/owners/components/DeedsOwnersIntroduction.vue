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
    <v-card-title class="justify-center flex-nowrap ps-4 ps-sm-2">
      <div class="d-flex flex-column col-12 col-lg-8 col-md-7 pa-0">
        <span class="text-sm-h3 display-1 font-weight-bold text-center text-sm-start">{{ $t('page.owners.title') }}</span>
        <span class="display-1 hidden-sm-and-down">{{ $t('page.owners.subtitle') }}</span>
      </div>
      <v-spacer class="hidden-sm-and-down" />
      <v-img 
        :src="`${parentLocation}/static/images/owners_banner.webp`"
        max-width="322px"
        class="hidden-sm-and-down"
        alt=""
        contain
        eager />
    </v-card-title>
    <v-progress-linear
      v-if="loading"
      class="mt-8"
      indeterminate />
    <div v-else>
      <div v-if="hasTenants" class="d-flex justify-end mt-6">
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
      <div v-if="!collapsed || !hasTenants">
        <div class="d-flex flex-column flex-md-row pb-6 my-16">
          <div class="d-flex flex-column me-7 ps-4 ps-sm-2">
            <span class="headline text-sm-h4 font-weight-bold">{{ $t('dapp.owners.mintOrBuyDeed.title') }}</span>
            <span class="dark-grey--text mt-5 mb-5 mb-md-0 text-h6 font-weight-light">{{ $t('dapp.owners.mintOrBuyDeed.descriptionPart1') }}</span>
            <div class="mb-7">
              <v-btn
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
            <span class="headline text-sm-h4 font-weight-bold">{{ $t('dapp.owners.useOrRentDeed.title') }}</span>
            <span class="dark-grey--text text-h6 font-weight-light mt-10 mb-5 mb-md-0">{{ $t('dapp.owners.useOrRentDeed.descriptionPart1') }}</span>
            <span class="dark-grey--text text-h6 font-weight-light mt-10 mb-5 mb-md-0">{{ $t('dapp.owners.useOrRentDeed.descriptionPart2') }}</span>
            <div class="mb-7">
              <v-btn
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
    openDeeds(event) {
      if (event?.target?.tagName?.toLowerCase() === 'a') {
        this.$root.$emit('switch-page', 'mint');
      }
    },
    changeCollapsedTextVisibility() {
      this.collapsed= !this.collapsed;
    }
  },
};
</script>