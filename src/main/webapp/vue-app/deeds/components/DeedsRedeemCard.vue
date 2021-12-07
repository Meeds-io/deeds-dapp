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
  <v-card
    :loading="sendingRedeem || loading"
    class="mx-auto my-2"
    max-width="250px">
    <v-card-text class="d-flex">
      <v-icon size="72" class="mx-auto">mdi-image-size-select-actual</v-icon>
    </v-card-text>
    <v-card-title class="justify-center pt-0">
      {{ cardName }}
    </v-card-title>
    <v-card-text class="pt-0">
      <div class="text-subtitle-1">
        {{ $t('cardSupply') }}:
        <v-chip class="ms-2">{{ cardSupply }} / {{ cardMaxSupply }}</v-chip>
      </div>
    </v-card-text>
    <v-divider class="mx-4" />
    <v-card-text>
      <div>
        Lorem Ipsum is simply dummy text of the printing and typesetting industry.
      </div>
    </v-card-text>
    <v-card-actions>
      <v-spacer />
      <v-btn
        :disabled="disableRedeemButton"
        :loading="sendingRedeem"
        outlined
        text
        @click="redeem">
        <span class="text-none">{{ $t('redeem') }}</span>
      </v-btn>
      <v-chip
        class="ms-2 primary"
        dark
        small>
        {{ cardAmountNoDecimals }} {{ $t('points') }}
      </v-chip>
      <v-spacer />
    </v-card-actions>
  </v-card>
</template>
<script>
export default {
  props: {
    card: {
      type: Object,
      default: null,
    },
    loading: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    sendingRedeem: false,
  }),
  computed: Vuex.mapState({
    address: state => state.address,
    language: state => state.language,
    provider: state => state.provider,
    xMeedContract: state => state.xMeedContract,
    pointsBalance: state => state.pointsBalance,
    gasLimit: state => state.gasLimit,
    currentCityMintable: state => state.currentCityMintable,
    cardName() {
      return this.card && this.card.name;
    },
    cardSupply() {
      return this.card && this.card.supply;
    },
    cardMaxSupply() {
      return this.card && this.card.maxSupply;
    },
    cardType() {
      return this.card && this.card.cardType;
    },
    cardAmount() {
      return this.card && this.card.amount;
    },
    cardAmountNoDecimals() {
      return this.$ethUtils.computeTokenBalanceNoDecimals(
        this.cardAmount || 0,
        6,
        this.language);
    },
    disableRedeemButton() {
      return this.sendingRedeem
        || !this.currentCityMintable
        || !this.pointsBalance
        || !this.cardAmount
        || this.cardAmount.gt(this.pointsBalance)
        || (this.cardSupply || 0) >= this.cardMaxSupply;
    },
    redeemMethod() {
      if (this.provider && this.xMeedContract) {
        const xMeedContractSigner = this.xMeedContract.connect(this.provider.getSigner());
        return xMeedContractSigner.redeem;
      }
      return null;
    },
  }),
  methods: {
    redeem() {
      this.sendingRedeem = true;
      const options = {
        gasLimit: this.gasLimit,
      };
      return this.redeemMethod(
        this.cardType,
        options
      ).then(receipt => {
        const transactionHash = receipt.hash;
        this.$root.$emit('transaction-sent', transactionHash);
      }).finally(() => this.sendingRedeem = false);
    },
  },
};
</script>