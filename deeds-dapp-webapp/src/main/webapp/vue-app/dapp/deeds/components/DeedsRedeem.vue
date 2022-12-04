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
  <v-card flat class="d-flex flex-column">
    <v-card-title class="d-flex flex-nowrap pa-0">
      {{ $t('deedsToRedeem') }}
      <deeds-contract-address
        :address="deedAddress"
        button-top="-6"
        token />
      <v-divider class="my-auto ms-4" />
    </v-card-title>
    <v-card-text v-html="$t('deedsToRedeemIntroduction')" />
    <template v-if="xMeedAddress">
      <v-skeleton-loader
        v-if="currentCityName === null"
        type="chip"
        max-height="17"
        tile />
      <h4 v-else>{{ currentCityName }}</h4>
      <small class="d-flex flex-column flex-sm-row align-center">
        {{ $t('cityPopulation') }}:
        <v-skeleton-loader
          v-if="currentCityPopulation === null || currentCityMaxPopulation === null"
          type="chip"
          class="ms-2" />
        <v-chip v-else class="ms-2">{{ currentCityPopulation }} / {{ currentCityMaxPopulation }}</v-chip>
        <div v-if="!currentCityMintable && currentCityMintingStartDate">
          . {{ $t('cityMintingStartDate') }}:
          <deeds-timer
            :end-time="currentCityMintingStartDate"
            class="my-auto"
            @end="endCountDown" />
        </div>
      </small>
      <v-container v-if="hasCardTypes" class="mt-2">
        <deeds-card-caroussel>
          <div
            v-for="card in currentCardTypes"
            :key="card.name"
            class="mx-2">
            <deeds-redeem-card :card="card" :loading="loadingCityDetails" />
          </div>
        </deeds-card-caroussel>
      </v-container>
      <v-container v-else-if="deedLoading" class="grey lighten-5 mt-2">
        <v-row class="mx-auto" no-gutters>
          <v-col
            v-for="i in 4"
            :key="i">
            <v-skeleton-loader type="card" class="mx-2" />
          </v-col>
        </v-row>
      </v-container>
      <deeds-empty-city v-else />
    </template>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    loadingCityDetails: false,
  }),
  computed: Vuex.mapState({
    deedAddress: state => state.deedAddress,
    xMeedAddress: state => state.xMeedAddress,
    deedLoading: state => state.deedLoading,
    currentCity: state => state.currentCity,
    currentCardTypes: state => state.currentCardTypes,
    currentCityMintable: state => state.currentCityMintable,
    lastCityMintingCompleteDate: state => state.lastCityMintingCompleteDate,
    hasCardTypes() {
      return !!this.currentCardTypes?.length;
    },
    currentCityMintingStartDate() {
      if (!this.currentCityMintable && this.currentCityAvailability && this.lastCityMintingCompleteDate) {
        return (this.currentCityAvailability.toNumber() + this.lastCityMintingCompleteDate.toNumber()) * 1000;
      } else {
        return 0;
      }
    },
    currentCityAvailability() {
      return this.currentCity && this.currentCity.availability;
    },
    currentCityName() {
      return this.currentCity && this.currentCity.name;
    },
    currentCityPopulation() {
      return this.currentCity && this.currentCity.population;
    },
    currentCityMaxPopulation() {
      return this.currentCity && this.currentCity.maxPopulation;
    },
  }),
  watch: {
    currentCityMintable() {
      if (this.currentCityMintable) {
        this.loadingCityDetails = false;
      }
    },
  },
  methods: {
    endCountDown() {
      this.loadingCityDetails = true;
      window.setTimeout(() => {
        this.$store.commit('loadCurrentCity', true);
        window.setTimeout(() => {
          if (!this.currentCityMintable) {
            this.endCountDown();
          }
        }, 3000);
      }, 1000);
    },
  },
};
</script>