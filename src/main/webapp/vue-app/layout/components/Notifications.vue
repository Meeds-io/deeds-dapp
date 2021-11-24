<template>
  <v-snackbar
    v-if="snackbar"
    v-model="snackbar"
    :left="!$vuetify.rtl"
    :right="$vuetify.rtl"
    timeout="20000"
    color="transparent"
    elevation="0"
    app>
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
        target="_blank"
        class="primary--text"
        icon
        link>
        <v-icon>{{ alertLinkIcon }}</v-icon>
      </v-btn>
      <v-btn
        slot="close"
        slot-scope="{toggle}"
        icon
        small
        light
        @click="toggle">
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </v-alert>
  </v-snackbar>
</template>
<script>
export default {
  data: () => ({
    snackbar: false,
    alertMessage: null,
    alertType: null,
    alertLink: null,
    alertLinkIcon: null,
    alertLinkTooltip: null,
  }),
  computed: Vuex.mapState({
    etherscanBaseLink: state => state.etherscanBaseLink,
    isMobile() {
      return this.$vuetify && this.$vuetify.breakpoint && this.$vuetify.breakpoint.name === 'xs';
    },
    maxWidth() {
      return this.isMobile && '100vw' || '50vw';
    },
  }),
  created() {
    this.$root.$on('transaction-sent', transactionHash => {
      this.alertLink = `${this.etherscanBaseLink}/tx/${transactionHash}`;
      this.alertLinkIcon = 'mdi-open-in-new';
      this.alertType = 'success';
      this.alertMessage = this.$t('transactionSent');
      this.alertLinkTooltip = this.$t('viewOnEtherscan');
      this.snackbar = false;
      this.$nextTick().then(() => this.snackbar = true);
    });
  },
};
</script>