<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 
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
    fixed
    elevation="4"
    color="white">
    <v-spacer />
    <v-toolbar-title class="d-flex">
      <v-img
        max-height="64px"
        max-width="64px"
        :src="`/${parentLocation}/static/images/meeds.png`"
        contain
        eager />
      <div class="ps-2 pb-1">{{ $t('dao') }}</div>
      <v-chip
        v-if="isTestNetwork"
        color="orange"
        dark
        small
        class="testnet-chip mt-1 ms-2">
        {{ testnetName }}
      </v-chip>
    </v-toolbar-title>
    <v-spacer />
    <div class="ms-4 d-none d-sm-inline-block">
      <deeds-topbar-address-selector v-if="address" />
    </div>
    <div class="ms-4 d-none d-sm-inline-block">
      <deeds-topbar-gas-price />
    </div>
    <div class="ms-4">
      <deeds-topbar-fiat-currency-selector v-if="address" />
    </div>
    <div class="ms-4">
      <deeds-topbar-language-selector />
    </div>
    <v-spacer />
  </v-app-bar>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    isMetamaskInstalled: state => state.isMetamaskInstalled,
    networkId: state => state.networkId,
    validNetwork: state => state.validNetwork,
    address: state => state.address,
    isTestNetwork() {
      return this.networkId !== 1 && this.validNetwork;
    },
    testnetName() {
      if (!this.networkId) {
        return '';
      }
      switch (this.networkId) {
      case 4:
        return 'Rinkeby';
      case 5:
        return 'Goerli';
      default:
        return '';
      }
    },
  }),
};
</script>