<template>
  <v-chip
    :color="color"
    :href="blockexplorer"
    :title="$t('wom.blockchainNetwork')"
    target="_blank"
    rel="nofollow noreferrer noopener"
    dark
    small>
    <v-icon
      :color="blockchainColor"
      size="18"
      class="me-1 ms-n1">
      fab fa-ethereum
    </v-icon>
    <span class="font-weight-bold">{{ name }}</span>
  </v-chip>
</template>
<script>
export default {
  props: {
    networkId: {
      type: Number,
      default: null,
    },
  },
  computed: {
    blockchain() {
      return this.$root.blockchains[this.networkId || 0] || this.$root.blockchains[0];
    },
    name() {
      return this.blockchain?.name;
    },
    testnet() {
      return this.blockchain.testnet;
    },
    blockexplorer() {
      return this.blockchain.blockexplorer;
    },
    color() {
      return this.testnet && 'orange' || 'green lighten-2';
    },
    blockchainColor() {
      return this.testnet && 'white' || 'blue darken-3';
    },
  },
};
</script>