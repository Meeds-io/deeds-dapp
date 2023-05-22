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
  <v-list dense class="pb-16">
    <div class="d-flex flex-nowrap align-center">
      <v-img
        :src="`${parentLocation}/static/images/meeds.png`"
        height="30px"
        max-width="81px"
        contain
        eager />
      <span class="headline font-weight-bold align-center ms-5 mb-1">
        {{ $t('tokens.title') }}
      </span>
      <v-spacer />
      <v-btn
        :href="stakeURL"
        class="rounded-pill px-6"
        color="primary"
        height="45px"
        outlined>
        <h4>{{ $t('stake') }}</h4>
      </v-btn>
    </div>
    <v-skeleton-loader
      v-if="loading"
      type="image"
      class="me-4 my-2"
      max-width="100%"
      max-height="80px" />
    <div v-else-if="hasTokens">
      <deeds-meed-asset v-if="hasMeedBalance" />
      <deeds-polygon-meed-asset v-if="hasPolygonMeedBalance" />
      <deeds-x-meed-asset v-if="hasXMeedBalance" />
      <deeds-liquidity-pool-asset
        v-for="pool in rewardedPools"
        :key="`${pool.address}_${pool.refresh}`"
        :pool="pool" />
      <deeds-liquidity-pool-asset :pool="comethPool" />
    </div>
    <deeds-empty-assets
      v-else
      id="emptyTokenAssets"
      description-part1="noTokensDescriptionPart1"
      description-part2="noTokensDescriptionPart2"
      link-part1="becomingAHolderLink"
      link-part2="stakeTabLink"
      target-tab="stake" />
  </v-list>
</template>
<script>
export default {
  computed: Vuex.mapState({
    meedsBalance: state => state.meedsBalance,
    polygonMeedsBalance: state => state.polygonMeedsBalance,
    xMeedsBalance: state => state.xMeedsBalance,
    tokenLoading: state => state.tokenLoading,
    lpLoading: state => state.lpLoading,
    rewardedPools: state => state.rewardedPools,
    comethPool: state => state.comethPool,
    parentLocation: state => state.parentLocation,
    stakeURL: state => state.stakeURL,
    loading() {
      return this.poolsLoading || this.tokenLoading;
    },
    poolsLoading() {
      return this.lpLoading || (this.rewardedPools !== null && this.rewardedPools.filter(pool => pool.loading).length > 0);
    },
    hasMeedBalance() {
      return !this.tokenLoading && this.meedsBalance && !this.meedsBalance.isZero();
    },
    hasPolygonMeedBalance() {
      return !this.tokenLoading && this.polygonMeedsBalance && !this.polygonMeedsBalance.isZero();
    },
    hasXMeedBalance() {
      return !this.tokenLoading && this.xMeedsBalance && !this.xMeedsBalance.isZero();
    },
    hasLPTokens() {
      return (this.comethPool?.userInfo?.amount && !this.comethPool?.userInfo?.amount?.isZero()) || (!this.poolsLoading && this.rewardedPools?.length && this.rewardedPools.find(pool => {
        const amount = pool?.userInfo?.amount;
        return amount && !amount.isZero();
      }));
    },
    hasTokens() {
      return !this.loading && (this.hasMeedBalance || this.hasPolygonMeedBalance || this.hasXMeedBalance || this.hasLPTokens);
    },
  }),
  created() {
    this.$store.commit('loadComethRewardPool');
  },
};
</script>