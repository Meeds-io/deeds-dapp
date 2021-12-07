<template>
  <v-card flat>
    <h3 class="d-flex flex-nowrap">
      {{ $t('yourDeeds') }}
      <v-divider class="my-auto ms-4" />
    </h3>
    <v-card-text v-html="$t('yourDeedsIntroduction')" />
    <v-data-table
      :headers="headers"
      :items="nfts"
      :items-per-page="10"
      hide-default-header>
      <template v-slot:item.id="{item}">
        <v-btn
          :href="`${etherscanBaseLink}/token/${nftAddress}?a=${item.id}#inventory`"
          target="_blank"
          color="primary"
          text
          link>
          #{{ item.id }}
        </v-btn>
      </template>
      <template v-slot:item.actions="{item}">
        <v-menu offset-y>
          <template v-slot:activator="{ on, attrs }">
            <v-btn
              text
              icon
              v-bind="attrs"
              v-on="on">
              <v-icon>mdi-dots-vertical</v-icon>
            </v-btn>
          </template>
          <v-list dense>
            <v-list-item disabled>
              <v-list-item-title class="text-capitalize">{{ $t('claimRewards') }} ({{ $t('comingSoon') }})</v-list-item-title>
            </v-list-item>
            <v-list-item disabled>
              <v-list-item-title class="text-capitalize">{{ $t('moveIn') }} ({{ $t('comingSoon') }})</v-list-item-title>
            </v-list-item>
            <v-list-item disabled>
              <v-list-item-title class="text-capitalize">{{ $t('listRentalOffer') }} ({{ $t('comingSoon') }})</v-list-item-title>
            </v-list-item>
            <v-list-item>
              <v-list-item-title class="text-capitalize">
                <a :href="`${openSeaBaseLink}/${item.id}/sell`" target="_blank">
                  {{ $t('sellOnOpenSea') }}
                </a>
              </v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </template>
    </v-data-table>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    cities: ['Tanit', 'Reshef', 'Ashtarte', 'Melqart', 'Eshmun', 'Kushor', 'Hammon'],
    cardTypes: ['Common', 'Uncommon', 'Epic', 'Legendary'],
    headers: [
      {
        text: '',
        value: 'id',
      },
      {
        text: '',
        value: 'cityName',
      },
      {
        text: 'id',
        value: 'name',
      },
      {
        text: 'status',
        value: 'statusLabel',
      },
      {
        text: 'earnedReward',
        value: 'earnedRewardNoDecimals',
      },
      {
        text: 'actions',
        value: 'actions',
      },
    ],
  }),
  computed: Vuex.mapState({
    etherscanBaseLink: state => state.etherscanBaseLink,
    openSeaBaseLink: state => state.openSeaBaseLink,
    nftAddress: state => state.nftAddress,
    ownedNfts: state => state.ownedNfts,
    nfts() {
      return this.ownedNfts && this.ownedNfts.length && this.ownedNfts.slice().reverse().map(nft => {
        return Object.assign({
          cityName: this.cities[nft.cityIndex],
          statusLabel: this.$t('vacant'),
          earnedRewardNoDecimals: '0 MEED',
        }, nft);
      }) || [];
    },
  }),
};
</script>