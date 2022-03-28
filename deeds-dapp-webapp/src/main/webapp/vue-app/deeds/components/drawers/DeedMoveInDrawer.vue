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
      <h4>{{ $t('requestTenantTitle') }}</h4>
    </template>
    <template #content>
      <v-form ref="form" @submit="sendRequest">
        <v-card flat>
          <v-card-text v-if="transactionHash">
            <v-list-item class="px-0">
              <v-list-item-avatar size="72">
                <v-img :src="`/${parentLocation}/static/images/transactionInProgress.png`" />
              </v-list-item-avatar>
              <v-list-item-content class="d-inline">
                {{ $t('requestTenantSentDescription') }}
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
            {{ $t('requestTenantDescription') }}
          </v-card-text>
          <v-card-title>
            {{ $t('email') }}
          </v-card-title>
          <v-card-text>
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
              <template v-if="transactionHash" v-slot:append-outer>
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
          <v-card-actions v-if="!transactionHash" class="ms-2">
            <v-btn
              :disabled="!validForm"
              :loading="sending"
              name="moveInConfirmButton"
              type="submit"
              color="primary">
              <span class="text-capitalize">
                {{ $t('requestTenantButton') }}
              </span>
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-form>
    </template>
  </deeds-drawer>
</template>

<script>
export default {
  data: () => ({
    nftId: 0,
    email: null,
    emailChanged: false,
    isEditingEmail: false,
    sending: false,
    sendingEmail: false,
    transactionHash: null,
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
  },
  methods: {
    open(nftId) {
      this.nftId = nftId;
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
            this.$root.$emit('nft-status-changed', this.nftId, 'loading', this.$t('tenant.starting'));
            this.transactionHash = receipt.hash;
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