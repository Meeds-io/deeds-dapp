<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 
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
  <v-list dense class="pb-4 pb-sm-0">
    <div class="d-flex flex-nowrap align-center mb-4">
      <v-img
        :src="`${parentLocation}/static/images/deeds.webp`"
        height="52px"
        max-width="81px"
        contain
        eager />
      <span class="headline font-weight-bold align-center mx-5 mb-1">
        {{ $t('deed') }}
      </span>
      <v-spacer />
      <v-btn
        id="pMint-button"
        :href="mintUrl"
        class="px-7"
        color="primary"
        height="45px"
        outlined>
        <h4 class="ms-1">{{ $t('page.deeds') }}</h4>
      </v-btn>
    </div>
    <v-skeleton-loader
      v-if="deedLoading"
      type="image"
      class="me-4 my-2"
      max-width="100%"
      max-height="80px" />
    <div v-else-if="ownedNfts && ownedNfts.length">
      <deeds-deed-asset 
        v-for="nft in nftsByCardType"
        :key="nft.id"
        :deed="nft" />
    </div>
    <deeds-empty-assets
      v-else
      id="emptyDeedAssets"
      description-part1="noDeedsDescriptionPart1"
      description-part2="noDeedsDescriptionPart2"
      link-part1="becomingADeedOwner"
      link-part2="deedTabLink"
      target-tab="mint" />
  </v-list>
</template>
<script>
export default {
  computed: Vuex.mapState({
    deedLoading: state => state.deedLoading,
    ownedNfts: state => state.ownedNfts,
    whitepaperLink: state => state.whitepaperLink,
    cities: state => state.cities,
    cardTypes: state => state.cardTypes,
    parentLocation: state => state.parentLocation,
    mintUrl: state => state.mintUrl,
    nftsByCardType() {
      const nftsByCardType = {};
      if (this.ownedNfts) {
        this.ownedNfts.forEach(nft => {
          if (!nftsByCardType[nft.cardType]) {
            nftsByCardType[nft.cardType] = {
              id: nft.cardType,
              cityName: this.cities[nft.cityIndex],
              cardName: this.cardTypes[parseInt(nft.cardType % 4)],
              count: 1,
            };
          } else {
            nftsByCardType[nft.cardType].count++;
          }
        });
      }
      return Object.values(nftsByCardType).sort((type1, type2) => type1.id - type2.id);
    },
  }),
  created() {
    this.$store.commit('loadOwnedNfts');
  },
};
</script>