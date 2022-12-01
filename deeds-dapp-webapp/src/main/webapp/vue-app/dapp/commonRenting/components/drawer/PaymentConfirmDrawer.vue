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
      <h4>{{ $t('confirmRentPayment') }}</h4>
    </template>
    <template #content>
      <v-stepper
        v-model="step"
        color="secondary"
        vertical
        flat>
        <v-stepper-step
          step="1"
          color="secondary"
          editable>
          {{ $t('approveMeedsForRenting') }}
        </v-stepper-step>
        <v-stepper-content step="1">
          <v-card
            v-if="approvalInProgress"
            class="mb-12"
            flat>
            <v-card-text>
              {{ $t('waitApprovalTransactionInProgress') }}
            </v-card-text>
          </v-card>
          <v-card
            v-else
            class="mb-12"
            flat>
            <v-card-text>
              {{ $t('approveMeedsForRentPaymentSubtitle') }}
              <v-text-field
                v-model="amountNoDecimals"
                :rules="allowanceValidator"
                disabled
                large
                outlined
                hide-details
                dense>
                <template #append>
                  <div class="mt-1">
                    MEED
                  </div>
                </template>
              </v-text-field>
            </v-card-text>
            <v-card-actions class="ms-2">
              <v-btn
                :disabled="disabledApproveButton"
                :loading="sendingApproval"
                name="approveTokensButton"
                color="primary"
                @click="approve">
                {{ $t('approve') }}
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-stepper-content>
        <v-stepper-step
          step="2"
          color="secondary"
          editable>
          {{ $t('confirmRentPayment') }}
        </v-stepper-step>
        <v-stepper-content step="2">
          <v-card class="mb-12" flat>
            <v-card-text>
              {{ $t('confirmRentPaymentSubtitle') }}
              <v-text-field
                v-model="amountNoDecimals"
                :rules="confirmValidator"
                disabled
                large
                outlined
                hide-details
                dense>
                <template #append>
                  <div class="mt-1">
                    MEED
                  </div>
                </template>
              </v-text-field>
            </v-card-text>
            <v-card-actions class="ms-2">
              <v-btn
                :disabled="disabledConfirmButton"
                :loading="sendingConfirm"
                name="confirmOperationButton"
                color="primary"
                @click="confirm">
                {{ $t('confirm') }}
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-stepper-content>
      </v-stepper>
    </template>
  </deeds-drawer>
</template>

<script>
export default {
  props: {
    leaseId: {
      type: Number,
      default: null,
    },
    ownerAddress: {
      type: String,
      default: null,
    },
    monthsToPay: {
      type: Number,
      default: null,
    },
    amount: {
      type: Number,
      default: null,
    },
  },
  data: () => ({
    step: 1,
    allowance: 0,
    drawer: false,
    sendingApproval: false,
    approvalInProgress: false,
    sendingConfirm: false,
  }),
  computed: Vuex.mapState({
    provider: state => state.provider,
    meedContract: state => state.meedContract,
    address: state => state.address,
    language: state => state.language,
    etherBalance: state => state.etherBalance,
    meedsBalance: state => state.meedsBalance,
    transactionGas: state => state.transactionGas,
    approvalGasLimit: state => state.approvalGasLimit,
    maxGasLimit: state => state.maxGasLimit,
    tenantRentingAddress: state => state.tenantRentingAddress,
    tenantRentingContract: state => state.tenantRentingContract,
    sending() {
      return this.sendingApproval || this.sendingConfirm;
    },
    amountNoDecimals() {
      return this.amount && this.$ethUtils.computeTokenBalanceNoDecimals(this.amount, 2, this.language) || 0;
    },
    hasSufficientGas() {
      if (this.etherBalance && this.transactionGas) {
        const maxEther = this.$ethUtils.fromDecimals(this.etherBalance.sub(this.transactionGas), 18);
        return Number(maxEther) > 0;
      }
      return false;
    },
    hasSufficientMeeds() {
      return this.meedsBalance && this.meedsBalance.gte(this.amount);
    },
    hasSufficientAllowance() {
      return this.allowance && this.allowance.gte(this.amount);
    },
    allowanceValidator() {
      return [
        () => !!this.hasSufficientGas || this.$t('insufficientTransactionFee'),
        () => !!this.hasSufficientMeeds || this.$t('insufficientMeeds'),
      ];
    },
    confirmValidator() {
      return [
        () => !!this.hasSufficientGas || this.$t('insufficientTransactionFee'),
        () => !!this.hasSufficientMeeds || this.$t('insufficientMeeds'),
        () => !!this.hasSufficientAllowance || this.$t('insufficientAllowedMeeds'),
      ];
    },
    disabledApproveButton() {
      return !this.hasSufficientGas || !this.hasSufficientMeeds || this.sendingApproval || this.approvalInProgress;
    },
    disabledConfirmButton() {
      return this.disabledApproveButton || !this.hasSufficientMeeds || !this.hasSufficientAllowance;
    },
  }),
  watch: {
    approvalInProgress() {
      if (this.approvalInProgress) {
        this.$refs.drawer.startLoading();
      } else {
        this.$refs.drawer.endLoading();
      }
    },
    allowance() {
      if (this.hasSufficientAllowance) {
        this.step = 2;
        this.approvalInProgress = false;
      }
    },
  },
  created() {
    this.computeAllowance();
    document.addEventListener('dapp-meeds-approved', this.handleAllowanceEvent);
  },
  beforeDestroy() {
    document.removeEventListener('dapp-meeds-approved', this.handleAllowanceEvent);
  },
  methods: {
    open() {
      this.$refs.drawer.open();
    },
    handleAllowanceEvent(event) {
      const detail = event?.detail;
      if (detail?.to?.toLowerCase() === this.tenantRentingAddress.toLowerCase()) {
        this.computeAllowance();
      }
    },
    computeAllowance() {
      return this.meedContract.allowance(this.address, this.tenantRentingAddress)
        .then(allowance => this.allowance = allowance);
    },
    approve() {
      this.sendingApproval = true;
      const options = {
        from: this.address,
        gasLimit: this.approvalGasLimit,
      };
      return this.$ethUtils.sendTransaction(
        this.provider,
        this.meedContract,
        'approve',
        options,
        [this.tenantRentingAddress, this.amount]
      ).then(receipt => {
        const transactionHash = receipt?.hash;
        if (transactionHash) {
          this.approvalInProgress = true;
          this.allowance = 0;
        }
        this.sendingApproval = false;
      }).catch(() => this.sendingApproval = false);
    },
    confirm() {
      this.sendingConfirm = true;
      const options = {
        from: this.address,
        gasLimit: this.maxGasLimit,
      };
      return this.$ethUtils.sendTransaction(
        this.provider,
        this.tenantRentingContract,
        'payRent',
        options,
        [this.leaseId, this.ownerAddress, this.monthsToPay]
      ).then(receipt => {
        const transactionHash = receipt?.hash;
        if (transactionHash) {
          this.$emit('transaction-sent', transactionHash);
          this.drawer = false;
        }
        this.sendingConfirm = false;
      }).catch(() => this.sendingConfirm = false);
    },
  },
};
</script>