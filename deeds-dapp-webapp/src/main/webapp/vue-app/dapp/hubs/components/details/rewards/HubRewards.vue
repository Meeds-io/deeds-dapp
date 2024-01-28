<template>
  <v-card :loading="loading" flat>
    <v-list-item two-line>
      <v-list-item-content>
        <v-list-item-title class="text-subtitle-1 font-weight-bold">{{ $t('wom.hubRewardSelfReports') }}</v-list-item-title>
        <v-list-item-subtitle>{{ $t('wom.hubRewardSelfReportsSummary') }}</v-list-item-subtitle>
      </v-list-item-content>
    </v-list-item>
    <v-data-iterator
      :items="reports"
      :loading="loading"
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
        <v-row class="mx-0 border-box-sizing">
          <template v-for="report in props.items">
            <v-col
              v-if="report.reportId && (!report.hidden || report.reportId === expandedReportId)"
              :key="report.reportId"
              cols="12"
              sm="12"
              md="6"
              lg="4">
              <deeds-hub-details-reward-item
                :key="`report-${report.reportId}`"
                :report="report"
                :expand="expandedReportId === report.reportId"
                @refresh="refreshReport"
                @expand="expandReport(report.reportId)"
                @collapse="expandReport(null)" />
            </v-col>
          </template>
        </v-row>
      </template>
    </v-data-iterator>
    <v-btn
      v-if="hasMore"
      class="mx-3 my-4 btn"
      elevation="0"
      outlined
      block
      @click="loadMore">
      {{ $t('wom.loadMore') }}
    </v-btn>
  </v-card>
</template>
<script>
export default {
  props: {
    hub: {
      type: Object,
      default: null,
    },
    selectedReport: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    reports: [],
    loading: false,
    pageSize: 10,
    page: -1,
    hasMore: false,
    expandedReportId: null,
  }),
  computed: Vuex.mapState({
    hubAddress() {
      return this.hub?.address;
    },
    expandedReportIndex() {
      return this.expandedReportId && this.reports.findIndex(r => r.reportId === this.expandedReportId) || -1;
    },
  }),
  watch: {
    expandedReportId() {
      if (this.expandedReportId && this.expandedReportIndex < 0) {
        this.refreshReportById(this.expandedReportId);
      }
      this.$utils.refreshHubUrl(this.hub?.address, this.expandedReportId || null);
    },
  },
  created() {
    this.init();
  },
  methods: {
    init() {
      return this.loadMore()
        .then(() => this.expandReport(this.selectedReport?.reportId));
    },
    loadMore() {
      this.page++;
      this.loading = true;
      return this.$hubReportService.getReports({
        hubAddress: this.hubAddress,
        page: this.page,
        size: this.pageSize,
      })
        .then(data => {
          if (data?._embedded?.reports?.length) {
            this.reports.push(...data._embedded.reports);
            this.hasMore = data.page.totalPages > (this.page + 1);
          } else {
            this.hasMore = false;
          }
        })
        .finally(() => this.loading = false);
    },
    expandReport(reportId) {
      this.expandedReportId = reportId;
      this.$emit('report-expanded', reportId);
    },
    refreshReportById(reportId) {
      return this.$hubReportService.getReport(reportId)
        .then(report => {
          if (this.hub?.address === report?.hubAddress) {
            this.refreshReport(report);
          } else {
            this.$root.$emit('report-not-found', reportId);
          }
        })
        .finally(() => this.loading = false);
    },
    refreshReport(report) {
      if (!report) {
        return;
      }
      const index = this.reports.findIndex(r => report.reportId === r.reportId);
      if (index >= 0) {
        this.reports.splice(index, 1, report);
      } else {
        report.hidden = true;
        this.reports.push(report);
      }
      this.reports = this.reports.slice();
    },
  },
};
</script>