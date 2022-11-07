<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 
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
  <v-bottom-navigation
    v-if="isMobile"
    v-model="selectedTab"
    active-class="selected-item"
    class="bottom-navigation overflow-x-auto justify-start"
    hide-on-scroll
    fixed
    grow>
    <v-btn
      ref="overview"
      id="overview"
      :href="`/${parentLocation}/overview`"
      value="overview"
      class="content-box-sizing px-1"
      link
      @click="openPage">
      <h3 class="text-capitalize">{{ $t('page.overview') }}</h3>
      <v-icon class="mb-1">fas fa-home</v-icon>
      <v-tabs-slider color="secondary" class="mobile-menu-slider" />
    </v-btn>
    <v-btn
      ref="marketplace"
      id="marketplace"
      :href="`/${parentLocation}/marketplace`"
      value="marketplace"
      class="content-box-sizing px-1"
      link
      @click="openPage">
      <h3 class="text-capitalize">{{ $t('page.marketplace') }}</h3>
      <v-icon class="mb-1">fas fa-store</v-icon>
      <v-tabs-slider color="secondary" class="mobile-menu-slider" />
    </v-btn>
    <v-btn
      ref="stake"
      id="stake"
      :href="`/${parentLocation}/stake`"
      value="stake"
      class="content-box-sizing px-0"
      link
      @click="openPage">
      <h3 class="text-capitalize">{{ $t('page.stake') }}</h3>
      <v-icon class="mb-1">fas fa-piggy-bank</v-icon>
      <v-tabs-slider color="secondary" class="mobile-menu-slider" />
    </v-btn>
    <v-btn
      ref="deeds"
      id="deeds"
      :href="`/${parentLocation}/deeds`"
      value="deeds"
      class="content-box-sizing px-0"
      link
      @click="openPage">
      <h3 class="text-capitalize">{{ $t('page.deeds') }}</h3>
      <v-icon class="mb-1">fas fa-building</v-icon>
      <v-tabs-slider color="secondary" class="mobile-menu-slider" />
    </v-btn>
    <v-btn
      ref="farm"
      id="farm"
      :href="`/${parentLocation}/farm`"
      value="farm"
      class="content-box-sizing px-0"
      link
      @click="openPage">
      <h3 class="text-capitalize">{{ $t('page.farm') }}</h3>
      <v-icon class="mb-1">fas fa-sack-dollar</v-icon>
      <v-tabs-slider color="secondary" class="mobile-menu-slider" />
    </v-btn>
  </v-bottom-navigation>
  <v-tabs
    v-else
    v-model="selectedTab"
    color="secondary">
    <v-tab
      ref="overview"
      id="overview"
      :href="`/${parentLocation}/overview`"
      link 
      class="px-0 me-2"
      @click="openPage">
      <h3 class="text-capitalize">{{ $t('page.overview') }}</h3>
    </v-tab>
    <v-tab
      ref="marketplace"
      id="marketplace"
      :href="`/${parentLocation}/marketplace`"
      link 
      class="px-2 me-2"
      @click="openPage">
      <h3 class="text-capitalize">{{ $t('page.marketplace') }}</h3>
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
    avoidAddToHistory: false,
  }),
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    isMobile: state => state.isMobile,
    defaultTab() {
      return `/${this.parentLocation}/overview`;
    }
  }),
  created() {
    this.initSelectedTab();
    this.$root.$on('switch-page', this.switchPage);
    window.addEventListener('popstate', (event) => this.initSelectedTab(event));
  },
  methods: {
    initSelectedTab(event) {
      const href = window.location.pathname;
      const hrefParts = href.split('/');
      if (event) {
        this.avoidAddToHistory = true;
        this.switchPage(hrefParts[hrefParts.length - 1] || 'overview');
      } else {
        if (this.isMobile) {
          this.selectedTab = hrefParts[hrefParts.length - 1];
        } else {
          this.selectedTab = hrefParts.length > 2 && `/${this.parentLocation}/${hrefParts[2]}` || this.defaultTab;
        }
      }
    },
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
          if (!this.avoidAddToHistory) {
            window.history.pushState({}, '', link);
          }
          this.avoidAddToHistory = false;
          this.$root.$emit('location-change', `/${event.target.id}`);
        }
        if (this.isMobile) {
          window.scrollTo(0, 0);
        }
      }
    }
  },
};
</script>