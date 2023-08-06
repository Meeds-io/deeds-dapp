<template>
  <div>
    <div v-if="address" class="d-flex align-center">
      <div>
        {{ $t('wom.deedManager') }}
      </div>
      <deeds-hub-address
        :address="address"
        clearable
        class="ms-auto"
        @clear="reset" />
    </div>
    <div v-else>
      <div v-sanitized-html="signMessageTitle" class="mb-4"></div>
      <deeds-hub-metamask-button
        :message="rawMessage"
        :disabled="!rawMessage"
        :address.sync="address"
        :signature.sync="signature" />
    </div>
  </div>
</template>
<script>
export default {
  props: {
    rawMessage: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    address: null,
    signature: null,
  }),
  computed: {
    signMessageTitle() {
      return this.$t('wom.signMessageTitle', {
        0: '<a href="https://www.meeds.io/marketplace" target="_blank">',
        1: '</a>',
      });
    },
  },
  watch: {
    address() {
      this.$emit('update:address', this.address);
    },
    signature() {
      this.$emit('update:signature', this.signature);
    },
  },
  methods: {
    reset() {
      this.address = null;
      this.signature = null;
    },
  },
};
</script>