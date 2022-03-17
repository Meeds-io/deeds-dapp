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
  <v-list dense>
    <v-list-item>
      <h4>{{ $t('tokens') }}</h4>
    </v-list-item>
    <v-list-item class="ps-8">
      <v-list-item-content>Meeds</v-list-item-content>
      <v-list-item-content />
      <v-list-item-content class="align-end">
        <v-skeleton-loader
          v-if="loadingMeedsBalance"
          type="chip"
          max-height="17"
          tile />
        <template v-else>
          {{ meedsBalanceNoDecimals }} MEED
        </template>
      </v-list-item-content>
      <v-list-item-content class="align-end">
        <v-skeleton-loader
          v-if="loadingMeedsBalance"
          type="chip"
          max-height="17"
          tile />
        <deeds-number-format
          v-else
          :value="meedsBalance"
          currency />
      </v-list-item-content>
    </v-list-item>
    <v-list-item v-if="xMeedAddress" class="ps-8">
      <v-list-item-content>xMeeds</v-list-item-content>
      <v-list-item-content>
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
                v-if="rewardsStarted"
                :value="apy"
                no-decimals>
                %
              </deeds-number-format>
              <v-icon
                v-else
                size="15px"
                color="primary"
                class="ms-2">
                mdi-alert-circle-outline
              </v-icon>
            </div>
          </template>
          <span v-if="maxMeedSupplyReached">
            {{ $t('maxMeedsSupplyReached') }}
          </span>
          <ul v-else-if="rewardsStarted">
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
      </v-list-item-content>
      <v-list-item-content class="align-end">
        <v-skeleton-loader
          v-if="loadingXMeedsBalance"
          type="chip"
          max-height="17"
          tile />
        <template v-else>
          {{ xMeedsBalanceNoDecimals }} xMEED
        </template>
      </v-list-item-content>
      <v-list-item-content class="align-end">
        <v-skeleton-loader
          v-if="loadingXMeedsBalance"
          type="chip"
          max-height="17"
          tile />
        <deeds-number-format
          v-else
          :value="xMeedsBalanceInMeeds"
          currency />
      </v-list-item-content>
    </v-list-item>
  </v-list>
</template>
<script>
export default {
  data: () => ({
    yearInMinutes: 365 * 24 * 60,
  }),
  computed: Vuex.mapState({
    language: state => state.language,
    selectedFiatCurrency: state => state.selectedFiatCurrency,
    meedPrice: state => state.meedPrice,
    meedsBalance: state => state.meedsBalance,
    loadingMeedsBalance: state => state.loadingMeedsBalance,
    meedsBalanceNoDecimals: state => state.meedsBalanceNoDecimals,
    xMeedAddress: state => state.xMeedAddress,
    xMeedsBalance: state => state.xMeedsBalance,
    rewardedMeedPerMinute: state => state.rewardedMeedPerMinute,
    rewardedTotalAllocationPoints: state => state.rewardedTotalAllocationPoints,
    rewardedTotalFixedPercentage: state => state.rewardedTotalFixedPercentage,
    loadingXMeedsBalance: state => state.loadingXMeedsBalance,
    xMeedsBalanceNoDecimals: state => state.xMeedsBalanceNoDecimals,
    meedsBalanceOfXMeeds: state => state.meedsBalanceOfXMeeds,
    xMeedsTotalSupply: state => state.xMeedsTotalSupply,
    rewardedFunds: state => state.rewardedFunds,
    meedsPendingBalanceOfXMeeds: state => state.meedsPendingBalanceOfXMeeds,
    stakingStartTime: state => state.stakingStartTime,
    maxMeedSupplyReached: state => state.maxMeedSupplyReached,
    now: state => state.now,
    xMeedsBalanceInMeeds() {
      if (this.xMeedsBalance && this.xMeedsTotalSupply && !this.xMeedsTotalSupply.isZero() && this.meedsBalanceOfXMeeds) {
        return this.xMeedsBalance.mul(this.meedsBalanceOfXMeeds).div(this.xMeedsTotalSupply);
      } else {
        return 0;
      }
    },
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
      return this.stakingStartTime < this.now;
    },
    apyLoading() {
      return this.meedsBalanceOfXMeeds == null || this.rewardedFunds == null || !this.meedsPendingBalanceOfXMeeds == null;
    },
    apy() {
      if (!this.meedsTotalBalanceOfXMeeds
          || !this.yearlyRewardedMeeds
          || this.meedsTotalBalanceOfXMeeds.isZero()
          || this.yearlyRewardedMeeds.isZero()
          || this.maxMeedSupplyReached
          || !this.rewardsStarted) {
        return 0;
      }
      return this.yearlyRewardedMeeds.dividedBy(this.meedsTotalBalanceOfXMeeds.toString()).multipliedBy(100);
    },
  }),
  created() {
    this.$store.commit('loadRewardedFunds');
  },
};
</script>