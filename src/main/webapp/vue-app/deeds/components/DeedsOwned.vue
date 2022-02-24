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
      :items="nfts"
      :items-per-page="10"
      hide-default-header>
      <template v-slot:item.id="{item}">
        <v-btn
          :href="`${etherscanBaseLink}/token/${nftAddress}?a=${item.id}#inventory`"
          target="_blank"
          rel="noreferrer"
          color="primary"
          text
          link>
          #{{ item.id }}
        </v-btn>
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
        <deeds-owned-actions :nft="item" />
      </template>
    </v-data-table>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    cities: ['Tanit', 'Reshef', 'Ashtarte', 'Melqart', 'Eshmun', 'Kushor', 'Hammon'],
    cardTypes: ['Common', 'Uncommon', 'Epic', 'Legendary'],
    headers: [
      {
        text: '',
        value: 'id',
      },
      {
        text: '',
        value: 'cityName',
      },
      {
        text: 'id',
        value: 'name',
      },
      {
        text: 'status',
        value: 'statusLabel',
      },
      {
        text: 'earnedRewards',
        value: 'earnedRewards',
      },
      {
        text: 'actions',
        value: 'actions',
      },
    ],
  }),
  computed: Vuex.mapState({
    etherscanBaseLink: state => state.etherscanBaseLink,
    openSeaBaseLink: state => state.openSeaBaseLink,
    nftAddress: state => state.nftAddress,
    ownedNfts: state => state.ownedNfts,
    address: state => state.address,
    provider: state => state.provider,
    nfts() {
      return this.ownedNfts && this.ownedNfts.length && this.ownedNfts.slice().reverse().map(nft => {
        return Object.assign({
          id: nft.id,
          cityName: this.cities[nft.cityIndex],
          hasEarnedMeeds: false,
          earnedRewardsNoDecimals: 0,
        }, nft);
      }) || [];
    },
  }),
};
</script>