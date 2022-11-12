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
  <div class="d-flex flex-column mt-8 mt-sm-10">
    <deeds-nft-title />
    <template v-if="!noCityLeft">
      <div class="d-flex flex-column-reverse flex flex-md-row mb-8">
        <div class="d-flex flex-column ms-0 ms-md-8">
          <deeds-nft-introduction />
          <deeds-points-simulator />
        </div>
        <deeds-earned-points class="ms-md-auto me-md-0 mx-auto mb-8 flex-shrink-0 flex-grow-0" />
      </div>
    </template>
    <deeds-redeem class="mb-8" />
    <deeds-owned-table v-if="!noCityLeft && !metamaskOffline" />
    <deeds-trade />
  </div>
</template>
<script>
export default {
  computed: Vuex.mapState({
    noCityLeft: state => state.noCityLeft,
    metamaskOffline: state => state.metamaskOffline,
  }),
};
</script>