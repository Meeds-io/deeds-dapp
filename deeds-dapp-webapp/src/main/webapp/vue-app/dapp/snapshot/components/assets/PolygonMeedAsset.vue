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
  <deeds-token-asset-template>
    <template #image>
      <v-img
        :src="`${parentLocation}/static/images/meedsToken.png`"
        max-height="50px"
        max-width="50px"
        class="ps-1"
        contain
        eager />
    </template>
    <template #col1>
      <deeds-tab-link
        label="Meeds"
        tab-link="farm"
        class="ms-n4" />
    </template>
    <template #col2>
      <v-img
        :src="`${parentLocation}/static/images/polygon_network.webp`"
        width="23px"
        height="21px" />
    </template>
    <template #col3>
      <deeds-number-format
        v-if="!smallScreen"
        :value="polygonMeedsBalance"
        :fractions="2"
        currency />
      <deeds-number-format
        v-else
        :value="polygonMeedsBalance"
        :fractions="2">
        Ɱ
      </deeds-number-format>
    </template>
    <template #col4>
      <deeds-number-format
        v-if="smallScreen"
        :value="polygonMeedsBalance"
        :fractions="2"
        currency />
    </template>
    <template #col5>
      <deeds-number-format
        v-if="!smallScreen"
        :value="polygonMeedsBalance"
        :fractions="2">
        Ɱ
      </deeds-number-format>
    </template>
  </deeds-token-asset-template>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    polygonMeedsBalance: state => state.polygonMeedsBalance,
    smallScreen() {
      return this.$vuetify.breakpoint.smAndDown;
    },
  }),
};
</script>
