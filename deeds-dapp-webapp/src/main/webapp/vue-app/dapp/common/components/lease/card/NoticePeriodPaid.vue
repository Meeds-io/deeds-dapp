<template>
  <v-list-item class="pa-0 my-n2">
    <v-list-item-content class="py-0 d-flex flex-nowrap text-no-wrap">
      {{ $t('deedRentingNoticePeriodPaid') }}
      <v-list-item-action-text
        v-if="noticePeriodAmount"
        class="py-0 ms-2">
        ({{ noticePeriodMonthsLabel }})
      </v-list-item-action-text>
    </v-list-item-content>
    <v-list-item-action-text class="py-0">
      <template v-if="!confirmed">
        {{ $t('waitingForConfirmation') }}
      </template>
      <deeds-number-format
        v-else-if="noticePeriodAmount"
        :value="noticePeriodAmount"
        :fractions="2"
        no-decimals
        class="ms-2">
        <span v-text="$t('meedsSymbol')" class="secondary--text font-weight-bold"></span>
      </deeds-number-format>
      <template v-else>{{ $t('deedRentingDurationNoNotice') }}</template>
    </v-list-item-action-text>
  </v-list-item>
</template>
<script>
export default {
  props: {
    noticePeriodMonths: {
      type: Number,
      default: () => 0,
    },
    rentAmount: {
      type: Number,
      default: () => 0,
    },
    confirmed: {
      type: Boolean,
      default: false,
    },
  },
  computed: Vuex.mapState({
    noticePeriodMonthsLabel() {
      if (this.noticePeriodMonths === 1) {
        return this.$t('deedRentingRemainingTimeOneMonth');
      } else if (this.noticePeriodMonths > 1) {
        return this.$t('deedRentingRemainingTimeMonths', {0: this.noticePeriodMonths});
      }
      return null;
    },
    noticePeriodAmount() {
      return this.noticePeriodMonths * this.rentAmount;
    },
  }),
};
</script>