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
        v-if="isMetamaskInstalled"
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
        v-else
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
    </v-bottom-navigation>
  </div>
</template>
<script>
export default {
  computed: Vuex.mapState({
    isMetamaskInstalled: state => state.isMetamaskInstalled,
    address: state => state.address,
    showMetamaskButton() {
      return !this.isMetamaskInstalled || !this.address;
    },
    connectionLabel() {
      return this.isMetamaskInstalled && this.$t('connectMetamaskLabel') || this.$t('installMetamaskLabel');
    },
  }),
  methods: {
    connectToMetamask() {
      if (this.isMetamaskInstalled) {
        this.$ethUtils.connectToMetamask();
      }
    },
  },
};
</script>