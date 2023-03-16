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
    <v-card flat>
      <v-card-title
        :class="isMobile && 'display-2' || 'display-3'"
        class="justify-center text-center font-weight-black">
        {{ $t('homePage.title') }}
      </v-card-title>
    </v-card>
    <span class="display-1 font-weight-light text-center mb-10 mt-11 mt-sm-9 grey--text">{{ $t('homePage.description') }}</span>
    <div class="mx-auto mb-15">
      <v-btn
        href="./whitepaper" 
        class="primary"
        width="319px"
        height="75px"
        depressed
        dark
        outlined>
        <span class="display-1 font-weight-bold">{{ $t('discover') }}</span>
        <v-icon class="ms-4" size="34px">fas fa-arrow-right</v-icon>
      </v-btn>
    </div>
    <v-card class="pa-5 mb-6 mt-15" flat>
      <div class="d-block d-md-flex pb-15">
        <div class="col-12 col-md-6 my-auto">
          <div class="position-absolute">
            <v-img
              :src="`/${parentLocation}/static/images/onboard.jpg`"
              max-width="200px"
              alt=""
              class="onboard-image"
              contain
              eager />    
          </div>        
          <div class="d-flex flex-column">
            <span class="display-1 font-weight-bold mb-6">
              {{ $t('homepage.onboard.title') }}
            </span>
            <span class="text-h5 font-weight-light mt-6 grey--text">
              {{ $t('homepage.onboard.descriptionPart1') }}
            </span>
            <span class="text-h5 font-weight-light grey--text">
              {{ $t('homepage.onboard.descriptionPart2') }}
            </span>
            <span class="text-h5 font-weight-light grey--text">
              {{ $t('homepage.onboard.descriptionPart3') }}
            </span>
          </div>
        </div>
        <v-img
          :src="`/${parentLocation}/static/images/onboard_image.png`"
          class="col-12 col-md-6 my-10"
          alt=""
          contain
          eager />
      </div>
      <div class="d-flex flex-column-reverse flex-md-row py-15">
        <v-img
          :src="`/${parentLocation}/static/images/organize_image.png`"
          class="col-12 col-md-6 my-10"
          alt=""
          contain
          eager />
        <div class="col-12 col-md-6 ps-md-14 my-auto">
          <div class="position-absolute">
            <v-img
              :src="`/${parentLocation}/static/images/organize.png`"
              max-width="200px"
              class="organize-image"
              alt=""
              contain
              eager />
          </div>
          <div class="d-flex flex-column">
            <span class="display-1 font-weight-bold mb-6">
              {{ $t('homepage.organize.title') }}
            </span>
            <span class="text-h5 font-weight-light mt-6 grey--text">
              {{ $t('homepage.organize.descriptionPart1') }}
            </span>
            <span class="text-h5 font-weight-light grey--text">
              {{ $t('homepage.organize.descriptionPart2') }}
            </span>
            <span class="text-h5 font-weight-light grey--text">
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
                :src="`/${parentLocation}/static/images/reward.png`"
                max-width="200px"
                class="reward-image"
                alt=""
                contain
                eager />
            </div>
            <div class="d-flex flex-column">
              <span class="display-1 font-weight-bold mb-6">
                {{ $t('homepage.reward.title') }}
              </span>
              <span class="text-h5 font-weight-light mt-6 grey--text">
                {{ $t('homepage.reward.descriptionPart1') }}
              </span>
              <span class="text-h5 font-weight-light grey--text">
                {{ $t('homepage.reward.descriptionPart2') }}
              </span>
              <span class="text-h5 font-weight-light grey--text">
                {{ $t('homepage.reward.descriptionPart3') }}
              </span>
            </div>
          </div>
        </div>
        <v-img 
          :src="`/${parentLocation}/static/images/reward_image.png`"
          class="col-12 col-md-6 py-10"
          alt=""
          contain
          eager />
      </div>
      <div class="d-flex">
        <v-btn
          href="https://builders.meeds.io"
          target="_blank"
          class="mx-auto d-flex mb-16"
          width="300px"
          height="75px"
          outlined>
          <v-icon size="34" class="me-2 dark-grey-color">fas fa-external-link-alt</v-icon>
          <span class="display-1 font-weight-bold dark-grey-color">{{ $t('try') }}</span>
        </v-btn>
      </div>
    </v-card>
    <div class="pt-16 mt-6">
      <span
        :class="isMobile && 'display-2' || 'display-3'"
        class="d-flex justify-center text-center font-weight-bold">
        {{ $t('poweredByWeb3.title') }}
      </span>
      <v-card class="my-8 mx-4 d-block d-md-flex" flat>
        <div class="ps-3 ps-md-0 my-2 col-12 col-md-4">
          <div class="ps-0 d-flex justify-md-center justify-start my-6">
            <v-img 
              :src="`/${parentLocation}/static/images/MEED-circle.png`" 
              width="100px"
              max-width="100px"
              max-height="100px"
              class="me-9"
              alt=""
              contain
              eager />
            <span class="display-1 d-flex align-center font-weight-bold ms-md-7">{{ $t('tokens.title') }}</span>
          </div>
          <span class="mt-3 text-h5 grey--text d-flex text-center font-weight-light">
            {{ $t('tokens.description') }}
          </span>
        </div>
        <div class="my-2 col-12 col-md-4">
          <div class="ps-0 d-flex justify-md-center justify-start my-6">
            <v-img
              :src="`/${parentLocation}/static/images/hand-holding-heart.png`" 
              height="100px"
              max-height="100px"
              max-width="100px"
              class="me-9"
              alt=""
              contain
              eager />
            <span class="display-1 d-flex align-center font-weight-bold ms-md-7">{{ $t('store.title') }}</span>
          </div>
          <span class="text-h5 grey--text d-flex text-center font-weight-light">
            {{ $t('store.description') }}
          </span>
        </div>
        <div class="my-2 col-12 col-md-4">
          <div class="ps-0 d-flex justify-md-center justify-start my-6">
            <v-img
              :src="`/${parentLocation}/static/images/city.png`" 
              height="100px"
              max-height="100px"
              max-width="100px"
              class="me-9"
              alt=""
              contain
              eager />
            <span class="display-1 d-flex align-center font-weight-bold ms-md-7">{{ $t('workMetaverse.title') }}</span>
          </div>
          <span class="text-h5 grey--text d-flex text-center font-weight-light">
            {{ $t('workMetaverse.description') }}
          </span>
        </div>
      </v-card>
      <div class="d-flex">
        <v-btn
          href="./whitepaper" 
          class="primary mx-auto my-14"
          width="319px"
          height="75px"
          depressed
          dark
          outlined>
          <span class="display-1 font-weight-bold">{{ $t('learnMore') }}</span>
        </v-btn>
      </div>
    </div>
    <div class="pt-16 mt-16">
      <span
        :class="isMobile && 'display-2' || 'display-3'"
        class="d-flex justify-center text-center font-weight-bold mb-11">
        {{ $t('whatInMeedForYou.title') }}
      </span>
      <v-container>
        <v-row no-gutters>
          <v-col
            cols="12"
            lg="6"
            md="12">
            <div class="my-4 my-md-0 d-flex flex-column fill-height">
              <div class="d-flex my-8">
                <v-img
                  :src="`/${parentLocation}/static/images/leader.png`"
                  height="81px"
                  max-height="81px"
                  max-width="135px"
                  alt=""
                  contain
                  eager />
                <span class="display-1 d-flex align-center font-weight-bold ms-7">
                  {{ $t('leader.title') }}
                </span>
              </div>
              <div class="d-flex flex-column">
                <span class="text-h5 grey--text py-3 font-weight-light">{{ $t('leader.descriptionPart1') }}</span>
                <span class="text-h5 grey--text py-3 font-weight-light">{{ $t('leader.descriptionPart2') }}</span>
                <span class="text-h5 grey--text py-3 font-weight-light">{{ $t('leader.descriptionPart3') }}</span>
              </div>
              <v-spacer />
              <v-btn
                href="./tenants"
                class="primary mx-auto my-t mb-15"
                width="319px"
                height="75px"
                depressed
                dark
                outlined>
                <span class="display-1 font-weight-bold">{{ $t('getAHub') }}</span>
              </v-btn>
            </div>
          </v-col>
          <v-col
            cols="12"
            lg="6"
            md="12">
            <div class="my-4 my-md-0 fill-height">
              <div class="d-flex my-8">
                <v-img
                  :src="`/${parentLocation}/static/images/contributor.png`"
                  height="81px"
                  max-height="81px"
                  max-width="135px"
                  alt=""
                  contain
                  eager />
                <span class="display-1 d-flex align-center font-weight-bold ms-7">
                  {{ $t('contributor.title') }}
                </span>
              </div>
              <div class="d-flex flex-column">
                <span class="text-h5 grey--text py-3 font-weight-light">{{ $t('contributor.descriptionPart1') }}</span>
                <span class="text-h5 grey--text py-3 font-weight-light">{{ $t('contributor.descriptionPart2') }}</span>
                <span class="text-h5 grey--text py-3 font-weight-light">{{ $t('contributor.descriptionPart3') }}</span>
              </div>
              <v-btn
                href="https://builders.meeds.io"
                target="_blank"
                class="mx-auto d-flex my-6"
                width="319px"
                height="75px"
                outlined>
                <v-icon size="34" class="me-2 dark-grey-color">fas fa-external-link-alt</v-icon>
                <span class="display-1 dark-grey-color font-weight-bold">{{ $t('trynow') }}</span>
              </v-btn>
            </div>
          </v-col>
        </v-row>
      </v-container>
    </div>
  </div>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    isMobile() {
      return this.$vuetify.breakpoint.smAndDown;
    },
  }),
};
</script>