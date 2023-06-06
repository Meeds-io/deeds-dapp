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
          <div v-if="keyword" class="display-1 font-weight-bold ps-0 py-0">{{ hubsCount }} {{ $t('hubs.hubsFound') }} </div>
          <div v-else class="display-1 font-weight-bold ps-0 py-0">{{ $t('hubs.title.featuredHubs') }}</div>
        </div>
      </v-col>
      <v-col
        v-for="(hub, index) in filteredHubs"
        :key="`${hub.id}-${index}`"
        class="d-flex justify-center"
        cols="12"
        lg="4"
        md="12">
        <v-slide-x-transition>
          <deeds-hub-card :hub="hub" />
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
    hubs: [
      {
        id: 1,
        name: {
          fr: 'Builders Hub',
          en: 'Builders Hub'
        },
        description: {
          fr: 'Hub officiel de la DAO Meeds',
          en: 'Official Hub of the Meeds DAO'
        },
        logoUrl: 'https://res.cloudinary.com/dcooc6vig/image/upload/v1685699618/meedsdao-site/assets/images/MeedsDAO%20Logo.png',
        backgroundUrl: 'https://res.cloudinary.com/dcooc6vig/image/upload/v1685699674/meedsdao-site/assets/images/MeedsDAO%20Background.png',
        usersCount: 248
      },
    ],
  }),
  computed: Vuex.mapState({
    language: state => state.language,
    hubsCount() {
      return this.filteredHubs.length;
    },
    filteredHubs() {
      if (this.keyword) {
        if (this.language === 'fr') {
          return this.hubs.filter(hub => hub.name.fr.indexOf(this.keyword) >= 0 || hub.description.fr.indexOf(this.keyword) >= 0);
        } else {
          return this.hubs.filter(hub => hub.name.en.indexOf(this.keyword) >= 0 || hub.description.en.indexOf(this.keyword) >= 0);
        }
      } 
      else {
        return this.hubs; 
      }
    }
  }),
};
</script>