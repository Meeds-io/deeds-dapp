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
  <v-card
    class="d-flex flex-column"
    elevation="0"
    outlined
    @click="$emit('select')">
    <div class="d-flex">
      <v-list-item-avatar
        class="deed-avatar mx-2 mt-3 mb-auto"
        height="70"
        width="70">
        <v-img :src="cardImage" />
      </v-list-item-avatar>
      <v-card
        :id="cardElementId"
        class="flex-grow-1"
        flat>
        <v-card-title class="d-flex ps-0 py-2">
          <span>{{ $t('deedRentalNftTypeTitle', {0: cardTypeI18N, 1: nftId}) }}</span>
          <v-spacer />
          <v-tooltip
            v-if="isOwner"
            :attach="`#${cardElementId}`"
            z-index="4"
            max-width="300px"
            bottom>
            <template #activator="{on, bind}">
              <v-btn
                color="amber lighten-2"
                icon
                x-small
                v-on="on"
                v-bind="bind">
                <v-icon size="27">fa-crown</v-icon>
              </v-btn>
            </template>
            <span>{{ $t('deedsOfferOwner') }}</span>
          </v-tooltip>
          <v-tooltip
            v-else-if="isRestricted"
            :attach="`#${cardElementId}`"
            z-index="4"
            max-width="300px"
            bottom>
            <template #activator="{on, bind}">
              <v-btn
                :color="isRestrictedToCurrent && 'success' || 'error'"
                icon
                x-small
                v-on="on"
                v-bind="bind">
                <v-icon>{{ isRestrictedToCurrent && 'fas fa-unlock' || 'fas fa-lock' }}</v-icon>
              </v-btn>
            </template>
            <span>{{ isRestrictedToCurrent && $t('deedsOfferIsRestrictedToCurrent') || $t('deedsOfferIsRestricted') }}</span>
          </v-tooltip>
        </v-card-title>
        <v-card-text class="ps-sm-0 pb-6">
          <v-list dense>
            <v-list-item class="pa-0 my-n2">
              <v-list-item-content class="py-0 text-h6 font-weight-normal">
                {{ $t('cityName', {0: city}) }}
              </v-list-item-content>
              <div class="text-h6 font-weight-normal">
                {{ maxUsersLabel }}
              </div>
            </v-list-item>
            <v-list-item class="pa-0 my-n2">
              <v-list-item-content class="py-0 text-h6 font-weight-normal">
                {{ $t('deedRentingDurationTitle') }}
              </v-list-item-content>
              <div class="text-h6 font-weight-normal">
                {{ rentalDurationLabel }}
              </div>
            </v-list-item>
            <v-list-item class="pa-0 my-n2">
              <v-list-item-content class="py-0 text-h6 font-weight-normal">
                {{ $t('deedRentingRewardDistribution') }}
              </v-list-item-content>
              <div class="text-h6 font-weight-normal">
                {{ rentalTenantMintingPercentage }} %
              </div>
            </v-list-item>
          </v-list>
        </v-card-text>
      </v-card>
    </div>
    <v-card-title class="ps-2 pt-7">
      <div class="mb-n2 flex-grow-1 d-flex flex-wrap justify-space-between">
        <div class="mx-auto d-flex align-center">
          <deeds-card-type-chip
            :card="cardType"
            :city="city"
            :selected-cards="selectedCards"
            class="my-2 my-sm-0 ms-0 ms-sm-3"
            avatar-size="30"
            extra-class="rounded-pill px-3 py-1"
            small-sized-text />
        </div>
        <v-spacer class="hidden-sm-and-down" />
        <div class="d-flex flex-row my-2 mx-auto align-center">
          <h4 class="font-weight-normal">
            {{ periodicTokenAmount }}
          </h4>
          <div v-text="$t('meedsSymbol')" class="secondary--text text-h6 font-weight-medium mx-1"></div>
          <h4 class="font-weight-normal">{{ rentPeriodicityLabel }}</h4>
        </div>
        <template v-if="expirationTime">
          <v-list-item-action-text v-if="hasExpired" class="d-flex py-0">
            <span class="error--text">{{ $t('deedsOfferRentingExpired') }}</span>
          </v-list-item-action-text>
          <v-list-item-action-text v-else class="d-flex py-0">
            <v-icon :color="blackThemeColor" size="16">fas fa-stopwatch</v-icon>
            <deeds-timer
              :end-time="expirationTime"
              text-color=""
              short-format />
          </v-list-item-action-text>
        </template>
      </div>
    </v-card-title>
  </v-card>
</template>
<script>
export default {
  props: {
    offer: {
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
    address: state => state.address,
    now: state => state.now,
    blackThemeColor: state => state.blackThemeColor,
    nftId() {
      return this.offer?.nftId;
    },
    cardElementId() {
      return `deedOfferCard-${this.offer.id}`;
    },
    cardType() {
      return this.offer?.cardType?.toUpperCase() || '';
    },
    cardTypeI18N() {
      return this.cardType && this.$t(this.cardType.toLowerCase());
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
      return this.city && this.cardType && `${this.parentLocation}/static/images/nft/${this.city.toLowerCase()}-${this.cardType.toLowerCase()}.png`;
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
    expirationTime() {
      return this.originalOffer?.expirationDate && new Date(this.originalOffer.expirationDate).getTime() || 0;
    },
    hasExpired() {
      return this.originalOffer?.expirationDate && this.expirationTime < this.now;
    },
    tokenAmount() {
      return this.originalOffer?.amount || 0;
    },
    reducedTokenAmount() {
      return this.tokenAmount > 999 ? Math.trunc(this.tokenAmount / 1000) : this.tokenAmount;
    },
    periodicTokenAmount() {
      return this.tokenAmount > 999 ? this.reducedTokenAmount.toString().concat('K') : this.tokenAmount;
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
    isOwner() {
      return this.address && this.offer?.owner?.toLowerCase() === this.address?.toLowerCase();
    },
    isRestricted() {
      return !!this.offer?.hostAddress;
    },
    isRestrictedToCurrent() {
      return this.offer?.hostAddress.toLowerCase() === this.address?.toLowerCase();
    },
  }),
  created() {
    this.originalOffer = Object.assign({}, this.offer);
  },
};
</script>