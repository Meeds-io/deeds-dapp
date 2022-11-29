<template>
  <v-list-item class="pa-0 my-n2">
    <v-list-item-content class="py-0">
      {{ $t('deedRentingHubStatus') }}
    </v-list-item-content>
    <v-list-item-action-text class="d-flex py-0">
      {{ rented && $t('deedRentingHubStatusUsed') || $t('deedRentingHubStatusFreeToUse') }}
    </v-list-item-action-text>
  </v-list-item>
</template>
<script>
export default {
  props: {
    rented: {
      type: Boolean,
      default: false,
    },
  },
};
</script>