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
    <v-card-title class="ps-0 headline font-weight-bold">
      {{ $t('governance') }}
    </v-card-title>
    <v-card-text class="ps-0 text-h6 font-weight-normal">
      {{ $t('governanceIntroduction') }}
    </v-card-text>
    <v-card-actions class="ps-0 justify-center d-flex flex-column flex-sm-row">
      <v-btn
        id="sDiscord-button"
        name="discussOnDiscordLink"
        href="https://discord.gg/7d9Byf4Fz6"
        target="_blank"
        rel="nofollow noreferrer noopener"
        height="40px"
        outlined
        text
        class="me-sm-4">
        <v-icon 
          size="24" 
          color="indigo accent-2" 
          class="me-2">
          fab fa-discord
        </v-icon>
        <span class="text-none">{{ $t('discussOnDiscord') }}</span>
      </v-btn>
      <v-btn
        id="sSnapshot-button"
        name="voteOnSnapshotLink"
        href="https://snapshot.org/#/meedsdao.eth"
        target="_blank"
        rel="nofollow noreferrer noopener"
        height="40px"
        outlined
        text
        class="ms-0 ms-sm-4 mt-4 mt-sm-0">
        <img
          :src="`${parentLocation}/static/images/snapshot.png`"
          alt=""
          class="me-2 img-24px">
        <span class="text-none">{{ $t('voteOnSnapshot') }}</span>
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