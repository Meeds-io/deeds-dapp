<template>
  <v-tooltip
    z-index="4"
    bottom>
    <template #activator="{ on, attrs }">
      <v-card
        class="mx-auto mt-2 mt-md-0"
        max-width="300px"
        color="transparent"
        flat
        v-bind="attrs"
        v-on="on">
        <v-btn
          :disabled="!claimableRewards"
          color="primary"
          elevation="0"
          @click="claim">
          <span class="text-none">
            {{ $t('claimRewards') }}
          </span>
        </v-btn>
      </v-card>
    </template>
    <span>{{ claimableRewards && $t('claimYourRewards') || $t('noRewardsToClaim') }}</span>
  </v-tooltip>
</template>
<script>
export default {
  data: () => ({
    loading: false,
    claimableRewards: 0,
    claimAbi: [
      'function claim(address _receiver, uint256 _amount)',
    ],
  }),
  computed: Vuex.mapState({
    address: state => state.address,
    polygonNetwork: state => state.polygonNetwork,
    networkId: state => state.networkId,
    uemAddress: state => state.uemAddress,
    ethereumChainId: state => state.chainId,
    polygonChainId() {
      return this.polygonNetwork?.chainId;
    },
  }),
  watch: {
    loading() {
      if (this.loading) {
        this.$store.commit('pauseNetworkChangeListening', true);
      } else {
        this.$ethUtils.switchMetamaskNetwork(this.ethereumChainId)
          .finally(() => this.$store.commit('pauseNetworkChangeListening', false));
      }
    },
  },
  created() {
    this.retrieveClaimableAmount();
  },
  methods: {
    retrieveClaimableAmount() {
      this.$hubReportService.getClaimableRewards(this.address)
        .then(claimableRewards => this.claimableRewards = Number(claimableRewards));
    },
    async claim() {
      this.loading = true;
      try {
        await this.switchToPolygonNetwork();
        await this.sendTransaction();
        await this.retrieveClaimableAmount();
      } finally {
        this.loading = false;
      }
    },
    async switchToPolygonNetwork() {
      try {
        await window.ethereum.request({
          method: 'wallet_switchEthereumChain',
          params: [{ chainId: this.polygonChainId }],
        });
      } catch (error) {
        console.error(error);
        await window.ethereum?.request({
          method: 'wallet_addEthereumChain',
          params: [this.polygonNetwork],
        });
      }
    },
    sendTransaction() {
      const provider = new window.ethers.providers.Web3Provider(window.ethereum);
      return this.$ethUtils.sendTransaction(
        provider,
        new window.ethers.Contract(
          this.uemAddress,
          this.claimAbi,
          provider
        ),
        'claim(address,uint256)',
        {gasLimit: '300000'},
        [this.address, 0]
      )
        .then(receipt => {
          if (receipt?.wait) {
            return receipt.wait(1);
          } else {
            throw new Error('uem.errorClaimingRewards');
          }
        })
        .then(receipt => {
          if (receipt?.status) {
            this.$root.$emit('alert-message', this.$t('uem.claimedRewardsSuccessfully'), 'success');
            this.$root.$emit('uem-claim-success');
            this.loading = false;
          } else {
            throw new Error('uem.errorClaimingRewards');
          }
        })
        .catch(e => {
          console.error(e);
          const error = (e?.data?.message || e?.message || e?.cause || String(e));
          let errorMessageKey = error.includes('wom.') && `wom.${error.split('wom.')[1].split(/[^A-Za-z0-9]/g)[0]}` || error;
          if (!this.$te(errorMessageKey)) {
            errorMessageKey = 'wom.errorConnectingToWom';
          }
          this.$root.$emit('alert-message', this.$t(errorMessageKey), 'error');
          throw e;
        });
    },
  }
};
</script>