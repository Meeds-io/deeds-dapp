<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 
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