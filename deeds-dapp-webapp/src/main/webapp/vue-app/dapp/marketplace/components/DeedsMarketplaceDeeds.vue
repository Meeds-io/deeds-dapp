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
  <v-card flat>
    <div class="d-flex flex-row">
      <v-card-title class="ps-0 py-0">{{ $t('dapp.marketplace.deedsListTitle') }}</v-card-title>
      <v-divider class="my-auto" />
    </div>
    <deeds-marketplace-deeds-list
      :offers="offers"
      :has-offers="hasOffers"
      :total-size="totalSize"
      :loading="loading"
      :selected-cards="cardTypes"
      :selected-offers="offerTypes"
      @load-more="loadMore" />
    <deeds-marketplace-deeds-empty v-if="!hasOffers && !loading" />
  </v-card>
</template>
<script>
export default {
  data: () => ({
    offers: [],
    cardTypes: [],
    offerTypes: [],
    sortField: 'modifiedDate',
    sortDirection: 'desc',
    pageSize: 9,
    limit: 0,
    totalSize: 0,
    loading: false,
  }),
  computed: Vuex.mapState({
    availableCardTypes: state => state.cardTypes,
    availableOfferTypes: state => state.offerTypes,
    hasOffers() {
      return this.offers.length > 0;
    },
  }),
  watch: {
    limit() {
      this.refresh();
    },
    cardTypes() {
      this.refresh();
    },
    offerTypes() {
      this.refresh();
    },
  },
  created() {
    this.limit = this.pageSize;
    this.$root.$on('deeds-offers-load-more', this.loadMore);
    this.$root.$on('deeds-offers-select-card-types', this.selectCardTypes);
    this.$root.$on('deeds-offers-select-offer-types', this.selectOfferTypes);
  },
  methods: {
    loadMore() {
      this.limit += this.pageSize;
    },
    selectCardTypes(cardTypes) {
      this.cardTypes = cardTypes;
    },
    selectOfferTypes(offerTypes) {
      this.offerTypes = offerTypes;
    },
    refresh() {
      this.loading = true;
      this.$deedTenantOfferService.getOffers({
        page: 0,
        size: this.limit,
        sort: `${this.sortField},${this.sortDirection}`,
        cardType: this.cardTypes,
        offerType: this.offerTypes,
      })
        .then(offers => {
          this.offers = offers?._embedded?.deedTenantOfferDTOList || [];
          this.totalSize = offers?.page?.totalElements || 0;
        })
        .finally(() => this.loading = false);
    },
  },
};
</script>