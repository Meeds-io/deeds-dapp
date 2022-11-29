<template>
  <v-list-item class="pa-0 my-n2">
    <v-list-item-content
      v-if="noticePeriodStarted"
      class="py-0 mt-n4">
      <div class="mt-4">
        <v-badge color="transparent" bordered>
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
              <span>{{ $t('noticePeriodStartedTooltip') }}</span>
            </v-tooltip>
          </template>
          {{ $t('deedRentingNoticePeriodTitle') }}
        </v-badge>
      </div>
    </v-list-item-content>
    <v-list-item-content v-else class="py-0">
      {{ $t('deedRentingNoticePeriodTitle') }}
    </v-list-item-content>
    <v-list-item-action-text class="py-0">
      <template v-if="!confirmed">
        {{ $t('waitingForConfirmation') }}
      </template>
      <deeds-date-format
        v-else-if="hasNoticePeriod"
        :value="noticePeriodDate" />
      <template v-else>
        {{ $t('deedRentingNoNoticePeriodLabel') }}
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
    rentPeriodMonths: {
      type: String,
      default: null,
    },
    noticePeriodMonths: {
      type: String,
      default: null,
    },
    startDate: {
      type: String,
      default: null,
    },
    endDate: {
      type: String,
      default: null,
    },
  },
  computed: Vuex.mapState({
    hasNoticePeriod() {
      if (!this.noticePeriodDate) {
        return false;
      }
      return this.noticePeriodDate < this.rentalEndDate;
    },
    rentalEndDate() {
      return this.endDate && new Date(this.endDate).getTime() || 0;
    },
    rentalStartDate() {
      return this.startDate && new Date(this.startDate).getTime() || 0;
    },
    noticePeriodStarted() {
      if (!this.noticePeriodDate) {
        return false;
      }
      return this.rentalEndDate > Date.now()
          && this.noticePeriodDate < Date.now();
    },
    noticePeriodDate() {
      if (!this.startDate || !this.endDate) {
        return 0;
      }
      return parseInt(this.rentalEndDate
          - (this.rentalEndDate - this.rentalStartDate)
          * (this.noticePeriodMonths / this.rentPeriodMonths));
    },
  }),
};
</script>