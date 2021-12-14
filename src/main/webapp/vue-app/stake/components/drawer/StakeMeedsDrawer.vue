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
  <deeds-drawer ref="drawer">
    <template #title>
      <h4>{{ title }}</h4>
    </template>
    <template #content>
      <deeds-stake-meeds-steps
        v-if="stake"
        @start-loading="$refs.drawer.startLoading()"
        @end-loading="$refs.drawer.endLoading()" />
      <deeds-stake-meeds-step-unstake v-else />
    </template>
  </deeds-drawer>
</template>

<script>
export default {
  props: {
    stake: {
      type: Boolean,
      default: () => false,
    },
  },
  computed: Vuex.mapState({
    title() {
      return this.stake && this.$t('stakeMeeds') || this.$t('unstakeMeeds');
    },
  }),
  methods: {
    open() {
      this.$refs.drawer.open();
    },
  },
};
</script>