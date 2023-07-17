<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2022 Meeds Association contact@meeds.io
 
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
        :src="imageSrc"
        max-height="50px"
        max-width="50px"
        class="ps-1"
        contain
        eager />
    </template>
    <template #col1>
      <deeds-tab-link
        :label="cardTypeI18N"
        tab-link="owners"
        class="ms-n4"
        @click="selectDeed" />
    </template>
    <template #col2>
      <span class="text-capitalize">{{ cityName }}</span>
    </template>
    <template #col3>
      <div v-if="smallScreen">
        {{ count }}
      </div>
      <div v-else>
        <v-btn
          id="pView-button"
          :href="ownersURL"
          class="px-5"
          color="primary"
          height="40px"
          outlined>
          <span class="font-weight-bold subtitle-1">{{ $t('view') }}</span>
        </v-btn>
      </div>
    </template>
    <template #col5>
      <div v-if="!smallScreen">
        {{ count }}
      </div>
    </template>
  </deeds-token-asset-template>
</template>
<script>
export default {
  props: {
    deed: {
      type: Object,
      default: null,
    },
  },
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    ownersURL: state => state.ownersURL,
    smallScreen() {
      return this.$vuetify.breakpoint.smAndDown;
    },
    cityName() {
      return this.deed?.cityName;
    },
    cardName() {
      return this.deed?.cardName;
    },
    cardTypeI18N() {
      return this.cardName && this.$t(this.cardName.toLowerCase());
    },
    count() {
      return this.deed?.count;
    },
    imageSrc() {
      return this.cardName && this.cityName && `${this.parentLocation}/static/images/nft/${this.cityName.toLowerCase()}-${this.cardName.toLowerCase()}.webp`;
    },
  }),
  methods: {
    selectDeed() {
      this.$store.commit('setStandaloneDeedCardName', this.cardName);
    },
  },
};
</script>