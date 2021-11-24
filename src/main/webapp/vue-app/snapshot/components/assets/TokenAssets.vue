<template>
  <v-list dense>
    <v-list-item>
      <h4>{{ $t('tokens') }}</h4>
    </v-list-item>
    <v-list-item class="ps-8">
      <v-list-item-content>Meeds</v-list-item-content>
      <v-list-item-content />
      <v-list-item-content class="align-end">{{ meedsBalanceNoDecimals }} MEED</v-list-item-content>
      <v-list-item-content class="align-end">{{ meedsBalanceFiat }}</v-list-item-content>
    </v-list-item>
    <v-list-item class="ps-8">
      <v-list-item-content>xMeeds</v-list-item-content>
      <v-list-item-content>-</v-list-item-content>
      <v-list-item-content class="align-end">{{ xMeedsBalanceNoDecimals }} xMEED</v-list-item-content>
      <v-list-item-content class="align-end">{{ xMeedsBalanceFiat }}</v-list-item-content>
    </v-list-item>
  </v-list>
</template>
<script>
export default {
  computed: Vuex.mapState({
    language: state => state.language,
    selectedFiatCurrency: state => state.selectedFiatCurrency,
    meedsPrice: state => state.meedsPrice,
    ethPrice: state => state.ethPrice,
    exchangeRate: state => state.exchangeRate,
    meedsBalance: state => state.meedsBalance,
    xMeedsBalance: state => state.xMeedsBalance,
    xMeedsBalanceNoDecimals() {
      return this.computeMeedsBalanceNoDecimals(
        this.xMeedsBalance,
        3);
    },
    xMeedsBalanceFiat() {
      return this.computeFiatBalance(
        this.xMeedsBalance,
        this.meedsPrice,
        this.ethPrice,
        this.exchangeRate,
        this.selectedFiatCurrency,
        this.language);
    },
    meedsBalanceNoDecimals() {
      return this.computeMeedsBalanceNoDecimals(
        this.meedsBalance,
        3);
    },
    meedsBalanceFiat() {
      return this.computeFiatBalance(
        this.meedsBalance,
        this.meedsPrice,
        this.ethPrice,
        this.exchangeRate,
        this.selectedFiatCurrency,
        this.language);
    },
  }),
  methods: {
    computeMeedsBalanceNoDecimals(meedsBalance, fractions) {
      if (meedsBalance) {
        const meedsBalanceNoDecimals = this.$ethUtils.fromDecimals(meedsBalance, 18);
        return this.$ethUtils.fractionsToDisplay(meedsBalanceNoDecimals, fractions);
      } else {
        return '-';
      }
    },
    computeFiatBalance(meedsBalance, meedsPrice, ethPrice, exchangeRate, selectedFiatCurrency, language) {
      if (meedsPrice && meedsBalance) {
        const meedsBalanceNoDecimals = this.$ethUtils.fromDecimals(meedsBalance, 18);
        if (selectedFiatCurrency === 'eur') {
          const meedsBalanceEur = this.$ethUtils.fractionsToDisplay(new BigNumber(meedsBalanceNoDecimals).multipliedBy(meedsPrice).multipliedBy(ethPrice).multipliedBy(new BigNumber(exchangeRate)), 3);
          return this.$ethUtils.toCurrencyDisplay(meedsBalanceEur, selectedFiatCurrency, language);
        } else if (selectedFiatCurrency === 'usd' && ethPrice) {
          const meedsBalanceUsd = this.$ethUtils.fractionsToDisplay(new BigNumber(meedsBalanceNoDecimals).multipliedBy(meedsPrice).multipliedBy(ethPrice), 3);
          return this.$ethUtils.toCurrencyDisplay(meedsBalanceUsd, selectedFiatCurrency, language);
        } else if (selectedFiatCurrency === 'eth') {
          const meedsBalanceEth = this.$ethUtils.fractionsToDisplay(new BigNumber(meedsBalanceNoDecimals).multipliedBy(meedsPrice), 8);
          return this.$ethUtils.toCurrencyDisplay(meedsBalanceEth, selectedFiatCurrency, language);
        }
      }
      return '-';
    },
  },
};
</script>