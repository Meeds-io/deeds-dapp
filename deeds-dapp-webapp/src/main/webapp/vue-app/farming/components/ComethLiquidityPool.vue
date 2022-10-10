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
  <v-hover v-slot="{hover}">
    <v-card
      :elevation="hover ? 3 : 0"
      class="mx-auto ma-2 d-flex flex-column"
      width="400px"
      max-width="100%"
      outlined>
      <v-card flat tile>
        <v-card-text class="d-flex flex-column">
          <img
            :src="`/${parentLocation}/static/images/cometh.ico`"
            class="mx-auto addLiquidityIcon"
            alt="Cometh Icon">
        </v-card-text>
        <v-card-title class="justify-center pt-0 text-center text-break">
          {{ $t('rentLiquidityOnCometh') }}
        </v-card-title>
        <v-card-subtitle class="justify-center pt-0 text-center text-break">
          (Polygon)
        </v-card-subtitle>
      </v-card>
      <v-list class="mt-n5">
        <v-list-item>
          <v-list-item-content class="pb-0">
            <v-list-item-title>
              {{ $t('apy') }}
            </v-list-item-title>
            <v-list-item-subtitle class="font-weight-bold">
              <v-skeleton-loader
                v-if="loading"
                type="chip"
                max-height="17"
                tile />
              <v-tooltip v-else bottom>
                <template #activator="{ on, attrs }">
                  <div
                    class="d-flex flex-nowrap ms-2"
                    v-bind="attrs"
                    v-on="on">
                    <deeds-number-format
                      :value="apy"
                      no-decimals>
                      %
                    </deeds-number-format>
                  </div>
                </template>
                <ul>
                  <li>
                    <deeds-number-format :value="yearlyRewardedMeeds" label="yearlyRewardedMeeds" />
                  </li>
                  <li>
                    <deeds-number-format :value="stakedEquivalentMeedsBalanceOfPool" label="meedsBalanceOfPool" />
                  </li>
                </ul>
              </v-tooltip>
            </v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
        <v-list-item two-line>
          <v-list-item-content>
            <v-list-item-title>
              {{ $t('totalHoldings') }}
            </v-list-item-title>
            <v-list-item-subtitle class="font-weight-bold ms-2">
              <template v-if="loading">
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
                <deeds-number-format :value="lpBalanceOfTokenFactory" :fractions="2">
                  <deeds-contract-address
                    :address="lpAddress"
                    :button-top="-2"
                    label="UNI-V2"
                    token
                    polygon />
                </deeds-number-format>
                <deeds-number-format
                  :value="stakedEquivalentMeedsBalanceOfPool"
                  class="caption"
                  currency />
              </template>
            </v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
      </v-list>
      <strong class="d-flex flex-row">
        <v-divider class="ms-8 me-2 my-auto" />
        <h6>{{ $t('myAssets') }}</h6>
        <v-divider class="me-8 ms-2 my-auto" />
      </strong>
      <div v-if="!address" class="d-flex flex-grow-1 flex-shrink-0">
        <deeds-metamask-button class="mx-auto my-12" />
      </div>
      <template v-else>
        <v-list-item two-line>
          <v-list-item-content>
            <v-list-item-title>
              {{ $t('availableToStake') }}
            </v-list-item-title>
            <v-list-item-subtitle class="font-weight-bold ms-2">
              <v-skeleton-loader
                v-if="loading"
                type="chip"
                max-height="17"
                tile />
              <deeds-number-format
                v-else
                :value="lpBalance"
                :fractions="2">
                <deeds-contract-address
                  :address="lpAddress"
                  :button-top="-2"
                  label="UNI-V2"
                  token
                  polygon />
              </deeds-number-format>
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              :href="rentComethLiquidityLink"
              target="_blank"
              rel="nofollow noreferrer"
              class="mx-auto d-flex"
              outlined
              text>
              <v-icon class="me-2" small>fa-external-link</v-icon>
              <span class="text-capitalize">{{ $t('stake') }}</span>
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
                v-if="loading"
                type="chip"
                max-height="17"
                tile />
              <deeds-number-format
                v-else
                :value="lpStaked"
                :fractions="2">
                <deeds-contract-address
                  :address="lpAddress"
                  :button-top="-2"
                  label="UNI-V2"
                  token
                  polygon />
              </deeds-number-format>
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              :href="rentComethLiquidityLink"
              target="_blank"
              rel="nofollow noreferrer"
              class="mx-auto d-flex"
              outlined
              text>
              <v-icon class="me-2" small>fa-external-link</v-icon>
              <span class="text-capitalize">{{ $t('unstake') }}</span>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
        <v-list-item two-line>
          <v-list-item-content>
            <v-list-item-title>
              {{ $t('earned') }}
            </v-list-item-title>
            <v-list-item-subtitle class="font-weight-bold ms-2">
              <template v-if="loading">
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
                <deeds-number-format
                  :value="meedsPendingUserReward"
                  :fractions="2">
                  <deeds-contract-address
                    :address="polygonMeedAddress"
                    label="MEED (Polygon)"
                    :button-top="-2"
                    token
                    polygon />
                </deeds-number-format>
                <deeds-number-format
                  :value="meedsPendingUserReward"
                  :fractions="2"
                  class="caption"
                  currency />
              </template>
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              :href="rentComethLiquidityLink"
              target="_blank"
              rel="nofollow noreferrer"
              class="mx-auto d-flex"
              outlined
              text>
              <v-icon class="me-2" small>fa-external-link</v-icon>
              <span class="text-capitalize">{{ $t('claim') }}</span>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </template>
    </v-card>
  </v-hover>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    address: state => state.address,
    polygonMeedAddress: state => state.polygonMeedAddress,
    lpAddress: state => state.comethPairAddress,
    rentComethLiquidityLink: state => state.rentComethLiquidityLink,
    pool: state => state.comethPool,
    loading() {
      return !this.pool;
    },
    userInfo() {
      return this.pool?.userInfo;
    },
    lpStaked() {
      return this.userInfo?.amount || 0;
    },
    lpBalance() {
      return this.userInfo?.lpBalance || 0;
    },
    meedsPendingUserReward() {
      return this.userInfo?.meedsPendingUserReward || 0;
    },
    stakedEquivalentMeedsBalanceOfPool() {
      return this.pool?.stakedEquivalentMeedsBalanceOfPool;
    },
    yearlyRewardedMeeds() {
      return this.pool?.yearlyRewardedMeeds;
    },
    lpBalanceOfTokenFactory() {
      return this.pool?.lpBalanceOfTokenFactory;
    },
    meedsBalanceOfPool() {
      return this.pool?.meedsBalance;
    },
    apy() {
      return this.pool?.apy;
    },
  }),
  created() {
    this.$store.commit('loadComethRewardPool');
  },
};
</script>