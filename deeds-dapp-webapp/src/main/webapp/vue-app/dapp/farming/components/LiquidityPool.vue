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
          <template v-if="$slots.icon">
            <slot name="icon"></slot>
          </template>
          <template v-else>
            <img
              v-if="isSushiswapPool"
              :src="`/${parentLocation}/static/images/sushiswap.ico`"
              alt="sushiswap"
              class="mx-auto addLiquidityIcon">
            <div v-else-if="isUniswapPool" class="mx-auto headline">🦄</div>
            <v-icon
              v-else
              size="32"
              class="mx-auto">
              fas fa-money-bill
            </v-icon>
          </template>
        </v-card-text>
        <v-card-title class="justify-center pt-0 text-center text-break">
          <template v-if="$slots.title">
            <slot name="title"></slot>
          </template>
          <template v-else-if="poolName">
            <span class="headline font-weight-medium">{{ $t('rentLiquidityOn', {0: poolName}) }}</span>
          </template>
          <template v-else>
            {{ $t('rentLiquidity') }}
          </template>
        </v-card-title>
      </v-card>
      <template v-if="$slots.content">
        <slot name="content"></slot>
      </template>
      <template v-else>
        <v-list>
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
                  <span v-if="noMeedSupplyForLPRemaining">
                    {{ $t('maxMeedsSupplyReached') }}
                  </span>
                  <ul v-else>
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
                      :label="lpSymbol"
                      token />
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
          <h6 class="font-weight-bold subtitle-1">{{ $t('myAssets') }}</h6>
          <v-divider class="me-8 ms-2 my-auto" />
        </strong>
        <div v-if="metamaskOffline" class="d-flex flex-grow-1 flex-shrink-0">
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
                  v-if="loadingBalance"
                  type="chip"
                  max-height="17"
                  tile />
                <deeds-number-format
                  v-else
                  :value="lpBalance"
                  :fractions="2">
                  <deeds-contract-address
                    :address="lpAddress"
                    :label="lpSymbol"
                    token />
                </deeds-number-format>
              </v-list-item-subtitle>
            </v-list-item-content>
            <v-list-item-action>
              <v-btn
                name="stakeLPTokenButton"
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
                  v-if="loadingUserInfo"
                  type="chip"
                  max-height="17"
                  tile />
                <deeds-number-format
                  v-else
                  :value="lpStaked"
                  :fractions="2">
                  <deeds-contract-address
                    :address="lpAddress"
                    :label="lpSymbol"
                    token />
                </deeds-number-format>
              </v-list-item-subtitle>
            </v-list-item-content>
            <v-list-item-action>
              <v-btn
                name="unstakeLPTokenButton"
                outlined
                text
                @click="openStakeDrawer(false)">
                <span class="text-none">{{ $t('unstake') }}</span>
              </v-btn>
            </v-list-item-action>
          </v-list-item>
          <v-list-item two-line>
            <v-list-item-content>
              <v-list-item-title>
                {{ $t('earned') }}
              </v-list-item-title>
              <v-list-item-subtitle class="font-weight-bold ms-2">
                <template v-if="loadingUserReward">
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
                      :address="meedAddress"
                      label="MEED"
                      token />
                  </deeds-number-format>
                  <deeds-number-format
                    :value="meedsPendingUserReward"
                    class="caption"
                    currency />
                </template>
              </v-list-item-subtitle>
            </v-list-item-content>
            <v-list-item-action>
              <v-btn
                name="claimRewardButton"
                :loading="sendingClaim"
                :disabled="sendingClaim"
                outlined
                text
                @click="claimReward()">
                {{ $t('claim') }}
              </v-btn>
            </v-list-item-action>
          </v-list-item>
        </template>
        <deeds-stake-liquidity-drawer
          v-if="!metamaskOffline"
          ref="stakeDrawer"
          :lp-address="lpAddress"
          :lp-symbol="lpSymbol"
          :lp-balance="lpBalance"
          :lp-staked="lpStaked"
          :lp-allowance="lpAllowance"
          :lp-contract="lpContract"
          :stake="stake" />
      </template>
    </v-card>
  </v-hover>
</template>
<script>
export default {
  props: {
    pool: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    loadingBalance: true,
    loadingUserReward: true,
    sendingClaim: false,
    meedsPendingUserReward: null,
    stake: true,
    lpBalance: null,
    lpAllowance: null,
  }),
  computed: Vuex.mapState({
    metamaskOffline: state => state.metamaskOffline,
    parentLocation: state => state.parentLocation,
    provider: state => state.provider,
    address: state => state.address,
    meedAddress: state => state.meedAddress,
    harvestGasLimit: state => state.harvestGasLimit,
    tokenFactoryAddress: state => state.tokenFactoryAddress,
    sushiswapPairAddress: state => state.sushiswapPairAddress,
    univ2PairAddress: state => state.univ2PairAddress,
    tokenFactoryContract: state => state.tokenFactoryContract,
    poolsChanged: state => state.poolsChanged,
    isSushiswapPool() {
      return this.metamaskOffline || this.lpAddress?.toUpperCase() === this.sushiswapPairAddress?.toUpperCase();
    },
    isUniswapPool() {
      return this.univ2PairAddress && this.lpAddress?.toUpperCase() === this.univ2PairAddress?.toUpperCase();
    },
    poolName() {
      if (this.isSushiswapPool) {
        return 'Sushiswap';
      } else if (this.isUniswapPool) {
        return 'Uniswap';
      }
      return this.lpSymbol;
    },
    lpAddress() {
      return this.poolsChanged > 1 && this.pool?.address;
    },
    lpContract() {
      return this.poolsChanged > 1 && this.pool?.contract;
    },
    lpSymbol() {
      return this.poolsChanged > 1 && this.pool?.symbol;
    },
    lpBalanceOfTokenFactory() {
      return this.poolsChanged > 1 && this.pool?.lpBalanceOfTokenFactory;
    },
    userInfo() {
      return this.poolsChanged > 1 && this.pool?.userInfo;
    },
    lpStaked() {
      return this.userInfo && this.userInfo.amount || 0;
    },
    stakedEquivalentMeedsBalanceOfPool() {
      return this.poolsChanged > 1 && this.pool?.stakedEquivalentMeedsBalanceOfPool;
    },
    yearlyRewardedMeeds() {
      return this.poolsChanged > 1 && this.pool?.yearlyRewardedMeeds;
    },
    meedsBalanceOfPool() {
      return this.poolsChanged > 1 && this.pool?.meedsBalance;
    },
    loadingUserInfo() {
      return this.poolsChanged > 1 && this.pool?.loadingUserInfo;
    },
    loading() {
      return this.poolsChanged > 1 && this.pool?.loading;
    },
    apy() {
      return this.poolsChanged > 1 && this.pool?.apy;
    },
    lpContractBalanceOf() {
      return this.lpContract?.balanceOf;
    },
    lpContractAllowance() {
      return this.lpContract?.allowance;
    },
    tokenFactoryUserPendingReward() {
      if (this.provider && this.tokenFactoryContract) {
        return this.tokenFactoryContract['pendingRewardBalanceOf(address,address)'];
      }
      return null;
    },
  }),
  watch: {
    lpContractBalanceOf: {
      immadiate: true,
      handler() {
        if (this.lpAddress && this.lpContractBalanceOf && !this.lpBalance) {
          this.refreshLPBalance();
          if (this.tokenFactoryAddress) {
            this.refreshTokenFactoryAllowance();
          }
        }
      },
    },
    tokenFactoryUserPendingReward: {
      immadiate: true,
      handler() {
        if (this.lpAddress && this.tokenFactoryUserPendingReward && !this.meedsPendingUserReward) {
          this.refreshPendingReward();
        }
      },
    },
  },
  created() {
    this.init();
  },
  methods: {
    init() {
      if (!this.lpAddress) {
        return;
      }
      if (this.lpContractBalanceOf && !this.lpBalance) {
        this.refreshLPBalance();
        if (this.tokenFactoryAddress) {
          this.refreshTokenFactoryAllowance();
        }
      }
      if (this.tokenFactoryUserPendingReward && !this.meedsPendingUserReward) {
        this.refreshPendingReward();
      }

      // eslint-disable-next-line new-cap
      this.lpContract?.on(this.lpContract.filters.Transfer(), (from, to) => {
        const address = this.address.toUpperCase();
        if (from.toUpperCase() === address || to.toUpperCase() === address) {
          this.refreshLPBalance();
          this.refreshPendingReward();
          this.$store.commit('loadLPTokenAsset', this.pool);
        }
      });

      // eslint-disable-next-line new-cap
      this.lpContract?.on(this.lpContract.filters.Approval(), (from, to) => {
        const address = this.address.toUpperCase();
        if (from.toUpperCase() === address || to.toUpperCase() === address) {
          this.refreshTokenFactoryAllowance();
        }
      });

      this.tokenFactoryContract?.on(
        // eslint-disable-next-line new-cap
        this.tokenFactoryContract.filters.Harvest(this.address, this.lpAddress), 
        () => this.refreshPendingReward()
      );
    },
    refreshTokenFactoryAllowance() {
      this.lpContractAllowance(this.address, this.tokenFactoryAddress)
        .then(balance => this.lpAllowance = balance);
    },
    refreshLPBalance() {
      this.loadingBalance = true;
      this.lpContractBalanceOf(this.address)
        .then(balance => this.lpBalance = balance)
        .finally(() => this.loadingBalance = false);
    },
    refreshPendingReward() {
      this.loadingUserReward = true;
      this.tokenFactoryUserPendingReward(this.lpAddress, this.address)
        .then(balance => this.meedsPendingUserReward = balance)
        .catch(() => this.meedsPendingUserReward = 0)
        .finally(() => this.loadingUserReward = false);
    },
    openStakeDrawer(stake) {
      this.stake = stake;
      if (this.$refs && this.$refs.stakeDrawer) {
        this.$refs.stakeDrawer.open();
      }
    },
    claimReward() {
      this.sendingClaim = true;
      const options = {
        from: this.address,
        gasLimit: this.harvestGasLimit,
      };
      return this.$ethUtils.sendTransaction(
        this.provider,
        this.tokenFactoryContract,
        'harvest',
        options,
        [this.lpAddress]
      ).then(receipt => {
        const transactionHash = receipt?.hash;
        if (transactionHash) {
          this.stakeAmount = 0;
          this.sendingStake = false;
          this.$root.$emit('close-drawer');
          this.step = 1;
        }
        this.sendingClaim = false;
      }).catch(() => this.sendingClaim = false);
    },
  },
};
</script>