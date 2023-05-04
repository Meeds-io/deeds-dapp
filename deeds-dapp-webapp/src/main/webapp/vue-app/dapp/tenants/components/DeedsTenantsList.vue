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
    <v-row v-if="hasLeases" class="pa-0 my-0">
      <v-col cols="12">
        <div class="d-flex flex-row flex-grow-1">
          <div class="dark-grey--text display-1 font-weight-bold ps-0 py-0">{{ $t('dapp.tenants.yourTenants') }}</div>
        </div>
      </v-col>
      <v-col
        v-for="(lease, index) in leases"
        :key="`${lease.id}-${index}`"
        class="d-flex justify-center"
        cols="12">
        <v-slide-x-transition>
          <deeds-tenants-lease-card :lease="lease" />
        </v-slide-x-transition>
      </v-col>
      <v-col v-if="hasMore" cols="12">
        <v-btn
          :loading="loading"
          outlined
          text
          block
          @click="loadMore">
          {{ $t('loadMore') }}
        </v-btn>
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
    pageSize: 9,
    limit: 0,
    totalSize: 0,
    loading: false,
  }),
  computed: Vuex.mapState({
    address: state => state.address,
    networkId: state => state.networkId,
    loadedLeasesLength() {
      return this.leases?.length || 0;
    },
    hasLeases() {
      return this.loadedLeasesLength > 0;
    },
    hasMore() {
      return this.totalSize > this.loadedLeasesLength;
    },
  }),
  watch: {
    limit() {
      if (!this.loading) {
        this.refresh();
      }
    },
    loading() {
      if (!this.loading) {
        this.showEmptyBlock = !this.hasOffers;
      }
    },
  },
  created() {
    this.init();
    this.$root.$on('deed-lease-ended', this.refreshLease);
    this.$root.$on('deed-lease-payed', this.refreshLease);
    document.addEventListener('deed-lease-paid', this.refreshLeaseFromblockchain);
    document.addEventListener('deed-lease-ended', this.refreshLeaseFromblockchain);
    document.addEventListener('deed-lease-tenant-evicted', this.refreshLeaseFromblockchain);
  },
  beforeDestroy() {
    this.$root.$off('deed-lease-ended', this.refreshLease);
    this.$root.$off('deed-lease-payed', this.refreshLease);
    document.removeEventListener('deed-lease-paid', this.refreshLeaseFromblockchain);
    document.removeEventListener('deed-lease-ended', this.refreshLeaseFromblockchain);
    document.removeEventListener('deed-lease-tenant-evicted', this.refreshLeaseFromblockchain);
  },
  methods: {
    init() {
      this.loadFirstPage();
    },
    loadFirstPage() {
      this.limit = this.pageSize;
    },
    loadMore() {
      this.limit += this.pageSize;
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
    refresh() {
      this.loading = true;
      return this.$deedTenantLeaseService.getLeases({
        page: 0,
        size: this.limit,
        sort: `${this.sortField},${this.sortDirection}`,
        address: this.address,
        onlyConfirmed: false,
        owner: false,
      }, this.networkId)
        .then(leases => {
          this.leases = leases?._embedded?.leases || [];
          this.totalSize = leases?.page?.totalElements || this.leases.length;
          this.$root.$emit('deed-leases-loaded', this.leases, this.totalSize);
        })
        .finally(() => this.loading = false);
    },
  },
};
</script>