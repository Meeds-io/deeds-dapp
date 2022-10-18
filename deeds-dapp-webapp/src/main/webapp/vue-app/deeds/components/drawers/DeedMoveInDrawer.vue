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
    second-level
    @opened="$emit('opened')"
    @closed="$emit('closed')">
    <template #title>
      <h4>{{ $t('deedMoveInDrawerTitle', {0: cardTypeName, 1: nftId}) }}</h4>
    </template>
    <template #content>
      <v-form ref="form" @submit="sendRequest">
        <v-card flat>
          <v-card-text>
            {{ $t('deedMoveInParagraph1') }}
          </v-card-text>
          <v-card-text class="pt-0">
            {{ $t('deedOwnerMoveInParagraph2') }}
          </v-card-text>
          <v-card-text class="pt-0">
            {{ $t('deedMoveInParagraph3') }}
          </v-card-text>
          <v-card-title class="pt-0">
            {{ $t('email') }}
          </v-card-title>
          <v-card-text class="pt-0">
            <label for="email">
              {{ $t('requestTenantEmailLabel') }}
            </label>
            <v-text-field
              ref="email"
              v-model="email"
              :placeholder="$t('requestTenantEmailPlaceholder')"
              :readonly="disabledEmail"
              :disabled="sendingEmail"
              name="email"
              type="email"
              class="align-center"
              hide-details
              large
              outlined
              dense
              @focus="editEmail"
              @blur="cancelEditEmail">
              <template v-if="transactionHash" #append-outer>
                <v-slide-x-reverse-transition mode="out-in">
                  <v-btn
                    ref="emailEditButton"
                    :key="emailAppendIcon"
                    :title="emailAppendIconTooltip"
                    :loading="sendingEmail"
                    :disabled="!emailChanged && isEditingEmail"
                    name="emailAppendIcon"
                    icon
                    @click="isEditingEmail && sendEmail() || editEmail()">
                    <v-icon :color="emailAppendIconColor">
                      {{ emailAppendIcon }}
                    </v-icon>
                  </v-btn>
                </v-slide-x-reverse-transition>
              </template>
            </v-text-field>
          </v-card-text>
        </v-card>
      </v-form>
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
      <deeds-move-out-button
        :nft-id="nftId"
        label="cancel"
        class="ms-auto me-2"
        @sending="$refs.drawer.startLoading()"
        @end-sending="$refs.drawer.endLoading()" />
      <v-btn
        :min-width="minButtonsWidth"
        name="moveInConfirmButton"
        color="tertiary"
        depressed
        dark
        @click="close(nftId)">
        <span class="text-capitalize">
          {{ $t('gotIt') }}
        </span>
      </v-btn>
    </template>
    <template v-else #footer>
      <v-btn
        outlined
        text
        class="ms-auto me-2"
        name="cancelMoveIn"
        min-width="120"
        @click="close(nftId)">
        <span class="text-capitalize">
          {{ $t('cancel') }}
        </span>
      </v-btn>
      <v-btn
        :min-width="minButtonsWidth"
        :disabled="!validForm"
        :loading="sending"
        name="moveInConfirmButton"
        color="tertiary"
        depressed
        dark
        @click="sendRequest">
        <span class="text-capitalize">
          {{ $t('requestTenantButton') }}
        </span>
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
    isEditingEmail: false,
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
    validForm() {
      return !this.email || !this.email.length || (this.$refs.form && this.$refs.form.$el.reportValidity());
    },
    nftId() {
      return this.nft?.id;
    },
    cardTypeName() {
      return this.nft?.cardTypeName;
    },
    transactionHashAlias() {
      return this.transactionHash && `${this.transactionHash.substring(0, 5)}...${this.transactionHash.substring(this.transactionHash.length - 3)}`;
    },
    transactionLink() {
      return `${this.etherscanBaseLink}/tx/${this.transactionHash}`;
    },
    disabledEmail() {
      return this.sending || this.transactionHash && !this.isEditingEmail;
    },
    emailAppendIcon() {
      return this.isEditingEmail ? 'mdi-check-outline' : 'mdi-circle-edit-outline';
    },
    emailAppendIconColor() {
      return this.isEditingEmail && 'success' || 'info';
    },
    emailAppendIconTooltip() {
      return this.isEditingEmail && this.$t('sendEmailButtonTooltip') || this.$t('editEmailButtonTooltip');
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
  methods: {
    open(nft) {
      this.nft = nft;
      this.transactionHash = null;
      this.sending = false;
      this.isEditingEmail = false;
      this.emailChanged = false;
      this.$nextTick()
        .then(() => {
          if (this.$refs.drawer) {
            this.$refs.drawer.open();
          }
        });
    },
    close(nftId) {
      if (nftId === this.nftId) {
        this.$refs.drawer.close();
      }
    },
    sendRequest(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      if (!this.transactionHash) {
        return this.startTenant();
      } else if (this.isEditingEmail) {
        this.sendEmail();
      }
    },
    cancelEditEmail() {
      if (this.transactionHash && !this.emailChanged) {
        window.setTimeout(() => {
          if (this.isEditingEmail && !this.sendingEmail) {
            this.isEditingEmail = false;
          }
        }, 200);
      }
    },
    editEmail() {
      if (this.transactionHash && !this.isEditingEmail) {
        this.isEditingEmail = true;
        this.emailChanged = false;
        this.$nextTick().then(() => this.$refs.email.focus());
      }
    },
    sendEmail(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      if (this.email) {
        this.sendingEmail = true;
        return this.$tenantManagement.saveEmail(this.nftId, this.email)
          .then(() => this.emailChanged = false)
          .finally(() => this.isEditingEmail = this.sendingEmail = false);
      }
    },
    startTenant(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      if (this.nftId && this.$refs.form.$el.checkValidity()) {
        this.sending = true;
        const options = {
          gasLimit: this.startTenantGasLimit,
        };
        return this.$ethUtils.sendTransaction(
          this.provider,
          this.tenantProvisioningContract,
          'startTenant',
          options,
          [this.nftId]
        ).then(receipt => {
          if (receipt) {
            this.transactionHash = receipt.hash;
            this.$root.$emit('nft-status-changed', this.nftId, 'loading', this.transactionHash);
            this.$root.$emit('transaction-sent', this.transactionHash);
            this.saveStartTenantRequest();
          }
        }).finally(() => this.sending = false);
      }
    },
    saveStartTenantRequest() {
      this.sendingEmail = true;
      return this.$tenantManagement.startTenant(this.nftId, this.email, this.transactionHash)
        .then(() => this.emailChanged = false)
        .finally(() => this.isEditingEmail = this.sendingEmail = false);
    },
  },
};
</script>