<template>
  <v-card
    class="mx-auto"
    width="240"
    height="200"
    flat>
    <v-card-title class="justify-center pb-2">
      {{ $t('buyOrSell') }}
    </v-card-title>
    <div class="elevation-2">
      <v-card-text class="d-flex flex-column">
        <v-text-field
          v-model="fromValue"
          :loading="loadingAmount"
          :rules="fromValueValidator"
          :hide-details="isFromValueValid"
          placeholder="0.0"
          large
          outlined
          dense>
          <template #append>
            <div class="mt-1">
              {{ buy && 'ETH' || 'MEED' }}
            </div>
          </template>
        </v-text-field>
        <v-btn
          icon
          class="mx-auto my-3"
          @click="switchInputs"
          x-large>
          <v-icon size="48">mdi-autorenew</v-icon>
        </v-btn>
        <v-text-field
          v-model="toValueDisplay"
          placeholder="0.0"
          hide-details
          large
          outlined
          dense
          disabled
          filled>
          <template #append>
            <div class="mt-1">
              {{ buy && 'MEED' || 'ETH' }}
            </div>
          </template>
        </v-text-field>
        <v-card-actions>
          <v-btn
            v-if="hasSufficientAllowedTokens"
            :loading="!!sendingTransaction"
            :disabled="disabledButton"
            class="mx-auto mt-4"
            @click="sendSwapTransaction">
            <span class="text-capitalize">
              {{ swapButtonLabel }}
            </span>
          </v-btn>
          <div v-else class="d-flex flex-column mx-auto mt-4">
            <div class="mx-auto">({{ $t('step') }} {{ step }} / 2)</div>
            <v-btn
              :loading="!!sendingTransaction"
              :disabled="disabledButton"
              class="mx-auto mt-4"
              @click="sendApproveTransaction">
              <span class="text-capitalize">
                {{ approveButtonLabel }}
              </span>
            </v-btn>
          </div>
        </v-card-actions>
      </v-card-text>
    </div>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    slippage: 0.05,
    deadlineMinutes: 30,
    computingAmount: false,
    approvalInProgress: false,
    sendingTransaction: 0,
    step: 1,
    buy: true,
    fromValue: null,
    toValue: null,
    startSearchAfterInMilliseconds: 600,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    typing: false,
  }),
  computed: Vuex.mapState({
    address: state => state.address,
    language: state => state.language,
    routerAddress: state => state.routerAddress,
    routerContract: state => state.routerContract,
    meedContract: state => state.meedContract,
    wethContract: state => state.wethContract,
    meedAddress: state => state.meedAddress,
    wethAddress: state => state.wethAddress,
    etherBalance: state => state.etherBalance,
    meedsBalance: state => state.meedsBalance,
    transactionGas: state => state.transactionGas,
    meedsAllowance: state => state.meedsAllowance,
    provider: state => state.provider,
    loadingAmount() {
      return !!this.computingAmount || this.typing;
    },
    fromContract() {
      return this.buy && this.meedContract || this.wethContract;
    },
    tokenAdresses() {
      return this.buy && [this.wethAddress, this.meedAddress] || [this.meedAddress, this.wethAddress];
    },
    swapButtonLabel() {
      return this.buy && this.$t('buyMeeds') || this.$t('sellMeeds');
    },
    approveButtonLabel() {
      return this.sendingTransaction > 0 && this.$t('sellMeeds') || this.$t('approveMeeds');
    },
    disabledButton() {
      return !this.toValue || !this.isFromValueValid || !!this.sendingTransaction;
    },
    toValueDisplay() {
      return this.toValue && this.$ethUtils.toFixed(this.toValue, 8) || null;
    },
    fromValueValidator() {
      return [
        () => !!this.hasSufficientGas || this.$t('insufficientTransactionFee'),
        () => !!this.isFromValueNumeric || this.$t('valueMustBeNumeric'),
        () => !!this.hasSufficientFunds || this.$t('insufficientFunds'),
        () => !!this.isFromValueLessThanMax || this.$t('valueMustBeLessThan', {0: this.maxFromValueLabel}),
      ];
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
    maxFromValueLabel() {
      if (this.buy) {
        return this.$ethUtils.toFixedDisplay(this.maxEther, 8, this.language);
      } else {
        return this.$ethUtils.toFixedDisplay(this.maxMeed, 2, this.language);
      }
    },
    isFromValueValid() {
      return !this.fromValue || (this.isFromValueNumeric && this.isFromValueLessThanMax && this.hasSufficientGas);
    },
    hasSufficientAllowedTokens() {
      if (this.buy || !this.isFromValueNumeric || !this.fromValue) {
        return true;
      } else {
        const meedsAllowance = this.meedsAllowance;
        const meedsToSend = this.$ethUtils.toDecimals(this.fromValue, 18);
        return meedsToSend.lte(meedsAllowance);
      }
    },
    hasSufficientGas() {
      if (!this.fromValue) {
        return true;
      }
      if (this.etherBalance && this.transactionGas) {
        const maxEther = this.$ethUtils.fromDecimals(this.etherBalance.sub(this.transactionGas), 18);
        return Number(maxEther) > 0;
      }
      return false;
    },
    hasSufficientFunds() {
      if (!this.fromValue) {
        return true;
      }
      if (this.buy) {
        return this.maxEther > 0;
      } else {
        return this.maxMeed > 0;
      }
    },
    isFromValueLessThanMax() {
      if (!this.fromValue) {
        return true;
      }
      if (this.buy) {
        return Number(this.fromValue) <= this.maxEther;
      } else {
        return Number(this.fromValue) <= this.maxMeed;
      }
    },
    isFromValueNumeric() {
      if (!this.fromValue) {
        return true;
      }
      return this.fromValue && Number.isFinite(Number(this.fromValue));
    },
    step() {
      return this.sendingTransaction + 1;
    },
    canComputeValue() {
      return this.fromValue && this.isFromValueNumeric;
    },
    swapMethod() {
      if (this.provider && this.routerContract) {
        const routerContractSigner = this.routerContract.connect(this.provider.getSigner());
        return this.buy && routerContractSigner.swapExactETHForTokens || routerContractSigner.swapExactTokensForETH;
      }
      return null;
    },
    approveMethod() {
      if (this.provider && this.routerContract) {
        const meedContractSigner = this.meedContract.connect(this.provider.getSigner());
        return meedContractSigner.approve;
      }
      return null;
    },
  }),
  watch: {
    meedsAllowance() {
      if (this.meedsAllowance && this.approvalInProgress && !this.meedsAllowance.isZero()) {
        this.step = 2;
        this.sendingTransaction = 0;
      }
    },
    fromValue() {
      if (!this.fromValue) {
        this.computeValue();
        return;
      }
      this.startTypingKeywordTimeout = Date.now() + this.startSearchAfterInMilliseconds;
      if (!this.typing) {
        this.typing = true;
        this.waitForEndTyping();
      }
    },
  },
  methods: {
    switchInputs() {
      this.buy = !this.buy;
      this.fromValue = this.toValue;
      this.toValue = null;
      this.computeValue();
    },
    waitForEndTyping() {
      window.setTimeout(() => {
        if (Date.now() > this.startTypingKeywordTimeout) {
          this.computeValue();
        } else {
          this.waitForEndTyping();
        }
      }, this.endTypingKeywordTimeout);
    },
    computeValue() {
      this.typing = false;
      if (!this.canComputeValue) {
        this.toValue = null;
      } else {
        this.computingAmount = true;
        this.toValue = null;
        const amountIn = this.$ethUtils.toDecimals(this.fromValue, 18);
        return this.routerContract.getAmountsOut(amountIn, this.tokenAdresses)
          .then(amounts =>  this.toValue = this.$ethUtils.fromDecimals(amounts[1], 18))
          .finally(() => this.computingAmount = false);
      }
    },
    sendSwapTransaction() {
      this.sendingTransaction++;
      const amountIn = this.$ethUtils.toDecimals(this.fromValue, 18);
      const amountOutMin = this.$ethUtils.toDecimals(this.toValue, 18)
        .mul((1 - this.slippage) * 1000)
        .div(1000);
      const options = {
        gasLimit: this.gasLimit,
      };
      return new Promise((resolve, reject) => {
        if (this.buy) {
          options.value = amountIn.toHexString();
          return this.swapMethod(
            amountOutMin.toHexString(),
            this.tokenAdresses,
            this.address,
            this.getTransactionDeadline(),
            options,
          ).then(resolve).catch(reject);
        } else {
          return this.swapMethod(
            amountIn,
            amountOutMin.toHexString(),
            this.tokenAdresses,
            this.address,
            this.getTransactionDeadline(),
            options,
          ).then(resolve).catch(reject);
        }
      })
        .then(receipt => {
          const transactionHash = receipt.hash;
          this.$root.$emit('transaction-sent', transactionHash);
        })
        .finally(() => this.sendingTransaction--);
    },
    sendApproveTransaction() {
      this.sendingTransaction++;
      const amount = this.$ethUtils.toDecimals(this.fromValue, 18);
      const options = {
        gasLimit: this.gasLimit,
      };
      return this.approveMethod(
        this.routerAddress,
        amount,
        options
      ).then(receipt => {
        const transactionHash = receipt.hash;
        this.$root.$emit('transaction-sent', transactionHash);
        this.approvalInProgress = true;
      })
        .catch(() => {
          this.sendingTransaction--;
          this.step = 1;
        });
    },
    getTransactionDeadline() {
      return Date.now() + this.deadlineMinutes * 60 * 1000;
    },
  },
};
</script>