<template>
  <v-list-item class="pa-0 my-n2">
    <v-list-item-content
      v-if="monthPaymentInProgress"
      class="py-0 mt-n4">
      <div class="mt-4">
        <v-badge
          color="transparent"
          bordered>
          <template #badge>
            <v-tooltip top left>
              <template #activator="{ on, attrs }">
                <v-btn
                  icon
                  x-small
                  class="error position-relative-1"
                  dark
                  v-bind="attrs"
                  v-on="on">
                  {{ monthPaymentInProgress }}
                </v-btn>
              </template>
              <span>{{ $t('monthsPaymentTransactionInProgressTooltip') }}</span>
            </v-tooltip>
          </template>
          {{ $t('deedRentingCumulativeRentsPaid') }}
        </v-badge>
      </div>
    </v-list-item-content>
    <v-list-item-content v-else class="py-0">
      {{ $t('deedRentingCumulativeRentsPaid') }}
    </v-list-item-content>
    <v-list-item-action-text class="py-0">
      <template v-if="!confirmed">
        {{ $t('waitingForConfirmation') }}
      </template>
      <deeds-number-format
        v-else
        :value="cumulativeRentsPaid"
        :fractions="2"
        no-decimals>
        <span v-text="$t('meedsSymbol')" class="secondary--text font-weight-bold"></span>
      </deeds-number-format>
    </v-list-item-action-text>
  </v-list-item>
</template>
<script>
export default {
  props: {
    monthPaymentInProgress: {
      type: Number,
      default: () => 0,
    },
    cumulativeRentsPaid: {
      type: Number,
      default: () => 0,
    },
    confirmed: {
      type: Boolean,
      default: false,
    },
  },
};
</script>