<template>
  <v-tooltip
    v-if="started"
    :disabled="disableButtons"
    z-index="4"
    max-width="300px"
    bottom>
    <template #activator="{ on, attrs }">
      <v-card
        class="mx-auto mt-2 mt-md-0"
        color="transparent"
        :width="buttonsWidth"
        :min-width="minButtonsWidth"
        :max-width="maxButtonsWidth"
        flat
        v-bind="attrs"
        v-on="on">
        <v-badge
          :value="!!hasRentOffers"
          icon="fas fa-triangle-exclamation mt-n2px"
          color="error"
          class="full-width"
          bordered
          overlap>
          <v-btn
            :loading="loadingMoveDeed"
            :width="buttonsWidth"
            :min-width="minButtonsWidth"
            :max-width="maxButtonsWidth"
            :disabled="disableButtons"
            color="secondary"
            outlined
            depressed
            dark
            @click="openMoveOutDrawer">
            <span class="text-truncate position-absolute full-width">
              {{ $t('moveOut') }}
            </span>
          </v-btn>
        </v-badge>
      </v-card>
    </template>
    <span>{{ moveOutDescription }}</span>
  </v-tooltip>
  <v-tooltip
    v-else-if="stopped"
    :disabled="disableButtons"
    z-index="4"
    max-width="300px"
    bottom>
    <template #activator="{ on, attrs }">
      <v-card
        class="mx-auto mt-2 mt-md-0"
        color="transparent"
        :width="buttonsWidth"
        :min-width="minButtonsWidth"
        :max-width="maxButtonsWidth"
        flat
        v-bind="attrs"
        v-on="on">
        <v-badge
          :value="!!hasRentOffers"
          color="info"
          icon="fas fa-info mt-n2px"
          class="full-width"
          bordered
          overlap>
          <v-btn
            :disabled="disableButtons"
            :loading="loadingMoveDeed"
            :width="buttonsWidth"
            :min-width="minButtonsWidth"
            :max-width="maxButtonsWidth"
            :outlined="hasRentOffers || disableButtons"
            color="primary"
            depressed
            dark
            @click="openMoveInDrawer">
            <span class="text-truncate position-absolute full-width">
              {{ $t('moveIn') }}
            </span>
          </v-btn>
        </v-badge>
      </v-card>
    </template>
    <span>{{ moveInDescription }}</span>
  </v-tooltip>
  <v-tooltip
    v-else-if="starting"
    z-index="4"
    max-width="300px"
    bottom>
    <template #activator="{ on, attrs }">
      <v-card
        class="mx-auto mt-2 mt-md-0"
        color="transparent"
        :width="buttonsWidth"
        :min-width="minButtonsWidth"
        :max-width="maxButtonsWidth"
        flat
        v-bind="attrs"
        v-on="on">
        <v-btn
          :width="buttonsWidth"
          :min-width="minButtonsWidth"
          :max-width="maxButtonsWidth"
          outlined
          disabled
          color="primary"
          depressed
          dark>
          <span class="text-truncate position-absolute full-width">
            {{ $t('moveInProgress') }}
          </span>
        </v-btn>
      </v-card>
    </template>
    <span>{{ $t('moveInProgress') }}</span>
  </v-tooltip>
  <v-tooltip
    v-else-if="stopping"
    z-index="4"
    max-width="300px"
    bottom>
    <template #activator="{ on, attrs }">
      <v-card
        class="mx-auto mt-2 mt-md-0"
        color="transparent"
        :width="buttonsWidth"
        :min-width="minButtonsWidth"
        :max-width="maxButtonsWidth"
        flat
        v-bind="attrs"
        v-on="on">
        <v-btn
          :width="buttonsWidth"
          :min-width="minButtonsWidth"
          :max-width="maxButtonsWidth"
          outlined
          disabled
          color="primary"
          depressed
          dark>
          <span class="text-truncate position-absolute full-width">
            {{ $t('moveOutInProgress') }}
          </span>
        </v-btn>
      </v-card>
    </template>
    <span>{{ $t('moveOutInProgress') }}</span>
  </v-tooltip>
</template>
<script>
export default {
  props: {
    lease: {
      type: Object,
      default: null,
    },
    owner: {
      type: Boolean,
      default: false,
    },
    hasRentOffers: {
      type: Boolean,
      default: false,
    },
    confirmed: {
      type: Boolean,
      default: false,
    },
    hasPaidCurrentPeriod: {
      type: Boolean,
      default: false,
    },
    minButtonsWidth: {
      type: String,
      default: () => '150px',
    },
    maxButtonsWidth: {
      type: String,
      default: () => '250px',
    },
    buttonsWidth: {
      type: String,
      default: () => '100%',
    },
  },
  data: () => ({
    loadingMoveDeed: false,
    deedInfo: {},
  }),
  computed: Vuex.mapState({
    authenticated: state => state.authenticated,
    nftId() {
      return this.lease?.nftId;
    },
    disableButtons() {
      return !this.owner && (!this.hasPaidCurrentPeriod || !this.confirmed);
    },
    cardType() {
      return this.lease?.cardType?.toUpperCase() || '';
    },
    ownerAccess() {
      return this.owner && !this.confirmed;
    },
    tenantAccess() {
      return !this.owner && this.confirmed;
    },
    stopped() {
      return (this.ownerAccess || this.tenantAccess) && (!this.provisioningStatus || this.provisioningStatus === 'STOP_CONFIRMED');
    },
    started() {
      return (this.ownerAccess || this.tenantAccess) && this.provisioningStatus === 'START_CONFIRMED';
    },
    starting() {
      return (this.ownerAccess || this.tenantAccess) && this.provisioningStatus === 'START_IN_PROGRESS';
    },
    stopping() {
      return (this.ownerAccess || this.tenantAccess) && this.provisioningStatus === 'STOP_IN_PROGRESS';
    },
    provisioningStatus() {
      return this.deedInfo?.provisioningStatus || this.lease?.provisioningStatus;
    },
    tenantStatus() {
      return this.deedInfo?.status || this.lease?.tenantStatus;
    },
    moveOutDescription() {
      return this.hasRentOffers && this.$t('deedMoveOutOfferWarningTooltip') || this.$t('deedMoveOutDescription');
    },
    moveInDescription() {
      return this.hasRentOffers && this.$t('deedMoveInOfferWarningTooltip') || this.$t('deedMoveInDescription');
    },
  }),
  watch: {
    provisioningStatus: {
      immediate: true,
      handler() {
        this.$emit('provisioning-status', this.provisioningStatus);
      },
    },
    tenantStatus: {
      immediate: true,
      handler() {
        this.$emit('tenant-status', this.tenantStatus);
      },
    },
  },
  created() {
    this.$root.$on('nft-status-changed', this.handleStatusChanged);
    document.addEventListener('nft-tenant-provisioning-changed', this.handleBlockchainStatusChanged);
    this.init();
  },
  beforeDestroy() {
    this.$root.$off('nft-status-changed', this.handleStatusChanged);
    document.removeEventListener('nft-tenant-provisioning-changed', this.handleBlockchainStatusChanged);
  },
  methods: {
    init() {
      this.$store.commit('installProvisioningListeners');
      if (this.lease?.deedInfo?.then) { // Promise loading in progress
        this.lease?.deedInfo.then(deedInfo => this.deedInfo = deedInfo);
      } else if (this.lease?.deedInfo) {
        this.deedInfo = this.lease?.deedInfo;
      } else {
        this.refreshDeedInfo();
      }
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
        return this.$tenantManagement.getTenantInfo(this.nftId)
          .then(deedInfo => this.deedInfo = deedInfo)
          .catch(() => this.$emit('logout', true));
      }
    },
  },
};
</script>