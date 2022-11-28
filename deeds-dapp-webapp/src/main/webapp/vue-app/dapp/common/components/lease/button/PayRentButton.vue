<template>
  <v-tooltip bottom>
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
          :disabled="disabled"
          :width="buttonsWidth"
          :min-width="minButtonsWidth"
          :max-width="maxButtonsWidth"
          color="primary"
          outlined
          depressed
          dark
          @click="$root.$emit('deeds-rent-pay-drawer', lease)">
          <span class="text-truncate position-absolute full-width text-capitalize">
            {{ payRentButtonLabel }}
          </span>
        </v-btn>
      </v-card>
    </template>
    <span>{{ payRentTooltip }}</span>
  </v-tooltip>
</template>
<script>
export default {
  props: {
    lease: {
      type: Boolean,
      default: false,
    },
    confirmed: {
      type: Boolean,
      default: false,
    },
    hasPaymentInProgress: {
      type: Boolean,
      default: false,
    },
    isLeaseNotFullyPaid: {
      type: Boolean,
      default: false,
    },
    disabled: {
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
  computed: Vuex.mapState({
    payRentButtonLabel() {
      return this.confirmed && (this.hasPaymentInProgress && this.$t('paymentInProgress') || this.$t('payTheRent')) || this.$t('acquisitionInProgress');
    },
    payRentButtonDisabled() {
      return this.hasPaymentInProgress || this.isLeaseNotFullyPaid;
    },
    payRentTooltip() {
      if (!this.isLeaseNotFullyPaid) {
        return this.$t('deedOfferAllRentsPaid');
      } else if (this.hasPaymentInProgress) {
        return this.$t('paymentInProgress');
      } else {
        return this.$t('payTheRent');
      }
    },
  }),
};
</script>