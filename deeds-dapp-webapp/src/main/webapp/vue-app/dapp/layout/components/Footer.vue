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
  <v-card
    :class="backgroundColorClass"
    class="pa-5 mx-md-auto"
    flat>
    <div class="d-flex flex-column-reverse flex-sm-row align-center my-2 footerLayout mx-auto mb-md-0 mb-10">
      <span :class="textColor" class="caption text-center text-sm-start">{{ $t('copyright') }}</span>
      <v-spacer />
      <div class="d-flex">
        <div class="mx-4">
          <deeds-footer-fiat-currency-selector v-if="!staticPage" />
        </div>
        <deeds-footer-language-selector />
        <deeds-theme-button
          css-class="ms-4" 
          topbar />
      </div>
    </div>
  </v-card>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    whitepaperLink: state => state.whitepaperLink,
    marketplaceURL: state => state.marketplaceURL,
    tourURL: state => state.tourURL,
    legalsURL: state => state.legalsURL,
    stakeURL: state => state.stakeURL,
    aboutUsURL: state => state.aboutUsURL,
    tenantsURL: state => state.tenantsURL,
    mintUrl: state => state.mintUrl,
    whitepaperURL: state => state.whitepaperURL,
    hubsUrl: state => state.hubsUrl,
    whyMeedsUrl: state => state.whyMeedsUrl,
    dark: state => state.dark,
    staticPage: state => state.staticPage,
    githubColor() {
      return this.dark && 'white' || 'black';
    },
    textColor() {
      return this.dark && 'white--text' || 'text-sub-title';
    },
    backgroundColorClass() {
      return this.dark && 'dark-color' || 'light-grey-background';
    }
  }),
};
</script>
