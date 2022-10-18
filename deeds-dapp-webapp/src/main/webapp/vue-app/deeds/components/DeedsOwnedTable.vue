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
  <v-card flat class="mb-8">
    <v-card-title class="d-flex flex-nowrap pa-0">
      {{ $t('yourDeeds') }}
      <v-divider class="my-auto ms-4" />
    </v-card-title>
    <template v-if="hasDeeds">
      <deeds-manage-drawer :second-level-opened="secondLevelOpened" />
      <deeds-move-in-drawer @opened="secondLevelOpened = true" @closed="secondLevelOpened = false" />
      <deeds-move-out-drawer @opened="secondLevelOpened = true" @closed="secondLevelOpened = false" />
    </template>
    <v-card-text v-html="$t('yourDeedsIntroduction')" />
    <v-data-table
      ref="table"
      :headers="headers"
      :items="ownedDeeds"
      :hide-default-header="!isMobile"
      :disable-sort="isMobile"
      :no-data-text="$t('noDeedYet')"
      :loading="!initialized"
      item-key="key"
      disable-pagination
      disable-filtering
      hide-default-footer>
      <template #[`item.id`]="{item}">
        <deeds-owned-item-id :nft="item" />
      </template>
      <template #[`item.status`]="{item}">
        <deeds-owned-item-status :nft="item" />
      </template>
      <template #[`item.earnedRewards`]="{item}">
        <deeds-owned-item-earned :nft="item" />
      </template>
      <template #[`item.actions`]="{item}">
        <deeds-owned-item-menu :nft="item" />
      </template>
    </v-data-table>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    ownedDeeds: [],
    authenticated: false,
    initialized: false,
    secondLevelOpened: false,
    contractListenersInstalled: false,
  }),
  computed: Vuex.mapState({
    isMobile: state => state.isMobile,
    language: state => state.language,
    cities: state => state.cities,
    cardTypes: state => state.cardTypes,
    etherscanBaseLink: state => state.etherscanBaseLink,
    openSeaBaseLink: state => state.openSeaBaseLink,
    deedAddress: state => state.deedAddress,
    ownedNfts: state => state.ownedNfts,
    address: state => state.address,
    provider: state => state.provider,
    tenantProvisioningContract: state => state.tenantProvisioningContract,
    tenantProvisioningAddress: state => state.tenantProvisioningAddress,
    hasDeeds() {
      return this.ownedNfts && this.ownedNfts.length > 0;
    },
    headers() {
      return [{
        text: this.$t('nft.id'),
        value: 'id',
      }, {
        text: this.$t('nft.cityName'),
        value: 'cityName',
      }, {
        text: this.$t('nft.name'),
        value: 'name',
      }, {
        text: this.$t('nft.status'),
        value: 'status',
      }, {
        text: this.$t('nft.earnedRewards'),
        value: 'earnedRewards',
      }, {
        text: this.$t('nft.actions'),
        value: 'actions',
      }];
    },
  }),
  watch: {
    ownedNfts: {
      immediate: true,
      handler() {
        this.reloadStatus(true);
      },
    },
    tenantProvisioningContract: {
      immediate: true,
      handler() {
        if (!this.contractListenersInstalled) {
          this.installListeners();
        }
        this.reloadStatus(false);
      },
    },
  },
  created() {
    this.installListeners();

    this.$root.$on('nft-status-changed', (id, status, statusLabel) => {
      const nft = this.ownedNfts.find(ownedNft => ownedNft.id === id);
      this.setStatus(nft, status, statusLabel);
    });
  },
  methods: {
    installListeners() {
      if (this.tenantProvisioningContract) {
        this.contractListenersInstalled = true;

        // eslint-disable-next-line new-cap
        this.tenantProvisioningContract.on(this.tenantProvisioningContract.filters.TenantStarted(),
          this.refreshTenantStatus);

        // eslint-disable-next-line new-cap
        this.tenantProvisioningContract.on(this.tenantProvisioningContract.filters.TenantStopped(),
          this.refreshTenantStatus);
      }
    },
    reloadStatus(markInitialized) {
      if (this.hasDeeds) {
        this.ownedDeeds = this.ownedNfts.slice();

        const promises = [];
        this.ownedNfts.forEach(ownedNft => {
          if (!ownedNft.loaded) {
            Object.assign(ownedNft, {
              key: ownedNft.id,
              cityName: this.cities[ownedNft.cityIndex],
              cardTypeName: this.cardTypes[ownedNft.cardType % 4],
              earnedRewardsNoDecimals: 0,
              hasEarnedMeeds: false,
              owner: true,
              provisioningManager: false,
              status: false,
            });
            promises.push(Promise.resolve(this.loadStatus(ownedNft)));
          }
        });
        return Promise.all(promises)
          .finally(() => this.initialized = true);
      } else {
        this.ownedDeeds = [];
        if (markInitialized) {
          this.initialized = true;
        }
      }
    },
    refreshTenantStatus(address, nftId) {
      if (address.toUpperCase() === this.address.toUpperCase()) {
        if (this.hasDeeds) {
          const id = nftId.toNumber();
          const nft = this.ownedNfts.find(ownedNft => ownedNft.id === id);
          if (nft) {
            this.loadStatus(nft);
          }
        }
      }
    },
    loadStatus(nft) {
      if (nft.loaded) {
        return;
      }
      if (!this.tenantProvisioningContract) {
        this.setStatus(nft, 'STOPPED');
        return;
      }
      this.setStatus(nft, 'loading');
      return this.tenantProvisioningContract.isProvisioningManager(this.address, nft.id)
        .then(provisioningManager => {
          nft.provisioningManager = provisioningManager;
          if (provisioningManager) {
            return this.tenantProvisioningContract.tenantStatus(nft.id)
              .then(status => {
                if (status) {
                  nft.link = `https://${this.cities[nft.cityIndex]}-${nft.id}.wom.meeds.io`;
                  nft.linkLabel = `${this.cities[nft.cityIndex]}-${nft.id}.wom.meeds.io`;
                }
                this.setStatus(nft, status && 'STARTED' || 'STOPPED');
              })
              .finally(() => nft.loaded = true);
          }
        });
    },
    setStatus(nft, status, statusLabel) {
      if (nft) {
        nft.status = status;
        nft.statusLabel = statusLabel;
        nft.loaded = true;
        this.updateNft(nft);
      }
    },
    updateNft(nft) {
      if (nft) {
        // Force update nft in table by updating Table Key
        this.$set(nft, 'key', `${nft.id}-${Date.now()}-${nft.status}`);

        const index = this.ownedDeeds.findIndex(deed => deed.id === nft.id);
        if (index > -1) {
          this.$set(this.ownedDeeds, index, nft);
        }
      }
      this.ownedDeeds = this.ownedDeeds.slice();
    },
  },
};
</script>