<template>
  <v-list dense>
    <v-list-item>
      <h4>{{ $t('deeds') }}</h4>
    </v-list-item>
    <v-list-item
      v-for="nft in nftsByCardType"
      :key="nft.id"
      class="ps-8">
      <v-list-item-content>{{ nft.cardName }}</v-list-item-content>
      <v-list-item-content class="align-end">{{ nft.cityName }}</v-list-item-content>
      <v-list-item-content class="align-end">{{ nft.count }}</v-list-item-content>
      <v-list-item-content class="align-end" />
    </v-list-item>
  </v-list>
</template>
<script>
export default {
  data: () => ({
    cities: ['Tanit', 'Reshef', 'Ashtarte', 'Melqart', 'Eshmun', 'Kushor', 'Hammon'],
    cardTypes: ['Common', 'Uncommon', 'Epic', 'Legendary'],
  }),
  computed: Vuex.mapState({
    ownedNfts: state => state.ownedNfts,
    nftsByCardType() {
      const nftsByCardType = {};
      if (this.ownedNfts) {
        this.ownedNfts.forEach(nft => {
          if (!nftsByCardType[nft.cardType]) {
            nftsByCardType[nft.cardType] = {
              id: nft.cardType,
              cityName: this.cities[nft.cityIndex],
              cardName: this.cardTypes[parseInt(nft.cardType % 4)],
              count: 1,
            };
          } else {
            nftsByCardType[nft.cardType].count++;
          }
        });
      }
      return Object.values(nftsByCardType).sort((type1, type2) => type1.id - type2.id);
    },
  }),
};
</script>