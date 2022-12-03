<template>
  <v-tooltip bottom>
    <template #activator="{ on, attrs }">
      <v-btn
        v-show="address"
        name="gasPriceButton"
        outlined
        text
        plain
        class="px-2"
        v-bind="attrs"
        v-on="on"
        @click="refreshGasPrice">
        <v-icon color="grey" class="me-1">mdi-gas-station</v-icon>
        <div v-if="gasPriceGwei">{{ floorGasPriceGwei }} GWEI</div>
        <v-skeleton-loader
          v-else
          type="chip"
          max-height="17"
          max-width="55"
          tile />
      </v-btn>
    </template>
    <span>{{ $t('gasPrice') }}</span>
  </v-tooltip>
</template>
<script>
export default {
  data: () => ({
    timeout: null,
  }),
  computed: Vuex.mapState({
    address: state => state.address,
    language: state => state.language,
    gasPriceGwei: state => state.gasPriceGwei,
    provider: state => state.provider,
    validNetwork: state => state.validNetwork,
    pollingInterval: state => state.pollingInterval,
    floorGasPriceGwei() {
      if (this.gasPriceGwei < 1) {
        return this.$ethUtils.toFixedDisplay(this.gasPriceGwei, 2, this.language);
      } else {
        return parseInt(this.gasPriceGwei);
      }
    },
  }),
  watch: {
    provider() {
      this.init();
    },
  },
  created() {
    this.init();
  },
  beforeDestroy() {
    this.reset();
  },
  methods: {
    init() {
      this.reset();
      if (this.provider && this.validNetwork) {
        this.timeout = window.setInterval(() => this.refreshGasPrice(), this.pollingInterval);
      }
    },
    reset() {
      if (this.timeout) {
        window.clearInterval(this.timeout);
      }
    },
    refreshGasPrice() {
      if (this.validNetwork) {
        this.$store.commit('loadGasPrice');
      }
    },
  },
};
</script>