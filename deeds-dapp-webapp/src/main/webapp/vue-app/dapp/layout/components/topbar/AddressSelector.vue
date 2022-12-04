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
  <div>
    <v-tooltip bottom>
      <template #activator="{ on, attrs }">
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
      <span>{{ address }}</span>
    </v-tooltip>
  </div>
</template>
<script>
export default {
  computed: Vuex.mapState({
    address: state => state.address,
    ens: state => state.ens,
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
    copyAddress() {
      this.$utils.copyToClipboard(this.address);
    },
  },
};
</script>