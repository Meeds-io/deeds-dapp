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
  <div class="d-flex flex-column mt-2 mt-sm-6">
    <v-card flat>
      <v-card-title class="py-0 justify-center">
        <span class="col-7 ps-0 text-sm-h2 display-2 font-weight-bold">{{ $t('productTour.title') }}</span>
        <v-spacer />
        <v-img 
          :src="`${parentLocation}/static/images/tour_banner.png`"
          :width="isMobile && '131px' || '360px'"
          height="183px"
          class="me-0 me-sm-16"
          alt=""
          contain
          eager />
      </v-card-title>
    </v-card>
    <div class="d-flex flex-column pt-4 pb-16 my-16">
      <div class="ps-0 display-1 grey--text align-center font-weight-light">{{ $t('productTour.description') }}</div>
      <video
        class="mt-11 mt-md-13"
        height="360px"
        max-width="750px"
        loop="true"
        autoplay
        muted
        controls>
        <source :src="`${introductiveVideoLink}`" type="video/mp4">
      </video>
      <div class="d-flex flex-wrap justify-center mt-13">
        <v-btn
          href="./marketplace"
          class="primary mx-5 mt-7"
          width="300px"
          height="75px"
          depressed
          dark
          outlined>
          <span class="display-1 font-weight-bold">{{ $t('startYourHub') }}</span>
        </v-btn>
        <v-btn
          href="https://builders.meeds.io"
          target="_blank"
          width="300px"
          height="75px"
          class="dark-grey-border-color dark-grey-color d-flex justify-center mx-5 mt-7" 
          outlined>
          <v-icon size="40" class="me-2">fas fa-external-link-alt</v-icon>
          <span class="display-1 font-weight-bold">{{ $t('exploreAHub') }}</span>
        </v-btn>
      </div>
    </div>
    <div class="mt-16">
      <div class="d-flex flex-column flex-md-row pb-16 my-16">
        <div class="d-flex flex-column my-auto mx-0 mx-md-14">
          <span class="display-1 font-weight-bold">{{ $t('contributionPrograms.title') }}</span>
          <span class="mt-10 mb-5 mb-md-0 text-h5 grey--text font-weight-light">{{ $t('contributionPrograms.description') }}</span>
          <div class="position-absolute">
            <v-img
              :src="`${parentLocation}/static/images/contribution_icon.png`"
              max-width="275px"
              class="contribution-icon"
              alt=""
              contain
              eager />    
          </div>
        </div>
        <video
          height="350px"
          loop="true"
          autoplay
          muted
          controls>
          <source :src="`${contributionProgramsVideoLink}`" type="video/mp4">
        </video>
      </div>
      <div class="d-flex flex-column-reverse flex-md-row py-16 mb-16 ms-0 ms-md-14">
        <video
          height="350px"
          loop="true"
          autoplay
          muted
          controls>
          <source :src="`${rewardsVideoLink}`" type="video/mp4">
        </video>
        <div class="d-flex flex-column my-auto mx-0 mx-md-14">
          <span class="display-1 font-weight-bold">{{ $t('rewards.title') }}</span>
          <span class="text-h5 grey--text font-weight-light mt-10 mb-5 mb-md-0">{{ $t('rewards.description') }}</span>
          <div class="position-absolute">
            <v-img
              :src="`${parentLocation}/static/images/reward_icon.png`"
              max-width="236px"
              class="reward-icon"
              alt=""
              contain
              eager />    
          </div>
        </div>
      </div>
      <div class="d-flex flex-column flex-md-row py-16 mb-16">
        <div class="d-flex flex-column my-auto mx-0 mx-md-14">
          <span class="display-1 font-weight-bold">{{ $t('overview.title') }}</span>
          <span class="text-h5 grey--text font-weight-light mt-10 mb-5 mb-md-0">{{ $t('overview.description') }}</span>
          <div class="position-absolute">
            <v-img
              :src="`${parentLocation}/static/images/overview_icon.png`"
              max-width="330px"
              class="overview-icon"
              alt=""
              contain
              eager />    
          </div>
        </div>
        <video
          height="350px"
          loop="true"
          autoplay
          muted
          controls>
          <source :src="`${overviewVideoLink}`" type="video/mp4">
        </video>
      </div>
      <div class="d-flex flex-column-reverse flex-md-row py-16 mb-16 ms-0 ms-md-14">
        <video
          height="350px"
          loop="true"
          autoplay
          muted
          controls>
          <source :src="`${perksVideoLink}`" type="video/mp4">
        </video>
        <div class="d-flex flex-column my-auto mx-0 mx-md-14">
          <span class="display-1 font-weight-bold">{{ $t('perks.title') }}</span>
          <span class="text-h5 grey--text font-weight-light mt-10 mb-5 mb-md-0">{{ $t('perks.description') }}</span>
          <div class="position-absolute">
            <v-img
              :src="`${parentLocation}/static/images/perks_icon.png`"
              max-width="400px"
              class="perks-icon"
              alt=""
              contain
              eager />    
          </div>
        </div>
      </div>
      <div class="d-flex flex-column flex-md-row py-16 mb-16">
        <div class="d-flex flex-column my-auto mx-0 mx-md-14">
          <span class="display-1 font-weight-bold">{{ $t('teamwork.title') }}</span>
          <span class="text-h5 grey--text font-weight-light mt-10 mb-5 mb-md-0">{{ $t('teamwork.description') }}</span>
          <div class="position-absolute">
            <v-img
              :src="`${parentLocation}/static/images/teamwork_icon.png`"
              max-width="298px"
              class="teamwork-icon"
              alt=""
              contain
              eager />    
          </div>
        </div>
        <video
          height="350px"
          loop="true"
          autoplay
          muted
          controls>
          <source :src="`${teamworkVideoLink}`" type="video/mp4">
        </video>
      </div>
    </div>
    <div class="mt-16">
      <v-card
        height="8px"
        width="140px"
        color="primary no-border-radius"
        flat />
      <span class="display-3 d-flex justify-center font-weight-bold mt-4 mb-7">{{ $t('andMore') }}</span>
      <v-container class="mb-10">
        <v-row no-gutters>
          <v-col
            cols="12"
            lg="4"
            md="12">
            <div 
              class="mx-3 my-4 my-md-0 d-flex flex-column"
              :class="!isSmallScreen && 'fill-height'">
              <div class="d-flex my-8 mx-5">
                <div>
                  <v-icon 
                    size="75" 
                    color="primary" 
                    class="me-md-0 me-9">
                    fas fa-award
                  </v-icon>
                </div>
                <span class="display-1 font-weight-bold d-flex align-center ms-5">
                  {{ $t('kudos') }}
                </span>
              </div>
              <span class="text-h5 grey--text font-weight-light">
                {{ $t('kudos.description') }}
              </span>
            </div>
          </v-col>
          <v-col
            cols="12"
            lg="4"
            md="12">
            <div 
              class="mx-3 my-4 my-md-0 d-flex flex-column"
              :class="!isSmallScreen && 'fill-height'">
              <div class="d-flex my-8 mx-5">
                <div>
                  <v-icon size="75" color="primary">fas fa-graduation-cap</v-icon>
                </div>
                <span class="display-1 font-weight-bold d-flex align-center ms-5">
                  {{ $t('badges') }}
                </span>
              </div>
              <span class="text-h5 grey--text font-weight-light">
                {{ $t('badges.description') }}
              </span>
            </div>
          </v-col>
          <v-col
            cols="12"
            lg="4"
            md="12">
            <div 
              class="mx-3 my-4 my-md-0 d-flex flex-column"
              :class="!isSmallScreen && 'fill-height'">
              <div class="d-flex my-8 mx-5">
                <div>
                  <v-icon 
                    size="75" 
                    color="primary" 
                    class="me-md-0 me-5">
                    fas fa-wallet
                  </v-icon>
                </div>
                <span class="display-1 font-weight-bold d-flex align-center ms-5">
                  {{ $t('wallet') }}
                </span>
              </div>
              <span class="text-h5 grey--text font-weight-light">
                {{ $t('wallet.description') }}
              </span>
            </div>
          </v-col>
        </v-row>
      </v-container>
      <div class="d-flex justify-end mb-16 pb-16">
        <v-card
          height="8px"
          width="140px"
          color="secondary no-border-radius"
          flat />
      </div>
    </div>
  </div>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    isSmallScreen() {
      return this.$vuetify.breakpoint.mdAndDown;
    },
    introductiveVideoLink: state => state.introductiveVideoLink,
    contributionProgramsVideoLink: state => state.contributionProgramsVideoLink,
    rewardsVideoLink: state => state.rewardsVideoLink,
    overviewVideoLink: state => state.overviewVideoLink,
    perksVideoLink: state => state.perksVideoLink,
    teamworkVideoLink: state => state.teamworkVideoLink,
    isMobile() {
      return this.$vuetify.breakpoint.smAndDown;
    },
  })
};
</script>