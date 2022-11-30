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
      <h4 v-if="isNew">{{ $t('deedRentingTitle', {0: cardTypeCapitalized, 1: nftId}) }}</h4>
      <h4 v-else>{{ $t('deedRentingEditDrawerTitle', {0: cardTypeCapitalized, 1: nftId}) }}</h4>
    </template>
    <template v-if="authenticated" #content>
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
        <deeds-renting-offer-card v-if="drawer" :offer="offer" />
      </v-card-text>
      <v-card-text class="d-flex flex-column flex-grow-1 rental-steps pt-0">
        <v-list-item
          class="d-flex align-center mt-2 flex-grow-0 max-height-40px pa-0"
          dense
          @click="step = 1">
          <v-chip :color="step === 1 && 'secondary' || 'secondary lighten-2'"><span class="font-weight-bold">1</span></v-chip>
          <span class="subtitle-1 ms-4">{{ $t('deedOfferDescriptionStepTitle') }}</span>
        </v-list-item>
        <v-expand-transition>
          <v-card
            v-show="step === 1"
            color="transparent"
            class="flex-grow-1"
            flat>
            <div class="px-0 pt-4">
              <deeds-extended-textarea
                v-if="drawer"
                v-model="offer.description"
                :placeholder="$t('deedRentingDescriptionPlaceholder')"
                :max-length="DESCRIPTION_MAX_LENGTH"
                class="mt-1" />
            </div>
          </v-card>
        </v-expand-transition>
        <v-list-item
          class="d-flex align-center mt-2 flex-grow-0 max-height-40px pa-0"
          dense
          @click="goToStep2">
          <v-chip :color="step === 2 && 'secondary' || 'secondary lighten-2'"><span class="font-weight-bold">2</span></v-chip>
          <span class="subtitle-1 ms-4">{{ $t('deedRentingConditionsStepTitle') }}</span>
        </v-list-item>
        <v-expand-transition>
          <v-form
            v-show="step === 2"
            ref="conditionsForm"
            color="transparent"
            class="flex-grow-1 mb-8"
            flat>
            <div class="mb-2 mt-6">{{ $t('deedRentingDurationTitle') }}</div>
            <deeds-renting-duration v-if="drawer" v-model="offer.duration" />
            <div class="mb-2 mt-6">{{ $t('deedRentingRentalOffer') }}</div>
            <div class="d-flex">
              <v-text-field
                v-model="offer.amount"
                name="rentalAmount"
                type="number"
                min="1"
                step="1"
                pattern="(?=.*\d)"
                color="grey"
                class="mt-0 pt-0 me-2"
                hide-details
                outlined
                dense
                required
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
            <div class="mb-2 mt-6">{{ $t('deedRentingRewardDistributionOfferForm') }}</div>
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
            <div class="mt-6">{{ $t('deedRentingNoticePeriodTitle') }}</div>
            <div class="mb-2 caption text--disabled">{{ $t('deedRentingNoticePeriodSubtitle') }}</div>
            <deeds-notice-period
              v-if="drawer"
              v-model="offer.noticePeriod"
              :max-value="maxNoticePeriod"
              max-value-exclusive />
          </v-form>
        </v-expand-transition>
        <v-list-item
          class="d-flex align-center mt-2 flex-grow-0 max-height-40px pa-0"
          dense
          @click="goToStep3">
          <v-chip :color="step === 3 && 'secondary' || 'secondary lighten-2'"><span class="font-weight-bold">3</span></v-chip>
          <span class="subtitle-1 ms-4">{{ $t('deedOfferVisibilityStepTitle') }}</span>
        </v-list-item>
        <v-expand-transition>
          <v-card
            v-show="step === 3"
            color="transparent"
            class="flex-grow-1 mb-6"
            flat>
            <div class="px-0 pt-4">
              {{ $t('deedOfferVisibilityStepDescription') }}
              <v-radio-group
                v-model="visibility"
                hide-details>
                <v-radio
                  :label="$t('deedOfferPublicVisibility')"
                  class="align-start"
                  value="ALL">
                  <template #label>
                    <div class="d-flex flex-column">
                      <div class="text--color">{{ $t('deedOfferPublicVisibility') }}</div>
                      <div class="caption">{{ $t('deedOfferPublicVisibilityDescription') }}</div>
                    </div>
                  </template>
                </v-radio>
                <v-radio
                  :label="$t('deedOfferAddressVisibility')"
                  class="align-start"
                  value="ADDRESS">
                  <template #label>
                    <div class="d-flex flex-column">
                      <div class="text--color">{{ $t('deedOfferAddressVisibility') }}</div>
                      <div class="caption">{{ $t('deedOfferAddressVisibilityDescription') }}</div>
                    </div>
                  </template>
                </v-radio>
              </v-radio-group>
              <div v-if="visibility === 'ADDRESS'" class="mb-8 mt-4">
                <span>{{ $t('deedOfferAddressVisibilityAssignedTo') }}</span>
                <v-text-field
                  v-model="offer.hostAddress"
                  :placeholder="$t('deedOfferAddressVisibilityAssignedToPlaceholder')"
                  name="hostAddress"
                  autocomplete="off"
                  class="mt-0 pt-0 me-2"
                  hide-details
                  outlined
                  dense />
                <span class="caption text--disabled">
                  {{ $t('deedOfferAddressVisibilityAssignedToSubtitle') }}
                </span>
              </div>
            </div>
            <div class="mb-2 mt-6">{{ $t('deedRentingExpirationDurationTitle') }}</div>
            <deeds-renting-expiration-duration v-if="drawer" v-model="offer.expirationDuration" />
          </v-card>
        </v-expand-transition>
        <v-list-item
          class="d-flex align-center mt-2 flex-grow-0 max-height-40px pa-0"
          dense
          @click="goToStep4">
          <v-chip :color="step === 4 && 'secondary' || 'secondary lighten-2'"><span class="font-weight-bold">4</span></v-chip>
          <span class="subtitle-1 ms-4">
            {{ isNew && $t('deedRentingStepTitleCreation') || $t('deedRentingStepTitleUpdate') }}
          </span>
        </v-list-item>
        <v-expand-transition>
          <v-card
            v-show="step === 4"
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
            <template v-if="hasNoticePeriod">
              <div class="d-flex mt-2">
                <div class="flex-grow-1">{{ $t('deedRentingNoticePeriodTitle') }}</div>
                {{ noticePeriodLabel }}
              </div>
              <div class="caption font-italic mb-4">
                {{ $t('deedRentingNoticePeriodSummarySubtitle') }}
              </div>
            </template>
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
                :code="emailCode"
                @valid-email="validEmail = $event"
                @email-confirmation-success="emailCode = $event"
                @email-confirmation-error="emailCodeError = true"
                @loading="sending = $event"
                @submit="confirmOffer" />
            </template>
            <div v-if="!isNew && offer.startDate && expirationDuration" class="d-flex align-center">
              <div class="flex-grow-1">
                <v-switch
                  v-model="updateExpirationDate"
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
        @click="closeAndReset">
        {{ $t('cancel') }}
      </v-btn>
      <v-btn
        v-else-if="canDelete"
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
  data: () => ({
    drawer: false,
    sending: false,
    deleting: false,
    isNew: false,
    step: 0,
    offer: {},
    originalOffer: null,
    offerChanged: false,
    email: null,
    emailCode: null,
    emailCodeSent: false,
    emailCodeError: false,
    validEmail: false,
    forceUpdateExpiration: false,
    visibility: 'ALL',
    DEFAULT_OFFER: {
      description: null,
      duration: 'ONE_YEAR',
      noticePeriod: 'ONE_MONTH',
      expirationDuration: null,
      amount: 0,
      paymentPeriodicity: 'ONE_MONTH',
      ownerMintingPercentage: 50,
    },
    updateExpirationDate: false,
    DESCRIPTION_MAX_LENGTH: 200,
    MIN_BUTTONS_WIDTH: 120,
  }),
  computed: Vuex.mapState({
    authenticated: state => state.authenticated,
    whiteThemeColor: state => state.whiteThemeColor,
    provider: state => state.provider,
    address: state => state.address,
    tenantRentingContract: state => state.tenantRentingContract,
    maxGasLimit: state => state.maxGasLimit,
    DAY_IN_SECONDS: state => state.DAY_IN_SECONDS,
    ZERO_X_ADDRESS: state => state.ZERO_X_ADDRESS,
    ZERO_BN: state => state.ZERO_BN,
    offerBlockchainStateChanged() {
      if (this.originalOffer) {
        const originalOfferBlockchainState = this.getBlockchainOfferStructure(this.originalOffer, false);
        const offerBlockchainState = this.getBlockchainOfferStructure(this.offer, this.updateExpirationDate);
        return JSON.stringify(originalOfferBlockchainState) !== JSON.stringify(offerBlockchainState);
      } else {
        return true;
      }
    },
    nftId() {
      return this.offer?.nftId;
    },
    cardType() {
      return this.offer?.cardType;
    },
    cardTypeCapitalized() {
      return this.cardType && `${this.cardType[0].toUpperCase()}${this.cardType.substring(1).toLowerCase()}`;
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
      return this.step < 4;
    },
    buttonLabel() {
      return (this.intermediateStep && this.$t('next'))
        || (this.confirmEmailStep && (this.emailCodeSent && this.$t('resend') || this.$t('send')))
        || (this.isNew
            && this.$t('deedRentingCreateButton')
            || (this.offerBlockchainStateChanged
                && this.$t('deedRentingUpdateButton')
                || this.$t('deedRentingUpdateNoBlockchainButton')));
    },
    step1ButtonDisabled() {
      return this.offer?.description?.length > this.DESCRIPTION_MAX_LENGTH;
    },
    step2ButtonDisabled() {
      return this.step1ButtonDisabled
        || !this.offer?.duration
        || !this.offer.amount
        || Number(this.offer.amount) <= 0
        || !Number.isInteger(Number(this.offer.amount))
        || !this.offer.paymentPeriodicity
        || !this.offer.noticePeriod;
    },
    step3ButtonDisabled() {
      return this.step1ButtonDisabled
        || this.step2ButtonDisabled
        || (this.visibility !== 'ALL' && (!this.offer?.hostAddress?.length || !ethers.utils.isAddress(this.offer.hostAddress)));
    },
    step4ButtonDisabled() {
      return this.step1ButtonDisabled
        || this.step2ButtonDisabled
        || this.step3ButtonDisabled
        || (!this.isNew && !this.offerChanged && !this.offerBlockchainStateChanged)
        || (this.isNew && !this.validEmail);
    },
    confirmEmailStep() {
      return this.isNew && !this.emailCode;
    },
    buttonDisabled() {
      return (this.step === 1 && this.step1ButtonDisabled)
        || (this.step === 2 && this.step2ButtonDisabled)
        || (this.step === 3 && this.step3ButtonDisabled)
        || (this.step === 4 && this.step4ButtonDisabled);
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
    canDelete() {
      return this.offer?.offerId;
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
    expirationDateChoiceLabel() {
      return this.updateExpirationDate
        && this.$t('deedUpdateExpirationDate')
        || this.$t('deedKeepExpirationDate');
    },
    expirationDuration() {
      return this.offer.expirationDuration;
    },
    maxNoticePeriod() {
      switch (this.offer?.paymentPeriodicity){
      case 'ONE_MONTH': return this.offer.duration;
      case 'ONE_YEAR': return 'NO_PERIOD';
      }
      return 'NO_PERIOD';
    },
    expirationDurationInMillis() {
      if (!this.expirationDuration) {
        return 0;
      }
      switch (this.expirationDuration) {
      case 'ONE_DAY': return this.DAY_IN_SECONDS * 1000;
      case 'THREE_DAYS': return 3 * this.DAY_IN_SECONDS * 1000;
      case 'ONE_WEEK': return 7 * this.DAY_IN_SECONDS * 1000;
      case 'ONE_MONTH': return 30 * this.DAY_IN_SECONDS * 1000;
      }
      return 0;
    },
    expirationDate() {
      return this.offer?.startDate && this.updateExpirationDate && (Date.now() + this.expirationDurationInMillis) || (new Date(this.offer.startDate).getTime() + this.expirationDurationInMillis);
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
    visibility() {
      if (this.visibility === 'ALL') {
        this.offer.hostAddress = null;
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
    open(nftId, offer, cardType) {
      if (this.isSameOffer(nftId, this.offer, offer)) {
        this.$refs.drawer?.open();
      } else {
        this.step = 1;
        if (offer) {
          this.offer = Object.assign({}, this.DEFAULT_OFFER, offer);
          this.originalOffer = Object.assign({}, this.DEFAULT_OFFER, offer);
          if (this.offer.expirationDuration) {
            this.forceUpdateExpiration = false;
            this.updateExpirationDate = false;
          } else {
            this.forceUpdateExpiration = true;
            this.updateExpirationDate = true;
          }
          this.visibility = this.offer.hostAddress ? 'ADDRESS' : 'ALL';
        } else {
          this.offer = Object.assign({
            nftId,
            cardType,
          }, this.DEFAULT_OFFER);
          this.originalOffer = null;
          this.visibility = 'ALL';
        }
        this.isNew = !offer;
        this.emailCode = null;
        this.emailCodeSent = false;
        this.$refs.email?.resetForm();
        this.$refs.drawer?.open();
        this.$nextTick().then(() => {
          this.offerChanged = false;
        });
      }
    },
    isSameOffer(nftId, offer1, offer2) {
      const sameIds = nftId && nftId === this.nftId && offer2?.id === offer1?.id;
      if (sameIds && offer1 && offer2) {
        const previousOffer = this.getBlockchainOfferStructure(offer1);
        const newOffer = this.getBlockchainOfferStructure(offer2);
        return JSON.stringify(previousOffer) !== JSON.stringify(newOffer);
      }
      return false;
    },
    closeAndReset() {
      this.close(this.nftId);
      this.$nextTick().then(() => this.offer = {});
    },
    close(nftId) {
      if (nftId === this.nftId) {
        this.$refs.drawer.close();
      }
    },
    goToStep2() {
      if (this.step !== 2 && (this.step !== 1 || !this.step1ButtonDisabled)) {
        this.step = 2;
        this.$nextTick().then(() => this.$refs?.conditionsForm?.$el?.reportValidity());
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
    goToStep4() {
      if (this.step !== 4
          && (this.step !== 1 || !this.step1ButtonDisabled)
          && (this.step !== 2 || !this.step2ButtonDisabled)
          && (this.step !== 3 || !this.step3ButtonDisabled)) {
        this.step = 4;
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
      } else if (this.step === 3) {
        if (this.$refs?.conditionsForm?.$el?.reportValidity()) {
          this.goToStep4();
        }
      } else if (this.confirmEmailStep) {
        this.sendEmailConfirmation();
      } else if (this.isNew) {
        this.createOffer();
      } else {
        this.updateOffer();
      }
    },
    saveOfferTransaction(isCreate) {
      if (!isCreate && !this.offerBlockchainStateChanged) {
        return Promise.resolve(null);
      }
      const deedOfferParam = this.getBlockchainOfferStructure(this.offer, this.updateExpirationDate);
      const options = {
        from: this.address,
        gasLimit: this.maxGasLimit
      };
      return this.$ethUtils.sendTransaction(
        this.provider,
        this.tenantRentingContract,
        isCreate && 'createOffer' || 'updateOffer',
        options,
        deedOfferParam
      ).then(receipt => receipt?.hash);
    },
    createOffer() {
      this.sending = true;
      return this.saveOfferTransaction(true)
        .then(transactionHash => {
          if (transactionHash) {
            this.offer.offerTransactionHash = transactionHash;
            return this.$deedTenantOfferService.createOffer(this.offer, this.emailCode)
              .then(offer => {
                this.$root.$emit('deed-offer-renting-created', offer);
                this.sending = false;// Make sending = false to delete permanent behavior of drawer before closing it
                return this.$nextTick();
              })
              .then(() => this.closeAndReset())
              .catch(e => {
                console.debug('Error creating offer', e); // eslint-disable-line no-console
                this.sending = false;
                this.$root.$emit('alert-message', this.$t('deedRentingOfferCreationError'), 'error');
              });
          } else {
            this.sending = false;
          }
        }).catch(() => this.sending = false);
    },
    updateOffer() {
      this.sending = true;
      return this.saveOfferTransaction(false)
        .then(transactionHash => {
          if (transactionHash || !this.offerBlockchainStateChanged) {
            this.offer.offerTransactionHash = transactionHash;
            return this.$deedTenantOfferService.updateOffer(this.offer.id, this.offer)
              .then(offer => {
                this.$root.$emit('deed-offer-renting-updated', offer);
                this.sending = false;// Make sending = false to delete permanent behavior of drawer before closing it
                return this.$nextTick();
              })
              .then(() => this.closeAndReset())
              .catch(e => {
                console.debug('Error updating offer', e); // eslint-disable-line no-console
                this.sending = false;
                this.$root.$emit('alert-message', this.$t('deedRentingOfferUpdateError'), 'error');
              });
          } else {
            this.sending = false;
          }
        }).catch(() => this.sending = false);
    },
    deleteOffer(confirmed) {
      if (!confirmed) {
        this.$refs.confirmDialog.open();
        return;
      }
      this.deleting = true;
      const options = {
        from: this.address,
        gasLimit: this.maxGasLimit
      };
      return this.$ethUtils.sendTransaction(
        this.provider,
        this.tenantRentingContract,
        'deleteOffer',
        options,
        [this.offer.offerId]
      )
        .then(receipt => {
          const transactionHash = receipt?.hash;
          if (transactionHash) {
            return this.$deedTenantOfferService.deleteOffer(this.offer.id, transactionHash)
              .then(() => {
                this.$root.$emit('deed-offer-renting-deleted', this.offer);
                this.$nextTick().then(() => this.closeAndReset());
              })
              .catch(() => this.$root.$emit('alert-message', this.$t('deedRentingOfferDeletionError'), 'error'))
              .finally(() => this.deleting = false);
          } else {
            this.deleting = false;
          }
        })
        .catch(e => {
          console.debug('Error deleting offer', e); // eslint-disable-line no-console
          this.deleting = false;
        });
    },
    getBlockchainOfferStructure(offer, updateExpirationDate) {
      if (!offer) {
        return [];
      }
      const offerStartDate = (updateExpirationDate || !offer.startDate || !offer.expirationDuration) ? this.ZERO_BN : ethers.BigNumber.from(parseInt(new Date(offer.startDate).getTime() / 1000));
      return [[
        offer.offerId || 0, // id
        offer.nftId, // NFT id
        this.ZERO_X_ADDRESS, // creator - AUTOMATICALLY SET
        this.getRentalDurationMonthsCount(offer), // months
        this.getNoticePeriodMonthsCount(offer), // noticePeriod
        offer.amount && this.$ethUtils.toDecimals(offer.amount, 18), // price
        0, // overAllPrice
        offerStartDate, // offerStartDate
        0, // offerExpirationDate - AUTOMATICALLY SET
        this.getExpirationDurationInDays(offer), // offerExpirationDays
        offer.hostAddress || this.ZERO_X_ADDRESS, // authorizedTenant
        offer.ownerMintingPercentage && parseInt(offer.ownerMintingPercentage) // ownerMintingPercentage
      ]];
    },
    getExpirationDurationInDays(offer) {
      if (!offer.expirationDuration) {
        return 0;
      }
      switch (offer.expirationDuration) {
      case 'ONE_DAY': return 1;
      case 'THREE_DAYS': return 3;
      case 'ONE_WEEK': return 7;
      case 'ONE_MONTH': return 30;
      }
      return 0;
    },
    getNoticePeriodMonthsCount(offer) {
      if (!offer.noticePeriod) {
        return 0;
      }
      switch (offer.noticePeriod) {
      case 'ONE_MONTH': return 1;
      case 'TWO_MONTHS': return 2;
      case 'THREE_MONTHS': return 3;
      }
      return 0;
    },
    getRentalDurationMonthsCount(offer) {
      if (!offer.duration) {
        return 0;
      }
      switch (offer.duration) {
      case 'ONE_MONTH': return 1;
      case 'THREE_MONTHS': return 3;
      case 'SIX_MONTHS': return 6;
      case 'ONE_YEAR': return 12;
      }
      return 0;
    },
  },
};
</script>