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
  <v-sheet
    class="overflow-y-auto">
    <div class="siteContentLayout mt-11 mt-sm-13">
      <v-progress-linear v-if="appLoading" indeterminate />
      <div v-else class="mainPageLayout mx-auto pa-4">
        <deeds-navbar id="navbar" role="navigation" />
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
  computed: Vuex.mapState({
    appLoading: state => state.appLoading,
  }),
};
</script>