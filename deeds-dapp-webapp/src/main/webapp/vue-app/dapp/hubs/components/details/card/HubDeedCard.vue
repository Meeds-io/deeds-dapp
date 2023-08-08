<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
  <v-card
    :loading="loading"
    min-height="200"
    flat>
    <v-card
      v-if="!loading"
      :style="hubStyle"
      width="420"
      max-width="100%"
      height="270px"
      max-height="270px"
      class="mx-auto overflow-hidden position-relative z-index-two rounded-lg d-flex flex-column"
      flat>
      <v-card-actions
        v-if="isManager"
        class="position-absolute z-index-two r-0">
        <v-btn
          :title="$t('wom.disconnectHubTooltip')"
          icon
          class="mx-0 px-0 elevation-1 white"
          @click.prevent.stop="$root.$emit('disconnect-hub')">
          <v-icon
            size="22"
            color="error">
            fa-power-off
          </v-icon>
        </v-btn>
      </v-card-actions>
      <v-card
        class="d-flex position-absolute z-index-zero ms-n4"
        height="100%"
        width="100%"
        tile
        flat>
        <v-img
          :src="cardImage"
          width="110%"
          max-width="110%" />
      </v-card>
      <v-card
        class="transparent"
        height="100px"
        rounded="lg"
        flat />
      <v-card
        class="d-flex flex-column flex-grow-1 opacity-8"
        tile
        flat>
        <v-card
          height="75px"
          width="75px"
          class="ms-5 mt-n10 rounded-lg position-absolute z-index-two"
          outlined>
          <v-img
            v-if="hubLogoUrl"
            :src="hubLogoUrl"
            class="no-border-radius mx-auto"
            height="100%"
            width="90%"
            contain />
        </v-card>
        <div
          :style="hubStyleTop"
          class="d-flex flex-column flex-grow-1 pt-2 px-4 pb-4 position-relative z-index-one">
          <div class="ms-10 ps-15">
            <div
              :title="hubName"
              class="text-h6 font-weight-bold text-no-wrap text-truncate">
              {{ hubName }}
            </div>
            <div
              v-sanitized-html="hubUrl"
              class="text-truncate">
            </div>
          </div>
          <div
            :title="hubDescription"
            v-sanitized-html="hubDescription"
            class="text-light-color transparent font-weight-normal mt-3 text-truncate-2 flex-grow-1 pa-0"
            flat
            tile>
          </div>
          <div class="d-flex mt-4">
            <div class="d-flex align-center justify-center">
              <v-img 
                :src="`${parentLocation}/static/images/teamwork_icon_red.webp`"
                class="me-2"
                width="25px"
                height="25px" />
              <div class="text-light-color font-weight-normal">
                {{ hubUsersCount }} {{ $t('wom.users') }}
              </div>
            </div>
            <div class="d-flex align-center justify-center ms-10">
              <v-img 
                :src="`${parentLocation}/static/images/meed_circle.webp`"
                class="me-2"
                width="25px"
                height="25px" />
              <div class="text-light-color d-flex font-weight-normal">
                {{ hubRewardsAmount }}
                <span class="ms-2 text-no-wrap">Ɱ / {{ hubRewardsPeriod }}</span>
              </div>
            </div>
          </div>
        </div>
      </v-card>
    </v-card>
  </v-card>
</template>
<script>
export default {
  props: {
    hub: {
      type: Object,
      default: null,
    },
    isManager: {
      type: Boolean,
      default: false,
    },
  },
  computed: Vuex.mapState({
    cardTypes: state => state.cardTypes,
    cities: state => state.cities,
    language: state => state.language,
    parentLocation: state => state.parentLocation,
    hubNames() {
      return this.hub?.name || {};
    },
    hubDescriptions() {
      return this.hub?.description || {};
    },
    hubName() {
      return this.hubNames[this.language] || this.hubNames['en'] || '';
    },
    hubDescription() {
      return this.hubDescriptions[this.language] || this.hubDescriptions['en'] || '';
    },
    hubAddress() {
      return this.hub?.address;
    },
    hubUpdateTime() {
      return this.hub?.updatedDate && new Date(this.hub?.updatedDate).getTime();
    },
    hubLogoUrl() {
      return `${this.parentLocation}/api/hubs/${this.hubAddress}/avatar?v=${this.hubUpdateTime || 0}`;
    },
    hubUrl() {
      return this.hub?.url;
    },
    hubBackgroundColor() {
      return this.hub?.color || 'primary';
    },
    cityIndex() {
      return this.hub?.city;
    },
    cardTypeIndex() {
      return this.hub?.type;
    },
    city() {
      return this.cities[this.cityIndex];
    },
    cardType() {
      return this.cardTypes[this.cardTypeIndex];
    },
    cardImage() {
      return this.city && this.cardType && `${this.parentLocation}/static/images/nft/${this.city.toLowerCase()}-${this.cardType.toLowerCase()}.png`;
    },
    hubStyle() {
      return `border: 1px solid ${this.hub?.color || '#707070'} !important;`;
    },
    hubStyleTop() {
      return `border-top: 1px solid ${this.hub?.color || '#707070'} !important;`;
    },
    hubUsersCount() {
      return new Intl.NumberFormat(this.language, {
        style: 'decimal',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
      }).format(this.hub?.usersCount || 0);
    },
    hubRewardsPeriodType() {
      return this.hub?.rewardsPeriodType?.toLowerCase();
    },
    hubRewardsPeriod() {
      return this.$t(`wom.${this.hubRewardsPeriodType}`);
    },
    hubRewardsAmount() {
      return new Intl.NumberFormat(this.language, {
        style: 'decimal',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
      }).format(this.hub?.rewardsPerPeriod || 0);
    },
  }),
};
</script>