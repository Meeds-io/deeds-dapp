<template>
  <v-list-item class="pa-0 my-n2">
    <v-list-item-content class="py-0">
      {{ $t('deedRentingRewardDistribution') }}
    </v-list-item-content>
    <v-list-item-action-text class="d-flex py-0">
      <v-card min-width="50" flat>
        <v-tooltip z-index="4" bottom>
          <template #activator="{ on, attrs }">
            <v-progress-linear
              :value="tenantMintingPercentage"
              color="success"
              background-color="error"
              height="6"
              rounded
              v-bind="attrs"
              v-on="on" />
          </template>
          <span>{{ $t('deedMintingPercentageDetails', {
            0: tenantMintingPercentage,
            1: ownerMintingPercentage || 0,
          }) }}</span>
        </v-tooltip>
      </v-card>
    </v-list-item-action-text>
  </v-list-item>
</template>
<script>
export default {
  props: {
    ownerMintingPercentage: {
      type: Number,
      default: () => 0,
    },
  },
  computed: Vuex.mapState({
    tenantMintingPercentage() {
      return 100 - (this.ownerMintingPercentage || 0);
    },
  }),
};
</script>