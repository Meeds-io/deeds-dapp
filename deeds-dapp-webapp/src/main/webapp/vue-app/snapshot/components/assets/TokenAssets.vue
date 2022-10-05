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
  <v-list 
    dense>
    <v-list-item>
      <h4>{{ $t('yourTokens') }}</h4>
    </v-list-item>
    <v-skeleton-loader
      v-if="loading"
      type="image"
      class="mx-auto ma-2"
      max-width="90%" />
    <div v-else-if="hasTokens">
      <deeds-meed-asset v-if="hasMeedBalance" />
      <deeds-x-meed-asset v-if="hasXMeedBalance" />
      <deeds-liquidity-pool-asset
        v-for="pool in rewardedPools"
        :key="`${pool.address}_${pool.refresh}`"
        :pool="pool" />
    </div>
    <deeds-empty-token-assets v-else />
  </v-list>
</template>
<script>
export default {
  computed: Vuex.mapState({
    meedsBalance: state => state.meedsBalance,
    xMeedsBalance: state => state.xMeedsBalance,
    tokenLoading: state => state.tokenLoading,
    lpLoading: state => state.lpLoading,
    rewardedPools: state => state.rewardedPools,
    loading() {
      return this.poolsLoading || this.tokenLoading;
    },
    poolsLoading() {
      return this.lpLoading || (this.rewardedPools !== null && this.rewardedPools.filter(pool => pool.loading).length > 0);
    },
    hasMeedBalance() {
      return !this.tokenLoading && this.meedsBalance && !this.meedsBalance.isZero();
    },
    hasXMeedBalance() {
      return !this.tokenLoading && this.xMeedsBalance && !this.xMeedsBalance.isZero();
    },
    hasLPTokens() {
      return !this.poolsLoading && this.rewardedPools?.length && this.rewardedPools.find(pool => {
        const amount = pool?.userInfo?.amount;
        return amount && !amount.isZero();
      });
    },
    hasTokens() {
      return !this.loading && (this.hasMeedBalance || this.hasXMeedBalance || this.hasLPTokens);
    },
  }),
};
</script>