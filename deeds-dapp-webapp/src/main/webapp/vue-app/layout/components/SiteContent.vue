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
  <div>
    <v-divider v-if="!isMobile" />
    <v-sheet
      :class="isMobile && 'mt-13'"
      class="overflow-y-auto">
      <v-container class="mt-n3" style="height: auto;">
        <v-progress-linear v-if="loading" indeterminate />
        <div v-else class="mainPageLayout pa-4">
          <deeds-metamask />
          <template v-if="validNetwork">
            <deeds-navbar v-if="address" role="navigation" />
            <deeds-page
              v-if="address"
              class="mt-8 mt-sm-10"
              role="main" />
          </template>
        </div>
        <deeds-notifications />
      </v-container>
    </v-sheet>
  </div>
</template>
<script>
export default {
  computed: Vuex.mapState({
    address: state => state.address,
    loading: state => state.appLoading,
    validNetwork: state => state.validNetwork,
    isMobile: state => state.isMobile,
  }),
};
</script>