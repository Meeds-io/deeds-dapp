<template>
  <v-card class="mb-12" flat>
    <v-card-text>
      {{ $t('unstakeMeedsDescription', {0: xMeedsBalanceNoDecimals}) }}
    </v-card-text>
    <v-card-text>
      <v-text-field
        v-model="unstakeAmount"
        :rules="unstakeAmountValidator"
        :hide-details="isUnstakeAmountValid"
        placeholder="0.0"
        large
        outlined
        dense>
        <template #append>
          <div class="mt-1">
            MEED
          </div>
        </template>
      </v-text-field>
    </v-card-text>
    <v-card-actions class="px-4">
      <v-btn
        :disabled="disabledUnstakeButton"
        :loading="sendingUnstake"
        color="primary"
        @click="unstake">
        {{ $t('unstake') }}
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script>
export default {
  data: () => ({
    step: 1,
    allowance: 0,
    unstakeAmount: 0,
    sendingApproval: false,
    approvalInProgress: false,
    sendingUnstake: false,
  }),
  computed: Vuex.mapState({
    language: state => state.language,
    etherBalance: state => state.etherBalance,
    xMeedsBalance: state => state.xMeedsBalance,
    xMeedsBalanceNoDecimals: state => state.xMeedsBalanceNoDecimals,
    provider: state => state.provider,
    xMeedContract: state => state.xMeedContract,
    transactionGas: state => state.transactionGas,
    gasLimit: state => state.gasLimit,
    disabledUnstakeButton() {
      return !this.unstakeAmount || !Number(this.unstakeAmount) || !this.isUnstakeAmountValid || this.sendingUnstake;
    },
    hasSufficientGas() {
      if (!this.unstakeAmount) {
        return true;
      }
      if (this.etherBalance && this.transactionGas) {
        const maxEther = this.$ethUtils.fromDecimals(this.etherBalance.sub(this.transactionGas), 18);
        return Number(maxEther) > 0;
      }
      return false;
    },
    isUnstakeAmountLessThanMax() {
      if (!this.unstakeAmount || !this.isUnstakeAmountNumeric) {
        return true;
      }
      const xMeedsBalance = this.xMeedsBalance;
      const unstakeAmountToSend = this.$ethUtils.toDecimals(this.unstakeAmount, 18);
      return unstakeAmountToSend.lte(xMeedsBalance);
    },
    isUnstakeAmountNumeric() {
      if (!this.unstakeAmount) {
        return true;
      }
      return this.unstakeAmount && Number.isFinite(Number(this.unstakeAmount));
    },
    isUnstakeAmountValid() {
      return !this.unstakeAmount || (this.isUnstakeAmountNumeric && this.isUnstakeAmountLessThanMax && this.hasSufficientGas);
    },
    unstakeAmountValidator() {
      return [
        () => !!this.hasSufficientGas || this.$t('insufficientTransactionFee'),
        () => !!this.isUnstakeAmountNumeric || this.$t('valueMustBeNumeric'),
        () => !!this.isUnstakeAmountLessThanMax || this.$t('valueMustBeLessThan', {0: this.xMeedsBalanceNoDecimals}),
      ];
    },
    withdrawMethod() {
      if (this.provider && this.xMeedContract) {
        const xMeedContractSigner = this.xMeedContract.connect(this.provider.getSigner());
        return xMeedContractSigner.withdraw;
      }
      return null;
    },
  }),
  methods: {
    unstake() {
      this.sendingUnstake = true;
      const amount = this.$ethUtils.toDecimals(this.unstakeAmount, 18);
      const options = {
        gasLimit: this.gasLimit,
      };
      return this.withdrawMethod(
        amount,
        options
      ).then(receipt => {
        const transactionHash = receipt.hash;
        this.$root.$emit('transaction-sent', transactionHash);
        this.unstakeAmount = 0;
        this.sendingUnstake = false;
        this.$root.$emit('close-drawer');
      }).catch(() => {
        this.sendingUnstake = false;
      });
    },
  },
};
</script>