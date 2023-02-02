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
      <span class="subtitle-1">{{ $t('unstakeLPDescription', {0: stakedLp}) }}</span>
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
            @click="setMaxLPAmount">
            {{ $t('max') }}
          </v-chip>
          <div class="mt-1 text-no-wrap">
            {{ lpSymbol }}
          </div>
        </template>
      </v-text-field>
    </v-card-text>
    <v-card-actions class="px-4">
      <v-btn
        :disabled="disabledUnstakeButton"
        :loading="sendingUnstake"
        name="unstakeTokenButton"
        color="primary"
        @click="unstake">
        <span class="subtitle-1 font-weight-medium">{{ $t('unstake') }}</span>
      </v-btn>
    </v-card-actions>
  </v-card>
</template>
<script>
export default {
  props: {
    lpSymbol: {
      type: String,
      default: null,
    },
    lpAddress: {
      type: Object,
      default: null,
    },
    lpStaked: {
      type: Object,
      default: null,
    },
  },
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
    provider: state => state.provider,
    transactionGas: state => state.transactionGas,
    withdrawGasLimit: state => state.withdrawGasLimit,
    tokenFactoryContract: state => state.tokenFactoryContract,
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
      const lpStaked = this.lpStaked;
      const unstakeAmountToSend = this.$ethUtils.toDecimals(this.unstakeAmount, 18);
      return unstakeAmountToSend.lte(lpStaked);
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
    lpStakedNoDecimals() {
      return this.$ethUtils.fromDecimals(this.lpStaked, 18);
    },
    unstakeAmountValidator() {
      return [
        () => !!this.hasSufficientGas || this.$t('insufficientTransactionFee'),
        () => !!this.isUnstakeAmountNumeric || this.$t('valueMustBeNumeric'),
        () => !!this.isUnstakeAmountLessThanMax || this.$t('valueMustBeLessThan', {0: this.lpStakedNoDecimals}),
      ];
    },
  }),
  methods: {
    setMaxLPAmount() {
      this.unstakeAmount = this.$ethUtils.fromDecimals(this.lpStaked, 18);
    },
    unstake() {
      this.sendingUnstake = true;
      const amount = this.$ethUtils.toDecimals(this.unstakeAmount, 18);
      const options = {
        gasLimit: this.withdrawGasLimit,
      };
      return this.$ethUtils.sendTransaction(
        this.provider,
        this.tokenFactoryContract,
        'withdraw',
        options,
        [this.lpAddress, amount]
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