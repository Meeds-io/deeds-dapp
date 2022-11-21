<template>
  <v-btn
    v-if="started"
    :loading="loadingMoveDeed"
    :width="buttonsWidth"
    :min-width="minButtonsWidth"
    :max-width="maxButtonsWidth"
    :disabled="!hasPaidCurrentPeriod"
    :title="$t('moveOut')"
    class="mx-auto mt-2 mt-md-0"
    color="secondary"
    outlined
    depressed
    dark
    @click="openMoveOutDrawer">
    <span class="text-truncate position-absolute full-width text-capitalize">
      {{ $t('moveOut') }}
    </span>
  </v-btn>
  <v-btn
    v-else-if="stopped"
    :width="buttonsWidth"
    :min-width="minButtonsWidth"
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
    <span class="text-truncate position-absolute full-width text-capitalize">
      {{ $t('moveIn') }}
    </span>
  </v-btn>
  <v-btn
    v-else-if="starting"
    :width="buttonsWidth"
    :min-width="minButtonsWidth"
    :max-width="maxButtonsWidth"
    :title="$t('moveInProgress')"
    class="mx-auto mt-2 mt-md-0"
    outlined
    disabled
    color="primary"
    depressed
    dark>
    <span class="text-truncate position-absolute full-width text-capitalize">
      {{ $t('moveInProgress') }}
    </span>
  </v-btn>
  <v-btn
    v-else-if="stopping"
    :width="buttonsWidth"
    :min-width="minButtonsWidth"
    :max-width="maxButtonsWidth"
    :title="$t('moveOutInProgress')"
    class="mx-auto mt-2 mt-md-0"
    outlined
    disabled
    color="primary"
    depressed
    dark>
    <span class="text-truncate position-absolute full-width text-capitalize">
      {{ $t('moveOutInProgress') }}
    </span>
  </v-btn>
</template>
<script>
export default {
  props: {
    lease: {
      type: Object,
      default: null,
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
    cardType() {
      return this.lease?.cardType?.toUpperCase() || '';
    },
    stopped() {
      return this.confirmed && (!this.provisioningStatus || this.provisioningStatus === 'STOP_CONFIRMED');
    },
    started() {
      return this.confirmed && this.provisioningStatus === 'START_CONFIRMED';
    },
    starting() {
      return this.confirmed && this.provisioningStatus === 'START_IN_PROGRESS';
    },
    stopping() {
      return this.confirmed && this.provisioningStatus === 'STOP_IN_PROGRESS';
    },
    provisioningStatus() {
      return this.deedInfo?.provisioningStatus || this.lease?.provisioningStatus;
    },
    tenantStatus() {
      return this.deedInfo?.status || this.lease?.tenantStatus;
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
        return this.$tenantManagement.getTenantInfo(this.nftId)
          .then(deedInfo => this.deedInfo = deedInfo)
          .catch(() => this.$emit('logout', true));
      }
    },
  },
};
</script>