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
  <v-card
    width="530"
    class="px-4"
    outlined>
    <v-card-title class="ps-0 pb-0 mx-1 text-capitalize">
      {{ $t('pointsSimulator') }}
    </v-card-title>
    <v-card-text class="ps-0">
      <v-slider
        v-model="xMeedAmount"
        id="xMeedAmountSlider"
        name="xMeedAmountSlider"
        min="0"
        max="999999"
        class="align-center"
        hide-details>
        <template #append>
          <div class="font-weight-bold d-flex flex-nowrap">
            <v-text-field
              v-model="xMeedAmount"
              name="xMeedAmount"
              max="999999"
              class="mt-0 pt-0 me-2"
              hide-details
              outlined
              dense
              style="max-width: 185px"
              dir="rtl" />
            <label for="xMeedAmount" class="my-auto">xMEED</label>
          </div>
        </template>
      </v-slider>
      <v-skeleton-loader
        v-if="tokenLoading"
        type="chip"
        max-height="17"
        tile />
      <div v-else class="mx-1">
        {{ pointsSimulationRate }}
      </div>
    </v-card-text>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    xMeedAmount: 100000,
    cardPrice: 8000,
    dayInSeconds: 24 * 60 * 60,
  }),
  computed: Vuex.mapState({
    language: state => state.language,
    tokenLoading: state => state.tokenLoading,
    xMeedsBalance: state => state.xMeedsBalance,
    xMeedsBalanceNoDecimals() {
      return this.xMeedsBalance && this.$ethUtils.fromDecimals(this.xMeedsBalance, 18) || 0;
    },
    dailyPoints() {
      if (!this.xMeedAmount) {
        return 0;
      }
      const amount = new BigNumber(this.xMeedAmount);
      const ratePerSecond = amount.dividedBy(amount.plus(12000)).dividedBy(240);
      return ratePerSecond.multipliedBy(this.dayInSeconds).toNumber();
    },
    days() {
      if (!this.dailyPoints) {
        return 0;
      }
      return this.cardPrice / this.dailyPoints;
    },
    pointsSimulationRate() {
      return this.$t('pointsSimulationRate', {
        0: this.$ethUtils.toFixedDisplay(this.dailyPoints, 1, this.language),
        1: this.$ethUtils.toFixedDisplay(this.days, 1, this.language),
        2: this.$ethUtils.toFixedDisplay(this.cardPrice, 0, this.language),
      });
    },
  }),
  watch: {
    xMeedsBalanceNoDecimals: {
      immediate: true,
      handler() {
        if (this.xMeedAmount === 100000 && this.xMeedsBalanceNoDecimals) {
          this.xMeedAmount = parseInt(this.xMeedsBalanceNoDecimals);
        }
      },
    },
  },
};
</script>