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
  <v-card>
    <v-card-title>
      <v-list-item-avatar>
        <v-img :src="cardImage" />
      </v-list-item-avatar>
      {{ $t('deedRentalNftTypeTitle', {0: cardTypeName, 1: nftId}) }}
    </v-card-title>
    <v-card-text>
      <v-list dense>
        <v-list-item class="pa-0">
          <v-list-item-content class="py-0">
            {{ $t('deedRentingRewardDistribution') }}
          </v-list-item-content>
          <v-list-item-content class="py-0">
            <v-list-item-subtitle class="d-flex">
              <v-icon color="secondary" size="16">fas fa-bolt-lightning</v-icon>
              <div class="ms-1">{{ rentalTenantMintingPercentage }}%</div>
            </v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
        <v-list-item class="pa-0">
          <v-list-item-content class="py-0">
            {{ $t('deedRentingDurationTitle') }}
          </v-list-item-content>
          <v-list-item-content class="py-0">
            {{ rentalDurationLabel }}
          </v-list-item-content>
        </v-list-item>
        <v-list-item class="pa-0">
          <v-list-item-content class="py-0">
            <v-list-item-subtitle class="d-flex">
              <v-icon color="black" size="16">fas fa-stopwatch</v-icon>
              <deeds-timer
                v-if="expirationDate"
                :end-time="expirationDate"
                text-color=""
                short-format />
              <span v-else class="ms-2">{{ $t('deedRentalNeverExpires') }}</span>
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-content class="py-0">
            <deeds-number-format
              :value="tokenAmount"
              :fractions="2"
              no-decimals>
              <span v-text="$t('meedsSymbol')" class="secondary--text font-weight-bold"></span>
            </deeds-number-format>
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </v-card-text>
  </v-card>
</template>
<script>
export default {
  props: {
    offer: {
      type: Object,
      default: null,
    },
    nft: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    originalOffer: null,
  }),
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    nftId() {
      return this.nft?.id;
    },
    cardTypeName() {
      return this.nft?.cardTypeName || '';
    },
    cityName() {
      return this.nft?.cityName || '';
    },
    cardImage() {
      return `/${this.parentLocation}/static/images/nft/${this.cityName.toLowerCase()}-${this.cardTypeName.toLowerCase()}.png`;
    },
    rentalTenantMintingPercentage() {
      return 100 - this.originalOffer?.ownerMintingPercentage || 0;
    },
    rentalDuration() {
      return this.originalOffer?.duration;
    },
    expirationDate() {
      return this.originalOffer?.expirationDate && new Date(this.originalOffer.expirationDate).getTime() || 0;
    },
    tokenAmount() {
      return this.originalOffer?.amount || 0;
    },
    rentalDurationLabelKey() {
      switch (this.rentalDuration){
      case 'ONE_MONTH': return 'deedRentingDurationOneMonth';
      case 'THREE_MONTHS': return 'deedRentingDurationThreeMonths';
      case 'SIX_MONTHS': return 'deedRentingDurationSixMonths';
      case 'ONE_YEAR': return 'deedRentingDurationOneYear';
      default: return '';
      }
    },
    rentalDurationLabel() {
      return this.$t(this.rentalDurationLabelKey);
    },
  }),
  created() {
    this.originalOffer = Object.assign({}, this.offer);
  },
};
</script>