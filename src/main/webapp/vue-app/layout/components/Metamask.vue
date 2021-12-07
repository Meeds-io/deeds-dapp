<template>
  <div v-if="showMetamaskButton">
    <h3 class="d-flex justify-center">
      {{ connectionLabel }}
    </h3>
    <v-bottom-navigation
      width="auto"
      height="auto"
      class="elevation-0 mt-4">
      <v-btn
        v-if="!isMetamaskInstalled"
        height="168px"
        width="168px"
        class="rounded-lg elevation-1"
        link
        href="https://metamask.io/"
        target="_blank">
        <span class="py-2">Metamask</span>
        <v-img
          src="../images/metamask.svg"
          max-height="57px"
          max-width="57px" />
      </v-btn>
      <v-btn
        v-else-if="!hasMetamashConnectedAddress"
        height="168px"
        width="168px"
        class="rounded-lg elevation-1"
        @click="connectToMetamask">
        <span class="py-2">Metamask</span>
        <v-img
          src="../images/metamask.svg"
          max-height="57px"
          max-width="57px" />
      </v-btn>
      <v-btn
        v-else-if="!validNetwork"
        height="168px"
        width="168px"
        class="rounded-lg elevation-1"
        @click="switchMetamaskNetwork">
        <span class="py-2">Metamask</span>
        <v-img
          src="../images/metamask.svg"
          max-height="57px"
          max-width="57px" />
      </v-btn>
    </v-bottom-navigation>
  </div>
</template>
<script>
export default {
  computed: Vuex.mapState({
    isMetamaskInstalled: state => state.isMetamaskInstalled,
    validNetwork: state => state.validNetwork,
    address: state => state.address,
    hasMetamashConnectedAddress() {
      return !!this.address;
    },
    showMetamaskButton() {
      return !this.isMetamaskInstalled || !this.hasMetamashConnectedAddress || !this.validNetwork;
    },
    connectionLabel() {
      if (!this.isMetamaskInstalled) {
        return this.$t('installMetamaskLabel');
      } else if (!this.hasMetamashConnectedAddress) {
        return this.$t('connectMetamaskLabel');
      } else if (!this.validNetwork) {
        return this.$t('switchMetamaskNetworkLabel');
      }
      return '';
    },
  }),
  methods: {
    connectToMetamask() {
      this.$ethUtils.connectToMetamask();
    },
    switchMetamaskNetwork() {
      this.$ethUtils.switchMetamaskNetwork();
    },
  },
};
</script>