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
            <v-skeleton-loader
              v-if="xMeedsTotalSupply === null"
              type="chip"
              max-height="17"
              tile />
            <template v-else>{{ xMeedsTotalSupplyNoDecimals }} xMEED</template>
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
      <strong class="d-flex flex-row">
        <v-divider class="ms-8 me-2 my-auto" />
        <h6>{{ $t('myAssets') }}</h6>
        <v-divider class="me-8 ms-2 my-auto" />
      </strong>
      <v-list-item two-line class="mt-2">
        <v-list-item-content>
          <v-list-item-title>
            {{ $t('staked') }}
          </v-list-item-title>
          <v-list-item-subtitle class="font-weight-bold ms-2">
            <v-skeleton-loader
              v-if="xMeedsBalance === null"
              type="chip"
              max-height="17"
              tile />
            <template v-else>{{ xMeedsBalanceNoDecimals }} xMEED</template>
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
      <v-list-item two-line>
        <v-list-item-content>
          <v-list-item-title>
            {{ $t('earned') }}
          </v-list-item-title>
          <v-list-item-subtitle class="font-weight-bold ms-2">
            <v-skeleton-loader
              v-if="pointsBalance === null"
              type="chip"
              max-height="17"
              tile />
            <template v-else>{{ pointsBalanceNoDecimals }} {{ $t('points') }}</template>
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
      if (this.xMeedsTotalSupply) {
        return this.$ethUtils.computeTokenBalanceNoDecimals(
          this.xMeedsTotalSupply,
          3,
          this.language);
      } else {
        return '-';
      }
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