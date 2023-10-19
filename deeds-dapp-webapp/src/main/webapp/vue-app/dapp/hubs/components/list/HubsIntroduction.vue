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
  <div class="d-flex flex-column pt-0 pt-md-6 mt-16">
    <v-card-title class="justify-center flex-nowrap mb-16 ps-4 ps-sm-1">
      <div class="d-flex flex-column text-center">
        <span class="display-1 font-weight-bold">{{ $t('page.hubs.title') }}</span>
        <span class="headline hidden-sm-and-down">{{ $t('page.hubs.subtitle') }}</span>
      </div>
    </v-card-title>
    <v-text-field
      v-model="keyword"
      :placeholder="$t('hubs.search.placeholder')"
      class="rounded-pill mx-auto col-12 col-sm-6 headline"
      height="60px"
      @keydown="enterEvent"  
      outlined
      hide-details
      clearable
      clear-icon="fa-times fa-1x mt-1 mb-auto me-4"> 
      <template #prepend-inner>
        <v-icon
          v-if="!keyword"
          class="mx-9 my-1"
          size="24">
          fa fa-search
        </v-icon>
        <div v-else class="ms-9"></div>
      </template>
    </v-text-field>
  </div>
</template>
<script>
export default {
  data: () => ({
    keyword: null
  }),
  computed: Vuex.mapState({
    tenantsURL: state => state.tenantsURL,
    parentLocation: state => state.parentLocation,
  }),
  watch: {
    keyword() {
      this.$emit('keyword-changed', this.keyword);
    },
  },
  methods: {
    enterEvent(event) {
      if (event.key === 'Escape') {
        this.keyword = null;
      }
    }
  }
};
</script>