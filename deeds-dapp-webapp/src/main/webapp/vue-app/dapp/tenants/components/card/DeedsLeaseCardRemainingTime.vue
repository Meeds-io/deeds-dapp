<template>
  <v-list-item class="pa-0 my-n2">
    <v-list-item-content class="py-0">
      {{ $t('deedRentingRemainingTime') }}
    </v-list-item-content>
    <v-list-item-action-text class="py-0">
      {{ remainingTimeLabel || $t('waitingForConfirmation') }}
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
    endDate: {
      type: String,
      default: null,
    },
  },
  computed: Vuex.mapState({
    MONTH_IN_SECONDS: state => state.MONTH_IN_SECONDS,
    DAY_IN_SECONDS: state => state.DAY_IN_SECONDS,
    monthInMillis() {
      return this.MONTH_IN_SECONDS * 1000;
    },
    dayInMillis() {
      return this.DAY_IN_SECONDS * 1000;
    },
    remainingTimeLabel() {
      if (!this.confirmed || !this.endDate) {
        return null;
      }
      const endDate = new Date(this.endDate);
      const now = new Date();
      let months;
      let days;
      if (this.MONTH_IN_SECONDS < 2629800) {
        months = parseInt((endDate.getTime() - now.getTime()) / this.monthInMillis);
        days = parseInt((endDate.getTime() - now.getTime() - (months * this.monthInMillis)) / this.dayInMillis);
      } else {
        months = (endDate.getYear() * 12 + endDate.getMonth()) - (now.getYear() * 12 + now.getMonth());
        days = endDate.getDate() - now.getDate();
        if (days < 0) {
          months--;
          const endDateMillis = endDate.getTime();
          endDate.setMonth(endDate.getMonth() - 1);
          days = (endDateMillis - endDate.getTime()) / 1000 / 60 / 60 / 24 + days;
        }
      }
      let monthsLabel = '';
      if (months === 1) {
        monthsLabel = this.$t('deedRentingRemainingTimeOneMonth');
      } else if (months > 0) {
        monthsLabel = this.$t('deedRentingRemainingTimeMonths', {0: months});
      }
      let daysLabel = '';
      if (days === 1) {
        daysLabel = this.$t('deedRentingRemainingTimeOneDay');
      } else if (days > 0) {
        daysLabel = this.$t('deedRentingRemainingTimeDays', {0: days});
      }
      return `${monthsLabel} ${daysLabel}`;
    },
  }),
};
</script>