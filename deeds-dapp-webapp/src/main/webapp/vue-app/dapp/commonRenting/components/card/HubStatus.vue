<template>
  <v-list-item class="pa-0 my-n2">
    <v-list-item-content class="py-0">
      {{ $t('deedRentingRentedTenantStatus') }}
    </v-list-item-content>
    <v-list-item-action-text class="d-flex py-0">
      <template v-if="confirmed || owner">
        <a
          v-if="started || starting || stopping"
          :href="deedTenantLink"
          target="_blank"
          rel="nofollow noreferrer noopener">
          <span v-if="starting" class="text-end">{{ $t('tenantDeployTransactionInProgress') }}</span>
          <span v-else-if="stopping" class="text-end">{{ $t('tenantUndeployTransactionInProgress') }}</span>
          <span v-else-if="beingPrepared" class="text-end">{{ $t('tenantBeingPrepared') }}</span>
          <span v-else class="text-lowercase">{{ deedTenantLinkLabel }}</span>
        </a>
        <div v-else-if="beingStopped" class="text-end">
          {{ $t('tenantBeingStopped') }}
        </div>
        <div v-else-if="stopped" class="text-end">
          {{ isProvisioningManager && $t('deedTenantNotStartedYet') || $t('vacant') }}
        </div>
        <div v-else>-</div>
      </template>
      <template v-else>
        {{ $t('waitingForConfirmation') }}
      </template>
    </v-list-item-action-text>
  </v-list-item>
</template>
<script>
export default {
  props: {
    nft: {
      type: Object,
      default: null,
    },
    confirmed: {
      type: Boolean,
      default: false,
    },
    owner: {
      type: Boolean,
      default: false,
    },
  },
  computed: Vuex.mapState({
    cities: state => state.cities,
    nftId() {
      return this.nft?.id;
    },
    status() {
      return this.nft?.status;
    },
    tenantStatus() {
      return this.nft?.tenantStatus;
    },
    transactionHash() {
      return this.nft?.transactionHash;
    },
    loading() {
      return this.nft?.loading || this.transactionHash || !this.status || this.status === 'loading';
    },
    stopped() {
      return this.nft?.stopped || this.status === 'STOPPED';
    },
    started() {
      return this.nft?.started || this.status === 'STARTED';
    },
    starting() {
      return this.nft?.starting && this.loading;
    },
    stopping() {
      return this.nft?.stopping && this.loading;
    },
    beingPrepared() {
      return this.nft?.beingPrepared;
    },
    beingStopped() {
      return this.nft?.beingStopped;
    },
    cityIndex() {
      return this.nft?.cityIndex;
    },
    cityName() {
      return this.nft?.cityName || this.cities[this.cityIndex];
    },
    isProvisioningManager() {
      return this.nft?.isProvisioningManager;
    },
    deedTenantLink() {
      return this.cityName && `https://${this.cityName.toLowerCase()}-${this.nftId}.meeds.io`;
    },
    deedTenantLinkLabel() {
      return this.cityName && `${this.cityName.toLowerCase()}-${this.nftId}.meeds.io`;
    },
  }),
};
</script>