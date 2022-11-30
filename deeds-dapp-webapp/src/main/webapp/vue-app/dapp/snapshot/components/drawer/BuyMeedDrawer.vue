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
  <deeds-drawer
    ref="drawer"
    v-model="drawer"
    :permanent="sending"
    @opened="$emit('opened')"
    @closed="$emit('closed')">
    <template #title>
      <h4 class="text-capitalize">{{ $t('buyMeeds') }}</h4>
    </template>
    <template #content>
      <v-card color="transparent" flat>
        <v-card-text>
          <div class="d-flex flex-column mb-8">
            {{ $t('buyMeedsIntroduction') }}
            <ul class="mt-4">
              <li class="ps-0 ps-sm-4">
                {{ $t('buyMeedsIntroductionBulletPoint1') }}
              </li>
              <li class="ps-0 ps-sm-4">
                {{ $t('buyMeedsIntroductionBulletPoint2') }}
              </li>
              <li class="ps-0 ps-sm-4">
                {{ $t('buyMeedsIntroductionBulletPoint3') }}
              </li>
            </ul>
          </div>
          <deeds-trade-meeds
            :sending="sending"
            :completed-steps="!swapInToSteps"
            :max-meed="maxMeed"
            :max-ether="maxEther"
            @computing="computingAmountIn = $event"
            @changed-amount-out="amountOut = Number($event)"
            @changed-amount-in="amountIn = Number($event)"
            @changed-buy="buy = $event" />
        </v-card-text>
      </v-card>
    </template>
    <template #footer>
      <div class="d-flex flex-row align-center">
        <deeds-metamask-button v-if="hasInvalidAddress" />
        <template v-else>
          <div
            v-if="swapInToSteps || !hasSufficientAllowedTokens"
            class="me-4">
            {{ $t('step') }} {{ step }}/2
          </div>
          <v-btn
            v-if="displayApproveButton"
            :loading="sending"
            :disabled="disabled"
            :class="disabled && 'primary'"
            :min-width="minWidthButtons"
            name="sendApproveTransactionButton"
            color="primary"
            depressed
            dark
            @click="sendApproveTransaction">
            {{ $t('approveMeeds') }}
          </v-btn>
          <v-btn
            v-else
            :loading="sending"
            :disabled="disabled"
            :class="disabled && 'primary'"
            :min-width="minWidthButtons"
            name="sendSwapTransactionButton"
            color="primary"
            depressed
            dark
            @click="sendSwapTransaction">
            {{ swapButtonLabel }}
          </v-btn>
        </template>
      </div>
    </template>
  </deeds-drawer>
</template>
<script>
export default {
  data: () => ({
    slippage: 0.05,
    deadlineMinutes: 30,
    amountOut: 0,
    amountIn: 0,
    computingAmountIn: false,
    buy: true,
    swapInToSteps: false,
    sending: false,
    step: 1,
    minWidthButtons: 150,
  }),
  computed: Vuex.mapState({
    address: state => state.address,
    appLoading: state => state.appLoading,
    language: state => state.language,
    tradeGasLimit: state => state.tradeGasLimit,
    approvalGasLimit: state => state.approvalGasLimit,
    sushiswapRouterAddress: state => state.sushiswapRouterAddress,
    sushiswapRouterContract: state => state.sushiswapRouterContract,
    meedContract: state => state.meedContract,
    wethContract: state => state.wethContract,
    meedAddress: state => state.meedAddress,
    wethAddress: state => state.wethAddress,
    etherBalance: state => state.etherBalance,
    meedsBalance: state => state.meedsBalance,
    transactionGas: state => state.transactionGas,
    meedsRouteAllowance: state => state.meedsRouteAllowance,
    provider: state => state.provider,
    fromContract() {
      return this.buy && this.meedContract || this.wethContract;
    },
    tokenAdresses() {
      return this.buy && [this.wethAddress, this.meedAddress] || [this.meedAddress, this.wethAddress];
    },
    hasInvalidAddress() {
      return this.appLoading || !this.address;
    },
    isAmountOutNumeric() {
      if (!this.amountOut) {
        return true;
      }
      return this.amountOut && Number.isFinite(Number(this.amountOut));
    },
    hasSufficientAllowedTokens() {
      if (this.buy) {
        return true;
      } else {
        const meedsRouteAllowance = this.meedsRouteAllowance;
        const meedsToSend = this.$ethUtils.toDecimals(this.amountOut, 18);
        return meedsToSend.lte(meedsRouteAllowance);
      }
    },
    hasSufficientGas() {
      if (!this.amountOut) {
        return true;
      }
      if (this.etherBalance && this.transactionGas) {
        const maxEther = this.$ethUtils.fromDecimals(this.etherBalance.sub(this.transactionGas), 18);
        return Number(maxEther) > 0;
      }
      return false;
    },
    maxMeed() {
      if (this.meedsBalance) {
        const maxMeed = this.$ethUtils.fromDecimals(this.meedsBalance, 18);
        return Number(maxMeed);
      }
      return 0;
    },
    maxEther() {
      if (this.etherBalance && this.transactionGas) {
        const maxEther = this.$ethUtils.fromDecimals(this.etherBalance.sub(this.transactionGas), 18);
        return Number(maxEther) > 0 && Number(maxEther) || 0;
      }
      return 0;
    },
    isAmountOutLessThanMax() {
      if (!this.amountOut) {
        return true;
      }
      if (this.buy) {
        return Number(this.amountOut) <= this.maxEther;
      } else {
        return Number(this.amountOut) <= this.maxMeed;
      }
    },
    isAmountOutValid() {
      return !this.amountOut || (this.isAmountOutNumeric && this.isAmountOutLessThanMax && this.hasSufficientGas);
    },
    disabled() {
      return !this.isAmountOutValid || this.sending || this.computingAmountIn || !this.amountIn;
    },
    displayApproveButton() {
      return !this.buy && !this.hasSufficientAllowedTokens && !this.swapInToSteps && this.step === 1;
    },
    swapButtonLabel() {
      return this.buy && this.$t('buyMeeds') || this.$t('sellMeeds');
    },
  }),
  watch: {
    buy() {
      if (this.buy) {
        this.step = 1;
        this.swapInToSteps = false;
      } else {
        this.resetStep();
      }
    },
    etherBalance() {
      this.resetStep();
    },
    meedsBalance() {
      this.resetStep();
      this.swapInToSteps = false;
    },
    meedsRouteAllowance() {
      this.sending = false;
      if (this.swapInToSteps && this.meedsRouteAllowance && !this.meedsRouteAllowance.isZero()) {
        this.step = 2;
      }
    },
  },
  created() {
    this.$root.$on('open-buy-meed-drawer', this.open);
  },
  beforeDestroy() {
    this.$root.$off('open-buy-meed-drawer', this.open);
  },
  methods: {
    resetStep() {
      this.sending = false;
      if (!this.swapInToSteps) {
        this.step = 1;
      }
    },
    open() {
      if (!this.sending && !this.swapInToSteps) {
        this.step = 1;
      }
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    sendSwapTransaction() {
      this.sending = true;
      const amountIn = this.$ethUtils.toDecimals(this.amountOut, 18);
      const amountOutMin = this.$ethUtils.toDecimals(this.amountIn, 18)
        .mul((1 - this.slippage) * 1000)
        .div(1000);
      return this.$ethUtils.sendTransaction(
        this.provider,
        this.sushiswapRouterContract,
        this.buy && 'swapExactETHForTokens' || 'swapExactTokensForETH',
        {
          from: this.address,
          gasLimit: this.tradeGasLimit,
        },
        this.buy && [
          amountOutMin.toHexString(),
          this.tokenAdresses,
          this.address,
          this.getTransactionDeadline()
        ] || [
          amountIn,
          amountOutMin.toHexString(),
          this.tokenAdresses,
          this.address,
          this.getTransactionDeadline()
        ]
      )
        .catch(() => this.sending = false);
    },
    sendApproveTransaction() {
      this.sending = true;
      const amount = this.$ethUtils.toDecimals(this.amountOut, 18);
      return this.$ethUtils.sendTransaction(
        this.provider,
        this.meedContract,
        'approve',
        {
          from: this.address,
          gasLimit: this.approvalGasLimit,
        },
        [this.sushiswapRouterAddress, amount]
      )
        .then(() => this.swapInToSteps = true)
        .catch(() => this.sending = false);
    },
    getTransactionDeadline() {
      return parseInt(Date.now() / 1000 + this.deadlineMinutes * 60);
    },
  },
};
</script>