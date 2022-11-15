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
  <div class="mt-4">
    <v-list-item v-if="offer.description" class="px-0 align-start">
      <v-list-item-action-text>
        <v-card
          :width="leftSize"
          :max-width="maxLeftSize"
          class="me-4"
          flat>
          <v-card-text class="px-0 font-weight-bold">
            {{ $t('deedsOfferDescriptionLabel') }}
          </v-card-text>
        </v-card>
      </v-list-item-action-text>
      <v-list-item-content class="py-4 pe-0 ps-4">
        <v-card-text class="text-color pa-0">
          {{ offer.description }}
        </v-card-text>
      </v-list-item-content>
    </v-list-item>
    <v-list-item class="px-0 align-start mt-0 mt-sm-4">
      <v-list-item-action-text class="hidden-xs-only">
        <v-card
          :width="leftSize"
          :max-width="maxLeftSize"
          class="me-4"
          flat>
          <v-card-text class="px-0 font-weight-bold">
            {{ $t('deedsOfferConditionsLabel') }}
          </v-card-text>
        </v-card>
      </v-list-item-action-text>
      <v-list-item-content class="pa-0 py-sm-4 ps-sm-4 pe-sm-0 flex-column align-stretch">
        <v-card flat>
          <v-card-text class="px-0 pt-0 font-weight-bold hidden-sm-and-up">
            {{ $t('deedsOfferConditionsLabel') }}
          </v-card-text>
          <div>
            <v-card-text
              v-html="$t('deedsOfferRentingDurationConditionText', {0: `<span class='primary--text font-weight-bold'>${rentalDurationLabel}</span>`})"
              class="pa-0 d-block hidden-xs-only" />
          </div>
          <v-row class="mx-0 mb-0 mt-0 mt-sm-4">
            <v-col cols="auto" class="ps-0 hidden-xs-only">
              <v-card-text class="text-color pa-0 d-block">
                {{ $t('deedRentingPeriodicRentPrice') }}
              </v-card-text>
              <v-card-text class="text-color pa-0 d-block mt-4">
                {{ $t('deedRentingRewardDistribution') }}
              </v-card-text>
            </v-col>
            <v-col class="px-0 ps-sm-8 pe-sm-0 pt-0 pt-sm-3">
              <v-list-item class="min-height-auto px-0 align-start mt-8 mt-sm-0">
                <v-list-item-action-text class="hidden-sm-and-up">
                  <v-card
                    :width="leftSize"
                    :max-width="maxLeftSize"
                    class="me-4"
                    flat>
                    <v-card-text class="pa-0 font-weight-bold">
                      {{ $t('deedRentingPeriodicRentPrice') }}
                    </v-card-text>
                  </v-card>
                </v-list-item-action-text>
                <div class="d-flex py-0 px-4 px-sm-0">
                  <deeds-number-format
                    :value="tokenAmount"
                    :fractions="2"
                    no-decimals>
                    <span v-text="$t('meedsSymbol')" class="secondary--text font-weight-bold"></span>
                  </deeds-number-format>
                  <span class="ms-1 text-lowercase">{{ rentPeriodicityLabel }}</span>
                </div>
              </v-list-item>
              <v-list-item class="min-height-auto px-0 align-start mt-4 mt-sm-0">
                <v-list-item-action-text class="hidden-sm-and-up">
                  <v-card
                    :width="leftSize"
                    :max-width="maxLeftSize"
                    class="me-4"
                    flat>
                    <v-card-text class="font-weight-bold pa-0 hidden-sm-and-up">
                      {{ $t('deedRentingRewardDistribution') }}
                    </v-card-text>
                  </v-card>
                </v-list-item-action-text>
                <div class="d-flex flex-column flex-grow-1 mt-0 mt-sm-5 me-0 me-sm-4 px-4 px-sm-0">
                  <div class="d-flex">
                    <div class="flex-grow-1">
                      <div class="error--text mb-n1">{{ offer.ownerMintingPercentage }}%</div>
                      <v-label class="my-1">
                        <div class="caption my-1">
                          {{ $t('deedRentingRewardDistributionForTheLender') }}
                        </div>
                      </v-label>
                    </div>
                    <div class="flex-grow-1">
                      <div class="green--text mb-n1">{{ (100 - offer.ownerMintingPercentage) }}%</div>
                      <v-label>
                        <div class="caption my-1">
                          {{ $t('deedRentingRewardDistributionForTheTenant') }}
                        </div>
                      </v-label>
                    </div>
                  </div>
                  <div class="me-n4">
                    <v-progress-linear
                      :value="offer.ownerMintingPercentage"
                      color="error"
                      background-color="success"
                      height="12"
                      rounded />
                  </div>
                </div>
              </v-list-item>
            </v-col>
          </v-row>
        </v-card>
      </v-list-item-content>
    </v-list-item>
  </div>
</template>
<script>
export default {
  props: {
    offer: {
      type: Object,
      default: null,
    },
    leftSize: {
      type: String,
      default: null,
    },
    maxLeftSize: {
      type: String,
      default: null,
    },
  },
  computed: Vuex.mapState({
    cardType() {
      return this.offer?.cardType?.toUpperCase() || '';
    },
    city() {
      return this.offer?.city?.toUpperCase() || '';
    },
    rentalDuration() {
      return this.offer?.duration;
    },
    rentalDurationLabelKey() {
      switch (this.rentalDuration) {
      case 'ONE_MONTH': return 'deedRentingDurationOneMonth';
      case 'THREE_MONTHS': return 'deedRentingDurationThreeMonths';
      case 'SIX_MONTHS': return 'deedRentingDurationSixMonths';
      case 'ONE_YEAR': return 'deedRentingDurationOneYear';
      default: return '';
      }
    },
    rentalDurationLabel() {
      return this.$t(this.rentalDurationLabelKey).toLowerCase();
    },
    paymentPeriodicity() {
      return this.offer?.paymentPeriodicity || '';
    },
    tokenAmount() {
      return this.offer?.amount || 0;
    },
    rentPeriodicityLabel() {
      switch (this.paymentPeriodicity){
      case 'ONE_MONTH': return this.$t('deedRentingDurationPerMonth');
      case 'ONE_YEAR': return this.$t('deedRentingDurationPerYear');
      default: return '';
      }
    },
  }),
};
</script>