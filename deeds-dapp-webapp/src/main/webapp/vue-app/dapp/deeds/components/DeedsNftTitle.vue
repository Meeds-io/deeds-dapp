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
    <v-card-title class="ps-0 py-0 justify-center">{{ $t('becomeDeedOwner') }}</v-card-title>
    <v-card-text class="ps-0 pt-4 ms-md-2" v-html="$t('becomeDeedOwnerDescription', {0: `<a target='_blank' href='${whitepaperLink}' class='link--color' rel='nofollow noreferrer noopener'>${$t('whitePaper')}</a>`})" />
  </v-card>
</template>
<script>
export default {
  computed: Vuex.mapState({
    whitepaperLink: state => state.whitepaperLink,
  }),
};
</script>