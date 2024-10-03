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
  <v-row class="mb-15 mt-7">
    <v-col cols="12">
      <div class="d-flex flex-row flex-grow-1">
        <div v-if="keyword" class="headline font-weight-bold pb-1">{{ hubsCount }} {{ $t('hubs.hubsFound') }} </div>
      </div>
    </v-col>
    <v-col
      v-for="(hub, index) in filteredHubs"
      :key="`${hub.address}-${index}`"
      class="d-flex justify-center pb-12"
      cols="12"
      lg="4"
      md="12">
      <v-slide-x-transition>
        <deeds-hub-card :hub="hub" class="full-width" />
      </v-slide-x-transition>
    </v-col>
  </v-row>
</template>
<script>
export default {
  props: {
    keyword: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    loading: false,
    pageSize: 100,
    page: 0,
    hasMore: false,
    hubs: [],
    upcomingHubs: [],
  }),
  computed: Vuex.mapState({
    language: state => state.language,
    formLink: state => state.formLink,
    hubsCount() {
      return this.filteredHubs.length;
    },
    filteredHubs() {
      if (this.loading) {
        return [];
      } else if (this.keyword) {
        if (this.language === 'fr') {
          return this.hubs.filter(hub => hub?.name?.fr?.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0 || hub?.description?.fr?.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0);
        } else {
          return this.hubs.filter(hub => hub?.name?.en?.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0 || hub?.description?.en?.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0);
        }
      } else {
        return this.hubs; 
      }
    },
  }),
  created() {
    this.upcomingHubs = this.$utils.sortByName(this.upcomingHubs, this.language);
    this.retrieveHubs();
  },
  methods: {
    retrieveHubs() {
      this.loading = true;
      this.$hubService.getHubs({
        page: this.page,
        size: this.pageSize,
      })
        .then(data => {
          const hubs = data?._embedded?.hubs?.filter?.(h => h.connected);
          if (hubs?.length) {
            this.hubs = this.$utils.sortByName(hubs, this.language);
            this.hasMore = data.page.totalPages > (this.page + 1);
          } else {
            this.hasMore = false;
          }
        })
        .finally(() => this.loading = false);
    },
  },
};
</script>
