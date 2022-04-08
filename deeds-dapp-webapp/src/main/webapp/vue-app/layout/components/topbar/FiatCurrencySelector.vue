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
  <v-menu offset-y>
    <template #activator="{ on, attrs }">
      <v-btn
        name="fiatCurrencySelectorButton"
        outlined
        text
        class="ps-2 pe-0"
        v-bind="attrs"
        v-on="on">
        <div>{{ selectedFiatCurrencyLabel }}</div>
        <v-icon>mdi-menu-down</v-icon>
      </v-btn>
    </template>
    <v-list>
      <v-list-item
        v-for="fiatCurrencyOption in fiatCurrencyOptions"
        :key="fiatCurrencyOption.value"
        @click="changeFiatCurrency(fiatCurrencyOption.value)">
        <v-list-item-title>{{ fiatCurrencyOption.label }}</v-list-item-title>
      </v-list-item>
    </v-list>
  </v-menu>
</template>
<script>
export default {
  data: () => ({
    fiatCurrencies: ['usd', 'eur', 'eth'],
  }),
  computed: Vuex.mapState({
    selectedFiatCurrency: state => state.selectedFiatCurrency,
    selectedFiatCurrencyLabel() {
      return this.$t(`fiat.currency.${this.selectedFiatCurrency}`);
    },
    fiatCurrencyOptions() {
      return this.fiatCurrencies.map(currency => ({
        value: currency,
        label: this.$t(`fiat.currency.${currency}`),
      }));
    },
  }),
  methods: {
    changeFiatCurrency(fiatCurrency) {
      this.$store.commit('selectFiatCurrency', fiatCurrency);
    },
  },
};
</script>