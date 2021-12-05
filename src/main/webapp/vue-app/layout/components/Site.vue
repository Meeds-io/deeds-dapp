<template>
  <v-app>
    <v-card flat>
      <deeds-topbar class="mainPageLayout" role="banner" />
      <v-divider />
      <div v-if="!loading" class="mainPageLayout pa-4">
        <deeds-metamask />
        <deeds-navbar v-if="address" role="navigation" />
        <deeds-page
          v-if="address"
          class="mt-8"
          role="main" />
      </div>
      <deeds-notifications />
    </v-card>
  </v-app>
</template>
<script>
export default {
  computed: Vuex.mapState({
    address: state => state.address,
    loading: state => state.appLoading,
  }),
};
</script>