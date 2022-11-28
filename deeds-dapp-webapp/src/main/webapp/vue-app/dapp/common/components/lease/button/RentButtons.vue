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
  <v-tooltip disabled>
    <!-- eslint-disable-next-line vue/no-unused-vars -->
    <template #activator="{ onParent, attrsParent }">
      <v-tooltip bottom>
        <template #activator="{ on, attrs }">
          <v-btn
            :width="buttonsWidth"
            :min-width="minButtonsWidth"
            :max-width="maxButtonsWidth"
            :class="!showSeeRentPart && 'd-none'"
            class="mx-auto mt-2 mt-md-0"
            color="primary"
            outlined
            depressed
            v-bind="attrs"
            v-on="on"
            @click="openRentDetails">
            <span class="text-truncate position-absolute full-width text-capitalize">
              {{ $t('deedRentSeeRentButton') }}
            </span>
          </v-btn>
        </template>
        <span>{{ $t('deedRentSeeRentDescription') }}</span>
      </v-tooltip>
      <v-tooltip bottom>
        <template #activator="{ on, attrs }">
          <v-btn
            :loading="loadingRentDrawer"
            :width="buttonsWidth"
            :min-width="minButtonsWidth"
            :max-width="maxButtonsWidth"
            :outlined="!hasRentOffers"
            :class="!showRentPart && 'd-none'"
            class="mx-auto mt-2 mt-md-0"
            color="primary"
            depressed
            v-bind="attrs"
            v-on="on"
            @click="openRentDrawer">
            <span class="text-truncate position-absolute full-width text-capitalize">
              {{ rentingButtonName }}
            </span>
          </v-btn>
        </template>
        <span>{{ rentingDescription }}</span>
      </v-tooltip>
    </template>
  </v-tooltip>
</template>
<script>
export default {
  props: {
    nftId: {
      type: Number,
      default: null,
    },
    offers: {
      type: Array,
      default: null,
    },
    started: {
      type: Boolean,
      default: false,
    },
    isProvisioningManager: {
      type: Boolean,
      default: false,
    },
    minButtonsWidth: {
      type: String,
      default: () => '150px',
    },
    maxButtonsWidth: {
      type: String,
      default: () => '250px',
    },
    buttonsWidth: {
      type: String,
      default: () => '100%',
    },
  },
  data: () => ({
    loadingRentDrawer: false,
    loadingRentalOffers: false,
    rentalOffers: [],
  }),
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    address: state => state.address,
    networkId: state => state.networkId,
    language: state => state.language,
    authenticated: state => state.authenticated,
    tenantProvisioningContract: state => state.tenantProvisioningContract,
    now: state => state.now,
    showRentPart() {
      return this.authenticated && (!this.rentalOffer || !!this.rentalOffer.offerId);
    },
    showSeeRentPart() {
      return this.authenticated && this.hasAvailableRentOffers;
    },
    hasRentOffers() {
      return !!this.rentalOffers?.length;
    },
    availableRentOffers() {
      return this.rentalOffers?.filter(offer => !offer.expirationDate || new Date(offer.expirationDate).getTime() > this.now);
    },
    hasAvailableRentOffers() {
      return this.availableRentOffers?.length > 0;
    },
    hasOnlyExpiredOffers() {
      return this.hasRentOffers && !this.hasAvailableRentOffers;
    },
    rentalOffer() {
      return (this.hasAvailableRentOffers && this.availableRentOffers[0])
        || (this.hasRentOffers && this.rentalOffers[0]);
    },
    isRentingEnabled() {
      return this.showRentPart && this.isProvisioningManager;
    },
    rented() {
      return !this.isProvisioningManager;
    },
    rentingDescription() {
      return this.hasOnlyExpiredOffers
        && this.$t('deedRentOfferExpired')
        || (this.hasRentOffers
        && this.$t('deedRentEditDescription')
        || (this.isRentingEnabled
            && this.$t('deedRentEnabledDescription')
            ||  this.$t('deedRentDisabledDescription')));
    },
    rentingButtonName() {
      return this.hasRentOffers
        && this.$t('deedRentEditButton')
        || this.$t('deedRentButton');
    },
  }),
  watch: {
    loadingRentalOffers() {
      this.loadingRentDrawer = this.loadingRentalOffers;
    },
  },
  created() {
    this.$root.$on('deed-offer-renting-created', this.handleRefreshOffers);
    this.$root.$on('deed-offer-renting-updated', this.handleRefreshOffers);
    this.$root.$on('deed-offer-renting-deleted', this.handleRefreshOffers);
    this.rentalOffers = this.offers || [];
  },
  beforeDestroy() {
    this.$root.$off('deed-offer-renting-created', this.handleRefreshOffers);
    this.$root.$off('deed-offer-renting-updated', this.handleRefreshOffers);
    this.$root.$off('deed-offer-renting-deleted', this.handleRefreshOffers);
  },
  methods: {
    openRentDrawer() {
      this.loadingRentDrawer = true;
      this.refreshOffers()
        .then(() => this.$root.$emit('deeds-rent-drawer', this.nftId, this.rentalOffer))
        .catch(() => this.$root.$emit('alert-message', this.$t('loggedOutPleaseLoginAgain'), 'warning'))
        .finally(() => this.loadingRentDrawer = false);
    },
    handleRefreshOffers(offer) {
      if (offer?.nftId === this.nftId) {
        return this.refreshOffers(true);
      }
    },
    refreshOffers(broadcast) {
      this.loadingRentalOffers = true;
      return this.$deedTenantOfferService.getOffers({
        nftId: this.nftId,
        address: this.address,
        onlyOwned: true,
        excludeExpired: true,
        excludeNotStarted: false,
      }, this.networkId)
        .then(offers => {
          this.rentalOffers = offers?._embedded?.offers && [offers?._embedded?.offers[0]] || [];
          if (broadcast) {
            this.$root.$emit('deed-offers-loaded', this.nftId, this.rentalOffers);
          }
        })
        .finally(() => this.loadingRentalOffers = false);
    },
    openRentDetails() {
      this.$store.commit('setStandaloneOfferId', this.rentalOffers[0].id);
      this.$root.$emit('switch-page', 'marketplace', true);
    },
  },
};
</script>