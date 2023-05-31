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
    :class="backgroundColorClass"
    class="pa-5 mx-md-auto"
    flat>
    <div class="d-flex flex-column flex-sm-row footerLayout mx-auto">
      <div class=" py-0 d-flex flex-grow-0 flex-column px-0 mb-6 me-0 me-sm-6 col-12 col-sm-4">
        <div class="d-flex justify-center justify-sm-start mb-4">
          <v-img
            :src="`${parentLocation}/static/images/meeds.png`"
            width="77px"
            max-width="77px"
            alt=""
            contain
            eager />
        </div>
        <span 
          :class="textColor"
          class="font-size-normal text-center text-sm-start">
          {{ $t('footer.subtitle') }}</span>
        <div class="d-flex justify-center justify-sm-start mt-4">
          <v-btn
            href="https://twitter.com/IoMeeds"
            target="_blank"
            class="me-1"
            icon>
            <v-icon size="30" color="light-blue darken-1">fab fa-twitter</v-icon>
          </v-btn>
          <v-btn
            href="https://discord.com/invite/7d9Byf4Fz6"
            target="_blank"
            class="mx-1"
            icon>
            <v-icon size="30" color="indigo accent-2">fab fa-discord</v-icon>
          </v-btn>
          <v-btn
            href="https://github.com/Meeds-io"
            target="_blank"
            class="mx-1"
            icon>
            <v-icon size="30" :color="githubColor">fab fa-github</v-icon>
          </v-btn>
          <v-btn
            href="https://www.linkedin.com/company/meeds-io/"
            target="_blank"
            class="mx-1"
            icon>
            <v-icon size="30" color="light-blue darken-3">fab fa-linkedin</v-icon>
          </v-btn>
        </div>
      </div>
      <div class="py-0 d-flex flex-grow-1 flex-column full-height flex-md-row">
        <div class="col-md-6 col-12 pt-0 d-flex text-center text-md-start">
          <div class="py-0 d-flex flex-column mx-auto">
            <span
              :class="textColor"
              class="font-size-normal font-weight-black text-uppercase mb-3">
              {{ $t('product') }}</span>
            <a
              :href="tourURL"
              :class="textColor"
              class="no-decoration my-2">
              {{ $t('productTour') }}
            </a>
            <a
              :href="tenantsURL"
              :class="textColor"
              class="no-decoration">
              {{ $t('yourHub') }}
            </a>
          </div>
          <div class="py-0 d-flex flex-column mx-auto">
            <span 
              :class="textColor"
              class="font-size-normal font-weight-black text-uppercase mb-3">
              {{ $t('workMetaverse.title') }}</span>
            <a
              :href="hubsURL"
              :class="textColor"
              class="no-decoration my-2">
              {{ $t('joinHubs') }}
            </a>
            <a
              :href="tokenomicsURL"
              :class="textColor"
              class="no-decoration">
              {{ $t('meedsToken') }}
            </a>
            <a
              :href="deedsURL"
              :class="textColor"
              class="text-capitalize no-decoration my-2">
              {{ $t('becomingADeedOwner') }}
            </a>
            <a
              :href="marketplaceURL"
              :class="textColor"
              class="no-decoration text-capitalize">
              {{ $t('marketplace') }}
            </a>
          </div>
        </div>
        <div class="col-md-6 col-12 pt-0 d-flex text-center text-md-start">
          <div class="py-0 d-flex flex-column mx-auto">
            <span
              :class="textColor"
              class="font-size-normal font-weight-black text-uppercase mb-3">
              {{ $t('meedDAO') }}</span>
            <a
              :href="whitepaperURL"
              :class="textColor"
              class="no-decoration my-2">
              {{ $t('whitePaper') }}
            </a>
            <a
              :href="aboutUsURL"
              :class="textColor"
              class="no-decoration">
              {{ $t('aboutUs') }}
            </a>
            <a
              :href="legalsURL"
              :class="textColor"
              class="no-decoration my-2">
              {{ $t('legal') }}
            </a>
          </div>
          <div class="py-0 d-flex flex-column mx-auto">
            <span
              :class="textColor"
              class="font-size-normal font-weight-black text-uppercase mb-3">
              {{ $t('community') }}</span>
            <a
              :href="stakeURL"
              :class="textColor"
              class="no-decoration my-2">
              {{ $t('joinMeedsDAO') }}
            </a>
            <a
              :class="textColor"
              href="https://builders.meeds.io/"
              target="_blank"
              class="no-decoration d-flex">
              <v-icon size="16" class="me-2">fas fa-external-link-alt</v-icon>
              {{ $t('buildWithUs') }}
            </a>
            <a
              :class="textColor"
              href="https://medium.com/meeds-dao"
              target="_blank"
              class="no-decoration d-flex my-2">
              <v-icon size="16" class="me-2">fas fa-external-link-alt</v-icon>
              {{ $t('blog') }}
            </a>
          </div>
        </div>
      </div>
    </div>
    <div class="d-flex flex-column-reverse flex-sm-row align-center my-2 footerLayout mx-auto">
      <span :class="textColor" class="caption text-center text-sm-start">{{ $t('copyright') }}</span>
      <v-spacer />
      <div class="d-flex">
        <deeds-topbar-language-selector />
        <deeds-theme-button
          css-class="ms-4" 
          topbar />
      </div>
    </div>
  </v-card>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    whitepaperLink: state => state.whitepaperLink,
    marketplaceURL: state => state.marketplaceURL,
    tourURL: state => state.tourURL,
    legalsURL: state => state.legalsURL,
    stakeURL: state => state.stakeURL,
    aboutUsURL: state => state.aboutUsURL,
    tokenomicsURL: state => state.tokenomicsURL,
    tenantsURL: state => state.tenantsURL,
    deedsURL: state => state.deedsURL,
    whitepaperURL: state => state.whitepaperURL,
    hubsURL: state => state.hubsURL,
    dark: state => state.dark,
    githubColor() {
      return this.dark && 'white' || 'black';
    },
    textColor() {
      return this.dark && 'white--text' || 'text-sub-title';
    },
    backgroundColorClass() {
      return this.dark && 'dark-color' || 'light-grey-background';
    }
  }),
};
</script>