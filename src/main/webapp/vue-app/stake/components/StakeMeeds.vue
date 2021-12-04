<template>
  <v-card
    class="mx-auto elevation-2"
    width="340px"
    height="400"
    flat>
    <v-card-title class="d-flex flex-column justify-center pb-2">
      <v-icon>mdi-key</v-icon>
      <span>{{ $t('meedsStakes') }}</span>
    </v-card-title>
    <v-card-text>
      <v-list-item two-line>
        <v-list-item-content>
          <v-list-item-title>
            {{ $t('apy') }}
          </v-list-item-title>
          <v-list-item-subtitle>
            -
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
      <v-list-item two-line class="mb-8">
        <v-list-item-content>
          <v-list-item-title>
            {{ $t('totalHoldings') }}
          </v-list-item-title>
          <v-list-item-subtitle class="font-weight-bold ms-2">
            {{ meedsBalanceOfXMeedsNoDecimals }} MEED
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
      <v-list-item two-line>
        <v-list-item-content>
          <v-list-item-title>
            {{ $t('availableToStake') }}
          </v-list-item-title>
          <v-list-item-subtitle class="font-weight-bold ms-2">
            {{ meedsBalanceNoDecimals }} MEED
          </v-list-item-subtitle>
        </v-list-item-content>
        <v-list-item-action>
          <v-btn
            outlined
            text
            @click="openStakeDrawer(true)">
            <span class="text-none">{{ $t('stake') }}</span>
          </v-btn>
        </v-list-item-action>
      </v-list-item>
      <v-list-item two-line>
        <v-list-item-content>
          <v-list-item-title>
            {{ $t('balance') }}
          </v-list-item-title>
          <v-list-item-subtitle class="font-weight-bold ms-2">
            {{ xMeedsBalanceNoDecimals }} xMEED
          </v-list-item-subtitle>
        </v-list-item-content>
        <v-list-item-action>
          <v-btn
            outlined
            text
            @click="openStakeDrawer(false)">
            <span class="text-none">{{ $t('unstake') }}</span>
          </v-btn>
        </v-list-item-action>
      </v-list-item>
    </v-card-text>
    <deeds-stake-meeds-drawer ref="stakeDrawer" :stake="stake" />
  </v-card>
</template>
<script>
export default {
  data: () => ({
    stake: true,
  }),
  computed: Vuex.mapState({
    language: state => state.language,
    meedsBalanceOfXMeeds: state => state.meedsBalanceOfXMeeds,
    meedsBalance: state => state.meedsBalance,
    meedsBalanceNoDecimals: state => state.meedsBalanceNoDecimals,
    xMeedsBalance: state => state.xMeedsBalance,
    xMeedsBalanceNoDecimals: state => state.xMeedsBalanceNoDecimals,
    meedsBalanceOfXMeedsNoDecimals() {
      return this.$ethUtils.computeMeedsBalanceNoDecimals(
        this.meedsBalanceOfXMeeds,
        3,
        this.language);
    },
  }),
  methods: {
    openStakeDrawer(stake) {
      this.stake = stake;
      if (this.$refs && this.$refs.stakeDrawer) {
        this.$refs.stakeDrawer.open();
      }
    },
  },
};
</script>