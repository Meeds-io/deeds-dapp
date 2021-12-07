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
  <div class="liquidityActions d-flex flex-nowrap">
    <v-btn
      :href="addLiquidityLink"
      link
      text
      target="_blank"
      color="primary"
      small>
      <span class="text-capitalize">
        {{ $t('addLiquidity') }}
      </span>
      <v-icon class="ms-2">mdi-open-in-new</v-icon>
    </v-btn>
  </div>
</template>

<script>
export default {
  data: () => ({
    sellLiquidityLink: 'https://app.sushi.com/swap?inputCurrency=0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2&outputCurrency=0x8503a7b00B4b52692cC6c14e5b96F142E30547b7',
    addLiquidityLink: 'https://app.sushi.com/add/0x8503a7b00b4b52692cc6c14e5b96f142e30547b7/0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2',
  }),
};
</script>