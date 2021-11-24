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
        v-else-if="!isMainNetwork"
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
      <v-btn
        v-else-if="!isMetamaskConnected"
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
    </v-bottom-navigation>
  </div>
</template>
<script>
export default {
  computed: Vuex.mapState({
    isMetamaskInstalled: state => state.isMetamaskInstalled,
    networkId: state => state.networkId,
    address: state => state.address,
    isMainNetwork() {
      return this.networkId === 1;
    },
    isMetamaskConnected() {
      return !!this.address;
    },
    showMetamaskButton() {
      return !this.isMetamaskInstalled || !this.isMetamaskConnected || !this.isMainNetwork;
    },
    connectionLabel() {
      if (!this.isMetamaskInstalled) {
        return this.$t('installMetamaskLabel');
      } else if (!this.isMainNetwork) {
        return this.$t('switchMetamaskNetworkLabel');
      } else if (!this.isMetamaskConnected) {
        return this.$t('connectMetamaskLabel');
      }
      return this.isMetamaskInstalled && this.$t('connectMetamaskLabel') || this.$t('installMetamaskLabel');
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