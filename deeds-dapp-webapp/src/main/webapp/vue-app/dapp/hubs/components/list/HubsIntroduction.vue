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
  <div class="d-flex flex-column pt-0 pt-md-6">
    <v-card-title
      :class="reduced && 'mt-8' || 'mt-16'"
      class="justify-center flex-nowrap ps-4 ps-sm-1">
      <div class="d-flex flex-column col-12 col-lg-8 col-md-7 pa-0">
        <span class="display-1 font-weight-bold text-center text-sm-start">{{ $t('page.hubs.title') }}</span>
        <span v-show="!reduced" class="headline hidden-sm-and-down">{{ $t('page.hubs.subtitle') }}</span>
      </div>
      <v-spacer />
      <v-img
        :src="`${parentLocation}/static/images/marketplace_banner.webp`"
        :max-width="reduced && 48 || 300"
        class="hidden-sm-and-down"
        alt=""
        contain
        eager />
    </v-card-title>
    <v-scale-transition>
      <v-text-field
        v-if="!reduced"
        v-model="keyword"
        :placeholder="$t('hubs.search.placeholder')"
        class="rounded-pill mx-auto col-12 col-sm-6 headline mt-16"
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
    </v-scale-transition>
  </div>
</template>
<script>
export default {
  props: {
    reduced: {
      type: Boolean,
      default: false,
    },
  },
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