<template>
  <v-card :loading="loading" flat>
    <v-list-item two-line class="px-0">
      <v-list-item-content>
        <v-list-item-title class="text-subtitle-1 font-weight-bold">{{ $t('wom.hubRewardSelfReports') }}</v-list-item-title>
        <v-list-item-subtitle>{{ $t('wom.hubRewardSelfReportsSummary') }}</v-list-item-subtitle>
      </v-list-item-content>
    </v-list-item>
    <v-data-iterator
      :items="reports"
      item-key="rewardId"
      disable-pagination
      disable-filtering
      disable-sort
      hide-default-footer>
      <template #no-data>
        <div class="align-center justify-center text-subtitle dark-grey-color">
          <v-icon
            size="36"
            color="secondary"
            class="me-4">
            fa-cloud-upload-alt
          </v-icon>
          {{ $t('wom.noHubSelfReports') }}
        </div>
      </template>
      <template #default="props">
        <v-row class="mx-n3 border-box-sizing">
          <template v-for="report in props.items">
            <v-col
              v-if="report.reportId && (!report.hidden || report.reportId === expandedReportId)"
              :key="report.reportId"
              cols="auto"
              class="flex-grow-1">
              <v-card max-width="370" flat>
                <deeds-hub-details-reward-item
                  :key="`report-${report.reportId}`"
                  :report="report"
                  :reports="reports"
                  :expand="expandedReportId === report.reportId"
                  @expand="expandedReportId = report.reportId"
                  @collapse="expandedReportId = null" />
              </v-card>
            </v-col>
          </template>
          <v-col
            cols="auto"
            class="flex-grow-1 py-0">
            <v-card min-width="370" flat />
          </v-col>
        </v-row>
      </template>
    </v-data-iterator>
  </v-card>
</template>
<script>
export default {
  props: {
    hub: {
      type: Object,
      default: null,
    },
    reports: {
      type: Object,
      default: null,
    },
    loading: {
      type: Boolean,
      default: false,
    },
    selectedReport: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    expandedReportId: null,
  }),
  computed: Vuex.mapState({
    hubAddress() {
      return this.hub?.address;
    },
  }),
  watch: {
    reports() {
      if (this.selectedReport) {
        this.expandedReportId = this.selectedReport.reportId;
      }
    },
    selectedReport() {
      if (this.selectedReport) {
        this.expandedReportId = this.selectedReport.reportId;
      } else {
        this.expandedReportId = null;
      }
    },
    expandedReportId() {
      this.$emit('report-expanded', this.expandedReportId);
      this.$utils.refreshHubUrl(this.hub?.address, this.expandedReportId || null);
      if (this.expandedReportId) {
        document.body.parentElement.style.overflow = 'hidden';
      } else {
        document.body.parentElement.style.overflow = '';
      }
    },
  },
};
</script>