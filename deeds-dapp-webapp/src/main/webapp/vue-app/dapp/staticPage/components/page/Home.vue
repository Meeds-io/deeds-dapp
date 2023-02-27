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
    <v-card height="150px" flat>
      <v-card-title class="full-height justify-center text-center display-1 font-weight-black dark-grey-color">{{ $t('homePage.title') }}</v-card-title>
    </v-card>
    <span class="headline text-center pb-10 dark-grey-color">{{ $t('homePage.description') }}</span>
    <div class="mx-auto">
      <v-btn
        href="./whitepaper" 
        class="primary"
        width="240px"
        depressed
        dark
        x-large
        outlined>
        <h4>{{ $t('discover') }}</h4>
      </v-btn>
    </div>
    <v-card class="light-grey-background pa-5 my-6" flat>
      <div class="d-block d-md-flex pb-10">
        <div class="col-12 col-md-6">
          <div class="d-flex">
            <v-img
              :src="`/${parentLocation}/static/images/onboard.jpg`"
              width="75px"
              max-width="75px"
              max-height="75px"
              alt=""
              contain
              eager />
            <span class="headline font-weight-black dark-grey-color my-auto ps-5">{{ $t('homepage.onboard.title') }}</span>
          </div>
          <div class="d-flex flex-column pt-4">
            <v-list-item class="ps-0">
              <v-list-item-icon class="my-3">
                <v-icon>fas fa-check</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <span class="dark-grey-color font-size-normal">{{ $t('homepage.onboard.descriptionPart1') }}</span>
              </v-list-item-content>
            </v-list-item>
            <v-list-item class="ps-0">
              <v-list-item-icon class="my-3">
                <v-icon>fas fa-check</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <span class="dark-grey-color font-size-normal">{{ $t('homepage.onboard.descriptionPart2') }}</span>
              </v-list-item-content>
            </v-list-item>
            <v-list-item class="ps-0">
              <v-list-item-icon class="my-3">
                <v-icon>fas fa-check</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <span class="dark-grey-color font-size-normal">{{ $t('homepage.onboard.descriptionPart3') }}</span>
              </v-list-item-content>
            </v-list-item>
          </div>
        </div>
        <v-img
          :src="`/${parentLocation}/static/images/onboard_image.png`"
          class="col-12 col-md-6"
          alt=""
          contain
          eager />
      </div>
      <div class="d-flex flex-column-reverse flex-md-row py-10">
        <v-img
          :src="`/${parentLocation}/static/images/organize_image.png`"
          class="col-12 col-md-6"
          alt=""
          contain
          eager />
        <div class="col-12 col-md-6 ps-md-14">
          <div class="d-flex">
            <v-img
              :src="`/${parentLocation}/static/images/organize.png`"
              width="75px"
              max-width="75px"
              max-height="75px"
              alt=""
              contain
              eager />
            <span class="headline font-weight-black dark-grey-color my-auto ps-5">{{ $t('homepage.organize.title') }}</span>
          </div>
          <div class="d-flex flex-column pt-4">
            <v-list-item class="ps-0">
              <v-list-item-icon class="my-3">
                <v-icon>fas fa-check</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <span class="dark-grey-color font-size-normal">{{ $t('homepage.organize.descriptionPart1') }}</span>
              </v-list-item-content>
            </v-list-item>
            <v-list-item class="ps-0">
              <v-list-item-icon class="my-3">
                <v-icon>fas fa-check</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <span class="dark-grey-color font-size-normal">{{ $t('homepage.organize.descriptionPart2') }}</span>
              </v-list-item-content>
            </v-list-item>
            <v-list-item class="ps-0">
              <v-list-item-icon class="my-3">
                <v-icon>fas fa-check</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <span class="dark-grey-color font-size-normal">{{ $t('homepage.organize.descriptionPart3') }}</span>
              </v-list-item-content>
            </v-list-item>
          </div>
        </div>
      </div>
      <div class="d-block d-md-flex py-10">
        <div class="col-12 col-md-6">
          <div class="d-flex">
            <v-img
              :src="`/${parentLocation}/static/images/reward.png`"
              width="75px"
              max-width="75px"
              max-height="75px"
              alt=""
              contain
              eager />
            <span class="headline font-weight-black dark-grey-color my-auto ps-5">{{ $t('homepage.reward.title') }}</span>
          </div>
          <div class="d-flex flex-column pt-4">
            <v-list-item class="ps-0">
              <v-list-item-icon class="my-3">
                <v-icon>fas fa-check</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <span class="dark-grey-color font-size-normal">{{ $t('homepage.reward.descriptionPart1') }}</span>
              </v-list-item-content>
            </v-list-item>
            <v-list-item class="ps-0">
              <v-list-item-icon class="my-3">
                <v-icon>fas fa-check</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <span class="dark-grey-color font-size-normal">{{ $t('homepage.reward.descriptionPart2') }}</span>
              </v-list-item-content>
            </v-list-item>
            <v-list-item class="ps-0">
              <v-list-item-icon class="my-3">
                <v-icon>fas fa-check</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <span class="dark-grey-color font-size-normal">{{ $t('homepage.reward.descriptionPart3') }}</span>
              </v-list-item-content>
            </v-list-item>
          </div>
        </div>
        <v-img 
          :src="`/${parentLocation}/static/images/reward_image.png`"
          class="col-12 col-md-6"
          alt=""
          contain
          eager />
      </div>
      <div class="d-flex">
        <v-btn
          href="./whitepaper" 
          class="primary mx-auto"
          width="240px"
          depressed
          dark
          x-large
          outlined>
          <h4>{{ $t('discover') }}</h4>
        </v-btn>
      </div>
    </v-card>
    <div class="pt-12">
      <div class="v-card__title full-height justify-center display-1 font-weight-black dark-grey-color">{{ $t('poweredByWeb3.title') }}</div>
      <v-card class="my-8 mx-4 d-block d-md-flex" flat>
        <div class="ps-0 my-2 col-12 col-md-4">
          <div class="ps-0 d-flex justify-md-center justify-start mt-3">
            <v-img 
              :src="`/${parentLocation}/static/images/MEED-circle.png`" 
              width="75px"
              max-width="75px"
              max-height="75px"
              class="me-7"
              alt=""
              contain
              eager />
            <div class="d-flex flex-column flex-md-row  align-md-center align-start">
              <span class="headline font-weight-black dark-grey-color ms-md-7">{{ $t('tokens.title') }}</span>
              <span class="dark-grey-color hidden-md-and-up">{{ $t('tokens.description') }}</span>
            </div>
          </div>
          <span class="mt-3 dark-grey-color d-none d-md-flex text-center hidden-md-and-down">
            {{ $t('tokens.description') }}
          </span>
        </div>
        <div class="my-2 col-12 col-md-4">
          <div class="ps-0 d-flex justify-md-center justify-start my-6">
            <v-img
              :src="`/${parentLocation}/static/images/hand-holding-heart.png`" 
              height="50px"
              max-height="50px"
              max-width="50px"
              class="me-9"
              alt=""
              contain
              eager />
            <div class="d-flex flex-column flex-md-row  align-md-center align-start">
              <span class="headline font-weight-black dark-grey-color ms-md-7">{{ $t('store.title') }}</span>
              <span class="dark-grey-color hidden-md-and-up">{{ $t('store.description') }}</span>
            </div>
          </div>
          <span class="mt-6 dark-grey-color d-none d-md-flex text-center hidden-md-and-down">
            {{ $t('store.description') }}
          </span>
        </div>
        <div class="my-2 col-12 col-md-4">
          <div class="ps-0 d-flex justify-md-center justify-start my-6">
            <v-img
              :src="`/${parentLocation}/static/images/city.png`" 
              height="50px"
              max-height="50px"
              max-width="50px"
              class="me-7"
              alt=""
              contain
              eager />
            <div class="d-flex flex-column flex-md-row  align-md-center align-start">
              <span class="headline font-weight-black dark-grey-color ms-md-7">{{ $t('workMetaverse.title') }}</span>
              <span class="dark-grey-color hidden-md-and-up">{{ $t('workMetaverse.description') }}</span>
            </div>
          </div>
          <span class="mt-6 dark-grey-color d-none d-md-flex text-center hidden-md-and-down">
            {{ $t('workMetaverse.description') }}
          </span>
        </div>
      </v-card>
      <div class="d-flex">
        <v-btn
          href="./whitepaper" 
          class="primary mx-auto"
          width="240px"
          depressed
          dark
          x-large
          outlined>
          <h4>{{ $t('learnMore') }}</h4>
        </v-btn>
      </div>
    </div>
    <div class="pt-12">
      <div class="v-card__title full-height justify-center display-1 font-weight-black dark-grey-color">{{ $t('whatInMeedForYou.title') }}</div>
      <v-card class="my-8 mx-4 d-block d-md-flex" flat>
        <v-list class="ps-0 col-12 col-md-6">
          <v-list-item class="ps-0">
            <v-list-item-icon>
              <v-img
                :src="`/${parentLocation}/static/images/contributor.png`"
                alt=""
                height="60px"
                max-height="60px"
                max-width="75px"
                contain
                eager />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title class="d-flex flex-column">
                <span class="headline font-weight-black dark-grey-color">{{ $t('contributor.title') }}</span>
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <v-list>
                <v-list-item class="ps-0">
                  <v-list-item-icon class="my-3 me-4">
                    <v-icon>fas fa-check</v-icon>
                  </v-list-item-icon>
                  <v-list-item-content>
                    <span class="dark-grey-color font-size-normal">{{ $t('contributor.descriptionPart1') }}</span>
                  </v-list-item-content>
                </v-list-item>
                <v-list-item class="ps-0">
                  <v-list-item-icon class="my-3 me-4">
                    <v-icon>fas fa-check</v-icon>
                  </v-list-item-icon>
                  <v-list-item-content>
                    <span class="dark-grey-color font-size-normal">{{ $t('contributor.descriptionPart2') }}</span>
                  </v-list-item-content>
                </v-list-item>
                <v-list-item class="ps-0">
                  <v-list-item-icon class="my-3 me-4">
                    <v-icon>fas fa-check</v-icon>
                  </v-list-item-icon>
                  <v-list-item-content>
                    <span class="dark-grey-color font-size-normal">{{ $t('contributor.descriptionPart3') }}</span>
                  </v-list-item-content>
                </v-list-item>
              </v-list>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="my-6">
            <v-btn
              href="https://builders.meeds.io"
              target="_blank"
              class="primary mx-auto"
              width="240px"
              depressed
              dark
              x-large
              outlined>
              <h4>{{ $t('trynow') }}</h4>
            </v-btn>
          </v-list-item>
        </v-list>
        <v-list class="ps-0 col-12 col-md-6">
          <v-list-item class="ps-0">
            <v-list-item-icon>
              <v-img
                :src="`/${parentLocation}/static/images/leader.png`"
                height="60px"
                max-height="60px"
                max-width="100px"
                alt=""
                contain
                eager />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title class="d-flex flex-column">
                <span class="headline font-weight-black dark-grey-color">{{ $t('leader.title') }}</span>
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="ps-0">
            <v-list-item-content>
              <v-list>
                <v-list-item class="ps-0">
                  <v-list-item-icon class="my-3 me-4">
                    <v-icon>fas fa-check</v-icon>
                  </v-list-item-icon>
                  <v-list-item-content>
                    <span class="dark-grey-color font-size-normal">{{ $t('leader.descriptionPart1') }}</span>
                  </v-list-item-content>
                </v-list-item>
                <v-list-item class="ps-0">
                  <v-list-item-icon class="my-3 me-4">
                    <v-icon>fas fa-check</v-icon>
                  </v-list-item-icon>
                  <v-list-item-content>
                    <span class="dark-grey-color font-size-normal">{{ $t('leader.descriptionPart2') }}</span>
                  </v-list-item-content>
                </v-list-item>
                <v-list-item class="ps-0">
                  <v-list-item-icon class="my-3 me-4">
                    <v-icon>fas fa-check</v-icon>
                  </v-list-item-icon>
                  <v-list-item-content>
                    <span class="dark-grey-color font-size-normal">{{ $t('leader.descriptionPart3') }}</span>
                  </v-list-item-content>
                </v-list-item>
              </v-list>
            </v-list-item-content>
          </v-list-item>
          <v-list-item class="my-6">
            <v-btn
              href="./tenants"
              class="primary mx-auto"
              width="240px"
              depressed
              dark
              x-large
              outlined>
              <h4>{{ $t('getAHub') }}</h4>
            </v-btn>
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