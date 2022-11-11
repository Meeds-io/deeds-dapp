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
  <v-card :loading="loading" flat>
    <deeds-marketplace-deeds-selector :has-offers="hasOffers" />
    <v-row v-if="hasOffers" class="pa-0">
      <v-col
        v-for="offer in offers"
        :key="offer.id"
        class="d-flex justify-center">
        <v-card
          width="357"
          max-width="100%"
          flat>
          <deeds-renting-offer-card
            :offer="offer"
            :selected-cards="selectedCards"
            :selected-offers="selectedOffers" />
        </v-card>
      </v-col>
      <v-col v-if="hasMore" cols="12">
        <v-btn
          outlined
          text
          block
          @click="$root.$emit('deeds-offers-load-more')">
          <span class="text-ordinary-capitalize">{{ $t('loadMore') }}</span>
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
    totalSize: {
      type: Number,
      default: () => 0,
    },
    loading: {
      type: Boolean,
      default: false,
    },
    hasOffers: {
      type: Boolean,
      default: false,
    },
    selectedOffers: {
      type: Array,
      default: null,
    },
    selectedCards: {
      type: Array,
      default: null,
    },
  },
  computed: {
    hasMore() {
      return this.totalSize > this.offers?.length;
    },
  },
};
</script>