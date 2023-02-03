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
  <v-card class="mb-12" flat>
    <v-card-text>
      {{ $t('unstakeXMeedsDescription', {0: xMeedsBalanceNoDecimals}) }}
      <v-text-field
        v-model="unstakeAmount"
        :rules="unstakeAmountValidator"
        :hide-details="isUnstakeAmountValid"
        autocomplete="off"
        placeholder="0.0"
        large
        outlined
        dense>
        <template #append>
          <v-chip
            outlined
            x-small
            class="mt-1 me-1"
            @click="setMaxXMeeds">
            {{ $t('max') }}
          </v-chip>
          <div class="mt-1">
            xMEED
          </div>
        </template>
      </v-text-field>
      <small v-if="unstakedMeedsAmountNoDecimals">
        {{ $t('unstakeMeedsEstimation', {0: unstakedMeedsAmountNoDecimals}) }}
      </small>
    </v-card-text>
    <v-card-actions class="px-4">
      <v-btn
        :disabled="disabledUnstakeButton"
        :loading="sendingUnstake"
        name="unstakeTokensButton"
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
    provider: state => state.provider,
    xMeedContract: state => state.xMeedContract,
    transactionGas: state => state.transactionGas,
    unstakeGasLimit: state => state.unstakeGasLimit,
    address: state => state.address,
    meedsBalanceOfXMeeds: state => state.meedsBalanceOfXMeeds,
    xMeedsTotalSupply: state => state.xMeedsTotalSupply,
    meedsPendingBalanceOfXMeeds: state => state.meedsPendingBalanceOfXMeeds,
    totalMeedsBalanceOfXMeeds() {
      return this.meedsBalanceOfXMeeds && this.meedsPendingBalanceOfXMeeds && this.meedsPendingBalanceOfXMeeds.add(this.meedsBalanceOfXMeeds) || 0;
    },
    disabledUnstakeButton() {
      return !this.unstakeAmount || !Number(this.unstakeAmount) || !this.isUnstakeAmountValid || this.sendingUnstake;
    },
    xMeedsBalanceNoDecimals() {
      return this.xMeedsBalance && this.$ethUtils.computeTokenBalanceNoDecimals(this.xMeedsBalance, 2, this.language) || 0;
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
    unstakedMeedsAmount() {
      if (this.isUnstakeAmountValid && Number(this.unstakeAmount) && this.totalMeedsBalanceOfXMeeds && this.xMeedsTotalSupply) {
        if (this.xMeedsTotalSupply.isZero() || this.totalMeedsBalanceOfXMeeds.isZero()) {
          return this.unstakeAmount;
        } else {
          return new BigNumber(this.totalMeedsBalanceOfXMeeds.toString()).multipliedBy(this.unstakeAmount).dividedBy(this.xMeedsTotalSupply.toString()).toString();
        }
      } else {
        return 0;
      }
    },
    unstakedMeedsAmountNoDecimals() {
      if (this.unstakedMeedsAmount) {
        return this.$ethUtils.toFixedDisplay(
          this.unstakedMeedsAmount,
          3,
          this.language);
      } else {
        return 0;
      }
    },
    unstakeAmountValidator() {
      return [
        () => !!this.hasSufficientGas || this.$t('insufficientTransactionFee'),
        () => !!this.isUnstakeAmountNumeric || this.$t('valueMustBeNumeric'),
        () => !!this.isUnstakeAmountLessThanMax || this.$t('valueMustBeLessThan', {0: this.xMeedsBalanceNoDecimals}),
      ];
    },
  }),
  methods: {
    setMaxXMeeds() {
      this.unstakeAmount = this.$ethUtils.fromDecimals(this.xMeedsBalance, 18);
    },
    unstake() {
      this.sendingUnstake = true;
      const amount = this.$ethUtils.toDecimals(this.unstakeAmount, 18);
      const options = {
        from: this.address,
        gasLimit: this.unstakeGasLimit,
      };
      return this.$ethUtils.sendTransaction(
        this.provider,
        this.xMeedContract,
        'withdraw',
        options,
        [amount]
      ).then(receipt => {
        const transactionHash = receipt?.hash;
        if (transactionHash) {
          this.unstakeAmount = 0;
          this.$root.$emit('close-drawer');
        }
        this.sendingUnstake = false;
      }).catch(() => this.sendingUnstake = false);
    },
  },
};
</script>