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
  <v-card flat>
    <v-card-title class="d-flex flex-nowrap pa-0">
      {{ $t('yourDeeds') }}
      <v-divider class="my-auto ms-4" />
    </v-card-title>
    <template v-if="ownedNfts && ownedNfts.length">
      <deeds-move-in-drawer />
      <deeds-move-out-drawer />
    </template>
    <v-card-text v-html="$t('yourDeedsIntroduction')" />
    <v-data-table
      :headers="headers"
      :items="nftsList"
      :hide-default-header="!isMobile"
      :disable-sort="isMobile"
      :no-data-text="$t('noDeedYet')"
      item-key="updateDate"
      disable-pagination
      disable-filtering
      hide-default-footer>
      <template #[`item.id`]="{item}">
        <a
          :name="`nftEtherscanLink${item.id}`"
          :href="`${etherscanBaseLink}/nft/${deedAddress}/${item.id}`"
          target="_blank"
          rel="nofollow noreferrer noopener"
          class="px-0 px-sm-3 d-inline d-sm-flex link--color">
          #{{ item.id }}
        </a>
      </template>
      <template #[`item.status`]="{item}">
        <a
          v-if="item.status === 'STARTED'"
          :href="item.link"
          target="_blank"
          rel="nofollow noreferrer noopener">
          {{ item.linkLabel }}
        </a>
        <div v-else-if="item.status === 'STOPPED'" class="text-capitalize">
          {{ $t('vacant') }}
        </div>
        <v-tooltip v-else-if="item.status === 'loading'" bottom>
          <template #activator="{ on, attrs }">
            <v-progress-circular
              size="24"
              color="primary"
              indeterminate
              v-bind="attrs"
              v-on="on" />
          </template>
          <span>{{ item.statusLabel || '' }}</span>
        </v-tooltip>
      </template>
      <template #[`item.earnedRewards`]="{item}">
        <v-tooltip bottom>
          <template #activator="{ on, attrs }">
            <div
              v-bind="attrs"
              v-on="on">
              {{ item.earnedRewardsNoDecimals }} MEED
            </div>
          </template>
          <span>{{ item.hasEarnedMeeds && $t('tooltipClaimRewardedMeeds', {0: item.earnedRewardsNoDecimals}) || $t('tooltipClaimReward') }}</span>
        </v-tooltip>
      </template>
      <template #[`item.actions`]="{item}">
        <deeds-owned-actions
          :nft="item"
          :authenticated="authenticated"
          @login="login"
          @logout="logout" />
      </template>
    </v-data-table>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    cities: ['Tanit', 'Reshef', 'Ashtarte', 'Melqart', 'Eshmun', 'Kushor', 'Hammon'],
    cardTypes: ['Common', 'Uncommon', 'Epic', 'Legendary'],
    nfts: {},
    authenticated: false,
  }),
  computed: Vuex.mapState({
    isMobile: state => state.isMobile,
    language: state => state.language,
    etherscanBaseLink: state => state.etherscanBaseLink,
    openSeaBaseLink: state => state.openSeaBaseLink,
    deedAddress: state => state.deedAddress,
    ownedNfts: state => state.ownedNfts,
    address: state => state.address,
    provider: state => state.provider,
    tenantProvisioningContract: state => state.tenantProvisioningContract,
    tenantProvisioningAddress: state => state.tenantProvisioningAddress,
    nftsList() {
      return Object.values(this.nfts).sort((nft1, nft2) => nft2.id - nft1.id)
        .map(nft => Object.assign(nft, {
          updateDate: Date.now(),
        }));
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
        this.reloadStatus();
      },
    },
    tenantProvisioningContract: {
      immediate: true,
      handler() {
        this.reloadStatus();
      },
    },
  },
  created() {
    this.refreshAuthentication();

    if (this.tenantProvisioningContract) {
      // eslint-disable-next-line new-cap
      this.tenantProvisioningContract.on(this.tenantProvisioningContract.filters.TenantStarted(),
        this.refreshTenantStatus);
      // eslint-disable-next-line new-cap
      this.tenantProvisioningContract.on(this.tenantProvisioningContract.filters.TenantStopped(),
        this.refreshTenantStatus);
    }

    this.$root.$on('nft-status-changed', (id, status, statusLabel) => {
      const nft = this.ownedNfts.find(ownedNft => ownedNft.id === id);
      if (nft) {
        nft.status = status;
        nft.statusLabel = statusLabel;
        this.nfts = Object.assign({}, this.nfts);
        this.$forceUpdate();
      }
    });
  },
  methods: {
    refreshAuthentication() {
      this.authenticated = this.$authentication.isAuthenticated(this.address);
    },
    logout() {
      this.$authentication.logout(this.address)
        .finally(() => this.refreshAuthentication());
    },
    login() {
      const token = document.querySelector('[name=loginMessage]').value;
      const message = this.$t('signMessage', {0: token});
      this.$ethUtils.signMessage(this.provider, this.address, message)
        .then(signedMessage => this.$authentication.login(this.address, message, signedMessage))
        .finally(() => this.refreshAuthentication());
    },
    refreshTenantStatus(address, nftId) {
      if (address.toUpperCase() === this.address.toUpperCase()) {
        if (this.ownedNfts) {
          const id = nftId.toNumber();
          const nft = this.ownedNfts.find(ownedNft => ownedNft.id === id);
          if (nft) {
            Promise.resolve(this.loadStatus(nft))
              .finally(() => this.nfts = Object.assign({}, this.nfts));
          }
        }
      }
    },
    reloadStatus() {
      if (this.ownedNfts && this.ownedNfts.length) {
        const promises = [];
        this.ownedNfts.forEach(ownedNft => {
          if (!this.nfts[ownedNft.id]) {
            this.nfts[ownedNft.id] = Object.assign(ownedNft, {
              cityName: this.cities[ownedNft.cityIndex],
              earnedRewardsNoDecimals: 0,
              hasEarnedMeeds: false,
              owner: true,
              provisioningManager: false,
              status: false,
            });
          }
          if (!ownedNft.status) {
            promises.push(Promise.resolve(this.loadStatus(ownedNft)));
          }
        });
        // force refresh computed value
        Promise.all(promises)
          .then(() => this.nfts = Object.assign({}, this.nfts));
      }
    },
    loadStatus(nft) {
      if (!this.tenantProvisioningContract) {
        nft.status = 'STOPPED';
        return;
      }
      nft.status = 'loading';
      return this.tenantProvisioningContract.isProvisioningManager(this.address, nft.id)
        .then(provisioningManager => {
          if (provisioningManager) {
            nft.provisioningManager = true;
            return this.tenantProvisioningContract.tenantStatus(nft.id)
              .then(status => {
                if (status) {
                  nft.link = `https://${this.cities[nft.cityIndex]}-${nft.id}.wom.meeds.io`;
                  nft.linkLabel = `${this.cities[nft.cityIndex]}-${nft.id}.wom.meeds.io`;
                }
                nft.status = status && 'STARTED' || 'STOPPED';
                nft.statusLabel = null;
                // Force update nft in table by updating Table Key
                nft.updateDate = Date.now();
              });
          }
        });
    },
  },
};
</script>