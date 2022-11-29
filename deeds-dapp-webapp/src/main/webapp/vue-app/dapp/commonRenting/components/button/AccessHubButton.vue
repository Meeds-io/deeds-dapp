<template>
  <v-tooltip z-index="4" bottom>
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
          v-if="accessButtonDisabled"
          :width="buttonsWidth"
          :min-width="minButtonsWidth"
          :max-width="maxButtonsWidth"
          outlined
          disabled>
          {{ $t('deedTenantAccessButton') }}
        </v-btn>
        <v-btn
          v-else
          :href="deedTenantLink"
          :width="buttonsWidth"
          :min-width="minButtonsWidth"
          :max-width="maxButtonsWidth"
          :outlined="outlined"
          color="primary"
          rel="nofollow noreferrer noopener"
          target="_blank"
          class="mx-auto"
          depressed>
          <v-icon class="me-2" small>fa-external-link</v-icon>
          {{ $t('deedTenantAccessButton') }}
        </v-btn>
      </v-card>
    </template>
    <span>{{ accessButtonTooltip }}</span>
  </v-tooltip>
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
    owner: {
      type: Boolean,
      default: false,
    },
    outlined: {
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
    accessButtonTooltip() {
      return this.accessButtonDisabled
        && this.$t('deedTenantAccessButtonDisabledTooltip')
        || this.$t('deedTenantAccessButtonTooltip');
    },
    accessButtonDisabled() {
      return !this.started
        || (!this.hasPaidCurrentPeriod && !this.owner);
    },
    deedTenantLink() {
      return `https://${this.city}-${this.nftId}.wom.meeds.io`;
    },
  }),
};
</script>