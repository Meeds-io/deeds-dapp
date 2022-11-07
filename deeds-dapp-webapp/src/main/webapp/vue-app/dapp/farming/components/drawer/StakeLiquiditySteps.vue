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
  <v-stepper
    v-model="step"
    color="secondary"
    vertical
    flat>
    <v-stepper-step
      step="1"
      color="secondary"
      editable>
      {{ $t('approveLiquidity', {0: lpSymbol}) }}
    </v-stepper-step>
    <v-stepper-content step="1">
      <v-card
        v-if="approvalInProgress"
        class="mb-12"
        flat>
        <v-card-text>
          {{ $t('approvalTransactionInProgress') }}
        </v-card-text>
      </v-card>
      <v-card
        v-else
        class="mb-12"
        flat>
        <v-card-text>
          <deeds-number-format
            :value="lpBalance"
            :label-params="{1: lpSymbol}"
            :fractions="2"
            label="approveLPDescription"
            hide-zero />
          <v-text-field
            v-model="allowance"
            :rules="allowanceValueValidator"
            :hide-details="isAllowanceValueValid"
            placeholder="0.0"
            large
            outlined
            dense>
            <template #append>
              <v-chip
                outlined
                x-small
                class="mt-1 me-1"
                @click="setMaxAllowance">
                {{ $t('max') }}
              </v-chip>
              <div class="mt-1 text-no-wrap">
                {{ lpSymbol }}
              </div>
            </template>
          </v-text-field>
        </v-card-text>
        <v-card-actions class="ms-2">
          <v-btn
            :disabled="disabledApproveButton"
            :loading="sendingApproval"
            name="approveTokenButton"
            color="secondary"
            @click="approve">
            {{ $t('approve') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-stepper-content>
    <v-stepper-step
      step="2"
      color="secondary"
      :editable="hasStakeAllowance">
      {{ $t('stakeLiquidity', {0: lpSymbol}) }}
    </v-stepper-step>
    <v-stepper-content step="2">
      <v-card class="mb-12" flat>
        <v-card-text>
          <deeds-number-format
            :value="lpAllowance"
            :label-params="{1: lpSymbol}"
            :fractions="2"
            label="stakeLPDescription"
            hide-zero />
          <v-text-field
            v-model="stakeAmount"
            :rules="stakeAmountValidator"
            :hide-details="isStakeAmountValid"
            placeholder="0.0"
            large
            outlined
            dense>
            <template #append>
              <v-chip
                outlined
                x-small
                class="mt-1 me-1"
                @click="setMaxStakeAmount">
                {{ $t('max') }}
              </v-chip>
              <div class="mt-1 text-no-wrap">
                {{ lpSymbol }}
              </div>
            </template>
          </v-text-field>
        </v-card-text>
        <v-card-actions class="ms-2">
          <v-btn
            :disabled="disabledStakeButton"
            :loading="sendingStake"
            name="stakeTokenButton"
            color="secondary"
            @click="stake">
            {{ $t('stake') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-stepper-content>
  </v-stepper>
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
    lpBalance: {
      type: Object,
      default: null,
    },
    lpAllowance: {
      type: Object,
      default: null,
    },
    lpContract: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    step: 1,
    allowance: 0,
    stakeAmount: 0,
    sendingApproval: false,
    approvalInProgress: false,
    sendingStake: false,
  }),
  computed: Vuex.mapState({
    language: state => state.language,
    provider: state => state.provider,
    etherBalance: state => state.etherBalance,
    tokenFactoryAddress: state => state.tokenFactoryAddress,
    tokenFactoryContract: state => state.tokenFactoryContract,
    transactionGas: state => state.transactionGas,
    approvalGasLimit: state => state.approvalGasLimit,
    depositGasLimit: state => state.depositGasLimit,
    disabledApproveButton() {
      return !this.allowance || !Number(this.allowance) || !this.isAllowanceValueValid || this.sendingApproval || this.approvalInProgress;
    },
    disabledStakeButton() {
      return !this.stakeAmount || !Number(this.stakeAmount) || !this.isStakeAmountValid || this.sendingStake;
    },
    maxLpAmount() {
      if (this.lpBalance && !this.lpBalance.isZero()) {
        const max = this.$ethUtils.fromDecimals(this.lpBalance, 18);
        return Number(max);
      }
      return 0;
    },
    hasSufficientGas() {
      if (!this.allowance) {
        return true;
      }
      if (this.etherBalance && this.transactionGas) {
        const maxEther = this.$ethUtils.fromDecimals(this.etherBalance.sub(this.transactionGas), 18);
        return Number(maxEther) > 0;
      }
      return false;
    },
    isAllowanceLessThanMax() {
      if (!this.allowance) {
        return true;
      }
      return Number(this.allowance) <= this.maxLpAmount;
    },
    isStakeAmountLessThanMax() {
      if (!this.stakeAmount || !this.isStakeAmountNumeric) {
        return true;
      }
      const lpAllowance = this.lpAllowance;
      const stakeAmountToSend = this.$ethUtils.toDecimals(this.stakeAmount, 18);
      return stakeAmountToSend.lte(lpAllowance);
    },
    isAllowanceValueNumeric() {
      if (!this.allowance) {
        return true;
      }
      return this.allowance && Number.isFinite(Number(this.allowance));
    },
    isStakeAmountNumeric() {
      if (!this.stakeAmount) {
        return true;
      }
      return this.stakeAmount && Number.isFinite(Number(this.stakeAmount));
    },
    isAllowanceValueValid() {
      return !this.allowance || (this.isAllowanceValueNumeric && this.isAllowanceLessThanMax && this.hasSufficientGas);
    },
    isStakeAmountValid() {
      return !this.stakeAmount || (this.isStakeAmountNumeric && this.isStakeAmountLessThanMax && this.hasSufficientGas);
    },
    maxLpAmountBalanceNoDecimals() {
      return this.$ethUtils.fromDecimals(this.lpBalance, 18);
    },
    maxLpAmountAllowanceNoDecimals() {
      return this.$ethUtils.fromDecimals(this.lpAllowance, 18);
    },
    allowanceValueValidator() {
      return [
        () => !!this.hasSufficientGas || this.$t('insufficientTransactionFee'),
        () => !!this.isAllowanceValueNumeric || this.$t('valueMustBeNumeric'),
        () => !!this.isAllowanceLessThanMax || this.$t('valueMustBeLessThan', {0: this.maxLpAmountBalanceNoDecimals}),
      ];
    },
    stakeAmountValidator() {
      return [
        () => !!this.hasSufficientGas || this.$t('insufficientTransactionFee'),
        () => !!this.isStakeAmountNumeric || this.$t('valueMustBeNumeric'),
        () => !!this.isStakeAmountLessThanMax || this.$t('valueMustBeLessThan', {0: this.maxLpAmountAllowanceNoDecimals}),
      ];
    },
    hasStakeAllowance() {
      return this.lpAllowance && !this.lpAllowance.isZero();
    },
  }),
  watch: {
    approvalInProgress() {
      if (this.approvalInProgress) {
        this.$emit('start-loading');
      } else {
        this.$emit('end-loading');
      }
    },
    lpAllowance() {
      if (this.hasStakeAllowance) {
        this.step = 2;
        this.approvalInProgress = false;
      }
    },
  },
  methods: {
    approve() {
      this.sendingApproval = true;
      const amount = this.$ethUtils.toDecimals(this.allowance, 18);
      const options = {
        gasLimit: this.approvalGasLimit,
      };
      return this.$ethUtils.sendTransaction(
        this.provider,
        this.lpContract,
        'approve',
        options,
        [this.tokenFactoryAddress, amount]
      ).then(receipt => {
        const transactionHash = receipt.hash;
        this.$root.$emit('transaction-sent', transactionHash);
        this.approvalInProgress = true;
        this.sendingApproval = false;
        this.allowance = 0;
      }).catch(() => {
        this.sendingApproval = false;
      });
    },
    setMaxAllowance() {
      this.allowance = this.$ethUtils.fromDecimals(this.lpBalance, 18);
    },
    setMaxStakeAmount() {
      this.stakeAmount = this.$ethUtils.fromDecimals(this.lpAllowance, 18);
    },
    stake() {
      this.sendingStake = true;
      const amount = this.$ethUtils.toDecimals(this.stakeAmount, 18);
      const options = {
        gasLimit: this.depositGasLimit,
      };
      return this.$ethUtils.sendTransaction(
        this.provider,
        this.tokenFactoryContract,
        'deposit',
        options,
        [this.lpAddress, amount]
      ).then(receipt => {
        const transactionHash = receipt.hash;
        this.$root.$emit('transaction-sent', transactionHash);
        this.stakeAmount = 0;
        this.sendingStake = false;
        this.$root.$emit('close-drawer');
        this.step = 1;
      }).catch(() => {
        this.sendingStake = false;
      });
    },
  },
};
</script>