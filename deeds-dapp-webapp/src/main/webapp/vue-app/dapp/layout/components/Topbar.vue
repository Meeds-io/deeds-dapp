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
  <v-app-bar
    id="banner"
    :color="whiteThemeColor"
    role="banner"
    fixed
    elevate-on-scroll>
    <v-spacer />
    <div class="d-flex headerLayout px-0 px-sm-7 mx-1">
      <deeds-topbar-logo />
      <deeds-navbar v-if="defaultNavigationBar" />
      <v-spacer />
      <div v-if="staticPage || buypage">
        <v-btn
          id="tbHubs-button"
          :href="hubsUrl"
          class="px-4 font-weight-black letter-spacing-normal"
          color="primary"
          outlined>
          <h4>{{ $t('explore') }}</h4>
        </v-btn>
      </div>
      <div v-else-if="homepage">
        <v-btn
          :href="buyUrl"
          class="primary-border-color px-4 letter-spacing-normal"
          color="primary"
          outlined>
          {{ $t('create') }}
        </v-btn>
      </div>
      <div v-else class="d-flex">
        <template v-if="validNetwork && address">
          <div class="ms-4 d-none d-md-inline-block">
            <deeds-topbar-address-selector v-if="address" />
          </div>
          <div class="ms-4 d-none d-md-inline-block">
            <deeds-topbar-gas-price />
          </div>
        </template>
        <div v-else-if="!appLoading" class="ms-4 d-none d-md-block">
          <deeds-metamask-button />
        </div>
        <div class="ms-4">
          <v-btn
            id="tbBuy-button"
            :href="buyMeedsLink"
            target="_blank"
            class="hidden-xs-only"
            elevation="0"
            color="secondary">
            <span class="font-size-normal">{{ $t('buyMeedsButton') }}</span>
          </v-btn>
        </div>
      </div>
    </div>
    <v-spacer />
  </v-app-bar>
</template>
<script>
export default {
  computed: Vuex.mapState({
    appLoading: state => state.appLoading,
    validNetwork: state => state.validNetwork,
    address: state => state.address,
    whiteThemeColor: state => state.whiteThemeColor,
    parentLocation: state => state.parentLocation,
    staticPage: state => state.staticPage,
    hubsUrl: state => state.hubsUrl,
    isMobile: state => state.isMobile,
    buyMeedsLink: state => state.buyMeedsLink,
    page: state => state.page,
    buyUrl: state => state.buyUrl,
    defaultNavigationBar() {
      return !this.isMobile;
    },
    homepage() {
      return this.page === 'home';
    },
    buypage() {
      return this.page === 'buy';
    }
  }),
};
</script>