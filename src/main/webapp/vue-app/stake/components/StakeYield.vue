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
        {{ $t('yields') }}
      </v-card-title>
      <small v-if="!rewardsStarted" class="d-flex">
        <span class="my-auto">{{ $t('startsAfter') }}</span>
        <deeds-timer
          :end-time="meedsStartRewardsTime"
          class="my-auto"
          @end="endCountDown = true" />
      </small>
    </flex>
    <v-card-text class="ps-0">
      {{ $t('yieldsIntroduction1') }}
      <br>
      {{ $t('yieldsIntroduction2') }}
    </v-card-text>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    endCountDown: false,
  }),
  computed: Vuex.mapState({
    meedsStartRewardsTime: state => state.meedsStartRewardsTime,
    rewardsStarted() {
      return !this.endCountDown && this.meedsStartRewardsTime < Date.now();
    },
  }),
};
</script>