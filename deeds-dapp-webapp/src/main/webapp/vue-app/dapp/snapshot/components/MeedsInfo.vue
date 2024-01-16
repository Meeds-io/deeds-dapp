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
  <v-card class="mt-8 mt-sm-10" flat>
    <deeds-page-title-layout>
      <template #title>
        {{ $t('page.tokenomics.title') }}
      </template>
      <template #subtitle>
        {{ $t('page.tokenomics.subtitle') }}
      </template>
    </deeds-page-title-layout>
    <v-row class="px-5 pt-2 pt-md-7">
      <v-col
        cols="12"
        md="5"
        class="ps-md-0 py-0">
        <deeds-max-supply />
      </v-col>
      <v-col
        cols="12"
        md="6"
        offset="0"
        offset-md="1"
        class="py-0">
        <deeds-market-cap :metrics="metrics" />
      </v-col>
      <v-col
        cols="12"
        md="5"
        class="ps-md-0 py-0">
        <deeds-total-supply :metrics="metrics" />
      </v-col>
      <v-col
        cols="12"
        md="6"
        offset="0"
        offset-md="1"
        class="py-0">
        <deeds-total-value-locked :metrics="metrics" />
      </v-col>
    </v-row>
    <v-row class="px-5 pb-0 pt-6">
      <v-col
        cols="12"
        md="5"
        class="ps-md-0 py-0 pb-0">
        <deeds-meed-price />
      </v-col>
    </v-row>
    <v-row class="pt-0">
      <v-col
        cols="12"
        md="6"
        class="pt-0">
        <deeds-price-chart class="mb-4 mb-sm-8" />
      </v-col>
      <v-col cols="12" md="6">
        <deeds-currencies-chart 
          class="mt-0 mt-md-15 ms-0 ms-sm-8"
          :metrics="metrics" />
      </v-col>
    </v-row>
    <v-row class="pb-16 pt-6 justify-center">
      <v-btn
        id="tAssets-button"
        :href="portfolioURL"
        class="px-8"
        color="primary"
        height="60px"
        dark
        depressed>
        <span class="headline font-weight-bold">{{ $t('manageAssets') }}</span>
      </v-btn>
    </v-row>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    metrics: null,
  }),
  computed: Vuex.mapState({
    selectedFiatCurrency: state => state.selectedFiatCurrency,
    parentLocation: state => state.parentLocation,
    portfolioURL: state => state.portfolioURL,
  }),
  watch: {
    selectedFiatCurrency() {
      this.refreshMetrics();
    },
  },
  created() {
    this.refreshMetrics();
  },
  methods: {
    refreshMetrics() {
      this.$tokenMetricService.getMetrics(this.selectedFiatCurrency)
        .then(metrics => {
          this.metrics = metrics;
        });
    },
  }
};
</script>