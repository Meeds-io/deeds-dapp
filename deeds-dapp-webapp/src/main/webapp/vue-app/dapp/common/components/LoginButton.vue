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
  <v-btn
    v-if="authenticated"
    :min-width="minButtonsWidth"
    text
    outlined
    v-bind="attrs"
    v-on="on"
    @click="logout(false)">
    {{ $t('signOut') }}
  </v-btn>
  <v-tooltip
    v-else
    top
    left>
    <template #activator="{ on, attrs }">
      <div
        class="full-width d-flex"
        v-bind="attrs"
        v-on="on">
        <v-btn
          :min-width="minButtonsWidth"
          class="ms-auto"
          color="primary"
          depressed
          dark
          @click="login()">
          {{ $t('signIn') }}
        </v-btn>
      </div>
    </template>
    <span>{{ loginTooltip }}</span>
  </v-tooltip>
</template>
<script>
export default {
  props: {
    loginTooltip: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    minButtonsWidth: 120,
  }),
  computed: Vuex.mapState({
    authenticated: state => state.authenticated,
    provider: state => state.provider,
    address: state => state.address,
  }),
  created() {
    this.$root.$on('deed-dapp-login', this.login);
    this.$root.$on('deed-dapp-logout', this.logout);
  },
  methods: {
    logout(avoidRefreshInfo) {
      return this.$authentication.logout(this.address)
        .finally(() => this.$store.commit('refreshAuthentication'))
        .then(() => {
          if (!avoidRefreshInfo) {
            return this.$emit('logged-out');
          }
        });
    },
    login() {
      const token = document.querySelector('[name=loginMessage]').value;
      const message = this.$t('signMessage', {0: token}).replace(/\\n/g, '\n');
      this.$root.$emit('close-alert-message');
      return this.$ethUtils.signMessage(this.provider, this.address, message)
        .then(signedMessage => this.$authentication.login(this.address, message, signedMessage))
        .finally(() => this.$store.commit('refreshAuthentication'))
        .then(() => {
          if (this.authenticated) {
            this.$emit('logged-in');
          }
        });
    },
  },
};
</script>