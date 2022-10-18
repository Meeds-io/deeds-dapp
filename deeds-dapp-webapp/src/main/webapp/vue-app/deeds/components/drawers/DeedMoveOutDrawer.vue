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
  <deeds-drawer
    ref="drawer"
    v-model="drawer"
    second-level
    @opened="$emit('opened')"
    @closed="$emit('closed')">
    <template #title>
      <h4>{{ $t('removeTenantTitle') }}</h4>
    </template>
    <template #content>
      <v-card flat>
        <v-card-text v-if="transactionHash">
          <v-list-item class="px-0">
            <v-list-item-avatar size="72">
              <v-img :src="`/${parentLocation}/static/images/transactionInProgress.png`" eager />
            </v-list-item-avatar>
            <v-list-item-content class="d-inline">
              {{ $t('removeTenantSentDescription') }}
              <a
                :href="transactionLink"
                target="_blank"
                rel="nofollow noreferrer noopener">
                {{ transactionHashAlias }}
              </a>
            </v-list-item-content>
          </v-list-item>
        </v-card-text>
        <v-card-text v-else>
          <v-list-item class="px-0">
            <v-list-item-icon size="72">
              <v-icon size="72" color="warning">mdi-alert-remove</v-icon>
            </v-list-item-icon>
            <v-list-item-content class="d-inline">
              {{ $t('removeTenantDescription') }}
            </v-list-item-content>
          </v-list-item>
        </v-card-text>
      </v-card>
    </template>
    <template v-if="!transactionHash" #footer>
      <v-btn
        outlined
        text
        class="ms-auto me-2"
        name="cancelMoveIn"
        min-width="120"
        @click="close(nftId)">
        <span class="text-capitalize">
          {{ $t('cancel') }}
        </span>
      </v-btn>
      <deeds-move-out-button
        v-model="transactionHash"
        :nft-id="nftId"
        label="removeTenantButton"
        primary
        @sending="$refs.drawer.startLoading()"
        @end-sending="$refs.drawer.endLoading()" />
    </template>
  </deeds-drawer>
</template>
<script>
export default {
  data: () => ({
    nftId: 0,
    drawer: false,
    transactionHash: null,
  }),
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    etherscanBaseLink: state => state.etherscanBaseLink,
    transactionHashAlias() {
      return this.transactionHash && `${this.transactionHash.substring(0, 5)}...${this.transactionHash.substring(this.transactionHash.length - 3)}`;
    },
    transactionLink() {
      return `${this.etherscanBaseLink}/tx/${this.transactionHash}`;
    },
  }),
  created() {
    this.$root.$on('deeds-move-out-drawer', this.open);
    this.$root.$on('deeds-move-drawer-close', this.close);
  },
  methods: {
    open(nftId) {
      this.nftId = nftId;
      this.transactionHash = null;
      this.$nextTick()
        .then(() => {
          if (this.$refs.drawer) {
            this.$refs.drawer.open();
          }
        });
    },
    close(nftId) {
      if (nftId === this.nftId) {
        this.$refs.drawer.close();
      }
    },
  },
};
</script>