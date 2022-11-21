<template>
  <v-list-item class="pa-0 my-n2">
    <v-list-item-content class="py-0">
      {{ $t('deedRentingStartDate') }}
    </v-list-item-content>
    <v-list-item-action-text class="py-0">
      <deeds-date-format
        v-if="startDate"
        :value="startDate" />
      <template v-else>
        {{ $t('waitingForConfirmation') }}
      </template>
    </v-list-item-action-text>
  </v-list-item>
</template>
<script>
export default {
  props: {
    startDate: {
      type: Object,
      default: null,
    },
  },
};
</script>