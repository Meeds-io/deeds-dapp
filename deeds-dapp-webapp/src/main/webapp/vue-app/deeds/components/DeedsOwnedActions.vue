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
    <template #activator="{ on, attrs }">
      <v-btn
        :aria-label="$t('deedTenantActions', {0: nftId})"
        name="deedTenantCommands"
        text
        icon
        v-bind="attrs"
        v-on="on"
        @click="checkAuthentication">
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
          <template #activator="{ on, attrs }">
            <v-list-item
              v-bind="attrs"
              v-on="on"
              @click="$emit('login')">
              <v-list-item-title class="text-capitalize">
                <v-icon size="16">mdi-lock-open</v-icon>
                {{ $t('signIn') }}
              </v-list-item-title>
            </v-list-item>
          </template>
          <span>{{ $t('authenticateToCommandTenantTooltip') }}</span>
        </v-tooltip>
      </template>
      <v-divider v-if="provisioningManager && owner && status === 'STOPPED'" class="my-1" />
      <v-list-item v-if="owner && status === 'STOPPED'">
        <v-list-item-title class="text-capitalize">
          <a
            :href="`${openSeaBaseLink}/${nft.id}/sell`"
            target="_blank"
            rel="nofollow noreferrer noopener">
            {{ $t('sellOnOpenSea') }}
          </a>
        </v-list-item-title>
      </v-list-item>
      <template v-if="authenticated">
        <v-divider v-if="status !== 'loading'" class="my-1" />
        <v-list-item @click="$emit('logout')">
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
    authenticated: {
      type: Boolean,
      default: false,
    },
  },
  computed: Vuex.mapState({
    openSeaBaseLink: state => state.openSeaBaseLink,
    networkId: state => state.networkId,
    address: state => state.address,
    provider: state => state.provider,
    tenantProvisioningContract: state => state.tenantProvisioningContract,
    nftId() {
      return this.nft && this.nft.id;
    },
    provisioningManager() {
      return this.nft && this.nft.provisioningManager;
    },
    status() {
      return this.nft && this.nft.status;
    },
    owner() {
      return this.nft && this.nft.owner;
    },
  }),
  methods: {
    startTenant() {
      this.$root.$emit('deeds-move-in-drawer', this.nftId);
    },
    stopTenant() {
      this.$root.$emit('deeds-move-out-drawer', this.nftId);
    },
    checkAuthentication() {
      if (!this.tenantProvisioningContract) {
        return;
      }
      this.$tenantManagement.loadLastCommand(this.nftId)
        .catch(() => this.$emit('logout'));
    },
  },
};
</script>