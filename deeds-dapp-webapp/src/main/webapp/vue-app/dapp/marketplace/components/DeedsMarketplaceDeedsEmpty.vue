<template>
  <v-card class="d-flex flex-column justify-center align-center" flat>
    <v-card
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
      <div class="pa-0">{{ $t('dapp.marketplace.deedsListEmptyTitle') }}</div>
    </v-card>
  </v-card>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
  }),
};
</script>