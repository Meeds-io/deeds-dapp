<!--
  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 3 of the License, or (at your option) any later version.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
<template>
  <v-card
    v-if="hub"
    class="pa-0 pa-sm-4"
    flat>
    <deeds-hub-details-deed-card-topbar />
    <deeds-hub-not-found
      v-if="hub.notFound"
      :address="hub.address" />
    <deeds-hub-report-not-found
      v-else-if="hub.reportNotFound"
      :report-id="hub.reportId" />
    <template v-else>
      <div class="d-flex flex-wrap align-center justify-center">
        <deeds-hub-card
          :hub="hub"
          class="col-sm-12 col-md-4 pa-0 ma-2 hidden-xs-only"
          standalone />
        <deeds-hub-card-chart
          :hub="hub"
          :reports="reports"
          class="col-sm-12 col-md-4 pa-0 ma-2" />
      </div>
      <deeds-hub-details-rewards
        :hub="hub"
        :reports="reports"
        :selected-report="selectedReport"
        :loading="loading"
        @report-expanded="selectReport" />
    </template>
  </v-card>
</template>
<script>
export default {
  props: {
    hub: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    isManager: false,
    loading: false,
    initialized: false,
    reports: [],
    selectedReport: null,
  }),
  computed: Vuex.mapState({
    isMobile: state => state.isMobile,
    deedId() {
      return this.hub?.deedId;
    },
  }),
  watch: {
    hub() {
      if (this.hub) {
        this.retrieveReports();
      }
      if (this.initialized) {
        document.body.parentElement.scrollTo({
          top: 0
        });
        this.refreshUrl();
      }
    },
  },
  created() {
    if (!this.hub) {
      Promise.resolve(this.init())
        .then(() => this.$nextTick())
        .finally(() => this.initialized = true);
    }
  },
  methods: {
    init() {
      const hubAddress = this.$utils.getQueryParam('address');
      const hubReportId = this.$utils.getQueryParam('report');
      if (hubReportId) {
        return this.$hubReportService.getReport(hubReportId)
          .then(report => {
            if (report?.hubAddress) {
              this.selectedReport = report;
              return this.refresh(report?.hubAddress, hubReportId);
            } else {
              this.$root.$emit('report-not-found', hubReportId);
            }
          })
          .catch(() => this.$root.$emit('report-not-found', hubReportId))
          .finally(() => this.loading = false);
      } else if (hubAddress) {
        return this.refresh(hubAddress);
      } else {
        this.$root.$emit('close-hub-details');
      }
    },
    retrieveReports() {
      this.loading = true;
      return this.$hubReportService.getReports({
        hubAddress: this.hub?.address,
        page: 0,
        size: 100,
      })
        .then(data => this.reports = data?._embedded?.reports || [])
        .finally(() => this.loading = false);
    },
    refresh(hubAddress, hubReportId) {
      this.loading = true;
      return this.$hubService.getHub(hubAddress)
        .then(hub => {
          if (hub) {
            this.$root.$emit('open-hub-details', hub);
          } else if (hubReportId) {
            this.$root.$emit('report-not-found', hubReportId);
          } else {
            this.$root.$emit('hub-not-found', hubAddress);
          }
        })
        .catch(() => {
          if (hubReportId) {
            this.$root.$emit('report-not-found', hubReportId);
          } else {
            this.$root.$emit('hub-not-found', hubAddress);
          }
        })
        .finally(() => this.loading = false);
    },
    selectReport(reportId) {
      if (reportId !== this.selectedReport?.reportId) {
        this.selectedReport = null;
      }
    },
    refreshUrl() {
      if (!this.hub || (!this.hub.upcomingDeedId && !this.hub.notFound && !this.hub.reportNotFound)) {
        this.$utils.refreshHubUrl(this.hub?.address);
      }
    },
  },
};
</script>
