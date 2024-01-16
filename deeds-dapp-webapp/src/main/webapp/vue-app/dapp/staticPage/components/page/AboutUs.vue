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
  <div class="d-flex flex-column mt-8 mt-sm-10">
    <v-card flat>
      <deeds-page-title-layout>
        <template #title>
          {{ $t('page.aboutUs.title') }}
        </template>
        <template #subtitle>
          {{ $t('page.aboutUs.subtitle') }}
        </template>
      </deeds-page-title-layout>
    </v-card>
    <div class="d-flex pt-4 pt-md-11 ps-0 ps-sm-2">
      <div class="d-flex flex-column">
        <span class="mb-5 display-1 font-weight-bold">
          {{ $t('governance.title') }}
        </span>
        <v-card
          height="8px"
          width="140px"
          color="primary no-border-radius"
          flat />
        <div 
          :class="textColor" 
          class="ps-0 py-8 text-h6 font-weight-light" 
          v-html="$t('governance.descriptionPart1', {0: '<br><br>'})"></div>
      </div>
      <v-img 
        :src="`${parentLocation}/static/images/governance_image.svg`"
        width="486px"
        class="hidden-md-and-down"
        alt=""
        contain
        eager />
    </div>
    <div class="d-flex justify-end">
      <v-card
        height="8px"
        width="140px"
        color="secondary no-border-radius"
        flat />
    </div>
    <div class="d-flex justify-center my-15">
      <v-btn
        id="aContact-button"
        href="mailto:contact@meeds.io" 
        height="60px"
        class="primary px-8"
        depressed
        dark
        outlined>
        <span class="headline font-weight-bold">{{ $t('governance.button') }}</span>
      </v-btn>
    </div>
    <div class="d-flex flex-column mb-16 ps-0 ps-sm-2">
      <div class="px-0 display-1 font-weight-bold mb-12 mt-16">
        {{ $t('whatWeDo.title') }}
      </div> 
      <v-card class="d-block d-md-flex" flat>
        <v-list class="ps-0 col-12 col-md-4">
          <v-list-item class="ps-0">
            <v-list-item-icon>
              <v-img
                :src="`${parentLocation}/static/images/meed_circle.webp`"
                class="mt-3 mb-4"
                alt=""
                width="75px"
                contain
                eager />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title>
                <span class="pb-4 headline font-weight-bold">{{ $t('whatWeDo.meedToken') }}</span>
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <span :class="textColor" class="pb-4 text-h6 font-weight-light">
                {{ $t('meedToken.description') }}
              </span>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-action>
              <v-btn
                id="aToken-button"
                :href="portfolioURL"
                height="54px"
                class="px-7"
                color="primary"
                outlined>
                <span class="text-h6 font-weight-bold">{{ $t('tokenomics') }}</span>
              </v-btn>
            </v-list-item-action>
          </v-list-item>
        </v-list>
        <v-list class="ps-0 col-12 col-md-4">
          <v-list-item class="ps-0">
            <v-list-item-icon>
              <v-img
                :src="`${parentLocation}/static/images/meeds_software.webp`"
                alt=""
                width="75px"
                contain
                eager />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title>
                <span class="pb-4 headline font-weight-bold">{{ $t('meedSoftware') }}</span>
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <span :class="textColor" class="pb-4 text-h6 font-weight-light">
                {{ $t('meedSoftware.description') }}
              </span>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-action>
              <v-btn
                id="aTour-button"
                :href="tourURL"
                height="54px"
                class="px-7"
                color="primary"
                outlined>
                <span class="text-h6 font-weight-bold">{{ $t('discover') }}</span>
              </v-btn>
            </v-list-item-action>
          </v-list-item>
        </v-list>
        <v-list class="ps-0 col-12 col-md-4">
          <v-list-item class="ps-0 my-5">
            <v-list-item-icon>
              <v-img
                :src="`${parentLocation}/static/images/deed_icon.webp`"
                alt=""
                width="75px"
                contain
                eager />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title>
                <span class="pb-4 headline font-weight-bold">{{ $t('deedsServices') }}</span>
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <span :class="textColor" class="pb-4 text-h6 font-weight-light">
                {{ $t('deedsServices.description') }}
              </span>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-action>
              <v-btn
                id="aWhitepaper-button"
                :href="whitepaperURL"
                height="54px"
                class="px-7"
                color="primary"
                outlined>
                <span class="text-h6 font-weight-bold">{{ $t('learnMore') }}</span>
              </v-btn>
            </v-list-item-action>
          </v-list-item>
        </v-list>
      </v-card>
    </div>
    <div class="d-flex flex-column my-16">
      <v-card class="py-5" flat>
        <div class="d-flex position-absolute">
          <v-img
            :src="`${parentLocation}/static/images/bulle_grey_background.png`"
            :class="opacityClass"
            alt="" /> 
          
          <v-img
            :src="`${parentLocation}/static/images/logo_m.webp`"
            alt=""
            width="470px"
            class="logo-m hidden-md-and-down mt-n3" />
        </div>
        <div class="position-relative">
          <div class="pt-4 px-4 display-1 font-weight-bold">
            {{ $t('sustainability') }}
          </div>
          <div class="px-4 pb-2 pt-7 headline font-weight-light">
            {{ $t('duringTheMintingPeriod') }}
          </div>
          <div :class="textColor" class="text-h6 font-weight-light pa-4">
            {{ $t('duringTheMintingPeriod.descriptionPart1') }}
          </div>
          <div :class="textColor" class="text-h6 font-weight-light pa-4">
            {{ $t('duringTheMintingPeriod.descriptionPart2') }}
          </div>
          <div class="px-4 pb-2 pt-3 headline font-weight-light">
            {{ $t('afterTheMintingPeriod') }}
          </div>
          <div :class="textColor" class="text-h6 font-weight-light pa-4">
            {{ $t('afterTheMintingPeriod.descriptionPart1') }}
          </div>
          <div :class="textColor" class="ext text-h6 font-weight-light pa-4">
            {{ $t('afterTheMintingPeriod.descriptionPart2') }}
          </div>
        </div>
      </v-card>
    </div>
    <div class="d-flex flex-column mt-16 ps-0 ps-sm-2">
      <div class="px-0 display-1 font-weight-bold">
        {{ $t('foundingMembers') }}
      </div> 
      <v-container>
        <v-row class="pb-4" no-gutters>
          <v-col
            cols="12"
            lg="6"
            md="12">
            <div class="my-4 my-md-0 d-flex flex-column fill-height mx-0 mx-md-4">
              <div class="my-8 mx-auto">
                <v-img
                  :src="`${parentLocation}/static/images/logo_bonitasoft.webp`"
                  alt=""
                  width="225px"
                  contain
                  eager />
              </div>
              <div class="py-4 text-center">
                <span :class="textColor" class="pb-4 text-h6 font-weight-light">
                  {{ $t('bonitaSoft.description') }}
                </span>
              </div>
              <v-spacer />
              <div class="mb-10 mb-sm-0 mx-auto">
                <v-btn
                  id="aBonita-button"
                  href="https://bonitasoft.com"
                  target="_blank"
                  height="54px"
                  class="px-7"
                  color="primary"
                  outlined>
                  <span class="text-h6 font-weight-bold">{{ $t('visit') }}</span>
                </v-btn>
              </div>
            </div>
          </v-col>
          <v-col
            cols="12"
            lg="6"
            md="12">
            <div class="my-4 my-md-0 d-flex flex-column fill-height">
              <div class="my-6 mx-auto">
                <v-img
                  src="https://res.cloudinary.com/dcooc6vig/image/upload/v1696854039/meedsdao-site/assets/images/Ceres-Advisory.png"
                  alt=""
                  width="150px"
                  contain
                  eager />
              </div>
              <div class="py-4 text-center">
                <span :class="textColor" class="pb-4 text-h6 font-weight-light">
                  {{ $t('CeresAdvisory.description') }}
                </span>
              </div>
              <v-spacer />
              <div class="mb-10 mb-sm-0 mx-auto">
                <v-btn
                  id="aCeres-button"
                  href="https://www.ceres-advisory.io/"
                  target="_blank"
                  height="54px"
                  class="px-7"
                  color="primary"
                  outlined>
                  <span class="text-h6 font-weight-bold">{{ $t('visit') }}</span>
                </v-btn>
              </div>
            </div>
          </v-col>
        </v-row>
        <v-row class="pt-4" no-gutters>
          <v-col
            cols="12"
            lg="6"
            md="12">
            <div class="my-4 my-md-0 d-flex flex-column fill-height mx-0 mx-md-4">
              <div class="my-8 mx-auto">
                <v-img
                  :src="`${parentLocation}/static/images/logo_exo.webp`"
                  alt=""
                  width="112px"
                  contain
                  eager />
              </div>
              <div class="py-4 text-center">
                <span :class="textColor" class="pb-4 text-h6 font-weight-light">
                  {{ $t('exoPlatform.description') }}
                </span>
              </div>
              <v-spacer />
              <div class="mb-10 mb-sm-0 mx-auto">
                <v-btn
                  id="aExo-button"
                  href="https://exoplatform.com"
                  target="_blank"
                  height="54px"
                  class="px-7"
                  color="primary"
                  outlined>
                  <span class="text-h6 font-weight-bold">{{ $t('visit') }}</span>
                </v-btn>
              </div>
            </div>
          </v-col>
          <v-col
            cols="12"
            lg="6"
            md="12">
            <div
              class="my-4 my-md-0 d-flex flex-column fill-height">
              <div class="my-5 mx-auto">
                <v-img
                  :src="meedsLabLogo"
                  alt=""
                  width="122px"
                  contain
                  eager />
              </div>
              <div class="py-4 text-center">
                <span :class="textColor" class="pb-4 text-h6 font-weight-light">
                  {{ $t('meedsLab.description') }}
                </span>
              </div>
              <v-spacer />
              <div class="mb-10 mb-sm-0 mx-auto">
                <v-btn
                  id="aMeedsLab-button"
                  href="https://builders.meeds.io"
                  target="_blank"
                  height="54px"
                  class="px-7"
                  color="primary"
                  outlined>
                  <span class="text-h6 font-weight-bold">{{ $t('visit') }}</span>
                </v-btn>
              </div>
            </div>
          </v-col>
        </v-row>
        <v-row class="pt-4" no-gutters>
          <v-col
            class="mx-auto"
            cols="12"
            lg="6"
            md="12">
            <div
              class="my-4 my-md-0 d-flex flex-column fill-height">
              <div class="my-5 mx-auto">
                <v-img
                  :src="newlogicLogo"
                  alt=""
                  width="116px"
                  contain
                  eager />
              </div>
              <div class="py-4 text-center">
                <span :class="textColor" class="pb-4 text-h6 font-weight-light">
                  {{ $t('newlogic.description') }}
                </span>
              </div>
              <v-spacer />
              <div class="mb-10 mb-sm-0 mx-auto">
                <v-btn
                  id="aNewlogic-button"
                  href="https://newlogic.com"
                  target="_blank"
                  height="54px"
                  class="px-7"
                  color="primary"
                  outlined>
                  <span class="text-h6 font-weight-bold">{{ $t('visit') }}</span>
                </v-btn>
              </div>
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
    portfolioURL: state => state.portfolioURL,
    whitepaperURL: state => state.whitepaperURL,
    tourURL: state => state.tourURL,
    dark: state => state.dark,
    opacityClass() {
      return this.dark && 'low-opacity' || 'half-opacity';
    },
    textColor() {
      return this.dark && 'white--text' || 'grey--text';
    },
    newlogicLogo() {
      return this.dark && 'https://res.cloudinary.com/dcooc6vig/image/upload/v1697620494/meedsdao-site/assets/images/newlogic-dark.png' || 'https://res.cloudinary.com/dcooc6vig/image/upload/v1697620472/meedsdao-site/assets/images/newlogic-light.png';
    },
    meedsLabLogo() {
      return this.dark && 'https://res.cloudinary.com/dcooc6vig/image/upload/v1697621445/meedsdao-site/assets/images/meedslab-dark.png' || 'https://res.cloudinary.com/dcooc6vig/image/upload/v1697621474/meedsdao-site/assets/images/meedslab-light.png';
    }
  }),
};
</script>