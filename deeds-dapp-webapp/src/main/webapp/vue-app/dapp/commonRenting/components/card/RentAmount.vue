<template>
  <v-list-item class="pa-0 my-n2">
    <v-list-item-content class="py-0">
      {{ $t('deedRentingPeriodicRentPrice') }}
    </v-list-item-content>
    <v-list-item-action-text class="py-0 d-flex">
      <deeds-number-format
        :value="tokenAmount"
        :fractions="2"
        no-decimals>
        <span v-text="$t('meedsSymbol')" class="secondary--text font-weight-bold"></span>
      </deeds-number-format>
      <span class="ms-1 text-lowercase">{{ rentPeriodicityLabel }}</span>
    </v-list-item-action-text>
  </v-list-item>
</template>
<script>
export default {
  props: {
    amount: {
      type: Number,
      default: () => 0,
    },
    paymentPeriodicity: {
      type: String,
      default: null,
    },
  },
  computed: Vuex.mapState({
    tokenAmount() {
      return this.amount || 0;
    },
    rentPeriodicityLabel() {
      switch (this.paymentPeriodicity){
      case 'ONE_MONTH': return this.$t('deedRentingDurationPerMonth');
      case 'ONE_YEAR': return this.$t('deedRentingDurationPerYear');
      default: return '';
      }
    },
  }),
};
</script>