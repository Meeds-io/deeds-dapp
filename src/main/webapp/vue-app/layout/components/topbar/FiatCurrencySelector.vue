<template>
  <v-menu offset-y>
    <template v-slot:activator="{ on, attrs }">
      <v-btn
        elevation="1"
        color="grey lighten-5"
        class="ps-2 pe-0"
        v-bind="attrs"
        v-on="on">
        <div>{{ selectedFiatCurrencyLabel }}</div>
        <v-icon>mdi-menu-down</v-icon>
      </v-btn>
    </template>
    <v-list>
      <v-list-item
        v-for="fiatCurrencyOption in fiatCurrencyOptions"
        :key="fiatCurrencyOption.value"
        @click="changeFiatCurrency(fiatCurrencyOption.value)">
        <v-list-item-title>{{ fiatCurrencyOption.label }}</v-list-item-title>
      </v-list-item>
    </v-list>
  </v-menu>
</template>
<script>
export default {
  data: () => ({
    fiatCurrencies: ['usd', 'eur', 'eth'],
  }),
  computed: Vuex.mapState({
    selectedFiatCurrency: state => state.selectedFiatCurrency,
    selectedFiatCurrencyLabel() {
      return this.$t(`fiat.currency.${this.selectedFiatCurrency}`);
    },
    fiatCurrencyOptions() {
      return this.fiatCurrencies.map(currency => ({
        value: currency,
        label: this.$t(`fiat.currency.${currency}`),
      }));
    },
  }),
  methods: {
    changeFiatCurrency(fiatCurrency) {
      this.$store.commit('selectFiatCurrency', fiatCurrency);
    },
  },
};
</script>