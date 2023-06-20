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
  <v-card flat>
    <v-card-title class="ps-0 headline text-sm-h4 font-weight-bold">
      {{ $t('contribute') }}
    </v-card-title>
    <v-card-text class="ps-0 text-h6 font-weight-normal">
      {{ $t('contributionIntroduction') }}
    </v-card-text>
    <v-card-actions class="ps-0 justify-center">
      <v-btn
        name="getInvolvedLink"
        href="https://meeds.io/builders"
        target="_blank"
        rel="nofollow noreferrer noopener"
        outlined
        text>
        <v-img
          :src="`${parentLocation}/static/images/meedsicon.png`"
          class="me-2"
          alt=""
          max-height="20"
          max-width="20"
          eager
          contain />
        {{ $t('getInvolved') }}
      </v-btn>
    </v-card-actions>
  </v-card>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
  }),
};
</script>