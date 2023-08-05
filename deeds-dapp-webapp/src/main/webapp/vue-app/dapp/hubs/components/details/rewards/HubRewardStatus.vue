<template>
  <div v-if="statusTitle">
    <v-menu
      v-model="menu"
      :close-on-content-click="false"
      max-width="300"
      open-on-hover
      attach
      offset-y>
      <template #activator="{ on, attrs }">
        <v-btn
          :color="color"
          outlined
          icon
          small
          class="border-color my-n1"
          v-bind="attrs"
          v-on="on">
          <v-icon
            :color="color"
            :size="iconSize">
            {{ icon }}
          </v-icon>
        </v-btn>
      </template>
      <v-card class="d-flex flex-column pa-4">
        <div v-sanitized-html="statusTitle"></div>
      </v-card>
    </v-menu>
  </div>
</template>
<script>
export default {
  props: {
    report: {
      type: Object,
      default: null,
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
    hubJoinDate() {
      return this.hub?.createdDate && new Date(this.hub?.createdDate);
    },
    reportEndDate() {
      return this.report?.hubRewardReport?.toDate && new Date(this.report?.hubRewardReport?.toDate);
    },
    outdatedReport() {
      return this.hubJoinDate && this.reportEndDate && this.hubJoinDate.getTime() > this.reportEndDate.getTime();
    },
    enableMoreActions() {
      return this.isErrorSending;
    },
    icon() {
      switch (this.status) {
      case 'INVALID': return 'fa-info-circle';
      case 'SENT': return 'fa-hourglass-start';
      case 'ERROR_SENDING': return 'fa-exclamation-triangle';
      case 'PENDING_REWARD': return 'fa-hourglass-half';
      case 'REWARDED': return 'fa-check-circle';
      case 'REJECTED': return 'fa-exclamation-circle';
      default: return '';
      }
    },
    iconSize() {
      switch (this.status) {
      case 'ERROR_SENDING': return 14;
      default: return 16;
      }
    },
    color() {
      switch (this.status) {
      case 'INVALID': return 'grey darken-2';
      case 'SENT': return 'blue';
      case 'PENDING_REWARD': return 'green darken-2';
      case 'REWARDED': return 'green';
      case 'REJECTED': return 'red';
      default: return '';
      }
    },
    statusTitle() {
      switch (this.status) {
      case 'INVALID': return this.$t('wom.invalidReport');
      case 'SENT': return this.$t('wom.sentReport');
      case 'PENDING_REWARD': return this.$t('wom.pendingWoMRewardTransaction');
      case 'REWARDED': return this.$t('wom.hubRewardSent');
      case 'REJECTED': return this.$t('wom.hubRewardRejected');
      default: return null;
      }
    },
  },
};
</script>