<template>
  <v-card
    v-if="isOwner || isProvisioningManager"
    class="d-flex flex-column"
    max-height="1000px"
    width="100%"
    max-width="100%"
    elevation="0"
    outlined>
    <div class="d-flex">
      <v-list-item-avatar
        class="deed-avatar mx-2 mt-3 mb-auto"
        height="70"
        min-width="70"
        width="70">
        <v-img :src="cardImage" />
      </v-list-item-avatar>
      <v-card
        :id="cardElementId"
        class="flex-grow-1"
        flat>
        <v-card-title class="d-flex ps-0 py-2">
          <span class="text-capitalize">{{ $t('deedRentalNftTypeTitle', {0: cardType, 1: nftId}) }}</span>
        </v-card-title>
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
                      <v-list-item class="pa-0 my-n2">
                        <v-list-item-content class="py-0 text-capitalize">
                          {{ $t('cityName', {0: city}) }}
                        </v-list-item-content>
                        <v-list-item-action-text class="d-flex py-0">
                          {{ maxUsersLabel }}
                        </v-list-item-action-text>
                      </v-list-item>

                      <v-list-item class="pa-0 my-n2">
                        <v-list-item-content class="py-0">
                          {{ $t('deedMintingPower') }}
                        </v-list-item-content>
                        <v-list-item-action-text class="d-flex py-0">
                          <v-tooltip bottom>
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

                      <v-list-item class="pa-0 my-n2 hidden-md-and-up">
                        <v-list-item-content class="py-0">
                          {{ $t('deedRentingRentedTenantStatus') }}
                        </v-list-item-content>
                        <v-list-item-action-text class="d-flex py-0">
                          <deeds-owned-item-status :nft="nft" />
                        </v-list-item-action-text>
                      </v-list-item>

                      <v-list-item class="pa-0 my-n2 hidden-sm-and-down">
                        <v-list-item-content class="py-0">
                          {{ $t('deedCharacteristicsCityVotingRights') }}
                        </v-list-item-content>
                        <v-list-item-action-text class="py-0">
                          {{ cityVotingRights }}
                        </v-list-item-action-text>
                      </v-list-item>

                      <v-list-item class="pa-0 my-n2">
                        <v-list-item-content class="py-0">
                          {{ $t('deedRentingStartDate') }}
                        </v-list-item-content>
                        <v-list-item-action-text class="py-0">
                          <deeds-date-format :value="rentalStartDate" />
                        </v-list-item-action-text>
                      </v-list-item>

                      <v-list-item class="pa-0 my-n2">
                        <v-list-item-content class="py-0">
                          {{ $t('deedRentingEndDate') }}
                        </v-list-item-content>
                        <v-list-item-action-text class="py-0">
                          <deeds-date-format :value="rentalEndDate" />
                        </v-list-item-action-text>
                      </v-list-item>

                      <v-list-item class="pa-0 my-n2 hidden-sm-and-down">
                        <v-list-item-content class="py-0">
                          {{ $t('deedRentingRemainingTime') }}
                        </v-list-item-content>
                        <v-list-item-action-text class="py-0">
                          {{ remainingTimeLabel }}
                        </v-list-item-action-text>
                      </v-list-item>

                      <v-list-item class="pa-0 my-n2 hidden-sm-and-down">
                        <v-list-item-content class="py-0">
                          {{ $t('deedRentingNoticePeriodTitle') }}
                        </v-list-item-content>
                        <v-list-item-action-text class="py-0">
                          <deeds-date-format v-if="noticeDate" :value="noticeDate" />
                          <template v-else>{{ $t('deedRentingNoNoticePeriodLabel') }}</template>
                        </v-list-item-action-text>
                      </v-list-item>
                    </v-list>
                  </v-col>

                  <v-col
                    class="py-0 ps-0 ps-md-2 mt-2 mt-md-0"
                    cols="12"
                    md="5">
                    <v-list dense>
                      <v-list-item class="pa-0 my-n2 hidden-sm-and-down">
                        <v-list-item-content class="py-0">
                          {{ $t('deedRentingRentedTenantStatus') }}
                        </v-list-item-content>
                        <v-list-item-action-text class="d-flex py-0">
                          <deeds-owned-item-status :nft="nft" />
                        </v-list-item-action-text>
                      </v-list-item>

                      <v-list-item class="pa-0 my-n2">
                        <v-list-item-content class="py-0">
                          {{ $t('deedRentingRewardDistribution') }}
                        </v-list-item-content>
                        <v-list-item-action-text class="d-flex py-0">
                          <v-card min-width="50" flat>
                            <v-tooltip bottom>
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

                      <v-list-item class="pa-0 my-n2">
                        <v-list-item-content class="py-0">
                          {{ $t('deedRentingRewardsDistibuted') }}
                        </v-list-item-content>
                        <v-list-item-action-text class="py-0 d-flex">
                          <deeds-number-format
                            v-if="distributedAmount"
                            :value="distributedAmount"
                            :fractions="2"
                            no-decimals>
                            <span v-text="$t('meedsSymbol')" class="secondary--text font-weight-bold"></span>
                          </deeds-number-format>
                          <template v-else>
                            {{ $t('deedRentingRewardsNotDistibutedYet') }}
                          </template>
                        </v-list-item-action-text>
                      </v-list-item>

                      <v-list-item class="pa-0 my-n2">
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

                      <v-list-item class="pa-0 my-n2 hidden-sm-and-down">
                        <v-list-item-content class="py-0">
                          {{ $t('deedRentingSecurityDepositPeriodTitle') }}
                        </v-list-item-content>
                        <v-list-item-action-text class="py-0">
                          {{ securityDepositPeriodLabel }}
                        </v-list-item-action-text>
                      </v-list-item>

                      <v-list-item class="pa-0 my-n2 hidden-sm-and-down">
                        <v-list-item-content class="py-0">
                          {{ $t('deedRentingCumulativeRentsPaid') }}
                        </v-list-item-content>
                        <v-list-item-action-text class="py-0">
                          <deeds-number-format
                            :value="cumulativeRentsPaid"
                            :fractions="2"
                            no-decimals>
                            <span v-text="$t('meedsSymbol')" class="secondary--text font-weight-bold"></span>
                          </deeds-number-format>
                        </v-list-item-action-text>
                      </v-list-item>

                      <v-list-item class="pa-0 my-n2 hidden-sm-and-down">
                        <v-list-item-content class="py-0">
                          {{ $t('deedRentingSecurityDepositPaid') }}
                        </v-list-item-content>
                        <v-list-item-action-text class="py-0">
                          <deeds-number-format
                            v-if="securityDepositAmount"
                            :value="securityDepositAmount"
                            :fractions="2"
                            no-decimals>
                            <span v-text="$t('meedsSymbol')" class="secondary--text font-weight-bold"></span>
                          </deeds-number-format>
                          <template v-else>{{ $t('deedRentingDurationNoSecurityDepositLabel') }}</template>
                        </v-list-item-action-text>
                      </v-list-item>
                    </v-list>
                  </v-col>
                  <v-col
                    class="d-flex flex-row flex-md-column flex-wrap py-0 mt-8 mt-md-0 justify-space-between"
                    cols="12"
                    md="2">
                    <template v-if="authenticated">
                      <template v-if="!stopped">
                        <v-btn
                          v-if="accessButtonDisabled"
                          :min-width="minButtonsWidth"
                          :width="buttonsWidth"
                          :max-width="maxButtonsWidth"
                          disabled
                          class="primary mx-auto mt-2 mt-md-0"
                          depressed
                          dark>
                          <v-icon class="me-2" small>fa-external-link</v-icon>
                          {{ $t('deedTenantAccessButton') }}
                        </v-btn>
                        <v-btn
                          v-else
                          :href="deedTenantLink"
                          :min-width="minButtonsWidth"
                          :width="buttonsWidth"
                          :max-width="maxButtonsWidth"
                          :disabled="accessButtonDisabled"
                          :class="accessButtonDisabled && 'primary'"
                          target="_blank"
                          rel="nofollow noreferrer noopener"
                          class="mx-auto mt-2 mt-md-0"
                          color="primary"
                          depressed
                          dark>
                          <v-icon class="me-2" small>fa-external-link</v-icon>
                          {{ $t('deedTenantAccessButton') }}
                        </v-btn>
                      </template>

                      <v-btn
                        v-if="started"
                        :loading="loadingMoveDeed"
                        :min-width="minButtonsWidth"
                        :width="buttonsWidth"
                        :max-width="maxButtonsWidth"
                        :disabled="!hasPaidCurrentPeriod"
                        :title="$t('moveOut')"
                        class="mx-auto mt-2 mt-md-0"
                        color="secondary"
                        outlined
                        depressed
                        dark
                        @click="openMoveOutDrawer">
                        <span class="text-truncate position-absolute full-width">{{ $t('moveOut') }}</span>
                      </v-btn>
                      <v-btn
                        v-else-if="stopped"
                        :min-width="minButtonsWidth"
                        :width="buttonsWidth"
                        :max-width="maxButtonsWidth"
                        :loading="loadingMoveDeed"
                        :disabled="!hasPaidCurrentPeriod"
                        :class="!hasPaidCurrentPeriod && 'primary'"
                        :title="$t('moveIn')"
                        class="mx-auto mt-2 mt-md-0"
                        color="primary"
                        depressed
                        dark
                        @click="openMoveInDrawer">
                        <span class="text-truncate position-absolute full-width">{{ $t('moveIn') }}</span>
                      </v-btn>
                      <v-btn
                        v-else-if="starting"
                        :min-width="minButtonsWidth"
                        :width="buttonsWidth"
                        :max-width="maxButtonsWidth"
                        :title="$t('moveInProgress')"
                        class="mx-auto mt-2 mt-md-0"
                        outlined
                        disabled
                        color="primary"
                        depressed
                        dark>
                        <span class="text-truncate position-absolute full-width">{{ $t('moveInProgress') }}</span>
                      </v-btn>
                      <v-btn
                        v-else-if="stopping"
                        :min-width="minButtonsWidth"
                        :width="buttonsWidth"
                        :max-width="maxButtonsWidth"
                        :title="$t('moveOutInProgress')"
                        class="mx-auto mt-2 mt-md-0"
                        outlined
                        disabled
                        color="primary"
                        depressed
                        dark>
                        <span class="text-truncate position-absolute full-width">{{ $t('moveOutInInProgress') }}</span>
                      </v-btn>

                      <v-btn
                        :min-width="minButtonsWidth"
                        :width="buttonsWidth"
                        :max-width="maxButtonsWidth"
                        :title="$t('payTheRent')"
                        color="primary"
                        class="mx-auto mt-2 mt-md-0"
                        outlined
                        depressed
                        dark>
                        <span class="text-truncate position-absolute full-width">{{ $t('payTheRent') }}</span>
                      </v-btn>
                      <v-btn
                        :disabled="!hasPaidCurrentPeriod"
                        :min-width="minButtonsWidth"
                        :width="buttonsWidth"
                        :max-width="maxButtonsWidth"
                        :title="$t('claimRewards')"
                        class="mx-auto mt-2 mt-md-0"
                        color="primary"
                        outlined
                        depressed
                        dark>
                        <span class="text-truncate position-absolute full-width">{{ $t('claimRewards') }}</span>
                      </v-btn>
                      <v-btn
                        :min-width="minButtonsWidth"
                        :width="buttonsWidth"
                        :max-width="maxButtonsWidth"
                        :title="$t('endRental')"
                        class="mx-auto mt-2 mt-md-0"
                        color="secondary"
                        outlined
                        depressed
                        dark>
                        <span class="text-truncate position-absolute full-width">{{ $t('endRental') }}</span>
                      </v-btn>
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
    tenant: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    deedInfo: {},
    minButtonsWidth: '150px',
    buttonsWidth: '100%',
    maxButtonsWidth: '250px',
    loadingMoveDeed: false,
    loading: true,
  }),
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    address: state => state.address,
    authenticated: state => state.authenticated,
    nftId() {
      return this.tenant?.nftId;
    },
    nft() {
      return this.loading && {} || {
        id: this.nftId,
        stopped: this.stopped,
        started: this.started,
        starting: this.starting,
        stopping: this.stopping,
        beingPrepared: this.tenantStatus === 'UNDEPLOYED',
        cityName: this.city,
      };
    },
    cardElementId() {
      return `deedTenantCard-${this.tenant.id}`;
    },
    cardType() {
      return this.tenant?.cardType?.toUpperCase() || '';
    },
    city() {
      return this.tenant?.city?.toUpperCase() || '';
    },
    cityVotingRights() {
      switch (this.cardType) {
      case 'COMMON': return 1;
      case 'UNCOMMON': return 10;
      case 'RARE': return 100;
      case 'LEGENDARY': return 1000;
      default: return '';
      }
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
    paymentPeriodicity() {
      return this.tenant?.paymentPeriodicity || '';
    },
    rentalTenantMintingPower() {
      return this.tenant?.mintingPower;
    },
    rentalTenantMintingPowerPercentage() {
      return parseInt((this.rentalTenantMintingPower - 1) * 100);
    },
    rentalOwnerMintingPercentage() {
      return this.tenant?.ownerMintingPercentage || 0;
    },
    rentalTenantMintingPercentage() {
      return 100 - this.rentalOwnerMintingPercentage;
    },
    rentalStartDate() {
      return this.tenant?.acquiredDate;
    },
    rentalEndDate() {
      return this.tenant?.rentingEndDate;
    },
    remainingTimeLabel() {
      const rentalEndDate = new Date(this.rentalEndDate.substring(0, 10));
      const now = new Date();
      let months = (rentalEndDate.getYear() * 12 + rentalEndDate.getMonth()) - (now.getYear() * 12 + now.getMonth());
      let days = rentalEndDate.getDate() - now.getDate();
      if (days < 0) {
        months--;
        const rentalEndDateMillis = rentalEndDate.getTime();
        rentalEndDate.setMonth(rentalEndDate.getMonth() - 1);
        days = (rentalEndDateMillis - rentalEndDate.getTime()) / 1000 / 60 / 60 / 24 + days;
      }
      let monthsLabel = '';
      if (months === 1) {
        monthsLabel = this.$t('deedRentingRemainingTimeOneMonth');
      } else if (months > 0) {
        monthsLabel = this.$t('deedRentingRemainingTimeMonths', {0: months});
      }
      let daysLabel = '';
      if (days === 1) {
        daysLabel = this.$t('deedRentingRemainingTimeOneDay');
      } else if (days > 0) {
        daysLabel = this.$t('deedRentingRemainingTimeDays', {0: days});
      }
      return `${monthsLabel} ${daysLabel}`;
    },
    noticeDate() {
      return this.tenant?.noticeDate;
    },
    tokenAmount() {
      return this.tenant?.amount || 0;
    },
    tokenAmountPerMonth() {
      switch (this.paymentPeriodicity){
      case 'ONE_MONTH': return this.tokenAmount;
      case 'ONE_YEAR': return this.tokenAmount / 12;
      default: return 0;
      }
    },
    distributedAmount() {
      return this.tenant?.distributedAmount || 0;
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
    securityDepositPeriodLabel() {
      if (!this.tenant?.securityDepositPeriod) {
        return this.$t('deedRentingDurationNoSecurityDepositLabel');
      }
      switch (this.tenant?.securityDepositPeriod) {
      case 'ONE_MONTH': return this.$t('deedRentingDurationOneMonth');
      case 'TWO_MONTHS': return this.$t('deedRentingDurationTwoMonths');
      case 'THREE_MONTHS': return this.$t('deedRentingDurationThreeMonths');
      }
      return this.$t('deedRentingDurationNoSecurityDepositLabel');
    },
    securityDepositFactor() {
      if (!this.tenant?.securityDepositPeriod) {
        return 0;
      }
      switch (this.tenant?.securityDepositPeriod) {
      case 'ONE_MONTH': return 1;
      case 'TWO_MONTHS': return 2;
      case 'THREE_MONTHS': return 3;
      }
      return 0;
    },
    securityDepositAmount() {
      return this.securityDepositFactor * this.tokenAmountPerMonth;
    },
    cumulativeRentsPaid() {
      return this.tenant?.paidPeriods * this.tokenAmountPerMonth;
    },
    provisioningStatus() {
      return this.deedInfo?.provisioningStatus || this.tenant?.provisioningStatus;
    },
    tenantStatus() {
      return this.deedInfo?.status || this.tenant?.tenantStatus;
    },
    tenantStarted() {
      return this.tenantStatus === 'DEPLOYED';
    },
    tenantStartDate() {
      return this.tenantStarted && this.tenant?.tenantStartDate;
    },
    isProvisioningManager() {
      return this.tenant?.managerAddress?.toLowerCase() === this.address?.toLowerCase();
    },
    isOwner() {
      return this.tenant?.ownerAddress?.toLowerCase() === this.address?.toLowerCase();
    },
    showDeedLinkPart() {
      return this.showMovePart && this.started;
    },
    hasPaidCurrentPeriod() {
      return !this.tenant?.nextPaymentDate || new Date(this.tenant?.nextPaymentDate).getTime() > Date.now();
    },
    accessButtonDisabled() {
      return !this.started || !this.hasPaidCurrentPeriod;
    },
    deedTenantLink() {
      return `https://${this.city}-${this.nftId}.wom.meeds.io`;
    },
    stopped() {
      return !this.provisioningStatus || this.provisioningStatus === 'STOP_CONFIRMED';
    },
    started() {
      return this.provisioningStatus === 'START_CONFIRMED';
    },
    starting() {
      return this.provisioningStatus === 'START_IN_PROGRESS';
    },
    stopping() {
      return this.provisioningStatus === 'STOP_IN_PROGRESS';
    },
  }),
  watch: {
    authenticated() {
      // TO DELETE Once Backend Works
      this.refreshDeedInfo();
    },
    tenantProvisioningContract: {
      immediate: true,
      handler() {
        this.installListeners();
      },
    },
  },
  created() {
    // TO DELETE Once Backend Works
    this.refreshDeedInfo();

    this.installListeners();
    this.$root.$on('nft-status-changed', this.handleStatusChanged);
    document.addEventListener('nft-tenant-provisioning-changed', this.handleBlockchainStatusChanged);
  },
  beforeDestroy() {
    this.$root.$off('nft-status-changed', this.handleStatusChanged);
    document.removeEventListener('nft-tenant-provisioning-changed', this.handleBlockchainStatusChanged);
  },
  methods: {
    installListeners() {
      this.$store.commit('installProvisioningListeners');
    },
    handleBlockchainStatusChanged(event) {
      const detail = event?.detail;
      if (detail) {
        this.handleStatusChanged(detail.nftId, null, null, detail.command);
      }
    },
    handleStatusChanged(id, status, _transactionHash, command) {
      if (id && Number(this.nftId) === Number(id)) {
        const isInProgress = status === 'loading';
        const isStartCommand = command === 'start';
        const provisioningStatus = isInProgress && (isStartCommand && 'START_IN_PROGRESS' || 'STOP_IN_PROGRESS')
          || (isStartCommand && 'START_CONFIRMED' || 'STOP_CONFIRMED');
        this.deedInfo = Object.assign(this.deedInfo, {
          provisioningStatus,
        });
      }
    },
    openMoveOutDrawer() {
      this.loadingMoveDeed = true;
      this.openDrawer(false, 'deeds-move-out-drawer')
        .finally(() => this.loadingMoveDeed = false);
    },
    openMoveInDrawer() {
      this.loadingMoveDeed = true;
      this.openDrawer(true, 'deeds-move-in-drawer')
        .finally(() => this.loadingMoveDeed = false);
    },
    openDrawer(sendNft, eventName, obj) {
      return this.refreshDeedInfo()
        .finally(() =>
          this.$nextTick().then(() => {
            if (this.authenticated && this.nftId) {
              if (sendNft) {
                this.$root.$emit(eventName, {
                  id: this.nftId,
                  cardName: this.cardType,
                }, obj);
              } else {
                this.$root.$emit(eventName, this.nftId, obj);
              }
            } else {
              this.$root.$emit('alert-message', this.$t('loggedOutPleaseLoginAgain'), 'warning');
            }
          })
        );
    },
    refreshDeedInfo() {
      if (this.authenticated) {
        this.loading = true;
        return this.$tenantManagement.getTenantInfo(this.nftId)
          .then(deedInfo => this.deedInfo = deedInfo)
          .catch(() => {
            if (this.$refs.loginButton) {
              return this.$refs.loginButton.logout(true);
            }
          })
          .finally(() => this.loading = false);
      }
    },
  },
};
</script>