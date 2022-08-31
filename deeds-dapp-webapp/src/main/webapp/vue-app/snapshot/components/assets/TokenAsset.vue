<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2022 Meeds Association contact@meeds.io
 
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
  <v-list-item v-if="existBalance" class="ps-8">
    <v-list-item-content class="align-start">
      <div>
        <deeds-contract-address
          :address="lpAddress"
          :label="poolName"
          token />
      </div>
    </v-list-item-content>
    <v-list-item-content class="align-end">
      <v-skeleton-loader
        v-if="loadingBalance"
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
              :value="lpBalance"
              :fractions="2" />
            <span class="mx-1"> {{ lpSymbol }}  </span>
          </div>
        </template>
        <span v-if="noMeedSupplyForLPRemaining">
          {{ $t('maxMeedsSupplyReached') }}
        </span>
        <div v-if="rewardsStarted" class="d-flex flex-row">
          <span class="mx-1"> {{ $t('apy') }} </span>
          <deeds-number-format
            :value="apy"
            no-decimals>
            %
          </deeds-number-format>
        </div>
        <div v-else>
          {{ $t('meedsRewardingDidntStarted') }}
        </div>
      </v-tooltip>
    </v-list-item-content>
    <v-list-item-content class="align-end">
      <v-skeleton-loader
        v-if="loadingXMeedsBalance"
        type="chip"
        max-height="17"
        tile /> 
      <div 
        v-else
        class="d-flex">
        <span class="mx-1">+</span>
        <deeds-number-format 
          :value="weeklyRewardedMeeds" 
          :fractions="2" />
        <span class="mx-1">MEED / {{ $t('week') }}</span>
      </div>
    </v-list-item-content>
    <v-list-item-content class="align-end">
      <v-skeleton-loader
        v-if="loadingBalance"
        type="chip"
        max-height="17"
        tile />
      <deeds-number-format
        v-else
        :value="lpBalance"
        :fractions="2"
        currency />
    </v-list-item-content>
  </v-list-item>
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
    userInfo: null,
    loadingBalance: true,
    loadingUserInfo: false,
    lpSymbol: null,
    yearInMinutes: 365 * 24 * 60,
    yearInWeeks: 7 / 365,
    meedsBalanceOfPool: null,
    lpBalanceOfTokenFactory: null,
    lpTotalSupply: null,
    lpBalance: null
  }),
  computed: Vuex.mapState({
    sushiswapPairAddress: state => state.sushiswapPairAddress,
    univ2PairAddress: state => state.univ2PairAddress,
    address: state => state.address,
    provider: state => state.provider,
    erc20ABI: state => state.erc20ABI,
    tokenFactoryContract: state => state.tokenFactoryContract,
    farmingStartTime: state => state.farmingStartTime,
    now: state => state.now,
    rewardedMeedPerMinute: state => state.rewardedMeedPerMinute,
    rewardedTotalAllocationPoints: state => state.rewardedTotalAllocationPoints,
    rewardedTotalFixedPercentage: state => state.rewardedTotalFixedPercentage,
    meedContract: state => state.meedContract,
    tokenFactoryAddress: state => state.tokenFactoryAddress,
    noMeedSupplyForLPRemaining: state => state.noMeedSupplyForLPRemaining,
    univ2PairContract: state => state.univ2PairContract,
    sushiswapPairContract: state => state.sushiswapPairContract,
    lpAddress() {
      return this.pool && this.pool.address;
    },
    lpStaked() {
      return this.userInfo && this.userInfo.amount || 0;
    },
    lpContract() {
      if (this.lpAddress && this.lpAddress.toLowerCase() === this.sushiswapPairAddress.toLowerCase()) {
        return this.sushiswapPairContract;
      } else if (this.lpAddress && this.lpAddress.toLowerCase() === this.univ2PairAddress.toLowerCase()) {
        return this.univ2PairContract;
      }
      return null;
    },
    lpContractBalanceOf() {
      return this.lpContract && this.lpContract.balanceOf;
    },
    lpContractTotalSupply() {
      return this.lpContract && this.lpContract.totalSupply;
    },
    isSushiswapPool() {
      return this.sushiswapPairAddress && this.pool && this.pool.address && this.pool.address.toUpperCase() === this.sushiswapPairAddress.toUpperCase();
    },
    isUniswapPool() {
      return this.univ2PairAddress && this.pool && this.pool.address && this.pool.address.toUpperCase() === this.univ2PairAddress.toUpperCase();
    },
    tokenFactoryUserLpInfos() {
      if (this.provider && this.tokenFactoryContract) {
        return this.tokenFactoryContract.userLpInfos;
      }
      return null;
    },
    poolName() {
      if (this.isSushiswapPool) {
        return 'Sushiswap';
      } else if (this.isUniswapPool) {
        return 'Uniswap';
      }
      return this.lpSymbol;
    },
    meedContractBalanceOf() {
      if (this.provider && this.meedContract) {
        return this.meedContract.balanceOf;
      }
      return null;
    },
    lpContractSymbol() {
      return this.lpContract && this.lpContract.symbol;
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
    weeklyRewardedMeeds() {
      if (this.lpStaked && this.apy) {
        return new BigNumber(this.lpStaked.toString())
          .multipliedBy(this.apy.toString())
          .dividedBy(100)
          .multipliedBy(this.yearInWeeks);
      }
    },
    existBalance() {
      return this.lpBalance > 0;
    }
  }),
  watch: {
    tokenFactoryUserLpInfos: {
      immadiate: true,
      handler() {
        if (this.lpAddress && this.tokenFactoryUserLpInfos) {
          this.refreshUserInfo();
        }
      }
    },
    lpContractSymbol: {
      immadiate: true,
      handler() {
        if (this.lpAddress && this.lpContractSymbol) {
          this.refreshLPSymbol();
        }
      },
    },
    meedContractBalanceOf: {
      immadiate: true,
      handler() {
        if (this.lpAddress && this.meedContractBalanceOf && !this.meedsBalanceOfPool) {
          this.refreshMeedsBalanceOfPool();
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
    lpContractBalanceOf: {
      immadiate: true,
      handler() {
        if (this.lpAddress && this.lpContractBalanceOf && !this.lpBalance) {
          this.refreshLPBalance();
        }
      },
    },
  },
  methods: {
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
    refreshLPSymbol() {
      this.loadingBalance = true;
      this.lpContractSymbol()
        .then(symbol => this.lpSymbol = symbol)
        .finally(() => this.loadingBalance = false);
    },
    refreshMeedsBalanceOfPool() {
      this.loadingBalance = true;
      this.meedContractBalanceOf(this.lpAddress)
        .then(balance => this.meedsBalanceOfPool = balance)
        .finally(() => this.loadingBalance = false);
    },
    refreshTokenFactoryBalance() {
      this.loadingBalance = true;
      this.lpContractBalanceOf(this.tokenFactoryAddress)
        .then(balance => this.lpBalanceOfTokenFactory = balance)
        .finally(() => this.loadingBalance = false);
    },
    refreshLPTotalSupply() {
      this.loadingBalance = true;
      this.lpContractTotalSupply()
        .then(totalSupply => this.lpTotalSupply = totalSupply)
        .finally(() => this.loadingBalance = false);
    },
    refreshLPBalance() {
      this.loadingBalance = true;
      this.lpContractBalanceOf(this.address)
        .then(balance => this.lpBalance = balance)
        .finally(() => this.loadingBalance = false);
    },
  },
  created() {
    if (this.tokenFactoryUserLpInfos) {
      this.refreshUserInfo();
    }
    if (this.lpContractSymbol) {
      this.refreshLPSymbol();
    }
    if (this.meedContractBalanceOf && !this.meedsBalanceOfPool) {
      this.refreshMeedsBalanceOfPool();
    }
    if (this.tokenFactoryAddress) {
      this.refreshTokenFactoryBalance();
    }
    if (this.lpContractTotalSupply && !this.lpTotalSupply) {
      this.refreshLPTotalSupply();
    }
    if (this.lpContractBalanceOf && !this.lpBalance) {
      this.refreshLPBalance();
    }    
  },
};
</script>