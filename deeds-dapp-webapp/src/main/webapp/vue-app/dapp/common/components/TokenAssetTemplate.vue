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
  <v-row class="pt-8 ma-0 d-flex flex-grow-1">
    <v-col class="pa-0">
      <v-list-item class="px-0 height-auto">
        <v-list-item-avatar
          v-if="$slots.image"
          class="my-0"
          width="50px"
          height="50px"
          min-width="50px"
          tile>
          <slot name="image"></slot>
        </v-list-item-avatar>
        <v-list-item-content class="py-0 overflow-visible">
          <v-list-item-title class="token-asset-title height-auto text-wrap d-flex text-h6 text-sm-h5 pe-2">
            <div class="my-auto me-auto">
              <slot name="col1"></slot>
            </div>
          </v-list-item-title>
          <v-list-item-subtitle class="token-asset-subtitle d-flex subtitle-1">
            <div class="my-auto me-auto">
              <slot name="col2"></slot>
            </div>
          </v-list-item-subtitle>
        </v-list-item-content>
        <v-list-item-content v-if="extraCols" class="py-0 overflow-visible flex-grow-0">
          <v-list-item-title class="dark-grey--text token-asset-title height-auto d-flex justify-center text-h6 text-sm-h5 font-weight-normal overflow-visible">
            <div class="my-auto pt-2px">
              <slot name="col5"></slot>
            </div>
          </v-list-item-title>
          <v-list-item-subtitle class="token-asset-subtitle d-flex subtitle-1">
            <div class="my-auto">
              <slot name="col6"></slot>
            </div>
          </v-list-item-subtitle>
        </v-list-item-content>
        <v-list-item-content class="py-0 overflow-visible">
          <v-list-item-title class="dark-grey--text token-asset-title d-flex text-h6 text-sm-h5 font-weight-normal overflow-visible">
            <div class="my-auto ms-auto">
              <slot name="col3"></slot>
            </div>
          </v-list-item-title>
          <v-list-item-subtitle class="token-asset-subtitle d-flex subtitle-1">
            <div class="mt-auto ms-auto">
              <slot name="col4"></slot>
            </div>
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
    </v-col>
  </v-row>
</template>
<script>
export default {
  props: {
    extraCols: {
      type: Boolean,
      default: true,
    },
  },
};
</script>