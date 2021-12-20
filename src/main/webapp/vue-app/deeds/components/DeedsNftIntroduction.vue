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
  <v-card flat>
    <flex class="d-flex flex-nowrap">
      <v-card-title class="ps-0 py-0">
        {{ $t('deedsNfts') }}
      </v-card-title>
      <small v-if="!rewardsStarted" class="d-flex">
        <span class="mt-auto mb-1">{{ $t('startsAfter') }}</span>
        <deeds-timer
          :end-time="pointsStartRewardsTime"
          class="mt-auto mb-1"
          @end="endCountDown = true" />
      </small>
    </flex>
    <v-card-text class="ps-0">
      {{ $t('deedPointsIntroduction') }}
    </v-card-text>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    endCountDown: false,
  }),
  computed: Vuex.mapState({
    pointsStartRewardsTime: state => state.pointsStartRewardsTime,
    rewardsStarted() {
      return !this.endCountDown && this.pointsStartRewardsTime < Date.now();
    },
  }),
};
</script>