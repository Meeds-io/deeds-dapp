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
    this.$root.$on('deed-offer-renting-updated', this.handleOfferUpdated);
    this.$root.$on('deed-offer-renting-deleted', this.handleOfferDeleted);
    this.$root.$on('deed-offer-rented', this.handleOfferRented);
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
    this.$root.$off('deed-offer-renting-updated', this.handleOfferUpdated);
    this.$root.$off('deed-offer-renting-deleted', this.handleOfferDeleted);
    this.$root.$off('deed-offer-rented', this.handleOfferRented);
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
    loadSelectedOffer() {
      this.loadOnStandaloneClose = true;
      this.loadOffer(this.selectedStandaloneOfferId);
    },
    handleOfferUpdated(offer) {
      const index = this.offers.findIndex(displayedOffer => displayedOffer.offerId === offer.offerId || displayedOffer.id === offer.id);
      if (index >= 0) {
        this.offers.splice(index, 1, offer);
        this.offers.sort((offer1, offer2) => new Date(offer2.modifiedDate || offer2.createdDate).getTime() - new Date(offer1.modifiedDate || offer1.createdDate).getTime());
        if (this.selectedStandaloneOfferId && offer.id !== this.selectedStandaloneOfferId) {
          this.$store.commit('setStandaloneOfferId', offer.id);
        }
      }
    },
    handleOfferDeleted(offer) {
      if (offer) {
        this.loadOffer(offer.id);
      }
    },
    handleOfferRented(_lease, offer) {
      if (offer) {
        this.loadOffer(offer.id);
      }
    },
    refreshLeaseFromblockchain(event) {
      const leaseId = event?.detail?.offerId?.toNumber() || event?.detail?.leaseId?.toNumber();
      const creator = event?.detail?.creator || event?.detail?.manager || event?.detail?.owner;
      if (leaseId && creator) {
        this.$deedTenantLeaseService.getLease(leaseId, true)
          .finally(() => this.refreshOfferFromblockchain(event));
      }
    },
    refreshOfferFromblockchain(event) {
      const offerId = event?.detail?.offerId?.toNumber() || event?.detail?.leaseId?.toNumber();
      const creator = event?.detail?.creator || event?.detail?.manager || event?.detail?.owner;
      if (offerId) {
        const selectedOffer = event.type === 'deed-offer-created' ?
          this.offers.find(offer => !offer.offerId)
          : this.offers.find(offer => offer.offerId === offerId);
        if (selectedOffer?.id) {
          const isCreator = creator?.toUpperCase() === this.address?.toUpperCase();
          if (isCreator) {
            this.$deedTenantOfferService.getOffer(selectedOffer.id, isCreator)
              .catch(e => {
                if (selectedOffer.parentId) {
                  return this.$deedTenantOfferService.getOffer(selectedOffer.parentId, isCreator)
                    .catch(() => console.debug('offer seems to be deleted', selectedOffer.parentId)); // eslint-disable-line no-console
                } else {
                  throw e;
                }
              })
              .then(offer => {
                if (offer) {
                  this.handleOfferUpdated(offer);
                } else if (event.type === 'deed-offer-deleted') {
                  const index = this.offers.findIndex(displayedOffer => displayedOffer?.id === selectedOffer.id);
                  if (index >= 0) {
                    this.offers.splice(index, 1);
                  }
                }
              })
              .catch(() => this.refresh());
          } else if (event.type === 'deed-offer-deleted') { // When not creator of Deed, just delete the offer
            const index = this.offers.findIndex(displayedOffer => displayedOffer?.id === selectedOffer?.id);
            if (index >= 0) {
              this.offers.splice(index, 1);
            }
          } else if (event.type === 'deed-offer-updated' || event.type === 'deed-lease-paid') { // When not creator of Deed, just indicate thet it's updated
            const offer = this.offers.find(displayedOffer => displayedOffer?.id === selectedOffer?.id);
            if (offer?.id) {
              offer.updateId = true;
            }
          }
        }
      }
    },
    loadOffer(id) {
      this.loading = true;
      this.$deedTenantOfferService.getOffer(id)
        .then(offer => {
          if (offer) {
            const index = this.offers.findIndex(displayedOffer => displayedOffer.id === offer.id);
            if (index >= 0) {
              this.handleOfferUpdated(offer);
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
  },
};
</script>