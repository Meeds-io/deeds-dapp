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
    <template #image>
      <v-img
        :src="imageSrc"
        max-height="40px"
        max-width="40px"
        class="ps-1"
        contain
        eager />
    </template>
    <template #col1>
      <deeds-tab-link
        :label="poolName"
        tab-link="farm"
        class="ms-n4" />
    </template>
    <template #col3>
      <div class="d-flex">
        <v-badge
          overlap
          color="transparent"
          :value="hasUnstakedLp">
          <template #badge>
            <v-tooltip bottom>
              <template #activator="{ on, attrs }">
                <div
                  class="red rounded-circle ms-1 lp-badge"
                  v-bind="attrs"
                  v-on="on">
                </div>
              </template>
              <div class="d-flex flex-nowrap">
                <deeds-number-format
                  :value="lpBalance"
                  :fractions="2"
                  class="me-1">
                  {{ lpSymbol }}
                </deeds-number-format>
                {{ $t('availableToStake') }}
              </div>
            </v-tooltip>
          </template>
          <deeds-number-format
            :value="lpStaked"
            :fractions="2">
            {{ lpSymbol }}
          </deeds-number-format>
        </v-badge>
      </div>
    </template>
    <template #col2>
      <v-tooltip bottom>
        <template #activator="{ on, attrs }">
          <div
            class="d-flex flex-nowrap me-auto"
            v-bind="attrs"
            v-on="on">
            <span class="mx-1">+</span>
            <deeds-number-format 
              :value="weeklyRewardedInMeed" 
              :fractions="2"
              class="small--text" />
            <span class="mx-1 text-no-wrap">Ɱ / {{ $t('week') }}</span>
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
        :value="userStakedEquivalentMeeds"
        :fractions="2"
        class="small--text"
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
    parentLocation: state => state.parentLocation,
    sushiswapPairAddress: state => state.sushiswapPairAddress,
    univ2PairAddress: state => state.univ2PairAddress,
    comethPairAddress: state => state.comethPairAddress,
    isSushiswapPool() {
      return this.sushiswapPairAddress && this.pool && this.pool.address && this.pool.address.toUpperCase() === this.sushiswapPairAddress.toUpperCase();
    },
    isUniswapPool() {
      return this.univ2PairAddress && this.pool && this.pool.address && this.pool.address.toUpperCase() === this.univ2PairAddress.toUpperCase();
    },
    isComethPool() {
      return this.comethPairAddress && this.pool?.address?.toUpperCase() === this.comethPairAddress.toUpperCase();
    },
    poolName() {
      if (this.isSushiswapPool) {
        return this.$t('poolFarming', {0: 'Sushiswap'});
      } else if (this.isUniswapPool) {
        return this.$t('poolFarming', {0: 'Uniswap'});
      } else if (this.isComethPool) {
        return this.$t('poolFarming', {0: 'Cometh'});
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
      return this.userInfo && this.userInfo.amount || new BigNumber(0);
    },
    lpBalance() {
      return this.userInfo?.lpBalance || 0;
    },
    lpBalanceNoDecimals() {
      return this.lpBalance && this.$ethUtils.fromDecimals(this.lpBalance, 18);
    },
    hasUnstakedLp() {
      return this.lpBalance && !this.lpBalance.isZero() && this.lpBalance.gte(ethers.BigNumber.from('10000000000000000'));
    },
    hasStakedToken() {
      return this.lpStaked && !this.lpStaked.isZero();
    },
    userStakedEquivalentMeeds() {
      return this.lpStaked && !this.lpStaked.isZero()
        && this.pool.meedsBalance.mul(this.lpStaked).mul(2).div(this.pool.totalSupply)
        || new BigNumber(0);
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
          .multipliedBy(this.apy)
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
    imageSrc() {
      return this.isSushiswapPool && `/${this.parentLocation}/static/images/sushiswap.ico`
        || this.isComethPool && `/${this.parentLocation}/static/images/cometh.ico`
        || '';
    },
  }),
};
</script>