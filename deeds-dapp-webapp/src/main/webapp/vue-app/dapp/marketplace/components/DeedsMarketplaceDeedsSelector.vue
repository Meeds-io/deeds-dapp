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
  <v-layout v-if="displaySelector" class="d-flex flex-column align-center mx-2 my-8">
    <v-row class="ma-0">
      <v-chip-group v-model="offerTypes" multiple>
        <v-chip
          class="px-2"
          color="secondary"
          text-color="secondary"
          label
          outlined>
          {{ $t('rentalsTag') }}
        </v-chip>
      </v-chip-group>
    </v-row>
    <v-row class="ma-0">
      <v-chip-group
        v-model="types"
        active-class="primary primary--text lighten-3"
        multiple>
        <deeds-card-type-chip
          v-for="cardType in cardTypes"
          :key="cardType"
          :card="cardType"
          avatar-size="18"
          class="mx-2" />
      </v-chip-group>
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
      this.$root.$emit('deeds-offers-select-card-types', this.types.map(index => this.cardTypes[index].toUpperCase()));
    },
    offerTypes() {
      this.$root.$emit('deeds-offers-select-offer-types', this.offerTypes.map(index => this.availableOfferTypes[index]));
    },
    hasOffers() {
      if (this.hasOffers) {
        this.displaySelector = true;
      }
    },
  },
};
</script>