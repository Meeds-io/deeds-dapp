<template>
  <v-menu
    v-model="menu"
    :close-on-content-click="false"
    :nudge-width="200"
    close-delay="500"
    open-on-hover
    offset-x>
    <template #activator="{ on, attrs }">
      <span class="d-flex-inline position-relative pe-8">
        <span v-if="label">{{ label }}</span>
        <v-btn
          v-if="!metamaskOffline"
          :style="buttonTop && {top: `${buttonTop}px`}"
          absolute
          icon
          x-small
          class="ms-1"
          v-bind="attrs"
          v-on="on">
          <v-icon small>mdi-information-outline</v-icon>
        </v-btn>
      </span>
    </template>
    <v-card v-if="!metamaskOffline">
      <v-list-item class="px-2">
        <v-list-item-avatar class="elevation-1">
          <svg
            ref="avatar"
            :data-jdenticon-value="address"></svg>
        </v-list-item-avatar>
        <v-list-item-content>
          <v-list-item-title>{{ addressAlias }}</v-list-item-title>
        </v-list-item-content>
        <v-list-item-action class="d-flex flex-row">
          <v-btn
            :title="$t('viewOnEtherscan')"
            :href="etherscanLink"
            target="_blank"
            rel="nofollow noreferrer noopener"
            icon
            class="me-3 rounded-lg border-color">
            <v-icon>mdi-link</v-icon>
          </v-btn>
          <v-btn
            :title="$t('copyAddress')"
            icon
            class="me-3 rounded-lg border-color"
            @click="copyToClipboard">
            <v-icon>mdi-content-copy</v-icon>
          </v-btn>
        </v-list-item-action>
      </v-list-item>
    </v-card>
  </v-menu>
</template>

<script>
export default {
  props: {
    label: {
      type: String,
      default: null,
    },
    address: {
      type: String,
      default: null,
    },
    token: {
      type: Boolean,
      default: false,
    },
    buttonTop: {
      type: Number,
      default: 0,
    },
  },
  data: () => ({
    menu: false,
  }),
  computed: Vuex.mapState({
    etherscanBaseLink: state => state.etherscanBaseLink,
    metamaskOffline: state => state.metamaskOffline,
    etherscanLink() {
      return this.token && `${this.etherscanBaseLink}token/${this.address}` || `${this.etherscanBaseLink}address/${this.address}`;
    },
    addressAlias() {
      return this.address && `${this.address.substring(0, 10)}...${this.address.substring(this.address.length - 10)}`;
    },
  }),
  watch: {
    menu() {
      if (this.menu) {
        this.$nextTick().then(() => {
          if (this.$refs.avatar) {
            jdenticon.updateSvg(this.$refs.avatar);
          }
        });
      }
    },
  },
  methods: {
    copyToClipboard() {
      const copyToClipboardInput = document.getElementById('copyToClipboardInput');
      copyToClipboardInput.value = this.address;
      copyToClipboardInput.select();
      if (document.execCommand) {
        try {
          document.execCommand('copy');
        } catch (e) {
          console.error('Error executing document.execCommand', e);
        }
      }
    },
  },
};
</script>