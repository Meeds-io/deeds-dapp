<template>
  <v-card flat>
    <div class="d-flex flex-row">
      <v-card-title class="ps-0 py-0">{{ $t('dapp.marketplace.deedsListTitle') }}</v-card-title>
      <v-divider class="my-auto" />
    </div>
    <deeds-marketplace-deeds-list v-if="hasDeeds" />
    <deeds-marketplace-deeds-empty v-else />
  </v-card>
</template>
<script>
export default {
  data: () => ({
    deeds: [],
  }),
  computed: Vuex.mapState({
    hasDeeds() {
      return this.deeds.length > 0;
    },
  }),
};
</script>