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
    @opened="$emit('opened')"
    @closed="$emit('closed')">
    <template #title>
      <h4 class="text-capitalize">{{ $t('deedMoveInDrawerTitle', {0: cardName, 1: nftId}) }}</h4>
    </template>
    <template v-if="drawer" #content>
      <v-card color="transparent" flat>
        <v-card-text>
          {{ $t('deedMoveInParagraph1') }}
        </v-card-text>
        <v-card-text class="pt-0">
          {{ $t('deedMoveInParagraph2') }}
        </v-card-text>
        <v-card-title class="pt-0">
          {{ $t('email') }}
        </v-card-title>
        <v-card-text class="pt-0">
          <label for="email">
            {{ $t('startTenantEmailLabel') }}
          </label>
          <deeds-email-field
            ref="email"
            v-model="email"
            :placeholder="$t('deedEmailContactPlaceholder')"
            :readonly="sending"
            :disabled="disabledEmail"
            :reset="drawer"
            @submit="sendRequest"
            @valid-email="validEmail = $event" />
        </v-card-text>
      </v-card>
      <template v-if="transactionHash">
        <v-spacer />
        <v-card-text>
          {{ $t('deedMoveInConfirmMessage1') }}
        </v-card-text>
        <v-card-text>
          {{ $t('deedMoveInConfirmMessage2') }}
        </v-card-text>
      </template>
    </template>
    <template v-if="transactionHash" #footer>
      <v-btn
        :min-width="minButtonsWidth"
        name="moveInConfirmButton"
        color="primary"
        class="ms-auto"
        depressed
        dark
        @click="closeAll()">
        {{ $t('gotIt') }}
      </v-btn>
    </template>
    <template v-else #footer>
      <v-btn
        :disabled="sending"
        name="cancelMoveIn"
        class="ms-auto me-2"
        min-width="120"
        outlined
        text
        @click="close(nftId)">
        {{ $t('cancel') }}
      </v-btn>
      <v-btn
        :min-width="minButtonsWidth"
        :disabled="!validEmail"
        :loading="sending"
        name="moveInConfirmButton"
        color="primary"
        class="primary"
        depressed
        dark
        @click="sendRequest">
        {{ $t('requestTenantButton') }}
      </v-btn>
    </template>
  </deeds-drawer>
</template>
<script>
export default {
  data: () => ({
    nft: null,
    drawer: false,
    email: null,
    emailChanged: false,
    validEmail: false,
    sending: false,
    sendingEmail: false,
    transactionHash: null,
    minButtonsWidth: 120,
  }),
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    address: state => state.address,
    provider: state => state.provider,
    etherscanBaseLink: state => state.etherscanBaseLink,
    tenantProvisioningContract: state => state.tenantProvisioningContract,
    startTenantGasLimit: state => state.startTenantGasLimit,
    nftId() {
      return this.nft?.id;
    },
    cardName() {
      return this.nft?.cardName;
    },
    transactionHashAlias() {
      return this.transactionHash && `${this.transactionHash.substring(0, 5)}...${this.transactionHash.substring(this.transactionHash.length - 3)}`;
    },
    transactionLink() {
      return `${this.etherscanBaseLink}/tx/${this.transactionHash}`;
    },
    disabledEmail() {
      return this.sending || this.transactionHash;
    },
  }),
  watch: {
    sending() {
      if (this.sending) {
        this.$refs.drawer?.startLoading();
      } else {
        this.$refs.drawer?.endLoading();
      }
    },
    email(newVal, oldVal) {
      if (this.transactionHash && newVal !== oldVal) {
        this.emailChanged = true;
      }
    },
  },
  created() {
    this.$root.$on('deeds-move-in-drawer', this.open);
    this.$root.$on('deeds-move-drawer-close', this.close);
  },
  beforeDestroy() {
    this.$root.$off('deeds-move-in-drawer', this.open);
    this.$root.$off('deeds-move-drawer-close', this.close);
  },
  methods: {
    open(nft) {
      this.nft = nft;
      this.transactionHash = null;
      this.sending = false;
      this.email = null;
      this.emailChanged = false;
      this.$nextTick()
        .then(() => this.$refs.drawer?.open());
    },
    closeAll() {
      const nftId = this.nftId;
      window.setTimeout(() => {
        this.$root.$emit('deeds-manage-drawer-close', nftId);
      }, 50);
      this.close(nftId);
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
      if (!this.transactionHash && this.$refs.email?.isValid()) {
        return this.startTenant();
      }
    },
    startTenant(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      if (this.nftId && this.$refs.email?.isValid()) {
        this.sending = true;
        const options = {
          from: this.address,
          gasLimit: this.startTenantGasLimit,
        };
        return this.$ethUtils.sendTransaction(
          this.provider,
          this.tenantProvisioningContract,
          'startTenant',
          options,
          [this.nftId]
        ).then(receipt => {
          this.transactionHash = receipt?.hash;
          if (this.transactionHash) {
            this.$root.$emit('nft-status-changed', this.nftId, 'loading', this.transactionHash, 'start');
            this.saveStartTenantRequest();
          }
          this.sending = false;
        }).catch(() => this.sending = false);
      }
    },
    saveStartTenantRequest() {
      this.sendingEmail = true;
      return this.$tenantManagement.startTenant(this.nftId, this.email?.trim(), this.transactionHash)
        .then(() => this.emailChanged = false)
        .finally(() => this.sendingEmail = false);
    },
  },
};
</script>