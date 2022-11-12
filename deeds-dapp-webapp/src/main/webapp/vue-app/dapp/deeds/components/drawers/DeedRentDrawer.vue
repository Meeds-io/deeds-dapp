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
      <h4 v-if="isNew" class="text-capitalize">{{ $t('deedRentingTitle', {0: cardName, 1: nftId}) }}</h4>
      <h4 v-else>{{ $t('deedRentingEditTitle') }}</h4>
    </template>
    <template v-if="offer" #content>
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
          @click="nextStep">
          <v-chip :color="step === 2 && 'secondary' || 'secondary lighten-2'"><span class="font-weight-bold">2</span></v-chip>
          <span class="subtitle-1 ms-4">{{ $t('deedRentingStep2Title') }}</span>
        </v-list-item>
        <v-expand-transition>
          <v-card
            v-show="step === 2"
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
                  color="error py-1"
                  track-color="success py-1"
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
      <deeds-confirm-dialog
        ref="confirmDialog"
        :title="$t('deedRentalDeleteOfferConfirmTitle')"
        :message="$t('deedRentalDeleteOfferConfirmDescription')"
        :ok-label="$t('confirm')"
        :cancel-label="$t('cancel')"
        @ok="deleteOffer(true)" />
    </template>
    <template #footer>
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
        :outlined="buttonOutlined"
        :class="buttonDisabled && 'primary'"
        name="rentConfirmButton"
        color="primary"
        depressed
        dark
        @click="saveOffer">
        {{ buttonLabel }}
      </v-btn>
    </template>
  </deeds-drawer>
</template>
<script>
export default {
  data: () => ({
    nft: null,
    drawer: false,
    sending: false,
    deleting: false,
    isNew: false,
    step: 0,
    offer: null,
    offerChanged: false,
    DEFAULT_OFFER: {
      nftId: null,
      description: null,
      duration: null,
      expirationDuration: null,
      amount: 10,
      paymentPeriodicity: 'ONE_MONTH',
      ownerMintingPercentage: 50,
    },
    DESCRIPTION_MAX_LENGTH: 200,
    MIN_BUTTONS_WIDTH: 120,
  }),
  computed: Vuex.mapState({
    nftId() {
      return this.nft?.id;
    },
    cardName() {
      return this.nft?.cardName;
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
    buttonOutlined() {
      return this.step === 1;
    },
    buttonLabel() {
      return this.step === 1
        && this.$t('next')
        || (this.isNew && this.$t('deedRentingCreateButton') || this.$t('deedRentingUpdateButton'));
    },
    buttonDisabled() {
      return this.step === 2
        && (
          !this.offerChanged
          || !this.offer?.duration
          || !this.offer?.amount
        );
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
  methods: {
    open(nft, offer) {
      this.nft = nft;
      if (!this.offer || (offer?.id && this.offer?.id !== offer?.id)) {
        this.step = 1;
      }
      if (offer) {
        this.offer = offer;
      } else {
        this.offer = Object.assign({}, this.DEFAULT_OFFER);
      }
      this.isNew = !offer;
      this.offer.nftId = this.nftId;
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
        .then(offer => {
          this.$root.$emit('deed-offer-renting-deleted', offer);
          this.deleting = false;
          this.$root.$emit('alert-message', this.$t('deedRentingOfferDeleted'), 'success');
          this.$nextTick().then(() => this.cancel());
        })
        .catch(() => {
          this.deleting = false;
          this.$root.$emit('alert-message', this.$t('deedRentingOfferDeletionError'), 'error');
        });
    },
    nextStep() {
      if (this.step === 1) {
        this.step = 2;
        window.setTimeout(() => {
          this.$refs.drawer.$el.querySelector('.rental-steps').scrollIntoView({
            behavior: 'smooth',
            block: 'end',
          });
        }, 200);
      }
    },
    saveOffer() {
      if (this.step === 1) {
        this.nextStep();
      } else {
        this.sending = true;
        const savePromise = this.isNew && this.$deedTenantOfferService.createOffer(this.offer)
          || this.$deedTenantOfferService.updateOffer(this.offer.id, this.offer);
        return savePromise
          .then(offer => {
            this.$root.$emit(this.isNew && 'deed-offer-renting-created' || 'deed-offer-renting-updated', offer);
            this.sending = false;
            const message = this.$t(this.isNew && 'deedRentingOfferCreated' || 'deedRentingOfferUpdated');
            this.$root.$emit('alert-message',
              message,
              'success', 
              () => this.openOfferInMarketplace(offer.id),
              'fas fa-magnifying-glass primary--text mx-4 ps-1',
              this.$t('deedRentingOpenOfferInMarketplace'));
            this.$nextTick().then(() => this.cancel());
          })
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