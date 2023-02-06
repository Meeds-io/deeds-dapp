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
    <v-card-title class="ps-0 py-0 justify-center">{{ $t('dapp.owners.ownersListTitle') }}</v-card-title>
    <v-card-text v-show="showIntroduction" class="px-0 pt-4">
      {{ $t('deedsOwnersCommunityIntroductionPart1') }}
      <ul class="mt-4">
        <ol
          v-html="$t('deedsOwnersCommunityIntroductionPart2', {0: `<a href='/${parentLocation}/deeds'>`, 1: `</a>`})"
          class="ps-0 ps-sm-4"
          @click.prevent.stop="openDeeds">
        </ol>
        <ol
          v-html="$t('deedsOwnersCommunityIntroductionPart3', {0: `<a href='${openSeaLink}' title='${$t('sellOnOpenSea')}' target='${openSeaTarget}' rel='nofollow noreferrer noopener'>`, 1: `</a>`})"
          class="ps-0 ps-sm-4">
        </ol>
      </ul>
    </v-card-text>
    <v-progress-linear v-if="loading" indeterminate />
  </v-card>
</template>
<script>
export default {
  data: () => ({
    showIntroduction: false,
    hasTenants: false,
    loading: true,
  }),
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    openSeaCollectionLink: state => state.openSeaCollectionLink,
    openSeaLink() {
      return this.openSeaCollectionLink || 'javascript:void(0)';
    },
    openSeaTarget() {
      return this.openSeaCollectionLink && '_blank' || '';
    },
  }),
  created() {
    this.$root.$on('deed-leases-loaded', this.computeLeasesLength);
  },
  beforeDestroy() {
    this.$root.$off('deed-leases-loaded', this.computeLeasesLength);
  },
  methods: {
    computeLeasesLength(_leases, totalSize) {
      this.hasTenants = totalSize > 0;
      this.showIntroduction = true;
      window.setTimeout(() => this.loading = false, 500);
    },
    openDeeds(event) {
      if (event?.target?.tagName?.toLowerCase() === 'a') {
        this.$root.$emit('switch-page', 'deeds');
      }
    },
  },
};
</script>