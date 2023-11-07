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
  <div class="d-flex flex-column mt-0 mt-sm-4">
    <div class="d-flex flex-column flex-md-row">
      <div class="my-auto d-flex flex-column">
        <v-card flat>
          <v-card-title
            class="justify-center ps-0 font-weight-black text-center text-md-start text-sm-h3 display-1">
            {{ $t('homePage.title') }}
          </v-card-title>
        </v-card>
        <span class="headline text-sm-h4 mt-6 mt-md-4 mb-10 mb-md-14 font-weight-light text-center text-md-start grey--text">{{ $t('homePage.description') }}</span>
        <div class="d-flex justify-center justify-md-start mb-12 mb-md-0">
          <v-btn
            id="hExplore-button"
            :href="homeUrl"
            height="60px"
            class="primary px-8"
            depressed
            dark
            outlined>
            <span class="headline font-weight-bold">{{ $t('explore') }}</span>
          </v-btn>
          <v-btn
            id="hCreate-button"
            :href="tenantsURL"
            height="60px"
            class="px-8 ms-10"
            color="primary"
            outlined>
            <span class="headline font-weight-bold">{{ $t('create') }}</span>
          </v-btn>
        </div>
      </div>
      <v-img
        :src="`${parentLocation}/static/images/hero_image.webp`"
        class="mb-16 py-5 mb-md-0 mx-auto"
        width="727px"
        alt=""
        contain
        eager />
    </div>
    
    <v-card class="pa-5 mb-6 mt-16 mt-md-11" flat>
      <div class="d-block d-md-flex pb-15">
        <div class="col-12 col-md-6 my-auto">
          <div class="position-absolute">
            <v-img
              :src="`${parentLocation}/static/images/onboard.webp`"
              max-width="200px"
              alt=""
              class="onboard-image"
              contain
              eager />    
          </div>        
          <div class="d-flex flex-column">
            <span class="headline font-weight-bold mb-6">
              {{ $t('homepage.onboard.title') }}
            </span>
            <span class="text-h6 font-weight-light mt-6 grey--text">
              {{ $t('homepage.onboard.descriptionPart1') }}
            </span>
            <span class="text-h6 font-weight-light grey--text">
              {{ $t('homepage.onboard.descriptionPart2') }}
            </span>
            <span class="text-h6 font-weight-light grey--text">
              {{ $t('homepage.onboard.descriptionPart3') }}
            </span>
          </div>
        </div>
        <v-img
          :src="`${parentLocation}/static/images/onboard_image.webp`"
          class="col-12 col-md-6 my-10"
          alt=""
          contain
          eager />
      </div>
      <div class="d-flex flex-column-reverse flex-md-row py-15">
        <v-img
          :src="`${parentLocation}/static/images/organize_image.webp`"
          class="col-12 col-md-6 my-10"
          alt=""
          contain
          eager />
        <div class="col-12 col-md-6 ps-md-14 my-auto">
          <div class="position-absolute">
            <v-img
              :src="`${parentLocation}/static/images/organize.webp`"
              max-width="200px"
              class="organize-image"
              alt=""
              contain
              eager />
          </div>
          <div class="d-flex flex-column">
            <span class="headline font-weight-bold mb-6">
              {{ $t('homepage.organize.title') }}
            </span>
            <span class="text-h6 font-weight-light mt-6 grey--text">
              {{ $t('homepage.organize.descriptionPart1') }}
            </span>
            <span class="text-h6 font-weight-light grey--text">
              {{ $t('homepage.organize.descriptionPart2') }}
            </span>
            <span class="text-h6 font-weight-light grey--text">
              {{ $t('homepage.organize.descriptionPart3') }}
            </span>
          </div>
        </div>
      </div>
      <div class="d-block d-md-flex py-15">
        <div class="col-12 col-md-6 my-auto">
          <div class="d-flex">
            <div class="position-absolute">
              <v-img
                :src="`${parentLocation}/static/images/reward.webp`"
                max-width="200px"
                class="reward-image"
                alt=""
                contain
                eager />
            </div>
            <div class="d-flex flex-column">
              <span class="headline font-weight-bold mb-6">
                {{ $t('homepage.reward.title') }}
              </span>
              <span class="text-h6 font-weight-light mt-6 grey--text">
                {{ $t('homepage.reward.descriptionPart1') }}
              </span>
              <span class="text-h6 font-weight-light grey--text">
                {{ $t('homepage.reward.descriptionPart2') }}
              </span>
              <span class="text-h6 font-weight-light grey--text">
                {{ $t('homepage.reward.descriptionPart3') }}
              </span>
            </div>
          </div>
        </div>
        <v-img 
          :src="`${parentLocation}/static/images/reward_image.webp`"
          class="col-12 col-md-6 py-10"
          alt=""
          contain
          eager />
      </div>
      <div class="d-flex justify-center mb-16">
        <v-btn
          id="hTour-button"
          :href="tourURL"
          height="60px"
          class="px-8 primary"
          depressed
          dark
          outlined>
          <span class="headline font-weight-bold">{{ $t('discover') }}</span>
        </v-btn>
      </div>
    </v-card>
    <div class="pt-16 mt-0 mt-md-6">
      <span
        class="d-flex justify-center text-center font-weight-bold mb-11 display-1">
        {{ $t('whatInMeedForYou.title') }}
      </span>
      <v-container>
        <v-row no-gutters>
          <v-col
            cols="12"
            lg="6"
            md="12">
            <div class="my-4 my-md-0 fill-height">
              <div class="d-flex my-8">
                <v-img
                  :src="`${parentLocation}/static/images/contributor.webp`"
                  height="81px"
                  max-height="81px"
                  max-width="135px"
                  alt=""
                  contain
                  eager />
                <span class="headline d-flex align-center font-weight-bold ms-7">
                  {{ $t('contributor.title') }}
                </span>
              </div>
              <div class="d-flex flex-column">
                <span class="text-h6 grey--text py-3 font-weight-light">{{ $t('contributor.descriptionPart1') }}</span>
                <span class="text-h6 grey--text py-3 font-weight-light">{{ $t('contributor.descriptionPart2') }}</span>
                <span class="text-h6 grey--text py-3 font-weight-light">{{ $t('contributor.descriptionPart3') }}</span>
              </div>
              <div class="d-flex justify-center">
                <v-btn
                  id="hContributor-button"
                  :href="homeUrl"
                  height="60px"
                  class="px-8 my-6 primary"
                  dark
                  depressed
                  outlined>
                  <span class="headline font-weight-bold">{{ $t('explore') }}</span>
                </v-btn>
              </div>
            </div>
          </v-col>
          <v-col
            cols="12"
            lg="6"
            md="12">
            <div class="my-4 my-md-0 d-flex flex-column fill-height">
              <div class="d-flex my-8">
                <v-img
                  :src="`${parentLocation}/static/images/leader.webp`"
                  height="81px"
                  max-height="81px"
                  max-width="135px"
                  alt=""
                  contain
                  eager />
                <span class="headline d-flex align-center font-weight-bold ms-7">
                  {{ $t('leader.title') }}
                </span>
              </div>
              <div class="d-flex flex-column">
                <span class="text-h6 grey--text py-3 font-weight-light">{{ $t('leader.descriptionPart1') }}</span>
                <span class="text-h6 grey--text py-3 font-weight-light">{{ $t('leader.descriptionPart2') }}</span>
                <span class="text-h6 grey--text py-3 font-weight-light">{{ $t('leader.descriptionPart3') }}</span>
              </div>
              <v-spacer />
              <div class="d-flex justify-center">
                <v-btn
                  id="hLeader-button"
                  :href="tenantsURL"
                  height="60px"
                  class="primary px-8 mt-6 mb-15 mb-md-6"
                  depressed
                  dark
                  outlined>
                  <span class="headline font-weight-bold">{{ $t('create') }}</span>
                </v-btn>
              </div>
            </div>
          </v-col>
        </v-row>
      </v-container>
    </div>
    <div class="pt-16 mt-16">
      <span
        class="d-flex justify-center text-center font-weight-bold display-1">
        {{ $t('poweredByWeb3.title') }}
      </span>
      <v-card class="my-8 mx-4 d-block d-md-flex" flat>
        <div class="ps-3 ps-md-0 my-2 col-12 col-md-4">
          <div class="ps-0 d-flex justify-md-center justify-start my-6">
            <v-img 
              :src="`${parentLocation}/static/images/meed_circle.webp`" 
              width="100px"
              max-width="100px"
              max-height="100px"
              class="me-9"
              alt=""
              contain
              eager />
            <span class="headline d-flex align-center font-weight-bold ms-md-7">{{ $t('tokens.title') }}</span>
          </div>
          <span class="mt-3 text-h6 grey--text d-flex text-center font-weight-light">
            {{ $t('tokens.description') }}
          </span>
        </div>
        <div class="my-2 col-12 col-md-4">
          <div class="ps-0 d-flex justify-md-center justify-start my-6">
            <v-img
              :src="`${parentLocation}/static/images/hand_holding_heart.webp`" 
              height="100px"
              max-height="100px"
              max-width="100px"
              class="me-9"
              alt=""
              contain
              eager />
            <span class="headline d-flex align-center font-weight-bold ms-md-7">{{ $t('store.title') }}</span>
          </div>
          <span class="text-h6 grey--text d-flex text-center font-weight-light">
            {{ $t('store.description') }}
          </span>
        </div>
        <div class="my-2 col-12 col-md-4">
          <div class="ps-0 d-flex justify-md-center justify-start my-6">
            <v-img
              :src="`${parentLocation}/static/images/city.webp`" 
              height="100px"
              max-height="100px"
              max-width="100px"
              class="me-9"
              alt=""
              contain
              eager />
            <span class="headline d-flex align-center font-weight-bold ms-md-7">{{ $t('workMetaverse.title') }}</span>
          </div>
          <span class="text-h6 grey--text d-flex text-center font-weight-light">
            {{ $t('workMetaverse.description') }}
          </span>
        </div>
      </v-card>
      <div class="d-flex justify-center">
        <v-btn
          id="hWhitepaper-button"
          :href="whitepaperURL"
          height="60px"
          class="primary px-8 my-14"
          depressed
          dark
          outlined>
          <span class="headline font-weight-bold">{{ $t('learnMore') }}</span>
        </v-btn>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    marketplaceURL: state => state.marketplaceURL,
    tourURL: state => state.tourURL,
    whitepaperURL: state => state.whitepaperURL,
    tenantsURL: state => state.tenantsURL,
    homeUrl: state => state.homeUrl,
  }),
};
</script>