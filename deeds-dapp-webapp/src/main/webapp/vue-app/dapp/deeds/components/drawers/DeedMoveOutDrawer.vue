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
  <deeds-drawer
    ref="drawer"
    v-model="drawer"
    :permanent="sending"
    second-level
    @opened="$emit('opened')"
    @closed="$emit('closed')">
    <template #title>
      <h4>{{ $t('removeTenantTitle') }}</h4>
    </template>
    <template #content>
      <v-card flat>
        <v-card-text v-if="transactionHash">
          <v-list-item class="px-0">
            <v-list-item-avatar size="72">
              <v-img :src="`/${parentLocation}/static/images/transactionInProgress.png`" eager />
            </v-list-item-avatar>
            <v-list-item-content class="d-inline">
              {{ $t('removeTenantSentDescription') }}
              <a
                :href="transactionLink"
                target="_blank"
                rel="nofollow noreferrer noopener">
                {{ transactionHashAlias }}
              </a>
            </v-list-item-content>
          </v-list-item>
        </v-card-text>
        <v-card-text v-else>
          <v-list-item class="px-0">
            <v-list-item-icon size="72">
              <v-icon size="72" color="warning">mdi-alert-remove</v-icon>
            </v-list-item-icon>
            <v-list-item-content class="d-inline">
              {{ $t('removeTenantDescription') }}
            </v-list-item-content>
          </v-list-item>
        </v-card-text>
      </v-card>
    </template>
    <template v-if="!transactionHash" #footer>
      <v-btn
        :disabled="sending"
        outlined
        text
        class="ms-auto me-2"
        name="cancelMoveIn"
        min-width="120"
        @click="close(nftId)">
        {{ $t('cancel') }}
      </v-btn>
      <v-btn
        :loading="sending"
        name="moveOutConfirmButton"
        color="primary"
        min-width="120"
        depressed
        dark
        @click="sendRequest">
        {{ $t('removeTenantButton') }}
      </v-btn>
    </template>
  </deeds-drawer>
</template>
<script>
export default {
  data: () => ({
    nftId: 0,
    drawer: false,
    sending: false,
    transactionHash: null,
  }),
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    etherscanBaseLink: state => state.etherscanBaseLink,
    provider: state => state.provider,
    tenantProvisioningContract: state => state.tenantProvisioningContract,
    stopTenantGasLimit: state => state.stopTenantGasLimit,
    transactionHashAlias() {
      return this.transactionHash && `${this.transactionHash.substring(0, 5)}...${this.transactionHash.substring(this.transactionHash.length - 3)}`;
    },
    transactionLink() {
      return `${this.etherscanBaseLink}/tx/${this.transactionHash}`;
    },
  }),
  watch: {
    sending() {
      if (this.sending) {
        this.$emit('sending');
      } else {
        this.$emit('end-sending');
      }
    },
  },
  created() {
    this.$root.$on('deeds-move-out-drawer', this.open);
    this.$root.$on('deeds-move-drawer-close', this.close);
  },
  beforeDestroy() {
    this.$root.$off('deeds-move-out-drawer', this.open);
    this.$root.$off('deeds-move-drawer-close', this.close);
  },
  methods: {
    open(nftId) {
      this.nftId = nftId;
      this.sending = false;
      this.transactionHash = null;
      this.$nextTick()
        .then(() => this.$refs.drawer?.open());
    },
    close(nftId) {
      if (nftId === this.nftId) {
        this.$refs.drawer?.close();
      }
    },
    sendRequest(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      if (!this.transactionHash) {
        return this.stopTenant();
      }
    },
    stopTenant(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      if (this.nftId) {
        this.sending = true;
        const options = {
          gasLimit: this.stopTenantGasLimit,
        };
        return this.$ethUtils.sendTransaction(
          this.provider,
          this.tenantProvisioningContract,
          'stopTenant',
          options,
          [this.nftId]
        ).then(receipt => {
          this.transactionHash = receipt.hash;
          this.$root.$emit('nft-status-changed', this.nftId, 'loading', this.transactionHash, 'stop');
          this.$root.$emit('transaction-sent', this.transactionHash);
          this.saveStopTenantRequest();
        }).finally(() => this.sending = false);
      }
    },
    saveStopTenantRequest() {
      return this.$tenantManagement.stopTenant(this.nftId, this.transactionHash);
    },
  },
};
</script>