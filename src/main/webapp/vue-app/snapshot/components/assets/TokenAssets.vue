<template>
  <v-list dense>
    <v-list-item>
      <h4>{{ $t('tokens') }}</h4>
    </v-list-item>
    <v-list-item class="ps-8">
      <v-list-item-content>Meeds</v-list-item-content>
      <v-list-item-content />
      <v-list-item-content class="align-end">
        <v-skeleton-loader
          v-if="meedsBalance === null"
          type="chip"
          max-height="17"
          tile />
        <template v-else>
          {{ meedsBalanceNoDecimals }} MEED
        </template>
      </v-list-item-content>
      <v-list-item-content class="align-end">
        <v-skeleton-loader
          v-if="meedsBalance === null"
          type="chip"
          max-height="17"
          tile />
        <template v-else>
          {{ meedsBalanceFiat }}
        </template>
      </v-list-item-content>
    </v-list-item>
    <v-list-item class="ps-8">
      <v-list-item-content>xMeeds</v-list-item-content>
      <v-list-item-content>-</v-list-item-content>
      <v-list-item-content class="align-end">
        <v-skeleton-loader
          v-if="xMeedsBalance === null"
          type="chip"
          max-height="17"
          tile />
        <template v-else>
          {{ xMeedsBalanceNoDecimals }} xMEED
        </template>
      </v-list-item-content>
      <v-list-item-content class="align-end">
        <v-skeleton-loader
          v-if="xMeedsBalance === null"
          type="chip"
          max-height="17"
          tile />
        <template v-else>
          {{ xMeedsBalanceFiat }}
        </template>
      </v-list-item-content>
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
    meedsBalanceNoDecimals: state => state.meedsBalanceNoDecimals,
    xMeedsBalance: state => state.xMeedsBalance,
    xMeedsBalanceNoDecimals: state => state.xMeedsBalanceNoDecimals,
    meedsBalanceOfXMeeds: state => state.meedsBalanceOfXMeeds,
    xMeedsTotalSupply: state => state.xMeedsTotalSupply,
    xMeedsBalanceFiat() {
      if (this.xMeedsBalance && this.xMeedsTotalSupply && this.meedsBalanceOfXMeeds) {
        const meedsBalance = this.xMeedsBalance.mul(this.meedsBalanceOfXMeeds).div(this.xMeedsTotalSupply);
        return this.$ethUtils.computeFiatBalance(
          meedsBalance,
          this.meedsPrice,
          this.ethPrice,
          this.exchangeRate,
          this.selectedFiatCurrency,
          this.language);
      } else {
        return 0;
      }
    },
    meedsBalanceFiat() {
      return this.$ethUtils.computeFiatBalance(
        this.meedsBalance,
        this.meedsPrice,
        this.ethPrice,
        this.exchangeRate,
        this.selectedFiatCurrency,
        this.language);
    },
  }),
};
</script>