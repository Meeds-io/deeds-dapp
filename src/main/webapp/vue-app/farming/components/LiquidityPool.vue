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
      class="mx-auto my-2"
      width="400px"
      max-width="100%"
      outlined>
      <v-card-text class="d-flex">
        <v-icon size="72" class="mx-auto">mdi-cash</v-icon>
      </v-card-text>
      <v-card-title class="justify-center pt-0 text-center">
        <template v-if="poolName">
          {{ $t('rentLiquidityOn', {0: poolName}) }}
        </template>
        <template v-else>
          {{ $t('rentLiquidity') }}
        </template>
      </v-card-title>
      <v-list-item>
        <v-list-item-content class="pb-0">
          <v-list-item-title>
            {{ $t('apy') }}
          </v-list-item-title>
          <v-list-item-subtitle class="font-weight-bold ms-2">
            <v-skeleton-loader
              v-if="loading"
              type="chip"
              max-height="17"
              tile />
            <v-tooltip v-else bottom>
              <template v-slot:activator="{ on, attrs }">
                <div
                  v-bind="attrs"
                  v-on="on">
                  <deeds-number-format
                    :value="apy"
                    no-decimals>
                    %
                  </deeds-number-format>
                </div>
              </template>
              <ul v-if="rewardsStarted">
                <li>
                  <deeds-number-format :value="yearlyRewardedMeeds" label="yearlyRewardedMeeds" />
                </li>
                <li>
                  <deeds-number-format :value="stakedEquivalentMeedsBalanceOfPool" label="meedsBalanceOfPool" />
                </li>
              </ul>
              <div v-else>
                {{ $t('meedsRewardingDidntStarted') }}
              </div>
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
            <deeds-number-format v-else :value="lpBalanceOfMasterChef">
              {{ lpSymbol }}
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
            <deeds-number-format v-else :value="lpBalance">
              {{ lpSymbol }}
            </deeds-number-format>
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
              v-if="loadingUserInfo"
              type="chip"
              max-height="17"
              tile />
            <deeds-number-format v-else :value="lpStaked">
              {{ lpSymbol }}
            </deeds-number-format>
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
      <deeds-stake-liquidity-drawer
        ref="stakeDrawer"
        :lp-address="lpAddress"
        :lp-symbol="lpSymbol"
        :lp-balance="lpBalance"
        :lp-staked="lpStaked"
        :lp-allowance="lpAllowance"
        :lp-contract="lpContract"
        :stake="stake"
        :claim="claim" />
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
    meedsBalanceOfPool: null,
    lpBalanceOfMasterChef: null,
    lpTotalSupply: null,
    stake: true,
    claim: true,
    lpSymbol: null,
    lpBalance: null,
    lpAllowance: null,
    userInfo: null,
    yearInMinutes: 365 * 24 * 60,
  }),
  computed: Vuex.mapState({
    provider: state => state.provider,
    address: state => state.address,
    masterChefAddress: state => state.masterChefAddress,
    sushiswapPairAddress: state => state.sushiswapPairAddress,
    univ2PairAddress: state => state.univ2PairAddress,
    erc20ABI: state => state.erc20ABI,
    meedContract: state => state.meedContract,
    masterChefContract: state => state.masterChefContract,
    meedsStartRewardsTime: state => state.meedsStartRewardsTime,
    rewardedTotalFixedPercentage: state => state.rewardedTotalFixedPercentage,
    rewardedTotalAllocationPoints: state => state.rewardedTotalAllocationPoints,
    rewardedMeedPerMinute: state => state.rewardedMeedPerMinute,
    lpAddress() {
      return this.pool && this.pool.address;
    },
    poolName() {
      if (this.sushiswapPairAddress && this.pool && this.pool.address && this.pool.address.toUpperCase() === this.sushiswapPairAddress.toUpperCase()) {
        return 'Sushiswap';
      } else if (this.univ2PairAddress && this.pool && this.pool.address && this.pool.address.toUpperCase() === this.univ2PairAddress.toUpperCase()) {
        return 'Uniswap';
      }
      return this.lpSymbol;
    },
    lpContract() {
      if (this.provider) {
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
    masterChefUserLpInfos() {
      if (this.provider && this.masterChefContract) {
        return this.masterChefContract.userLpInfos;
      }
      return null;
    },
    lpStaked() {
      return this.userInfo && this.userInfo.amount || 0;
    },
    rewardsStarted() {
      return this.meedsStartRewardsTime < Date.now();
    },
    yearlyRewardedMeeds() {
      if (this.pool) {
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
            .multipliedBy(100)
            .dividedBy(100 - this.rewardedTotalFixedPercentage.toNumber());
        }
      }
      return new BigNumber(0);
    },
    stakedEquivalentMeedsBalanceOfPool() {
      return this.meedsBalanceOfPool
        && this.lpBalanceOfMasterChef
        && this.lpTotalSupply
        && !this.lpTotalSupply.isZero()
        && this.meedsBalanceOfPool.mul(this.lpBalanceOfMasterChef).div(this.lpTotalSupply);
    },
    apy() {
      if (!this.stakedEquivalentMeedsBalanceOfPool
          || !this.yearlyRewardedMeeds
          || this.stakedEquivalentMeedsBalanceOfPool.isZero()
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
        if (this.meedContractBalanceOf && !this.meedsBalanceOfPool) {
          this.refreshMeedsBalanceOfPool();
        }
      },
    },
    lpContractBalanceOf: {
      immadiate: true,
      handler() {
        if (this.lpContractBalanceOf && !this.lpBalance) {
          this.refreshLPBalance();
          if (this.masterChefAddress) {
            this.refreshMasterChefBalance();
            this.refreshMasterChefAllowance();
          }
        }
      },
    },
    lpContractSymbol: {
      immadiate: true,
      handler() {
        if (this.lpContractBalanceOf && !this.lpBalance) {
          this.refreshLPSymbol();
        }
      },
    },
    lpContractTotalSupply: {
      immadiate: true,
      handler() {
        if (this.lpContractTotalSupply && !this.lpTotalSupply) {
          this.refreshLPTotalSupply();
        }
      },
    },
    masterChefUserLpInfos: {
      immadiate: true,
      handler() {
        if (this.masterChefUserLpInfos && !this.userInfo) {
          this.refreshUserInfo();
        }
      },
    },
  },
  created() {
    if (this.meedContractBalanceOf && !this.meedsBalanceOfPool) {
      this.refreshMeedsBalanceOfPool();
    }
    if (this.lpContractBalanceOf && !this.lpBalance) {
      this.refreshLPBalance();
      if (this.masterChefAddress) {
        this.refreshMasterChefBalance();
        this.refreshMasterChefAllowance();
      }
    }
    if (this.lpContractSymbol && !this.lpBalance) {
      this.refreshLPSymbol();
    }
    if (this.lpContractTotalSupply && !this.lpTotalSupply) {
      this.refreshLPTotalSupply();
    }
    if (this.masterChefUserLpInfos && !this.userInfo) {
      this.refreshUserInfo();
    }

    // eslint-disable-next-line new-cap
    this.lpContract.on(this.lpContract.filters.Transfer(), (from, to) => {
      const address = this.address.toUpperCase();
      if (from.toUpperCase() === address || to.toUpperCase() === address) {
        this.refreshLPBalance();
        this.refreshMasterChefBalance();
        this.refreshUserInfo();
      }
    });

    // eslint-disable-next-line new-cap
    this.lpContract.on(this.lpContract.filters.Approval(), (from, to) => {
      const address = this.address.toUpperCase();
      if (from.toUpperCase() === address || to.toUpperCase() === address) {
        this.refreshMasterChefAllowance();
      }
    });
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
    refreshMasterChefBalance() {
      this.loadingBalance++;
      this.lpContractBalanceOf(this.masterChefAddress)
        .then(balance => this.lpBalanceOfMasterChef = balance)
        .finally(() => this.loadingBalance--);
    },
    refreshMasterChefAllowance() {
      this.loadingBalance++;
      this.lpContractAllowance(this.address, this.masterChefAddress)
        .then(balance => this.lpAllowance = balance)
        .finally(() => this.loadingBalance--);
    },
    refreshLPBalance() {
      this.loadingBalance++;
      this.lpContractBalanceOf(this.address)
        .then(balance => this.lpBalance = balance)
        .finally(() => this.loadingBalance--);
    },
    refreshUserInfo() {
      this.loadingUserInfo = true;
      this.masterChefUserLpInfos(this.lpAddress, this.address)
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
    openStakeDrawer(stake, claim) {
      this.stake = stake;
      this.claim = claim;
      if (this.$refs && this.$refs.stakeDrawer) {
        this.$refs.stakeDrawer.open();
      }
    },
  },
};
</script>