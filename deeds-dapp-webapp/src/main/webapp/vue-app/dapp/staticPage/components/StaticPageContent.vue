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
  <deeds-home v-if="homePage" />
  <deeds-about-us v-else-if="aboutUsPage" />
  <deeds-whitepaper v-else-if="whitepaperPage" />
  <deeds-legals v-else-if="legalsPage" />
  <deeds-product-tour v-else-if="tourPage" />
</template>
<script>
export default {
  computed: Vuex.mapState({
    page: state => state.page,
    language: state => state.language,
    homePage() {
      return this.page === 'home';
    },
    aboutUsPage() {
      return this.page === 'about-us';
    },
    whitepaperPage() {
      return this.page === 'whitepaper';
    },
    legalsPage() {
      return this.page === 'legals';
    },
    tourPage() {
      return this.page === 'tour';
    },
  }),
};
</script>