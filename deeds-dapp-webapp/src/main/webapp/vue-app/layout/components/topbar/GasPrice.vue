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
  computed: Vuex.mapState({
    address: state => state.address,
    language: state => state.language,
    gasPriceGwei: state => state.gasPriceGwei,
    floorGasPriceGwei() {
      if (this.gasPriceGwei < 1) {
        return this.$ethUtils.toFixedDisplay(this.gasPriceGwei, 2, this.language);
      } else {
        return parseInt(this.gasPriceGwei);
      }
    },
  }),
  methods: {
    refreshGasPrice() {
      this.$store.commit('loadGasPrice');
    },
  },
};
</script>