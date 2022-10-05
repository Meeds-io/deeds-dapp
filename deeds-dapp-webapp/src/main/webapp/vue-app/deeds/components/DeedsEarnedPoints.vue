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
    class="d-flex flex-column"
    width="340"
    height="350"
    outlined>
    <v-card-title class="d-flex flex-column justify-center">
      <v-icon>mdi-office-building-outline</v-icon>
      <span>{{ $t('xMeedsStakes') }}</span>
    </v-card-title>
    <v-card
      class="d-flex flex-column flex-grow-1"
      flat
      tile>
      <v-list>
        <v-list-item two-line class="mb-2">
          <v-list-item-content>
            <v-list-item-title>
              {{ $t('totalHoldings') }}
            </v-list-item-title>
            <v-list-item-subtitle class="font-weight-bold ms-2">
              <template v-if="xMeedsTotalSupply === null">
                <v-skeleton-loader
                  type="chip"
                  max-height="17"
                  tile />
                <v-skeleton-loader
                  type="chip"
                  max-height="17"
                  tile />
              </template>
              <template v-else>
                <deeds-number-format :value="xMeedsTotalSupply">
                  <deeds-contract-address
                    :address="xMeedAddress"
                    :button-top="-2"
                    label="xMEED"
                    token />
                </deeds-number-format>
                <deeds-number-format
                  :value="meedsTotalBalanceOfXMeeds"
                  class="caption"
                  currency />
              </template>
            </v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
      </v-list>
      <strong class="d-flex flex-row">
        <v-divider class="ms-8 me-2 my-auto" />
        <div>{{ $t('myAssets') }}</div>
        <v-divider class="me-8 ms-2 my-auto" />
      </strong>
      <div v-if="metamaskOffline" class="d-flex flex-grow-1 flex-shrink-0">
        <deeds-metamask-button class="ma-auto" />
      </div>
      <template v-else>
        <v-list-item
          two-line
          class="mt-2">
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
              <template v-else>
                {{ xMeedsBalanceNoDecimals }}
                <deeds-contract-address
                  :address="xMeedAddress"
                  :button-top="-2"
                  label="xMEED"
                  token />
              </template>
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
              <deeds-number-format
                v-else
                :value="pointsBalance"
                fractions="2">
                {{ $t('points') }}
              </deeds-number-format>
            </v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
      </template>
    </v-card>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    stake: true,
  }),
  computed: Vuex.mapState({
    language: state => state.language,
    metamaskOffline: state => state.metamaskOffline,
    xMeedAddress: state => state.xMeedAddress,
    xMeedsTotalSupply: state => state.xMeedsTotalSupply,
    meedsBalance: state => state.meedsBalance,
    xMeedsBalance: state => state.xMeedsBalance,
    pointsBalance: state => state.pointsBalance,
    xMeedsBalanceNoDecimals: state => state.xMeedsBalanceNoDecimals,
    maxMeedSupplyReached: state => state.maxMeedSupplyReached,
    meedsPendingBalanceOfXMeeds: state => state.meedsPendingBalanceOfXMeeds,
    meedsBalanceOfXMeeds: state => state.meedsBalanceOfXMeeds,
    meedsTotalBalanceOfXMeeds() {
      const meedsPendingBalanceOfXMeeds = this.maxMeedSupplyReached && '0' || this.meedsPendingBalanceOfXMeeds;
      return this.meedsBalanceOfXMeeds
        && this.meedsPendingBalanceOfXMeeds
        && this.meedsBalanceOfXMeeds.add(meedsPendingBalanceOfXMeeds)
        || 0;
    },
  }),
};
</script>