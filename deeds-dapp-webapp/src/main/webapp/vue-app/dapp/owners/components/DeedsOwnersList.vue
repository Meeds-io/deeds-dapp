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
    id="leasesList"
    :loading="loading"
    flat>
    <v-row v-if="hasLeases || hasNfts" class="pa-0 my-0">
      <v-col cols="12">
        <div class="d-flex flex-row flex-grow-1">
          <div class="subtitle-1 font-weight-bold ps-0 py-0">{{ $t('yourDeeds') }}</div>
          <v-divider class="my-auto" />
        </div>
      </v-col>
      <v-col
        v-for="lease in leases"
        :key="lease.id"
        class="d-flex justify-center"
        cols="12">
        <v-slide-x-transition>
          <deeds-owners-deed-card :lease="lease" />
        </v-slide-x-transition>
      </v-col>
      <v-col
        v-for="deed in ownedDeeds"
        :key="deed.nftId"
        class="d-flex justify-center"
        cols="12">
        <v-slide-x-transition>
          <deeds-owners-deed-card :lease="deed" />
        </v-slide-x-transition>
      </v-col>
    </v-row>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    leases: [],
    sortField: 'createdDate',
    sortDirection: 'desc',
    pageSize: 100,
    totalSize: 0,
    loading: true,
    rentedOffersLoaded: false,
    rentedOffers: {},
    tenants: {},
    tenantsLoaded: false,
  }),
  computed: Vuex.mapState({
    address: state => state.address,
    networkId: state => state.networkId,
    ownedNfts: state => state.ownedNfts,
    cities: state => state.cities,
    cardTypes: state => state.cardTypes,
    loadedLeasesLength() {
      return this.leases?.length || 0;
    },
    hasLeases() {
      return this.loadedLeasesLength > 0;
    },
    hasNfts() {
      return this.ownedNfts.length > 0;
    },
    ownedDeeds() {
      if (!this.ownedNfts?.length || !this.rentedOffersLoaded || !this.tenantsLoaded) {
        return [];
      }
      const ownedNftsNoLease = this.ownedNfts.filter(deed => {
        return !this.leases.find(lease => lease.nftId === deed.id);
      });
      return ownedNftsNoLease.map(deed => ({
        nftId: deed.id,
        city: this.cities[deed.cityIndex],
        cardType: this.cardTypes[deed.cardType],
        ownerAddress: this.address,
        managerAddress: this.address,
        rentedOffers: this.rentedOffers[deed.id]?.slice() || [],
        deedInfo: this.tenants[deed.id],
      })).sort((deed1, deed2) => (deed2.rentedOffers?.length || 0) - (deed1.rentedOffers?.length || 0));
    },
  }),
  watch: {
    hasLeases() {
      this.emitLoadedDeeds();
    },
    hasNfts() {
      this.emitLoadedDeeds();
    },
  },
  created() {
    this.init();
    this.$root.$on('deed-lease-ended', this.refreshLease);
    this.$root.$on('deed-lease-payed', this.refreshLease);
    this.$root.$on('deed-offer-renting-updated', this.handleOfferUpdated);
    this.$root.$on('deed-offer-renting-deleted', this.handleOfferUpdated);
    this.$root.$on('deed-offer-rented', this.handleOfferRented);
    document.addEventListener('deed-lease-paid', this.refreshLeaseFromblockchain);
    document.addEventListener('deed-lease-ended', this.refreshLeaseFromblockchain);
    document.addEventListener('deed-lease-tenant-evicted', this.refreshLeaseFromblockchain);
  },
  beforeDestroy() {
    this.$root.$off('deed-lease-ended', this.refreshLease);
    this.$root.$off('deed-lease-payed', this.refreshLease);
    this.$root.$off('deed-offer-renting-updated', this.handleOfferUpdated);
    this.$root.$off('deed-offer-renting-deleted', this.handleOfferUpdated);
    this.$root.$off('deed-offer-rented', this.handleOfferRented);
    document.removeEventListener('deed-lease-paid', this.refreshLeaseFromblockchain);
    document.removeEventListener('deed-lease-ended', this.refreshLeaseFromblockchain);
    document.removeEventListener('deed-lease-tenant-evicted', this.refreshLeaseFromblockchain);
  },
  methods: {
    init() {
      Promise.all([
        this.refreshLeases(),
        this.refreshOffers(),
        this.refreshTenants()
      ]).finally(() => this.emitLoadedDeeds());
    },
    emitLoadedDeeds() {
      const deeds = [...this.leases, ...this.ownedDeeds];
      this.$root.$emit('deed-leases-loaded', deeds, deeds.length);
      this.$nextTick(() => this.loading = false);
    },
    refreshLeaseFromblockchain(event) {
      if (event?.detail?.leaseId?.toNumber) {
        const leaseId = event.detail.leaseId.toNumber();
        const selectedLease = this.leases.find(lease => lease.id === leaseId);
        if (selectedLease) {
          this.$deedTenantLeaseService.getLease(leaseId, true)
            .then(lease => {
              if (lease) {
                this.refreshLease(lease);
              }
            });
        }
      }
    },
    refreshLease(lease) {
      const index = this.leases.findIndex(displayedLease => displayedLease.id === lease.id);
      if (index >= 0) {
        this.leases.splice(index, 1, lease);
      }
    },
    refreshTenants() {
      return this.$tenantManagement.getTenants()
        .then(tenants => {
          this.tenants = {};
          tenants.forEach(tenant => this.tenants[tenant.nftId] = tenant);
        })
        .catch(() => this.tenants = {})
        .finally(() => this.tenantsLoaded = true);
    },
    refreshOffers() {
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
        .finally(() => this.rentedOffersLoaded = true);
    },
    handleOfferUpdated(offer) {
      if (offer) {
        this.refreshOffers();
      }
    },
    handleOfferRented(lease, offer) {
      if (offer) {
        this.refreshOffers();
      }
      if (lease) {
        this.refreshLeases();
      }
    },
    refreshLeases() {
      return this.$deedTenantLeaseService.getLeases({
        sort: `${this.sortField},${this.sortDirection}`,
        address: this.address,
        onlyConfirmed: false,
        owner: true,
      }, this.networkId)
        .then(leases => {
          this.leases = leases?._embedded?.leases || [];
          this.totalSize = leases?.page?.totalElements || this.leases.length;
        });
    },
  },
};
</script>