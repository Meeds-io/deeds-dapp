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
      <h4 class="text-capitalize">{{ $t('deedsRentPayDrawerTitle', {0: cardType, 1: nftId}) }}</h4>
    </template>
    <template v-if="lease" #content>
      <v-card
        class="flex-grow-1 mb-4 pa-4"
        color="transparent"
        flat>
        <div class="font-weight-bold mb-3">
          {{ $t('deedRentalOfferPaymentTitle') }}
        </div>
        <div class="d-flex mb-2">
          <div class="flex-grow-1">{{ $t('deedRentingStartDate') }}</div>
          <div class="d-flex">
            <deeds-date-format :value="rentalStartDate" />
          </div>
        </div>
        <div class="d-flex mb-2">
          <div class="flex-grow-1">{{ $t('deedRentingEndDate') }}</div>
          <div class="d-flex">
            <deeds-date-format :value="rentalEndDate" />
          </div>
        </div>
        <div class="d-flex mb-2">
          <div class="flex-grow-1">{{ $t('deedRentingPeriodicRentPrice') }}</div>
          <div class="d-flex">
            <deeds-number-format
              :value="lease.amount"
              :fractions="2"
              no-decimals>
              <span v-text="$t('meedsSymbol')" class="secondary--text font-weight-bold"></span>
            </deeds-number-format>
          </div>
        </div>
        <div v-if="hasNoticePeriod" class="d-flex mb-2">
          <div class="flex-grow-1">{{ $t('deedRentingNoticePeriodPaid') }}</div>
          <div class="d-flex">
            <deeds-number-format
              :value="noticePeriodAmount"
              :fractions="2"
              no-decimals>
              <span v-text="$t('meedsSymbol')" class="secondary--text font-weight-bold"></span>
            </deeds-number-format>
          </div>
        </div>
        <div class="d-flex mb-2">
          <div class="flex-grow-1">{{ $t('deedRentingCumulativeRentsPaid') }}</div>
          <div class="d-flex">
            <deeds-number-format
              :value="cumulativeRentsPaidAmount"
              :fractions="2"
              no-decimals>
              <span v-text="$t('meedsSymbol')" class="secondary--text font-weight-bold"></span>
            </deeds-number-format>
          </div>
        </div>
        <div class="d-flex mb-2">
          <div class="flex-grow-1">{{ $t('deedsRentPayDueDate') }}</div>
          <div class="d-flex">
            <template v-if="isDueDateNextMonth">
              {{ $t('deedsRentPayDueDateNextMonth') }}
            </template>
            <span v-else-if="isDueDateCurrentMonth" class="error--text">
              {{ $t('deedsRentPayDueDateCurrentMonth') }}
            </span>
            <span
              v-else-if="paidRentsDate"
              :class="isDueDatePast && 'error--text'"
              class="caption text-center">
              <deeds-date-format :value="dueRentStartDate" />
              -
              <deeds-date-format :value="dueRentEndDate" />
            </span>
          </div>
        </div>

        <div class="font-weight-bold mb-3 mt-4">
          {{ $t('deedRentPaymentTitle') }}
        </div>
        <div class="mb-2">
          <div>{{ $t('deedRentMonthsToPay') }}</div>
          <div class="d-flex mb-2">
            <div class="flex-grow-1">
              <v-slider
                v-model="monthsToPay"
                :max="rentRemainingMonths"
                min="1"
                hide-details />
            </div>
            <div class="d-flex text-ordinary-capitalize">
              {{ monthsToPayLabel }}
            </div>
          </div>
        </div>

        <div class="font-weight-bold mb-3 mt-4">
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
          class="d-flex mb-2"
          @click.stop.prevent="openBuyMeeds">
        </div>
      </v-card>
      <deeds-tenants-lease-payment-drawer
        ref="paymentDrawer"
        :lease-id="leaseId"
        :owner-address="ownerAddress"
        :months-to-pay="monthsToPay"
        :amount="totalAmountToPayWithDecimals"
        @transaction-sent="confirmPayment" />
    </template>
    <template #footer>
      <v-btn
        :disabled="sending"
        name="cancelPayRentButton"
        class="ms-auto me-2"
        min-width="120"
        outlined
        text
        @click="close">
        {{ $t('cancel') }}
      </v-btn>
      <v-btn
        :loading="sending"
        :min-width="MIN_BUTTONS_WIDTH"
        :class="primary"
        name="payRentConfirmButton"
        color="primary"
        depressed
        dark
        @click="confirm">
        {{ $t('deedsRentPayButton') }}
      </v-btn>
    </template>
  </deeds-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    sending: false,
    lease: null,
    monthsToPay: 1,
    MIN_BUTTONS_WIDTH: 120,
  }),
  computed: Vuex.mapState({
    authenticated: state => state.authenticated,
    meedsBalance: state => state.meedsBalance,
    tenantRentingAddress: state => state.tenantRentingAddress,
    ZERO_BN: state => state.ZERO_BN,
    MONTH_IN_SECONDS: state => state.MONTH_IN_SECONDS,
    nftId() {
      return this.lease?.nftId;
    },
    monthInMillis() {
      return this.MONTH_IN_SECONDS * 1000;
    },
    cardType() {
      return this.lease?.cardType;
    },
    leaseId() {
      return this.lease?.id;
    },
    hasNoticePeriod() {
      return this.lease?.noticePeriod && this.lease?.noticePeriod !== 'NO_PERIOD';
    },
    noticePeriodLabel() {
      if (!this.lease?.noticePeriod) {
        return null;
      }
      switch (this.lease?.noticePeriod) {
      case 'ONE_MONTH': return this.$t('deedRentingDurationOneMonth');
      case 'TWO_MONTHS': return this.$t('deedRentingDurationTwoMonths');
      case 'THREE_MONTHS': return this.$t('deedRentingDurationThreeMonths');
      }
      return null;
    },
    noticeMonths() {
      if (!this.lease?.noticePeriod) {
        return 0;
      }
      switch (this.lease?.noticePeriod) {
      case 'ONE_MONTH': return 1;
      case 'TWO_MONTHS': return 2;
      case 'THREE_MONTHS': return 3;
      }
      return 0;
    },
    noticePeriodAmount() {
      return this.lease.amount * this.noticeMonths;
    },
    paidMonths() {
      return this.lease?.paidMonths || 0;
    },
    cumulativeRentsPaidAmount() {
      return this.lease.amount * (this.paidMonths - this.noticeMonths);
    },
    rentalStartDate() {
      return this.lease?.startDate;
    },
    rentalEndDate() {
      return this.lease?.endDate;
    },
    buyMeedProposalLabel() {
      return this.$t('deedRentingPaymentMeedsBuyProposal', {
        0: '<a id="buyMeeds" class="mx-1 primary--text font-weight-bold">',
        1: this.$t('meeds'),
        2: '</a>',
      });
    },
    totalAmountToPay() {
      return this.monthsToPay * this.lease.amount;
    },
    totalAmountToPayWithDecimals() {
      return this.totalAmountToPay
        && ethers.BigNumber.from(this.totalAmountToPay).mul(ethers.BigNumber.from(10).pow(18))
        || this.ZERO_BN;
    },
    ownerAddress() {
      return this.lease?.ownerAddress;
    },
    rentPeriodMonths() {
      return this.lease?.months;
    },
    rentRemainingMonths() {
      return this.rentPeriodMonths - this.paidMonths;
    },
    monthsToPayLabel() {
      if (this.monthsToPay === 1) {
        return this.$t('deedRentingRemainingTimeOneMonth');
      } else {
        return this.$t('deedRentingRemainingTimeMonths', {0: this.monthsToPay});
      }
    },
    paidRentsDate() {
      return this.lease?.paidRentsDate && new Date(this.lease?.paidRentsDate) || null;
    },
    dueRentStartDate() {
      if (!this.paidRentsDate) {
        return null;
      }
      return this.paidRentsDate;
    },
    dueRentEndDate() {
      if (!this.paidRentsDate) {
        return null;
      }
      return new Date(this.dueRentStartDate.getTime() + this.monthInMillis);
    },
    isDueDatePast() {
      if (!this.paidRentsDate) {
        return false;
      }
      return this.dueRentStartDate.getTime() < Date.now();
    },
    isDueDateNextMonth() {
      if (!this.paidRentsDate || this.isDueDatePast) {
        return false;
      }
      const nextMonthDate = (Date.now() + this.monthInMillis);
      return nextMonthDate < this.dueRentEndDate.getTime() && nextMonthDate > this.dueRentStartDate.getTime();
    },
    isDueDateCurrentMonth() {
      if (!this.paidRentsDate || !this.isDueDatePast) {
        return false;
      }
      return Date.now() < this.dueRentEndDate.getTime();
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
    this.$root.$on('deeds-rent-pay-drawer', this.open);
    this.$root.$on('deeds-rent-pay-close', this.close);
  },
  beforeDestroy() {
    this.$root.$off('deeds-rent-pay-drawer', this.open);
    this.$root.$off('deeds-rent-pay-close', this.close);
  },
  methods: {
    open(lease) {
      if (!lease) {
        return;
      }
      this.lease = Object.assign({}, lease);
      this.sending = false;
      this.$refs.drawer?.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    confirm() {
      this.$refs.paymentDrawer.open();
    },
    confirmPayment(transactionHash) {
      this.sending = true;
      return this.$deedTenantLeaseService.payRent(this.leaseId, this.lease.ownerAddress, this.monthsToPay, transactionHash)
        .then(lease => {
          this.$root.$emit('deed-lease-payed', lease);
          this.sending = false;// Make sending = false to delete permanent behavior of drawer before closing it
          return this.$nextTick().then(() => this.drawer = false);
        })
        .catch(() => {
          this.sending = false;
          this.$root.$emit('alert-message', this.$t('deedRentPaymentError'), 'error');
        });
    },
    openBuyMeeds(event) {
      if (!event || event?.target?.tagName?.toLowerCase() === 'a') {
        this.$root.$emit('open-buy-meed-drawer');
      }
    },
  },
};
</script>