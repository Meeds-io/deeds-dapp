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
  <v-card
    min-height="50vh"
    class="d-flex flex-column flex-grow-1 align-center justify-center py-4"
    flat>
    <v-icon size="100" color="primary">
      far fa-question-circle
    </v-icon>
    <div class="text-color headline mt-6">
      {{ $t('wom.hubReportNotFound') }}
    </div>
    <v-card
      class="text-color mt-6 d-none d-sm-inline title"
      max-width="80%"
      min-width="50vw"
      flat>
      {{ $t('wom.hubReportNotFoundMessage', {0: reportId}) }}
    </v-card>
    <v-btn
      class="primary mt-6"
      elevation="0"
      large
      @click="$root.$emit('close-hub-details')">
      <span class="text-none title">
        {{ $t('wom.backToHubList') }}
      </span>
    </v-btn>
  </v-card>
</template>
<script>
export default {
  props: {
    reportId: {
      type: String,
      default: null,
    },
  },
};
</script>