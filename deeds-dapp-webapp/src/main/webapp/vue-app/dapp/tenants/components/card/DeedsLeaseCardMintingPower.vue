<template>
  <v-list-item class="pa-0 my-n2">
    <v-list-item-content class="py-0">
      {{ $t('deedMintingPower') }}
    </v-list-item-content>
    <v-list-item-action-text class="d-flex py-0">
      <v-tooltip bottom>
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
    mintingPower: {
      type: Number,
      default: null,
    },
  },
  computed: Vuex.mapState({
    mintingPowerPercentage() {
      return this.mintingPower && parseInt((this.mintingPower - 1) * 100);
    },
  }),
};
</script>