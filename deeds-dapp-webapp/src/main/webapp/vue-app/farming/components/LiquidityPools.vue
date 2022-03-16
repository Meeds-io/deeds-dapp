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
  <v-container class="mt-2">
    <v-row class="mx-auto" no-gutters>
      <template v-if="rewardedFunds">
        <v-col
          v-for="pool in rewardedPools"
          :key="pool.address">
          <deeds-liquidity-pool :pool="pool" />
        </v-col>
        <v-col key="cometh">
          <deeds-liquidity-pool>
            <template #icon>
              <img
                :src="`/${parentLocation}/static/images/cometh.ico`"
                class="mx-auto addLiquidityIcon">
            </template>
            <template #title>
              {{ $t('rentLiquidityOnCometh') }}
            </template>
            <template #content>
              <v-btn
                :href="rentComethLiquidityLink"
                target="_blank"
                rel="nofollow noreferrer noopener"
                class="mx-auto d-flex"
                link
                text>
                <span class="text-capitalize link--color">{{ $t('stake') }}</span>
              </v-btn>
            </template>
          </deeds-liquidity-pool>
        </v-col>
      </template>
      <template v-else>
        <v-col
          v-for="i in 2"
          :key="i">
          <v-skeleton-loader
            type="card"
            class="mx-auto ma-2"
            width="400px"
            max-width="100%" />
        </v-col>
      </template>
    </v-row>
  </v-container>
</template>
<script>
export default {
  computed: Vuex.mapState({
    rentComethLiquidityLink: state => state.rentComethLiquidityLink,
    parentLocation: state => state.parentLocation,
    rewardedFunds: state => state.rewardedFunds,
    rewardedPools() {
      return this.rewardedFunds && this.rewardedFunds.filter(fund => fund.isLPToken);
    },
  }),
};
</script>