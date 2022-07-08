<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2022 Meeds Association contact@meeds.io
 
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
  <v-card flat>
    <h3 class="d-flex flex-nowrap">
      {{ $t('Meeds token') }}
      <v-divider class="my-auto ms-4" />
    </h3>
    {{ MeedsPriceValue }}
    <deeds-price-chart class="mb-4 mb-sm-8" />
    <deeds-currencies-chart />
  </v-card>
</template>
<script>
export default {
  data: () => ({
  }),
  computed: Vuex.mapState({
    meedPrice: state => state.meedPrice,
    xMeedsBalance: state => state.xMeedsBalance,
    selectedFiatCurrency: state => state.selectedFiatCurrency,
  
    MeedsPriceValue() {
      const fractions = this.fractions || (this.selectedFiatCurrency === 'eth' ? 8 : 3);
      return this.$ethUtils.fractionsToDisplay(
        this.meedPrice,
        fractions);
   
    },
  
  }),
};
</script>