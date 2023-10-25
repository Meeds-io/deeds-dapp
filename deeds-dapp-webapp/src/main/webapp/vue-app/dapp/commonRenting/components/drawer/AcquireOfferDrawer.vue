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
    @opened="$emit('opened')"
    @closed="$emit('closed')">
    <template #title>
      <h4>{{ $t('deedGetOfferRentingTitle', {0: cardTypeI18N, 1: nftId}) }}</h4>
    </template>
    <template v-if="offer" #content>
      <v-card-text class="d-flex flex-column flex-grow-1 rental-steps">
        <div class="mb-2">
          {{ $t('deedRentOfferSummary') }}
        </div>
        <v-list-item
          v-if="authenticated"
          class="d-flex align-center mt-4 flex-grow-0 max-height-40px pa-0"
          dense
          @click="step = 1">
          <v-chip :color="step === 1 && 'secondary' || 'secondary lighten-2'">
            <span class="font-weight-bold">1</span>
          </v-chip>
          <span class="subtitle-1 ms-4">{{ $t('deedRentOfferConditions') }}</span>
        </v-list-item>
        <v-expand-transition>
          <v-card
            v-show="step === 1"
            class="flex-grow-1 mb-8"
            color="transparent"
            flat>
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
            <template v-if="hasNoticePeriod">
              <div class="d-flex mt-2">
                <div class="flex-grow-1">{{ $t('deedRentingNoticePeriodTitle') }}</div>
                {{ noticePeriodLabel }}
              </div>
              <div class="caption font-italic mb-4">
                {{ $t('deedRentingNoticePeriodSummarySubtitle') }}
              </div>
            </template>
            <div class="d-flex mt-2">
              <div class="flex-grow-1">{{ $t('deedRentingEndDateTitle') }}</div>
              <deeds-date-format :value="rentalEndDate" />
            </div>
            <div
              v-if="hasNoticePeriod"
              class="caption font-italic mb-4"
              v-html="$t('deedRentingEndDateSummarySubtitle', {0: `<strong>${noticePeriodLabel}</strong>`})"></div>
          </v-card>
        </v-expand-transition>
        <v-list-item
          v-if="authenticated"
          class="d-flex align-center mt-4 flex-grow-0 max-height-40px pa-0"
          dense
          @click="goToStep2">
          <v-chip :color="step === 2 && 'secondary' || 'secondary lighten-2'">
            <span class="font-weight-bold">2</span>
          </v-chip>
          <span class="subtitle-1 ms-4">{{ $t('deedRentOfferAgreement') }}</span>
        </v-list-item>
        <v-expand-transition>
          <v-card
            v-if="authenticated"
            v-show="step === 2"
            class="flex-grow-1 mb-8"
            color="transparent"
            flat>
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
              <template v-if="knownEmail">
                {{ $t('deedKnownEmailConfirmSubTitle') }}
              </template>
              <template v-else>
                {{ $t('deedEmailConfirmSubTitle') }}
              </template>
            </div>
            <deeds-email-field
              ref="email"
              v-model="email"
              :placeholder="$t('deedEmailContactPlaceholder')"
              :readonly="sending"
              :disabled="disabledEmail"
              :code="emailCode"
              @valid-email="validEmail = $event"
              @email-confirmation-success="emailCode = $event"
              @email-confirmation-error="emailCodeError = true"
              @email-found="knownEmail = $event"
              @loading="sending = $event" />
          </v-card>
        </v-expand-transition>
        <v-list-item
          v-if="authenticated"
          class="d-flex align-center mt-4 flex-grow-0 max-height-40px pa-0"
          dense
          @click="goToStep3">
          <v-chip :color="step === 3 && 'secondary' || 'secondary lighten-2'">
            <span class="font-weight-bold">3</span>
          </v-chip>
          <span class="subtitle-1 ms-4">{{ $t('deedRentOfferPayment') }}</span>
        </v-list-item>
        <v-expand-transition>
          <v-card
            v-if="authenticated"
            v-show="step === 3"
            class="flex-grow-1 mb-8"
            color="transparent"
            flat>
            <div class="mt-8">
              {{ $t('deedRentalOfferPaymentSummary') }}
            </div>

            <div class="font-weight-bold mb-3 mt-8">
              {{ $t('deedRentalOfferPaymentTitle') }}
            </div>
            <div class="d-flex mb-2">
              <div class="flex-grow-1">{{ paymentFirstPeriodLabel }}</div>
              <div class="d-flex">
                <deeds-number-format
                  :value="offer.amount"
                  :fractions="2"
                  no-decimals>
                  <span v-text="$t('meedsSymbol')" class="secondary--text font-weight-bold"></span>
                </deeds-number-format>
              </div>
            </div>
            <div v-if="hasNoticePeriod" class="d-flex mb-2">
              <div class="flex-grow-1">
                {{ $t('deedRentingNoticePeriodTitle') }}
                <span class="ms-1"> - {{ noticeMonthsToPayLabel }}</span>
              </div>
              <div class="d-flex">
                <deeds-number-format
                  :value="noticePeriodAmount"
                  :fractions="2"
                  no-decimals>
                  <span v-text="$t('meedsSymbol')" class="secondary--text font-weight-bold"></span>
                </deeds-number-format>
              </div>
            </div>
            <div v-if="maxMonthsToPay > 1" class="mb-2">
              <div>{{ $t('deedRentMonthsToPay') }}</div>
              <div class="caption" v-html="$t('deedRentMonthsToPayInAdvance', {0: '<strong>', 1: maxMonthsToPay, 2: '</strong>'})"></div>
              <div class="d-flex mb-2">
                <div class="flex-grow-1">
                  <v-slider
                    v-model="monthsToPay"
                    :max="maxMonthsToPay"
                    min="1"
                    hide-details />
                </div>
                <div class="d-flex align-center">
                  {{ monthsToPayLabel }}
                </div>
              </div>
            </div>
            <div class="d-flex mb-2">
              <div class="flex-grow-1 font-weight-bold">
                <div>{{ $t('deedRentingPaymentTotal') }}</div>
                <div v-if="hasNoticePeriod" class="caption">
                  (<span>{{ $t('deedMonthRents') }}</span>
                  +
                  <span>{{ $t('deedRentingNoticePeriodTitle') }}</span>)
                </div>
              </div>
              <div class="d-flex">
                <deeds-number-format
                  :value="totalAmountToPay"
                  :fractions="2"
                  no-decimals>
                  <span v-text="$t('meedsSymbol')" class="secondary--text font-weight-bold"></span>
                </deeds-number-format>
              </div>
            </div>

            <div class="font-weight-bold mb-3 mt-8">
              {{ $t('deedRentingPaymentYourBalance') }}
            </div>
            <div class="d-flex mb-2">
              <div class="flex-grow-1">{{ $t('meeds') }}</div>
              <div class="d-flex">
                <deeds-number-format
                  :value="meedsBalance"
                  :fractions="2">
                  <span v-text="$t('meedsSymbol')" class="secondary--text font-weight-bold"></span>
                </deeds-number-format>
              </div>
            </div>
            <div
              v-html="buyMeedProposalLabel"
              class="d-flex mb-2">
            </div>
          </v-card>
        </v-expand-transition>
      </v-card-text>
      <v-card-text
        v-if="sent"
        id="paymentTransactionConfirmation"
        class="mt-2 mt-sm-4">
        <div class="mb-4">
          {{ $t('deedRentOfferConfirmationSuccessPart1') }}
        </div>
        <div>
          {{ $t('deedRentOfferConfirmationSuccessPart2') }}
        </div>
        <ul class="mt-4">
          <li class="ps-0 ps-sm-4">
            {{ $t('deedRentOfferConfirmationSuccessPart3') }}
          </li>
          <li
            v-html="$t('deedRentOfferConfirmationSuccessPart4', {0: `<a href='${tenantsURL}'>`, 1: `</a>`})"
            class="ps-0 ps-sm-4"
            @click.prevent.stop="openTenants">
          </li>
        </ul>
      </v-card-text>
      <deeds-marketplace-offer-payment-drawer
        v-if="!intermediateStep"
        ref="paymentDrawer"
        :offer-id="offerId"
        :months-to-pay="monthsToPay"
        :amount="totalAmountToPayWithDecimals"
        @transaction-sent="confirmPayment" />
    </template>
    <template #footer>
      <template v-if="authenticated">
        <v-btn
          v-if="!sent"
          :disabled="sending"
          name="cancelRentButton"
          class="ms-auto me-2"
          min-width="120"
          outlined
          text
          @click="close">
          {{ $t('cancel') }}
        </v-btn>
        <v-btn
          :loading="sending"
          :disabled="buttonDisabled"
          :min-width="MIN_BUTTONS_WIDTH"
          :outlined="intermediateStep"
          :class="!intermediateStep && buttonDisabled && 'primary'"
          name="rentConfirmButton"
          color="primary"
          depressed
          dark
          @click="confirmRenting">
          {{ buttonLabel }}
        </v-btn>
      </template>
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
    sent: false,
    email: null,
    emailCode: null,
    emailCodeSent: false,
    emailCodeError: false,
    validEmail: false,
    knownEmail: false,
    agreeCondition1: false,
    agreeCondition2: false,
    offer: null,
    step: 1,
    MIN_BUTTONS_WIDTH: 120,
    monthsToPay: 1,
  }),
  computed: Vuex.mapState({
    authenticated: state => state.authenticated,
    meedsBalance: state => state.meedsBalance,
    tenantRentingAddress: state => state.tenantRentingAddress,
    parentLocation: state => state.parentLocation,
    ZERO_BN: state => state.ZERO_BN,
    MONTH_IN_SECONDS: state => state.MONTH_IN_SECONDS,
    tenantsURL: state => state.tenantsURL,
    buyMeedsLink: state => state.buyMeedsLink,
    nftId() {
      return this.offer?.nftId;
    },
    offerId() {
      return this.offer?.offerId;
    },
    cardType() {
      return this.offer?.cardType;
    },
    cardTypeI18N() {
      return this.cardType && this.$t(this.cardType.toLowerCase());
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
    paymentFirstPeriodLabel() {
      if (!this.offer?.paymentPeriodicity) {
        return null;
      }
      if (this.offer?.paymentPeriodicity === this.offer?.duration) {
        return this.$t('deedRentalOfferPaymentPeriodRent', {0: this.paymentPeriodicityLabel});
      } else {
        return this.$t('deedRentingPeriodicRentPriceSummarySubtitle', {0: this.paymentPeriodicityLabel});
      }
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
    rentalDurationMonths() {
      if (!this.offer?.duration) {
        return 1;
      }
      switch (this.offer?.duration) {
      case 'ONE_MONTH': return 1;
      case 'THREE_MONTHS': return 3;
      case 'SIX_MONTHS': return 6;
      case 'ONE_YEAR': return 12;
      }
      return 1;
    },
    maxMonthsToPay() {
      return this.rentalDurationMonths - this.noticeMonths;
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
    monthsToPayLabel() {
      if (this.monthsToPay === 1) {
        return this.$t('deedRentingRemainingTimeOneMonth');
      } else {
        return this.$t('deedRentingRemainingTimeMonths', {0: this.monthsToPay});
      }
    },
    noticeMonthsToPayLabel() {
      if (!this.noticeMonths || this.noticeMonths < 1) {
        return '';
      } else if (this.noticeMonths === 1) {
        return this.$t('deedRentingRemainingTimeOneMonth');
      } else {
        return this.$t('deedRentingRemainingTimeMonths', {0: this.noticeMonths});
      }
    },
    totalMonthsToPay() {
      return this.noticeMonths + this.monthsToPay;
    },
    noticeMonths() {
      if (!this.offer?.noticePeriod) {
        return 0;
      }
      switch (this.offer?.noticePeriod) {
      case 'ONE_MONTH': return 1;
      case 'TWO_MONTHS': return 2;
      case 'THREE_MONTHS': return 3;
      }
      return 0;
    },
    noticePeriodAmount() {
      return this.offer.amount * this.noticeMonths;
    },
    buyMeedProposalLabel() {
      return this.$t('deedRentingPaymentMeedsBuyProposal', {
        0: `<a id="buyMeeds" class="mx-1 primary--text font-weight-bold" href="${this.buyMeedsLink}" target="_blank">`,
        1: this.$t('meeds'),
        2: '</a>',
      });
    },
    totalAmountToPay() {
      return this.totalMonthsToPay * this.offer.amount;
    },
    totalAmountToPayWithDecimals() {
      return this.totalAmountToPay
        && ethers.BigNumber.from(this.totalAmountToPay).mul(ethers.BigNumber.from(10).pow(18))
        || this.ZERO_BN;
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
      case 'ONE_MONTH': return this.MONTH_IN_SECONDS * 1000;
      case 'THREE_MONTHS': return 3 * this.MONTH_IN_SECONDS * 1000;
      case 'SIX_MONTHS': return 6 * this.MONTH_IN_SECONDS * 1000;
      case 'ONE_YEAR': return 12 * this.MONTH_IN_SECONDS * 1000;
      }
      return 0;
    },
    rentalEndDate() {
      return this.rentalDurationInMillis && (Date.now() + this.rentalDurationInMillis);
    },
    disabledEmail() {
      return !this.agreeCondition1 || !this.agreeCondition2;
    },
    confirmEmailStep() {
      return this.step === 2 && !this.emailCode;
    },
    step2ButtonDisabled() {
      return !this.agreeCondition1
        || !this.agreeCondition2
        || !this.validEmail;
    },
    step3ButtonDisabled() {
      return this.step2ButtonDisabled || !this.emailCode;
    },
    buttonDisabled() {
      return (this.step === 2 && this.step2ButtonDisabled)
        || (this.step === 3 && this.step3ButtonDisabled);
    },
    intermediateStep() {
      return this.step < 3;
    },
    buttonLabel() {
      return (this.confirmEmailStep && (this.emailCodeSent && this.$t('resend') || this.$t('send')))
        || (this.intermediateStep && this.$t('next'))
        || (this.sent && this.$t('gotIt'))
        || this.$t('deedsOfferRentingButton');
    },
  }),
  watch: {
    emailCode() {
      if (this.emailCode && this.step === 2) {
        this.confirmRenting();
      }
    },
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
      if (!this.offer || offer.id !== this.offer.id) {
        this.agreeCondition1 = false;
        this.agreeCondition2 = false;
        this.emailCode = null;
        this.emailCodeSent = false;
        this.sent = false;
        this.step = 1;
        this.sending = false;
      }
      this.$refs.email?.resetForm();
      this.offer = Object.assign({}, offer);
      this.$refs.drawer?.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    goToStep2() {
      if (this.step !== 2) {
        this.step = 2;
        this.scrollDrawerContent();
      }
    },
    goToStep3() {
      if (this.step !== 3 && !this.step2ButtonDisabled && this.emailCode) {
        this.step = 3;
        this.scrollDrawerContent();
      }
    },
    confirmRenting() {
      if (this.confirmEmailStep) {
        this.sendEmailConfirmation();
      } else if (this.step === 1) {
        this.goToStep2();
      } else if (this.step === 2) {
        this.goToStep3();
      } else if (this.sent) {
        this.openTenants();
      } else if (this.$refs.paymentDrawer) {
        this.$refs.paymentDrawer.open();
      }
    },
    confirmPayment(transactionHash) {
      this.sending = true;
      return this.$deedTenantLeaseService.createLease(this.offer.id, transactionHash, this.emailCode)
        .then(lease => {
          this.$root.$emit('deed-offer-rented', lease, this.offer);
          this.sending = false;// Make sending = false to delete permanent behavior of drawer before closing it
          this.sent = true;
          return this.$nextTick();
        })
        .then(() => {
          window.setTimeout(() => {
            const element = document.querySelector('#paymentTransactionConfirmation');
            if (element) {
              element.scrollIntoView({
                block: 'end',
              });
            }
          }, 200);
        })
        .catch(() => {
          this.sending = false;
          this.$root.$emit('alert-message', this.$t('deedOfferRentingError'), 'error');
        });
    },
    openTenants(event) {
      if (!event || event?.target?.tagName?.toLowerCase() === 'a') {
        this.$root.$emit('switch-page', 'tenants');
      }
    },
    scrollDrawerContent() {
      window.setTimeout(() => {
        this.$refs.drawer?.$el.querySelector('.rental-steps').scrollIntoView({
          behavior: 'smooth',
          block: 'end',
        });
      }, 200);
    },
    sendEmailConfirmation() {
      this.$refs.email?.sendConfirmation();
      this.emailCodeSent = true;
    },
  },
};
</script>