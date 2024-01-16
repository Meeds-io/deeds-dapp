<!--
  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
    :right="!$vuetify.rtl"
    class="WoMDisconnectionDrawer"
    @opened="init"
    @closed="reset">
    <template slot="title">
      <h4>{{ $t('wom.disconnectionDrawerTitle') }}</h4>
    </template>
    <template v-if="!loading && drawer" #content>
      <div class="px-6 mt-8">
        <div class="font-weight-bold dark-grey-color title mb-4">
          {{ $t('wom.connectWallet') }}
        </div>
        <deeds-hub-deed-manager-selector
          ref="managerSelector"
          :raw-message="rawMessage"
          :address.sync="deedManagerAddress"
          :signature.sync="signedMessage" />
      </div>
    </template>
    <template v-if="!loading && drawer" #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn me-2"
          min-width="120"
          outlined
          text
          @click="close">
          {{ $t('cancel') }}
        </v-btn>
        <v-btn
          :loading="disconnecting"
          :disabled="!signedMessage"
          class="btn primary"
          @click="disconnect">
          {{ $t('wom.disconnect') }}
        </v-btn>
      </div>
    </template>
  </deeds-drawer>
</template>
<script>
export default {
  props: {
    hub: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
    loading: false,
    disconnecting: false,
    deedManagerAddress: null,
    token: null,
    signedMessage: null,
  }),
  computed: {
    hubAddress() {
      return this.hub?.address;
    },
    rawMessage() {
      return this.token && this.$t('wom.signDisconnectMessage', {
        0: this.token,
      }).replace(/\\n/g, '\n') || null;
    },
  },
  watch: {
    loading() {
      if (this.loading) {
        this.$refs.drawer.startLoading();
      } else {
        this.$refs.drawer.endLoading();
      }
    },
    disconnecting() {
      if (this.disconnecting) {
        this.$refs.drawer.startLoading();
      } else {
        this.$refs.drawer.endLoading();
      }
    },
  },
  created() {
    this.$root.$on('disconnect-hub', this.open);
  },
  methods: {
    open() {
      this.$refs.drawer.open();
    },
    init() {
      this.loading = true;
      this.$hubService.getToken()
        .then(token => this.token = token)
        .finally(() => this.loading = false);
    },
    reset() {
      this.signedMessage = null;
      this.token = null;

      this.$refs?.managerSelector?.reset();
    },
    close() {
      this.reset();
      this.$refs.drawer.close();
    },
    disconnect() {
      this.disconnecting = true;
      this.$hubService.disconnectFromWoM({
        hubAddress: this.hubAddress,
        deedManagerAddress: this.deedManagerAddress,
        signedMessage: this.signedMessage,
        rawMessage: this.rawMessage,
        token: this.token,
      })
        .then(() => {
          this.disconnecting = false;
          this.close();
          this.$root.$emit('alert-message', this.$t('wom.disconnectedFromWoMSuccessfully'), 'success');
          this.$root.$emit('close-hub-details');
        })
        .catch(e => {
          this.disconnecting = false;
          const error = (e?.cause || String(e));
          const errorMessageKey = error.includes('wom.') && `wom.${error.split('wom.')[1]}` || error;
          this.$root.$emit('alert-message', this.$t(errorMessageKey), 'error');
        });
    },
  },
};
</script>