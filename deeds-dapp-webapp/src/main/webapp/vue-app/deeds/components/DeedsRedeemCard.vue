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
  <v-hover v-slot="{hover}">
    <v-card
      :elevation="hover ? 3 : 0"
      :loading="sendingRedeem || loading"
      class="mx-auto my-2"
      min-width="250px"
      max-width="250px"
      outlined>
      <v-card-text class="d-flex">
        <v-img
          v-if="cardImage"
          :src="cardImage"
          min-width="215"
          width="100%"
          max-width="100%"
          contain
          eager />
        <v-icon
          v-else
          size="72"
          class="mx-auto">
          mdi-image-size-select-actual
        </v-icon>
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
          {{ cardDescription }}
        </div>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn
          :disabled="disableRedeemButton"
          :loading="sendingRedeem"
          name="redeemButton"
          outlined
          text
          @click="redeem">
          <span class="text-none">{{ $t('redeem') }}</span>
        </v-btn>
        <v-chip
          class="ms-2 primary"
          dark
          small>
          <deeds-number-format :value="cardAmount" :fractions="6">
            {{ $t('points') }}
          </deeds-number-format>
        </v-chip>
        <v-spacer />
      </v-card-actions>
    </v-card>
  </v-hover>
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
    cardImage: null,
    cardDescription: null,
  }),
  computed: Vuex.mapState({
    address: state => state.address,
    language: state => state.language,
    provider: state => state.provider,
    xMeedContract: state => state.xMeedContract,
    pointsBalance: state => state.pointsBalance,
    redeemGasLimit: state => state.redeemGasLimit,
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
    cityIndex() {
      return parseInt(this.cardType / 4);
    },
    cardTypeIndex() {
      return this.cardType % 4;
    },
    cardAmount() {
      return this.card && this.card.amount;
    },
    disableRedeemButton() {
      return this.sendingRedeem
        || !this.currentCityMintable
        || !this.pointsBalance
        || !this.cardAmount
        || this.cardAmount.gt(this.pointsBalance)
        || (this.cardSupply || 0) >= this.cardMaxSupply;
    },
  }),
  created() {
    this.$deedMetadata.getCardInfo(this.cityIndex, this.cardTypeIndex)
      .then(cardInfo => {
        this.cardDescription = cardInfo.description;
        this.cardImage = `${cardInfo.image}?_=3299479372153`;
      });
  },
  methods: {
    redeem() {
      this.sendingRedeem = true;
      const options = {
        gasLimit: this.redeemGasLimit,
      };
      return this.$ethUtils.sendTransaction(
        this.provider,
        this.xMeedContract,
        'redeem',
        options,
        [this.cardType]
      ).then(receipt => {
        const transactionHash = receipt.hash;
        this.$root.$emit('transaction-sent', transactionHash);
      }).finally(() => this.sendingRedeem = false);
    },
  },
};
</script>