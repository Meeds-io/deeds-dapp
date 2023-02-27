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
  <div class="d-flex flex-column mt-8 mt-sm-10">
    <v-card flat>
      <v-card-title class="py-0 justify-center font-weight-bold dark-grey-color">{{ $t('aboutUs.title') }}</v-card-title>
    </v-card>
    <div class="d-flex py-4">
      <div class="d-flex flex-column">
        <div class="ps-0 py-0 v-card__title font-weight-bold dark-grey-color">
          {{ $t('governance.title') }}
        </div> 
        <div class="ps-0 py-4 dark-grey-color v-card__text" v-html="$t('governance.descriptionPart1', {0:'<strong>', 1: '</strong>', 2: '<br><br>'})"></div>
        <div class="d-flex justify-center justify-md-start">
          <v-btn
            href="mailto:contact@meeds.io" 
            max-width="156px"
            class="dark-grey-border-color rounded-pill dark-grey-color d-flex justify-center" 
            outlined>
            <span class="subtitle-2 font-weight-normal">{{ $t('governance.button') }}</span>
          </v-btn>
        </div>
      </div>
      <v-img 
        :src="`/${parentLocation}/static/images/meeds-about-us-banner.svg`"
        width="486px"
        class="hidden-md-and-down"
        alt=""
        contain
        eager />
    </div>
    <div class="d-flex flex-column">
      <div class="px-0 v-card__title font-weight-bold dark-grey-color">
        {{ $t('whatWeDo.title') }}
      </div> 
      <v-card class="d-block d-md-flex" flat>
        <v-list class="ps-0 col-12 col-md-4">
          <v-list-item class="ps-0">
            <v-list-item-icon>
              <v-img
                :src="`/${parentLocation}/static/images/meedsToken.png`"
                alt=""
                width="47px"
                contain
                eager />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title class="d-flex flex-column">
                <span class="pb-4 dark-grey-color">{{ $t('whatWeDo.meedToken') }}</span>
                <v-btn
                  href="./overview"
                  max-width="94px"
                  class="dark-grey-border-color rounded-pill dark-grey-color"
                  small
                  outlined>
                  <span class="caption">{{ $t('tokenomics') }}</span>
                </v-btn>
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <span class="pb-4 dark-grey-color">
                {{ $t('meedToken.description') }}
              </span>
            </v-list-item-content>
          </v-list-item>
        </v-list>
        <v-list class="ps-0 col-12 col-md-4">
          <v-list-item class="ps-0">
            <v-list-item-icon>
              <v-img
                :src="`/${parentLocation}/static/images/meedsToken.png`"
                alt=""
                width="47px"
                contain
                eager />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title class="d-flex flex-column">
                <span class="pb-4 dark-grey-color">{{ $t('meedSoftware') }}</span>
                <v-btn
                  href="https://builders.meeds.io"
                  target="_blank"
                  max-width="94px"
                  class="dark-grey-border-color rounded-pill dark-grey-color"
                  small
                  outlined>
                  <span class="caption">{{ $t('discover') }}</span>
                </v-btn>
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <span class="pb-4 dark-grey-color">
                {{ $t('meedSoftware.description') }}
              </span>
            </v-list-item-content>
          </v-list-item>
        </v-list>
        <v-list class="ps-0 col-12 col-md-4">
          <v-list-item class="ps-0">
            <v-list-item-icon>
              <v-img
                :src="`/${parentLocation}/static/images/deed.png`"
                alt=""
                width="47px"
                contain
                eager />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title class="d-flex flex-column">
                <span class="pb-4 dark-grey-color">{{ $t('deedsServices') }}</span>
                <v-btn
                  href="./owners"
                  max-width="94px"
                  class="dark-grey-border-color rounded-pill dark-grey-color"
                  small
                  outlined>
                  <span class="caption">{{ $t('learnMore') }}</span>
                </v-btn>
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <span class="pb-4 dark-grey-color">
                {{ $t('deedsServices.description') }}
              </span>
            </v-list-item-content>
          </v-list-item>
        </v-list>
      </v-card>
    </div>
    <div class="d-flex flex-column">
      <v-card class="light-grey-background py-5" flat>
        <div class="d-flex">
          <div>
            <v-card-title class="pb-0 font-weight-bold dark-grey-color">
              {{ $t('sustainability') }}
            </v-card-title>
            <h4 class="px-4 pb-2 pt-3 font-weight-normal dark-grey-color">
              {{ $t('duringTheMintingPeriod') }}
            </h4>
            <v-card-subtitle class="dark-grey-color py-2">
              {{ $t('duringTheMintingPeriod.descriptionPart1') }}
            </v-card-subtitle>
            <v-card-subtitle class="dark-grey-color py-2">
              {{ $t('duringTheMintingPeriod.descriptionPart2') }}
            </v-card-subtitle>
            <h4 class="px-4 pb-2 pt-3 font-weight-normal dark-grey-color">
              {{ $t('afterTheMintingPeriod') }}
            </h4>
            <v-card-subtitle class="dark-grey-color py-2">
              {{ $t('afterTheMintingPeriod.descriptionPart1') }}
            </v-card-subtitle>
            <v-card-subtitle class="dark-grey-color pt-2 pb-4">
              {{ $t('afterTheMintingPeriod.descriptionPart2') }}
            </v-card-subtitle>
          </div>
          <v-img
            :src="`/${parentLocation}/static/images/logo-m-grand.svg`"
            alt=""
            width="350px"
            class="logo-m hidden-md-and-down mt-n3" />
        </div>
      </v-card>
    </div>
    <div class="d-flex flex-column">
      <div class="px-0 v-card__title font-weight-bold dark-grey-color">
        {{ $t('foundingMembers') }}
      </div> 
      <v-card class="d-block d-md-flex" flat>
        <v-list class="col-12 col-md-4 ps-0">
          <v-list-item class="ps-0">
            <v-list-item-icon>
              <v-img
                :src="`/${parentLocation}/static/images/logo-exo.png`"
                alt=""
                width="80px"
                contain
                eager />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title class="d-flex flex-column">
                <span class="pb-4 dark-grey-color">{{ $t('exoPlatform') }}</span>
                <v-btn
                  href="https://exoplatform.com"
                  target="_blank"
                  max-width="94px"
                  class="dark-grey-border-color rounded-pill dark-grey-color"
                  small
                  outlined>
                  <span class="caption">{{ $t('visit') }}</span>
                </v-btn>
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <span class="pb-4 dark-grey-color">
                {{ $t('exoPlatform.description') }}
              </span>
            </v-list-item-content>
          </v-list-item>
        </v-list>
        <v-list class="col-12 col-md-4 ps-0">
          <v-list-item class="ps-0">
            <v-list-item-icon>
              <v-img
                :src="`/${parentLocation}/static/images/logo-Bonitasoft.png`"
                alt=""
                width="80px"
                contain
                eager />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title class="d-flex flex-column">
                <span class="pb-4 dark-grey-color">{{ $t('bonitaSoft') }}</span>
                <v-btn
                  href="https://bonitasoft.com"
                  target="_blank"
                  max-width="94px"
                  class="dark-grey-border-color rounded-pill dark-grey-color"
                  small
                  outlined>
                  <span class="caption">{{ $t('visit') }}</span>
                </v-btn>
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <span class="pb-4 dark-grey-color">
                {{ $t('bonitaSoft.description') }}
              </span>
            </v-list-item-content>
          </v-list-item>
        </v-list>
        <v-list class="col-12 col-md-4 ps-0">
          <v-list-item class="ps-0">
            <v-list-item-icon>
              <v-img
                :src="`/${parentLocation}/static/images/Newlogic-Impact-Lab-logo.png`"
                alt=""
                width="80px"
                contain
                eager />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title class="d-flex flex-column">
                <span class="pb-4 dark-grey-color">{{ $t('newlogic') }}</span>
                <v-btn
                  href="https://newlogic.com"
                  target="_blank"
                  max-width="94px"
                  class="dark-grey-border-color rounded-pill dark-grey-color"
                  small
                  outlined>
                  <span class="caption">{{ $t('visit') }}</span>
                </v-btn>
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <span class="pb-4 dark-grey-color">
                {{ $t('newlogic.description') }}
              </span>
            </v-list-item-content>
          </v-list-item>
        </v-list>
      </v-card>
    </div>
  </div>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
  }),
};
</script>