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
  <v-row class="ma-0">
    <v-col class="pa-0 mt-6 mb-12">
      <div class="d-flex flex-column">
        <span 
          class="text-h6 font-weight-normal dark-grey--text" 
          v-html="$t(descriptionPart1, {0: `<a class='${id} primary--text embedded-link-text font-weight-bold v-btn v-btn--text theme--light text-none letter-spacing-normal' onclick='javascript:void(0)'>${$t(linkPart1)}</a>`})">
        </span>
        <span 
          class="text-h6 font-weight-normal dark-grey--text mt-4" 
          v-html="$t(descriptionPart2, {0: `<a class='${id} primary--text embedded-link-text font-weight-bold v-btn v-btn--text theme--light text-none letter-spacing-normal' onclick='javascript:void(0)'>${$t(linkPart2)}</a>`})">
        </span>
      </div>
    </v-col>
  </v-row>
</template>
<script>
export default {
  props: {
    id: {
      type: String,
      default: null,
    },
    descriptionPart1: {
      type: String,
      default: null,
    },
    descriptionPart2: {
      type: String,
      default: null,
    },
    linkPart1: {
      type: String,
      default: null,
    },
    linkPart2: {
      type: String,
      default: null,
    },
    targetTab: {
      type: String,
      default: null,
    },
  },
  mounted() {
    const links = document.querySelectorAll(`.${this.id}`);
    if (links.length) {
      links.forEach(link => link.onclick = () => this.$root.$emit('switch-page', this.targetTab));
    }
  },
};
</script>
