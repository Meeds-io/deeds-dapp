<template>
  <v-list-item class="pa-0 my-n2">
    <v-list-item-content class="py-0">
      {{ $t('deedRentingRentedTenantStatus') }}
    </v-list-item-content>
    <v-list-item-action-text class="d-flex py-0">
      <deeds-owned-item-status
        v-if="confirmed"
        :nft="nft" />
      <template v-else>
        {{ $t('waitingForConfirmation') }}
      </template>
    </v-list-item-action-text>
  </v-list-item>
</template>
<script>
export default {
  props: {
    confirmed: {
      type: Boolean,
      default: false,
    },
    nft: {
      type: Object,
      default: null,
    },
  },
};
</script>