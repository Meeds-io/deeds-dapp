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
  <deeds-button-group-item
    :selected-values="selectedCards"
    :value="card"
    :small="small"
    :color="blackThemeColor"
    selected-color="grey">
    <v-list-item-avatar
      class="deed-avatar ms-0 me-1 my-auto"
      :min-width="`${avatarSize}px !important`"
      :height="`${avatarSize}px !important`"
      :width="`${avatarSize}px !important`">
      <v-img :src="cardImage" />
    </v-list-item-avatar>
    <span class="text-capitalize">{{ cardTypeI18N }}</span>
  </deeds-button-group-item>
</template>
<script>
export default {
  props: {
    selectedCards: {
      type: Array,
      default: null,
    },
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
    blackThemeColor: state => state.blackThemeColor,
    cardImage() {
      return this.city && this.card && `/${this.parentLocation}/static/images/nft/${this.city.toLowerCase()}-${this.card.toLowerCase()}.png`;
    },
    cardTypeI18N() {
      return this.card && this.$t(this.card.toLowerCase());
    },
  }),
};
</script>