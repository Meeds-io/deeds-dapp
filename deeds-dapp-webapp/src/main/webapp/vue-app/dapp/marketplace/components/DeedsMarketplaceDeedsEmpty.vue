<template>
  <v-card
    min-height="300px"
    class="d-flex flex-column justify-center align-center"
    flat>
    <v-card
      v-if="!loading"
      max-width="400px"
      class="d-flex flex-column align-center text-center mx-0 mx-md-12"
      flat>
      <img
        :src="`${parentLocation}/static/images/deeds.webp`"
        alt=""
        class="mb-8 full-width">
      <template v-if="offerNotAvailable">
        <div class="pa-0">{{ $t('dapp.marketplace.deedsOfferNotAvailableAnyMore') }}</div>
        <div class="pa-0 mt-4" v-html="notAvailableMessage"></div>
      </template>
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
    offerNotAvailable: {
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
    notAvailableMessage() {
      return this.$t('dapp.marketplace.deedsOfferNotAvailableAnyMoreLink', {0: `<a href='${window.location.pathname}'>`, 1: '</a>'});
    },
  }),
};
</script>