<template>
  <v-toolbar elevation="0">
    <v-toolbar-title class="d-flex">
      <v-img
        max-height="64px"
        max-width="64px"
        src="../images/meeds.png"
        contain />
      <div class="ps-2 pb-1">{{ $t('dao') }}</div>
    </v-toolbar-title>
    <v-spacer />
    <div class="mx-2">
      <deeds-topbar-address-selector v-if="address" />
    </div>
    <div class="mx-2">
      <deeds-topbar-fiat-currency-selector v-if="address" />
    </div>
    <div class="mx-2">
      <deeds-topbar-language-selector />
    </div>
  </v-toolbar>
</template>
<script>
export default {
  computed: Vuex.mapState(['isMetamaskInstalled', 'address']),
};
</script>