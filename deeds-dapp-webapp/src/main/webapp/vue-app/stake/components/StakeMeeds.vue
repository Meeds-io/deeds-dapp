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
    <v-card-title class="d-flex flex-column justify-center pb-2">
      <v-icon>mdi-key</v-icon>
      <span>{{ $t('meedsStakes') }}</span>
    </v-card-title>
    <v-card
      class="d-flex flex-column flex-grow-1"
      flat
      tile>
      <v-list>
        <v-list-item>
          <v-list-item-content class="pb-0">
            <v-list-item-title>
              {{ $t('apy') }}
            </v-list-item-title>
            <v-list-item-subtitle class="font-weight-bold ms-2">
              <v-skeleton-loader
                v-if="apyLoading"
                type="chip"
                max-height="17"
                tile />
              <v-tooltip v-else bottom>
                <template #activator="{ on, attrs }">
                  <div
                    class="d-flex flex-nowrap"
                    v-bind="attrs"
                    v-on="on">
                    <deeds-number-format
                      :value="apy"
                      no-decimals>
                      %
                    </deeds-number-format>
                  </div>
                </template>
                <span v-if="maxMeedSupplyReached">
                  {{ $t('maxMeedsSupplyReached') }}
                </span>
                <ul v-else>
                  <li>
                    <deeds-number-format
                      :fractions="2"
                      :value="yearlyRewardedMeeds"
                      label="yearlyRewardedMeeds" />
                  </li>
                  <li>
                    <deeds-number-format
                      :fractions="2"
                      :value="meedsTotalBalanceOfXMeeds"
                      label="meedsTotalBalanceOfXMeeds" />
                  </li>
                </ul>
              </v-tooltip>
            </v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
        <v-list-item two-line>
          <v-list-item-content class="pb-0">
            <v-list-item-title>
              {{ $t('totalHoldings') }}
            </v-list-item-title>
            <v-list-item-subtitle class="font-weight-bold ms-2">
              <template v-if="meedsBalanceOfXMeeds === null || meedsPendingBalanceOfXMeeds == null">
                <v-skeleton-loader
                  type="chip"
                  max-height="17"
                  tile />
                <v-skeleton-loader
                  type="chip"
                  max-height="17"
                  tile />
              </template>
              <v-tooltip v-else bottom>
                <template #activator="{ on, attrs }">
                  <div
                    v-bind="attrs"
                    v-on="on">
                    <deeds-number-format :value="meedsTotalBalanceOfXMeeds" :fractions="2">
                      <deeds-contract-address
                        :address="meedAddress"
                        :button-top="-2"
                        label="MEED"
                        token />
                    </deeds-number-format>
                    <deeds-number-format
                      :value="meedsTotalBalanceOfXMeeds"
                      class="caption"
                      currency />
                  </div>
                </template>
                <ul>
                  <li>
                    <deeds-number-format
                      :fractions="2"
                      :value="meedsBalanceOfXMeeds"
                      label="xMeedCurrentBalance" />
                  </li>
                  <li v-if="!maxMeedSupplyReached">
                    <deeds-number-format
                      :fractions="2"
                      :value="meedsPendingBalanceOfXMeeds"
                      label="xMeedPendingRewards" />
                  </li>
                </ul>
              </v-tooltip>
            </v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
      </v-list>
      <strong class="d-flex flex-row">
        <v-divider class="ms-8 me-2 my-auto" />
        <h6>{{ $t('myAssets') }}</h6>
        <v-divider class="me-8 ms-2 my-auto" />
      </strong>
      <div v-if="metamaskOffline" class="d-flex flex-grow-1 flex-shrink-0">
        <deeds-metamask-button class="ma-auto" />
      </div>
      <template v-else>
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
                {{ meedsBalanceNoDecimals }}
                <deeds-contract-address
                  :address="meedAddress"
                  :button-top="-2"
                  label="MEED"
                  token />
              </template>
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              name="openStakeDrawerButton"
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
              <v-tooltip v-else-if="equivalentMeedBalance" bottom>
                <template #activator="{ on, attrs }">
                  <div
                    v-bind="attrs"
                    v-on="on">
                    {{ xMeedsBalanceNoDecimals }}
                    <deeds-contract-address
                      :address="xMeedAddress"
                      :button-top="-2"
                      label="xMEED"
                      token />
                  </div>
                </template>
                <deeds-number-format
                  :value="equivalentMeedBalance"
                  :fractions="2"
                  label="equivalentXMeedBalanceInMeed" />
              </v-tooltip>
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
          <v-list-item-action>
            <v-btn
              name="openUnstakeDrawerButton"
              outlined
              text
              @click="openStakeDrawer(false)">
              <span class="text-none">{{ $t('unstake') }}</span>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </template>
    </v-card>
    <deeds-stake-meeds-drawer
      v-if="!metamaskOffline"
      ref="stakeDrawer"
      :stake="stake" />
  </v-card>
</template>
<script>
export default {
  data: () => ({
    stake: true,
  }),
  computed: Vuex.mapState({
    meedsBalanceOfXMeeds: state => state.meedsBalanceOfXMeeds,
    meedsBalance: state => state.meedsBalance,
    meedsBalanceNoDecimals: state => state.meedsBalanceNoDecimals,
    metamaskOffline: state => state.metamaskOffline,
    meedAddress: state => state.meedAddress,
    xMeedAddress: state => state.xMeedAddress,
    xMeedsBalance: state => state.xMeedsBalance,
    xMeedsTotalSupply: state => state.xMeedsTotalSupply,
    xMeedsBalanceNoDecimals: state => state.xMeedsBalanceNoDecimals,
    meedsPendingBalanceOfXMeeds: state => state.meedsPendingBalanceOfXMeeds,
    rewardedMeedPerMinute: state => state.rewardedMeedPerMinute,
    rewardedFunds: state => state.rewardedFunds,
    rewardedTotalAllocationPoints: state => state.rewardedTotalAllocationPoints,
    rewardedTotalFixedPercentage: state => state.rewardedTotalFixedPercentage,
    maxMeedSupplyReached: state => state.maxMeedSupplyReached,
    yearInMinutes: state => state.yearInMinutes,
    xMeedRewardInfo() {
      return this.rewardedFunds && this.xMeedAddress && this.rewardedFunds.find(fund => fund.address.toUpperCase() === this.xMeedAddress.toUpperCase());
    },
    meedsTotalBalanceOfXMeeds() {
      const meedsPendingBalanceOfXMeeds = this.maxMeedSupplyReached && '0' || this.meedsPendingBalanceOfXMeeds;
      return this.meedsBalanceOfXMeeds
        && this.meedsPendingBalanceOfXMeeds
        && this.meedsBalanceOfXMeeds.add(meedsPendingBalanceOfXMeeds)
        || 0;
    },
    yearlyRewardedMeeds() {
      if (this.xMeedRewardInfo) {
        if (this.xMeedRewardInfo.fixedPercentage && !this.xMeedRewardInfo.fixedPercentage.isZero()) {
          return new BigNumber(this.rewardedMeedPerMinute.toString())
            .multipliedBy(this.yearInMinutes)
            .multipliedBy(this.xMeedRewardInfo.fixedPercentage.toString())
            .dividedBy(100);
        } else if (this.xMeedRewardInfo.allocationPoint && !this.xMeedRewardInfo.allocationPoint.isZero()) {
          return new BigNumber(this.rewardedMeedPerMinute.toString())
            .multipliedBy(this.yearInMinutes)
            .multipliedBy(this.xMeedRewardInfo.allocationPoint.toString())
            .dividedBy(this.rewardedTotalAllocationPoints.toString())
            .dividedBy(100)
            .multipliedBy(100 - this.rewardedTotalFixedPercentage.toNumber());
        }
      }
      return new BigNumber(0);
    },
    apyLoading() {
      return this.meedsBalanceOfXMeeds === null || this.rewardedFunds === null || this.meedsPendingBalanceOfXMeeds === null;
    },
    equivalentMeedBalance() {
      if (this.xMeedsTotalSupply && this.xMeedsBalance && this.meedsTotalBalanceOfXMeeds && !this.xMeedsTotalSupply.isZero()) {
        return this.xMeedsBalance.mul(this.meedsTotalBalanceOfXMeeds).div(this.xMeedsTotalSupply);
      } else {
        return 0;
      }
    },
    apy() {
      if (!this.meedsTotalBalanceOfXMeeds
          || this.meedsTotalBalanceOfXMeeds.isZero()
          || !this.yearlyRewardedMeeds
          || this.yearlyRewardedMeeds.isZero()
          || this.maxMeedSupplyReached) {
        return 0;
      }
      return this.yearlyRewardedMeeds.dividedBy(this.meedsTotalBalanceOfXMeeds.toString()).multipliedBy(100);
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