<template>
  <v-card
    v-if="isOwner || isProvisioningManager"
    class="d-flex flex-column"
    min-height="290px"
    max-height="1000px"
    width="100%"
    max-width="100%"
    elevation="0"
    outlined>
    <div v-show="show" :class="show && 'd-flex'">
      <deeds-lease-card-image
        :city="city"
        :card-type="cardType"
        class="mt-3 hidden-sm-and-down" />
      <v-card
        :id="cardElementId"
        class="flex-grow-1"
        flat>
        <deeds-lease-card-title
          :nft-id="nftId"
          :city="city"
          :card-type="cardType" />
        <v-card-text class="ps-0 pb-6">
          <v-list dense>
            <v-list-item class="pa-0">
              <v-list-item-content class="py-0">
                <v-row class="pa-0 ma-0">
                  <v-col
                    class="py-0 ps-0"
                    cols="12"
                    md="5">
                    <v-list dense>
                      <deeds-lease-card-max-users
                        :city="city"
                        :card-type="cardType" />
                      <deeds-lease-card-minting-power
                        :card-type="cardType" />
                      <deeds-lease-card-hub-status
                        :nft="nft"
                        :confirmed="rentalConfirmed"
                        class="hidden-md-and-up" />
                      <deeds-lease-card-votes
                        :card-type="cardType"
                        class="hidden-sm-and-down" />
                      <deeds-lease-card-start-date
                        :start-date="rentalStartDate" />
                      <deeds-lease-card-end-date
                        :end-date="rentalEndDate"
                        :ending-lease="lease.endingLease" />
                      <deeds-lease-card-remaining-time
                        :confirmed="rentalConfirmed"
                        :end-date="rentalEndDate"
                        class="hidden-sm-and-down" />
                      <deeds-lease-card-notice-period
                        :confirmed="rentalConfirmed"
                        :start-date="rentalStartDate"
                        :end-date="rentalEndDate"
                        :rent-period-months="lease.months"
                        :notice-period-months="noticePeriodMonths"
                        class="hidden-sm-and-down" />
                    </v-list>
                  </v-col>
                  <v-col
                    class="py-0 ps-0 ps-md-2 mt-2 mt-md-0"
                    cols="12"
                    md="5">
                    <v-list dense>
                      <deeds-lease-card-hub-status
                        :nft="nft"
                        :confirmed="rentalConfirmed"
                        class="hidden-sm-and-down" />
                      <deeds-lease-card-minting-distribution
                        :owner-minting-percentage="lease.ownerMintingPercentage" />
                      <deeds-lease-card-distributed-rewards
                        :confirmed="rentalConfirmed"
                        :distributed-reward="distributedReward" />
                      <deeds-lease-card-rent-amount
                        :amount="lease.amount"
                        :payment-periodicity="lease.paymentPeriodicity" />
                      <deeds-lease-card-cumulative-rents
                        :month-payment-in-progress="lease.monthPaymentInProgress"
                        :cumulative-rents-paid="cumulativeRentsPaid"
                        :confirmed="rentalConfirmed"
                        class="hidden-sm-and-down" />
                      <deeds-lease-card-notice-period-paid
                        :notice-period-months="noticePeriodMonths"
                        :rent-amount="lease.amount"
                        :confirmed="rentalConfirmed"
                        class="hidden-sm-and-down" />
                    </v-list>
                  </v-col>
                  <v-col
                    class="d-flex flex-row flex-md-column flex-wrap py-0 mt-8 mt-md-0 justify-space-between"
                    cols="12"
                    md="2">
                    <template v-if="authenticated">
                      <deeds-lease-card-hub-access-button
                        v-if="!stopped"
                        :nft-id="nftId"
                        :city="city"
                        :started="started"
                        :has-paid-current-period="hasPaidCurrentPeriod" />

                      <deeds-lease-card-move-button
                        :lease="lease"
                        :confirmed="rentalConfirmed"
                        :has-paid-current-period="hasPaidCurrentPeriod"
                        @provisioning-status="provisioningStatus = $event"
                        @tenant-status="tenantStatus = $event"
                        @logout="logout($event)" />

                      <deeds-lease-card-pay-rent-button
                        :lease="lease"
                        :confirmed="rentalConfirmed"
                        :disabled="disabledPayRent"
                        :has-payment-in-progress="hasPaymentInProgress"
                        :is-lease-not-fully-paid="isLeaseNotFullyPaid" />

                      <deeds-lease-card-claim-reward-button
                        :disabled="disabledClaimRewards" />

                      <deeds-lease-card-end-rent-button
                        :lease="lease"
                        :disabled="disabledEndRent" />
                    </template>
                    <div v-show="!authenticated" class="mt-auto">
                      <deeds-login-button
                        ref="loginButton"
                        :login-tooltip="$t('authenticateToCommandTenantTooltip')"
                        class="mt-auto" />
                    </div>
                  </v-col>
                </v-row>
              </v-list-item-content>
            </v-list-item>
          </v-list>
        </v-card-text>
      </v-card>
    </div>
  </v-card>
</template>
<script>
export default {
  props: {
    lease: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    provisioningStatus: null,
    tenantStatus: null,
    show: true,
  }),
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    address: state => state.address,
    authenticated: state => state.authenticated,
    MONTH_IN_SECONDS: state => state.MONTH_IN_SECONDS,
    DAY_IN_SECONDS: state => state.DAY_IN_SECONDS,
    nftId() {
      return this.lease?.nftId;
    },
    nft() {
      return {
        id: this.nftId,
        stopped: this.stopped,
        started: this.started,
        starting: this.starting,
        stopping: this.stopping,
        beingPrepared: this.started && this.tenantStatus !== 'DEPLOYED',
        beingStopped: this.stopped && this.tenantStatus === 'DEPLOYED',
        isProvisioningManager: this.isProvisioningManager,
        cityName: this.city,
      };
    },
    cardElementId() {
      return `deedTenantCard-${this.lease.id}`;
    },
    cardType() {
      return this.lease?.cardType?.toUpperCase() || '';
    },
    city() {
      return this.lease?.city?.toUpperCase() || '';
    },
    paymentPeriodicity() {
      return this.lease?.paymentPeriodicity || '';
    },
    rentPeriodMonths() {
      return this.lease?.months || 0;
    },
    paidMonths() {
      return this.lease?.paidMonths || 0;
    },
    disabledPayRent() {
      return this.hasPaymentInProgress || !this.isLeaseOngoing  || !this.isLeaseNotFullyPaid;
    },
    disabledEndRent() {
      return this.hasPaymentInProgress || !this.isLeaseOngoing  || !this.isLeaseNotFullyPaid;
    },
    disabledClaimRewards() {
      return !this.lease?.pendingRewardsAmount;
    },
    hasPaymentInProgress() {
      return !this.rentalConfirmed || (this.lease?.monthPaymentInProgress && this.lease?.monthPaymentInProgress > 0);
    },
    paidRentsDate() {
      return this.lease?.paidRentsDate;
    },
    rentalStartDate() {
      return this.lease?.startDate;
    },
    rentalEndDate() {
      return this.lease?.endDate;
    },
    rentalPaidEndDate() {
      return this.noticeDate || this.paidRentsDate;
    },
    rentalConfirmed() {
      return this.lease?.confirmed;
    },
    noticeDate() {
      return this.lease?.noticeDate;
    },
    tokenAmount() {
      return this.lease?.amount || 0;
    },
    tokenAmountPerMonth() {
      switch (this.paymentPeriodicity){
      case 'ONE_MONTH': return this.tokenAmount;
      case 'ONE_YEAR': return this.tokenAmount / 12;
      default: return 0;
      }
    },
    cumulativeRentsPaid() {
      return this.paidMonths * this.tokenAmountPerMonth;
    },
    distributedReward() {
      return this.lease?.distributedAmount || 0;
    },
    noticePeriodMonths() {
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
    isProvisioningManager() {
      return this.lease?.managerAddress?.toLowerCase() === this.address?.toLowerCase();
    },
    isOwner() {
      return this.lease?.ownerAddress?.toLowerCase() === this.address?.toLowerCase();
    },
    hasPaidCurrentPeriod() {
      return !this.lease?.nextPaymentDate || new Date(this.lease?.nextPaymentDate).getTime() > Date.now();
    },
    isLeaseOngoing() {
      return this.rentalConfirmed && new Date(this.rentalEndDate).getTime() > Date.now();
    },
    isLeaseNotFullyPaid() {
      return this.rentalConfirmed && this.rentalEndDate && this.rentalPaidEndDate
        && new Date(this.rentalEndDate).getTime() !== new Date(this.rentalPaidEndDate).getTime();
    },
    stopped() {
      return this.rentalConfirmed && (!this.provisioningStatus || this.provisioningStatus === 'STOP_CONFIRMED');
    },
    started() {
      return this.rentalConfirmed && this.provisioningStatus === 'START_CONFIRMED';
    },
    starting() {
      return this.rentalConfirmed && this.provisioningStatus === 'START_IN_PROGRESS';
    },
    stopping() {
      return this.rentalConfirmed && this.provisioningStatus === 'STOP_IN_PROGRESS';
    },
  }),
  watch: {
    lease() {
      this.animate();
    },
  },
  methods: {
    animate() {
      this.show = false;
      window.setTimeout(() => this.show = true, 200);
    },
    logout(validate) {
      if (this.$refs.loginButton) {
        return this.$refs.loginButton.logout(validate);
      }
    },
  },
};
</script>