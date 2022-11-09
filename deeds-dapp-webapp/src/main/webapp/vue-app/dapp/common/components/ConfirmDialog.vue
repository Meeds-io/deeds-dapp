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
  <v-dialog
    v-model="dialog"
    :width="width"
    max-width="100vw"
    @keydown.esc="close">
    <v-card class="elevation-12">
      <v-card-title class="text-h6 grey lighten-2 py-2">
        {{ title }}
      </v-card-title>
      <v-card-text v-html="message" class="pt-5" />
      <v-divider />
      <!-- eslint-disable-next-line vue/no-v-html -->
      <v-card-actions v-if="!hideActions">
        <v-spacer />
        <v-btn
          :disabled="loading"
          :loading="loading"
          class="primary me-4"
          depressed
          @click="ok">
          <span class="text-capitalize">
            {{ okLabel }}
          </span>
        </v-btn>
        <v-btn
          :disabled="loading"
          :loading="loading"
          class="ms-4"
          outlined
          text
          @click="close">
          <span class="text-capitalize">
            {{ cancelLabel }}
          </span>
        </v-btn>
        <v-spacer />
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<script>
export default {
  props: {
    loading: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    title: {
      type: String,
      default: function() {
        return null;
      },
    },
    titleClass: {
      type: String,
      default: function() {
        return '';
      },
    },
    message: {
      type: String,
      default: function() {
        return null;
      },
    },
    okLabel: {
      type: String,
      default: function() {
        return 'ok';
      },
    },
    cancelLabel: {
      type: String,
      default: function() {
        return 'Cancel';
      },
    },
    hideActions: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    width: {
      type: String,
      default: function() {
        return '420px';
      },
    },
  },
  data: () => ({
    dialog: false,
  }),
  watch: {
    dialog() {
      if (this.dialog) {
        this.$emit('dialog-opened');
      } else {
        this.$emit('dialog-closed');
      }
    },
  },
  methods: {
    ok() {
      this.$emit('ok');
      this.dialog = false;
    },
    open() {
      this.dialog = true;
    },
    close() {
      this.$emit('closed');
      this.dialog = false;
    },
  },
};
</script>