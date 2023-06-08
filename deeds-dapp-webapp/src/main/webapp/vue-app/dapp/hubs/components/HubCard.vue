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
  <a
    :href="hubUrl"
    class="no-decoration"
    target="_blank">
    <v-card
      class="rounded-xl"
      hover
      outlined>
      <v-card
        :color="hubBackgroundColor" 
        height="120px"
        width="100%"
        flat />
        <v-card
        height="100px"
        width="100px"
        class="ms-5 mt-n12 rounded-lg position-absolute"
        outlined>
        <v-img 
          :src="`${hubLogoUrl}`"
          class="no-border-radius mx-auto"
          height="100%"
          width="90%"
          contain />
      </v-card>
      
      <div class="d-flex flex-column pa-4">
        <div class="ms-16 ps-15">
          <span class="display-1 font-weight-bold">
            {{ hubName }}
          </span>
        </div>
        <div class="text-light-color text-h5 mt-3">{{ hubDescription }}</div>
        <div class="d-flex justify-center mt-9">
          <v-img 
            :src="`${parentLocation}/static/images/teamwork_icon_red.webp`"
            class="mx-4"
            max-width="40px" />
          <span class="text-light-color text-h5">
            {{ hubUsersCount }} {{ this.$t('hubs.users.title') }}
          </span>
        </div>
      </div>
    </v-card>
  </a>
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
      return this.language === 'fr' && this.hub?.name?.fr || this.hub?.name?.en;
    },
    hubDescription() {
      return this.language === 'fr' && this.hub?.description?.fr || this.hub?.description?.en;
    },
    hubBackgroundColor() {
      return this.hub?.backgroundColor;
    },
    hubLogoUrl() {
      return this.hub?.logoUrl;
    },
    hubUsersCount() {
      return this.hub?.usersCount || 0;
    },
    hubUrl() {
      return this.hub?.hubUrl;
    }
  }),
};
</script>