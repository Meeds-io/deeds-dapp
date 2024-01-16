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
  <v-app>
    <deeds-free-trial-form v-if="showForm" />
    <v-card
      v-else
      class="overflow-hidden"
      flat>
      <deeds-topbar />
      <deeds-site-content />
      <deeds-site-footer />
    </v-card>
    <deeds-notifications />
  </v-app>
</template>
<script>
export default {
  data() {
    return {
      showForm: false
    };
  },
  computed: Vuex.mapState({
    networkId: state => state.networkId,
    validNetwork: state => state.validNetwork,
    systemThemeDark: state => state.systemThemeDark,
    themePreference: state => state.themePreference,
    dark: state => state.dark,
    language: state => state.language,
    isMobile() {
      return this.$vuetify.breakpoint.xsOnly;
    },
  }),
  watch: {
    isMobile() {
      this.refreshMobileValue();
    },
  },
  created() {
    this.refreshMobileValue();
    this.refreshTheme();
    document.addEventListener('show-trial-form', this.openForm);
    this.showForm = window.location.hash === '#trial-request' || window.location.hash === '#demande-essai' ;
  },
  mounted() {
    window.addEventListener('popstate', this.refreshDocumentHead);
  },
  beforeDestroy() {
    window.removeEventListener('popstate', this.refreshDocumentHead);
  },
  methods: {
    refreshDocumentHead() {
      this.$store.commit('refreshDocumentHead');
    },
    refreshMobileValue() {
      this.$store.commit('setMobile', this.isMobile);
    },
    refreshTheme() {
      const isDark =  ((this.systemThemeDark && this.themePreference === 'system') || this.themePreference === 'dark');
      if (isDark !== this.dark) {
        this.$store.commit('setDark', isDark);
      }
    },
    openForm(event) {
      this.showForm = event.detail.showForm;
      window.history.replaceState('', '', this.language === 'en' ? '#trial-request' : '#demande-essai');
    },
  },
};
</script>