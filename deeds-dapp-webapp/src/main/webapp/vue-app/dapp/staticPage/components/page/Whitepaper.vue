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
  <div class="d-flex flex-column mt-8">
    <v-card class="mb-3" flat>
      <v-card-title class="py-0 justify-center flex-nowrap">
        <span class="col-12 col-lg-8 col-md-7 ps-0 text-sm-h2 display-2 font-weight-bold text-center text-md-start">{{ $t('whitepaper.title') }}</span>
        <v-spacer />
        <v-img 
          :src="`${parentLocation}/static/images/whitepaper_banner.webp`"
          max-width="350px"
          class="hidden-sm-and-down"
          alt=""
          contain
          eager />
      </v-card-title>
    </v-card>
    <div class="d-flex flex-column my-16 pb-16">
      <v-card class="pb-5 pt-8 pt-md-10" flat>
        <div class="d-flex position-absolute mt-n16">
          <v-img
            :src="`${parentLocation}/static/images/bulle_grey_background.png`"
            :height="isMobile && '100%' || 'auto'"
            :class="opacityClass"
            alt="" /> 
          <v-img
            :src="`${parentLocation}/static/images/logo_m.webp`"
            alt=""
            width="450px"
            class="logo-m hidden-md-and-down mt-7" />
        </div>
        <div class="position-relative">
          <v-card-subtitle 
            :class="textColor"
            class="text-h5 font-weight-light py-2">
            {{ $t('whitepaper.descriptionPart1') }}
          </v-card-subtitle>
          <v-card-subtitle 
            :class="textColor"
            class="text-h5 font-weight-light py-7">
            {{ $t('whitepaper.descriptionPart2') }}
          </v-card-subtitle>
          <v-card-subtitle 
            :class="textColor"
            class="text-h5 font-weight-light py-2">
            {{ $t('whitepaper.descriptionPart3') }}
          </v-card-subtitle>
          <v-card-subtitle 
            :class="textColor"
            class="text-h5 font-weight-light py-7">
            {{ $t('whitepaper.descriptionPart4') }}
          </v-card-subtitle>
          <v-card-subtitle 
            :class="textColor"
            class="text-h5 font-weight-light py-2">
            {{ $t('whitepaper.descriptionPart5') }}
          </v-card-subtitle>
          <div class="d-flex justify-center pt-3 pb-md-7">
            <v-btn
              :href="whitepaperLink" 
              target="_blank"
              height="70px"
              class="primary px-8"
              depressed
              dark
              outlined>
              <span class="display-1 font-weight-bold">{{ $t('readWhitepaper') }}</span>
            </v-btn>
          </div> 
        </div>  
      </v-card>
    </div>
    <div class="d-flex flex-column mt-5">
      <span class="display-1 font-weight-bold">
        {{ $t('enterTheWorkMetaverse.title') }}
      </span> 
      <v-card
        height="8px"
        width="140px"
        color="primary no-border-radius"
        class="mt-4 mb-2"
        flat />
      <v-card class="d-block d-md-flex" flat>
        <v-list class="ps-0 col-12 col-md-4">
          <v-list-item class="ps-0 pb-2">
            <v-list-item-icon>
              <v-img
                :src="`${parentLocation}/static/images/meed_circle.webp`"
                class="my-5"
                alt=""
                width="75px"
                height="75px"
                contain
                eager />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title>
                <span class="pb-4 display-1 font-weight-bold">{{ $t('meed') }}</span>
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <span 
                :class="textColor"
                class="pb-4 text-h5 font-weight-light">
                {{ $t('meed.descriptionPart1') }}
              </span>
              <span 
                :class="textColor"
                class="pb-4 text-h5 font-weight-light">{{ $t('meed.descriptionPart2') }}</span>
            </v-list-item-content>
          </v-list-item>
        </v-list>
        <v-list class="ps-0 col-12 col-md-4">
          <v-list-item class="ps-0 pb-2">
            <v-list-item-icon>
              <v-img
                :src="`${parentLocation}/static/images/deed_icon.webp`"
                class="my-5"
                alt=""
                width="75px"
                height="75px"
                contain
                eager />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title>
                <span class="pb-4 display-1 font-weight-bold">{{ $t('deed') }}</span>
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <span 
                :class="textColor"
                class="pb-4 text-h5 font-weight-light">{{ $t('deed.descriptionPart1') }}</span>
              <span 
                :class="textColor"
                class="pb-4 text-h5 font-weight-light">{{ $t('deed.descriptionPart2') }}</span>
            </v-list-item-content>
          </v-list-item>
        </v-list>
        <v-list class="ps-0 col-12 col-md-4">
          <v-list-item class="ps-0 pb-2">
            <v-list-item-icon>
              <v-img
                :src="`${parentLocation}/static/images/xeed_icon.webp`"
                class="my-5"
                alt=""
                width="75px"
                height="75px"
                contain
                eager />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title>
                <span class="pb-4 display-1 font-weight-bold">{{ $t('xeed') }}</span>
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <span 
                :class="textColor"
                class="pb-4 text-h5 font-weight-light">{{ $t('xeed.descriptionPart1') }}</span>
              <span 
                :class="textColor"
                class="pb-4 text-h5 font-weight-light">{{ $t('xeed.descriptionPart2') }}</span>
            </v-list-item-content>
          </v-list-item>
        </v-list>
      </v-card>
      <div class="d-flex justify-end mb-16 pb-16">
        <v-card
          height="8px"
          width="140px"
          color="secondary no-border-radius"
          flat />
      </div>
    </div>
    <div class="ps-0 py-3 font-weight-light display-1">{{ $t('invest.descriptionPart1') }}</div>
    <div class="ps-0 py-3 font-weight-light display-1">{{ $t('invest.descriptionPart2') }}</div>
    <div class="d-flex flex-column flex-md-row py-16">
      <div class="d-flex flex-column ps-5"> 
        <div class="d-flex justify-center flex-wrap justify-md-start mb-16">
          <v-btn
            :dark="!pageState || pageState === 'work'"
            :text="pageState === 'capital'"
            :class="(!pageState || pageState === 'work') && 'primary' || (pageState === 'capital' && 'primary--text primary-border-color')"
            height="52px"
            class="ma-4 px-5"
            outlined
            @click="$store.commit('setPageState','work')">
            <span class="text-h5 font-weight-bold">{{ $t('investWork') }}</span>
          </v-btn>
          <v-btn
            :dark="pageState === 'capital'"
            :text="!pageState || pageState === 'work'"
            :class="pageState === 'capital' && 'primary' || ((!pageState || pageState === 'work') && 'primary--text primary-border-color')"
            height="52px"
            class="ma-4 px-5"
            outlined
            @click="$store.commit('setPageState','capital')">
            <span class="text-h5 font-weight-bold">{{ $t('investCapital') }}</span>
          </v-btn>
        </div>
        <div>
          <div v-if="!pageState || pageState === 'work'" class="d-flex flex-column">
            <span :class="textColor" class="ps-0 pt-4 pb-2 text-h5 font-weight-light">{{ $t('investWork.descriptionPart1') }}</span>
            <span :class="textColor" class="ps-0 py-2 text-h5 font-weight-light">{{ $t('investWork.descriptionPart2') }}</span>
            <span :class="textColor" class="ps-0 py-2 text-h5 font-weight-light">{{ $t('investWork.descriptionPart3') }}</span>
            <span :class="textColor" class="ps-0 pt-2 pb-7 text-h5 font-weight-light">{{ $t('investWork.descriptionPart4') }}</span>
          </div>
          <div v-else class="d-flex flex-column">
            <span :class="textColor" class="ps-0 pt-4 pb-2 text-h5 font-weight-light">{{ $t('investCapital.descriptionPart1') }}</span>
            <span :class="textColor" class="ps-0 py-2 text-h5 font-weight-light">{{ $t('investCapital.descriptionPart2') }}</span>
            <span :class="textColor" class="ps-0 py-2 text-h5 font-weight-light">{{ $t('investCapital.descriptionPart3') }}</span>
            <span :class="textColor" class="ps-0 py-2 text-h5 font-weight-light">{{ $t('investCapital.descriptionPart4') }}</span>
            <span :class="textColor" class="ps-0 pt-2 pb-7 text-h5 font-weight-light">{{ $t('investCapital.descriptionPart5') }}</span>
          </div>
          <div class="position-absolute">
            <v-img
              :src="`${parentLocation}/static/images/invest_image.webp`"
              max-width="288px"
              class="invest-image low-opacity"
              alt=""
              contain
              eager />    
          </div>
        </div>
      </div>
      <div class="d-flex justify-center justify-md-start">
        <v-img 
          v-if="!pageState || pageState === 'work'"
          :src="`${parentLocation}/static/images/work_investment.webp`"
          width="564px"
          alt=""
          contain
          eager />
        <v-img 
          v-else
          :src="`${parentLocation}/static/images/capital_investment.webp`"
          width="564px"
          alt=""
          contain
          eager />
      </div>
    </div>
    <div class="d-flex justify-center  mb-16">
      <v-btn
        v-if="!pageState || pageState === 'work'"
        href="https://builders.meeds.io"
        target="_blank"
        height="70px"
        class="primary px-8"
        dark
        outlined>
        <span class="display-1 font-weight-bold">{{ $t('startEarning') }}</span>
      </v-btn>
      <v-btn
        v-else
        :href="portfolioURL"
        height="70px"
        class="primary px-8"
        dark
        outlined>
        <span class="display-1 font-weight-bold">{{ $t('investInMeeds') }}</span>
      </v-btn>
    </div>
  </div>
</template>
<script>
export default {
  computed: Vuex.mapState({
    pageState: state => state.pageState,
    parentLocation: state => state.parentLocation,
    whitepaperLink: state => state.whitepaperLink,
    portfolioURL: state => state.portfolioURL,
    dark: state => state.dark,
    isMobile() {
      return this.$vuetify.breakpoint.xsOnly;
    },
    opacityClass() {
      return this.dark && 'low-opacity' || 'half-opacity';
    },
    textColor() {
      return this.dark && 'white--text' || 'grey--text';
    },
  }),
};
</script>