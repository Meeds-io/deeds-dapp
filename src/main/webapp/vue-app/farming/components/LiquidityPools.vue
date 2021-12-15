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
          <deeds-liquidity-pool :pool="pool" class="mx-2" />
        </v-col>
      </template>
      <template v-else>
        <v-col
          v-for="i in 4"
          :key="i">
          <v-skeleton-loader type="card" class="mx-2" />
        </v-col>
      </template>
    </v-row>
  </v-container>
</template>
<script>
export default {
  computed: Vuex.mapState({
    rewardedFunds: state => state.rewardedFunds,
    rewardedPools() {
      return this.rewardedFunds && this.rewardedFunds.filter(fund => fund.isLPToken);
    },
  }),
};
</script>