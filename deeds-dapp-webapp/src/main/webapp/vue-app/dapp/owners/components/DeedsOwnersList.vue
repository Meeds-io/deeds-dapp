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
  <v-card id="leasesList" flat>
    <v-row v-if="hasLeases || hasNfts" class="pa-0 my-0">
      <v-col cols="12" class="ps-4 ps-sm-5">
        <div class="d-flex flex-row flex-grow-1">
          <div class="headline text-sm-h4 font-weight-bold ps-0 py-0">{{ $t('yourDeeds') }}</div>
        </div>
      </v-col>
      <v-col
        v-for="(lease, index) in deedsToDisplay"
        :key="`${lease.nftId}-${lease.index || index}`"
        class="d-flex justify-center"
        cols="12">
        <v-fade-transition>
          <deeds-owners-deed-card
            :lease="lease"
            @refresh="refreshDeedOrLease" />
        </v-fade-transition>
      </v-col>
    </v-row>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    leases: [],
    ownedDeeds: [],
    deedsToDisplay: [],
    sortField: 'createdDate',
    sortDirection: 'desc',
    pageSize: 100,
    totalSize: 0,
    rentedOffersLoaded: 0,
    rentedOffers: {},
    tenants: {},
    tenantsLoadingPromise: null,
    sortResults: true,
    refreshedLeases: {},
  }),
  computed: Vuex.mapState({
    address: state => state.address,
    networkId: state => state.networkId,
    ownedNfts: state => state.ownedNfts,
    cities: state => state.cities,
    cardTypes: state => state.cardTypes,
    selectedStandaloneDeedCardName: state => state.selectedStandaloneDeedCardName,
    authenticated: state => state.authenticated,
    deedLoading: state => state.deedLoading,
    ownedLeases() {
      return this.leases && this.leases.filter(lease => this.ownedNfts.findIndex(nft => nft.id === lease.nftId) >= 0);
    },
    loadedLeasesLength() {
      return this.ownedLeases?.length || 0;
    },
    hasLeases() {
      return this.loadedLeasesLength > 0;
    },
    hasNfts() {
      return this.ownedNfts.length > 0;
    },
  }),
  watch: {
    ownedNfts() {
      this.computeDeedsToDisplay();
    },
  },
  created() {
    this.init();
    this.$root.$on('deed-offers-loaded', this.refreshLoadedOffers);
  },
  beforeDestroy() {
    this.$root.$off('deed-offers-loaded', this.refreshLoadedOffers);
  },
  methods: {
    init() {
      this.tenantsLoadingPromise = this.loadTenants();
      Promise.all([
        this.tenantsLoadingPromise,
        this.loadLeases(),
        this.loadOffers()
      ]).finally(() => this.computeDeedsToDisplay());
    },
    loadLeases() {
      return this.$deedTenantLeaseService.getLeases({
        sort: `${this.sortField},${this.sortDirection}`,
        address: this.address,
        onlyConfirmed: false,
        owner: true,
      }, this.networkId)
        .then(data => {
          const leases = data?._embedded?.leases || [];
          leases.forEach(lease => {
            lease.deedInfo = this.getDeedInfo(lease, lease.nftId);
            lease.index = 1;
          });
          this.leases = leases.filter(lease => lease.confirmed);
          this.totalSize = leases?.page?.totalElements || this.leases.length;
        })
        .finally(() => this.rentedOffersLoaded++);
    },
    loadOffers() {
      return this.$deedTenantOfferService.getOffers({
        address: this.address,
        onlyOwned: true,
        excludeExpired: true,
        excludeNotStarted: false,
      }, this.networkId)
        .then(offers => {
          this.rentedOffers = {};
          const rentalOffers = offers?._embedded?.offers || [];
          rentalOffers.forEach(offer => {
            if (!this.rentedOffers[offer.nftId]) {
              this.rentedOffers[offer.nftId] = [offer];
            } else {
              this.rentedOffers[offer.nftId].push(offer);
            }
          });
        })
        .finally(() => this.rentedOffersLoaded++);
    },
    loadTenants() {
      if (this.authenticated) {
        return this.$tenantManagement.getTenants()
          .then(tenants => {
            this.tenants = {};
            tenants.forEach(tenant => this.tenants[tenant.nftId] = tenant);
            return this.tenants;
          })
          .catch(() => {
            this.tenants = {};
          })
          .finally(() => this.rentedOffersLoaded++);
      } else {
        this.rentedOffersLoaded++;
        return Promise.resolve(null);
      }
    },
    getDeedInfo(lease, deedId) {
      return this.tenantsLoadingPromise
        .then(tenants => lease.deedInfo = tenants[deedId])
        .catch(() => lease.deedInfo = null);
    },
    refreshLoadedOffers(deedId, rentalOffers) {
      const deed = this.deedsToDisplay.find(displayedLease => displayedLease.nftId === deedId);
      if (deed) {
        deed.rentedOffers = rentalOffers;
        deed.index = (deed.index || 0) + 1;
        this.refreshDeedOrLease(deed);
      }
    },
    refreshDeedOrLease(lease) {
      const index = this.deedsToDisplay.findIndex(displayedLease => displayedLease.nftId === lease.nftId);
      if (index >= 0) {
        lease.index = this.deedsToDisplay[index].index + 1;
        this.deedsToDisplay.splice(index, 1, lease);
      } else {
        lease.deedInfo = this.getDeedInfo(lease, lease.nftId);
        this.refreshedLeases[lease.nftId] = lease;
        this.deedsToDisplay = [...this.ownedLeases, this.ownedDeeds];
      }
    },
    computeDeedsToDisplay() {
      const ownedNftsNoLease = this.ownedNfts && this.ownedNfts.filter(deed => {
        return !this.leases.find(lease => lease.nftId === deed.id);
      }) || [];
      const ownedDeeds = ownedNftsNoLease.map(deed => {
        const lease = this.refreshedLeases[deed.id] || {
          nftId: deed.id,
          city: this.cities[deed.cityIndex],
          cardType: this.cardTypes[deed.cardType],
          ownerAddress: this.address,
          managerAddress: this.address,
          rentedOffers: this.rentedOffers[deed.id]?.slice() || [],
          deedInfo: this.tenants[deed.id],
          index: 1,
        };
        if (!lease.deedInfo) {
          lease.deedInfo = this.getDeedInfo(lease, deed.id);
        }
        return lease;
      });
      if (this.sortResults && ownedDeeds.length > 0) {
        if (this.ownedNfts?.length > 0 && this.rentedOffersLoaded) {
          this.sortResults = false;
        }
        this.ownedDeeds = ownedDeeds
          .sort((deed1, deed2) =>
            (deed2.rentedOffers?.length || 0) - (deed1.rentedOffers?.length || 0)
          );
      } else {
        this.ownedDeeds = ownedDeeds;
      }
      if (this.selectedStandaloneDeedCardName) {
        this.deedsToDisplay = [...this.ownedLeases, ...this.ownedDeeds].sort((a, b) => {
          if (a.cardType?.toUpperCase() !== b.cardType?.toUpperCase()) {
            if (a.cardType?.toUpperCase() === this.selectedStandaloneDeedCardName.toUpperCase()) {
              return -1;
            } else if (b.cardType?.toUpperCase() === this.selectedStandaloneDeedCardName.toUpperCase()) {
              return 1;
            }
          }
          return 0;
        });
      } else {
        this.deedsToDisplay = [...this.ownedLeases, ...this.ownedDeeds];
      }
      if (this.selectedStandaloneDeedId) {
        const index = this.deedsToDisplay.findIndex(displayedLease => displayedLease.nftId === this.selectedStandaloneDeedId);
        if (index >= 0) {
          const lease = this.deedsToDisplay[index];
          this.deedsToDisplay.splice(index, 1);
          this.deedsToDisplay.unshift(lease);
        }
      }
      if (!this.deedLoading && this.rentedOffersLoaded >= 3) {
        this.$root.$emit('deed-leases-loaded', this.deedsToDisplay, this.deedsToDisplay.length);
      }
    },
  },
};
</script>