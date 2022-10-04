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
  <v-row class="ps-8 pe-4 ma-0 d-flex flex-column flex-sm-row flex-grow-1">
    <v-col
      v-if="!isMobile || $slots.col1"
      class="px-0 pb-0 pb-sm-3 text-no-wrap"
      align-self="start">
      <slot name="col1"></slot>
    </v-col>
    <v-divider class="d-block d-sm-none" />
    <v-col
      v-if="!isMobile || $slots.col2"
      class="pe-0 ps-4 ps-sm-0 text-no-wrap"
      cols="3">
      <slot name="col2"></slot>
      <template v-if="isMobile && $slots.col4">
        <small class="d-flex text-no-wrap">
          <slot name="col4"></slot>
        </small>
      </template>
    </v-col>
    <v-col
      v-if="!isMobile || $slots.col3"
      class="pe-0 ps-4 ps-sm-0 text-no-wrap"
      cols="4">
      <slot name="col3"></slot>
    </v-col>
    <v-col
      v-if="!isMobile && $slots.col4"
      class="pe-0 ps-4 ps-sm-0 text-no-wrap"
      align-self="end">
      <slot name="col4"></slot>
    </v-col>
  </v-row>
</template>
<script>
export default {
  computed: Vuex.mapState({
    isMobile: state => state.isMobile,
  }),
};
</script>