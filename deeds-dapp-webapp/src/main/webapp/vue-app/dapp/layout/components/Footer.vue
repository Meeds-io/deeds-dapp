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
  <v-card class="light-grey-background mainPageLayout pa-5 mx-md-auto" flat>
    <div class="d-flex flex-column flex-sm-row">
      <div class=" py-0 d-flex flex-grow-0 flex-column px-0 mb-6 me-0 me-sm-6 col-12 col-sm-4">
        <div class="d-flex justify-center justify-sm-start">
          <v-img
            :src="`/${parentLocation}/static/images/meeds.png`"
            width="77px"
            max-width="77px"
            alt=""
            contain
            eager />
        </div>
        <span class="font-size-normal text-center text-sm-start text-sub-title my-4">{{ $t('footer.subtitle') }}</span>
        <div class="d-flex justify-center justify-sm-start">
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
            <v-icon size="30" color="black">fab fa-github</v-icon>
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
            <span class="font-size-normal font-weight-black text-sub-title text-uppercase mb-3">{{ $t('product') }}</span>
            <span class="font-size-normal text-sub-title my-2">{{ $t('productTour') }}</span>
            <a
              :href="`/${parentLocation}/tenants`"
              class="text-sub-title no-decoration">
              {{ $t('yourHub') }}
            </a>
          </div>
          <div class="py-0 d-flex flex-column mx-auto">
            <span class="font-size-normal font-weight-black text-sub-title text-uppercase mb-3">{{ $t('workMetaverse.title') }}</span>
            <a
              :href="`/${parentLocation}/overview`"
              class="text-sub-title no-decoration my-2">
              {{ $t('meedsToken') }}
            </a>
            <a
              :href="`/${parentLocation}/deeds`"
              class="text-sub-title text-capitalize no-decoration">
              {{ $t('becomingADeedOwner') }}
            </a>
            <a
              :href="`/${parentLocation}/marketplace`"
              class="text-sub-title no-decoration text-capitalize my-2">
              {{ $t('marketplace') }}
            </a>
          </div>
        </div>
        <div class="col-md-6 col-12 pt-0 d-flex text-center text-md-start">
          <div class="py-0 d-flex flex-column mx-auto">
            <span class="font-size-normal font-weight-black text-sub-title text-uppercase mb-3">{{ $t('meedDAO') }}</span>
            <a
              :href="`/${parentLocation}/whitepaper`"
              class="text-sub-title no-decoration my-2">
              {{ $t('whitePaper') }}
            </a>
            <a
              :href="`/${parentLocation}/about-us`"
              class="text-sub-title no-decoration">
              {{ $t('aboutUs') }}
            </a>
            <a
              :href="`/${parentLocation}/legals`"
              class="text-sub-title no-decoration my-2">
              {{ $t('legal') }}
            </a>
          </div>
          <div class="py-0 d-flex flex-column mx-auto">
            <span class="font-size-normal font-weight-black text-sub-title text-uppercase mb-3">{{ $t('community') }}</span>
            <a
              :href="`/${parentLocation}/stake`"
              class="text-sub-title no-decoration my-2">
              {{ $t('joinMeedsDAO') }}
            </a>
            <a
              href="https://builders.meeds.io/"
              target="_blank"
              class="text-sub-title no-decoration d-flex">
              <v-icon size="16" class="me-2">fas fa-external-link-alt</v-icon>
              {{ $t('buildWithUs') }}
            </a>
          </div>
        </div>
      </div>
    </div>
    <div class="d-flex flex-column-reverse flex-sm-row align-center my-2">
      <span class="caption text-sub-title text-center text-sm-start">{{ $t('copyright') }}</span>
      <v-spacer />
      <deeds-topbar-language-selector />
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