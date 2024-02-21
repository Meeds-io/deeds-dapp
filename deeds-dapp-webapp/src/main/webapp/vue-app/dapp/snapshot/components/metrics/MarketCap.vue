<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 
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
      <strong>{{ $t('marketCap') }}</strong>
    </template>
    <template #col2>
      {{ $t('circulatingSupply') }}
    </template>
    <template #col3>
      <template v-if="metrics">{{ marketCap }}</template>
      <v-skeleton-loader
        v-else
        type="chip"
        max-height="17"
        tile />
    </template>
    <template #col4>
      <deeds-number-format
        v-if="metrics"
        :value="circulatingSupply"
        :fractions="2"
        class="small--text"
        no-decimals>
        Ɱ
      </deeds-number-format>
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
    selectedFiatCurrency: state => state.selectedFiatCurrency,
    language: state => state.language,
    circulatingSupply() {
      return this.metrics?.circulatingSupply;
    },
    marketCap() {
      if (this.metrics) {
        return this.$ethUtils.toCurrencyDisplay(this.metrics.marketCapitalization, this.selectedFiatCurrency, this.language);
      } else {
        return null;
      }
    },
  }),
};
</script>
