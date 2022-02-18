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
  <v-toolbar elevation="0">
    <v-toolbar-title class="d-flex">
      <v-img
        max-height="64px"
        max-width="64px"
        src="/deeds-dapp/static/images/meeds.png"
        contain />
      <div class="ps-2 pb-1">{{ $t('dao') }}</div>
      <v-chip
        v-if="isTestNetwork"
        color="orange"
        dark
        small
        class="testnet-chip mt-1 ms-2">
        Rinkeby
      </v-chip>
    </v-toolbar-title>
    <v-spacer />
    <div class="ms-4 d-none d-sm-inline-block">
      <deeds-topbar-address-selector v-if="address" />
    </div>
    <div class="ms-4">
      <deeds-topbar-fiat-currency-selector v-if="address" />
    </div>
    <div class="ms-4 d-none d-sm-inline-block">
      <deeds-topbar-language-selector />
    </div>
  </v-toolbar>
</template>
<script>
export default {
  computed: Vuex.mapState({
    isMetamaskInstalled: state => state.isMetamaskInstalled,
    networkId: state => state.networkId,
    address: state => state.address,
    isTestNetwork() {
      return this.networkId === 4;
    },
  }),
};
</script>