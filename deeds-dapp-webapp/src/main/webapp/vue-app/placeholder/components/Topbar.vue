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
  <v-app-bar
    id="dappTopbar"
    fixed
    color="white"
    elevate-on-scroll>
    <v-spacer />
    <div class="d-flex headerLayout px-0 px-sm-7 mx-1">
      <v-toolbar-title class="d-flex align-center">
        <v-img
          max-height="64px"
          max-width="64px"
          :src="`${parentLocation}/static/images/meeds.png`"
          contain
          eager />
        <div class="ps-2 pb-1">DAO</div>
      </v-toolbar-title>
      <v-spacer />
    </div>
    <v-spacer />
  </v-app-bar>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
  }),
};
</script>