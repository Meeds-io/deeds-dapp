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
        <v-card-title class="px-0 py-2 text-capitalize">
          {{ $t('deedRentalNftTypeTitle', {0: cardType, 1: nftId}) }}
        </v-card-title>
        <v-card-text class="ps-0 pb-6">
          <v-list dense>
            <v-list-item class="pa-0 my-n3">
              <v-list-item-content class="py-0 text-capitalize">
                {{ $t('cityName', {0: city}) }}
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
                <v-tooltip bottom>
                  <template #activator="{ on, attrs }">
                    <v-progress-circular
                      :rotate="-90"
                      :size="30"
                      :width="5"
                      :value="rentalTenantMintingPowerPercentage"
                      color="success"
                      v-bind="attrs"
                      v-on="on">
                      <small class="primary--text">{{ rentalTenantMintingPower }}</small>
                    </v-progress-circular>
                  </template>
                  <span>{{ $t('deedMintingPowerDetails', {0: rentalTenantMintingPower}) }}</span>
                </v-tooltip>
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
                <v-card min-width="50" flat>
                  <v-tooltip bottom>
                    <template #activator="{ on, attrs }">
                      <v-progress-linear
                        :value="rentalTenantMintingPercentage"
                        color="success"
                        background-color="error"
                        height="6"
                        rounded
                        v-bind="attrs"
                        v-on="on" />
                    </template>
                    <span>{{ $t('deedMintingPercentageDetails', {0: rentalTenantMintingPercentage, 1: rentalOwnerMintingPercentage}) }}</span>
                  </v-tooltip>
                </v-card>
              </v-list-item-action-text>
            </v-list-item>
            <v-list-item class="pa-0 my-n3">
              <v-list-item-content class="py-0">
                {{ $t('deedRentingPeriodicRentPrice') }}
              </v-list-item-content>
              <v-list-item-action-text class="py-0 d-flex">
                <deeds-number-format
                  :value="tokenAmount"
                  :fractions="2"
                  no-decimals>
                  <span v-text="$t('meedsSymbol')" class="secondary--text font-weight-bold"></span>
                </deeds-number-format>
                <span class="ms-1 text-lowercase">{{ rentPeriodicityLabel }}</span>
              </v-list-item-action-text>
            </v-list-item>
          </v-list>
        </v-card-text>
      </v-card>
    </div>
    <v-card-text class="ps-2 pt-0">
      <v-list-item class="pa-0 my-n3">
        <v-list-item-content class="py-0">
          <v-list-item-subtitle class="d-flex flex-wrap">
            <deeds-offer-type-chip
              :label="$t('rentalsTag')"
              :selected-offers="selectedOffers"
              class="my-1 my-sm-0 me-2"
              color="secondary"
              offer-type="RENTING"
              active
              small />
            <deeds-card-type-chip
              :card="cardType"
              :city="city"
              :selected-cards="selectedCards"
              class="my-1 my-sm-0 me-2"
              avatar-size="18"
              small />
          </v-list-item-subtitle>
        </v-list-item-content>
        <v-list-item-action-text v-if="expirationDate" class="d-flex py-0">
          <v-icon color="black" size="16">fas fa-stopwatch</v-icon>
          <deeds-timer
            :end-time="expirationDate"
            text-color=""
            short-format />
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
    selectedOffers: {
      type: Array,
      default: null,
    },
    selectedCards: {
      type: Array,
      default: null,
    },
  },
  data: () => ({
    originalOffer: null,
  }),
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
    paymentPeriodicity() {
      return this.offer?.paymentPeriodicity || '';
    },
    maxUsersLabel() {
      switch (this.cardType){
      case 'COMMON': return this.$t('cardTypeMaxUsers', {0: 100});
      case 'UNCOMMON': return this.$t('cardTypeMaxUsers', {0: 1000});
      case 'RARE': return this.$t('cardTypeMaxUsers', {0: 10000});
      case 'LEGENDARY': return this.$t('unlimited');
      default: return '';
      }
    },
    cardImage() {
      return `/${this.parentLocation}/static/images/nft/${this.city.toLowerCase()}-${this.cardType.toLowerCase()}.png`;
    },
    rentalTenantMintingPower() {
      return this.originalOffer?.mintingPower;
    },
    rentalTenantMintingPowerPercentage() {
      return parseInt((this.rentalTenantMintingPower - 1) * 100);
    },
    rentalOwnerMintingPercentage() {
      return this.originalOffer?.ownerMintingPercentage || 0;
    },
    rentalTenantMintingPercentage() {
      return 100 - this.rentalOwnerMintingPercentage;
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
    rentPeriodicityLabel() {
      switch (this.paymentPeriodicity){
      case 'ONE_MONTH': return this.$t('deedRentingDurationPerMonth');
      case 'ONE_YEAR': return this.$t('deedRentingDurationPerYear');
      default: return '';
      }
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