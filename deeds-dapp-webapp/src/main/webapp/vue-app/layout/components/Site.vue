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
  <v-app>
    <v-card flat class="overflow-hidden">
      <deeds-topbar id="banner" role="banner" />
      <v-divider />
      <deeds-site-content id="siteContent" />
    </v-card>
  </v-app>
</template>
<script>
export default {
  computed: Vuex.mapState({
    appLoading: state => state.appLoading,
    isMobile() {
      return this.$vuetify.breakpoint.smAndDown;
    },
  }),
  watch: {
    isMobile() {
      this.refreshMobileValue();
    },
    appLoading() {
      this.$nextTick().then(() => this.installScrollControlListener());
    },
  },
  created() {
    this.refreshMobileValue();
    document.addEventListener('DOMContentLoaded', () => this.installScrollControlListener());
  },
  methods: {
    refreshMobileValue() {
      this.$store.commit('setMobile', this.isMobile);
    },
    installScrollControlListener() {
      const siteBody = document.querySelector('#mainPageContent');
      if (!siteBody) {
        return;
      }
      if (!siteBody.getAttribute('data-scroll-control')) {
        siteBody.setAttribute('data-scroll-control', 'true');
        const shadowBox = document.createElement('div');
        shadowBox.id = 'TopBarBoxShadow';
        shadowBox.style.boxShadow = '0 6px 4px -4px rgb(0 0 0 / 30%)';
        shadowBox.style.position = 'fixed';
        shadowBox.style.width = '100vw';
        shadowBox.style.height = this.isMobile && '56px' || '64px';
        shadowBox.style.top = 0;
        shadowBox.style.left = 0;
        shadowBox.style.right = 0;
        shadowBox.style.zIndex = 1;
        shadowBox.style.visibility = 'hidden';
        document.querySelector('#navbar').appendChild(shadowBox);
        document.addEventListener('scroll', this.controlBodyScrollClass, false);
        this.controlBodyScrollClass();
      }
    },
    controlBodyScrollClass() {
      const topBarBoxShadow = document.querySelector('#TopBarBoxShadow');
      if (window.scrollY) {
        if (topBarBoxShadow.style.visibility === 'hidden') {
          document.querySelector('#TopBarBoxShadow').style.visibility = '';
        }
      } else {
        if (topBarBoxShadow.style.visibility !== 'hidden') {
          document.querySelector('#TopBarBoxShadow').style.visibility = 'hidden';
        }
      }
    },
  },
};
</script>