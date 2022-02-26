<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 
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
  <v-tabs v-model="selectedTab">
    <v-tab
      ref="snapshot"
      id="snapshot"
      :href="`/${parentLocation}/snapshot`"
      link 
      class="px-0 me-2"
      @click="openPage">
      <h3 class="text-capitalize">{{ $t('page.snapshot') }}</h3>
    </v-tab>
    <v-tab
      ref="stake"
      id="stake"
      :href="`/${parentLocation}/stake`"
      link 
      class="px-0 me-2"
      @click="openPage">
      <h3 class="text-capitalize">{{ $t('page.stake') }}</h3>
    </v-tab>
    <v-tab
      ref="deeds"
      id="deeds"
      :href="`/${parentLocation}/deeds`"
      link 
      class="px-0 me-2"
      @click="openPage">
      <h3 class="text-capitalize">{{ $t('page.deeds') }}</h3>
    </v-tab>
    <v-tab
      ref="farm"
      id="farm"
      :href="`/${parentLocation}/farm`"
      link
      class="px-0 me-2"
      @click="openPage">
      <h3 class="text-capitalize">{{ $t('page.farm') }}</h3>
    </v-tab>
  </v-tabs>
</template>
<script>
export default {
  data: () => ({
    selectedTab: null,
  }),
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    defaultTab() {
      return `/${this.parentLocation}/snapshot`;
    }
  }),
  created() {
    const href = window.location.pathname;
    const hrefParts = href.split('/');
    this.selectedTab = hrefParts.length > 2 && `/${this.parentLocation}/${hrefParts[2]}` || this.defaultTab;
    this.$root.$on('switch-page', this.switchPage);
  },
  methods: {
    switchPage(tab) {
      if (tab && this.$refs[tab]) {
        this.$refs[tab].$el.click();
      }
    },
    openPage(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
        const link = event.target.href || event.target.parentElement && (event.target.parentElement.href || (event.target.parentElement.parentElement && event.target.parentElement.parentElement.href));
        if (link) {
          window.history.pushState({}, '', link);
          this.$root.$emit('location-change', `/${event.target.id}`);
        }
      }
    }
  },
};
</script>