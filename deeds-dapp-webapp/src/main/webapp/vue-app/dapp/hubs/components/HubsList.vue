<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 
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
    id="hubsList"
    flat>
    <v-row class="my-13">
      <v-col cols="12">
        <div class="d-flex flex-row flex-grow-1">
          <div v-if="validKeyword" class="display-1 font-weight-bold ps-0 py-0">{{ hubsCount }} {{ $t('hubs.hubsFound') }} </div>
          <div v-else class="display-1 font-weight-bold ps-0 py-0">{{ $t('hubs.title.featuredHubs') }}</div>
        </div>
      </v-col>
      <v-col
        v-for="(hub, index) in hubs"
        :key="`${hub.id}-${index}`"
        class="d-flex justify-center"
        cols="12"
        lg="4"
        md="12">
        <v-slide-x-transition>
          <deeds-hubs-card :hub="hub" />
        </v-slide-x-transition>
      </v-col>
    </v-row>
  </v-card>
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
    hubs: [],
  }),
  computed: Vuex.mapState({
    language: state => state.language,
    validKeyword() {
      return this.keyword !== null;
    },
    hubsCount() {
      return this.hubs.length;
    }
  }),
  created() {
    this.resetSearch();
  },
  watch: {
    keyword() {
      this.resetSearch();
      if (this.keyword) {
        this.searchHubs();
      }
    },
  },
  methods: {
    resetSearch() {
      this.hubs = [
        {
          id: 1,
          fr: {
            name: 'Builders Hub',
            description: 'Hub officiel de la DAO Meeds',
          },
          en: {
            name: 'Builders Hub',
            description: 'Official Hub of the Meeds DAO',
          },
          logoLink: 'https://res.cloudinary.com/dcooc6vig/image/upload/v1685699618/meedsdao-site/assets/images/MeedsDAO%20Logo.png',
          backgroundLink: 'https://res.cloudinary.com/dcooc6vig/image/upload/v1685699674/meedsdao-site/assets/images/MeedsDAO%20Background.png',
          nbUsers: 248
        },
      ];
    },
    searchHubs() {
      if (this.language === 'fr') {
        this.hubs = this.hubs.filter(hub => hub.fr.name.indexOf(this.keyword) >= 0 || hub.fr.description.indexOf(this.keyword) >= 0);
      } else {
        this.hubs = this.hubs.filter(hub => hub.en.name.indexOf(this.keyword) >= 0 || hub.en.description.indexOf(this.keyword) >= 0);
      }
    }
  }
};
</script>