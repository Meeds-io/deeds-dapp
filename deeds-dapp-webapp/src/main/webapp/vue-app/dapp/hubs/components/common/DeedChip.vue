<!--

 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 
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
  <div>
    <v-tooltip bottom>
      <template #activator="{ on, attrs }">
        <v-chip
          :href="`${openSeaBaseLink}/${deedId}`"
          :small="small"
          :class="small && 'px-1' || 'pe-1'"
          :color="color"
          target="_blank"
          rel="nofollow noreferrer noopener"
          class="overflow-hidden position-relative d-block"
          outlined
          v-bind="attrs"
          v-on="on"
          @click="nop">
          <v-card
            height="100%"
            class="position-static transparent"
            style="aspect-ratio: 0.5"
            flat>
            <v-card
              aspect-ratio="1"
              :max-height="height - 4"
              :height="height - 4"
              class="d-flex overflow-hidden transparent rounded-circle absolute-vertical-center"
              style="left: -1px"
              flat>
              <img
                :src="cardImage"
                :width="height + 4"
                class="ma-n1"
                height="auto"
                alt="Deed">
            </v-card>
          </v-card>
          <div :class="[small && 'caption' || 'body-1', fontColor]" class="font-weight-normal ms-2">
            #{{ deedId }}
          </div>
        </v-chip>
      </template>
      <span>{{ $t('wom.openDeedInOpenSea') }}</span>
    </v-tooltip>
  </div>
</template>
<script>
export default {
  props: {
    deed: {
      type: Object,
      default: null,
    },
    color: {
      type: String,
      default: () => 'white',
    },
    fontColor: {
      type: String,
      default: () => 'white--text',
    },
    small: {
      type: Boolean,
      default: false,
    },
    height: {
      type: Number,
      default: () => 36,
    },
  },
  computed: Vuex.mapState({
    cardTypes: state => state.cardTypes,
    cities: state => state.cities,
    openSeaBaseLink: state => state.openSeaBaseLink,
    parentLocation: state => state.parentLocation,
    deedId() {
      return this.deed?.nftId || this.deed?.deedId;
    },
    cityIndex() {
      return this.deed?.city;
    },
    cardTypeIndex() {
      return this.deed?.type === 0 ? this.deed?.type : this.deed?.type || this.deed?.cardType;
    },
    city() {
      return this.cities[this.cityIndex];
    },
    cardType() {
      return this.cardTypes[this.cardTypeIndex];
    },
    cardImage() {
      return this.city && this.cardType && `${this.parentLocation}/static/images/nft/${this.city.toLowerCase()}-${this.cardType.toLowerCase()}.png`;
    },
  }),
};
</script>