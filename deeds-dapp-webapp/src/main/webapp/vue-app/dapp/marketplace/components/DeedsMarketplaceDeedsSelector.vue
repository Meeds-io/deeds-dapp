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
  <div v-show="displaySelector">
    <v-layout class="d-flex flex-column align-center mx-2 mb-8">
      <v-row class="mt-1 mb-0 mx-0 text-center">
        <v-btn-toggle
          v-model="cardTypes"
          multiple
          outlined
          dense
          group>
          <v-row no-gutters>
            <v-col
              v-for="cardType in availableCardTypes"
              :key="cardType">
              <deeds-card-type-chip
                :card="cardType"
                :selected-cards="cardTypes"
                extra-class="rounded-pill px-5"
                avatar-size="43"
                class="my-2 mx-5"
                x-large />
            </v-col>
          </v-row>
        </v-btn-toggle>
      </v-row>
    </v-layout>
  </div>
</template>
<script>
export default {
  props: {
    hasOffers: {
      type: Boolean,
      default: false,
    },
    selectedCardTypes: {
      type: Array,
      default: null,
    },
    selectedOfferTypes: {
      type: Array,
      default: null,
    },
  },
  data: () => ({
    displaySelector: false,
    cardTypes: [],
    offerTypes: [],
  }),
  computed: Vuex.mapState({
    availableCardTypes: state => state.cardTypes,
    availableOfferTypes: state => state.offerTypes,
  }),
  watch: {
    cardTypes() {
      this.$root.$emit('deeds-offers-select-card-types', this.cardTypes.map(type => type.toUpperCase()));
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
  created() {
    this.cardTypes = this.selectedCardTypes || [];
    this.offerTypes = this.selectedOfferTypes || [];
    this.displaySelector = this.hasOffers;
  },
};
</script>