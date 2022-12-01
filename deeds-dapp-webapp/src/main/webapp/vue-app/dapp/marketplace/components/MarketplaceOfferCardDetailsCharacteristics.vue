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
  <div class="d-flex flex-row">
    <div class="flex-grow-0">
      <v-list-item-avatar
        :height="imageSize"
        :width="imageSize"
        :max-width="maxImageSize"
        :max-height="maxImageSize"
        class="deed-avatar">
        <v-img :src="cardImage" />
      </v-list-item-avatar>
    </div>
    <div class="d-flex flex-column flex-grow-1">
      <v-list-item class="min-height-auto">
        <v-list-item-title>
          <v-card-title class="px-0 py-2">
            {{ $t('deedRentalNftTypeTitle', {0: cardType, 1: nftId}) }}
          </v-card-title>
        </v-list-item-title>
      </v-list-item>
      <v-list-item class="min-height-auto" dense>
        <v-list-item-subtitle class="text-color">
          {{ $t('cityName', {0: city}) }}
        </v-list-item-subtitle>
      </v-list-item>
      <v-list-item class="min-height-auto" dense>
        <v-list-item-subtitle class="text-color">
          {{ $t('deedCharacteristicsMaxUsers') }}: {{ cardMaxUsers }}
        </v-list-item-subtitle>
      </v-list-item>
      <v-list-item class="min-height-auto" dense>
        <v-list-item-subtitle class="text-color">
          {{ $t('deedCharacteristicsPowerMinting') }}: {{ cardMintingPower }}
        </v-list-item-subtitle>
      </v-list-item>
      <v-list-item class="min-height-auto" dense>
        <v-list-item-subtitle class="text-color">
          {{ $t('deedCharacteristicsCityVotingRights') }}: {{ cityVotingRights }}
        </v-list-item-subtitle>
      </v-list-item>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    offer: {
      type: Object,
      default: null,
    },
    imageSize: {
      type: String,
      default: null,
    },
    maxImageSize: {
      type: String,
      default: null,
    },
  },
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    nftId() {
      return this.offer?.nftId;
    },
    cardType() {
      return this.offer?.cardType?.toUpperCase() || '';
    },
    city() {
      return this.offer?.city?.toUpperCase() || '';
    },
    cityVotingRights() {
      switch (this.cardType) {
      case 'COMMON': return 1;
      case 'UNCOMMON': return 10;
      case 'RARE': return 100;
      case 'LEGENDARY': return 1000;
      default: return '';
      }
    },
    cardImage() {
      return this.city && this.cardType && `/${this.parentLocation}/static/images/nft/${this.city.toLowerCase()}-${this.cardType.toLowerCase()}.png`;
    },
    cardMaxUsers() {
      switch (this.cardType) {
      case 'COMMON': return 100;
      case 'UNCOMMON': return 1000;
      case 'RARE': return 10000;
      case 'LEGENDARY': return this.$t('unlimited');
      default: return '';
      }
    },
    cardMintingPower() {
      return this.offer?.mintingPower;
    },
  }),
};
</script>