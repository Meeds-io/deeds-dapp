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
  <v-card class="d-flex flex-column" flat>
    <div class="d-flex flex-row flex-grow-1">
      <v-card-title class="ps-0 py-0">{{ $t('dapp.tenants.tenantsListTitle') }}</v-card-title>
      <v-divider class="my-auto" />
    </div>
    <v-card-text class="px-0">
      {{ $t('deedsTenantsCommunityIntroductionPart1') }}
      <div
        v-html="$t('deedsTenantsCommunityIntroductionPart2', {0: `<a href='/${parentLocation}/marketplace'>`, 1: `</a>`})"
        class="ps-0 ps-sm-4 mt-4"
        @click.prevent.stop="openMarketplace"></div>
      <div class="ps-0 ps-sm-4">{{ $t('deedsTenantsCommunityIntroductionPart3') }}</div>
    </v-card-text>
  </v-card>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
  }),
  methods: {
    openMarketplace(event) {
      if (event?.target?.tagName?.toLowerCase() === 'a') {
        this.$store.commit('setStandaloneOfferId', null);
        this.$store.commit('setOfferId', null);
        this.$root.$emit('switch-page', 'marketplace');
      }
    },
  },
};
</script>