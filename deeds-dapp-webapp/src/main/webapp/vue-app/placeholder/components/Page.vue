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
    flat
    tile
    class="d-flex flex-column align-center justify-center text-center mt-8">
    <h1 class="grey--text text--darken-1">{{ $t('welcomeMetaverse') }}</h1>
    <h3 class="grey--text">{{ $t('yourWorkspaceIsProcessing') }}</h3>
    <v-img
      :src="`${parentLocation}/static/images/WorkspaceBuilding_${language}.gif`"
      min-height="50%"
      height="50%"
      width="700px"
      max-width="100%"
      class="mt-8"
      contain />
    <h3 class="grey--text my-4">{{ $t('wantToKnowMore') }}</h3>
    <div class="d-flex flex-sm-row flex-column">
      <v-spacer />
      <v-btn
        color="primary"
        :min-width="isMobile && 'auto' || '180px'"
        elevation="0"
        class="mb-2 mb-sm-0"
        href="https://builders.meeds.io/"
        target="_blank"
        rel="nofollow noreferrer noopener"
        outlined>
        <span class="text-none">{{ $t('joinOurBuildersApp') }}</span>
      </v-btn>
      <div class="mx-4 mx-md-8"></div>
      <v-btn
        color="grey"
        :min-width="isMobile && 'auto' || '180px'"
        elevation="0"
        href="https://meeds.io/"
        target="_blank"
        rel="nofollow noreferrer noopener"
        dark>
        <span class="text-none">{{ $t('learnMore') }}</span>
      </v-btn>
      <v-spacer />
    </div>
  </v-card>
</template>
<script>
export default {
  computed: Vuex.mapState({
    language: state => state.language,
    parentLocation: state => state.parentLocation,
    isMobile() {
      return this.$vuetify.breakpoint.smAndDown;
    },
  }),
};
</script>