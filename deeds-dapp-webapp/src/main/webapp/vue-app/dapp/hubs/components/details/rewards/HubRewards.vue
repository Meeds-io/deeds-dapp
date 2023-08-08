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
      item-key="id"
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
              v-if="report.hash && (!report.hidden || report.hash === expandedHash)"
              :key="report.hash"
              cols="12"
              sm="12"
              md="6"
              lg="4">
              <deeds-hub-details-reward-item
                :key="`report-${report.hash}`"
                :report="report"
                :expand="expandedHash === report.hash"
                @refresh="refreshReport"
                @expand="expandReport(report.hash)"
                @collapse="expandReport(null)" />
            </v-col>
          </template>
        </v-row>
      </template>
    </v-data-iterator>
    <v-btn
      v-if="hasMore"
      class="btn"
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
    expandedHash: null,
  }),
  computed: Vuex.mapState({
    hubAddress() {
      return this.hub?.address;
    },
    expandedReportIndex() {
      return this.expandedHash && this.reports.findIndex(r => r.hash === this.expandedHash) || -1;
    },
  }),
  watch: {
    expandedHash() {
      if (this.expandedHash && this.expandedReportIndex < 0) {
        this.refreshReportByHash(this.expandedHash);
      }
      this.$utils.refreshHubUrl(this.hub?.address, this.expandedHash || null);
    },
  },
  created() {
    this.init();
  },
  methods: {
    init() {
      return this.loadMore()
        .then(() => this.expandReport(this.selectedReport?.hash));
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
    expandReport(reportHash) {
      this.expandedHash = reportHash;
      this.$emit('report-expanded', reportHash);
    },
    refreshReportByHash(reportHash) {
      return this.$hubReportService.getReport(reportHash)
        .then(report => {
          if (this.hub?.address === report?.hubRewardReport?.hubAddress) {
            this.refreshReport(report);
          } else {
            this.$root.$emit('report-not-found', reportHash);
          }
        })
        .finally(() => this.loading = false);
    },
    refreshReport(report) {
      if (!report) {
        return;
      }
      const index = this.reports.findIndex(r => report.hash === r.hash);
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