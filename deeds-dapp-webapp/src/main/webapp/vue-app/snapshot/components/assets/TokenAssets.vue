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
  <v-list 
    dense>
    <v-list-item>
      <h4>{{ $t('yourTokens') }}</h4>
    </v-list-item>
    <v-skeleton-loader
      v-if="poolsLoading"
      type="image"
      class="mx-auto ma-2"
      max-width="90%" />
    <div v-else>
      <template v-if="hasLPTokens">
        <deeds-token-asset-template v-if="hasMeedBalance">
          <template #col1>
            <div>
              <deeds-contract-address
                :address="meedAddress"
                label="Meeds"
                token />
            </div>
          </template>
          <template #col2>
            {{ meedsBalanceNoDecimals }} MEED
          </template>
          <template #col3>
            <div class="ms-n15 d-flex justify-center">-</div>
          </template>
          <template #col4>
            <deeds-number-format
              :value="meedsBalance"
              :fractions="2"
              currency />
          </template>
        </deeds-token-asset-template>
        <deeds-token-asset-template v-if="xMeedAddress && hasXMeedBalance">
          <template #col1>
            <div>
              <deeds-contract-address
                :address="xMeedAddress"
                label="xMeeds"
                token />
            </div>
          </template>
          <template #col2>
            {{ xMeedsBalanceNoDecimals }} xMEED
          </template>
          <template #col3>
            <v-tooltip bottom>
              <template #activator="{ on, attrs }">
                <div
                  class="d-flex flex-nowrap"
                  v-bind="attrs"
                  v-on="on">
                  <span class="mx-1">+</span>
                  <deeds-number-format 
                    :value="weeklyRewardedMeeds" 
                    :fractions="2" />
                  <span class="mx-1">MEED / {{ $t('week') }}</span>
                </div>
              </template>
              <span v-if="maxMeedSupplyReached">
                {{ $t('maxMeedsSupplyReached') }}
              </span>
              <div
                class="d-flex"
                v-if="rewardsStarted">
                <span class="mx-1"> {{ $t('apy') }} </span>
                <deeds-number-format
                  :value="apy"
                  no-decimals>
                  %
                </deeds-number-format>
              </div>
              <div v-else>
                {{ $t('meedsRewardingDidntStarted') }}
              </div>
            </v-tooltip>
          </template>
          <template #col4>
            <deeds-number-format
              :value="xMeedsBalanceInMeeds"
              :fractions="2"
              currency />
          </template>
        </deeds-token-asset-template>
        <deeds-token-asset
          v-for="pool in rewardedPools"
          :key="`${pool.address}_${pool.refresh}`"
          :pool="pool" />
      </template>
      <v-row
        v-else
        class="ms-4 d-flex flex-row">
        <v-col class="pa-0" align-self="start">
          <v-img 
            height="100px"
            width="140px"
            :src="`/${parentLocation}/static/images/meeds.png`"
            contain
            eager />
        </v-col>
        <v-col cols="9">
          <v-card flat>
            <v-card-text class="py-0">
              {{ $t('noTokensDescription') }}
            </v-card-text>
            <v-card-text class="d-flex">
              <div class="pe-1">
                {{ $t('see') }}
              </div>
              <a
                class="text-decoration-underline"
                @click="$root.$emit('switch-page', 'stake')">
                {{ $t('there') }}
              </a>
              <div class="ps-1">
                {{ $t('moreInformation') }}
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </div>
  </v-list>
</template>
<script>
export default {
  data: () => ({
    tokenCount: 0,
  }),
  computed: Vuex.mapState({
    language: state => state.language,
    selectedFiatCurrency: state => state.selectedFiatCurrency,
    meedPrice: state => state.meedPrice,
    meedsBalance: state => state.meedsBalance,
    loadingMeedsBalance: state => state.loadingMeedsBalance,
    meedsBalanceNoDecimals: state => state.meedsBalanceNoDecimals,
    meedAddress: state => state.meedAddress,
    xMeedAddress: state => state.xMeedAddress,
    xMeedsBalance: state => state.xMeedsBalance,
    yearInMinutes: state => state.yearInMinutes,
    rewardedMeedPerMinute: state => state.rewardedMeedPerMinute,
    rewardedTotalAllocationPoints: state => state.rewardedTotalAllocationPoints,
    rewardedTotalFixedPercentage: state => state.rewardedTotalFixedPercentage,
    loadingXMeedsBalance: state => state.loadingXMeedsBalance,
    xMeedsBalanceNoDecimals: state => state.xMeedsBalanceNoDecimals,
    meedsBalanceOfXMeeds: state => state.meedsBalanceOfXMeeds,
    xMeedsTotalSupply: state => state.xMeedsTotalSupply,
    meedsPendingBalanceOfXMeeds: state => state.meedsPendingBalanceOfXMeeds,
    stakingStartTime: state => state.stakingStartTime,
    maxMeedSupplyReached: state => state.maxMeedSupplyReached,
    rewardedFunds: state => state.rewardedFunds,
    rewardedPools: state => state.rewardedPools,
    now: state => state.now,
    parentLocation: state => state.parentLocation,
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
            .dividedBy(100)
            .multipliedBy(100 - this.rewardedTotalFixedPercentage.toNumber());
        }
      }
      return new BigNumber(0);
    },
    rewardsStarted() {
      return this.stakingStartTime < this.now;
    },
    weeklyRewardedMeeds() {
      if (this.xMeedsBalance && this.apy) {
        return new BigNumber(this.xMeedsBalance.toString())
          .multipliedBy(this.apy)
          .dividedBy(100)
          .multipliedBy(7)
          .dividedBy(365);
      }
      return new BigNumber(0);
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
    poolsLoading() {
      return !this.rewardedPools || this.rewardedPools.filter(pool => pool.loading).length > 0;
    },
    hasMeedBalance() {
      return this.meedsBalance && !this.meedsBalance.isZero();
    },
    hasXMeedBalance() {
      return this.xMeedsBalance && !this.xMeedsBalance.isZero();
    },
    hasLPTokens() {
      return this.tokenCount && this.tokenCount > 0;
    },
  }),
  watch: {
    poolsLoading() {
      this.updateTokenNumber();
    },
  },
  mounted() {
    this.updateTokenNumber();
  },
  methods: {
    updateTokenNumber() {
      if (this.rewardedPools && !this.poolsLoading) {
        this.rewardedPools.filter(pool => {
          if (!pool.userInfo?.amount.isZero()) {
            this.tokenCount ++;
          }
        });
      }
      if (this.meedsBalance && !this.meedsBalance.isZero()) {
        this.tokenCount ++;
      }
      if (this.xMeedsBalance && !this.xMeedsBalance.isZero()) {
        this.tokenCount ++;
      }
    }
  },
};
</script>