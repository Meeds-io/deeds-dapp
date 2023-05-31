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
  <v-card
    outlined
    flat>
    <v-img 
      :src="`${hubBackground}`"
      max-height="120px"
      min-width="100px" />
    <v-avatar
      color="white"
      size="100"
      class="ms-5 mt-n12 elevation-2 rounded-lg position-absolute">
      <v-img 
        :src="`${hubLogo}`"
        class="no-border-radius"
        height="60px" />
    </v-avatar>
    <div class="d-flex flex-column pa-4">
      <div class="ms-16 ps-15">
        <span class="display-1 font-weight-bold">
          {{ hubName }}
        </span>
      </div>
      <div class="text-light-color text-h5 mt-3">{{ hubDescription }}</div>
      <div class="d-flex justify-center mt-9">
        <v-img 
          :src="`${parentLocation}/static/images/teamwork_icon.webp`"
          class="mx-4"
          max-width="40px" />
        <span class="text-light-color text-h5">
          {{ hubNbUsers }} {{ this.$t('hubs.users.title') }}
        </span>
      </div>
    </div>
  </v-card>
</template>
<script>
export default {
  props: {
    hub: {
      type: Object,
      default: null,
    },
  },
  computed: Vuex.mapState({
    language: state => state.language,
    parentLocation: state => state.parentLocation,
    hubName() {
      return this.language === 'fr' ? this.hub?.fr.name : this.hub?.en.name;
    },
    hubDescription() {
      return this.language === 'fr' ? this.hub?.fr.description : this.hub?.en.description;
    },
    hubBackground() {
      return this.hub?.backgroundLink;
    },
    hubLogo() {
      return this.hub?.logoLink;
    },
    hubNbUsers() {
      return this.hub?.nbUsers;
    },
  }),
};
</script>