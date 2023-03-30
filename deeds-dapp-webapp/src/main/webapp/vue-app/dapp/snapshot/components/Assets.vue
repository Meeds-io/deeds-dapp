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
  <div class="mb-4 mb-md-8">
    <v-card-title class="justify-center flex-nowrap mb-10">
      <span class="col-12 col-lg-8 col-md-7 ps-0 text-sm-h2 display-2 font-weight-bold text-center text-md-start">{{ $t('startManagingAssets') }}</span>
      <v-spacer />
      <v-img 
        :src="`${parentLocation}/static/images/portfolio_banner.png`"
        max-width="360px"
        class="hidden-sm-and-down"
        alt=""
        contain
        eager />
    </v-card-title>
    <v-card-title class="d-flex flex-nowrap px-0 pb-0 pt-0 pt-md-12">
      {{ $t('assets') }}
      <v-divider class="my-auto ms-4" />
    </v-card-title>
    <div class="d-flex flex-column flex-md-row mt-4">
      <v-card flat class="assets ms-0 ms-md-8">
        <deeds-token-assets />
        <deeds-deed-assets />
      </v-card>
    </div>
  </div>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
  }),
};
</script>