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
          :href="`${openSeaBaseLink}/${nftId}/sell`"
          :width="buttonsWidth"
          :min-width="minButtonsWidth"
          :max-width="maxButtonsWidth"
          target="_blank"
          rel="nofollow noreferrer noopener"
          class="ms-auto"
          outlined
          depressed
          @click="openRentDetails">
          <span class="text-truncate position-absolute full-width text-capitalize text-center">
            <v-icon class="me-2" small>fa-external-link</v-icon>
            {{ $t('deedSellButton') }}
          </span>
        </v-btn>
      </v-card>
    </template>
    <span>{{ $t('deedSellDescription') }}</span>
  </v-tooltip>
</template>
<script>
export default {
  props: {
    nftId: {
      type: String,
      default: null,
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
    openSeaBaseLink: state => state.openSeaBaseLink,
  }),
};
</script>