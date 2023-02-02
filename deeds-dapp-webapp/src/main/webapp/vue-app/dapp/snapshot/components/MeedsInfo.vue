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
    <v-card-title class="d-flex flex-nowrap pa-0 headline font-weight-medium">
      {{ $t('meedToken') }}
      <v-divider class="my-auto ms-4" />
    </v-card-title>
    <v-row class="mt-4">
      <v-col
        cols="12"
        md="5"
        class="ps-md-0 py-0">
        <deeds-max-supply />
      </v-col>
      <v-col
        cols="12"
        md="5"
        offset="0"
        offset-md="2"
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
        md="5"
        offset="0"
        offset-md="2"
        class="py-0">
        <deeds-total-value-locked :metrics="metrics" />
      </v-col>
    </v-row>
    <v-row class="pb-0 pt-6">
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
  </v-card>
</template>
<script>
export default {
  data: () => ({
    metrics: null,
  }),
  computed: Vuex.mapState({
    selectedFiatCurrency: state => state.selectedFiatCurrency,
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