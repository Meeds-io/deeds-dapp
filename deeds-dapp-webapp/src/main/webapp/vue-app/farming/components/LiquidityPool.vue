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
  <v-hover v-slot="{hover}">
    <v-card
      :elevation="hover ? 3 : 0"
      class="mx-auto ma-2"
      width="400px"
      max-width="100%"
      outlined>
      <v-card-text class="d-flex">
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
            size="72"
            class="mx-auto">
            mdi-cash
          </v-icon>
        </template>
      </v-card-text>
      <v-card-title class="justify-center pt-0 text-center text-break">
        <template v-if="$slots.title">
          <slot name="title"></slot>
        </template>
        <template v-else-if="poolName">
          {{ $t('rentLiquidityOn', {0: poolName}) }}
        </template>
        <template v-else>
          {{ $t('rentLiquidity') }}
        </template>
      </v-card-title>
      <template v-if="$slots.content">
        <slot name="content"></slot>
      </template>
      <template v-else>
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
              <small v-else-if="!rewardsStarted" class="d-flex mt-2">
                <span class="my-auto text-capitalize text-capitalize grey--text">{{ $t('startsAfter') }}</span>
                <deeds-timer
                  :end-time="farmingStartTime"
                  class="my-auto" />
              </small>
              <v-tooltip v-else bottom>
                <template v-slot:activator="{ on, attrs }">
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
              <v-skeleton-loader
                v-if="loadingBalance > 0"
                type="chip"
                max-height="17"
                tile />
              <deeds-number-format v-else :value="lpBalanceOfTokenFactory">
                <deeds-contract-address
                  :address="lpAddress"
                  :label="lpSymbol"
                  :button-top="-2"
                  token />
              </deeds-number-format>
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
                v-if="loadingBalance > 0"
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
                  :button-top="-2"
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
                  :button-top="-2"
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
              <v-skeleton-loader
                v-if="loadingUserReward"
                type="chip"
                max-height="17"
                tile />
              <span v-else-if="noMeedSupplyForLPRemaining">
                0 <deeds-contract-address
                  :address="meedAddress"
                  label="MEED"
                  :button-top="-2"
                  token />
              </span>
              <deeds-number-format
                v-else-if="rewardsStarted"
                :value="meedsPendingUserReward"
                :fractions="2">
                <deeds-contract-address
                  :address="meedAddress"
                  label="MEED"
                  :button-top="-2"
                  token />
              </deeds-number-format>
              <v-tooltip v-else bottom>
                <template v-slot:activator="{ on, attrs }">
                  <div
                    v-bind="attrs"
                    v-on="on">
                    <deeds-number-format :value="meedsPendingUserReward" :fractions="2">
                      <deeds-contract-address
                        :address="meedAddress"
                        label="MEED"
                        :button-top="-2"
                        token />
                    </deeds-number-format>
                  </div>
                </template>
                <div>
                  {{ $t('meedsRewardingDidntStarted') }}
                </div>
              </v-tooltip>
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              name="claimRewardButton"
              :loading="sendingClaim"
              :disabled="sendingClaim || !rewardsStarted"
              outlined
              text
              @click="claimReward()">
              <span class="text-capitalize">{{ $t('claim') }}</span>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
        <deeds-stake-liquidity-drawer
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
    loading: false,
    loadingBalance: 0,
    loadingUserInfo: false,
    loadingUserReward: false,
    sendingClaim: false,
    meedsBalanceOfPool: null,
    meedsPendingUserReward: null,
    lpBalanceOfTokenFactory: null,
    lpTotalSupply: null,
    stake: true,
    lpSymbol: null,
    lpBalance: null,
    userInfo: null,
    lpAllowance: null,
    yearInMinutes: 365 * 24 * 60,
  }),
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    provider: state => state.provider,
    address: state => state.address,
    meedAddress: state => state.meedAddress,
    noMeedSupplyForLPRemaining: state => state.noMeedSupplyForLPRemaining,
    harvestGasLimit: state => state.harvestGasLimit,
    tokenFactoryAddress: state => state.tokenFactoryAddress,
    sushiswapPairAddress: state => state.sushiswapPairAddress,
    univ2PairAddress: state => state.univ2PairAddress,
    erc20ABI: state => state.erc20ABI,
    meedContract: state => state.meedContract,
    farmingStartTime: state => state.farmingStartTime,
    rewardedTotalFixedPercentage: state => state.rewardedTotalFixedPercentage,
    rewardedTotalAllocationPoints: state => state.rewardedTotalAllocationPoints,
    rewardedMeedPerMinute: state => state.rewardedMeedPerMinute,
    tokenFactoryContract: state => state.tokenFactoryContract,
    now: state => state.now,
    lpAddress() {
      return this.pool && this.pool.address;
    },
    isSushiswapPool() {
      return this.sushiswapPairAddress && this.pool && this.pool.address && this.pool.address.toUpperCase() === this.sushiswapPairAddress.toUpperCase();
    },
    isUniswapPool() {
      return this.univ2PairAddress && this.pool && this.pool.address && this.pool.address.toUpperCase() === this.univ2PairAddress.toUpperCase();
    },
    poolName() {
      if (this.isSushiswapPool) {
        return 'Sushiswap';
      } else if (this.isUniswapPool) {
        return 'Uniswap';
      }
      return this.lpSymbol;
    },
    lpContract() {
      if (this.provider && this.lpAddress) {
        return new ethers.Contract(
          this.lpAddress,
          this.erc20ABI,
          this.provider
        );
      }
      return null;
    },
    lpContractBalanceOf() {
      return this.lpContract && this.lpContract.balanceOf;
    },
    lpContractSymbol() {
      return this.lpContract && this.lpContract.symbol;
    },
    lpContractTotalSupply() {
      return this.lpContract && this.lpContract.totalSupply;
    },
    lpContractAllowance() {
      return this.lpContract && this.lpContract.allowance;
    },
    meedContractBalanceOf() {
      if (this.provider && this.meedContract) {
        return this.meedContract.balanceOf;
      }
      return null;
    },
    tokenFactoryUserLpInfos() {
      if (this.provider && this.tokenFactoryContract) {
        return this.tokenFactoryContract.userLpInfos;
      }
      return null;
    },
    tokenFactoryUserPendingReward() {
      if (this.provider && this.tokenFactoryContract) {
        return this.tokenFactoryContract['pendingRewardBalanceOf(address,address)'];
      }
      return null;
    },
    lpStaked() {
      return this.userInfo && this.userInfo.amount || 0;
    },
    rewardsStarted() {
      return this.farmingStartTime < this.now;
    },
    yearlyRewardedMeeds() {
      if (this.pool && this.lpAddress) {
        if (this.pool.fixedPercentage && !this.pool.fixedPercentage.isZero()) {
          return new BigNumber(this.rewardedMeedPerMinute.toString())
            .multipliedBy(this.yearInMinutes)
            .multipliedBy(this.pool.fixedPercentage.toString())
            .dividedBy(100);
        } else if (this.pool.allocationPoint && !this.pool.allocationPoint.isZero()) {
          return new BigNumber(this.rewardedMeedPerMinute.toString())
            .multipliedBy(this.yearInMinutes)
            .multipliedBy(this.pool.allocationPoint.toString())
            .dividedBy(this.rewardedTotalAllocationPoints.toString())
            .dividedBy(100)
            .multipliedBy(100 - this.rewardedTotalFixedPercentage.toNumber());
        }
      }
      return new BigNumber(0);
    },
    stakedEquivalentMeedsBalanceOfPool() {
      return this.meedsBalanceOfPool
        && this.lpBalanceOfTokenFactory
        && this.lpTotalSupply
        && !this.lpTotalSupply.isZero()
        && this.meedsBalanceOfPool.mul(this.lpBalanceOfTokenFactory).mul(2).div(this.lpTotalSupply);
    },
    apy() {
      if (!this.stakedEquivalentMeedsBalanceOfPool
          || !this.yearlyRewardedMeeds
          || !this.rewardsStarted
          || this.stakedEquivalentMeedsBalanceOfPool.isZero()
          || this.noMeedSupplyForLPRemaining
          || this.yearlyRewardedMeeds.isZero()
          || !this.rewardsStarted) {
        return 0;
      }
      return this.yearlyRewardedMeeds.dividedBy(this.stakedEquivalentMeedsBalanceOfPool.toString()).multipliedBy(100);
    },
  }),
  watch: {
    meedContractBalanceOf: {
      immadiate: true,
      handler() {
        if (this.lpAddress && this.meedContractBalanceOf && !this.meedsBalanceOfPool) {
          this.refreshMeedsBalanceOfPool();
        }
      },
    },
    lpContractBalanceOf: {
      immadiate: true,
      handler() {
        if (this.lpAddress && this.lpContractBalanceOf && !this.lpBalance) {
          this.refreshLPBalance();
          if (this.tokenFactoryAddress) {
            this.refreshTokenFactoryBalance();
            this.refreshTokenFactoryAllowance();
          }
        }
      },
    },
    lpContractSymbol: {
      immadiate: true,
      handler() {
        if (this.lpAddress && this.lpContractBalanceOf && !this.lpBalance) {
          this.refreshLPSymbol();
        }
      },
    },
    lpContractTotalSupply: {
      immadiate: true,
      handler() {
        if (this.lpAddress && this.lpContractTotalSupply && !this.lpTotalSupply) {
          this.refreshLPTotalSupply();
        }
      },
    },
    tokenFactoryUserLpInfos: {
      immadiate: true,
      handler() {
        if (this.lpAddress && this.tokenFactoryUserLpInfos && !this.lpStaked) {
          this.refreshUserInfo();
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
    if (!this.lpAddress) {
      return;
    }
    if (this.meedContractBalanceOf && !this.meedsBalanceOfPool) {
      this.refreshMeedsBalanceOfPool();
    }
    if (this.lpContractBalanceOf && !this.lpBalance) {
      this.refreshLPBalance();
      if (this.tokenFactoryAddress) {
        this.refreshTokenFactoryBalance();
        this.refreshTokenFactoryAllowance();
      }
    }
    if (this.lpContractSymbol && !this.lpBalance) {
      this.refreshLPSymbol();
    }
    if (this.lpContractTotalSupply && !this.lpTotalSupply) {
      this.refreshLPTotalSupply();
    }
    if (this.tokenFactoryUserLpInfos && !this.lpStaked) {
      this.refreshUserInfo();
    }
    if (this.tokenFactoryUserPendingReward && !this.meedsPendingUserReward) {
      this.refreshPendingReward();
    }

    // eslint-disable-next-line new-cap
    this.lpContract.on(this.lpContract.filters.Transfer(), (from, to) => {
      const address = this.address.toUpperCase();
      if (from.toUpperCase() === address || to.toUpperCase() === address) {
        this.refreshLPBalance();
        this.refreshTokenFactoryBalance();
        this.refreshUserInfo();
        this.refreshPendingReward();
      }
    });

    // eslint-disable-next-line new-cap
    this.lpContract.on(this.lpContract.filters.Approval(), (from, to) => {
      const address = this.address.toUpperCase();
      if (from.toUpperCase() === address || to.toUpperCase() === address) {
        this.refreshTokenFactoryAllowance();
      }
    });

    this.tokenFactoryContract.on(
      // eslint-disable-next-line new-cap
      this.tokenFactoryContract.filters.Harvest(this.address, this.lpAddress), 
      () => this.refreshPendingReward()
    );
  },
  methods: {
    refreshMeedsBalanceOfPool() {
      this.loading = true;
      this.meedContractBalanceOf(this.lpAddress)
        .then(balance => this.meedsBalanceOfPool = balance)
        .finally(() => this.loading = false);
    },
    refreshLPSymbol() {
      this.loadingBalance++;
      this.lpContractSymbol()
        .then(symbol => this.lpSymbol = symbol)
        .finally(() => this.loadingBalance--);
    },
    refreshLPTotalSupply() {
      this.loadingBalance++;
      this.lpContractTotalSupply()
        .then(totalSupply => this.lpTotalSupply = totalSupply)
        .finally(() => this.loadingBalance--);
    },
    refreshTokenFactoryBalance() {
      this.loadingBalance++;
      this.lpContractBalanceOf(this.tokenFactoryAddress)
        .then(balance => this.lpBalanceOfTokenFactory = balance)
        .finally(() => this.loadingBalance--);
    },
    refreshTokenFactoryAllowance() {
      this.loadingBalance++;
      this.lpContractAllowance(this.address, this.tokenFactoryAddress)
        .then(balance => this.lpAllowance = balance)
        .finally(() => this.loadingBalance--);
    },
    refreshLPBalance() {
      this.loadingBalance++;
      this.lpContractBalanceOf(this.address)
        .then(balance => this.lpBalance = balance)
        .finally(() => this.loadingBalance--);
    },
    refreshPendingReward() {
      this.loadingUserReward = true;
      this.tokenFactoryUserPendingReward(this.lpAddress, this.address)
        .then(balance => this.meedsPendingUserReward = balance)
        .catch(() => this.meedsPendingUserReward = 0)
        .finally(() => this.loadingUserReward = false);
    },
    refreshUserInfo() {
      this.loadingUserInfo = true;
      this.tokenFactoryUserLpInfos(this.lpAddress, this.address)
        .then(userInfo => {
          const user = {};
          Object.keys(userInfo).forEach(key => {
            if (Number.isNaN(Number(key))) {
              user[key] = userInfo[key];
            }
          });
          this.userInfo = user;
          return user;
        })
        .finally(() => this.loadingUserInfo = false);
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
        gasLimit: this.harvestGasLimit,
      };
      return this.$ethUtils.sendTransaction(
        this.provider,
        this.tokenFactoryContract,
        'harvest',
        options,
        [this.lpAddress]
      ).then(receipt => {
        const transactionHash = receipt.hash;
        this.$root.$emit('transaction-sent', transactionHash);
        this.stakeAmount = 0;
        this.sendingStake = false;
        this.$root.$emit('close-drawer');
        this.step = 1;
      })
        .finally(() => this.sendingClaim = false);
    },
  },
};
</script>