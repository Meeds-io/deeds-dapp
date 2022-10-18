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
  <a
    v-if="transactionHash"
    :href="`${etherscanBaseLink}/tx/${transactionHash}`"
    target="_blank"
    rel="nofollow noreferrer noopener"
    class="px-0 px-sm-3 d-inline d-sm-flex link--color">
    {{ $t('transactionSent') }}
  </a>
  <a
    v-else-if="started"
    :href="nft.link"
    target="_blank"
    rel="nofollow noreferrer noopener">
    {{ nft.linkLabel }}
  </a>
  <div v-else-if="stopped" class="text-capitalize">
    {{ $t('vacant') }}
  </div>
  <div v-else></div>
</template>
<script>
export default {
  props: {
    nft: {
      type: Object,
      default: null,
    },
  },
  computed: Vuex.mapState({
    etherscanBaseLink: state => state.etherscanBaseLink,
    nftId() {
      return this.nft?.id;
    },
    status() {
      return this.nft?.status;
    },
    transactionHash() {
      return this.nft?.transactionHash;
    },
    loading() {
      return this.transactionHash || !this.status || this.status === 'loading';
    },
    stopped() {
      return !this.transactionHash && this.status === 'STOPPED';
    },
    started() {
      return !this.transactionHash && this.status === 'STARTED';
    },
  }),
};
</script>