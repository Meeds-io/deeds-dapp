<template>
  <v-list-item class="pa-0 my-n2">
    <v-list-item-content class="py-0">
      {{ $t('deedRentingRewardsDistibuted') }}
    </v-list-item-content>
    <v-list-item-action-text class="py-0 d-flex">
      <template v-if="!confirmed && !owner">
        {{ $t('waitingForConfirmation') }}
      </template>
      <deeds-number-format
        v-else-if="distributedAmount"
        :value="distributedAmount"
        :fractions="2"
        no-decimals>
        <span v-text="$t('meedsSymbol')" class="secondary--text font-weight-bold"></span>
      </deeds-number-format>
      <template v-else>
        {{ $t('deedRentingRewardsNotDistibutedYet') }}
      </template>
    </v-list-item-action-text>
  </v-list-item>
</template>
<script>
export default {
  props: {
    confirmed: {
      type: Boolean,
      default: false,
    },
    owner: {
      type: Boolean,
      default: false,
    },
    distributedReward: {
      type: Number,
      default: null,
    },
  },
};
</script>