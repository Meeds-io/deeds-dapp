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
    :second-level="secondLevel"
    @opened="$emit('opened')"
    @closed="$emit('closed')">
    <template #title>
      <h4 v-if="isNew" class="text-capitalize">{{ $t('deedRentingTitle', {0: cardType, 1: nftId}) }}</h4>
      <h4 v-else>{{ $t('deedRentingEditTitle') }}</h4>
    </template>
    <template v-if="offer && authenticated" #content>
      <v-card-text v-if="isNew">
        {{ $t('deedRentingDescription1') }}
        <ul>
          <li>{{ $t('deedRentingDescriptionBulletPoint1') }}</li>
          <li>{{ $t('deedRentingDescriptionBulletPoint2') }}</li>
          <li>{{ $t('deedRentingDescriptionBulletPoint3') }}</li>
        </ul>
        <div class="pt-4">{{ $t('deedRentingDescription2') }}</div>
      </v-card-text>
      <v-card-text v-else>
        {{ $t('deedRentingEditDescription1') }}
        <div class="pt-4">{{ $t('deedRentingEditDescription2') }}</div>
        <deeds-renting-offer-card :offer="offer" />
      </v-card-text>
      <v-card-text class="d-flex flex-column flex-grow-1 rental-steps">
        <v-list-item
          class="d-flex align-center flex-grow-0 max-height-40px pa-0"
          dense
          @click="step = 1">
          <v-chip :color="step === 1 && 'secondary' || 'secondary lighten-2'"><span class="font-weight-bold">1</span></v-chip>
          <span class="subtitle-1 ms-4">{{ $t('deedRentingStep1Title') }}</span>
        </v-list-item>
        <v-expand-transition>
          <v-card
            v-show="step === 1"
            color="transparent"
            class="flex-grow-1"
            flat>
            <div class="px-0 pt-4">
              {{ $t('deedRentingStep1Description') }}
              <deeds-extended-textarea
                v-model="offer.description"
                :placeholder="$t('deedRentingDescriptionPlaceholder')"
                :max-length="DESCRIPTION_MAX_LENGTH"
                class="mt-1" />
            </div>
          </v-card>
        </v-expand-transition>
        <v-list-item
          class="d-flex align-center mt-4 flex-grow-0 max-height-40px pa-0"
          dense
          @click="goToStep2">
          <v-chip :color="step === 2 && 'secondary' || 'secondary lighten-2'"><span class="font-weight-bold">2</span></v-chip>
          <span class="subtitle-1 ms-4">{{ $t('deedRentingStep2Title') }}</span>
        </v-list-item>
        <v-expand-transition>
          <v-card
            v-show="step === 2"
            color="transparent"
            class="flex-grow-1 mb-8"
            flat>
            <div class="mb-2 mt-6">{{ $t('deedRentingDurationTitle') }}:</div>
            <deeds-renting-duration v-model="offer.duration" />
            <div class="mb-2 mt-6">{{ $t('deedRentingExpirationDurationTitle') }}:</div>
            <deeds-renting-expiration-duration v-model="offer.expirationDuration" />
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
                  <div class="error--text mb-n1">{{ offer.ownerMintingPercentage }}%</div>
                  <v-label>
                    <span class="caption">
                      {{ $t('deedRentingRewardDistributionForTheLender') }}
                    </span>
                  </v-label>
                </div>
                <div class="flex-grow-1">
                  <div class="green--text mb-n1">{{ (100 - offer.ownerMintingPercentage) }}%</div>
                  <v-label>
                    <span class="caption">
                      {{ $t('deedRentingRewardDistributionForTheTenant') }}
                    </span>
                  </v-label>
                </div>
              </div>
              <div class="mx-n2">
                <v-slider
                  v-model="offer.ownerMintingPercentage"
                  :thumb-color="`${whiteThemeColor} border-color mt-3px`"
                  color="error py-1"
                  track-color="success py-1"
                  loader-height="8px"
                  height="8px"
                  min="0"
                  max="100"
                  hide-details
                  dense />
              </div>
            </div>
            <div class="mt-6">{{ $t('deedRentingSecurityDepositPeriodTitle') }}:</div>
            <div class="mb-2 caption text--disabled">{{ $t('deedRentingSecurityDepositPeriodSubtitle') }}:</div>
            <deeds-security-deposit-period
              v-model="offer.securityDepositPeriod"
              :max-value="offer.duration"
              max-value-exclusive />
            <div class="mt-6">{{ $t('deedRentingNoticePeriodTitle') }}:</div>
            <div class="mb-2 caption text--disabled">{{ $t('deedRentingNoticePeriodSubtitle') }}:</div>
            <deeds-notice-period
              v-model="offer.noticePeriod"
              :max-value="offer.duration"
              max-value-exclusive />
          </v-card>
        </v-expand-transition>
        <v-list-item
          class="d-flex align-center mt-4 flex-grow-0 max-height-40px pa-0"
          dense
          @click="goToStep3">
          <v-chip :color="step === 3 && 'secondary' || 'secondary lighten-2'"><span class="font-weight-bold">3</span></v-chip>
          <span class="subtitle-1 ms-4">
            {{ isNew && $t('deedRentingStep3TitleCreation') || $t('deedRentingStep3TitleUpdate') }}
          </span>
        </v-list-item>
        <v-expand-transition>
          <v-card
            v-show="step === 3"
            color="transparent"
            class="flex-grow-1 mb-8"
            flat>
            <div class="font-weight-bold my-3">
              {{ $t('deedRentalOfferSummary') }}
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
            <template v-if="hasNoticePeriod">
              <div class="d-flex mt-2">
                <div class="flex-grow-1">{{ $t('deedRentingNoticePeriodTitle') }}</div>
                {{ noticePeriodLabel }}
              </div>
              <div class="caption font-italic mb-4">
                {{ $t('deedRentingNoticePeriodSummarySubtitle') }}
              </div>
            </template>
            <template v-if="drawer">
              <template v-if="isNew">
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
              <div v-else-if="expirationDuration" class="d-flex align-center">
                <div class="flex-grow-1">
                  <v-switch
                    v-model="offer.updateExpirationDate"
                    :disabled="forceUpdateExpiration"
                    class="ma-0 pa-0"
                    hide-details
                    dense>
                    <template #label>
                      <span class="text--color subtitle-2 font-weight-normal">
                        {{ expirationDateChoiceLabel }}
                      </span>
                    </template>
                  </v-switch>
                </div>
                <div class="flex-grow-0 pt-2px">
                  <deeds-date-format :value="expirationDate" />
                </div>
              </div>
            </template>
          </v-card>
        </v-expand-transition>
      </v-card-text>
      <deeds-confirm-dialog
        ref="confirmDialog"
        :title="$t('deedRentalDeleteOfferConfirmTitle')"
        :message="$t('deedRentalDeleteOfferConfirmDescription')"
        :ok-label="$t('confirm')"
        :cancel-label="$t('cancel')"
        @ok="deleteOffer(true)" />
    </template>
    <template v-if="authenticated" #footer>
      <v-btn
        v-if="isNew"
        :disabled="sending"
        :min-width="MIN_BUTTONS_WIDTH"
        outlined
        text
        class="me-2 ms-auto"
        name="cancelRent"
        @click="cancel">
        {{ $t('cancel') }}
      </v-btn>
      <v-btn
        v-else
        :loading="deleting"
        :min-width="MIN_BUTTONS_WIDTH"
        outlined
        name="deleteConfirmButton"
        color="error"
        class="ms-auto me-2"
        depressed
        dark
        @click="deleteOffer(false)">
        {{ $t('deedRentingDeleteButton') }}
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
        @click="confirmOffer">
        {{ buttonLabel }}
      </v-btn>
    </template>
    <template v-else #footer>
      <deeds-login-button
        :login-tooltip="$t('authenticateToEditOfferTooltip')" />
    </template>
  </deeds-drawer>
</template>
<script>
export default {
  props: {
    secondLevel: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    drawer: false,
    sending: false,
    deleting: false,
    isNew: false,
    step: 0,
    offer: null,
    offerChanged: false,
    email: null,
    emailCode: null,
    emailCodeSent: false,
    emailCodeError: false,
    validEmail: false,
    forceUpdateExpiration: false,
    DEFAULT_OFFER: {
      description: null,
      duration: null,
      expirationDuration: null,
      amount: 10,
      paymentPeriodicity: 'ONE_MONTH',
      noticePeriod: 'ONE_MONTH',
      securityDepositPeriod: 'ONE_MONTH',
      ownerMintingPercentage: 50,
      updateExpirationDate: false,
    },
    DESCRIPTION_MAX_LENGTH: 200,
    MIN_BUTTONS_WIDTH: 120,
  }),
  computed: Vuex.mapState({
    authenticated: state => state.authenticated,
    whiteThemeColor: state => state.whiteThemeColor,
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
    intermediateStep() {
      return this.step < 3;
    },
    buttonLabel() {
      return (this.intermediateStep && this.$t('next'))
        || (this.confirmEmailStep && (this.emailCodeSent && this.$t('resend') || this.$t('send')))
        || (this.isNew && this.$t('deedRentingCreateButton') || this.$t('deedRentingUpdateButton'));
    },
    step1ButtonDisabled() {
      return this.offer?.description?.length > this.DESCRIPTION_MAX_LENGTH;
    },
    step2ButtonDisabled() {
      return this.step1ButtonDisabled
        || !this.offer?.duration
        || !this.offer?.amount
        || !this.offer?.noticePeriod
        || !this.offer?.securityDepositPeriod;
    },
    step3ButtonDisabled() {
      return this.step1ButtonDisabled
        || this.step2ButtonDisabled
        || (!this.isNew && !this.offerChanged)
        || (this.isNew && !this.validEmail);
    },
    confirmEmailStep() {
      return this.isNew && !this.emailCode;
    },
    buttonDisabled() {
      return (this.step === 1 && this.step1ButtonDisabled)
        || (this.step === 2 && this.step2ButtonDisabled)
        || (this.step === 3 && this.step3ButtonDisabled);
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
    isUpdateExpirationDate() {
      return this.offer.updateExpirationDate;
    },
    expirationDateChoiceLabel() {
      return this.isUpdateExpirationDate
        && this.$t('deedUpdateExpirationDate')
        || this.$t('deedKeepExpirationDate');
    },
    expirationDuration() {
      return this.offer.expirationDuration;
    },
    expirationDurationInMillis() {
      if (!this.expirationDuration) {
        return 0;
      }
      switch (this.expirationDuration) {
      case 'ONE_DAY': return 24 * 60 * 60 * 1000;
      case 'THREE_DAYS': return 3 * 24 * 60 * 60 * 1000;
      case 'ONE_WEEK': return 7 * 24 * 60 * 60 * 1000;
      case 'ONE_MONTH': return 30 * 24 * 60 * 60 * 1000;
      }
      return 0;
    },
    expirationDate() {
      return this.isUpdateExpirationDate && (Date.now() + this.expirationDurationInMillis) || (new Date(this.offer.createdDate).getTime() + this.expirationDurationInMillis);
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
    deleting() {
      if (this.deleting) {
        this.$refs.drawer?.startLoading();
      } else {
        this.$refs.drawer?.endLoading();
      }
    },
    offer: {
      handler() {
        this.offerChanged = true;
      },
      deep: true
    },
  },
  created() {
    this.$root.$on('deeds-rent-drawer', this.open);
    this.$root.$on('deeds-rent-close', this.close);
  },
  beforeDestroy() {
    this.$root.$off('deeds-rent-drawer', this.open);
    this.$root.$off('deeds-rent-close', this.close);
  },
  methods: {
    open(nftId, offer) {
      if (!this.offer || (offer?.id && this.offer?.id !== offer?.id)) {
        this.step = 1;
      }
      if (offer) {
        this.offer = Object.assign({}, this.DEFAULT_OFFER, offer);
        if (this.offer.expirationDuration) {
          this.forceUpdateExpiration = false;
          this.offer.updateExpirationDate = false;
        } else {
          this.forceUpdateExpiration = true;
          this.offer.updateExpirationDate = true;
        }
      } else {
        this.offer = Object.assign({
          nftId,
        }, this.DEFAULT_OFFER);
      }
      this.isNew = !offer;
      this.emailCode = null;
      this.emailCodeSent = false;
      this.$refs.drawer?.open();
      this.$nextTick().then(() => this.offerChanged = false);
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
    deleteOffer(confirmed) {
      if (!confirmed) {
        this.$refs.confirmDialog.open();
        return;
      }
      this.deleting = true;
      return this.$deedTenantOfferService.deleteOffer(this.offer.id)
        .then(() => {
          this.$root.$emit('deed-offer-renting-deleted', this.offer);
          this.deleting = false;
          this.$root.$emit('alert-message', this.$t('deedRentingOfferDeleted'), 'success');
          this.$nextTick().then(() => this.cancel());
        })
        .catch(() => {
          this.deleting = false;
          this.$root.$emit('alert-message', this.$t('deedRentingOfferDeletionError'), 'error');
        });
    },
    goToStep2() {
      if (this.step !== 2 && (this.step !== 1 || !this.step1ButtonDisabled)) {
        this.step = 2;
        this.scrollDrawerContent();
      }
    },
    goToStep3() {
      if (this.step !== 3
          && (this.step !== 1 || !this.step1ButtonDisabled)
          && (this.step !== 2 || !this.step2ButtonDisabled)) {
        this.step = 3;
        this.scrollDrawerContent();
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
    confirmOffer() {
      if (this.step === 1) {
        this.goToStep2();
      } else if (this.step === 2) {
        this.goToStep3();
      } else if (this.confirmEmailStep) {
        this.sendEmailConfirmation();
      } else {
        this.sending = true;
        const savePromise = this.isNew && this.$deedTenantOfferService.createOffer(this.offer, this.emailCode)
          || this.$deedTenantOfferService.updateOffer(this.offer.id, this.offer);
        return savePromise
          .then(offer => {
            this.$root.$emit(this.isNew && 'deed-offer-renting-created' || 'deed-offer-renting-updated', offer);
            const message = this.$t(this.isNew && 'deedRentingOfferCreated' || 'deedRentingOfferUpdated');
            this.$root.$emit('alert-message',
              message,
              'success', 
              () => this.openOfferInMarketplace(offer.id),
              'fas fa-magnifying-glass primary--text mx-4 ps-1',
              this.$t('deedRentingOpenOfferInMarketplace'));
            this.sending = false;// Make sending = false to delete permanent behavior of drawer before closing it
            return this.$nextTick();
          })
          .then(() => this.cancel())
          .catch(() => {
            this.sending = false;
            this.$root.$emit('alert-message', this.$t(this.isNew && 'deedRentingOfferCreationError' || 'deedRentingOfferUpdateError'), 'error');
          });
      }
    },
    openOfferInMarketplace(offerId) {
      this.$store.commit('setOfferId', offerId);
      this.$root.$emit('switch-page', 'marketplace');
    },
  },
};
</script>