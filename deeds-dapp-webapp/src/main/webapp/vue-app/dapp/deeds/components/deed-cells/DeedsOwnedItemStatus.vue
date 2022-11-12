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
    v-if="started || starting || stopping"
    :href="deedTenantLink"
    target="_blank"
    rel="nofollow noreferrer noopener">
    <span v-if="starting" class="text-capitalize">{{ $t('tenantDeployTransactionInProgress') }}</span>
    <span v-else-if="stopping" class="text-capitalize">{{ $t('tenantUndeployTransactionInProgress') }}</span>
    <span v-else class="text-lowercase">{{ deedTenantLinkLabel }}</span>
  </a>
  <div v-else-if="stopped" class="text-capitalize">
    {{ $t('vacant') }}
  </div>
  <div v-else>-</div>
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
    cities: state => state.cities,
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
      return this.status === 'STOPPED';
    },
    started() {
      return this.status === 'STARTED';
    },
    starting() {
      return this.loading && this.nft.starting;
    },
    stopping() {
      return this.loading && this.nft.stopping;
    },
    cityIndex() {
      return this.nft?.cityIndex;
    },
    cityName() {
      return this.cities[this.cityIndex];
    },
    deedTenantLink() {
      return `https://${this.cityName.toLowerCase()}-${this.nftId}.meeds.io`;
    },
    deedTenantLinkLabel() {
      return `${this.cityName.toLowerCase()}-${this.nftId}.meeds.io`;
    },
  }),
};
</script>