<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 
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
    <deeds-page-title-layout>
      <template #title>
        {{ $t('page.farm.title') }}
      </template>
      <template #subtitle>
        {{ $t('page.farm.subtitle') }}
      </template>
    </deeds-page-title-layout>
    <v-card-text class="ps-0 pt-4 pt-md-11 text-h6 font-weight-normal">
      {{ $t('addLiquidityIntroduction') }}
    </v-card-text>
    <v-bottom-navigation
      width="auto"
      height="auto"
      class="elevation-0 mt-4"
      background-color="transparent">
      <v-btn
        id="fSushi-button"
        :href="addSushiswapLiquidityLink"
        name="addSushiswapLiquidityLink"
        target="_blank"
        rel="nofollow noreferrer noopener"
        height="90px"
        width="120px"
        outlined
        text
        class="rounded-lg me-4">
        <span class="text-none">Sushiswap</span>
        <img
          :src="`${parentLocation}/static/images/sushiswap.ico`"
          class="my-3 addLiquidityIcon">
      </v-btn>
      <v-btn
        id="fUniswap-button"
        v-if="addUniswapLiquidityLink"
        :href="addUniswapLiquidityLink"
        name="addUniswapLiquidityLink"
        target="_blank"
        rel="nofollow noreferrer noopener"
        height="90px"
        width="120px"
        outlined
        text
        class="rounded-lg me-4">
        <span class="text-none">Uniswap</span>
        <div class="my-3 headline">🦄</div>
      </v-btn>
      <v-btn
        id="fCometh-button"
        v-if="addComethLiquidityLink"
        :href="addComethLiquidityLink"
        name="addComethLiquidityLink"
        target="_blank"
        rel="nofollow noreferrer noopener"
        height="90px"
        width="120px"
        outlined
        text
        class="rounded-lg me-4">
        <span class="text-none">Cometh</span>
        <img
          :src="`${parentLocation}/static/images/cometh.ico`"
          class="my-3 addLiquidityIcon"
          alt="Cometh Icon">
      </v-btn>
    </v-bottom-navigation>
  </v-card>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    addSushiswapLiquidityLink: state => state.addSushiswapLiquidityLink,
    addUniswapLiquidityLink: state => state.addUniswapLiquidityLink,
    addComethLiquidityLink: state => state.addComethLiquidityLink,
  }),
};
</script>