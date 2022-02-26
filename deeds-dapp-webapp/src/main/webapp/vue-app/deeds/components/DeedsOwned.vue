<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 
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
    <h3 class="d-flex flex-nowrap">
      {{ $t('yourDeeds') }}
      <v-divider class="my-auto ms-4" />
    </h3>
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
      item-key="updateDate"
      disable-pagination
      disable-filtering
      hide-default-footer>
      <template v-slot:item.id="{item}">
        <v-btn
          :href="`${etherscanBaseLink}/token/${nftAddress}?a=${item.id}#inventory`"
          target="_blank"
          rel="noreferrer"
          color="primary"
          class="px-0 px-sm-3 d-inline d-sm-flex"
          text
          link>
          #{{ item.id }}
        </v-btn>
      </template>
      <template v-slot:item.status="{item}">
        <a
          v-if="item.status === 'STARTED'"
          :href="item.link"
          target="_blank"
          rel="noreferrer">
          {{ item.linkLabel }}
        </a>
        <div v-else-if="item.status === 'STOPPED'" class="text-capitalize">
          {{ $t('vacant') }}
        </div>
        <v-progress-circular
          v-else-if="item.status === 'loading'"
          size="24"
          color="primary"
          indeterminate />
      </template>
      <template v-slot:item.earnedRewards="{item}">
        <v-tooltip bottom>
          <template v-slot:activator="{ on, attrs }">
            <div
              v-bind="attrs"
              v-on="on">
              {{ item.earnedRewardsNoDecimals }} MEED
            </div>
          </template>
          <span>{{ item.hasEarnedMeeds && $t('tooltipClaimRewardedMeeds', {0: item.earnedRewardsNoDecimals}) || $t('tooltipClaimReward') }}</span>
        </v-tooltip>
      </template>
      <template v-slot:item.actions="{item}">
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
    etherscanBaseLink: state => state.etherscanBaseLink,
    openSeaBaseLink: state => state.openSeaBaseLink,
    nftAddress: state => state.nftAddress,
    ownedNfts: state => state.ownedNfts,
    address: state => state.address,
    provider: state => state.provider,
    tenantProvisioningContract: state => state.tenantProvisioningContract,
    tenantProvisioningAddress: state => state.tenantProvisioningAddress,
    nftContract: state => state.nftContract,
    isMobile() {
      return this.$vuetify && this.$vuetify.breakpoint && this.$vuetify.breakpoint.name === 'xs';
    },
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

    if (this.nftContract) {
      // eslint-disable-next-line new-cap
      const startedUsingNFT = this.nftContract.filters.StartedUsingNFT();
      this.nftContract.on(startedUsingNFT, (address, nftId, strategyAddress) => {
        if (address.toUpperCase() === this.address.toUpperCase()) {
          if (strategyAddress.toUpperCase() === this.tenantProvisioningAddress.toUpperCase()) {
            if (this.ownedNfts) {
              const id = nftId.toNumber();
              const nft = this.ownedNfts.find(ownedNft => ownedNft.id === id);
              if (nft) {
                Promise.resolve(this.loadStatus(nft))
                  .finally(() => this.nfts = Object.assign({}, this.nfts));
              }
            }
          }
        }
      });
      // eslint-disable-next-line new-cap
      const endedUsingNFT = this.nftContract.filters.EndedUsingNFT();
      this.nftContract.on(endedUsingNFT, (address, nftId, strategyAddress) => {
        if (address.toUpperCase() === this.address.toUpperCase()) {
          if (strategyAddress.toUpperCase() === this.tenantProvisioningAddress.toUpperCase()) {
            if (this.ownedNfts) {
              const id = nftId.toNumber();
              const nft = this.ownedNfts.find(ownedNft => ownedNft.id === id);
              if (nft) {
                Promise.resolve(this.loadStatus(nft))
                  .finally(() => this.nfts = Object.assign({}, this.nfts));
              }
            }
          }
        }
      });
    }

    this.$root.$on('nft-status-changed', (id, status) => {
      const nft = this.ownedNfts.find(ownedNft => ownedNft.id === id);
      if (nft) {
        nft.status = status;
        this.nfts = Object.assign({}, this.nfts);
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
    reloadStatus() {
      if (this.tenantProvisioningContract && this.ownedNfts && this.ownedNfts.length) {
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
                // Force update nft in table by updating Table Key
                nft.updateDate = Date.now();
              });
          }
        });
    },
  },
};
</script>