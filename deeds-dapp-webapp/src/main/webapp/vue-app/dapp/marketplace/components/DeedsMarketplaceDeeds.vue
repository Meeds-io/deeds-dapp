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
  <v-responsive
    v-if="standaloneDisplay"
    aspect-ratio="16/10"
    class="mt-n4">
    <v-fab-transition>
      <div v-show="standaloneDisplayAnimate">
        <deeds-marketplace-offer-card-details
          :offer="selectedStandaloneOffer"
          :loading="loading"
          :has-next="hasNextOffer"
          :has-previous="hasPreviousOffer"
          @next="nextOffer"
          @previous="previousOffer" />
      </div>
    </v-fab-transition>
  </v-responsive>
  <v-card v-else flat>
    <v-scale-transition>
      <div v-show="!selectedStandaloneOfferId">
        <div class="d-flex flex-row">
          <v-card-title class="ps-0 py-0">{{ $t('dapp.marketplace.deedsListTitle') }}</v-card-title>
          <v-divider class="my-auto" />
        </div>
      </div>
    </v-scale-transition>
    <deeds-marketplace-deeds-list
      :offers="offers"
      :has-offers="hasOffers"
      :total-size="totalSize"
      :selected-cards="cardTypes"
      :selected-offers="offerTypes"
      :loading="loading"
      @load-more="loadMore" />
    <deeds-marketplace-deeds-empty
      v-if="showEmptyBlock"
      :has-filter="hasFilter"
      :loading="loading" />
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
    showEmptyBlock: false,
    standaloneDisplay: false,
    standaloneDisplayAnimate: false,
    loadOnStandaloneClose: false,
  }),
  computed: Vuex.mapState({
    availableCardTypes: state => state.cardTypes,
    availableOfferTypes: state => state.offerTypes,
    loadedOffersLength() {
      return this.offers?.length || 0;
    },
    hasOffers() {
      return this.loadedOffersLength > 0;
    },
    hasFilter() {
      return this.offerTypes?.length > 0 || this.cardTypes?.length > 0;
    },
    hasMore() {
      return this.totalSize > this.loadedOffersLength;
    },
    selectedStandaloneOfferId: state => state.selectedStandaloneOfferId,
    selectedStandaloneOfferIndex() {
      return this.offers.findIndex(offer => offer.id === this.selectedStandaloneOfferId);
    },
    selectedStandaloneOffer() {
      if (this.selectedStandaloneOfferIndex >= 0) {
        return this.offers[this.selectedStandaloneOfferIndex];
      }
      return null;
    },
    hasNextOffer() {
      return this.selectedStandaloneOfferId
        && (this.hasMore || this.selectedStandaloneOfferIndex < (this.loadedOffersLength - 1));
    },
    hasPreviousOffer() {
      return this.selectedStandaloneOfferId && this.selectedStandaloneOfferIndex > 0;
    },
  }),
  watch: {
    limit() {
      if (!this.loading) {
        this.refresh();
      }
    },
    cardTypes() {
      this.refresh();
    },
    offerTypes() {
      this.refresh();
    },
    loading() {
      if (!this.loading) {
        this.showEmptyBlock = !this.hasOffers;
      }
    },
    selectedStandaloneOfferId() {
      if (this.selectedStandaloneOfferId) {
        window.setTimeout(() => this.standaloneDisplay = true, 500);
        if (!this.selectedStandaloneOffer) {
          this.loadSelectedOffer();
        }
      } else {
        this.standaloneDisplay = false;
        if (this.loadOnStandaloneClose) {
          this.loadFirstPage();
          this.loadOnStandaloneClose = false;
        }
      }
    },
    standaloneDisplay() {
      if (this.standaloneDisplay) {
        window.setTimeout(() => this.standaloneDisplayAnimate = true, 200);
      } else {
        this.standaloneDisplayAnimate = false;
      }
    },
  },
  created() {
    this.$root.$on('deeds-offers-load-more', this.loadMore);
    this.$root.$on('deeds-offers-select-card-types', this.selectCardTypes);
    this.$root.$on('deeds-offers-select-offer-types', this.selectOfferTypes);
    this.init();
  },
  methods: {
    init() {
      const offerId = this.$utils.getQueryParam('offer');
      if (offerId) {
        this.standaloneDisplay = true;
        this.$store.commit('setStandaloneOfferId', Number(offerId));
      } else if (this.selectedStandaloneOfferId) {
        this.standaloneDisplay = true;
        this.$store.commit('setStandaloneOfferId', Number(this.selectedStandaloneOfferId));
        this.loadSelectedOffer();
      } else {
        this.loadFirstPage();
      }
    },
    loadFirstPage() {
      this.limit = this.pageSize;
    },
    loadMore() {
      this.limit += this.pageSize;
    },
    selectCardTypes(cardTypes) {
      this.cardTypes = cardTypes;
    },
    selectOfferTypes(offerTypes) {
      this.offerTypes = offerTypes;
    },
    nextOffer() {
      const newIndex = Number(this.selectedStandaloneOfferIndex) + 1;
      if (newIndex < this.totalSize) {
        if (newIndex < this.loadedOffersLength) {
          const nextOfferId = this.offers[newIndex]?.id;
          this.$store.commit('setStandaloneOfferId', Number(nextOfferId));
        } else {
          this.loading = true;
          this.loadMore();
          return this.$nextTick()
            .then(() => this.refresh())
            .then(() => this.nextOffer());
        }
      }
    },
    previousOffer() {
      const newIndex = this.selectedStandaloneOfferIndex - 1;
      if (newIndex >= 0) {
        const previousOfferId = this.offers[newIndex]?.id;
        this.$store.commit('setStandaloneOfferId', Number(previousOfferId));
      }
    },
    refresh() {
      this.loading = true;
      return this.$deedTenantOfferService.getOffers({
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
    loadSelectedOffer() {
      this.loadOnStandaloneClose = true;
      this.loading = true;
      this.$deedTenantOfferService.getOffer(this.selectedStandaloneOfferId)
        .then(offer => {
          if (offer && !this.offers.find(existingOffer => existingOffer.id === offer.id)) {
            this.offers.push(offer);
            if (this.totalSize === 0) {
              this.totalSize += 1;
            }
          }
        })
        .finally(() => this.loading = false);
    },
  },
};
</script>