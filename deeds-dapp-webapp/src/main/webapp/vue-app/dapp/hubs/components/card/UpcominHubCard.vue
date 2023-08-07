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
    :href="hubUrl"
    class="full-width"
    target="_blank"
    flat>
    <v-card
      class="rounded-lg"
      min-height="270px"
      hover
      outlined>
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
          :src="`${hubLogoUrl}`"
          class="no-border-radius mx-auto"
          height="100%"
          width="90%"
          contain />
      </v-card>
      <div class="d-flex flex-column pt-2 px-4 pb-4">
        <div class="ms-10 ps-15">
          <span class="text-h6 font-weight-bold text-wrap">
            {{ hubName }}
          </span>
        </div>
        <v-card 
          height="50px"
          class="text-light-color font-weight-normal mt-3 text-truncate-2 pa-0"
          flat>
          {{ hubDescription }}
        </v-card>
        <v-spacer />
        <div v-if="!upcomingHub" class="d-flex mt-4">
          <div v-if="hubUsers" class="d-flex align-center justify-center">
            <v-img 
              :src="`${parentLocation}/static/images/teamwork_icon_red.webp`"
              class="me-2"
              width="25px"
              height="25px" />
            <div class="text-light-color font-weight-normal">
              {{ hubUsers }}
            </div>
          </div>
          <div v-if="hubRewards" class="d-flex align-center justify-center ms-auto">
            <v-img 
              :src="`${parentLocation}/static/images/meed_circle.webp`"
              class="me-2"
              width="25px"
              height="25px" />
            <div class="text-light-color d-flex font-weight-normal">
              {{ formattedHubRewards }}
              <span class="ms-2 text-no-wrap">Ɱ / {{ hubRewardsPeriodicity }}</span>
            </div>
          </div>
          <div v-if="noUsersDataToDisplay">
            <v-icon size="21" class="secondary--text me-3">fas fa-bolt</v-icon>
            <span class="text-light-color"> {{ $t('hubs.gettingStarted') }} </span>
          </div>
        </div>
        <div v-else class="d-flex flex-row pt-4">
          <div class="d-flex flex-row">
            <v-btn
              v-if="hubWebsiteUrl"
              :href="hubWebsiteUrl"
              target="_blank"
              class="me-4"
              icon>
              <v-icon 
                size="26" 
                class="dark-grey-color">
                fa-globe
              </v-icon>
            </v-btn>
            <v-btn
              v-if="hubTwitterUrl"
              :href="hubTwitterUrl"
              target="_blank"
              class="me-4"
              icon>
              <v-icon 
                size="26" 
                class="dark-grey-color">
                fab fa-twitter
              </v-icon>
            </v-btn>
            <v-btn
              v-if="hubDiscordUrl"
              :href="hubDiscordUrl" 
              target="_blank"
              class="me-4"
              icon>
              <v-icon 
                size="26" 
                class="dark-grey-color">
                fab fa-discord
              </v-icon>
            </v-btn>
            <v-btn
              v-if="hubGithubUrl"
              :href="hubGithubUrl"
              target="_blank"
              class="me-4"
              icon>
              <v-icon 
                size="26" 
                class="dark-grey-color">
                fab fa-github
              </v-icon>
            </v-btn>
          </div>
          <v-spacer />
          <div>
            <v-btn
              id="hClaim-button"
              :href="formLinkWithCommunityName"
              target="_blank"
              class="px-6"
              color="primary"
              outlined>
              <span class="font-size-normal">{{ $t('claim') }}</span>
            </v-btn>
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
    upcomingHub: {
      type: Boolean,
      default: false,
    }
  },
  computed: Vuex.mapState({
    language: state => state.language,
    parentLocation: state => state.parentLocation,
    formLink: state => state.formLink,
    hubName() {
      return this.language === 'fr' && this.hub?.name?.fr || this.hub?.name?.en;
    },
    hubDescription() {
      return this.language === 'fr' && this.hub?.description?.fr || this.hub?.description?.en;
    },
    hubBackgroundColor() {
      return this.hub?.backgroundColor || this.hub?.color;
    },
    hubLogoUrl() {
      return this.hub?.logoUrl;
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
    hubRewardsPerWeek() {
      return (this.hub?.rewardsPerWeek || 0) / 1000;
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
      return this.formLink.concat('#community_name=', this.hubName);
    },
    noUsersDataToDisplay() {
      return !this.hubRewards && !this.hubUsers;
    }
  }),
};
</script>
