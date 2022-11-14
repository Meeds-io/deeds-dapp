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
  <div>
    <v-list-item v-if="hasNext || hasPrevious" class="px-0 d-flex">
      <v-list-item-action>
        <v-btn
          v-if="hasPrevious"
          color="secondary"
          class="pa-2"
          text
          x-small
          @click="$emit('previous')">
          <v-icon class="me-2">fas fa-step-backward</v-icon>
          {{ $t('deedPreviousOffer') }}
        </v-btn>
      </v-list-item-action>
      <v-spacer />
      <v-list-item-action>
        <v-btn
          v-if="hasNext"
          :loading="loading"
          color="secondary"
          class="pa-2"
          text
          x-small
          @click="$emit('next')">
          {{ $t('deedNextOffer') }}
          <v-icon class="ms-2">fas fa-step-forward</v-icon>
        </v-btn>
      </v-list-item-action>
    </v-list-item>
  </div>
</template>
<script>
export default {
  props: {
    hasNext: {
      type: Boolean,
      default: false,
    },
    hasPrevious: {
      type: Boolean,
      default: false,
    },
    loading: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    drawer: false,
  }),
  created() {
    document.addEventListener('keydown', this.moveToOffer);
    this.$root.$on('drawer-opened', this.drawerOpened);
    this.$root.$on('drawer-closed', this.drawerClosed);
  },
  beforeDestroy() {
    document.removeEventListener('keydown', this.moveToOffer);
    this.$root.$off('drawer-opened', this.drawerOpened);
    this.$root.$off('drawer-closed', this.drawerClosed);
  },
  methods: {
    drawerOpened() {
      this.drawer = true;
    },
    drawerClosed() {
      window.setTimeout(() => this.drawer = false, 50);
    },
    moveToOffer(event) {
      if (this.drawer) {
        return;
      }
      switch (event?.key) {
      case 'Left': // IE/Edge specific value
      case 'ArrowLeft':
        if (this.hasPrevious && !this.loading) {
          this.$emit('previous');
        }
        break;
      case 'Right': // IE/Edge specific value
      case 'ArrowRight':
        if (this.hasNext && !this.loading) {
          this.$emit('next');
        }
        break;
      case 'Esc': // IE/Edge specific value
      case 'Escape':
        this.$store.commit('setStandaloneOfferId', null);
        break;
      default:
        return;
      }
    }
  },
};
</script>