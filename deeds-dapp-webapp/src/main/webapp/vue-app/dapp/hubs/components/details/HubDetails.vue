<!--
  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2023 Meeds Association contact@meeds.io

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
    <deeds-hub-details-deed-card
      :hub="hub" />
    <deeds-hub-details-rewards
      :hub="hub"
      class="mt-4" />
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
  watch: {
    hub() {
      this.refreshUrl();      
    },
  },
  created() {
    if (!this.hub) {
      const hubAddress = this.$utils.getQueryParam('address');
      if (hubAddress) {
        this.refresh(hubAddress);
      } else {
        this.$root.$emit('close-hub-details');
      }
    }
  },
  methods: {
    refresh(hubAddress) {
      this.loading = true;
      return this.$hubService.getHub(hubAddress)
        .then(hub => {
          if (hub) {
            this.$root.$emit('open-hub-details', hub);
          } else {
            this.$root.$emit('close-hub-details');
            this.refreshUrl();
          }
        })
        .finally(() => this.loading = false);
    },
    refreshUrl() {
      const link = this.hub
          && `${window.location.pathname}?address=${this.hub.address}`
          || window.location.pathname;
      if (!window.location.href.endsWith(link)) {
        const fullLink = `${origin}${link}`;
        this.$nextTick().then(() =>
          window.history.pushState({}, '', fullLink)
        );
      }
    },
  },
};
</script>
