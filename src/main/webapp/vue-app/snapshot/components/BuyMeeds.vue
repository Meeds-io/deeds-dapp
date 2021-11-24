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
          placeholder="0.0"
          hide-details
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
          v-model="toValue"
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
            :disabled="disabledButton"
            class="mx-auto mt-4"
            @click="sendTransaction">
            <span class="text-capitalize">
              {{ buttonLabel }}
            </span>
          </v-btn>
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
    sendingTransaction: 0,
    buy: true,
    fromValue: null,
    toValue: null,
    startSearchAfterInMilliseconds: 600,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    typing: false,
  }),
  computed: Vuex.mapState({
    gasLimit: state => state.gasLimit,
    gasPrice: state => state.gasPrice,
    address: state => state.address,
    routerAddress: state => state.routerAddress,
    routerContract: state => state.routerContract,
    meedContract: state => state.meedContract,
    wethContract: state => state.wethContract,
    meedAddress: state => state.meedAddress,
    wethAddress: state => state.wethAddress,
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
    buttonLabel() {
      return this.buy && this.$t('buyMeeds') || this.$t('sellMeeds');
    },
    disabledButton() {
      return !this.toValue;
    },
    swapMethod() {
      if (this.provider && this.routerContract) {
        const routerContractSigner = this.routerContract.connect(this.provider.getSigner());
        return this.buy && routerContractSigner.swapExactETHForTokens || routerContractSigner.swapExactTokensForETH;
      }
      return null;
    },
  }),
  watch: {
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
      if (!this.fromValue) {
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
    sendTransaction() {
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
    getTransactionDeadline() {
      return Date.now() + this.deadlineMinutes * 60 * 1000;
    },
  },
};
</script>