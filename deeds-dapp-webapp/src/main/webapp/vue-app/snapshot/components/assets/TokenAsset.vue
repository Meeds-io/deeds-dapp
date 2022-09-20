<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2022 Meeds Association contact@meeds.io
 
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
  <deeds-token-asset-template v-if="hasStakedToken">
    <template #col1>
      <deeds-contract-address
        :address="lpAddress"
        :label="poolName"
        token />
    </template>
    <template #col2>
      <div
        class="d-flex">
        <deeds-number-format
          :value="lpStaked"
          :fractions="2" />
        <span class="mx-1 text-no-wrap"> {{ lpSymbol }}  </span>
      </div>
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
              :value="weeklyRewardedInMeed" 
              :fractions="2" />
            <span class="mx-1 text-no-wrap">MEED / {{ $t('week') }}</span>
          </div>
        </template>
        <span v-if="noMeedSupplyForLPRemaining">
          {{ $t('maxMeedsSupplyReached') }}
        </span>
        <div class="d-flex flex-row">
          <span class="mx-1 text-no-wrap"> {{ $t('apy') }} </span>
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
        :value="lpStaked"
        :fractions="2"
        currency />
    </template>
  </deeds-token-asset-template>
</template>
<script>
export default {
  props: {
    pool: {
      type: Object,
      default: null,
    },
  },
  computed: Vuex.mapState({
    sushiswapPairAddress: state => state.sushiswapPairAddress,
    univ2PairAddress: state => state.univ2PairAddress,
    isSushiswapPool() {
      return this.sushiswapPairAddress && this.pool && this.pool.address && this.pool.address.toUpperCase() === this.sushiswapPairAddress.toUpperCase();
    },
    isUniswapPool() {
      return this.univ2PairAddress && this.pool && this.pool.address && this.pool.address.toUpperCase() === this.univ2PairAddress.toUpperCase();
    },
    poolName() {
      if (this.isSushiswapPool) {
        return 'Sushiswap';
      } else if (this.isUniswapPool) {
        return 'Uniswap';
      }
      return this.lpSymbol;
    },
    lpAddress() {
      return this.pool && this.pool.address;
    },
    lpSymbol() {
      return this.pool && this.pool.symbol;
    },
    userInfo() {
      return this.pool && this.pool.userInfo;
    },
    lpStaked() {
      return this.userInfo && this.userInfo.amount || 0;
    },
    lpTotalSupply() {
      return this.pool && this.pool.totalSupply;
    },
    lpMeedsBalance() {
      return this.pool && this.pool.meedsBalance;
    },
    loadingUserInfo() {
      return this.pool && this.pool.loadingUserInfo;
    },
    apy() {
      return this.pool && this.pool.apy;
    },
    weeklyRewardedInSLP() {
      if (this.lpStaked && this.apy) {
        return new BigNumber(this.lpStaked.toString())
          .multipliedBy(Math.trunc(this.apy))
          .dividedBy(100)
          .multipliedBy(7)
          .dividedBy(365);
      }
      return new BigNumber(0);
    },
    weeklyRewardedInMeed() {
      if (this.weeklyRewardedInSLP && this.lpTotalSupply && this.lpMeedsBalance) {
        return this.weeklyRewardedInSLP
          .dividedBy(this.lpTotalSupply.toString())
          .multipliedBy(this.lpMeedsBalance.toString())
          .multipliedBy(2);
      }
      return 0;
    },
    hasStakedToken() {
      return this.lpStaked && !this.lpStaked.isZero();
    },
  }),
};
</script>