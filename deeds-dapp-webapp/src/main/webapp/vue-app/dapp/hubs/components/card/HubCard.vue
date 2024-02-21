<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
    :href="hubUrl"
    target="_blank"
    class="no-border"
    max-width="395px"
    flat>
    <v-hover v-slot="{hover}">
      <v-card
        class="rounded-lg overflow-hidden border-box-sizing"
        height="270px"
        max-height="270px"
        hover
        outlined>
        <v-card-actions v-if="!standalone" class="position-absolute z-index-two r-0">
          <v-tooltip bottom>
            <template #activator="{ on, attrs }">
              <v-slide-y-transition>
                <v-btn
                  v-show="hover || isMobile"
                  icon
                  small
                  dense
                  class="mx-2 px-0 elevation-1 white"
                  v-bind="attrs"
                  v-on="on"
                  @click.prevent.stop="$root.$emit('open-hub-details', hub)">
                  <v-icon
                    size="14"
                    color="info">
                    fa-info
                  </v-icon>
                </v-btn>
              </v-slide-y-transition>
            </template>
            <span>{{ $t('wom.openHubReports') }}</span>
          </v-tooltip>
        </v-card-actions>
        <v-card
          :color="hubBackgroundColor" 
          class="no-border-radius border-box-sizing"
          height="100px"
          width="100%"
          flat />
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
        <v-card
          class="d-flex flex-column pt-2 px-4 pb-4 overflow-hidden text-start"
          height="170px"
          flat>
          <div class="ms-10 ps-15 text-truncate">
            <span class="text-h6 font-weight-bold text-truncate">
              {{ hubName || $t('wom.unknownHub', {0: deedId}) }}
            </span>
          </div>
          <v-card
            v-sanitized-html="hubDescriptionText"
            height="50px"
            class="text-light-color font-weight-normal my-1 text-truncate-2 pa-0"
            flat />
          <div v-if="hub.upcomingDeedId" class="d-flex my-auto">
            <div v-if="!hubUsers" class="d-flex align-center justify-center me-auto">
              <v-icon size="45" class="secondary--text me-3">fas fa-bolt</v-icon>
              <span class="text-light-color"> {{ $t('hubs.gettingStarted') }} </span>
            </div>
            <template v-else>
              <div class="d-flex align-center justify-center me-auto">
                <v-img 
                  :src="`${parentLocation}/static/images/teamwork_icon_red.webp`"
                  class="me-2"
                  width="25px"
                  height="25px" />
                <div class="text-light-color font-weight-normal">
                  {{ hubUsers }}
                </div>
              </div>
              <div v-if="!hubRewardsPeriod || !hubRewardsAmount" class="d-flex align-center justify-center ms-auto">
                <v-icon size="45" class="secondary--text me-3">fas fa-bolt</v-icon>
                <span class="text-light-color"> {{ $t('hubs.gettingStarted') }} </span>
              </div>
              <div v-else class="d-flex align-center justify-center ms-auto">
                <img
                  :src="`${parentLocation}/static/images/meed_circle.webp`"
                  :alt="$t('page.token')"
                  width="25"
                  height="25"
                  class="me-2">
                <div class="text-light-color d-flex font-weight-normal">
                  {{ hubRewardsAmountFormatted }}
                  <span class="ms-2 text-no-wrap">Ɱ / {{ hubRewardsPeriod }}</span>
                </div>
              </div>
            </template>
          </div>
          <div v-else class="d-flex my-auto">
            <div v-if="hasReports" class="d-flex align-center me-auto pa-0">
              <v-icon size="45" class="secondary--text me-3">fa-rocket</v-icon>
              <div class="d-flex flex-column justify-center text-start">
                <div :class="engagementScoreClass" class="title font-weight-bold my-auto"> {{ engagementScoreFormatted }} </div>
                <div class="text-light-color text-subtitle-2 my-auto hidden-xs-only"> {{ $t('hubs.engagementScore') }} </div>
              </div>
            </div>
            <div v-else class="d-flex align-center me-auto pa-0">
              <v-icon size="45" class="secondary--text me-3">fas fa-bolt</v-icon>
              <span class="text-light-color"> {{ $t('hubs.gettingStarted') }} </span>
            </div>
            <div class="d-flex flex-column align-center me-auto pa-0">
              <v-tooltip v-if="hubActionsCount" bottom>
                <template #activator="{ on, attrs }">
                  <div
                    v-on="on"
                    v-bind="attrs"
                    class="d-flex align-center justify-center mb-auto me-auto">
                    <v-icon size="22" class="secondary--text me-2">fas fa-trophy</v-icon>
                    <div class="text-light-color font-weight-normal">
                      {{ hubActionsCountFormatted }}
                    </div>
                  </div>
                </template>
                <span>{{ $t('wom.availableActionsForContributors', {0: hubActionsCountFormatted}) }}</span>
              </v-tooltip>
              <v-tooltip v-if="hubRewardsAmount" bottom>
                <template #activator="{ on, attrs }">
                  <div
                    v-on="on"
                    v-bind="attrs"
                    class="d-flex align-center justify-center mt-1 me-auto">
                    <img
                      :src="`${parentLocation}/static/images/meed_circle.webp`"
                      :alt="$t('page.token')"
                      width="25"
                      height="25"
                      class="me-2">
                    <div class="text-light-color d-flex font-weight-normal">
                      {{ hubRewardsAmountFormatted }}
                      <span class="ms-2 text-no-wrap">Ɱ / {{ hubRewardsPeriod }}</span>
                    </div>
                  </div>
                </template>
                <span>{{ $t('wom.rewardsSentToRecipients') }}</span>
              </v-tooltip>
            </div>
          </div>
        </v-card>
      </v-card>
    </v-hover>
  </v-card>
</template>
<script>
export default {
  props: {
    hub: {
      type: Object,
      default: null,
    },
    standalone: {
      type: Boolean,
      default: false,
    },
  },
  computed: Vuex.mapState({
    language: state => state.language,
    parentLocation: state => state.parentLocation,
    cardTypes: state => state.cardTypes,
    cities: state => state.cities,
    formLink: state => state.formLink,
    isMobile: state => state.isMobile,
    openSeaBaseLink: state => state.openSeaBaseLink,
    hubName() {
      return this.language === 'fr' && this.hub?.name?.fr || this.hub?.name?.en;
    },
    hubDescription() {
      return this.language === 'fr' && this.hub?.description?.fr || this.hub?.description?.en;
    },
    hubDescriptionText() {
      return this.hubDescription && this.$utils.htmlToText(this.hubDescription) || '';
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
      return this.hub?.logoUrl || `${this.parentLocation}/api/hubs/${this.hubAddress}/avatar?v=${this.hubUpdateTime || 0}`;
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
    disconnected() {
      return !this.hub?.upcomingDeedId && !this.hub?.connected;
    },
    unkownHub() {
      return !this.hubUrl || !this.hubName;
    },
    hubRewardsPeriodType() {
      return this.hub?.rewardsPeriodType?.toLowerCase?.();
    },
    hubRewardsPeriod() {
      return this.hubRewardsPeriodType && this.$t(`wom.${this.hubRewardsPeriodType}`);
    },
    hubRewardsAmount() {
      return this.hub?.rewardsPerPeriod || 0;
    },
    hubRewardsAmountFormatted() {
      return this.formatNumber(this.hubRewardsAmount);
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
    hubActionsCount() {
      return this.hub?.actionsCount || 0;
    },
    hubActionsCountFormatted() {
      return this.$utils.numberFormatWithDigits(this.hubActionsCount, this.language);
    },
    hasReports() {
      return this.hub?.hasReports;
    },
    engagementScore() {
      return this.hub?.engagementScore || 0;
    },
    engagementScoreFormatted() {
      return this.$utils.numberFormatWithDigits(this.engagementScore, this.language, 0, 1);
    },
    engagementScoreClass() {
      if (!this.engagementScore) {
        return 'text-light-color';
      } else if (this.engagementScore > 11) {
        return 'success--text';
      } else if (this.engagementScore < 9) {
        return 'error--text';
      } else {
        return 'text-light-color';
      }
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
    formatNumber(num) {
      const useKilo = num >= 1000;
      const value = useKilo ? num / 1000 : num;
      const formatted = this.$utils.numberFormatWithDigits(value, this.language, 0, useKilo && 1 || (value < 1 && 2 || 0));
      return useKilo ? this.$t('kilo', {0: formatted}) : formatted;
    },
  },
};
</script>