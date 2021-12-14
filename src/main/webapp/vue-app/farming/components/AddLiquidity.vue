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
  <v-card flat>
    <v-card-title class="ps-0">
      {{ $t('addLiquidity') }}
    </v-card-title>
    <v-card-text class="ps-0">
      {{ $t('addLiquidityIntroduction') }}
    </v-card-text>
    <v-bottom-navigation
      width="auto"
      height="auto"
      class="elevation-0 mt-4">
      <v-btn
        :href="addSushiswapLiquidityLink"
        target="_blank"
        rel="noreferrer"
        height="90px"
        width="90px"
        outlined
        text
        class="rounded-lg me-4">
        <span class="text-none">Sushiswap</span>
        <img src="/dapp/images/sushiswap.ico" class="my-3">
      </v-btn>
      <v-tooltip bottom>
        <template v-slot:activator="{ on, attrs }">
          <v-btn
            height="90px"
            width="90px"
            outlined
            text
            class="rounded-lg ms-4"
            v-bind="attrs"
            v-on="on">
            <span class="text-none">Uniswap</span>
            <div class="my-3 headline">🦄</div>
          </v-btn>
        </template>
        <span>{{ $t('comingSoon') }}</span>
      </v-tooltip>
    </v-bottom-navigation>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    addSushiswapLiquidityLink: 'https://app.sushi.com/add/ETH/0x8503a7b00B4b52692cC6c14e5b96F142E30547b7',
  }),
};
</script>