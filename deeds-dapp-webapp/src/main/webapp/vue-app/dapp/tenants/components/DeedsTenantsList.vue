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
    id="tenantsList"
    :loading="loading"
    flat>
    <v-row v-if="hasTenants" class="pa-0 my-0">
      <v-col cols="12">
        <div class="d-flex flex-row flex-grow-1">
          <div class="subtitle-1 font-weight-bold ps-0 py-0">{{ $t('dapp.tenants.yourTenants') }}</div>
          <v-divider class="my-auto" />
        </div>
      </v-col>
      <v-col
        v-for="(tenant, index) in tenants"
        :key="`${tenant.id}-${index}`"
        class="d-flex justify-center"
        cols="12">
        <deeds-tenant-card :tenant="tenant" />
      </v-col>
      <v-col v-if="hasMore" cols="12">
        <v-btn
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
    tenants: [{
      'id': '5z-So4QBvtG_sqDaIOWo',
      'nftId': 5,
      'city': 'TANIT',
      'cardType': 'LEGENDARY',
      'mintingPower': 2,
      'owner': '0x12eF3db2F4F2F2ace676d9EAABFaE4d98EFdA9f5',
      'manager': '0xa5ef66131fbd70a1ffa69a95aba172f0a0502e6a',
      'amount': 100,
      'tenantStatus': 'DEPLOYED',
      'distributedAmount': 14,
      'paidPeriods': 2,
      'paymentPeriodicity': 'ONE_MONTH',
      'securityDepositPeriod': 'ONE_MONTH',
      'ownerMintingPercentage': 50,
      'enabled': true,
      'tenantStartDate': '2022-01-17T15:01:25.903Z',
      'acquiredDate': '2022-11-17T15:01:25.903Z',
      'rentingEndDate': '2023-10-19T15:01:25.903Z',
      'noticeDate': '2023-09-18T15:01:25.903Z',
      'createdDate': '2022-11-17T15:01:25.903Z',
      'modifiedDate': '2022-11-17T17:52:02.522Z',
    }, {
      'id': '7z-So4QBvtG_sqDaIOAd',
      'nftId': 1,
      'city': 'TANIT',
      'cardType': 'COMMON',
      'mintingPower': 1,
      'owner': '0x12eF3db2F4F2F2ace676d9EAABFaE4d98EFdA9f5',
      'manager': '0xa5ef66131fbd70a1ffa69a95aba172f0a0502e6a',
      'amount': 178500,
      'tenantStatus': 'UNDEPLOYED',
      'distributedAmount': 0,
      'paidPeriods': 3,
      'paymentPeriodicity': 'ONE_YEAR',
      'securityDepositPeriod': 'NO_PERIOD',
      'ownerMintingPercentage': 25,
      'enabled': true,
      'tenantStartDate': null,
      'acquiredDate': '2022-01-17T15:01:25.903Z',
      'rentingEndDate': '2023-01-10T15:01:25.903Z',
      'noticeDate': null,
      'createdDate': '2022-11-17T15:01:25.903Z',
      'modifiedDate': '2022-11-17T17:52:02.522Z',
    }],
    sortField: 'acquiredDate',
    sortDirection: 'desc',
    pageSize: 9,
    limit: 0,
    totalSize: 0,
    loading: false,
  }),
  computed: Vuex.mapState({
    address: state => state.address,
    loadedTenantsLength() {
      return this.tenants?.length || 0;
    },
    hasTenants() {
      return this.loadedTenantsLength > 0;
    },
    hasMore() {
      return this.totalSize > this.loadedTenantsLength;
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
    refresh() {
      this.loading = true;
      return this.$tenantManagement.getRentedTenants({
        page: 0,
        size: this.limit,
        sort: `${this.sortField},${this.sortDirection}`,
        address: this.address,
      })
        .then(tenants => {
          this.tenants = tenants || this.tenants.filter(tenant => tenant.managerAddress.toLowerCase() === this.address.toLowerCase());
          this.totalSize = tenants?.page?.totalElements || this.tenants.length;
          this.$root.$emit('deed-tenants-loaded', this.tenants, this.totalSize);
        })
        .finally(() => this.loading = false);
    },
  },
};
</script>