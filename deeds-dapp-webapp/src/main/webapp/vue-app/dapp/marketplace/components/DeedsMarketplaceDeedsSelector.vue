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
  <v-layout v-if="displaySelector" class="d-flex flex-column align-center mx-2 mt-4 mb-8">
    <v-row class="ma-0">
      <v-btn-toggle
        v-model="offerTypes"
        multiple
        outlined
        dense
        group>
        <deeds-offer-type-chip
          :selected-offers="offerTypes"
          :label="$t('rentalsTag')"
          offer-type="RENTING"
          color="secondary"
          class="mx-2" />
      </v-btn-toggle>
    </v-row>
    <v-row class="mt-4 mb-0 mx-0">
      <v-btn-toggle
        v-model="types"
        multiple
        outlined
        dense
        group>
        <v-row no-gutters>
          <v-col
            v-for="cardType in cardTypes"
            :key="cardType">
            <deeds-card-type-chip
              :card="cardType"
              :selected-cards="types"
              avatar-size="24"
              class="ma-2" />
          </v-col>
        </v-row>
      </v-btn-toggle>
    </v-row>
  </v-layout>
</template>
<script>
export default {
  props: {
    hasOffers: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    displaySelector: false,
    types: [],
    offerTypes: [],
  }),
  computed: Vuex.mapState({
    cardTypes: state => state.cardTypes,
    availableOfferTypes: state => state.offerTypes,
  }),
  watch: {
    types() {
      this.$root.$emit('deeds-offers-select-card-types', this.types.map(type => type.toUpperCase()));
    },
    offerTypes() {
      this.$root.$emit('deeds-offers-select-offer-types', this.offerTypes.map(type => type.toUpperCase()));
    },
    hasOffers() {
      if (this.hasOffers) {
        this.displaySelector = true;
      }
    },
  },
};
</script>