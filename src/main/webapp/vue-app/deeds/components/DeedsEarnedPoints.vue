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
  <v-card
    width="340"
    height="350"
    outlined>
    <v-card-title class="d-flex flex-column justify-center">
      <v-icon>mdi-office-building-outline</v-icon>
      <span>{{ $t('xMeedsStakes') }}</span>
    </v-card-title>
    <v-card-text>
      <v-list-item two-line class="mb-2">
        <v-list-item-content>
          <v-list-item-title>
            {{ $t('totalHoldings') }}
          </v-list-item-title>
          <v-list-item-subtitle class="font-weight-bold ms-2">
            <v-skeleton-loader
              v-if="xMeedsTotalSupply === null"
              type="chip"
              max-height="17"
              tile />
            <deeds-number-format v-else :value="xMeedsTotalSupply">
              xMEED
            </deeds-number-format>
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
      <strong class="d-flex flex-row">
        <v-divider class="ms-8 me-2 my-auto" />
        <h6>{{ $t('myAssets') }}</h6>
        <v-divider class="me-8 ms-2 my-auto" />
      </strong>
      <v-list-item two-line class="mt-2">
        <v-list-item-content>
          <v-list-item-title>
            {{ $t('staked') }}
          </v-list-item-title>
          <v-list-item-subtitle class="font-weight-bold ms-2">
            <v-skeleton-loader
              v-if="xMeedsBalance === null"
              type="chip"
              max-height="17"
              tile />
            <template v-else>{{ xMeedsBalanceNoDecimals }} xMEED</template>
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
      <v-list-item two-line>
        <v-list-item-content>
          <v-list-item-title>
            {{ $t('earned') }}
          </v-list-item-title>
          <v-list-item-subtitle class="font-weight-bold ms-2">
            <v-skeleton-loader
              v-if="pointsBalance === null"
              type="chip"
              max-height="17"
              tile />
            <deeds-number-format v-else-if="rewardsStarted" :value="pointsBalance">
              {{ $t('points') }}
            </deeds-number-format>
            <v-tooltip v-else bottom>
              <template v-slot:activator="{ on, attrs }">
                <div
                  class="d-flex flex-nowrap"
                  v-bind="attrs"
                  v-on="on">
                  <deeds-number-format :value="pointsBalance">
                    {{ $t('points') }}
                  </deeds-number-format>
                  <v-icon
                    size="15px"
                    color="primary"
                    class="ms-2">
                    mdi-alert-circle-outline
                  </v-icon>
                </div>
              </template>
              <div>
                {{ $t('meedsRewardingDidntStarted') }}
              </div>
            </v-tooltip>
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
    </v-card-text>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    stake: true,
    now: Date.now(),
    refreshInterval: null,
  }),
  computed: Vuex.mapState({
    language: state => state.language,
    xMeedsTotalSupply: state => state.xMeedsTotalSupply,
    meedsBalance: state => state.meedsBalance,
    xMeedsBalance: state => state.xMeedsBalance,
    pointsBalance: state => state.pointsBalance,
    xMeedsBalanceNoDecimals: state => state.xMeedsBalanceNoDecimals,
    pointsStartRewardsTime: state => state.pointsStartRewardsTime,
    rewardsStarted() {
      return !this.endCountDown && this.pointsStartRewardsTime < this.now;
    },
  }),
  watch: {
    rewardsStarted() {
      if (this.rewardsStarted && this.refreshInterval) {
        window.clearInterval(this.refreshInterval);
      }
    },
  },
  created() {
    if (!this.rewardsStarted) {
      this.refreshInterval = window.setInterval(() => {
        this.now = Date.now();
      }, 1000);
    }
  },
};
</script>