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
  <deeds-drawer
    ref="drawer"
    v-model="drawer"
    :permanent="sending"
    second-level
    @opened="$emit('opened')"
    @closed="$emit('closed')">
    <template #title>
      <h4>{{ $t('deedRentingTitle', {0: cardTypeName, 1: nftId}) }}</h4>
    </template>
    <template v-if="offer" #content>
      <v-card-text>
        {{ $t('deedRentingDescription1') }}
        <ul>
          <li>{{ $t('deedRentingDescriptionBulletPoint1') }}</li>
          <li>{{ $t('deedRentingDescriptionBulletPoint2') }}</li>
          <li>{{ $t('deedRentingDescriptionBulletPoint3') }}</li>
        </ul>
        <div class="pt-4">{{ $t('deedRentingDescription2') }}</div>
      </v-card-text>
      <v-card-text class="d-flex flex-column flex-grow-1 rental-steps">
        <div class="d-flex align-center">
          <v-chip :color="step === 1 && 'secondary' || 'secondary lighten-2'"><span class="font-weight-bold">1</span></v-chip>
          <span class="subtitle-1 ms-4">{{ $t('deedRentingStep1Title') }}</span>
        </div>
        <v-expand-transition>
          <v-card
            v-show="step === 1"
            class="flex-grow-1"
            flat>
            <div class="px-0 py-4">
              {{ $t('deedRentingStep1Description') }}
              <deeds-extended-textarea
                v-model="offer.description"
                :placeholder="$t('deedRentingDescriptionPlaceholder')"
                :max-length="DESCRIPTION_MAX_LENGTH"
                class="mt-1" />
            </div>
          </v-card>
        </v-expand-transition>
        <div class="d-flex align-center mt-4">
          <v-chip :color="step === 2 && 'secondary' || 'secondary lighten-2'"><span class="font-weight-bold">2</span></v-chip>
          <span class="subtitle-1 ms-4">{{ $t('deedRentingStep2Title') }}</span>
        </div>
        <v-expand-transition>
          <v-card
            v-show="step === 2"
            class="flex-grow-1 mb-8"
            flat>
            <div class="mb-2 mt-6">{{ $t('deedRentingDurationTitle') }}:</div>
            <deeds-renting-duration v-model="offer.duration" />
            <div class="mb-2 mt-6">{{ $t('deedRentingExpirationDurationTitle') }}:</div>
            <deeds-renting-expiration-duration v-model="offer.offerExpiration" />
            <div class="mb-2 mt-6">{{ $t('deedRentingRentalOffer') }}:</div>
            <div class="d-flex">
              <v-text-field
                v-model="offer.amount"
                name="rentalAmount"
                color="grey"
                class="mt-0 pt-0 me-2"
                hide-details
                outlined
                dense
                style="max-width: 80px" />
              <label for="rentalAmount" class="my-auto">{{ $t('meeds') }}</label>
              <v-select
                v-model="offer.paymentPeriodicity"
                :items="periods"
                class="ms-4 py-0"
                outlined
                hide-details
                dense />
            </div>
            <div class="mb-2 mt-6">{{ $t('deedRentingRewardDistribution') }}:</div>
            <div class="d-flex flex-column">
              <div class="d-flex">
                <div class="flex-grow-1">
                  <div class="secondary--text mb-n1">{{ offer.mintingPercentage }}%</div>
                  <v-label>
                    <span class="caption">
                      {{ $t('deedRentingRewardDistributionForTheLender') }}
                    </span>
                  </v-label>
                </div>
                <div class="flex-grow-1">
                  <div class="primary--text mb-n1">{{ (100 - offer.mintingPercentage) }}%</div>
                  <v-label>
                    <span class="caption">
                      {{ $t('deedRentingRewardDistributionForTheTenant') }}
                    </span>
                  </v-label>
                </div>
              </div>
              <div class="mx-n2">
                <v-slider
                  v-model="offer.mintingPercentage"
                  color="secondary py-1"
                  track-color="primary py-1"
                  thumb-color="white border-color mt-3px"
                  loader-height="8px"
                  height="8px"
                  min="0"
                  max="100"
                  hide-details
                  dense />
              </div>
            </div>
          </v-card>
        </v-expand-transition>
      </v-card-text>
    </template>
    <template #footer>
      <v-btn
        :min-width="MIN_BUTTONS_WIDTH"
        outlined
        text
        class="ms-auto me-2"
        name="cancelMoveIn"
        @click="cancel">
        <span class="text-capitalize">
          {{ $t('cancel') }}
        </span>
      </v-btn>
      <v-btn
        :disabled="buttonDisabled"
        :min-width="MIN_BUTTONS_WIDTH"
        :outlined="buttonOutlined"
        name="moveInConfirmButton"
        color="primary"
        depressed
        dark
        @click="confirm">
        <span class="text-capitalize">
          {{ buttonLabel }}
        </span>
      </v-btn>
    </template>
  </deeds-drawer>
</template>
<script>
export default {
  data: () => ({
    nft: null,
    drawer: false,
    step: 0,
    offer: null,
    DEFAULT_OFFER: {
      description: null,
      duration: null,
      offerExpiration: null,
      amount: 10,
      paymentPeriodicity: '1M',
      mintingPercentage: 50,
    },
    DESCRIPTION_MAX_LENGTH: 200,
    MIN_BUTTONS_WIDTH: 120,
  }),
  computed: Vuex.mapState({
    nftId() {
      return this.nft?.id;
    },
    cardTypeName() {
      return this.nft?.cardTypeName;
    },
    periods() {
      return [{
        value: '1M',
        text: this.$t('deedRentingDurationPerMonth'),
      }, {
        value: '1Y',
        text: this.$t('deedRentingDurationPerYear'),
      }];
    },
    buttonOutlined() {
      return this.step === 1;
    },
    buttonLabel() {
      return this.step === 1 && this.$t('next') || this.$t('deedRentingSendButton');
    },
    buttonDisabled() {
      return this.step === 2
        && (
          !this.offer?.duration
          || !this.offer?.amount
          || !this.offer?.paymentPeriodicity
          || !this.offer?.mintingPercentage
        );
    },
  }),
  created() {
    this.$root.$on('deeds-rent-drawer', this.open);
    this.$root.$on('deeds-rent-close', this.close);
  },
  methods: {
    open(nft) {
      this.nft = nft;
      if (!this.offer) {
        this.step = 1;
        this.offer = Object.assign({}, this.DEFAULT_OFFER);
      }
      this.$refs.drawer.open();
    },
    cancel() {
      this.close(this.nftId);
      this.$nextTick().then(() => this.offer = null);
    },
    close(nftId) {
      if (nftId === this.nftId) {
        this.$refs.drawer.close();
      }
    },
    confirm() {
      if (this.step === 1) {
        this.step = 2;
        window.setTimeout(() => {
          this.$refs.drawer.$el.querySelector('.rental-steps').scrollIntoView({
            behavior: 'smooth',
            block: 'end',
          });
        }, 200);
      } else {
        // TODO
      }
    },
  },
};
</script>