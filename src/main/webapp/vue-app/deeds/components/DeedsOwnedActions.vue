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
      <template v-if="provisioningManager">
        <template v-if="authenticated">
          <v-list-item v-if="status === 'STOPPED'" @click="startTenant">
            <v-list-item-title class="text-capitalize">{{ $t('moveIn') }}</v-list-item-title>
          </v-list-item>
          <v-list-item v-else-if="status === 'STARTED'" @click="stopTenant">
            <v-list-item-title class="text-capitalize">{{ $t('moveOut') }}</v-list-item-title>
          </v-list-item>
        </template>
        <v-tooltip v-else bottom>
          <template v-slot:activator="{ on, attrs }">
            <v-list-item
              v-bind="attrs"
              v-on="on"
              @click="authenticate()">
              <v-list-item-title class="text-capitalize">
                <v-icon size="16">mdi-lock-open</v-icon>
                {{ $t('signIn') }}
              </v-list-item-title>
            </v-list-item>
          </template>
          <span>{{ $t('authenticateToCommandTenantTooltip') }}</span>
        </v-tooltip>
        <v-divider class="my-1" />
      </template>
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
      <template v-if="authenticated">
        <v-divider class="my-1" />
        <v-list-item @click="logout()">
          <v-list-item-title class="text-capitalize">{{ $t('signOut') }}</v-list-item-title>
        </v-list-item>
      </template>
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
    authenticated: false,
    provisioningManager: false,
  }),
  computed: Vuex.mapState({
    openSeaBaseLink: state => state.openSeaBaseLink,
    networkId: state => state.networkId,
    address: state => state.address,
    nftAddress: state => state.nftAddress,
    provider: state => state.provider,
    tenantProvisioningContract: state => state.tenantProvisioningContract,
    nftId() {
      return this.nft && this.nft.id;
    }
  }),
  created() {
    this.loadStatus();
    this.refreshAuthentication();
  },
  methods: {
    refreshAuthentication() {
      this.authenticated = this.$authentication.isAuthenticated(this.address);
    },
    logout() {
      this.$authentication.logout(this.address)
        .finally(() => this.refreshAuthentication());
    },
    authenticate() {
      const token = document.querySelector('[name=loginMessage]').value;
      const message = this.$t('signMessage', {0: token});
      this.$ethUtils.signMessage(this.provider, this.address, message)
        .then(signedMessage => this.$authentication.login(this.address, message, signedMessage))
        .finally(() => this.refreshAuthentication());
    },
    startTenant() {
      this.$root.$emit('deeds-move-in-drawer', this.nftId);
    },
    stopTenant() {
      this.$root.$emit('deeds-move-out-drawer', this.nftId);
    },
    loadStatus() {
      this.status = 'loading';
      return this.tenantProvisioningContract.isProvisioningManager(this.address, this.nftId)
        .then(provisioningManager => {
          this.provisioningManager = provisioningManager;
          if (this.provisioningManager) {
            return this.tenantProvisioningContract.tenantStatus(this.nft.id)
              .then(status => this.status = status && 'STARTED' || 'STOPPED');
          }
        });
    },
  },
};
</script>