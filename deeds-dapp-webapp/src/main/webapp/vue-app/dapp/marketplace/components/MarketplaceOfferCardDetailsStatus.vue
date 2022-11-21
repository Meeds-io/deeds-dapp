<template>
  <div>
    <v-alert
      v-if="isAcquisitionInProgress"
      type="warning"
      outlined>
      {{ $t('deedOfferAcquisitionInProgress', {0 : acquisitionInProgress}) }}
    </v-alert>
    <v-alert
      v-if="!isTransactionValidated || isDeletion || isUpdate"
      :type="alertType"
      outlined>
      {{ alertMessage }}
      <v-btn
        v-if="modificationLink"
        :href="modificationLink"
        text>
        {{ $t('deedRentingOfferSeeUpdatesLink') }}
      </v-btn>
    </v-alert>
    <v-alert
      v-if="isRestrictedTenant"
      type="info"
      outlined>
      <div v-html="$t('deedOfferAssignedTo', {0: `<strong class='${blackThemeColor}--text'>${tenantAddressAlias}</strong>`})"></div>
    </v-alert>
  </div>
</template>
<script>
export default {
  props: {
    offer: {
      type: Object,
      default: null,
    },
  },
  computed: Vuex.mapState({
    blackThemeColor: state => state.blackThemeColor,
    displayAlert() {
      return this.isCreation || this.isDeletion || this.isUpdate || this.isTransactionError || this.isTransactionPending;
    },
    isCreation() {
      return !this.offer.offerId;
    },
    isDeletion() {
      return this.offer.deleteId;
    },
    isUpdate() {
      return this.offer.updateId;
    },
    modificationLink() {
      return this.offer.updateId && `${window.location.pathname}?offer=${this.offer.updateId}`;
    },
    acquisitionInProgress() {
      return this.offer?.acquisitionIds?.length || 0;
    },
    isAcquisitionInProgress() {
      return this.acquisitionInProgress > 0;
    },
    isTransactionPending() {
      return this.offer.offerTransactionStatus === 'IN_PROGRESS';
    },
    isTransactionError() {
      return this.offer.offerTransactionStatus === 'ERROR';
    },
    isTransactionValidated() {
      return this.offer.offerTransactionStatus === 'VALIDATED';
    },
    isRestrictedTenant() {
      return this.offer.hostAddress;
    },
    tenantAddress() {
      return this.offer.hostAddress;
    },
    tenantAddressAlias() {
      return this.tenantAddress && `${this.tenantAddress.substring(0, 7)}...${this.tenantAddress.substring(this.tenantAddress.length - 3)}`;
    },
    alertType() {
      return this.isTransactionError && 'error' || 'info';
    },
    alertMessage() {
      if (this.isCreation) {
        return this.isTransactionError && this.$t('deedRentingOfferCreationError') || this.$t('deedRentingOfferCreationInProgress');
      } else if (this.isDeletion) {
        return this.$t('deedRentingOfferDeletionInProgress');
      } else if (this.hasUpdateInProgress) {
        return this.$t('deedRentingOfferUpdateInProgress');
      } else {
        return this.isTransactionError && this.$t('deedRentingOfferUpdateError') || this.$t('deedRentingOfferUpdateInProgress');
      }
    },
  }),
};
</script>