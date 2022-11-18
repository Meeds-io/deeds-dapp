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
    :permanent="sending">
    <template #title>
      <h4 class="text-capitalize">{{ $t('deedGetOfferRentingTitle', {0: cardType, 1: nftId}) }}</h4>
    </template>
    <template v-if="offer" #content>
      <v-card-text>
        <div class="mb-2">
          {{ $t('deedRentOfferSummary') }}
        </div>
        <div class="font-weight-bold my-3">
          {{ $t('deedRentOfferConditions') }}
        </div>
        <div class="d-flex my-2">
          <div class="flex-grow-1">{{ $t('deedRentingDurationTitle') }}</div>
          <div>{{ rentalDurationLabel }}</div>
        </div>
        <div class="d-flex mt-2">
          <div class="flex-grow-1">{{ $t('deedRentingPeriodicRentPrice') }}</div>
          <div class="d-flex">
            <deeds-number-format
              :value="offer.amount"
              :fractions="2"
              no-decimals>
              <span v-text="$t('meedsSymbol')" class="secondary--text font-weight-bold"></span>
            </deeds-number-format>
            <span class="ms-1 text-lowercase">{{ rentPeriodicityLabel }}</span>
          </div>
        </div>
        <div class="caption font-italic mb-4">
          {{ $t('deedRentingPeriodicRentPriceSummarySubtitle', {0: paymentPeriodicityLabel}) }}
        </div>
        <div class="d-flex mt-2">
          <div class="flex-grow-1">{{ $t('deedRentingRewardDistribution') }}</div>
          {{ $t('deedTenantMintingPercentage', {0: rewardTenantMintingPercentage}) }}
        </div>
        <div class="caption font-italic mb-4">
          {{ $t('deedRentingRewardDistributionSummarySubtitle') }}
        </div>
        <template v-if="hasSecurityDepositPeriod">
          <div class="d-flex mt-2">
            <div class="flex-grow-1">{{ $t('deedRentingSecurityDepositPeriodTitle') }}</div>
            {{ securityDepositPeriodLabel }}
          </div>
          <div class="caption font-italic mb-4">
            {{ $t('deedRentingSecurityDepositPeriodSummarySubtitle') }}
          </div>
        </template>
        <div class="d-flex mt-2">
          <div class="flex-grow-1">{{ $t('deedRentingEndDateTitle') }}</div>
          <deeds-date-format :value="rentalEndDate" />
        </div>
        <div v-if="hasNoticePeriod" class="caption font-italic mb-4">
          {{ $t('deedRentingEndDateSummarySubtitle', {0: noticePeriodLabel}) }}
        </div>
        <template v-if="authenticated">
          <div class="font-weight-bold mt-6 mb-3">
            {{ $t('deedRentOfferAgreement') }}
          </div>
          <v-checkbox
            v-model="agreeCondition1"
            class="mx-0 my-3 pa-0"
            hide-details
            dense>
            <template #label>
              <span class="text--color subtitle-2 font-weight-normal">
                {{ $t('deedRentOfferAgreementCondition1') }}
              </span>
            </template>
          </v-checkbox>
          <v-checkbox
            v-model="agreeCondition2"
            class="mx-0 my-3 pa-0"
            hide-details
            dense>
            <template #label>
              <span class="text--color subtitle-2 font-weight-normal">
                {{ $t('deedRentOfferAgreementCondition2') }}
              </span>
            </template>
          </v-checkbox>
          <div class="font-weight-bold d-flex mt-6">
            {{ $t('deedEmailConfirmTitle') }}
          </div>
          <div class="caption font-italic my-2">
            {{ $t('deedEmailConfirmSubTitle') }}
          </div>
          <deeds-email-field
            ref="email"
            v-model="email"
            :placeholder="$t('deedEmailContactPlaceholder')"
            :readonly="sending"
            :disabled="disabledEmail"
            @valid-email="validEmail = $event"
            @email-confirmation-success="emailCode = $event"
            @email-confirmation-error="emailCodeError = true"
            @loading="sending = $event"
            @submit="confirmOffer" />
        </template>
      </v-card-text>
    </template>
    <template #footer>
      <v-btn
        v-if="authenticated"
        :loading="sending"
        :disabled="disabledButton"
        :min-width="MIN_BUTTONS_WIDTH"
        :class="disabledButton && 'primary'"
        name="rentConfirmButton"
        color="primary"
        depressed
        dark
        @click="confirmRenting">
        {{ buttonLabel }}
      </v-btn>
      <deeds-login-button
        v-else
        :login-tooltip="$t('authenticateToEditOfferTooltip')" />
    </template>
  </deeds-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    sending: false,
    email: null,
    emailCode: null,
    emailCodeSent: false,
    emailCodeError: false,
    validEmail: false,
    agreeCondition1: false,
    agreeCondition2: false,
    offer: null,
    MIN_BUTTONS_WIDTH: 120,
  }),
  computed: Vuex.mapState({
    authenticated: state => state.authenticated,
    nftId() {
      return this.offer?.nftId;
    },
    cardType() {
      return this.offer?.cardType;
    },
    periods() {
      return this.offer.duration?.includes('YEAR') && [{
        value: 'ONE_MONTH',
        text: this.$t('deedRentingDurationPerMonth'),
      }, {
        value: 'ONE_YEAR',
        text: this.$t('deedRentingDurationPerYear'),
      }] || [{
        value: 'ONE_MONTH',
        text: this.$t('deedRentingDurationPerMonth'),
      }];
    },
    paymentPeriodicityLabel() {
      if (!this.offer?.paymentPeriodicity) {
        return null;
      }
      switch (this.offer?.paymentPeriodicity) {
      case 'ONE_MONTH': return this.$t('month').toLowerCase();
      case 'ONE_YEAR': return this.$t('year').toLowerCase();
      }
      return null;
    },
    rentPeriodicityLabel() {
      if (!this.offer?.paymentPeriodicity) {
        return null;
      }
      switch (this.offer?.paymentPeriodicity){
      case 'ONE_MONTH': return this.$t('deedRentingDurationPerMonth');
      case 'ONE_YEAR': return this.$t('deedRentingDurationPerYear');
      }
      return null;
    },
    rentalDurationLabel() {
      if (!this.offer?.duration) {
        return null;
      }
      switch (this.offer?.duration) {
      case 'ONE_MONTH': return this.$t('deedRentingDurationOneMonth');
      case 'THREE_MONTHS': return this.$t('deedRentingDurationThreeMonths');
      case 'SIX_MONTHS': return this.$t('deedRentingDurationSixMonths');
      case 'ONE_YEAR': return this.$t('deedRentingDurationOneYear');
      }
      return null;
    },
    hasSecurityDepositPeriod() {
      return this.offer?.securityDepositPeriod && this.offer?.securityDepositPeriod !== 'NO_PERIOD';
    },
    securityDepositPeriodLabel() {
      if (!this.offer?.securityDepositPeriod) {
        return null;
      }
      switch (this.offer?.securityDepositPeriod) {
      case 'ONE_MONTH': return this.$t('deedRentingDurationOneMonth');
      case 'TWO_MONTHS': return this.$t('deedRentingDurationTwoMonths');
      case 'THREE_MONTHS': return this.$t('deedRentingDurationThreeMonths');
      }
      return null;
    },
    hasNoticePeriod() {
      return this.offer?.noticePeriod && this.offer?.noticePeriod !== 'NO_PERIOD';
    },
    noticePeriodLabel() {
      if (!this.offer?.noticePeriod) {
        return null;
      }
      switch (this.offer?.noticePeriod) {
      case 'ONE_MONTH': return this.$t('deedRentingDurationOneMonth');
      case 'TWO_MONTHS': return this.$t('deedRentingDurationTwoMonths');
      case 'THREE_MONTHS': return this.$t('deedRentingDurationThreeMonths');
      }
      return null;
    },
    rentalOwnerMintingPercentage() {
      return this.offer?.ownerMintingPercentage || 0;
    },
    rewardTenantMintingPercentage() {
      return 100 - this.rentalOwnerMintingPercentage;
    },
    rentalDuration() {
      return this.offer.duration;
    },
    rentalDurationInMillis() {
      if (!this.rentalDuration) {
        return 0;
      }
      switch (this.rentalDuration) {
      case 'ONE_MONTH': return 30 * 24 * 60 * 60 * 1000;
      case 'THREE_MONTHS': return 91.25 * 24 * 60 * 60 * 1000;
      case 'SIX_MONTHS': return 182.5 * 24 * 60 * 60 * 1000;
      case 'ONE_YEAR': return 365 * 24 * 60 * 60 * 1000;
      }
      return 0;
    },
    rentalEndDate() {
      return this.rentalDurationInMillis && (Date.now() + this.rentalDurationInMillis);
    },
    disabledEmail() {
      return !this.agreeCondition1 || !this.agreeCondition2;
    },
    disabledButton() {
      return !this.agreeCondition1 || !this.agreeCondition2;
    },
    buttonLabel() {
      return (!this.emailCode && (this.emailCodeSent && this.$t('resend') || this.$t('send')))
        || this.$t('deedsOfferRentingButton');
    },
  }),
  watch: {
    sending() {
      if (this.sending) {
        this.$refs.drawer?.startLoading();
      } else {
        this.$refs.drawer?.endLoading();
      }
    },
  },
  created() {
    this.$root.$on('deeds-rent-offer-drawer', this.open);
    this.$root.$on('deeds-rent-offer-close', this.close);
  },
  beforeDestroy() {
    this.$root.$off('deeds-rent-offer-drawer', this.open);
    this.$root.$off('deeds-rent-offer-close', this.close);
  },
  methods: {
    open(offer) {
      if (!offer) {
        return;
      }
      this.offer = Object.assign({}, offer);
      this.agreeCondition1 = false;
      this.agreeCondition2 = false;
      this.email = null;
      this.emailCode = null;
      this.emailCodeSent = false;
      this.validEmail = false;
      this.$refs.drawer?.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    confirmRenting() {
      if (!this.emailCode) {
        this.$refs.email?.sendConfirmation();
        this.emailCodeSent = true;
      } else {
        this.sending = true;
        return this.$deedTenantOfferService.rentOffer(this.offer.id, this.offer)
          .then(offer => {
            this.$root.$emit('deed-offer-rented', offer);
            this.$root.$emit('alert-message', this.$t('deedOfferRentingSuccess'), 'success');
            this.sending = false;// Make sending = false to delete permanent behavior of drawer before closing it
            return this.$nextTick();
          })
          .then(() => this.close())
          .catch(() => {
            this.sending = false;
            this.$root.$emit('alert-message', this.$t('deedOfferRentingError'), 'error');
          });
      }
    },
  },
};
</script>