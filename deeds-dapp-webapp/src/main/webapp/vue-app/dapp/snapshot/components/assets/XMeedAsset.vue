<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 
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
  <deeds-token-asset-template>
    <template #image>
      <v-img
        :src="`/${parentLocation}/static/images/xMeedsToken.png`"
        max-height="40px"
        max-width="40px"
        contain
        eager />
    </template>
    <template #col1>
      <deeds-tab-link
        label="xMeeds"
        tab-link="stake"
        class="ms-n4"
        no-title-text-transform />
    </template>
    <template #col3>
      <deeds-number-format
        :value="xMeedsBalance"
        :fractions="2">
        xⱮ
      </deeds-number-format>
    </template>
    <template #col2>
      <v-tooltip bottom>
        <template #activator="{ on, attrs }">
          <div
            class="d-flex flex-nowrap"
            v-bind="attrs"
            v-on="on">
            <span class="mx-1">+</span>
            <deeds-number-format 
              :value="weeklyRewardedInMeed" 
              :fractions="2"
              class="small--text" />
            <span class="mx-1">Ɱ / {{ $t('week') }}</span>
          </div>
        </template>
        <span v-if="maxMeedSupplyReached">
          {{ $t('maxMeedsSupplyReached') }}
        </span>
        <div class="d-flex">
          <span class="mx-1"> {{ $t('apy') }} </span>
          <deeds-number-format
            :value="apy"
            no-decimals>
            %
          </deeds-number-format>
        </div>
      </v-tooltip>
    </template>
    <template #col4>
      <deeds-number-format
        :value="xMeedsBalanceInMeeds"
        :fractions="2"
        class="small--text"
        currency />
    </template>
  </deeds-token-asset-template>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    xMeedAddress: state => state.xMeedAddress,
    xMeedsBalance: state => state.xMeedsBalance,
    xMeedsTotalSupply: state => state.xMeedsTotalSupply,
    meedsBalanceOfXMeeds: state => state.meedsBalanceOfXMeeds,
    rewardedFunds: state => state.rewardedFunds,
    yearInMinutes: state => state.yearInMinutes,
    rewardedMeedPerMinute: state => state.rewardedMeedPerMinute,
    rewardedTotalAllocationPoints: state => state.rewardedTotalAllocationPoints,
    rewardedTotalFixedPercentage: state => state.rewardedTotalFixedPercentage,
    meedsPendingBalanceOfXMeeds: state => state.meedsPendingBalanceOfXMeeds,
    maxMeedSupplyReached: state => state.maxMeedSupplyReached,
    xMeedRewardInfo() {
      return this.rewardedFunds && this.xMeedAddress && this.rewardedFunds.find(fund => fund.address.toUpperCase() === this.xMeedAddress.toUpperCase());
    },
    xMeedsBalanceInMeeds() {
      if (this.xMeedsBalance && this.xMeedsTotalSupply && !this.xMeedsTotalSupply.isZero() && this.meedsTotalBalanceOfXMeeds) {
        return this.xMeedsBalance.mul(this.meedsTotalBalanceOfXMeeds).div(this.xMeedsTotalSupply);
      } else {
        return 0;
      }
    },
    weeklyRewardedInXMeed() {
      if (this.xMeedsBalance && this.apy) {
        return new BigNumber(this.xMeedsBalance.toString())
          .multipliedBy(this.apy)
          .dividedBy(100)
          .multipliedBy(7)
          .dividedBy(365);
      }
      return new BigNumber(0);
    },
    weeklyRewardedInMeed() {
      if (this.weeklyRewardedInXMeed && this.xMeedsTotalSupply && this.meedsBalanceOfXMeeds) {
        return this.weeklyRewardedInXMeed
          .dividedBy(this.xMeedsTotalSupply.toString())
          .multipliedBy(this.meedsBalanceOfXMeeds.toString());
      }
      return 0;
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
    apy() {
      if (!this.meedsTotalBalanceOfXMeeds
          || !this.yearlyRewardedMeeds
          || this.meedsTotalBalanceOfXMeeds.isZero()
          || this.yearlyRewardedMeeds.isZero()
          || this.maxMeedSupplyReached) {
        return 0;
      }
      return this.yearlyRewardedMeeds.dividedBy(this.meedsTotalBalanceOfXMeeds.toString()).multipliedBy(100);
    },
  }),
};
</script>