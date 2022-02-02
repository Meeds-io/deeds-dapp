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
  <v-menu offset-y>
    <template v-slot:activator="{ on, attrs }">
      <v-btn
        text
        icon
        v-bind="attrs"
        v-on="on">
        <v-icon>mdi-dots-vertical</v-icon>
      </v-btn>
    </template>
    <v-list dense>
      <v-list-item v-if="status === 'STOPPED'" @click="startTenant">
        <v-list-item-title class="text-capitalize">{{ $t('moveIn') }}</v-list-item-title>
      </v-list-item>
      <v-list-item v-else-if="status === 'STARTED'" @click="stopTenant">
        <v-list-item-title class="text-capitalize">{{ $t('moveOut') }}</v-list-item-title>
      </v-list-item>
      <v-list-item>
        <v-list-item-title class="text-capitalize">
          <a
            :href="`${openSeaBaseLink}/${nft.id}/sell`"
            target="_blank"
            rel="noreferrer">
            {{ $t('sellOnOpenSea') }}
          </a>
        </v-list-item-title>
      </v-list-item>
    </v-list>
  </v-menu>
</template>
<script>
export default {
  props: {
    nft: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    status: 'loading',
  }),
  computed: Vuex.mapState({
    openSeaBaseLink: state => state.openSeaBaseLink,
    networkId: state => state.networkId,
    address: state => state.address,
    nftAddress: state => state.nftAddress,
    provider: state => state.provider,
  }),
  created() {
    this.loadStatus();
  },
  methods: {
    startTenant() {
      return this.changeStatus(this.nft.id, 'START');
    },
    stopTenant() {
      return this.changeStatus(this.nft.id, 'STOP');
    },
    loadStatus() {
      this.status = 'loading';
      return this.$tenantManagement.getStatus(this.networkId, this.nftAddress, this.nft.id)
        .then(status => this.status = status);
    },
    changeStatus(nftId, status) {
      if (this.$authentication.isAuthenticated(this.address)) {
        return this.$tenantManagement.changeStatus(this.networkId, this.nftAddress, nftId, status)
          .then(() => this.loadStatus());
      } else {
        return this.$ethUtils.signMessage(this.provider, this.address, document.querySelector('[name=loginMessage]').value)
          .then(signedMessage => this.$authentication.login(this.address, signedMessage))
          .then(() => this.$tenantManagement.changeStatus(this.networkId, this.nftAddress, nftId, status))
          .then(() => this.loadStatus());
      }
    },
  },
};
</script>