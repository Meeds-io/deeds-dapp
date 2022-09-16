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
  <v-list dense v-if="ownedNfts && ownedNfts.length">
    <v-list-item>
      <h4>{{ $t('yourDeeds') }}</h4>
    </v-list-item>
    <v-list-item
      v-for="nft in nftsByCardType"
      :key="nft.id"
      class="ps-8">
      <v-list-item-content>{{ nft.cardName }}</v-list-item-content>
      <v-list-item-content class="align-end">{{ nft.cityName }}</v-list-item-content>
      <v-list-item-content class="align-end">{{ nft.count }}</v-list-item-content>
      <v-list-item-content class="align-end" />
    </v-list-item>
  </v-list>
</template>
<script>
export default {
  data: () => ({
    cities: ['Tanit', 'Reshef', 'Ashtarte', 'Melqart', 'Eshmun', 'Kushor', 'Hammon'],
    cardTypes: ['Common', 'Uncommon', 'Epic', 'Legendary'],
  }),
  computed: Vuex.mapState({
    ownedNfts: state => state.ownedNfts,
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