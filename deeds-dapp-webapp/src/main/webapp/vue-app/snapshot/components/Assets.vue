<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 
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
  <div class="mb-4 mb-sm-8">
    <h3 class="d-flex flex-nowrap">
      {{ $t('assets') }}
      <v-divider class="my-auto ms-4" />
    </h3>
    <div class="d-flex flex-column flex-sm-row">
      <v-card flat class="assets">
        <deeds-token-assets />
        <deeds-deed-assets />
      </v-card>
      <deeds-trade-meeds class="mx-auto ms-sm-32 me-sm-15 mb-8 mb-sm-0 flex-shrink-0 flex-grow-0" />
    </div>
  </div>
</template>