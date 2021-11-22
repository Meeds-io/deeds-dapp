<template>
  <div>
    <input
      ref="clipboardInput"
      v-model="address"
      class="copyToClipboardInput"
      type="text">
    <v-tooltip bottom>
      <template v-slot:activator="{ on, attrs }">
        <v-btn
          :ripple="false"
          elevation="1"
          active-class="none"
          color="grey lighten-5"
          class="px-2"
          v-bind="attrs"
          v-on="on"
          @click="copyAddress(on)">
          <div>{{ addressPart }}</div>
        </v-btn>
      </template>
      <span>{{ title }}</span>
    </v-tooltip>
  </div>
</template>
<script>
export default {
  data: () => ({
    copied: false,
  }),
  computed: Vuex.mapState({
    address: state => state.address,
    title() {
      return this.copied && this.$t('copied') || this.address;
    },
    addressPart() {
      if (this.address) {
        return `${this.address.substring(0, 10)}...${this.address.substring(39)}`;
      }
      return null;
    },
  }),
  methods: {
    copyAddress(event) {
      this.copyToClipboard();
      if (event && event.focus) {
        event.blur();
        window.setTimeout(() => event.focus(), 100);
        window.setTimeout(() => event.blur(), 1000);
      }
      window.setTimeout(() => {
        this.copied = true;
      }, 50);
      window.setTimeout(() => this.copied = true, 50);
      window.setTimeout(() => this.copied = false, 1100);
    },
    copyToClipboard() {
      this.$refs.clipboardInput.select();
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