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
  <v-list dense class="pb-4 pb-sm-0">
    <v-list-item>
      <h4>{{ $t('yourDeeds') }}</h4>
    </v-list-item>
    <v-skeleton-loader
      v-if="deedLoading"
      type="image"
      class="mx-auto ma-2"
      max-width="90%" />
    <div v-else-if="ownedNfts && ownedNfts.length">
      <deeds-deed-asset 
        v-for="nft in nftsByCardType"
        :key="nft.id"
        :deed="nft" />
    </div>
    <v-row v-else class="ps-4 ma-0 pt-4 d-flex flex-row">
      <v-col
        cols="auto"
        class="pa-0 mb-4 me-4"
        align-self="start">
        <v-img 
          height="100px"
          width="140px"
          :src="`/${parentLocation}/static/images/deeds.png`"
          contain
          eager />
      </v-col>
      <v-col align-self="end" class="pa-0 me-4">
        <v-card min-width="240" flat>
          <v-card-text class="pa-0" v-html="$t('noDeedsDescription', {0: `<a target='_blank' href='${whitepaperLink}' class='link--color' rel='nofollow noreferrer noopener'>${$t('whitePaper')}</a>`})" />
          <v-card-text class="px-0">
            {{ $t('howGetDeed') }}
            {{ $t('see') }}
            <a
              class="text-decoration-underline"
              @click="$root.$emit('switch-page', 'deeds')">
              {{ $t('there') }}
            </a>
            {{ $t('moreInformation') }}
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-list>
</template>
<script>
export default {
  data: () => ({
    cities: ['Tanit', 'Reshef', 'Ashtarte', 'Melqart', 'Eshmun', 'Kushor', 'Hammon'],
    cardTypes: ['Common', 'Uncommon', 'Epic', 'Legendary'],
  }),
  computed: Vuex.mapState({
    deedLoading: state => state.deedLoading,
    ownedNfts: state => state.ownedNfts,
    whitepaperLink: state => state.whitepaperLink,
    parentLocation: state => state.parentLocation,
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
};
</script>