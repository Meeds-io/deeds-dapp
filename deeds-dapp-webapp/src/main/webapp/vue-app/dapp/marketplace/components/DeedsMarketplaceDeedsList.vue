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
  <v-card 
    id="marketplaceOffersList"
    class="pt-4 pt-md-11"
    flat>
    <v-scale-transition>
      <div v-show="!selectedStandaloneOfferId">
        <deeds-marketplace-deeds-selector
          :has-offers="hasOffers"
          :selected-card-types="selectedCardTypes"
          :selected-offer-types="selectedOfferTypes" />
      </div>
    </v-scale-transition>
    <v-progress-linear
      v-if="loading"
      color="primary"
      class="position-absolute"
      indeterminate />
    <v-row v-if="hasOffers" class="pa-0 my-16">
      <v-col
        v-for="(offer, index) in offers"
        :key="`${offer.id}-${offer.updatedDate}-${index}`"
        class="d-flex justify-center"
        cols="12"
        lg="4"
        md="12">
        <v-card
          width="100%"
          flat>
          <deeds-marketplace-offer-card
            :offer="offer"
            :selected-cards="selectedCardTypes"
            :selected-offers="selectedOfferTypes" />
        </v-card>
      </v-col>
      <v-col v-if="hasMore" cols="12">
        <v-btn
          outlined
          text
          block
          @click="$root.$emit('deeds-offers-load-more')">
          {{ $t('loadMore') }}
        </v-btn>
      </v-col>
    </v-row>
  </v-card>
</template>
<script>
export default {
  props: {
    offers: {
      type: Array,
      default: null,
    },
    selectedOfferTypes: {
      type: Array,
      default: null,
    },
    selectedCardTypes: {
      type: Array,
      default: null,
    },
    totalSize: {
      type: Number,
      default: () => 0,
    },
    hasOffers: {
      type: Boolean,
      default: false,
    },
    loading: {
      type: Boolean,
      default: false,
    },
  },
  computed: Vuex.mapState({
    selectedStandaloneOfferId: state => state.selectedStandaloneOfferId,
    hasMore() {
      return this.totalSize > this.offers?.length;
    },
    noFilter() {
      return this.selectedOfferTypes?.length === 0 && this.selectedCardTypes?.length === 0;
    },
  }),
};
</script>