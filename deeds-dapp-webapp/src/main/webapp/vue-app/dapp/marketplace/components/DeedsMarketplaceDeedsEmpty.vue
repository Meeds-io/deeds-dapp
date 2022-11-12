<template>
  <v-card
    min-height="210px"
    class="d-flex flex-column justify-center align-center"
    flat>
    <v-card
      v-if="!loading"
      width="350px"
      max-width="100%"
      class="d-flex flex-column align-center text-center mx-12"
      flat>
      <v-img
        :src="`/${parentLocation}/static/images/deeds.png`"
        max-height="200"
        height="150"
        width="200"
        contain
        eager />
      <div v-if="offerNotFound" class="pa-0">{{ $t('dapp.marketplace.deedsOfferNotFound') }}</div>
      <div v-else-if="hasFilter" class="pa-0">{{ $t('dapp.marketplace.deedsFilterListEmptyTitle') }}</div>
      <div v-else class="pa-0">{{ $t('dapp.marketplace.deedsListEmptyTitle') }}</div>
    </v-card>
  </v-card>
</template>
<script>
export default {
  props: {
    hasFilter: {
      type: Boolean,
      default: false,
    },
    offerNotFound: {
      type: Boolean,
      default: false,
    },
    loading: {
      type: Boolean,
      default: false,
    },
  },
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
  }),
};
</script>