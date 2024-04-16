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
  <v-toolbar-title>
    <a 
      id="tbHomedApp-button"
      :href="homeUrl" 
      class="d-flex align-center">
      <img
        :src="`${parentLocation}/static/images/meeds.png`"
        width="101px"
        alt="">
      <v-chip
        v-if="isTestNetwork"
        :small="!isMobile"
        :x-small="isMobile"
        color="orange"
        dark
        class="testnet-chip mt-1 ms-4">
        {{ testnetName }}
      </v-chip>
    </a>
  </v-toolbar-title>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    networkId: state => state.networkId,
    validNetwork: state => state.validNetwork,
    isMobile: state => state.isMobile,
    homeUrl: state => state.homeUrl,
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
      case 11155111:
        return 'Sepolia';
      default:
        return '';
      }
    },
  }),
};
</script>