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
  <v-card flat>
    <v-card-title class="ps-0 headline font-weight-bold">
      {{ $t('deeds') }}
    </v-card-title>
    <v-card-text class="ps-0 text-h6 font-weight-normal">
      {{ $t('deedsIntroduction') }}
    </v-card-text>
    <v-card-actions class="ps-0 justify-center">
      <v-btn
        id="sDeeds-button"
        :href="ownersURL"
        name="displayOwnedDeedsButton"
        height="40px"
        outlined
        text>
        <img
          :src="`${parentLocation}/static/images/deed-small.png`"
          alt=""
          class="me-2 img-30px">
        <span class="text-none">{{ $t('myDeeds') }}</span>
      </v-btn>
    </v-card-actions>
  </v-card>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    ownersURL: state => state.ownersURL,
  }),
};
</script>