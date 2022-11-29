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
          <span class="text-capitalize">{{ $t('deedRentalNftTypeTitle', {0: cardType, 1: nftId}) }}</span>
          <v-spacer />
          <v-tooltip
            v-if="isOwner"
            :attach="`#${cardElementId}`"
            z-index="4"
            bottom>
            <template #activator="{on, bind}">
              <v-btn
                color="amber lighten-2"
                icon
                x-small
                v-on="on"
                v-bind="bind">
                <v-icon>fa-crown</v-icon>
              </v-btn>
            </template>
            <span class="text-no-wrap">{{ $t('deedsOfferOwner') }}</span>
          </v-tooltip>
          <v-tooltip
            v-else-if="isRestricted"
            :attach="`#${cardElementId}`"
            z-index="4"
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
            <span class="text-no-wrap">{{ isRestrictedToCurrent && $t('deedsOfferIsRestrictedToCurrent') || $t('deedsOfferIsRestricted') }}</span>
          </v-tooltip>
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
                <v-tooltip z-index="4" bottom>
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
                  <v-tooltip z-index="4" bottom>
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
    expirationTime() {
      return this.originalOffer?.expirationDate && new Date(this.originalOffer.expirationDate).getTime() || 0;
    },
    hasExpired() {
      return this.originalOffer?.expirationDate && this.expirationTime < this.now;
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