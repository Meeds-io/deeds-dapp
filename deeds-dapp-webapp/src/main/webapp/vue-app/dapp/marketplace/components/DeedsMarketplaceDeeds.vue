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
  <div v-if="standaloneDisplay" class="mt-n4 mx-n2">
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
  </div>
  <v-card v-else flat>
    <v-scale-transition>
      <div v-show="!selectedStandaloneOfferId">
        <deeds-page-title-layout>
          <template #title>
            {{ $t('page.marketplace.title') }}
          </template>
          <template #subtitle>
            {{ $t('page.marketplace.subtitle') }}
          </template>
        </deeds-page-title-layout>
      </div>
    </v-scale-transition>
    <deeds-marketplace-deeds-list
      :offers="offers"
      :has-offers="hasOffers"
      :total-size="totalSize"
      :selected-card-types="cardTypes"
      :selected-offer-types="offerTypes"
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
    networkId: state => state.networkId,
    address: state => state.address,
    parentLocation: state => state.parentLocation,
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
    cardTypes(newCardTypes, oldCardTypes) {
      if (newCardTypes.length !== oldCardTypes.length) {
        this.refresh();
      }
    },
    offerTypes(newOfferTypes, oldOfferTypes) {
      if (newOfferTypes.length !== oldOfferTypes.length) {
        this.refresh();
      }
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
    this.$root.$on('deed-offer-renting-updated', this.handleOfferUpdateInProgress);
    this.$root.$on('deed-offer-renting-deleted', this.handleOfferDeleteInProgress);
    this.$root.$on('deed-offer-rented', this.handleOfferRentingInProgress);
    document.addEventListener('deed-offer-created', this.refreshOfferFromblockchain);
    document.addEventListener('deed-offer-updated', this.refreshOfferFromblockchain);
    document.addEventListener('deed-offer-deleted', this.refreshOfferFromblockchain);
    document.addEventListener('deed-lease-paid', this.refreshLeaseFromblockchain);
    this.init();
  },
  beforeDestroy() {
    this.$root.$off('deeds-offers-load-more', this.loadMore);
    this.$root.$off('deeds-offers-select-card-types', this.selectCardTypes);
    this.$root.$off('deeds-offers-select-offer-types', this.selectOfferTypes);
    this.$root.$off('deed-offer-renting-updated', this.handleOfferUpdateInProgress);
    this.$root.$off('deed-offer-renting-deleted', this.handleOfferDeleteInProgress);
    this.$root.$off('deed-offer-rented', this.handleOfferRentingInProgress);
    document.removeEventListener('deed-offer-created', this.refreshOfferFromblockchain);
    document.removeEventListener('deed-offer-updated', this.refreshOfferFromblockchain);
    document.removeEventListener('deed-offer-deleted', this.refreshOfferFromblockchain);
    document.removeEventListener('deed-lease-paid', this.refreshLeaseFromblockchain);
  },
  methods: {
    init() {
      const offerId = this.$utils.getQueryParam('offer');
      if (offerId) {
        this.standaloneDisplay = true;
        this.$store.commit('setStandaloneOfferId', offerId);
      } else if (this.selectedStandaloneOfferId) {
        this.standaloneDisplay = true;
        this.$store.commit('setStandaloneOfferId', this.selectedStandaloneOfferId);
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
      const newIndex = this.selectedStandaloneOfferIndex + 1;
      if (newIndex < this.totalSize) {
        if (newIndex < this.loadedOffersLength) {
          const nextOfferId = this.offers[newIndex]?.id;
          this.$store.commit('setStandaloneOfferId', nextOfferId);
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
        this.$store.commit('setStandaloneOfferId', previousOfferId);
      }
    },
    refresh() {
      this.loading = true;
      const params = {
        page: 0,
        size: this.limit,
        sort: `${this.sortField},${this.sortDirection}`,
        excludeExpired: true,
        excludeNotStarted: true,
        cardType: this.cardTypes,
        offerType: this.offerTypes,
      };
      if (this.address) {
        params.address = this.address;
      }
      return this.$deedTenantOfferService.getOffers(params, this.networkId || 0)
        .then(offers => {
          this.offers = offers?._embedded?.offers || [];
          this.totalSize = offers?.page?.totalElements || 0;
        })
        .finally(() => this.loading = false);
    },
    loadOffer(id) {
      if (!id) {
        return;
      }
      this.loading = true;
      this.$deedTenantOfferService.getOffer(id)
        .then(offer => {
          if (offer) {
            const index = this.offers.findIndex(displayedOffer => displayedOffer.id === offer.id);
            if (index >= 0) {
              this.handleOfferUpdateInProgress(offer);
            } else {
              this.offers.push(offer);
              this.totalSize += 1;
            }
          }
          return offer;
        })
        .catch(() => console.debug('offer seems to be deleted', id)) // eslint-disable-line no-console
        .finally(() => this.loading = false);
    },
    handleOfferUpdateInProgress(offer) {
      if (!offer) {
        return;
      }
      const index = this.offers.findIndex(displayedOffer => displayedOffer.offerId === offer.offerId || displayedOffer.id === offer.id);
      if (index >= 0) {
        this.offers.splice(index, 1, offer);
        this.offers.sort((offer1, offer2) => new Date(offer2.modifiedDate || offer2.createdDate).getTime() - new Date(offer1.modifiedDate || offer1.createdDate).getTime());
        if (this.selectedStandaloneOfferId && offer.id !== this.selectedStandaloneOfferId) {
          this.$store.commit('setStandaloneOfferId', offer.id);
        }
      }
    },
    handleOfferDeleteInProgress(offer) {
      this.loadOffer(offer?.id);
    },
    handleOfferRentingInProgress(_lease, offer) {
      this.loadOffer(offer?.id);
    },
    refreshLeaseFromblockchain(event) {
      const offerId = this.getOfferId(event);
      if (offerId) {
        // Force Refresh Lease From Blockchain on Server 
        this.$deedTenantLeaseService.getLease(offerId, true)
          // Refresh Offer From Blockchain on Client & Server
          .finally(() => this.refreshOfferFromblockchain(event));
      }
    },
    refreshOfferFromblockchain(event) {
      const offerId = event?.detail?.offerId?.toNumber() || event?.detail?.leaseId?.toNumber();
      if (offerId) {
        const selectedOffers = event.type === 'deed-offer-created' ?
          this.offers.filter(offer => !offer.offerId)
          : this.offers.filter(offer => offer.offerId === offerId);
        if (selectedOffers.length > 0) {
          selectedOffers.forEach(selectedOffer => {
            // Refresh Offer From Blockchain on Client & Server
            this.$deedTenantOfferService.getOffer(selectedOffer.parentId || selectedOffer.id, true)
              .then(offer => this.handleOfferUpdateInProgress(offer))
              .catch(() => {
                if (event.type === 'deed-offer-deleted' || event.type === 'deed-lease-paid') {
                  this.deleteDisplayedOffer(selectedOffer);
                }
              });
          });
        }
      }
    },
    loadSelectedOffer() {
      this.loadOnStandaloneClose = true;
      this.loadOffer(this.selectedStandaloneOfferId);
    },
    getOfferId(event) {
      return event?.detail?.offerId?.toNumber() || event?.detail?.leaseId?.toNumber();
    },
    deleteDisplayedOffer(offer) {
      const index = this.offers.findIndex(displayedOffer => displayedOffer.offerId === offer.offerId || displayedOffer.id === offer.id);
      const found = index >= 0;
      if (found) {
        this.offers.splice(index, 1);
      }
      return found;
    },
  },
};
</script>