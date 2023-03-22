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
  <div class="d-flex flex-column mt-8 mt-sm-2">
    <v-card flat>
      <v-card-title class="py-0 px-0 px-sm-4 justify-center">
        <span class="text-sm-h2 display-2 font-weight-bold ms-0 ms-sm-16">{{ $t('aboutUs.title') }}</span>
        <v-spacer />
        <v-img 
          :src="`/${parentLocation}/static/images/about_us.png`"
          :max-width="isMobile && '97px' || '211px'"
          class="me-0 me-sm-16"
          alt=""
          contain
          eager />
      </v-card-title>
    </v-card>
    <div class="d-flex pt-15">
      <div class="d-flex flex-column">
        <span class="mb-5 display-1 font-weight-bold">
          {{ $t('governance.title') }}
        </span>
        <v-card
          height="8px"
          width="140px"
          color="primary no-border-radius"
          flat />
        <div class="ps-0 py-8 text-h5 grey--text font-weight-light" v-html="$t('governance.descriptionPart1', {0: '<br><br>'})"></div>
      </div>
      <v-img 
        :src="`/${parentLocation}/static/images/meeds-about-us-banner.svg`"
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
        href="mailto:contact@meeds.io" 
        width="319px"
        height="75px"
        class="primary"
        depressed
        dark
        outlined>
        <span class="display-1 font-weight-bold">{{ $t('governance.button') }}</span>
      </v-btn>
    </div>
    <div class="d-flex flex-column mb-16">
      <div class="px-0 display-1 font-weight-bold mb-12 mt-16">
        {{ $t('whatWeDo.title') }}
      </div> 
      <v-card class="d-block d-md-flex" flat>
        <v-list class="ps-0 col-12 col-md-4">
          <v-list-item class="ps-0">
            <v-list-item-icon>
              <v-img
                :src="`/${parentLocation}/static/images/meedsToken.png`"
                alt=""
                width="75px"
                contain
                eager />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title>
                <span class="pb-4 display-1 font-weight-bold">{{ $t('whatWeDo.meedToken') }}</span>
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <span class="pb-4 text-h5 grey--text font-weight-light">
                {{ $t('meedToken.description') }}
              </span>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <v-btn
                href="./overview"
                max-width="200px"
                height="50px"
                class="dark-grey-border-color rounded-pill dark-grey-color"
                outlined>
                <span class="text-h6 font-weight-bold">{{ $t('tokenomics') }}</span>
              </v-btn>
            </v-list-item-content>
          </v-list-item>
        </v-list>
        <v-list class="ps-0 col-12 col-md-4">
          <v-list-item class="ps-0">
            <v-list-item-icon>
              <v-img
                :src="`/${parentLocation}/static/images/meedsToken.png`"
                alt=""
                width="75px"
                contain
                eager />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title>
                <span class="pb-4 display-1 font-weight-bold">{{ $t('meedSoftware') }}</span>
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <span class="pb-4 text-h5 grey--text font-weight-light">
                {{ $t('meedSoftware.description') }}
              </span>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <v-btn
                href="https://builders.meeds.io"
                target="_blank"
                max-width="200px"
                height="50px"
                class="dark-grey-border-color rounded-pill dark-grey-color"
                outlined>
                <span class="text-h6 font-weight-bold">{{ $t('discover') }}</span>
              </v-btn>
            </v-list-item-content>
          </v-list-item>
        </v-list>
        <v-list class="ps-0 col-12 col-md-4">
          <v-list-item class="ps-0">
            <v-list-item-icon>
              <v-img
                :src="`/${parentLocation}/static/images/deed.png`"
                alt=""
                width="75px"
                contain
                eager />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title>
                <span class="pb-4 display-1 font-weight-bold">{{ $t('deedsServices') }}</span>
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <span class="pb-4 text-h5 grey--text font-weight-light">
                {{ $t('deedsServices.description') }}
              </span>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <v-btn
                href="./owners"
                max-width="200px"
                height="50px"
                class="dark-grey-border-color rounded-pill dark-grey-color"
                outlined>
                <span class="text-h6 font-weight-bold">{{ $t('learnMore') }}</span>
              </v-btn>
            </v-list-item-content>
          </v-list-item>
        </v-list>
      </v-card>
    </div>
    <div class="d-flex flex-column my-16">
      <v-card class="light-grey-background py-5" flat>
        <div class="d-flex">
          <div>
            <div class="pt-4 px-4 display-1 font-weight-bold">
              {{ $t('sustainability') }}
            </div>
            <div class="px-4 pb-2 pt-3 display-1 font-weight-bold grey--text">
              {{ $t('duringTheMintingPeriod') }}
            </div>
            <div class="grey--text text-h6 font-weight-light pa-4">
              {{ $t('duringTheMintingPeriod.descriptionPart1') }}
            </div>
            <div class="grey--text text-h6 font-weight-light pa-4">
              {{ $t('duringTheMintingPeriod.descriptionPart2') }}
            </div>
            <div class="px-4 pb-2 pt-3 display-1 font-weight-bold grey--text">
              {{ $t('afterTheMintingPeriod') }}
            </div>
            <div class="grey--text text-h6 font-weight-light pa-4">
              {{ $t('afterTheMintingPeriod.descriptionPart1') }}
            </div>
            <div class="grey--text text-h6 font-weight-light pa-4">
              {{ $t('afterTheMintingPeriod.descriptionPart2') }}
            </div>
          </div>
          <v-img
            :src="`/${parentLocation}/static/images/logo-m-grand.svg`"
            alt=""
            width="350px"
            class="logo-m hidden-md-and-down mt-n3" />
        </div>
      </v-card>
    </div>
    <div class="d-flex flex-column mt-16">
      <div class="px-0 display-1 font-weight-bold">
        {{ $t('foundingMembers') }}
      </div> 
      <v-container>
        <v-row no-gutters>
          <v-col
            cols="12"
            lg="4"
            md="12">
            <div class="my-4 my-md-0 d-flex flex-column fill-height">
              <div class="my-8">
                <v-img
                  :src="`/${parentLocation}/static/images/logo-exo.png`"
                  alt=""
                  width="112px"
                  contain
                  eager />
              </div>
              <div class="py-4">
                <span class="pb-4 grey--text text-h6 font-weight-light">
                  {{ $t('exoPlatform.description') }}
                </span>
              </div>
              <v-spacer />
              <v-btn
                href="https://exoplatform.com"
                target="_blank"
                width="125px"
                height="50px"
                class="dark-grey-border-color rounded-pill dark-grey-color my-10 my-sm-0"
                outlined>
                <span class="text-h6 font-weight-bold">{{ $t('visit') }}</span>
              </v-btn>
            </div>
          </v-col>
          <v-col
            cols="12"
            lg="4"
            md="12">
            <div class="my-4 my-md-0 d-flex flex-column fill-height">
              <div class="my-8">
                <v-img
                  :src="`/${parentLocation}/static/images/logo-Bonitasoft.png`"
                  alt=""
                  width="225px"
                  contain
                  eager />
              </div>
              <div class="py-4">
                <span class="pb-4 grey--text text-h6 font-weight-light">
                  {{ $t('bonitaSoft.description') }}
                </span>
              </div>
              <v-spacer />
              <v-btn
                href="https://bonitasoft.com"
                target="_blank"
                width="125px"
                height="50px"
                class="dark-grey-border-color rounded-pill dark-grey-color my-10 my-sm-0"
                outlined>
                <span class="text-h6 font-weight-bold">{{ $t('visit') }}</span>
              </v-btn>
            </div>
          </v-col>
          <v-col
            cols="12"
            lg="4"
            md="12">
            <div class="my-4 my-md-0 d-flex flex-column fill-height">
              <div class="my-5">
                <v-img
                  :src="`/${parentLocation}/static/images/Newlogic-Impact-Lab-logo.png`"
                  alt=""
                  width="156px"
                  contain
                  eager />
              </div>
              <div class="py-4">
                <span class="pb-4 grey--text text-h6 font-weight-light">
                  {{ $t('newlogic.description') }}
                </span>
              </div>
              <v-spacer />
              <v-btn
                href="https://newlogic.com"
                target="_blank"
                width="125px"
                height="50px"
                class="dark-grey-border-color rounded-pill dark-grey-color my-10 my-sm-0"
                outlined>
                <span class="text-h6 font-weight-bold">{{ $t('visit') }}</span>
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