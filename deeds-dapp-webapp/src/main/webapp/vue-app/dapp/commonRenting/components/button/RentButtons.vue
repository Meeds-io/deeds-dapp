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
  <v-tooltip z-index="4" disabled>
    <!-- eslint-disable-next-line vue/no-unused-vars -->
    <template #activator="{ onParent, attrsParent }">
      <v-tooltip z-index="4" bottom>
        <template #activator="{ on, attrs }">
          <v-card
            :width="buttonsWidth"
            :min-width="minButtonsWidth"
            :max-width="maxButtonsWidth"
            :class="!showSeeRentPart && 'd-none'"
            class="mx-auto mt-2 mt-md-0"
            color="transparent"
            flat
            v-bind="attrs"
            v-on="on">
            <v-badge
              :value="isCreateInProgress"
              class="full-width"
              icon="fas fa-info mt-n2px"
              color="info"
              bordered
              overlap>
              <v-btn
                :href="rentalOfferLink"
                :width="buttonsWidth"
                :min-width="minButtonsWidth"
                :max-width="maxButtonsWidth"
                color="primary"
                outlined
                depressed
                @click="openRentDetails">
                <span class="text-truncate position-absolute full-width text-capitalize text-center">
                  {{ $t('deedRentSeeRentButton') }}
                </span>
              </v-btn>
            </v-badge>
          </v-card>
        </template>
        <span>{{ rentingSeeDescription }}</span>
      </v-tooltip>
      <v-tooltip z-index="4" bottom>
        <template #activator="{ on, attrs }">
          <v-card
            :width="buttonsWidth"
            :min-width="minButtonsWidth"
            :max-width="maxButtonsWidth"
            :class="!showRentPart && 'd-none'"
            class="mx-auto mt-2 mt-md-0"
            color="transparent"
            flat
            v-bind="attrs"
            v-on="on">
            <v-badge
              :value="showEditOfferBadge"
              :icon="editOfferBadgeIcon"
              :color="editOfferBadgeIconColor"
              class="full-width"
              bordered
              overlap>
              <v-btn
                :disabled="disabledEdit"
                :loading="loadingRentDrawer"
                :width="buttonsWidth"
                :min-width="minButtonsWidth"
                :max-width="maxButtonsWidth"
                :outlined="!hasRentOffers || disabledEdit"
                color="primary"
                depressed
                @click="openRentDrawer">
                <span class="text-truncate position-absolute full-width text-capitalize">
                  {{ rentingButtonName }}
                </span>
              </v-btn>
            </v-badge>
          </v-card>
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
    cardType: {
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
  }),
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    address: state => state.address,
    networkId: state => state.networkId,
    language: state => state.language,
    authenticated: state => state.authenticated,
    tenantProvisioningContract: state => state.tenantProvisioningContract,
    now: state => state.now,
    offerId() {
      return this.rentalOffer?.offerId;
    },
    isDeleteInProgress() {
      return this.rentalOffer?.deleteId;
    },
    isUpdateInProgress() {
      return this.rentalOffer?.updateId;
    },
    isCreateInProgress() {
      return this.rentalOffer && !this.offerId || false;
    },
    acquisitionsCount() {
      return this.rentalOffer?.acquisitionIds?.length || 0;
    },
    isAcquisitionInProgress() {
      return this.acquisitionsCount > 0;
    },
    showEditOfferBadge() {
      return this.isAcquisitionInProgress || this.disabledEdit;
    },
    editOfferBadgeIcon() {
      return this.isAcquisitionInProgress && 'fas fa-cart-shopping mt-n2px' || 'fas fa-lock mt-n2px';
    },
    editOfferBadgeIconColor() {
      return this.isAcquisitionInProgress && 'warning' || 'error';
    },
    disabledEdit() {
      return this.isDeleteInProgress || this.isUpdateInProgress || false;
    },
    showRentPart() {
      return this.authenticated && (!this.rentalOffer || !!this.offerId) || false;
    },
    showSeeRentPart() {
      return this.authenticated && this.hasAvailableRentOffers || false;
    },
    hasRentOffers() {
      return !!this.offers?.length;
    },
    availableRentOffers() {
      return this.offers?.filter(offer => !offer.expirationDate || new Date(offer.expirationDate).getTime() > this.now);
    },
    hasAvailableRentOffers() {
      return this.availableRentOffers?.length > 0;
    },
    hasOnlyExpiredOffers() {
      return this.hasRentOffers && !this.hasAvailableRentOffers;
    },
    rentalOffer() {
      return (this.hasAvailableRentOffers && this.availableRentOffers[0])
        || (this.hasRentOffers && this.offers[0]);
    },
    rentalOfferLink() {
      return this.showSeeRentPart
        && `/${this.parentLocation}/marketplace?offer=${this.rentalOffer.id}`;
    },
    isRentingEnabled() {
      return this.showRentPart && this.isProvisioningManager;
    },
    rented() {
      return !this.isProvisioningManager;
    },
    rentingSeeDescription() {
      return this.offerId && this.$t('deedRentSeeRentDescription') || this.$t('deedRentingOfferCreationInProgressTooltip');
    },
    rentingDescription() {
      if (this.isAcquisitionInProgress) {
        return this.$t('deedOfferAcquisitionInProgressTooltip', {0: this.acquisitionsCount});
      }
      if (this.isDeleteInProgress) {
        return this.$t('deedRentingOfferDeletionInProgressTooltip');
      }
      if (this.isUpdateInProgress) {
        return this.$t('deedRentingOfferUpdateInProgressTooltip');
      }
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
    document.addEventListener('deed-offer-created', this.refreshOfferFromblockchain);
    document.addEventListener('deed-offer-updated', this.refreshOfferFromblockchain);
    document.addEventListener('deed-offer-deleted', this.refreshOfferFromblockchain);
    this.$root.$on('deed-offer-renting-created', this.handleRefreshOffers);
    this.$root.$on('deed-offer-renting-updated', this.handleRefreshOffers);
    this.$root.$on('deed-offer-renting-deleted', this.handleRefreshOffers);
  },
  beforeDestroy() {
    document.removeEventListener('deed-offer-created', this.refreshOfferFromblockchain);
    document.removeEventListener('deed-offer-updated', this.refreshOfferFromblockchain);
    document.removeEventListener('deed-offer-deleted', this.refreshOfferFromblockchain);
    this.$root.$off('deed-offer-renting-created', this.handleRefreshOffers);
    this.$root.$off('deed-offer-renting-updated', this.handleRefreshOffers);
    this.$root.$off('deed-offer-renting-deleted', this.handleRefreshOffers);
  },
  methods: {
    openRentDrawer() {
      this.loadingRentDrawer = true;
      this.refreshOffers()
        .then(() => this.$root.$emit('deeds-rent-drawer', this.nftId, this.rentalOffer, this.cardType))
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
          const rentalOffers = offers?._embedded?.offers && [offers?._embedded?.offers[0]] || [];
          if (broadcast) {
            this.$root.$emit('deed-offers-loaded', this.nftId, rentalOffers);
          }
        })
        .finally(() => this.loadingRentalOffers = false);
    },
    openRentDetails(event) {
      if (!event?.ctrlKey) {
        if (event) {
          event.preventDefault();
          event.stopPropagation();
        }
        this.$store.commit('setStandaloneOfferId', this.offers[0].id);
        this.$root.$emit('switch-page', 'marketplace', true);
      }
    },
    refreshOfferFromblockchain(event) {
      const offerId = event?.detail?.offerId?.toNumber() || event?.detail?.leaseId?.toNumber();
      const deedId = event?.detail?.nftId?.toNumber();
      if (this.rentalOffer && (offerId === this.offerId || deedId === this.nftId)) {
        this.$deedTenantOfferService.getOffer(this.rentalOffer.id, true)
          .catch(() => console.debug('offer seems to be deleted', this.rentalOffer.id)) // eslint-disable-line no-console
          .finally(() => this.refreshOffers(true));
      }
    },
  },
};
</script>