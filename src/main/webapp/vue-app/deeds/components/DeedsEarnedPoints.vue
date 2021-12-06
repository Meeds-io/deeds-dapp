<template>
  <v-card
    width="340"
    height="350"
    class="elevation-2"
    flat>
    <v-card-title class="d-flex flex-column justify-center">
      <v-icon>mdi-office-building-outline</v-icon>
      <span>{{ $t('xMeedsStakes') }}</span>
    </v-card-title>
    <v-card-text>
      <v-list-item two-line class="mb-2">
        <v-list-item-content>
          <v-list-item-title>
            {{ $t('totalHoldings') }}
          </v-list-item-title>
          <v-list-item-subtitle class="font-weight-bold ms-2">
            {{ xMeedsTotalSupplyNoDecimals }} xMEED
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
      <v-list-item two-line>
        <v-list-item-content>
          <v-list-item-title>
            {{ $t('staked') }}
          </v-list-item-title>
          <v-list-item-subtitle class="font-weight-bold ms-2">
            {{ xMeedsBalanceNoDecimals }} xMEED
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
      <v-list-item two-line>
        <v-list-item-content>
          <v-list-item-title>
            {{ $t('earned') }}
          </v-list-item-title>
          <v-list-item-subtitle class="font-weight-bold ms-2">
            {{ pointsBalanceNoDecimals }} {{ $t('points') }}
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
    </v-card-text>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    stake: true,
  }),
  computed: Vuex.mapState({
    language: state => state.language,
    xMeedsTotalSupply: state => state.xMeedsTotalSupply,
    meedsBalance: state => state.meedsBalance,
    xMeedsBalance: state => state.xMeedsBalance,
    pointsBalance: state => state.pointsBalance,
    xMeedsBalanceNoDecimals: state => state.xMeedsBalanceNoDecimals,
    xMeedsTotalSupplyNoDecimals() {
      return this.$ethUtils.computeTokenBalanceNoDecimals(
        this.xMeedsTotalSupply,
        3,
        this.language);
    },
    pointsBalanceNoDecimals() {
      return this.$ethUtils.computeTokenBalanceNoDecimals(
        this.pointsBalance,
        3,
        this.language);
    },
  }),
};
</script>