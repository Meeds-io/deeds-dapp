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
  <v-container class="mt-2">
    <v-row class="mx-auto" no-gutters>
      <template v-if="loading">
        <v-col
          v-for="i in poolCount"
          :key="i">
          <v-skeleton-loader
            type="card"
            class="mx-auto ma-2"
            width="400px"
            max-width="100%" />
        </v-col>
      </template>
      <template v-else>
        <v-col
          v-for="pool in rewardedPools"
          :key="`${pool.address}_${pool.refresh}`">
          <deeds-liquidity-pool :pool="pool" />
        </v-col>
      </template>
      <v-col key="cometh">
        <deeds-cometh-liquidity-pool />
      </v-col>
    </v-row>
  </v-container>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    rewardedFunds: state => state.rewardedFunds,
    rewardedPools: state => state.rewardedPools,
    lpLoading: state => state.lpLoading,
    poolCount() {
      return this.rewardedPools && this.rewardedPools.length || 1;
    },
    loading() {
      return !this.rewardedPools || this.rewardedPools.filter(pool => pool.loading).length > 0;
    },
  }),
};
</script>