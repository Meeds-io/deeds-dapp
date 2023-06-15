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
    nudge-left="110"
    content-class="my-menu"
    transition="slide-x-transition"
    offset-y
    open-on-hover>
    <template #activator="{ on, attrs }">
      <div 
        v-bind="attrs"
        v-on="on">
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
      </div>
    </template>
    <v-list>
      <v-list-item :href="portfolioURL">
        <v-list-item-avatar
          height="25px"
          width="23px"
          min-width="23px"
          tile>
          <v-img :src="`${parentLocation}/static/images/portfolio_menu.webp`" />
        </v-list-item-avatar>
        <v-hover v-slot="{hover}">  
          <v-list-item-content>
            <v-list-item-title class="d-flex">
              <span class="font-weight-black"> {{ $t('page.overview') }} </span>
              <v-icon
                v-if="hover"
                class="ms-2 black--text text--color"
                size="10">
                fa fa-chevron-right
              </v-icon>
            </v-list-item-title>
            <v-list-item-subtitle>
              <span  
                :class="hover && 'black--text text--color'"
                class="text-body-2"> 
                {{ $t('page.portfolio.menu.description') }}
              </span>
            </v-list-item-subtitle>
          </v-list-item-content>
        </v-hover>  
      </v-list-item>
    </v-list>
  </v-menu>
</template>
<script>
export default {
  computed: Vuex.mapState({
    address: state => state.address,
    ens: state => state.ens,
    parentLocation: state => state.parentLocation,
    portfolioURL: state => state.portfolioURL,
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