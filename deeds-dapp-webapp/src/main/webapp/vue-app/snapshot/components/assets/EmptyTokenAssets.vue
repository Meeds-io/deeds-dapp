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
  <v-row class="ms-4 d-flex flex-row">
    <v-col class="pa-0" align-self="start">
      <v-img 
        height="100px"
        width="140px"
        :src="`/${parentLocation}/static/images/meeds.png`"
        contain
        eager />
    </v-col>
    <v-col cols="9">
      <v-card flat>
        <v-card-text class="py-0">
          {{ $t('noTokensDescription') }}
        </v-card-text>
        <v-card-text class="d-flex">
          <div class="pe-1">
            {{ $t('see') }}
          </div>
          <a
            class="text-decoration-underline"
            @click="$root.$emit('switch-page', 'stake')">
            {{ $t('there') }}
          </a>
          <div class="ps-1">
            {{ $t('moreInformation') }}
          </div>
        </v-card-text>
      </v-card>
    </v-col>
  </v-row>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
  }),
};
</script>
