<template>
  <div>
    <v-btn
      v-if="expanded"
      :title="$t('wom.close')"
      :loading="loading"
      icon
      small
      class="ms-2"
      @click="expanded && $emit('collapse') || $emit('expand')">
      <v-icon size="16" class="icon-default-color">
        {{ expanded && 'fa-compress-alt' || 'fa-expand' }}
      </v-icon>
    </v-btn>
  </div>
</template>
<script>
export default {
  props: {
    report: {
      type: Object,
      default: null,
    },
    loading: {
      type: Boolean,
      default: false,
    },
    expanded: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    menu: false,
  }),
  computed: {
    hub() {
      return this.$root.hub;
    },
    connectedToWoM() {
      return !!this.hub;
    },
    status() {
      return this.report?.status;
    },
    isErrorSending() {
      return this.status === 'ERROR_SENDING';
    },
    sendButtonTitle() {
      return this.isErrorSending && this.$t('wom.resend') || this.$t('wom.send');
    },
  },
};
</script>