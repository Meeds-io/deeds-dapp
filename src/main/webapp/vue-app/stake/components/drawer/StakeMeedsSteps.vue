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
  <v-stepper
    v-model="step"
    vertical
    flat>
    <v-stepper-step
      :complete="meedsBalanceNoDecimals > 0"
      editable
      step="1">
      {{ $t('approveMeeds') }}
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
          {{ $t('approveMeedsDescription', {0: meedsBalanceNoDecimals}) }}
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
              <div class="mt-1">
                MEED
              </div>
            </template>
          </v-text-field>
          <small v-if="stakedMeedsAmountNoDecimals">
            {{ $t('stakeMeedsEstimation', {0: stakedMeedsAmountNoDecimals}) }}
          </small>
        </v-card-text>
        <v-card-actions class="ms-2">
          <v-btn
            :disabled="disabledApproveButton"
            :loading="sendingApproval"
            color="primary"
            @click="approve">
            {{ $t('approve') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-stepper-content>
    <v-stepper-step
      step="2"
      :editable="hasMeedsStakeAllowance">
      {{ $t('stakeMeeds') }}
    </v-stepper-step>
    <v-stepper-content step="2">
      <v-card class="mb-12" flat>
        <v-card-text>
          {{ $t('stakeMeedsDescription', {0: meedsStakeAllowanceNoDecimals}) }}
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
              <div class="mt-1">
                MEED
              </div>
            </template>
          </v-text-field>
          <small v-if="stakedMeedsAmountNoDecimals">
            {{ $t('stakeMeedsEstimation', {0: stakedMeedsAmountNoDecimals}) }}
          </small>
        </v-card-text>
        <v-card-actions class="ms-2">
          <v-btn
            :disabled="disabledStakeButton"
            :loading="sendingStake"
            color="primary"
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
    etherBalance: state => state.etherBalance,
    meedsBalance: state => state.meedsBalance,
    meedsBalanceNoDecimals: state => state.meedsBalanceNoDecimals,
    meedsStakeAllowance: state => state.meedsStakeAllowance,
    xMeedsTotalSupply: state => state.xMeedsTotalSupply,
    meedsBalanceOfXMeeds: state => state.meedsBalanceOfXMeeds,
    provider: state => state.provider,
    meedContract: state => state.meedContract,
    xMeedContract: state => state.xMeedContract,
    xMeedAddress: state => state.xMeedAddress,
    transactionGas: state => state.transactionGas,
    gasLimit: state => state.gasLimit,
    meedsStakeAllowanceNoDecimals() {
      return this.$ethUtils.computeTokenBalanceNoDecimals(
        this.meedsStakeAllowance,
        3,
        this.language);
    },
    stakedMeedsAmount() {
      const stakeAmount = Number(this.step) === 1 && this.allowance || this.stakeAmount;
      if (this.isStakeAmountValid && Number(stakeAmount) && this.meedsBalanceOfXMeeds && this.xMeedsTotalSupply) {
        if (this.xMeedsTotalSupply.isZero() || this.meedsBalanceOfXMeeds.isZero()) {
          return stakeAmount;
        } else {
          return new BigNumber(this.xMeedsTotalSupply.toString()).multipliedBy(stakeAmount).dividedBy(this.meedsBalanceOfXMeeds.toString()).toString();
        }
      } else {
        return 0;
      }
    },
    stakedMeedsAmountNoDecimals() {
      if (this.stakedMeedsAmount) {
        return this.$ethUtils.toFixedDisplay(
          this.stakedMeedsAmount,
          3,
          this.language);
      } else {
        return 0;
      }
    },
    disabledApproveButton() {
      return !this.allowance || !Number(this.allowance) || !this.isAllowanceValueValid || this.sendingApproval || this.approvalInProgress;
    },
    disabledStakeButton() {
      return !this.stakeAmount || !Number(this.stakeAmount) || !this.isStakeAmountValid || this.sendingStake;
    },
    maxMeed() {
      if (this.meedsBalance && !this.meedsBalance.isZero()) {
        const maxMeed = this.$ethUtils.fromDecimals(this.meedsBalance, 18);
        return Number(maxMeed);
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
      return Number(this.allowance) <= this.maxMeed;
    },
    isStakeAmountLessThanMax() {
      if (!this.stakeAmount || !this.isStakeAmountNumeric) {
        return true;
      }
      const meedsStakeAllowance = this.meedsStakeAllowance;
      const stakeAmountToSend = this.$ethUtils.toDecimals(this.stakeAmount, 18);
      return stakeAmountToSend.lte(meedsStakeAllowance);
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
    maxMeedsBalanceNoDecimals() {
      return this.$ethUtils.fromDecimals(this.meedsBalance, 18);
    },
    maxMeedsAllowanceNoDecimals() {
      return this.$ethUtils.fromDecimals(this.meedsBalance, 18);
    },
    allowanceValueValidator() {
      return [
        () => !!this.hasSufficientGas || this.$t('insufficientTransactionFee'),
        () => !!this.isAllowanceValueNumeric || this.$t('valueMustBeNumeric'),
        () => !!this.isAllowanceLessThanMax || this.$t('valueMustBeLessThan', {0: this.maxMeedsBalanceNoDecimals}),
      ];
    },
    stakeAmountValidator() {
      return [
        () => !!this.hasSufficientGas || this.$t('insufficientTransactionFee'),
        () => !!this.isStakeAmountNumeric || this.$t('valueMustBeNumeric'),
        () => !!this.isStakeAmountLessThanMax || this.$t('valueMustBeLessThan', {0: this.maxMeedsAllowanceNoDecimals}),
      ];
    },
    hasMeedsStakeAllowance() {
      return this.meedsStakeAllowance && !this.meedsStakeAllowance.isZero();
    },
    approveMethod() {
      if (this.provider && this.meedContract) {
        const meedContractSigner = this.meedContract.connect(this.provider.getSigner());
        return meedContractSigner.approve;
      }
      return null;
    },
    stakeMethod() {
      if (this.provider && this.xMeedContract) {
        const xMeedContractSigner = this.xMeedContract.connect(this.provider.getSigner());
        return xMeedContractSigner.stake;
      }
      return null;
    },
  }),
  watch: {
    meedsStakeAllowance() {
      if (this.hasMeedsStakeAllowance) {
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
        gasLimit: this.gasLimit,
      };
      return this.approveMethod(
        this.xMeedAddress,
        amount,
        options
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
      this.allowance = this.$ethUtils.fromDecimals(this.meedsBalance, 18);
    },
    setMaxStakeAmount() {
      this.stakeAmount = this.$ethUtils.fromDecimals(this.meedsStakeAllowance, 18);
    },
    stake() {
      this.sendingStake = true;
      const amount = this.$ethUtils.toDecimals(this.stakeAmount, 18);
      const options = {
        gasLimit: this.gasLimit,
      };
      return this.stakeMethod(
        amount,
        options
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