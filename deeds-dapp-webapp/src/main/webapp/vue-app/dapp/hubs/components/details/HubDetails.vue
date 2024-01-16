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
    class="pa-4"
    flat>
    <deeds-hub-details-deed-card-topbar />
    <deeds-hub-not-found
      v-if="hub.notFound"
      :address="hub.address" />
    <deeds-hub-report-not-found
      v-else-if="hub.reportNotFound"
      :hash="hub.reportHash" />
    <template v-else>
      <deeds-hub-details-deed-card
        :hub="hub"
        :is-manager="isManager" />
      <deeds-hub-details-rewards
        :hub="hub"
        :selected-report="selectedReport"
        class="mt-4" />
      <deeds-hub-disconnection-drawer
        ref="disconnectionDrawer"
        :hub="hub" />
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
    initialized: false,
    selectedReport: null,
  }),
  computed: Vuex.mapState({
    address: state => state.address,
    tenantProvisioningContract: state => state.tenantProvisioningContract,
    deedId() {
      return this.hub?.deedId;
    },
  }),
  watch: {
    hub() {
      this.checkManager();
      if (this.initialized) {
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
      const hubReportHash = this.$utils.getQueryParam('report');
      if (hubReportHash) {
        return this.$hubReportService.getReport(hubReportHash)
          .then(report => {
            if (report?.hubAddress) {
              this.selectedReport = report;
              return this.refresh(report?.hubAddress, hubReportHash);
            } else {
              this.$root.$emit('report-not-found', hubReportHash);
            }
          })
          .catch(() => this.$root.$emit('report-not-found', hubReportHash))
          .finally(() => this.loading = false);
      } else {
        if (hubAddress) {
          return this.refresh(hubAddress);
        } else {
          this.$root.$emit('close-hub-details');
        }
      }
    },
    checkManager() {
      if (this.address && this.deedId) {
        return this.tenantProvisioningContract.isProvisioningManager(this.address, this.deedId)
          .then(provisioningManager => this.isManager = provisioningManager);
      }
    },
    refresh(hubAddress, hubReportHash) {
      this.loading = true;
      return this.$hubService.getHub(hubAddress)
        .then(hub => {
          if (hub) {
            this.$root.$emit('open-hub-details', hub);
          } else if (hubReportHash) {
            this.$root.$emit('report-not-found', hubReportHash);
          } else {
            this.$root.$emit('hub-not-found', hubAddress);
          }
        })
        .catch(() => {
          if (hubReportHash) {
            this.$root.$emit('report-not-found', hubReportHash);
          } else {
            this.$root.$emit('hub-not-found', hubAddress);
          }
        })
        .finally(() => this.loading = false);
    },
    refreshUrl() {
      if (!this.hub || (!this.hub.notFound && !this.hub.reportNotFound)) {
        this.$utils.refreshHubUrl(this.hub?.address);
      }
    },
  },
};
</script>
