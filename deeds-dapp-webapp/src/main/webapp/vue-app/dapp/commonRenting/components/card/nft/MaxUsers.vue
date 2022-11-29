<template>
  <v-list-item class="pa-0 my-n2">
    <v-list-item-content class="py-0 text-capitalize">
      {{ $t('cityName', {0: city}) }}
    </v-list-item-content>
    <v-list-item-action-text class="d-flex py-0">
      {{ maxUsersLabel }}
    </v-list-item-action-text>
  </v-list-item>
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
    maxUsersLabel() {
      switch (this.cardType){
      case 'COMMON': return this.$t('cardTypeMaxUsers', {0: 100});
      case 'UNCOMMON': return this.$t('cardTypeMaxUsers', {0: 1000});
      case 'RARE': return this.$t('cardTypeMaxUsers', {0: 10000});
      case 'LEGENDARY': return this.$t('unlimited');
      default: return '';
      }
    },
  }),
};
</script>