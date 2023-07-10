<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2022 Meeds Association contact@meeds.io
 
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
  <v-sheet id="siteContent" class="overflow-y-auto overflow-x-hidden">
    <div class="siteContentLayout mt-11 mt-sm-13">
      <v-progress-linear v-if="appLoading" indeterminate />
      <div v-else class="mainPageLayout pa-5 mx-md-auto">
        <deeds-navbar
          v-if="mobileNavigationBar"
          id="navbar"
          role="navigation" />
        <v-tooltip
          bottom>
          <template #activator="{ on, attrs }">
            <v-btn
              v-show="scrolled"
              fab
              dark
              fixed
              bottom
              right
              color="primary"
              class="mb-16 mb-md-10"
              height="60px"
              v-bind="attrs"
              v-on="on"
              @click="toTop">
              <v-icon>fas fa-angle-up</v-icon>
            </v-btn>
          </template>
          <span>{{ $t('scrollToTop') }}</span>
        </v-tooltip>
        <deeds-page
          id="mainPageContent"
          class="mb-12 mb-sm-0"
          role="main" />
      </div>
      <deeds-notifications />
    </div>
  </v-sheet>
</template>
<script>
export default {
  data: () => ({
    scrolled: false
  }),
  computed: Vuex.mapState({
    appLoading: state => state.appLoading,
    staticPage: state => state.staticPage,
    isMobile: state => state.isMobile,
    mobileNavigationBar() {
      return !this.staticPage && this.isMobile;
    },
  }),
  created() {
    window.addEventListener('scroll', this.onScroll);
  },
  methods: {
    onScroll(event) {
      if (typeof window === 'undefined') {return;}
      const top = window.pageYOffset ||   event.target.scrollTop || 0;
      this.scrolled = top > 20;
    },
    toTop() {
      this.$vuetify.goTo(0);
    }
  }
};
</script>