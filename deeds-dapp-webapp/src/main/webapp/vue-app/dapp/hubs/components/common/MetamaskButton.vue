<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <div class="d-flex border-box-sizing">
    <v-btn
      v-if="!isMetamaskInstalled"
      :href="metamaskInstallLinlk"
      target="_blank"
      rel="nofollow noreferrer noopener"
      class="mx-auto white-background primary-border-color d-block rounded-lg"
      outlined
      large>
      <v-img
        :src="`${parentLocation}/static/images/metamask.svg`"
        max-height="25px"
        max-width="25px" />
      <span class="py-2 ms-2 text-truncate text-none primary--text">{{ $t('installMetamaskButton') }}</span>
    </v-btn>
    <v-btn
      v-else
      :disabled="disabled"
      class="mx-auto white-background primary-border-color d-block rounded-lg"
      outlined
      large
      @click="signInWithMetamask()">
      <v-img
        :src="`${parentLocation}/static/images/metamask.svg`"
        max-height="25px"
        max-width="25px" />
      <span class="py-2 ms-2 text-truncate text-none primary--text">{{ $t('wom.signWithMetamask') }}</span>
    </v-btn>
  </div>
</template>
<script>
export default {
  props: {
    message: {
      type: String,
      default: null,
    },
    disabled: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    isMetamaskInstalled: false,
    currentSiteLink: window.location.host,
    address: null,
    signature: null,
  }),
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    isMobile() {
      return this.$vuetify.breakpoint.mobile;
    },
    metamaskInstallLinlk() {
      return this.isMobile
        && `https://metamask.app.link/dapp/${this.currentSiteLink}`
        || 'https://metamask.io/';
    },
  }),
  watch: {
    address() {
      this.$emit('update:address', this.address);
    },
    signature() {
      this.$emit('update:signature', this.signature);
    },
  },
  created() {
    this.isMetamaskInstalled = this.$ethUtils.isMetamaskInstalled();
  },
  methods: {
    signInWithMetamask() {
      return this.$ethUtils.signInWithMetamask(this.message, this.isMobile)
        .then(signature => this.signature = signature.replace('SIGNED_MESSAGE@', ''))
        .then(() => this.$ethUtils.retrieveAddress())
        .then(address => this.address = address)
        .catch(console.debug);// eslint-disable-line no-console
    },
  },
};
</script>