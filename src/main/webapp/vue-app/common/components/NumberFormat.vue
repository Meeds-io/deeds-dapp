<template>
  <div v-if="display">
    <template v-if="label">
      {{ $t(label, {0: formattedValue}) }}
    </template>
    <template v-else>
      {{ formattedValue }}
    </template>
    <slot></slot>
  </div>
</template>

<script>
export default {
  props: {
    value: {
      type: Number,
      default: null,
    },
    label: {
      type: Number,
      default: null,
    },
    fractions: {
      type: Number,
      default: 2,
    },
    currency: {
      type: Boolean,
      default: false,
    },
    hideZero: {
      type: Boolean,
      default: false,
    },
  },
  computed: Vuex.mapState({
    language: state => state.language,
    meedsPrice: state => state.meedsPrice,
    ethPrice: state => state.ethPrice,
    exchangeRate: state => state.exchangeRate,
    selectedFiatCurrency: state => state.selectedFiatCurrency,
    display() {
      return !this.hideZero || (this.value && (!this.value.isZero || !this.value.isZero()));
    },
    formattedValue() {
      if (this.currency) {
        return this.$ethUtils.computeFiatBalance(
          this.value || 0,
          this.meedsPrice,
          this.ethPrice,
          this.exchangeRate,
          this.selectedFiatCurrency,
          this.language);
      } else {
        return this.$ethUtils.computeTokenBalanceNoDecimals(
          this.value || 0,
          this.fractions,
          this.language);
      }
    },
  }),
};
</script>