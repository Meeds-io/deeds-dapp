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
  <v-btn
    v-if="!isMetamaskInstalled"
    :href="metamaskInstallLinlk"
    name="topbarInstallMetamaskLink"
    target="_blank"
    rel="nofollow noreferrer noopener"
    color="primary"
    class="grey lighten-4 border-color-inherit"
    outlined>
    <img
      :src="`/${parentLocation}/static/images/metamask.svg`"
      alt=""
      class="me-0 me-sm-3 img-16px">
    <span v-if="!isMobile" class="text-none">{{ $t('installMetamaskButton') }}</span>
  </v-btn>
  <v-btn
    v-else-if="!validNetwork"
    name="topbarSwitchMetamaskNetworkButton"
    color="secondary"
    outlined
    @click="switchMetamaskNetwork">
    <img
      :src="`/${parentLocation}/static/images/metamask.svg`"
      alt=""
      class="me-0 me-sm-3 img-16px">
    <span v-if="!isMobile" class="text-none">{{ $t('switchMetamaskNetworkButton') }}</span>
  </v-btn>
  <v-btn
    v-else-if="!hasMetamaskConnectedAddress"
    name="topbarConnectMetamaskButton"
    color="primary"
    outlined
    @click="connectToMetamask">
    <img
      :src="`/${parentLocation}/static/images/metamask.svg`"
      alt=""
      class="me-0 me-sm-3 img-16px">
    <span v-if="!isMobile" class="text-none">{{ $t('connectMetamaskButton') }}</span>
  </v-btn>
</template>
<script>
export default {
  computed: Vuex.mapState({
    isMobile: state => state.isMobile,
    parentLocation: state => state.parentLocation,
    isMetamaskInstalled: state => state.isMetamaskInstalled,
    validNetwork: state => state.validNetwork,
    address: state => state.address,
    hasMetamaskConnectedAddress() {
      return !!this.address;
    },
    currentSiteLink() {
      return `${window.location.host}${window.location.pathname}`;
    },
    metamaskInstallLinlk() {
      return this.isMobile
        && `https://metamask.app.link/dapp/${this.currentSiteLink}`
        || 'https://metamask.io/';
    },
    connectionLabel() {
      if (!this.isMetamaskInstalled) {
        return this.$t('installMetamaskLabel');
      } else if (!this.hasMetamaskConnectedAddress) {
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
      this.$ethUtils.switchMetamaskNetwork('0x1');
    },
  },
};
</script>