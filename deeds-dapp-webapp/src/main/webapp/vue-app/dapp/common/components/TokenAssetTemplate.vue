<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2022 Meeds Association contact@meeds.io
 
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
  <v-row class="px-3 px-sm-8 pt-4 ma-0 d-flex flex-grow-1">
    <v-col class="pa-0">
      <v-list-item class="px-0 height-auto">
        <v-list-item-avatar
          v-if="$slots.image"
          class="my-0"
          tile>
          <slot name="image"></slot>
        </v-list-item-avatar>
        <v-list-item-content class="py-0 flex-grow-1">
          <v-list-item-title class="token-asset-title height-auto text-wrap d-flex mb-0 font-size-normal pe-2">
            <div class="my-auto me-auto">
              <slot name="col1"></slot>
            </div>
          </v-list-item-title>
          <v-list-item-subtitle class="token-asset-subtitle d-flex caption">
            <div class="my-auto me-auto">
              <slot name="col2"></slot>
            </div>
          </v-list-item-subtitle>
        </v-list-item-content>
        <v-list-item-content class="py-0 flex-grow-0 overflow-visible mb-auto">
          <v-list-item-title class="token-asset-title d-flex mb-0 font-size-normal overflow-visible">
            <div class="my-auto ms-auto pt-2px">
              <slot name="col3"></slot>
            </div>
          </v-list-item-title>
          <v-list-item-subtitle class="token-asset-subtitle d-flex caption">
            <div class="my-auto ms-auto">
              <slot name="col4"></slot>
            </div>
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
    </v-col>
  </v-row>
</template>