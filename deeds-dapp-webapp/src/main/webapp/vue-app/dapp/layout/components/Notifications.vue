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
  <v-snackbar
    v-if="snackbar"
    v-model="snackbar"
    :left="!$vuetify.rtl"
    :right="$vuetify.rtl"
    :timeout="timeout"
    color="transparent"
    elevation="0"
    app
    @click="cancelEvent">
    <v-alert
      :type="alertType"
      :max-width="maxWidth"
      border="left"
      class="white"
      elevation="2"
      dismissible
      colored-border
      outlined>
      <span class="black--text">
        {{ alertMessage }}
      </span>
      <v-btn
        v-if="alertLink"
        :href="alertLink"
        :title="alertLinkTooltip"
        name="closeSnackbarButton"
        target="_blank"
        rel="nofollow noreferrer noopener"
        class="secondary--text"
        icon
        link>
        <v-icon>{{ alertLinkIcon }}</v-icon>
      </v-btn>
      <v-btn
        slot="close"
        slot-scope="{toggle}"
        name="closeAlertButton"
        icon
        small
        light
        @click="cancelEvent($event);toggle($event);">
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </v-alert>
  </v-snackbar>
</template>
<script>
export default {
  data: () => ({
    snackbar: false,
    timeout: 10000,
    alertMessage: null,
    alertType: null,
    alertLink: null,
    alertLinkIcon: null,
    alertLinkTooltip: null,
  }),
  computed: Vuex.mapState({
    isMobile: state => state.isMobile,
    etherscanBaseLink: state => state.etherscanBaseLink,
    maxWidth() {
      return this.isMobile && '100vw' || '50vw';
    },
  }),
  created() {
    this.$root.$on('transaction-sent', transactionHash => {
      this.snackbar = false;
      this.alertLink = `${this.etherscanBaseLink}/tx/${transactionHash}`;
      this.alertLinkIcon = 'mdi-open-in-new';
      this.alertType = 'success';
      this.alertMessage = this.$t('transactionSent');
      this.alertLinkTooltip = this.$t('viewOnEtherscan');
      window.setTimeout(() => this.snackbar = true, 500);
    });
    this.$root.$on('alert-message', (message, type) => {
      this.snackbar = false;
      this.alertType = type;
      this.alertMessage = message;
      window.setTimeout(() => this.snackbar = true, 500);
    });
    this.$root.$on('close-alert-message', () => this.snackbar = false);
    window.setTimeout(() => this.snackbar = false, this.timeout);
  },
  methods: {
    cancelEvent(event) {
      if (event) {
        event.stopPropagation();
        event.preventDefault();
      }
    },
  },
};
</script>