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
  <deeds-drawer ref="drawer">
    <template #title>
      <h4>{{ $t('removeTenantTitle') }}</h4>
    </template>
    <template #content>
      <v-card flat>
        <v-card-text v-if="transactionHash">
          <v-list-item class="px-0">
            <v-list-item-avatar size="72">
              <v-img :src="`/${parentLocation}/static/images/transactionInProgress.png`" />
            </v-list-item-avatar>
            <v-list-item-content class="d-inline">
              {{ $t('removeTenantSentDescription') }}
              <a
                :href="transactionLink"
                target="_blank"
                rel="noreferrer">
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
        <v-card-actions v-if="!transactionHash" class="ms-2">
          <v-btn
            :loading="sending"
            color="primary"
            @click="sendRequest">
            <span class="text-capitalize">
              {{ $t('removeTenantButton') }}
            </span>
          </v-btn>
        </v-card-actions>
      </v-card>
    </template>
  </deeds-drawer>
</template>

<script>
export default {
  data: () => ({
    nftId: 0,
    sending: false,
    transactionHash: null,
  }),
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    address: state => state.address,
    provider: state => state.provider,
    etherscanBaseLink: state => state.etherscanBaseLink,
    tenantProvisioningContract: state => state.tenantProvisioningContract,
    stopTenantGasLimit: state => state.stopTenantGasLimit,
    stopTenantMethod() {
      if (this.provider && this.tenantProvisioningContract) {
        const signer = this.tenantProvisioningContract.connect(this.provider.getSigner());
        return signer.stopTenant;
      }
      return null;
    },
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
        this.$refs.drawer.startLoading();
      } else {
        this.$refs.drawer.endLoading();
      }
    },
  },
  created() {
    this.$root.$on('deeds-move-out-drawer', this.open);
  },
  methods: {
    open(nftId) {
      this.nftId = nftId;
      this.transactionHash = null;
      this.sending = false;
      this.$nextTick()
        .then(() => this.$refs.drawer.open());
    },
    close() {
      this.$refs.drawer.close();
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
        return this.stopTenantMethod(
          this.nftId,
          options
        ).then(receipt => {
          this.transactionHash = receipt.hash;
          this.$root.$emit('transaction-sent', this.transactionHash);
        }).finally(() => this.sending = false);
      }
    },
  },
};
</script>