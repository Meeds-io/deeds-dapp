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
  <div class="mt-8 mt-sm-10">
    <deeds-stake-introduction />
    <div class="d-flex flex-column-reverse flex-md-row">
      <div class="d-flex flex-column ms-0 ms-md-8">
        <deeds-stake-yield />
        <deeds-stake-governance />
        <deeds-stake-deeds />
        <deeds-stake-contribute />
      </div>
      <deeds-stake-meeds class="mx-auto ms-md-auto me-md-0 mb-8 flex-shrink-0 flex-grow-1" />
    </div>
  </div>
</template>
<script>
export default {
  created() {
    this.$store.commit('loadRewardedFunds', true, true);
  },
};
</script>