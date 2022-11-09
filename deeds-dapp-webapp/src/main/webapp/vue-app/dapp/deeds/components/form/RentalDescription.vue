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
  <v-card
    class="d-flex flex-column"
    elevation="0"
    outlined>
    <div class="d-flex">
      <v-list-item-avatar
        class="mx-2 mt-3 mb-auto"
        height="70"
        width="70">
        <v-img :src="cardImage" />
      </v-list-item-avatar>
      <v-card class="flex-grow-1" flat>
        <v-card-title class="px-0 py-2">
          {{ $t('deedRentalNftTypeTitle', {0: cardTypeName, 1: nftId}) }}
        </v-card-title>
        <v-card-text>
          <v-list dense>
            <v-list-item class="pa-0 my-n3">
              <v-list-item-content class="py-0">
                {{ $t('cityName', {0: cityName}) }}
              </v-list-item-content>
              <v-list-item-action-text class="d-flex py-0">
                {{ maxUsersLabel }}
              </v-list-item-action-text>
            </v-list-item>
            <v-list-item class="pa-0 my-n3">
              <v-list-item-content class="py-0">
                {{ $t('deedMintingPower') }}
              </v-list-item-content>
              <v-list-item-action-text class="d-flex py-0">
                <v-progress-circular
                  :title="$t('deedMintingPowerDetails', {0: rentalTenantMintingPower})"
                  :rotate="-90"
                  :size="30"
                  :width="5"
                  :value="rentalTenantMintingPowerPercentage"
                  color="primary">
                  <small>{{ rentalTenantMintingPower }}</small>
                </v-progress-circular>
              </v-list-item-action-text>
            </v-list-item>
            <v-list-item class="pa-0 my-n3">
              <v-list-item-content class="py-0">
                {{ $t('deedRentingDurationTitle') }}
              </v-list-item-content>
              <v-list-item-action-text class="py-0">
                {{ rentalDurationLabel }}
              </v-list-item-action-text>
            </v-list-item>
            <v-list-item class="pa-0 my-n3">
              <v-list-item-content class="py-0">
                {{ $t('deedRentingRewardDistribution') }}
              </v-list-item-content>
              <v-list-item-action-text class="d-flex py-0">
                <div class="ms-1">{{ rentalTenantMintingPercentage }}%</div>
              </v-list-item-action-text>
            </v-list-item>
            <v-list-item class="pa-0 my-n3">
              <v-list-item-content class="py-0">
                {{ $t('deedRentingPeriodicAmount') }}
              </v-list-item-content>
              <v-list-item-action-text class="py-0">
                <deeds-number-format
                  :value="tokenAmount"
                  :fractions="2"
                  no-decimals>
                  <span v-text="$t('meedsSymbol')" class="secondary--text font-weight-bold"></span>
                </deeds-number-format>
              </v-list-item-action-text>
            </v-list-item>
          </v-list>
        </v-card-text>
      </v-card>
    </div>
    <v-card-text class="ps-2 pt-0">
      <v-list-item class="pa-0 my-n3">
        <v-list-item-content class="py-0">
          <v-list-item-subtitle class="d-flex">
            <v-chip
              class="v-chip--active me-2"
              color="secondary"
              text-color="secondary"
              label
              outlined>
              {{ $t('rentalsTag') }}
            </v-chip>
            <v-chip
              class="me-2 pa-2"
              label
              outlined>
              <v-list-item-avatar
                class="me-2"
                height="35"
                width="35">
                <v-img :src="cardImage" />
              </v-list-item-avatar>
              {{ cardTypeName }}
            </v-chip>
          </v-list-item-subtitle>
        </v-list-item-content>
        <v-list-item-action-text class="d-flex py-0">
          <v-icon color="black" size="16">fas fa-stopwatch</v-icon>
          <deeds-timer
            v-if="expirationDate"
            :end-time="expirationDate"
            text-color=""
            short-format />
          <span v-else class="ms-2">{{ $t('deedRentalNeverExpires') }}</span>
        </v-list-item-action-text>
      </v-list-item>
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
    cardType() {
      return this.nft?.cardType;
    },
    cardTypeName() {
      return this.nft?.cardTypeName || '';
    },
    cityName() {
      return this.nft?.cityName || '';
    },
    maxUsersLabel() {
      switch (this.cardType){
      case 0: return this.$t('cardTypeMaxUsers', {0: 100});
      case 1: return this.$t('cardTypeMaxUsers', {0: 1000});
      case 2: return this.$t('cardTypeMaxUsers', {0: 10000});
      case 3: return this.$t('unlimited');
      default: return '';
      }
    },
    cardImage() {
      return `/${this.parentLocation}/static/images/nft/${this.cityName.toLowerCase()}-${this.cardTypeName.toLowerCase()}.png`;
    },
    rentalTenantMintingPower() {
      return this.originalOffer?.mintingPower;
    },
    rentalTenantMintingPowerPercentage() {
      return (this.rentalTenantMintingPower * 100 / 2);
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