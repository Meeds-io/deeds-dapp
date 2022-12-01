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
    app>
    <v-alert
      :type="alertType"
      :max-width="maxWidth"
      :dark="dark"
      :light="!dark"
      :class="whiteThemeColor"
      border="left"
      elevation="2"
      outlined
      colored-border>
      <div class="d-flex flex-nowrap align-center full-width">
        <template v-if="currency">
          <v-img
            v-if="currency === 'MEED'"
            :src="`/${parentLocation}/static/images/meedsicon.png`"
            :max-height="maxIconsSize"
            :max-width="maxIconsSize"
            class="me-4"
            contain
            eager />
          <v-img
            v-else-if="currency === 'ETH'"
            :src="`/${parentLocation}/static/images/ether.svg`"
            :max-height="maxIconsSize"
            :max-width="maxIconsSize"
            class="me-2"
            contain
            eager />
        </template>
        <span
          v-if="useHtml"
          class="text--lighten-1 flex-grow-1 pe-4"
          v-html="alertMessage"
          @click="handleAlertClicked">
        </span>
        <span v-else class="text--lighten-1 flex-grow-1 pe-4">
          {{ alertMessage }}
        </span>
        <v-btn
          v-if="alertLink || alertLinkCallback"
          :href="alertLink"
          :title="alertLinkTooltip"
          name="closeSnackbarButton"
          target="_blank"
          rel="nofollow noreferrer noopener"
          class="secondary--text"
          icon
          link
          @click="linkCallback">
          <v-icon>{{ alertLinkIcon }}</v-icon>
        </v-btn>
        <v-btn
          slot="close"
          name="closeAlertButton"
          icon
          @click="closeAlert">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </div>
    </v-alert>
  </v-snackbar>
</template>
<script>
export default {
  data: () => ({
    snackbar: false,
    timeout: 1000000,
    alertMessage: null,
    useHtml: false,
    alertType: null,
    alertLink: null,
    alertLinkCallback: null,
    alertLinkIcon: null,
    alertLinkTooltip: null,
    timeoutInstance: null,
    currency: null,
    maxIconsSize: '20px',
  }),
  computed: Vuex.mapState({
    isMobile: state => state.isMobile,
    etherscanBaseLink: state => state.etherscanBaseLink,
    dark: state => state.dark,
    whiteThemeColor: state => state.whiteThemeColor,
    parentLocation: state => state.parentLocation,
    maxWidth() {
      return this.isMobile && '100vw' || '50vw';
    },
  }),
  created() {
    this.$root.$on('alert-message', (message, type, linkCallback, linkIcon, linkTooltip) => {
      this.openAlert({
        alertType: type,
        alertMessage: message,
        alertLinkCallback: linkCallback,
        alertLinkIcon: linkIcon,
        alertLinkTooltip: linkTooltip,
        currency: null
      });
    });
    this.$root.$on('alert-message-html', (message, type, linkCallback, linkIcon, linkTooltip) => {
      this.openAlert({
        useHtml: true,
        alertType: type,
        alertMessage: message,
        alertLinkCallback: linkCallback,
        alertLinkIcon: linkIcon,
        alertLinkTooltip: linkTooltip,
        currency: null
      });
    });
    this.$root.$on('alert-message-currency', (message, type, linkCallback, currency) => {
      this.openAlert({
        useHtml: true,
        alertType: type,
        alertMessage: message,
        alertLinkCallback: linkCallback,
        currency,
      });
    });
    document.addEventListener('transaction-sent', this.handleTransactionSent);
    document.addEventListener('transaction-sending-error', this.handleTransactionEstimationError);
    this.$root.$on('close-alert-message', this.closeAlert);
    this.$root.$on('drawer-closed', this.closeAlert);
    this.$root.$on('drawer-opened', this.closeAlert);
    this.$root.$on('switch-page', this.closeAlert);
  },
  methods: {
    handleTransactionSent(event) {
      const transactionHash = event?.detail;
      if (transactionHash) {
        this.openAlert({
          alertLink: `${this.etherscanBaseLink}/tx/${transactionHash}`,
          alertLinkIcon: 'mdi-open-in-new',
          alertType: 'success',
          alertMessage: this.$t('transactionSent'),
          alertLinkTooltip: this.$t('viewOnEtherscan'),
        });
      }
    },
    handleTransactionEstimationError(event) {
      if (event?.detail?.length) {
        const message = event?.detail;
        try {
          let blockchainMessage = message.substring(message.indexOf(':', message.indexOf('execution reverted')) + 2, message.indexOf('"', message.indexOf('execution reverted')));
          if (blockchainMessage.includes('#')) {
            const blockchainMessages = blockchainMessage.split('#');
            blockchainMessage = this.$t(`BlockchainError.${blockchainMessages[1]}`);
          }
          this.$root.$emit('alert-message-html', this.$t('transactionSendingCanceledDueToError', {0: `<span class="error--text">${blockchainMessage}</span>`}), 'error');
        } catch (e) {
          console.debug(e);// eslint-disable-line no-console
          this.$root.$emit('alert-message-html', this.$t('transactionSendingCanceledDueToError', {0: `<span class="error--text">${message}</span>`}), 'error');
        }
      }
    },
    openAlert(params) {
      this.closeAlert();
      this.$nextTick().then(() => {
        this.useHtml = params.useHtml || false;
        this.alertLink = params.alertLink || null;
        this.alertMessage = params.alertMessage || null;
        this.alertLinkIcon = params.alertLinkIcon || null;
        this.alertType = params.alertType || 'info';
        this.alertLinkTooltip = params.alertLinkTooltip || null;
        this.alertLinkCallback = params.alertLinkCallback || null;
        this.currency = params.currency || null;
        this.timeoutInstance = window.setTimeout(() => this.snackbar = true, 500);
      });
    },
    closeAlert() {
      if (this.timeoutInstance) {
        window.clearTimeout(this.timeoutInstance);
      }
      this.snackbar = false;
    },
    linkCallback() {
      if (this.alertLinkCallback) {
        this.alertLinkCallback();
      }
    },
    cancelEvent(event) {
      if (event) {
        event.stopPropagation();
        event.preventDefault();
      }
    },
    handleAlertClicked(event) {
      if (event) {
        event.stopPropagation();
        event.preventDefault();
      }
      if (!event || event?.target?.tagName?.toLowerCase() === 'a') {
        this.linkCallback();
      }
    },
  },
};
</script>