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
      <v-card-title class="py-0 justify-center font-weight-bold dark-grey-color">{{ $t('whitepaper.title') }}</v-card-title>
    </v-card>
    <div class="d-flex flex-column my-6">
      <v-card class="light-grey-background pb-5 pt-8 pt-md-10 px-md-16 px-6" flat>
        <div class="d-flex">
          <div>
            <v-card-subtitle class="dark-grey-color title font-weight-normal py-2" v-html="$t('whitepaper.descriptionPart1', {0:'<strong>', 1: '</strong>'})" />
            <v-card-subtitle class="dark-grey-color title font-weight-normal py-2" v-html="$t('whitepaper.descriptionPart2', {0:'<strong>', 1: '</strong>'})" />
            <v-card-subtitle class="dark-grey-color title font-weight-normal py-2" v-html="$t('whitepaper.descriptionPart3', {0:'<strong>', 1: '</strong>'})" />
            <v-card-subtitle class="dark-grey-color title font-weight-normal py-2" v-html="$t('whitepaper.descriptionPart4', {0:'<strong>', 1: '</strong>'})" />
            <v-card-subtitle class="dark-grey-color title font-weight-normal py-2" v-html="$t('whitepaper.descriptionPart5', {0:'<strong>', 1: '</strong>'})" />
          </div>
          <v-img
            :src="`${parentLocation}/static/images/logo-m-grand.svg`"
            alt=""
            width="350px"
            class="logo-m hidden-md-and-down mt-7" />
        </div>
        <div class=" d-flex justify-center pt-3 pb-md-7">
          <v-btn
            :href="whitepaperLink" 
            target="_blank"
            class="primary"
            depressed
            dark
            x-large
            outlined>
            <h4>{{ $t('readWhitepaper') }}</h4>
          </v-btn>
        </div>   
      </v-card>
    </div>
    <div class="d-flex flex-column">
      <div class="px-0 pt-0 v-card__title font-weight-bold dark-grey-color">
        {{ $t('enterTheWorkMetaverse.title') }}
      </div> 
      <v-card class="d-block d-md-flex" flat>
        <v-list class="ps-0 col-12 col-md-4">
          <v-list-item class="ps-0">
            <v-list-item-icon>
              <v-img
                :src="`${parentLocation}/static/images/meedsToken.png`"
                alt=""
                width="47px"
                contain
                eager />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title>
                <h4 class="pb-4 font-weight-normal dark-grey-color">{{ $t('meed') }}</h4>
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <span class="pb-4 dark-grey-color">{{ $t('meed.descriptionPart1') }}</span>
              <span class="pb-4 dark-grey-color">{{ $t('meed.descriptionPart2') }}</span>
            </v-list-item-content>
          </v-list-item>
        </v-list>
        <v-list class="ps-0 col-12 col-md-4">
          <v-list-item class="ps-0">
            <v-list-item-icon>
              <v-img
                :src="`${parentLocation}/static/images/deed.png`"
                alt=""
                width="47px"
                contain
                eager />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title>
                <h4 class="pb-4 font-weight-normal dark-grey-color">{{ $t('deed') }}</h4>
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <span class="pb-4 dark-grey-color">{{ $t('deed.descriptionPart1') }}</span>
              <span class="pb-4 dark-grey-color" v-html="$t('deed.descriptionPart2', {0:`<a class='primary--text no-decoration font-weight-bold' href='./deeds'>`, 1: `</a>`, 2:`<a class='primary--text no-decoration font-weight-bold' href='./owners'>`, 3:`<a class='primary--text no-decoration font-weight-bold' href='./marketplace'>`})"></span>
            </v-list-item-content>
          </v-list-item>
        </v-list>
        <v-list class="ps-0 col-12 col-md-4">
          <v-list-item class="ps-0">
            <v-list-item-icon>
              <v-img
                :src="`${parentLocation}/static/images/meedsToken.png`"
                alt=""
                width="47px"
                contain
                eager />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title>
                <h4 class="pb-4 font-weight-normal dark-grey-color">{{ $t('xeed') }}</h4>
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <span class="pb-4 dark-grey-color">{{ $t('xeed.descriptionPart1') }}</span>
              <span class="pb-4 dark-grey-color">{{ $t('xeed.descriptionPart2') }}</span>
            </v-list-item-content>
          </v-list-item>
        </v-list>
      </v-card>
    </div>
    <div class="d-flex flex-column-reverse flex-md-row py-4">
      <div class="d-flex justify-center justify-md-start">
        <v-img 
          v-if="!pageState || pageState === 'work'"
          :src="`${parentLocation}/static/images/work-investment.svg`"
          width="564px"
          alt=""
          contain
          eager />
        <v-img 
          v-else
          :src="`${parentLocation}/static/images/Capital-Investement.svg`"
          width="564px"
          alt=""
          contain
          eager />
      </div>
      <div class="d-flex flex-column ps-5"> 
        <div class="ps-0 py-3 dark-grey-color v-card__text" v-html="$t('invest.descriptionPart1', {0:'<strong>', 1: '</strong>'})"></div>
        <div class="ps-0 py-3 dark-grey-color v-card__text">{{ $t('invest.descriptionPart2') }}</div>
        <div class="d-flex justify-center justify-md-start">
          <v-btn
            :dark="!pageState || pageState === 'work'"
            :text="pageState === 'capital'"
            :class="(!pageState || pageState === 'work') && 'primary'"
            :color="pageState === 'capital' && 'primary'"
            width="156px"
            class="me-4"
            outlined
            @click="$store.commit('setPageState','work')">
            <span class="subtitle-2 font-weight-normal">{{ $t('investWork') }}</span>
          </v-btn>
          <v-btn
            :dark="pageState === 'capital'"
            :text="!pageState || pageState === 'work'"
            :class="pageState === 'capital' && 'primary'"
            :color="(!pageState || pageState === 'work') && 'primary'"
            width="156px"
            class="ms-4"
            outlined
            @click="$store.commit('setPageState','capital')">
            <span class="subtitle-2 font-weight-normal">{{ $t('investCapital') }}</span>
          </v-btn>
        </div>
        <div v-if="!pageState || pageState === 'work'">
          <div class="ps-0 pt-4 pb-2 dark-grey-color v-card__text" v-html="$t('investWork.descriptionPart1', {0:`<a class='primary--text no-decoration font-weight-bold' href='./overview'>`, 1: `</a>`})"></div>
          <div class="ps-0 py-2 dark-grey-color v-card__text" v-html="$t('investWork.descriptionPart2', {0:`<a class='primary--text no-decoration font-weight-bold' href='./stake'>`, 1: `</a>`})"></div>
          <div class="ps-0 py-2 dark-grey-color v-card__text" v-html="$t('investWork.descriptionPart3', {0:`<a class='primary--text no-decoration font-weight-bold' href='./deeds'>`, 1: `</a>`})"></div>
          <div class="ps-0 pt-2 pb-7 dark-grey-color v-card__text" v-html="$t('investWork.descriptionPart4', {0:`<a class='primary--text no-decoration font-weight-bold' href='./owners'>`, 1: `</a>`, 3:`<a class='primary--text no-decoration font-weight-bold' target='_blank' href='https://opensea.io'>`})"></div>
        </div>
        <div v-else>
          <div class="ps-0 pt-4 pb-2 dark-grey-color v-card__text" v-html="$t('investCapital.descriptionPart1', {0:`<a class='primary--text no-decoration font-weight-bold' href='./overview'>`, 1: `</a>`})"></div>
          <div class="ps-0 py-2 dark-grey-color v-card__text" v-html="$t('investCapital.descriptionPart2', {0:`<a class='primary--text no-decoration font-weight-bold' href='./farm'>`, 1: `</a>`})"></div>
          <div class="ps-0 py-2 dark-grey-color v-card__text" v-html="$t('investCapital.descriptionPart3', {0:`<a class='primary--text no-decoration font-weight-bold' href='./stake'>`, 1: `</a>`})"></div>
          <div class="ps-0 py-2 dark-grey-color v-card__text" v-html="$t('investCapital.descriptionPart4', {0:`<a class='primary--text no-decoration font-weight-bold' href='./deeds'>`, 1: `</a>`})"></div>
          <div class="ps-0 pt-2 pb-7 dark-grey-color v-card__text" v-html="$t('investCapital.descriptionPart5', {0:`<a class='primary--text no-decoration font-weight-bold' href='./owners'>`, 1: `</a>`, 2:`<a class='primary--text no-decoration font-weight-bold' target='_blank' href='https://opensea.io'>`})"></div>
        </div>
        <div class="d-flex justify-center justify-md-start">
          <v-btn
            v-if="!pageState || pageState === 'work'"
            href="https://builders.meeds.io"
            target="_blank"
            max-width="156px"
            class="dark-grey-border-color rounded-pill dark-grey-color d-flex justify-center" 
            outlined>
            <span class="subtitle-2 font-weight-normal">{{ $t('startEarning') }}</span>
          </v-btn>
          <v-btn
            v-else
            href="./portfolio"
            max-width="156px"
            class="dark-grey-border-color rounded-pill dark-grey-color d-flex justify-center" 
            outlined>
            <span class="subtitle-2 font-weight-normal">{{ $t('investInMeeds') }}</span>
          </v-btn>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  computed: Vuex.mapState({
    pageState: state => state.pageState,
    parentLocation: state => state.parentLocation,
    whitepaperLink: state => state.whitepaperLink,
  }),
};
</script>