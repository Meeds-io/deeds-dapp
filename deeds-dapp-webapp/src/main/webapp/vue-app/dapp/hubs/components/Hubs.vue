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
  <div>
    <deeds-hubs-introduction
      :reduced="selectedHub"
      @keyword-changed="keyword = $event" />
    <v-scale-transition>
      <div v-show="selectedHub">
        <deeds-hub-details :hub="selectedHub" />
      </div>
    </v-scale-transition>
    <deeds-hubs-list
      v-if="!selectedHub"
      :keyword="keyword" />
  </div>
</template>
<script>
export default {
  data: () => ({
    keyword: null,
    selectedHub: null,
  }),
  created() {
    this.$root.$on('open-hub-details', this.openHubDetails);
    this.$root.$on('hub-not-found', this.openHubNotFound);
    this.$root.$on('report-not-found', this.openHubReportNotFound);
    this.$root.$on('close-hub-details', this.closeHubDetails);
  },
  methods: {
    openHubNotFound(hubAddress) {
      this.selectedHub = {
        address: hubAddress,
        notFound: true,
      };
    },
    openHubReportNotFound(reportId) {
      this.selectedHub = {
        reportId: reportId,
        reportNotFound: true,
      };
    },
    openHubDetails(hub) {
      this.selectedHub = hub;
    },
    closeHubDetails() {
      this.selectedHub = null;
    },
  },
};
</script>