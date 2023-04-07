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
  <deeds-token-asset-template :extra-cols="false">
    <template #col1>
      <v-tooltip :disabled="!hasTotalValueLocked" bottom>
        <template #activator="{ on, attrs }">
          <span
            class="d-inline-flex position-relative pe-8"
            v-bind="attrs"
            v-on="on">
            <strong>
              {{ $t('totalValueLocked') }}
            </strong>
            <v-btn
              height="12"
              width="12"
              icon
              outlined
              class="ms-2 mt-1">
              <v-icon size="8" class="my-auto">fas fa-info</v-icon>
            </v-btn>
          </span>
        </template>
        {{ $t('totalValueLockedTooltip') }}
        <ul>
          <template v-for="item in totalValueLockedItems">
            <li
              v-if="item.value"
              :key="item.name"
              class="text-no-wrap">
              {{ item.name }}
              <deeds-number-format
                :value="item.value"
                no-decimals
                class="d-inline">
                Ɱ
              </deeds-number-format>
            </li>
          </template>
        </ul>
      </v-tooltip>
    </template>
    <template #col3>
      <deeds-number-format
        v-if="metrics"
        :value="totalValueLocked"
        :fractions="2">
        Ɱ
      </deeds-number-format>
      <v-skeleton-loader
        v-else
        type="chip"
        max-height="17"
        tile />
    </template>
    <template #col4>
      <deeds-number-format
        v-if="metrics"
        :value="totalValueLocked"
        :fractions="2"
        class="small--text"
        currency />
      <v-skeleton-loader
        v-else
        type="chip"
        max-height="17"
        tile />
    </template>
  </deeds-token-asset-template>
</template>
<script>
export default {
  props: {
    metrics: {
      type: Object,
      default: null,
    },
  },
  computed: Vuex.mapState({
    comethPairAddress: state => state.comethPairAddress,
    sushiswapPairAddress: state => state.sushiswapPairAddress,
    vestingAddress: state => state.vestingAddress,
    xMeedAddress: state => state.xMeedAddress,
    lockedBalances() {
      return this.metrics?.lockedBalances || {};
    },
    hasTotalValueLocked() {
      return !this.totalValueLocked.isZero();
    },
    totalValueLocked() {
      return Object.values(this.lockedBalances).reduce((sum, v) => sum.plus(new BigNumber(v)), new BigNumber(0)).multipliedBy(new BigNumber(10).pow(18));
    },
    totalValueLockedItems() {
      return Object.keys(this.lockedBalances).map((address) => {
        let name = address.toLowerCase();
        if (name === this.comethPairAddress?.toLowerCase()) {
          name = this.$t('comethPool');
        } else if (name === this.xMeedAddress?.toLowerCase()) {
          name = this.$t('xMeedsStaked');
        } else if (name === this.sushiswapPairAddress?.toLowerCase()) {
          name = this.$t('sushiSwapPool');
        } else if (name === this.vestingAddress?.toLowerCase()) {
          name = this.$t('vestedMeeds');
        }
        return {
          name,
          value: this.lockedBalances[address],
        };
      });
    },
  }),
};
</script>
