<template>
  <v-btn
    v-if="accessButtonDisabled"
    :width="buttonsWidth"
    :min-width="minButtonsWidth"
    :max-width="maxButtonsWidth"
    class="primary mx-auto"
    disabled
    depressed
    dark>
    <v-icon class="me-2" small>fa-external-link</v-icon>
    {{ $t('deedTenantAccessButton') }}
  </v-btn>
  <v-btn
    v-else
    :href="deedTenantLink"
    :width="buttonsWidth"
    :min-width="minButtonsWidth"
    :max-width="maxButtonsWidth"
    color="primary"
    rel="nofollow noreferrer noopener"
    target="_blank"
    class="mx-auto"
    depressed
    dark>
    <v-icon class="me-2" small>fa-external-link</v-icon>
    {{ $t('deedTenantAccessButton') }}
  </v-btn>
</template>
<script>
export default {
  props: {
    nftId: {
      type: String,
      default: null,
    },
    city: {
      type: String,
      default: null,
    },
    started: {
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
  computed: Vuex.mapState({
    accessButtonDisabled() {
      return !this.started
        || !this.hasPaidCurrentPeriod;
    },
    deedTenantLink() {
      return `https://${this.city}-${this.nftId}.wom.meeds.io`;
    },
  }),
};
</script>