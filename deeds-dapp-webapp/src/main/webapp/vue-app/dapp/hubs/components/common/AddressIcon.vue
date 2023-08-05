<template>
  <v-avatar size="18px">
    <svg ref="identicon" :data-jdenticon-value="address"></svg>
  </v-avatar>
</template>
<script>
export default {
  props: {
    address: {
      type: String,
      default: null,
    },
  },
  mounted() {
    window.jdenticon.updateSvg(this.$refs.identicon);
  },
};
</script>