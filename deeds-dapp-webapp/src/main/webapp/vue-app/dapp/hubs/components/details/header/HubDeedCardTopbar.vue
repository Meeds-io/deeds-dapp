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
  <v-list-item class="px-0">
    <v-list-item-action class="me-2" @click="$root.$emit('close-hub-details')">
      <v-tooltip
        z-index="4"
        bottom>
        <template #activator="{on, attrs}">
          <v-btn
            class="mr-n2px"
            icon
            v-bind="attrs"
            v-on="on"
            @click="$root.$emit('close-hub-details')">
            <v-icon>fas fa-arrow-left</v-icon>
          </v-btn>
        </template>
        <span class="text-no-wrap">{{ $t('wom.hubBackToList') }}</span>
      </v-tooltip>
    </v-list-item-action>
    <v-list-item-content>
      <v-list-item-title class="d-flex">
        <v-tooltip
          z-index="4"
          bottom>
          <template #activator="{on, attrs}">
            <v-card
              class="flex-grow-0"
              color="transparent"
              flat
              v-bind="attrs"
              v-on="on"
              @click="$root.$emit('close-hub-details')">
              {{ $t('wom.hubBackToList') }}
            </v-card>
          </template>
          <span class="text-no-wrap">{{ $t('wom.hubBackToList') }}</span>
        </v-tooltip>
      </v-list-item-title>
    </v-list-item-content>
  </v-list-item>
</template>
