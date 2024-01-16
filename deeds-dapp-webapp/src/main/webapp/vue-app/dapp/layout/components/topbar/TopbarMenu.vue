<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 
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
  <div class="position-relative align-start text-start">
    <v-menu
      content-class="absolute-horizontal-center transparent elevation-0"
      transition="slide-x-transition"
      attach
      offset-y
      open-on-hover>
      <template #activator="{ on, attrs }">
        <slot
          name="activator"
          :on="on"
          :attrs="attrs"></slot>
      </template>
      <div class="d-flex justify-center mx-2 mt-n1 mb-n3 pt-2">
        <v-icon :class="dark && 'dark-black-color' || 'white--text'" class="mb-2px">fa fa-caret-up</v-icon>
      </div>
      <div class="elevation-4 mx-2 mb-2">
        <slot name="default"></slot>
      </div>
    </v-menu>
  </div>
</template>
<script>
export default {
  computed: Vuex.mapState({
    dark: state => state.dark,
  }),
};
</script>