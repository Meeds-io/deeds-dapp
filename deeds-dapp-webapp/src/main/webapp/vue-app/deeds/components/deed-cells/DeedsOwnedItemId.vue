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
  <v-list-item class="ps-0">
    <v-list-item-avatar
      class="ma-0"
      height="55"
      tile>
      <v-img
        :src="cardImage"
        contain />
    </v-list-item-avatar>
    <v-list-item-content>
      <v-list-item-title>
        <a
          :name="`nftEtherscanLink${nft.id}`"
          :href="`${etherscanBaseLink}/nft/${deedAddress}/${nft.id}`"
          target="_blank"
          rel="nofollow noreferrer noopener"
          class="px-0 px-sm-3 d-inline d-sm-flex link--color">
          #{{ nft.id }}
        </a>
      </v-list-item-title>
    </v-list-item-content>
  </v-list-item>
</template>
<script>
export default {
  props: {
    nft: {
      type: Object,
      default: null,
    },
  },
  computed: Vuex.mapState({
    etherscanBaseLink: state => state.etherscanBaseLink,
    deedAddress: state => state.deedAddress,
    cardTypeInfos: state => state.cardTypeInfos,
    cardType() {
      return this.nft?.cardType;
    },
    cardTypeInfo() {
      return this.cardTypeInfos[this.cardType];
    },
    cardImage() {
      return this.cardTypeInfo?.image;
    },
  }),
};
</script>