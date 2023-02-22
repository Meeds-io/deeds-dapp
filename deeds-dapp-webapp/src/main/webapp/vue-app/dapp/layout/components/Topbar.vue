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
  <v-app-bar
    id="banner"
    :color="whiteThemeColor"
    role="banner"
    fixed
    elevate-on-scroll>
    <v-spacer />
    <div v-if="homeDisplay" class="d-flex headerLayout px-0 px-sm-4 mx-1">
      <img
        :src="`/${parentLocation}/static/images/meeds.png`"
        height="37px"
        width="101px"
        alt="">
      <v-spacer />
      <div>
        <v-btn
          :href="`/${parentLocation}/overview`"
          class="dark-grey-color font-weight-black dark-grey-border-color"
          outlined
          text>
          <img
            :src="`/${parentLocation}/static/images/meedsicon.png`"
            alt=""
            class="img-24px">
          <h4 class="hidden-md-and-down mx-1">{{ $t('open') }}</h4>
          <h4>{{ $t('app') }}</h4>
        </v-btn>
      </div>
    </div>
    <div v-else class="d-flex headerLayout px-0 px-sm-4 mx-1">
      <deeds-topbar-logo />
      <deeds-theme-button
        css-class="ms-4" 
        topbar />
      <v-spacer />
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
        <deeds-topbar-fiat-currency-selector />
      </div>
      <div class="ms-4">
        <v-btn
          class="hidden-xs-only"
          elevation="0"
          color="secondary"
          @click="$root.$emit('open-buy-meed-drawer')">
          {{ $t('buyMeedsButton') }}
        </v-btn>
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
    currentSiteLink() {
      return window.location.pathname;
    },
    replacedParentLocation() {
      return this.currentSiteLink.replace(`/${this.parentLocation}`,'');
    },
    homeDisplay() {
      return this.replacedParentLocation === '/' || this.replacedParentLocation === '/index';
    },
  }),
};
</script>