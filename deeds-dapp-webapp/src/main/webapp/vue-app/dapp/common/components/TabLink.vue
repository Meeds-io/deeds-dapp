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
  <v-btn
    height="auto"
    color="primary"
    class="pe-0 d-inline-block text-wrap"
    text
    @click="clickOnLink">
    <span class="font-weight-bold text-h6 text-sm-h5">{{ label }}</span>
  </v-btn>
</template>
<script>
export default {
  props: {
    label: {
      type: String,
      default: null,
    },
    tabLink: {
      type: String,
      default: null,
    },
  },
  methods: {
    clickOnLink(event) {
      this.$emit('click', event);
      this.$root.$emit('switch-page', this.tabLink);
    },
  },
};
</script>