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
  <v-btn
    height="auto"
    color="primary"
    class="font-size-normal"
    text
    @click="$root.$emit('switch-page', tabLink)">
    <span class="text-none">
      <span class="font-weight-bold">{{ label }}</span>
      <span v-if="subLabel" class="font-weight-normal">({{ subLabel }})</span>
    </span>
  </v-btn>
</template>
<script>
export default {
  props: {
    label: {
      type: String,
      default: null,
    },
    subLabel: {
      type: String,
      default: null,
    },
    tabLink: {
      type: String,
      default: null,
    },
  },
};
</script>