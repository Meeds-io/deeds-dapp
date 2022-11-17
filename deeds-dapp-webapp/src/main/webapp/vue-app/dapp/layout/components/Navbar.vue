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
    class="bottom-navigation content-box-sizing overflow-x-auto justify-start"
    fixed
    grow>
    <v-row class="ma-0 pa-0 flex-nowrap" no-gutters>
      <v-col cols="3" class="me-2 ms-0 my-0 pa-0">
        <v-btn
          ref="marketplace"
          id="marketplace"
          :href="`/${parentLocation}/marketplace`"
          value="marketplace"
          class="box-box-sizing px-2"
          link
          @click="openPage">
          <h3 class="text-ordinary-capitalize">{{ $t('page.marketplace') }}</h3>
          <v-icon class="mb-1 mt-2">fas fa-store</v-icon>
          <v-tabs-slider color="secondary" class="mobile-menu-slider" />
        </v-btn>
      </v-col>
      <v-col cols="3" class="me-2 ms-0 my-0 pa-0">
        <v-btn
          ref="overview"
          id="overview"
          :href="`/${parentLocation}/overview`"
          value="overview"
          class="box-box-sizing px-2"
          link
          @click="openPage">
          <h3 class="text-ordinary-capitalize">{{ $t('page.overview') }}</h3>
          <v-icon class="mb-1 mt-2">fas fa-home</v-icon>
          <v-tabs-slider color="secondary" class="mobile-menu-slider" />
        </v-btn>
      </v-col>
      <v-col cols="2" class="me-2 ms-0 my-0 pa-0">
        <v-btn
          ref="stake"
          id="stake"
          :href="`/${parentLocation}/stake`"
          value="stake"
          class="box-box-sizing px-2"
          link
          @click="openPage">
          <h3 class="text-ordinary-capitalize">{{ $t('page.stake') }}</h3>
          <v-icon class="mb-1 mt-2">fas fa-piggy-bank</v-icon>
          <v-tabs-slider color="secondary" class="mobile-menu-slider" />
        </v-btn>
      </v-col>
      <v-col cols="2" class="me-2 ms-0 my-0 pa-0">
        <v-btn
          ref="deeds"
          id="deeds"
          :href="`/${parentLocation}/deeds`"
          value="deeds"
          class="box-box-sizing px-2"
          link
          @click="openPage">
          <h3 class="text-ordinary-capitalize">{{ $t('page.deeds') }}</h3>
          <v-icon class="mb-1 mt-2">fas fa-building</v-icon>
          <v-tabs-slider color="secondary" class="mobile-menu-slider" />
        </v-btn>
      </v-col>
      <v-col cols="2" class="me-2 ms-0 my-0 pa-0">
        <v-btn
          ref="farm"
          id="farm"
          :href="`/${parentLocation}/farm`"
          value="farm"
          class="box-box-sizing px-2"
          link
          @click="openPage">
          <h3 class="text-ordinary-capitalize">{{ $t('page.farm') }}</h3>
          <v-icon class="mb-1 mt-2">fas fa-sack-dollar</v-icon>
          <v-tabs-slider color="secondary" class="mobile-menu-slider" />
        </v-btn>
      </v-col>
    </v-row>
  </v-bottom-navigation>
  <v-tabs
    v-else
    v-model="selectedTab"
    color="secondary">
    <v-tab
      ref="marketplace"
      id="marketplace"
      :href="`/${parentLocation}/marketplace`"
      link 
      class="px-2 me-2"
      @click="openPage">
      <h3 class="text-ordinary-capitalize">{{ $t('page.marketplace') }}</h3>
    </v-tab>
    <v-tab
      ref="stake"
      id="stake"
      :href="`/${parentLocation}/stake`"
      link 
      class="px-0 me-2"
      @click="openPage">
      <h3 class="text-ordinary-capitalize">{{ $t('page.stake') }}</h3>
    </v-tab>
    <v-tab
      ref="deeds"
      id="deeds"
      :href="`/${parentLocation}/deeds`"
      link 
      class="px-0 me-2"
      @click="openPage">
      <h3 class="text-ordinary-capitalize">{{ $t('page.deeds') }}</h3>
    </v-tab>
    <v-tab
      ref="farm"
      id="farm"
      :href="`/${parentLocation}/farm`"
      link
      class="px-0 me-2"
      @click="openPage">
      <h3 class="text-ordinary-capitalize">{{ $t('page.farm') }}</h3>
    </v-tab>
    <v-tab
      ref="overview"
      id="overview"
      :href="`/${parentLocation}/overview`"
      link 
      class="px-0 ms-auto"
      @click="openPage">
      <h3 class="text-ordinary-capitalize">{{ $t('page.overview') }}</h3>
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
      return `/${this.parentLocation}/marketplace`;
    }
  }),
  created() {
    this.initSelectedTab();
    this.$root.$on('switch-page', this.switchPage);
    window.addEventListener('popstate', (event) => this.initSelectedTab(event));
  },
  beforeDestroy() {
    this.$root.$off('switch-page', this.switchPage);
  },
  methods: {
    initSelectedTab(event) {
      const href = window.location.pathname;
      const hrefParts = href.split('/');
      if (event) {
        this.avoidAddToHistory = true;
        this.switchPage(hrefParts[hrefParts.length - 1] || 'marketplace');
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
        this.$root.$emit('close-drawer');
        this.$root.$emit('close-alert-message');
        this.$nextTick().then(() => this.$refs[tab].$el.click());
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