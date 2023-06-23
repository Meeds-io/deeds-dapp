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
    <v-card-title class="justify-center flex-nowrap mb-10 ps-4 ps-sm-2">
      <div class="d-flex flex-column col-12 col-lg-8 col-md-7 pa-0">
        <span class="text-sm-h3 display-1 font-weight-bold text-center text-sm-start">{{ $t('page.portfolio.title') }}</span>
        <span class="display-1 hidden-sm-and-down">{{ $t('page.portfolio.subtitle') }}</span>
      </div>
      <v-spacer />
      <v-img 
        :src="`${parentLocation}/static/images/portfolio_banner.webp`"
        max-width="360px"
        class="hidden-sm-and-down"
        alt=""
        contain
        eager />
    </v-card-title>
    <div class="d-flex pt-0 pt-md-12 ms-0 ms-md-4">
      <span class="headline text-sm-h4 font-weight-bold">
        {{ $t('assets') }}
      </span>
      <v-spacer />
      <v-btn
        href=""
        class="rounded-pill px-8"
        color="secondary"
        height="45px"
        outlined
        @click.prevent.stop="$root.$emit('open-buy-meed-drawer', true)">
        <h4 class="ms-1">{{ $t('buy') }}</h4>
      </v-btn>
    </div>
    <div class="mt-4">
      <v-card flat class="ms-0 ms-md-4">
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