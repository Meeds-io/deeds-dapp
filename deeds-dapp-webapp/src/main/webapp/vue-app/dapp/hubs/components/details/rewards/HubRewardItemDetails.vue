<template>
  <div class="d-flex flex-wrap me-n4">
    <deeds-hub-details-reward-item-detail-card
      :value="report.usersCount"
      :previous-value="previousReport.usersCount"
      label="uem.members"
      class="me-auto mb-4" />
    <deeds-hub-details-reward-item-detail-card
      :value="report.participantsCount"
      :previous-value="previousReport.participantsCount"
      label="wom.participantsCount"
      class="me-auto mb-4" />
    <deeds-hub-details-reward-item-detail-card
      :value="report.actionsCount"
      :previous-value="previousReport.actionsCount"
      label="wom.actionsCount"
      class="me-auto mb-4" />
    <deeds-hub-details-reward-item-detail-card
      :value="report.achievementsCount"
      :previous-value="previousReport.achievementsCount"
      label="wom.achievementsCount"
      class="me-auto mb-4" />
    <deeds-hub-details-reward-item-detail-card
      :value="report.recipientsCount"
      :previous-value="previousReport.recipientsCount"
      label="wom.recipientsCount"
      class="me-auto mb-4" />
    <deeds-hub-details-reward-item-detail-card
      :value="report.hubRewardAmount"
      :previous-value="previousReport.hubRewardAmount"
      :label="hubRewardsPerPeriod"
      class="me-auto mb-4" />
  </div>
</template>
<script>
export default {
  props: {
    report: {
      type: Object,
      default: null,
    },
    reports: {
      type: Array,
      default: null,
    },
    reward: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    dateFormat: {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    },
  }),
  computed: Vuex.mapState({
    language: state => state.language,
    parentLocation: state => state.parentLocation,
    previousReport() {
      if (this.reports?.length && this.report) {
        const previousReports = this.reports.slice().filter(r => r.rewardId < this.report.rewardId);
        if (previousReports.length > 0) {
          previousReports.sort((r1, r2) => r2.rewardId - r1.rewardId);
          return previousReports[0];
        }
      }
      return {};
    },
    hubRewardsPeriodType() {
      return this.report?.periodType?.toLowerCase();
    },
    hubRewardsPeriod() {
      return this.hubRewardsPeriodType && this.$t(`wom.${this.hubRewardsPeriodType}`);
    },
    hubRewardsPerPeriod() {
      return this.hubRewardsPeriodType && this.$t('wom.rewardsPer', {0: this.hubRewardsPeriod}) || 'wom.rewards';
    },
  }),
};
</script>