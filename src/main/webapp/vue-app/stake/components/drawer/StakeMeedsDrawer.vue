<template>
  <deeds-drawer ref="drawer">
    <template #title>
      <h4>{{ title }}</h4>
    </template>
    <template #content>
      <deeds-stake-meeds-steps v-if="stake" />
      <deeds-stake-meeds-step-unstake v-else />
    </template>
  </deeds-drawer>
</template>

<script>
export default {
  props: {
    stake: {
      type: Boolean,
      default: () => false,
    },
  },
  computed: Vuex.mapState({
    title() {
      return this.stake && this.$t('stakeMeeds') || this.$t('unstakeMeeds');
    },
  }),
  methods: {
    open() {
      this.$refs.drawer.open();
    },
  },
};
</script>