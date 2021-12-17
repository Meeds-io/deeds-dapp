<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
<template>
  <v-card
    width="340"
    height="350"
    outlined>
    <v-card-title class="d-flex flex-column justify-center pb-2">
      <v-icon>mdi-key</v-icon>
      <span>{{ $t('meedsStakes') }}</span>
    </v-card-title>
    <v-card-text>
      <v-list-item>
        <v-list-item-content class="pb-0">
          <v-list-item-title>
            {{ $t('apy') }}
          </v-list-item-title>
          <v-list-item-subtitle class="font-weight-bold ms-2">
            <v-skeleton-loader
              v-if="apyLoading"
              type="chip"
              max-height="17"
              tile />
            <v-tooltip v-else bottom>
              <template v-slot:activator="{ on, attrs }">
                <div
                  class="d-flex flex-nowrap"
                  v-bind="attrs"
                  v-on="on">
                  <deeds-number-format
                    :value="apy"
                    no-decimals>
                    %
                  </deeds-number-format>
                  <v-icon
                    v-if="!rewardsStarted"
                    size="15px"
                    color="primary"
                    class="ms-2">
                    mdi-alert-circle-outline
                  </v-icon>
                </div>
              </template>
              <ul v-if="rewardsStarted">
                <li>
                  <deeds-number-format :value="yearlyRewardedMeeds" label="yearlyRewardedMeeds" />
                </li>
                <li>
                  <deeds-number-format :value="meedsTotalBalanceOfXMeeds" label="meedsTotalBalanceOfXMeeds" />
                </li>
              </ul>
              <div v-else>
                {{ $t('meedsRewardingDidntStarted') }}
              </div>
            </v-tooltip>
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
      <v-list-item two-line>
        <v-list-item-content>
          <v-list-item-title>
            {{ $t('totalHoldings') }}
          </v-list-item-title>
          <v-list-item-subtitle class="font-weight-bold ms-2">
            <v-skeleton-loader
              v-if="meedsBalanceOfXMeeds === null || meedsPendingBalanceOfXMeeds == null"
              type="chip"
              max-height="17"
              tile />
            <v-tooltip v-else bottom>
              <template v-slot:activator="{ on, attrs }">
                <div
                  v-bind="attrs"
                  v-on="on">
                  <deeds-number-format :value="meedsTotalBalanceOfXMeeds">
                    MEED
                  </deeds-number-format>
                </div>
              </template>
              <ul>
                <li>
                  <deeds-number-format :value="meedsBalanceOfXMeeds" label="xMeedCurrentBalance" />
                </li>
                <li>
                  <deeds-number-format :value="meedsPendingBalanceOfXMeeds" label="xMeedPendingRewards" />
                </li>
              </ul>
            </v-tooltip>
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
      <strong class="d-flex flex-row">
        <v-divider class="ms-8 me-2 my-auto" />
        <h6>{{ $t('myAssets') }}</h6>
        <v-divider class="me-8 ms-2 my-auto" />
      </strong>
      <v-list-item two-line>
        <v-list-item-content>
          <v-list-item-title>
            {{ $t('availableToStake') }}
          </v-list-item-title>
          <v-list-item-subtitle class="font-weight-bold ms-2">
            <v-skeleton-loader
              v-if="meedsBalance === null"
              type="chip"
              max-height="17"
              tile />
            <template v-else>
              {{ meedsBalanceNoDecimals }} MEED
            </template>
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
            <v-skeleton-loader
              v-if="xMeedsBalance === null"
              type="chip"
              max-height="17"
              tile />
            <template v-else>
              {{ xMeedsBalanceNoDecimals }} xMEED
            </template>
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
    refreshInterval: null,
    now: Date.now(),
    yearInMinutes: 365 * 24 * 60,
  }),
  computed: Vuex.mapState({
    meedsBalanceOfXMeeds: state => state.meedsBalanceOfXMeeds,
    meedsBalance: state => state.meedsBalance,
    meedsBalanceNoDecimals: state => state.meedsBalanceNoDecimals,
    xMeedAddress: state => state.xMeedAddress,
    xMeedsBalance: state => state.xMeedsBalance,
    xMeedsBalanceNoDecimals: state => state.xMeedsBalanceNoDecimals,
    meedsPendingBalanceOfXMeeds: state => state.meedsPendingBalanceOfXMeeds,
    rewardedMeedPerMinute: state => state.rewardedMeedPerMinute,
    rewardedFunds: state => state.rewardedFunds,
    rewardedTotalAllocationPoints: state => state.rewardedTotalAllocationPoints,
    rewardedTotalFixedPercentage: state => state.rewardedTotalFixedPercentage,
    meedsStartRewardsTime: state => state.meedsStartRewardsTime,
    xMeedRewardInfo() {
      return this.rewardedFunds && this.xMeedAddress && this.rewardedFunds.find(fund => fund.address.toUpperCase() === this.xMeedAddress.toUpperCase());
    },
    meedsTotalBalanceOfXMeeds() {
      return this.meedsBalanceOfXMeeds
        && this.meedsPendingBalanceOfXMeeds
        && this.meedsBalanceOfXMeeds.add(this.meedsPendingBalanceOfXMeeds)
        || 0;
    },
    yearlyRewardedMeeds() {
      if (this.xMeedRewardInfo) {
        if (this.xMeedRewardInfo.fixedPercentage && !this.xMeedRewardInfo.fixedPercentage.isZero()) {
          return new BigNumber(this.rewardedMeedPerMinute.toString())
            .multipliedBy(this.yearInMinutes)
            .multipliedBy(this.xMeedRewardInfo.fixedPercentage.toString())
            .dividedBy(100);
        } else if (this.xMeedRewardInfo.allocationPoint && !this.xMeedRewardInfo.allocationPoint.isZero()) {
          return new BigNumber(this.rewardedMeedPerMinute.toString())
            .multipliedBy(this.yearInMinutes)
            .multipliedBy(this.xMeedRewardInfo.allocationPoint.toString())
            .dividedBy(this.rewardedTotalAllocationPoints.toString())
            .multipliedBy(100)
            .dividedBy(100 - this.rewardedTotalFixedPercentage.toNumber());
        }
      }
      return new BigNumber(0);
    },
    rewardsStarted() {
      return this.meedsStartRewardsTime < this.now;
    },
    apyLoading() {
      return !this.meedsBalanceOfXMeeds || !this.rewardedFunds || !this.meedsPendingBalanceOfXMeeds;
    },
    apy() {
      if (!this.meedsTotalBalanceOfXMeeds
          || !this.yearlyRewardedMeeds
          || this.meedsTotalBalanceOfXMeeds.isZero()
          || this.yearlyRewardedMeeds.isZero()
          || !this.rewardsStarted) {
        return 0;
      }
      return this.yearlyRewardedMeeds.dividedBy(this.meedsTotalBalanceOfXMeeds.toString()).multipliedBy(100);
    },
  }),
  watch: {
    rewardsStarted() {
      if (this.rewardsStarted && this.refreshInterval) {
        window.clearInterval(this.refreshInterval);
      }
    },
  },
  created() {
    if (!this.rewardsStarted) {
      this.refreshInterval = window.setInterval(() => {
        this.now = Date.now();
      }, 1000);
    }
  },
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