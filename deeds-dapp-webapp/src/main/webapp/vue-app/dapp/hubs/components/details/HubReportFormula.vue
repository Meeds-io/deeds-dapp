<!--

 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 
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
    <div class="text-subtitle-1 font-weight-bold mb-8">
      {{ $t('uem.rewardComputing') }}
    </div>
    <deed-hub-report-formula-computing-result
      :reward="reward"
      :report="report" />
    <div class="text-subtitle-1 font-weight-bold my-8">
      {{ $t('uem.engagementScoreComputing') }}
    </div>
    <deed-hub-report-formula-computing
      :reward="reward"
      :report="report" />
    <div class="d-flex flex-column fa-lg my-8">
      {{ $t('uem.where') }}
    </div>
    <div class="reset-style-box">
      <ul>
        <deed-hub-report-engagement-score
          :reward="reward"
          :report="report" />
        <deed-hub-report-engagement-formula
          :reward="reward"
          :report="report" />
        <deed-hub-report-distribution-rate :report="report" />
        <deed-hub-report-distribution-spread :report="report" />
        <deed-hub-report-minting-power :report="report" />
      </ul>
    </div>
  </v-card>
</template>
<script>
export default {
  props: {
    reward: {
      type: Object,
      default: null,
    },
    report: {
      type: Object,
      default: null,
    },
  },
};
</script>