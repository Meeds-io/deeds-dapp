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
  <v-card
    max-width="165px"
    class="d-flex flex-column align-center text-center mx-sm-12 mx-auto"
    flat>
    <img
      :src="`${parentLocation}/static/images/${imageSrc}`"
      alt=""
      class="img-65px"
      width="65px"
      height="65px">
    <strong class="primary--text mt-2 mb-1">{{ $t(title) }}</strong>
    <v-card-subtitle class="pa-0 hidden-xs-only">{{ $t(subtitle) }}</v-card-subtitle>
  </v-card>
</template>
<script>
export default {
  props: {
    imageSrc: {
      type: String,
      default: null,
    },
    title: {
      type: String,
      default: null,
    },
    subtitle: {
      type: String,
      default: null,
    },
  },
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
  }),
};
</script>