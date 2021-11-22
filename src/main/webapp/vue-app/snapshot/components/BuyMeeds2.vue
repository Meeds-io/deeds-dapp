<template>
  <v-card
    class="mx-auto"
    width="240"
    height="200"
    flat>
    <v-card-title class="justify-center pb-2">
      {{ $t('buyOrSell') }}
    </v-card-title>
    <div class="elevation-2">
      <v-card-text class="d-flex flex-column">
        <v-text-field
          v-model="fromValue"
          :loading="!!computingAmount || typing"
          placeholder="0.0"
          hide-details
          large
          outlined
          dense>
          <template #append>
            <div class="mt-1">
              {{ buy && 'ETH' || 'MEED' }}
            </div>
          </template>
        </v-text-field>
        <v-btn
          icon
          class="mx-auto my-3"
          @click="switchInputs"
          x-large>
          <v-icon size="48">mdi-autorenew</v-icon>
        </v-btn>
        <v-text-field
          v-model="toValue"
          placeholder="0.0"
          hide-details
          large
          outlined
          dense
          disabled
          filled>
          <template #append>
            <div class="mt-1">
              {{ buy && 'MEED' || 'ETH' }}
            </div>
          </template>
        </v-text-field>
        <v-card-actions>
          <v-btn :disabled="disabledButton" class="mx-auto mt-4">
            <span class="text-capitalize">
              {{ buttonLabel }}
            </span>
          </v-btn>
        </v-card-actions>
      </v-card-text>
    </div>
  </v-card>
</template>

<script>
import UniswapV2PairAbi from '../../../json/UniswapV2Pair.abi.json';

export default {
  data: () => ({
    pairAddress: '0x960Bd61D0b960B107fF5309A2DCceD4705567070',
    sushiEthMeedPair: null,
    meedsPrice: 0,
    buy: true,
    fromValue: null,
    toValue: null,
  }),
  computed: {
    fromToken() {
      return this.buy && this.meedsToken || this.wethToken;
    },
    buttonLabel() {
      return this.buy && this.$t('buyMeeds') || this.$t('sellMeeds');
    },
    disabledButton() {
      return !this.toValue;
    },
  },
  watch: {
    fromValue() {
      this.computeValue();
    },
  },
  created() {
    this.init();
  },
  methods: {
    switchInputs() {
      this.buy = !this.buy;
      this.computeValue();
    },
    computeValue() {
      if (!this.fromValue) {
        this.toValue = null;
      } else if (this.buy) {
        this.toValue = Number(this.fromValue) / this.meedsPrice;
      } else {
        this.toValue = this.meedsPrice * Number(this.fromValue);
      }
    },
    dateFormat(timestamp) {
      const value = parseInt(timestamp && timestamp.value || timestamp);
      return new Date(value).toISOString().substring(0, 10);
    },
    init() {
      if (!this.sushiEthMeedPair) {
        this.sushiEthMeedPair = new ethers.Contract(
          this.pairAddress,
          UniswapV2PairAbi,
          new ethers.providers.Web3Provider(window.ethereum),
        );
      }
      return this.sushiEthMeedPair.getReserves().then(sushiReserves => {
        const reserveMeed = Number(ethers.utils.formatUnits(sushiReserves[0], 18));
        const reserveWeth = Number(ethers.utils.formatUnits(sushiReserves[1], 18));
        this.meedsPrice = reserveWeth / reserveMeed;
        console.log('reserveMeed', reserveMeed);
        console.log('reserveWeth', reserveWeth);
      });
    },
  },
};
</script>

