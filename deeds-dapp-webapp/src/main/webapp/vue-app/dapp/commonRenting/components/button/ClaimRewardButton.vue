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
          :loading="loading"
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
    <span>{{ claimableRewards && $t('claimYourRewards', {0: claimableRewardsFormatted}) || $t('noRewardsToClaim') }}</span>
  </v-tooltip>
</template>
<script>
export default {
  data: () => ({
    loading: false,
    ethereumChainId: null,
    polygonChainId: null,
    polygonBlockExplorer: null,
    uemAddress: null,
    claimableRewards: 0,
    claimAbi: [
      'function claim(address _receiver, uint256 _amount)',
    ],
  }),
  computed: Vuex.mapState({
    address: state => state.address,
    language: state => state.language,
    polygonNetwork: state => state.polygonNetwork,
    networkId: state => state.networkId,
    chainId: state => state.chainId,
    claimableRewardsFormatted() {
      return this.$utils.numberFormatWithDigits(this.claimableRewards, this.language);
    },
  }),
  watch: {
    loading() {
      this.$store.commit('pauseNetworkChangeListening', this.loading);
    },
  },
  created() {
    this.retrieveClaimableAmount();
  },
  methods: {
    retrieveClaimableAmount() {
      this.$hubReportService.getClaimableRewards(this.address)
        .then(data => {
          this.uemAddress = data?.contract;
          this.claimableRewards = Number(data?.claimable || 0);
        });
    },
    async claim() {
      this.loading = true;
      try {
        this.ethereumChainId = this.ethereumChainId || this.chainId;
        this.polygonChainId = this.polygonChainId || this.polygonNetwork?.chainId;
        this.polygonBlockExplorer = this.polygonBlockExplorer || this.polygonNetwork?.blockExplorerUrls?.[0];

        await this.switchToPolygonNetwork();
        // Start: Ensure Metamask changed the network
        await new Promise(resolve => setTimeout(resolve, 1000));
        await this.$ethUtils.getSelectedChainId();
        // End: Ensure Metamask changed the network
        await this.sendTransaction();
        await this.retrieveClaimableAmount();
        this.$root.$emit('alert-message', this.$t('uem.claimedRewardsSuccessfully'), 'success');
        this.$root.$emit('uem-claim-success');
      } catch (e) {
        if (e?.code === 4001) {
          this.$root.$emit('close-alert-message');
        } else if (e?.message) {
          this.$root.$emit('alert-message-html', this.$t('uem.errorClaimingRewardsWithMetamask', {
            0: '<span class="error--text font-italic">',
            1: this.$utils.htmlToText(e.message),
            2: '</span>',
          }), 'error');
        } else {
          this.$root.$emit('alert-message', this.$t('uem.errorClaimingRewards'), 'error');
        }
      } finally {
        this.loading = false;
      }
    },
    async switchToPolygonNetwork() {
      try {
        this.$root.$emit('alert-message', this.$t('uem.swtichToPolygon'), 'info');
        await window.ethereum.request({
          method: 'wallet_switchEthereumChain',
          params: [{ chainId: this.polygonChainId }],
        });
      } catch (e) {
        if (e?.code === 4902) {
          this.$root.$emit('alert-message', this.$t('uem.addPolygonNetwork'), 'info');
          await window.ethereum?.request({
            method: 'wallet_addEthereumChain',
            params: [this.polygonNetwork],
          });
        } else {
          throw e;
        }
      }
    },
    sendTransaction() {
      this.$root.$emit('alert-message-html', this.$t('uem.confirmClaimTransaction', {
        0: this.claimableRewardsFormatted,
        1: '<span class="secondary--text">',
        2: '</span>',
      }));
      const provider = new window.ethers.providers.Web3Provider(window.ethereum);
      return this.$ethUtils.sentTransactionWithNotification(
        provider,
        new window.ethers.Contract(
          this.uemAddress,
          this.claimAbi,
          provider
        ),
        'claim(address,uint256)',
        {gasLimit: '300000'},
        [this.address, 0],
        false
      )
        .then(receipt => {
          if (receipt?.hash) {
            this.$root.$emit('alert-message', this.$t('transactionSent'), 'success', null, 'fas fa-up-right-from-square', this.$t('viewOnEtherscan'), `${this.polygonBlockExplorer}/tx/${receipt.hash}`);
          }
          if (receipt?.wait) {
            return receipt.wait(2);
          } else {
            throw new Error('uem.errorClaimingRewards');
          }
        })
        .then(receipt => {
          if (!receipt?.status) {
            throw new Error('uem.errorClaimingRewards');
          }
        });
    },
  }
};
</script>