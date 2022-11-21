<template>
  <v-list-item class="pa-0 my-n2">
    <v-list-item-content class="py-0">
      {{ $t('deedCharacteristicsCityVotingRights') }}
    </v-list-item-content>
    <v-list-item-action-text class="py-0">
      {{ votingRights }}
    </v-list-item-action-text>
  </v-list-item>
</template>
<script>
export default {
  props: {
    cardType: {
      type: String,
      default: null,
    },
  },
  computed: Vuex.mapState({
    votingRights() {
      switch (this.cardType) {
      case 'COMMON': return 1;
      case 'UNCOMMON': return 10;
      case 'RARE': return 100;
      case 'LEGENDARY': return 1000;
      default: return '';
      }
    },
  }),
};
</script>