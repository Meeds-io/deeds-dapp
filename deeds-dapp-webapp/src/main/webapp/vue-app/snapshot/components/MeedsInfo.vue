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
  <v-card flat>
    <h3 class="d-flex flex-nowrap">
      {{ $t('meedToken') }}
      <v-divider class="my-auto ms-4" />
    </h3>
    <v-list>
      <v-list-item>
        <v-list-item-content class="align-start">
          <h4>{{ $t('marketCap') }}</h4>
        </v-list-item-content>
        <v-list-item-content class="align-end">
          {{ marketCap }}
        </v-list-item-content>
        <v-list-item-content class="align-end">
          <h4>{{ $t('circulatingSupply') }}</h4>
        </v-list-item-content>
        <v-list-item-content class="align-end">
          {{ circulatingSupply }} MEED
        </v-list-item-content>
      </v-list-item>
      <v-list-item>
        <v-list-item-content class="align-start">
          <h4>{{ $t('meedPrice') }}</h4>
        </v-list-item-content>
        <v-list-item-content class="align-end">
          {{ meedsPriceToDisplay }}
        </v-list-item-content>
        <v-list-item-content class="align-end">
          <h4>TVL</h4>
        </v-list-item-content>
        <v-list-item-content class="align-end">
          {{ totalValuelocked }}
        </v-list-item-content>
      </v-list-item>
    </v-list>
    <div class="d-flex flex-column flex-sm-row">
      <deeds-price-chart class="mb-4 mb-sm-8" />
      <deeds-currencies-chart 
        class="mt-15 ml-8"
        @metrics="getMetrics" />
    </div>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    metrics: null,
  }),
  computed: Vuex.mapState({
    meedPrice: state => state.meedPrice,
    selectedFiatCurrency: state => state.selectedFiatCurrency,
    language: state => state.language,
    meedsPriceToDisplay() {
      if (this.meedPrice) {
        return this.currencyFormat(this.meedPrice);
      } else {
        return '';
      }
    },
    circulatingSupply() {
      return this.metrics?.circulatingSupply.toFixed(2);
    },
    marketCap() {
      if (this.metrics) {
        return this.currencyFormat(this.metrics.marketCapitalization);
      } else {
        return '';
      }
    },
    totalValuelocked() {
      if (this.metrics) {
        return this.currencyFormat(this.metrics.totalValuelocked);
      } else {
        return '';
      }
    },
  }),
  methods: {
    getMetrics(value) {
      this.metrics = value;
    },
    currencyFormat(currencyValue) {
      const value = currencyValue && currencyValue.value || currencyValue;
      if (this.selectedFiatCurrency === 'eth') {
        return `${this.$ethUtils.toFixed(value, 8)} ${this.selectedFiatCurrencyLabel}`;
      } else {
        return this.$ethUtils.toCurrencyDisplay(this.$ethUtils.toFixed(value, 2), this.selectedFiatCurrency, this.language);
      }
    },
  }
};
</script>