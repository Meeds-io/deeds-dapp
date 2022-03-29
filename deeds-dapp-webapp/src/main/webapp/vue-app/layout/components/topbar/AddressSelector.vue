<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 
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
  <div>
    <input
      ref="clipboardInput"
      v-model="address"
      id="copyToClipboardInput"
      name="copyToClipboardInput"
      class="copyToClipboardInput"
      type="text">
    <v-tooltip bottom>
      <template v-slot:activator="{ on, attrs }">
        <v-btn
          name="copyAddressButton"
          outlined
          text
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
    ens: state => state.ens,
    title() {
      return this.copied && this.$t('copied') || this.address;
    },
    addressPart() {
      if (this.ens) {
        return this.ens;
      } else if (this.address) {
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