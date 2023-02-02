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
  <deeds-drawer ref="drawer">
    <template #title>
      <h4 class="title font-weight-bold">{{ title }}</h4>
    </template>
    <template #content>
      <deeds-stake-liquidity-steps
        v-if="stake"
        :lp-address="lpAddress"
        :lp-symbol="lpSymbol"
        :lp-balance="lpBalance"
        :lp-allowance="lpAllowance"
        :lp-staked="lpStaked"
        :lp-contract="lpContract"
        @start-loading="$refs.drawer.startLoading()"
        @end-loading="$refs.drawer.endLoading()" />
      <deeds-stake-liquidity-step-unstake
        v-else
        :lp-address="lpAddress"
        :lp-symbol="lpSymbol"
        :lp-staked="lpStaked" />
    </template>
  </deeds-drawer>
</template>

<script>
export default {
  props: {
    lpAddress: {
      type: String,
      default: null,
    },
    lpSymbol: {
      type: String,
      default: null,
    },
    lpBalance: {
      type: Object,
      default: null,
    },
    lpStaked: {
      type: Object,
      default: null,
    },
    lpAllowance: {
      type: Object,
      default: null,
    },
    lpContract: {
      type: Object,
      default: null,
    },
    stake: {
      type: Boolean,
      default: () => false,
    },
  },
  computed: Vuex.mapState({
    title() {
      return this.stake && this.$t('stakeLiquidity', {0: this.lpSymbol}) || this.$t('unstakeLiquidity', {0: this.lpSymbol});
    },
  }),
  methods: {
    open() {
      this.$refs.drawer?.open();
    },
  },
};
</script>