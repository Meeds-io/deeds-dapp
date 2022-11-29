<template>
  <v-list-item class="pa-0 my-n2">
    <v-list-item-content
      v-if="endingLease"
      class="py-0 mt-n4">
      <div class="mt-4">
        <v-badge
          color="transparent"
          bordered>
          <template #badge>
            <v-tooltip
              z-index="4"
              top
              left>
              <template #activator="{ on, attrs }">
                <v-btn
                  height="16px"
                  width="16px"
                  icon
                  class="error position-relative-1"
                  dark
                  v-bind="attrs"
                  v-on="on">
                  <v-icon size="10">fas fa-info</v-icon>
                </v-btn>
              </template>
              <span>{{ $t('endRentalTransactionInProgressTooltip') }}</span>
            </v-tooltip>
          </template>
          {{ $t('deedRentingEndDate') }}
        </v-badge>
      </div>
    </v-list-item-content>
    <v-list-item-content v-else class="py-0">
      {{ $t('deedRentingEndDate') }}
    </v-list-item-content>
    <v-list-item-action-text class="py-0">
      <deeds-date-format
        v-if="endDate"
        :value="endDate" />
      <template v-else>
        {{ $t('waitingForConfirmation') }}
      </template>
    </v-list-item-action-text>
  </v-list-item>
</template>
<script>
export default {
  props: {
    endingLease: {
      type: Boolean,
      default: false,
    },
    endDate: {
      type: String,
      default: null,
    },
  },
};
</script>