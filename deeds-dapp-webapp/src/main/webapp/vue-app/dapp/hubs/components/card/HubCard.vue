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
    class="full-width"
    flat
    @click="$root.$emit('open-hub-details', hub)">
    <v-card
      class="rounded-lg"
      height="270px"
      max-height="270px"
      hover
      outlined>
      <v-card-actions class="position-absolute z-index-two r-0">
        <v-tooltip bottom>
          <template #activator="{ on, attrs }">
            <v-btn
              icon
              small
              dense
              class="mx-0 px-0 elevation-1 white"
              v-bind="attrs"
              v-on="on"
              @click.prevent.stop="$root.$emit('open-hub-details', hub)">
              <v-icon
                size="14"
                color="info">
                fa-info
              </v-icon>
            </v-btn>
          </template>
          <span>{{ $t('wom.openHubDetails') }}</span>
        </v-tooltip>
      </v-card-actions>
      <v-card
        :color="hubBackgroundColor" 
        height="100px"
        width="100%"
        flat />
      <v-card
        height="75px"
        width="75px"
        class="ms-5 mt-n10 rounded-lg position-absolute"
        outlined>
        <v-img
          v-if="hubLogoUrl"
          :src="hubLogoUrl"
          class="no-border-radius mx-auto"
          height="100%"
          width="90%"
          contain />
      </v-card>
      <div class="d-flex flex-column pt-2 px-4 pb-4">
        <div class="ms-10 ps-15">
          <span class="text-h6 font-weight-bold text-no-wrap">
            {{ hubName }}
          </span>
        </div>
        <v-card
          v-sanitized-html="hubDescription"
          height="50px"
          class="text-light-color font-weight-normal mt-3 text-truncate-2 pa-0"
          flat />
        <v-spacer />
        <div class="d-flex mt-4">
          <v-tooltip v-if="deedId" bottom>
            <template #activator="{ on, attrs }">
              <v-chip
                :href="`${openSeaBaseLink}/${deedId}`"
                target="_blank"
                rel="nofollow noreferrer noopener"
                class="overflow-hidden d-block me-2"
                outlined
                v-bind="attrs"
                v-on="on"
                @click="nop">
                <v-img
                  :src="cardImage"
                  max-height="36"
                  max-width="36"
                  class="rounded-circle ms-n3" />
                <div class="text-light-color font-weight-normal body-1">
                  #{{ deedId }}
                </div>
              </v-chip>
            </template>
            <span>{{ $t('wom.openDeedInOpenSea') }}</span>
          </v-tooltip>
          <v-tooltip v-if="hubUrl" bottom>
            <template #activator="{ on, attrs }">
              <v-btn
                :href="hubUrl"
                target="_blank"
                class="me-2"
                icon
                v-bind="attrs"
                v-on="on"
                @click="nop">
                <v-icon
                  size="26"
                  class="dark-grey-color">
                  fa-globe
                </v-icon>
              </v-btn>
            </template>
            <span>{{ $t('wom.openHubWebsite') }}</span>
          </v-tooltip>
          <div class="d-flex align-center justify-center ms-auto">
            <v-img 
              :src="`${parentLocation}/static/images/teamwork_icon_red.webp`"
              class="me-2"
              width="25px"
              height="25px" />
            <div class="text-light-color font-weight-normal">
              {{ hubUsers }}
            </div>
          </div>
          <div class="d-flex align-center justify-center ms-2">
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
</template>
<script>
export default {
  props: {
    hub: {
      type: Object,
      default: null,
    },
  },
  computed: Vuex.mapState({
    language: state => state.language,
    parentLocation: state => state.parentLocation,
    cardTypes: state => state.cardTypes,
    cities: state => state.cities,
    formLink: state => state.formLink,
    openSeaBaseLink: state => state.openSeaBaseLink,
    hubName() {
      return this.language === 'fr' && this.hub?.name?.fr || this.hub?.name?.en;
    },
    hubDescription() {
      return this.language === 'fr' && this.hub?.description?.fr || this.hub?.description?.en;
    },
    hubBackgroundColor() {
      return this.hub?.backgroundColor || this.hub?.color;
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
    hubUsersCount() {
      return this.hub?.usersCount || 0;
    },
    hubUsers() {
      return this.hubUsersCount > 999 ? `${parseInt(this.hubUsersCount / 1000)}K` : this.hubUsersCount;
    },
    hubUrl() {
      return this.hub?.hubUrl || this.hub?.url;
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
    hubWebsiteUrl() {
      return this.hub?.websiteUrl;
    },
    hubTwitterUrl() {
      return this.hub?.twitterURL;
    },
    hubDiscordUrl() {
      return this.hub?.discordUrl;
    },
    hubGithubUrl() {
      return this.hub?.githubUrl;
    },
    formLinkWithCommunityName() {
      return this.formLink.concat('#communityName=', this.hubName);
    },
    deedId() {
      return this.hub?.deedId;
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
  }),
  methods: {
    nop(event) {
      if (event) {
        event.stopPropagation();
      }
    },
  },
};
</script>