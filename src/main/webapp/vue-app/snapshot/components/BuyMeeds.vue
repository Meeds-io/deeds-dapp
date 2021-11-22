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
import { ChainId, Token, TokenAmount, Pair } from '@sushiswap/sdk';

export default {
  data: () => ({
    computingAmount: false,
    buy: true,
    fromValue: null,
    toValue: null,
    meedsToken: new Token(
      ChainId.MAINNET,
      '0x8503a7b00b4b52692cc6c14e5b96f142e30547b7',
      18,
      'MEED',
      'Meeds Token'
    ),
    wethToken: new Token(
      ChainId.MAINNET,
      '0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2',
      18,
      'WETH',
      'Wrapped Ether'
    ),
    startSearchAfterInMilliseconds: 600,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    typing: false,
  }),
  computed: Vuex.mapState({
    pairHistoryData: state => state.pairHistoryData,
    todayDate() {
      return this.dateFormat(Date.now());
    },
    meedsReserve() {
      const meedsReserve = this.pairHistoryData && this.pairHistoryData[this.todayDate] && this.pairHistoryData[this.todayDate].meedsReserve;
      return this.toDecimal(meedsReserve, 18);
    },
    wethReserve() {
      const wethReserve = this.pairHistoryData && this.pairHistoryData[this.todayDate] && this.pairHistoryData[this.todayDate].wethReserve;
      return this.toDecimal(wethReserve, 18);
    },
    pairToken() {
      return new Pair(
        new TokenAmount(this.meedsToken, this.meedsReserve),
        new TokenAmount(this.wethToken, this.wethReserve)
      );
    },
    fromToken() {
      return this.buy && this.meedsToken || this.wethToken;
    },
    buttonLabel() {
      return this.buy && this.$t('buyMeeds') || this.$t('sellMeeds');
    },
    disabledButton() {
      return !this.toValue;
    },
  }),
  watch: {
    fromValue() {
      if (!this.fromValue) {
        this.computeValue();
        return;
      }
      this.startTypingKeywordTimeout = Date.now() + this.startSearchAfterInMilliseconds;
      if (!this.typing) {
        this.typing = true;
        this.waitForEndTyping();
      }
    },
  },
  created() {
    window.setTimeout(this.initBot, 100);
  },
  methods: {
    switchInputs() {
      this.buy = !this.buy;
      this.computeValue();
    },
    computeValue() {
      if (!this.fromValue) {
        this.toValue = null;
      }
      this.computingAmount = true;
      try {
        const result = this.pairToken.getOutputAmount(new TokenAmount(this.fromToken, this.toDecimal(this.fromValue, 18)));
        this.toValue = result && result.length && result[0] && result[0].toSignificant && result[0].toSignificant() || null;
      } finally {
        this.computingAmount = false;
      }
    },
    waitForEndTyping() {
      window.setTimeout(() => {
        if (Date.now() > this.startTypingKeywordTimeout) {
          this.computeValue();
          this.typing = false;
        } else {
          this.waitForEndTyping();
        }
      }, this.endTypingKeywordTimeout);
    },
    toDecimal(amount, decimals) {
      return new BigNumber(amount).multipliedBy(new BigNumber(10).pow(decimals)).toFixed(0);
    },
    dateFormat(timestamp) {
      const value = parseInt(timestamp && timestamp.value || timestamp);
      return new Date(value).toISOString().substring(0, 10);
    },
  },
};
</script>

