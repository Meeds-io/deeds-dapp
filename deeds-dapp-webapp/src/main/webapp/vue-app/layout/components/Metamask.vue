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
  <div v-if="showMetamaskButton">
    <h3 class="d-flex justify-center">
      {{ connectionLabel }}
    </h3>
    <v-bottom-navigation
      width="auto"
      height="auto"
      class="elevation-0 mt-4">
      <v-btn
        v-if="!isMetamaskInstalled"
        name="installMetamaskLink"
        href="https://metamask.io/"
        target="_blank"
        rel="nofollow noreferrer noopener"
        height="168px"
        width="168px"
        class="rounded-lg"
        outlined
        text>
        <span class="py-2">Metamask</span>
        <v-img
          :src="`/${parentLocation}/static/images/metamask.svg`"
          max-height="57px"
          max-width="57px" />
      </v-btn>
      <v-btn
        v-else-if="!hasMetamashConnectedAddress"
        name="connectMetamaskButton"
        height="168px"
        width="168px"
        class="rounded-lg"
        outlined
        text
        @click="connectToMetamask">
        <span class="py-2">Metamask</span>
        <v-img
          :src="`/${parentLocation}/static/images/metamask.svg`"
          max-height="57px"
          max-width="57px" />
      </v-btn>
      <v-btn
        v-else-if="!validNetwork"
        name="switchMetamaskNetworkButton"
        height="168px"
        width="168px"
        class="rounded-lg"
        outlined
        text
        @click="switchMetamaskNetwork">
        <span class="py-2">Metamask</span>
        <v-img
          :src="`/${parentLocation}/static/images/metamask.svg`"
          max-height="57px"
          max-width="57px" />
      </v-btn>
    </v-bottom-navigation>
  </div>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    isMetamaskInstalled: state => state.isMetamaskInstalled,
    validNetwork: state => state.validNetwork,
    address: state => state.address,
    hasMetamashConnectedAddress() {
      return !!this.address;
    },
    showMetamaskButton() {
      return !this.isMetamaskInstalled || !this.hasMetamashConnectedAddress || !this.validNetwork;
    },
    connectionLabel() {
      if (!this.isMetamaskInstalled) {
        return this.$t('installMetamaskLabel');
      } else if (!this.hasMetamashConnectedAddress) {
        return this.$t('connectMetamaskLabel');
      } else if (!this.validNetwork) {
        return this.$t('switchMetamaskNetworkLabel');
      }
      return '';
    },
  }),
  methods: {
    connectToMetamask() {
      this.$ethUtils.connectToMetamask();
    },
    switchMetamaskNetwork() {
      this.$ethUtils.switchMetamaskNetwork('0x4');
    },
  },
};
</script>