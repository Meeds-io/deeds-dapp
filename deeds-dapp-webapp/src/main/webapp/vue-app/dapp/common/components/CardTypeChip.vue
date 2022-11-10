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
  <v-chip
    :small="small"
    class="px-1 text-capitalize"
    label
    outlined>
    <v-list-item-avatar
      class="me-1"
      :min-width="`${avatarSize}px !important`"
      :height="`${avatarSize}px !important`"
      :width="`${avatarSize}px !important`">
      <v-img :src="cardImage" />
    </v-list-item-avatar>
    {{ card }}
  </v-chip>
</template>
<script>
export default {
  props: {
    card: {
      type: String,
      default: null,
    },
    city: {
      type: String,
      default: () => 'tanit',
    },
    avatarSize: {
      type: Number,
      default: null,
    },
    small: {
      type: Boolean,
      default: false,
    },
  },
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    cardImage() {
      return `/${this.parentLocation}/static/images/nft/${this.city.toLowerCase()}-${this.card.toLowerCase()}.png`;
    },
  }),
};
</script>