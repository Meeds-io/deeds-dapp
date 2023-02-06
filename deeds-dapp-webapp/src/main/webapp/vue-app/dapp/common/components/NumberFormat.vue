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
  <div v-if="display" class="number-format">
    <template v-if="label">
      {{ $t(label, labelComputedParams) }}
      <slot></slot>
    </template>
    <template v-else>
      {{ formattedValue || '-' }}
      <slot></slot>
    </template>
  </div>
</template>

<script>
export default {
  props: {
    value: {
      type: Number,
      default: null,
    },
    label: {
      type: Number,
      default: null,
    },
    labelParams: {
      type: Object,
      default: null,
    },
    fractions: {
      type: Number,
      default: 0,
    },
    noDecimals: {
      type: Boolean,
      default: false,
    },
    currency: {
      type: Boolean,
      default: false,
    },
    hideZero: {
      type: Boolean,
      default: false,
    },
  },
  computed: Vuex.mapState({
    language: state => state.language,
    meedPrice: state => state.meedPrice,
    selectedFiatCurrency: state => state.selectedFiatCurrency,
    display() {
      return !this.hideZero || (this.value && (!this.value.isZero || !this.value.isZero()));
    },
    labelComputedParams() {
      return this.labelParams && Object.assign({0: this.formattedValue || '-'}, this.labelParams) || {0: this.formattedValue || '-'};
    },
    formattedValue() {
      if (!this.value && this.value !== 0) {
        return null;
      }
      if (this.currency) {
        const fractions = this.fractions || (this.selectedFiatCurrency === 'eth' ? 8 : 3);
        return this.meedPrice && this.$ethUtils.computeFiatBalance(
          this.value || 0,
          this.meedPrice,
          this.selectedFiatCurrency,
          fractions,
          this.language) || '-';
      } else if (this.noDecimals) {
        return this.$ethUtils.toFixedDisplay(this.value, this.fractions, this.language);
      } else {
        return this.$ethUtils.computeTokenBalanceNoDecimals(
          this.value || 0,
          this.fractions,
          this.language);
      }
    },
  }),
  created() {
    if (!this.meedPrice && this.currency) {
      this.$store.commit('loadMeedPrice');
    }
  },
};
</script>