<template>
  <v-list-item-avatar
    class="deed-avatar mx-2 mb-auto"
    height="70"
    min-width="70"
    width="70">
    <v-img :src="cardImage" />
  </v-list-item-avatar>
</template>
<script>
export default {
  props: {
    city: {
      type: String,
      default: null,
    },
    cardType: {
      type: String,
      default: null,
    },
  },
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    cardImage() {
      return this.city && this.cardType && `/${this.parentLocation}/static/images/nft/${this.city.toLowerCase()}-${this.cardType.toLowerCase()}.png`;
    },
  }),
};
</script>