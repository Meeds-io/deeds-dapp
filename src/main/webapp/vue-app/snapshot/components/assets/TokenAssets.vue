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
          v-if="meedsBalance === null"
          type="chip"
          max-height="17"
          tile />
        <template v-else>
          {{ meedsBalanceNoDecimals }} MEED
        </template>
      </v-list-item-content>
      <v-list-item-content class="align-end">
        <v-skeleton-loader
          v-if="meedsBalance === null"
          type="chip"
          max-height="17"
          tile />
        <template v-else>
          {{ meedsBalanceFiat }}
        </template>
      </v-list-item-content>
    </v-list-item>
    <v-list-item class="ps-8">
      <v-list-item-content>xMeeds</v-list-item-content>
      <v-list-item-content>-</v-list-item-content>
      <v-list-item-content class="align-end">
        <v-skeleton-loader
          v-if="xMeedsBalance === null"
          type="chip"
          max-height="17"
          tile />
        <template v-else>
          {{ xMeedsBalanceNoDecimals }} xMEED
        </template>
      </v-list-item-content>
      <v-list-item-content class="align-end">
        <v-skeleton-loader
          v-if="xMeedsBalance === null"
          type="chip"
          max-height="17"
          tile />
        <template v-else>
          {{ xMeedsBalanceFiat }}
        </template>
      </v-list-item-content>
    </v-list-item>
  </v-list>
</template>
<script>
export default {
  computed: Vuex.mapState({
    language: state => state.language,
    selectedFiatCurrency: state => state.selectedFiatCurrency,
    meedsPrice: state => state.meedsPrice,
    ethPrice: state => state.ethPrice,
    exchangeRate: state => state.exchangeRate,
    meedsBalance: state => state.meedsBalance,
    meedsBalanceNoDecimals: state => state.meedsBalanceNoDecimals,
    xMeedsBalance: state => state.xMeedsBalance,
    xMeedsBalanceNoDecimals: state => state.xMeedsBalanceNoDecimals,
    meedsBalanceOfXMeeds: state => state.meedsBalanceOfXMeeds,
    xMeedsTotalSupply: state => state.xMeedsTotalSupply,
    xMeedsBalanceFiat() {
      if (this.xMeedsBalance && this.xMeedsTotalSupply && !this.xMeedsTotalSupply.isZero() && this.meedsBalanceOfXMeeds) {
        const meedsBalance = this.xMeedsBalance.mul(this.meedsBalanceOfXMeeds).div(this.xMeedsTotalSupply);
        return this.$ethUtils.computeFiatBalance(
          meedsBalance,
          this.meedsPrice,
          this.ethPrice,
          this.exchangeRate,
          this.selectedFiatCurrency,
          this.language);
      } else {
        return this.$ethUtils.computeFiatBalance(
          0,
          this.meedsPrice,
          this.ethPrice,
          this.exchangeRate,
          this.selectedFiatCurrency,
          this.language);
      }
    },
    meedsBalanceFiat() {
      return this.$ethUtils.computeFiatBalance(
        this.meedsBalance || 0,
        this.meedsPrice,
        this.ethPrice,
        this.exchangeRate,
        this.selectedFiatCurrency,
        this.language);
    },
  }),
};
</script>