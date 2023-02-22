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
  <v-card class="light-grey-background" flat>
    <div class="mainPageLayout pa-5 mx-md-auto">
      <div class="d-flex flex-column flex-sm-row">
        <div class="col-4 py-0 d-flex flex-column px-0 mb-6 mx-auto">
          <div class="d-flex justify-center justify-sm-start">
            <img
              :src="`/${parentLocation}/static/images/meeds.png`"
              width="77px"
              alt="">
          </div>
          <span class="font-size-normal text-center text-sm-start text-sub-title my-4">{{ $t('footer.subtitle') }}</span>
          <div class="d-flex justify-center justify-sm-start">
            <v-btn
              href="https://twitter.com/IoMeeds"
              target="_blank"
              class="me-1"
              icon>
              <v-icon size="30">fab fa-twitter</v-icon>
            </v-btn>
            <v-btn
              href="https://discord.com/invite/7d9Byf4Fz6"
              target="_blank"
              class="mx-1"
              icon>
              <v-icon size="30">fab fa-discord</v-icon>
            </v-btn>
            <v-btn
              href="https://github.com/Meeds-io"
              target="_blank"
              class="mx-1"
              icon>
              <v-icon size="30">fab fa-github</v-icon>
            </v-btn>
            <v-btn
              href="https://www.linkedin.com/company/meeds-io/"
              target="_blank"
              class="mx-1"
              icon>
              <v-icon size="30">fab fa-linkedin</v-icon>
            </v-btn>
          </div>
        </div>
        <div class="col-8 py-0 d-flex flex-column flex-md-row full-height mx-auto">
          <div class="col-md-6 col-12 py-0 d-flex">
            <div class="col-6 py-0 d-flex flex-column">
              <span class="font-size-normal font-weight-black text-sub-title">{{ $t('product') }}</span>
              <span class="font-size-normal text-sub-title">{{ $t('productTour') }}</span>
              <a
                :href="`/${parentLocation}/tenants`"
                class="text-sub-title no-decoration">
                {{ $t('yourHub') }}
              </a>
            </div>
            <div class="col-6 py-0 d-flex flex-column">
              <span class="font-size-normal font-weight-black text-sub-title">{{ $t('workMetaverse.title') }}</span>
              <a
                :href="`/${parentLocation}/overview`"
                class="text-sub-title no-decoration">
                {{ $t('meedsToken') }}
              </a>
              <a
                :href="`/${parentLocation}/mint`"
                class="text-sub-title no-decoration">
                {{ $t('becomingADeedOwner') }}
              </a>
              <a
                :href="`/${parentLocation}/marketplace`"
                class="text-sub-title no-decoration">
                {{ $t('marketplace') }}
              </a>
            </div>
          </div>
          <div class="col-md-6 col-12 py-0 d-flex">
            <div class="col-6 py-0 d-flex flex-column">
              <span class="font-size-normal font-weight-black text-sub-title">{{ $t('meedDAO') }}</span>
              <a
                :href="`${whitepaperLink}`"
                target="_blank"
                class="text-sub-title no-decoration">
                {{ $t('whitePaper') }}
              </a>
              <a
                :href="`/${parentLocation}/about-us`"
                class="text-sub-title no-decoration">
                {{ $t('aboutUs') }}
              </a>
              <span class="font-size-normal text-sub-title">{{ $t('legal') }}</span>
            </div>
            <div class="col-6 py-0 d-flex flex-column">
              <span class="font-size-normal font-weight-black text-sub-title">{{ $t('community') }}</span>
              <a
                :href="`/${parentLocation}/stake`"
                class="text-sub-title no-decoration">
                {{ $t('joinMeedsDAO') }}
              </a>
              <a
                href="https://builders.meeds.io/"
                target="_blank"
                class="text-sub-title no-decoration">
                {{ $t('buildWithUs') }}
              </a>
            </div>
          </div>
        </div>
      </div>
      <div class="d-flex flex-column-reverse flex-sm-row my-2">
        <span class="caption text-sub-title d-flex align-center text-center text-sm-start">{{ $t('copyright') }}</span>
        <v-spacer />
        <deeds-topbar-language-selector />
      </div>
    </div>
  </v-card>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    whitepaperLink: state => state.whitepaperLink,
  }),
};
</script>