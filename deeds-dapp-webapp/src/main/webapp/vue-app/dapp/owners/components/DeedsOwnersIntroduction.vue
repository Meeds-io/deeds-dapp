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
      <div>
        <span v-if="hasTenants" class="col-12 col-lg-8 col-md-7 ps-0 text-sm-h2 display-2 font-weight-bold text-center text-md-start">{{ $t('dapp.owners.ownersListTitle') }}</span>
        <span v-else class="col-12 col-lg-8 col-md-7 ps-0 text-sm-h2 display-2 font-weight-bold text-center text-md-start">{{ $t('dapp.owners.ownersListTitleWhenNoLeases') }}</span>
      </div>
      <v-spacer />
      <v-img 
        :src="`${parentLocation}/static/images/owners_banner.png`"
        max-width="322px"
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
        <span v-if="collapsed">{{ $t('dapp.seeMore') }}</span>
        <span v-else>{{ $t('dapp.seeLess') }}</span>
      </v-btn>
    </div>
    <div v-show="!collapsed || !hasTenants">
      <div class="d-flex flex-column flex-md-row pb-6 my-16">
        <div class="d-flex flex-column me-7">
          <span class="display-1 dark-grey-color font-weight-bold">{{ $t('dapp.owners.mintOrBuyDeed.title') }}</span>
          <span class="mt-5 mb-5 mb-md-0 text-h5 dark-grey-color font-weight-light">{{ $t('dapp.owners.mintOrBuyDeed.descriptionPart1') }}</span>
          <div class="mb-7">
            <v-btn
              :href="`${parentLocation}/deeds`"
              height="36px"
              class="px-4 mt-4 rounded-pill"
              color="primary"
              outlined>
              <span>{{ $t('dapp.owners.mintDeed.button') }}</span>
            </v-btn>
          </div>
          <span class="mb-5 mb-md-0 text-h5 dark-grey-color font-weight-light">{{ $t('dapp.owners.mintOrBuyDeed.descriptionPart2') }}</span>
          <div class="mb-7">
            <v-btn
              :href="`${openSeaLink}`"
              target="_blank"
              height="36px"
              class="px-4 mt-4 rounded-pill"
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
          class="ms-0 ms-sm-auto me-0 me-sm-auto"
          height="350px"
          loop="true"
          autoplay
          muted
          controls>
          <source :src="`${rentDeedVideoLink}`" type="video/mp4">
        </video>
        <div class="d-flex flex-column mx-0 mx-md-14">
          <span class="display-1 dark-grey-color font-weight-bold">{{ $t('dapp.owners.useOrRentDeed.title') }}</span>
          <span class="text-h5 dark-grey-color font-weight-light mt-10 mb-5 mb-md-0">{{ $t('dapp.owners.useOrRentDeed.descriptionPart1') }}</span>
          <span class="text-h5 dark-grey-color font-weight-light mt-10 mb-5 mb-md-0">{{ $t('dapp.owners.useOrRentDeed.descriptionPart2') }}</span>
          <div class="mb-7">
            <v-btn
              :href="`${parentLocation}/marketplace`"
              height="36px"
              class="px-4 mt-4 rounded-pill"
              color="primary"
              outlined>
              <span>{{ $t('dapp.owners.useOrRentDeed.button') }}</span>
            </v-btn>
          </div>
        </div>
      </div>
    </div>
    <!--<v-card-text v-show="showIntroduction" class="px-0 pt-4">
      {{ $t('deedsOwnersCommunityIntroductionPart1') }}
      <ul class="mt-4">
        <ol
          v-html="$t('deedsOwnersCommunityIntroductionPart2', {0: `<a href='${parentLocation}/deeds' class='secondary--text'>`, 1: `</a>`})"
          class="ps-0 ps-sm-4"
          @click.prevent.stop="openDeeds">
        </ol>
        <ol
          v-html="$t('deedsOwnersCommunityIntroductionPart3', {0: `<a href='${openSeaLink}' class='secondary--text' title='${$t('sellOnOpenSea')}' target='${openSeaTarget}' rel='nofollow noreferrer noopener'>`, 1: `</a>`})"
          class="ps-0 ps-sm-4">
        </ol>
      </ul>
    </v-card-text>-->
    <v-progress-linear v-if="loading" indeterminate />
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
      //this.hasTenants = false;
      this.showIntroduction = true;
      window.setTimeout(() => this.loading = false, 500);
    },
    openDeeds(event) {
      if (event?.target?.tagName?.toLowerCase() === 'a') {
        this.$root.$emit('switch-page', 'deeds');
      }
    },
    changeCollapsedTextVisibility() {
      this.collapsed= !this.collapsed;
    }
  },
};
</script>