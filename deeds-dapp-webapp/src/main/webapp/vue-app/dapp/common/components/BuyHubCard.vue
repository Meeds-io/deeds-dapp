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
    :class="extraClass"
    :href="link"
    class="pa-5 d-flex flex-column justify-center rounded-lg mx-auto"
    max-width="400px"
    min-height="325px"
    hover
    outlined>
    <v-card-title class="justify-center flex-nowrap">
      <v-icon size="65" color="secondary">
        <slot name="icon"></slot>
      </v-icon>
      <span class="font-weight-bold ms-8">
        <slot name="title"></slot>
      </span>
    </v-card-title>
    <v-card-actions class="justify-center pa-4">
      <v-btn
        :href="link"
        class="primary-border-color px-7"
        color="primary"
        text
        outlined>
        <slot name="button"></slot>
      </v-btn>
    </v-card-actions>
  </v-card>
</template>
<script>
export default {
  props: {
    extraClass: {
      type: String,
      default: null,
    },
    link: {
      type: String,
      default: null,
    }
  }
};
</script>