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
    :loading="sending"
    :color="primary && 'tertiary' || 'text-color'"
    :depressed="primary"
    :dark="primary"
    :outlined="!primary"
    :text="!primary"
    name="moveOutConfirmButton"
    min-width="120"
    @click="sendRequest">
    <span class="text-capitalize">
      {{ $t(label) }}
    </span>
  </v-btn>
</template>

<script>
export default {
  props: {
    nftId: {
      type: Number,
      default: () => 0,
    },
    label: {
      type: String,
      default: null,
    },
    primary: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    sending: false,
    transactionHash: null,
  }),
  computed: Vuex.mapState({
    provider: state => state.provider,
    tenantProvisioningContract: state => state.tenantProvisioningContract,
    stopTenantGasLimit: state => state.stopTenantGasLimit,
  }),
  watch: {
    sending() {
      if (this.sending) {
        this.$emit('sending');
      } else {
        this.$emit('end-sending');
      }
    },
    transactionHash() {
      this.$emit('input', this.transactionHash);
    },
  },
  methods: {
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
          this.$root.$emit('nft-status-changed', this.nftId, 'loading', this.transactionHash);
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