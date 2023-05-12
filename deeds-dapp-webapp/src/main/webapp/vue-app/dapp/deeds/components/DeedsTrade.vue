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
  <v-card class="ms-md-2" flat>
    <v-card-title class="pa-0">
      {{ $t('tradeDeeds') }}
    </v-card-title>
    <v-card-text class="ps-0" v-html="$t('tradeDeedsIntroduction', {0: `<a href='${openSeaLink}' target='${openSeaTarget}' class='link--color' rel='nofollow noreferrer noopener'>OpenSea</a>`})" />
    <v-card-actions class="ps-0 justify-center">
      <v-btn
        :href="openSeaLink"
        :target="openSeaTarget"
        rel="nofollow noreferrer noopener"
        height="73px"
        outlined
        text
        class="px-3">
        <img
          :src="`${parentLocation}/static/images/opensea.webp`"
          width="44px"
          class="me-2"
          alt="Available on OpenSea">
        <span class="text-none">{{ $t('availableOnOpenSea') }}</span>
      </v-btn>
    </v-card-actions>
  </v-card>
</template>
<script>
export default {
  computed: Vuex.mapState({
    openSeaCollectionLink: state => state.openSeaCollectionLink,
    parentLocation: state => state.parentLocation,
    openSeaLink() {
      return this.openSeaCollectionLink || 'javascript:void(0)';
    },
    openSeaTarget() {
      return this.openSeaCollectionLink && '_blank' || '';
    },
  }),
};
</script>