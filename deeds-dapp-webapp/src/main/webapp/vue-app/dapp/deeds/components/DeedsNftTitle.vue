<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 
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
  <v-card flat>
    <v-card-title class="justify-center flex-nowrap mb-10 ps-4 ps-sm-2">
      <div class="d-flex flex-column col-12 col-lg-8 col-md-7 pa-0">
        <span class="display-1 font-weight-bold text-center text-sm-start">{{ $t('page.deeds.title') }}</span>
        <span class="headline hidden-sm-and-down">{{ $t('page.deeds.subtitle') }}</span>
      </div>      
      <v-spacer />
      <v-img 
        :src="`${parentLocation}/static/images/mint_banner.webp`"
        max-width="340px"
        class="hidden-sm-and-down"
        alt=""
        contain
        eager />
    </v-card-title>
    <v-card-text class="ps-0 pt-0 pt-md-12 ms-md-2 text-h6 font-weight-normal" v-html="$t('becomeDeedOwnerDescription', {0: `<a target='_blank' href='${whitepaperLink}' class='primary--text embedded-link-text font-weight-bold  v-btn v-btn--text letter-spacing-normal' rel='nofollow noreferrer noopener'>${$t('whitePaper')}</a>`})" />
  </v-card>
</template>
<script>
export default {
  computed: Vuex.mapState({
    whitepaperLink: state => state.whitepaperLink,
    parentLocation: state => state.parentLocation,
  }),
};
</script>