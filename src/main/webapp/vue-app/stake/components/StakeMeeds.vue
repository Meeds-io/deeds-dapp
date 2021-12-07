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
    class="elevation-2"
    flat>
    <v-card-title class="d-flex flex-column justify-center pb-2">
      <v-icon>mdi-key</v-icon>
      <span>{{ $t('meedsStakes') }}</span>
    </v-card-title>
    <v-card-text>
      <v-list-item>
        <v-list-item-content class="pb-0">
          <v-list-item-title>
            {{ $t('apy') }}
          </v-list-item-title>
          <v-list-item-subtitle>
            -
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
      <v-list-item two-line>
        <v-list-item-content>
          <v-list-item-title>
            {{ $t('totalHoldings') }}
          </v-list-item-title>
          <v-list-item-subtitle class="font-weight-bold ms-2">
            <v-skeleton-loader
              v-if="meedsBalanceOfXMeeds === null"
              type="chip"
              max-height="17"
              tile />
            <template v-else>
              {{ meedsBalanceOfXMeedsNoDecimals }} MEED
            </template>
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
      <strong class="d-flex flex-row">
        <v-divider class="ms-8 me-2 my-auto" />
        <h6>{{ $t('myAssets') }}</h6>
        <v-divider class="me-8 ms-2 my-auto" />
      </strong>
      <v-list-item two-line>
        <v-list-item-content>
          <v-list-item-title>
            {{ $t('availableToStake') }}
          </v-list-item-title>
          <v-list-item-subtitle class="font-weight-bold ms-2">
            <v-skeleton-loader
              v-if="meedsBalance === null"
              type="chip"
              max-height="17"
              tile />
            <template v-else>
              {{ meedsBalanceNoDecimals }} MEED
            </template>
          </v-list-item-subtitle>
        </v-list-item-content>
        <v-list-item-action>
          <v-btn
            outlined
            text
            @click="openStakeDrawer(true)">
            <span class="text-none">{{ $t('stake') }}</span>
          </v-btn>
        </v-list-item-action>
      </v-list-item>
      <v-list-item two-line>
        <v-list-item-content>
          <v-list-item-title>
            {{ $t('balance') }}
          </v-list-item-title>
          <v-list-item-subtitle class="font-weight-bold ms-2">
            <v-skeleton-loader
              v-if="xMeedsBalance === null"
              type="chip"
              max-height="17"
              tile />
            <template v-else>
              {{ xMeedsBalanceNoDecimals }} xMEED
            </template>
          </v-list-item-subtitle>
        </v-list-item-content>
        <v-list-item-action>
          <v-btn
            outlined
            text
            @click="openStakeDrawer(false)">
            <span class="text-none">{{ $t('unstake') }}</span>
          </v-btn>
        </v-list-item-action>
      </v-list-item>
    </v-card-text>
    <deeds-stake-meeds-drawer ref="stakeDrawer" :stake="stake" />
  </v-card>
</template>
<script>
export default {
  data: () => ({
    stake: true,
  }),
  computed: Vuex.mapState({
    language: state => state.language,
    meedsBalanceOfXMeeds: state => state.meedsBalanceOfXMeeds,
    meedsBalance: state => state.meedsBalance,
    meedsBalanceNoDecimals: state => state.meedsBalanceNoDecimals,
    xMeedsBalance: state => state.xMeedsBalance,
    xMeedsBalanceNoDecimals: state => state.xMeedsBalanceNoDecimals,
    meedsBalanceOfXMeedsNoDecimals() {
      return this.meedsBalanceOfXMeeds && this.$ethUtils.computeTokenBalanceNoDecimals(
        this.meedsBalanceOfXMeeds,
        3,
        this.language);
    },
  }),
  methods: {
    openStakeDrawer(stake) {
      this.stake = stake;
      if (this.$refs && this.$refs.stakeDrawer) {
        this.$refs.stakeDrawer.open();
      }
    },
  },
};
</script>