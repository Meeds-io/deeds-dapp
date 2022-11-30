<template>
  <v-list-item class="pa-0 my-n2">
    <v-list-item-content class="py-0">
      {{ $t('deedMintingPower') }}
    </v-list-item-content>
    <v-list-item-action-text class="d-flex py-0">
      <v-tooltip
        z-index="4"
        max-width="300px"
        bottom>
        <template #activator="{ on, attrs }">
          <v-progress-circular
            :rotate="-90"
            :size="30"
            :width="5"
            :value="mintingPowerPercentage"
            color="success"
            v-bind="attrs"
            v-on="on">
            <small class="primary--text">{{ mintingPower }}</small>
          </v-progress-circular>
        </template>
        <span>{{ $t('deedMintingPowerDetails', {0: mintingPower}) }}</span>
      </v-tooltip>
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
    mintingPower() {
      switch (this.cardType){
      case 'COMMON': return 1;
      case 'UNCOMMON': return 1.1;
      case 'RARE': return 1.3;
      case 'LEGENDARY': return 2;
      default: return '';
      }
    },
    mintingPowerPercentage() {
      return this.mintingPower && parseInt((this.mintingPower - 1) * 100);
    },
  }),
};
</script>