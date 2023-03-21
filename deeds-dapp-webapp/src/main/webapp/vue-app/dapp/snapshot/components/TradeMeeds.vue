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
  <v-card
    elevation="0"
    width="100%"
    height="250"
    max-width="350px"
    class="d-flex flex-column mx-auto"
    outlined>
    <v-card-title class="d-flex flex-column justify-center pb-2">
      <template v-if="buy">
        {{ $t('buyMeeds') }}
      </template>
      <template v-else>
        {{ $t('sellMeeds') }}
      </template>
    </v-card-title>
    <v-card-text class="d-flex flex-column flex-grow-1 pt-2">
      <v-text-field
        v-model="fromValue"
        :disabled="hasInvalidAddress || sending"
        :readonly="hasInvalidAddress || sending"
        :loading="loadingAmount"
        :rules="fromValueValidator"
        :hide-details="isFromValueValid"
        :step="buy && '0.1' || '100'"
        :min="0"
        :max="buy && maxEther || maxMeed"
        :autofocus="focus && !isMobile"
        class="align-center no-border"
        type="number"
        autocomplete="off"
        placeholder="0.0"
        outlined
        large
        dense>
        <template #append>
          <v-chip
            outlined
            x-small
            class="mt-1 me-1"
            @click="setMaxValue">
            {{ $t('max') }}
          </v-chip>
          <v-img
            v-if="buy"
            :src="`${parentLocation}/static/images/ether.svg`"
            :max-height="maxIconsSize"
            :max-width="maxIconsSize"
            class="me-1 mt-2px"
            contain
            eager />
          <v-img
            v-else
            :src="`${parentLocation}/static/images/meedsicon.png`"
            :max-height="maxIconsSize"
            :max-width="maxIconsSize"
            class="me-1 mt-2px"
            contain
            eager />
          <div class="mt-1">
            {{ buy && 'ETH' || 'MEED' }}
          </div>
        </template>
      </v-text-field>
      <v-hover v-slot="{ hover }">
        <v-card
          color="transparent"
          class="d-flex flex-column flex-grow-1"
          flat>
          <v-card
            color="transparent"
            class="d-flex justify-center align-center full-width position-relative"
            flat>
            <v-btn
              :elevation="hover ? 12 : 0"
              :disabled="typing || sending"
              name="switchTokenTradingButton"
              class="mx-auto z-index-2 white"
              absolute
              large
              icon
              light
              @click="switchInputs">
              <v-icon size="36" light>fas fa-angle-down</v-icon>
            </v-btn>
            <v-divider />
          </v-card>
          <v-text-field
            v-model="toValueDisplay"
            placeholder="0.0"
            class="align-center no-border"
            autocomplete="off"
            hide-details
            readonly
            outlined
            large
            dense>
            <template #append>
              <v-img
                v-if="buy"
                :src="`${parentLocation}/static/images/meedsicon.png`"
                :max-height="maxIconsSize"
                :max-width="maxIconsSize"
                class="me-1 mt-2px"
                contain
                eager />
              <v-img
                v-else
                :src="`${parentLocation}/static/images/ether.svg`"
                :max-height="maxIconsSize"
                :max-width="maxIconsSize"
                class="me-1 mt-2px"
                contain
                eager />
              <div class="mt-1">
                {{ buy && 'MEED' || 'ETH' }}
              </div>
            </template>
          </v-text-field>
        </v-card>
      </v-hover>
    </v-card-text>
    <v-card-title class="d-flex flex-column justify-center pb-2" />
  </v-card>
</template>
<script>
export default {
  props: {
    sending: {
      type: Boolean,
      default: false,
    },
    completedSteps: {
      type: Boolean,
      default: false,
    },
    maxEther: {
      type: Number,
      default: null,
    },
    maxMeed: {
      type: Number,
      default: null,
    },
    focus: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    computingAmount: false,
    buy: true,
    fromValue: null,
    toValue: null,
    startSearchAfterInMilliseconds: 600,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    typing: false,
    maxIconsSize: '20px',
  }),
  computed: Vuex.mapState({
    address: state => state.address,
    appLoading: state => state.appLoading,
    language: state => state.language,
    sushiswapRouterContract: state => state.sushiswapRouterContract,
    meedAddress: state => state.meedAddress,
    wethAddress: state => state.wethAddress,
    etherBalance: state => state.etherBalance,
    meedsBalance: state => state.meedsBalance,
    transactionGas: state => state.transactionGas,
    meedsRouteAllowance: state => state.meedsRouteAllowance,
    parentLocation: state => state.parentLocation,
    whiteThemeColor: state => state.whiteThemeColor,
    blackThemeColor: state => state.blackThemeColor,
    isMobile: state => state.isMobile,
    loadingAmount() {
      return !!this.computingAmount || this.typing;
    },
    tokenAdresses() {
      return this.buy && [this.wethAddress, this.meedAddress] || [this.meedAddress, this.wethAddress];
    },
    hasInvalidAddress() {
      return this.appLoading || !this.address;
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
    maxFromValueLabel() {
      if (this.buy) {
        return this.$ethUtils.fromDecimals(this.etherBalance.sub(this.transactionGas), 18);
      } else {
        return this.$ethUtils.fromDecimals(this.meedsBalance, 18);
      }
    },
    isFromValueValid() {
      return !this.fromValue || (this.isFromValueNumeric && this.isFromValueLessThanMax && this.hasSufficientGas);
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
      return this.fromValue && this.isFromValueNumeric && Number(this.fromValue) > 0;
    },
  }),
  watch: {
    completedSteps() {
      if (this.completedSteps) {
        this.reset();
      }
    },
    fromValue() {
      this.$emit('changed-amount-out', this.fromValue);
      if (!this.fromValue) {
        this.computeValue();
      } else {
        this.startTypingKeywordTimeout = Date.now() + this.startSearchAfterInMilliseconds;
        if (!this.typing) {
          this.typing = true;
          this.waitForEndTyping();
        }
      }
    },
    toValue() {
      this.$emit('changed-amount-in', this.toValue);
    },
    buy() {
      this.$emit('changed-buy', this.buy);
    },
    typing() {
      this.$emit('typing', this.typing);
    },
    computingAmount() {
      this.$emit('computing-amount', this.computingAmount);
    },
  },
  methods: {
    reset() {
      this.buy = true;
      this.fromValue = null;
      this.toValue = null;
      this.computeValue();
    },
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
        return this.$nextTick()
          .then(() => {
            return this.sushiswapRouterContract.getAmountsOut(amountIn, this.tokenAdresses)
              .then(amounts =>  this.toValue = this.$ethUtils.fromDecimals(amounts[1], 18))
              .finally(() => this.computingAmount = false);
          });
      }
    },
    setMaxValue() {
      if (this.buy) {
        const remainingEtherValue = this.$ethUtils.fromDecimals(this.etherBalance.sub(this.transactionGas), 18);
        if (remainingEtherValue > 0) {
          this.fromValue = remainingEtherValue;
        } else {
          this.fromValue = 0;
        }
      } else {
        this.fromValue = this.$ethUtils.fromDecimals(this.meedsBalance, 18);
      }
    },
  },
};
</script>