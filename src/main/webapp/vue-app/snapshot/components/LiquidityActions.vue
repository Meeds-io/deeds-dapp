<template>
  <div class="liquidityActions">
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
    <v-btn
      :href="sellLiquidityLink"
      link
      text
      target="_blank"
      color="primary"
      small>
      <span class="text-capitalize">
        {{ $t('sellLiquidity') }}
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