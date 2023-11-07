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
      <deeds-page-title-layout>
        <template #title>
          {{ $t('page.buy.title') }}
        </template>
      </deeds-page-title-layout>
    </v-card>
    <v-row>
      <v-col
        cols="12"
        xs="12"
        md="6"
        lg="4">
        <deeds-buy-hub-card
          :link="formLink"
          extra-class="secondary-border-color">
          <template #icon>
            fa-rocket
          </template>
          <template #title>
            {{ $t('free.hub.card.title') }}
          </template>
          <template #button>
            {{ $t('free.hub.card.button') }}
          </template>
        </deeds-buy-hub-card>
      </v-col>
      <v-col
        cols="12"
        xs="12"
        md="6"
        lg="4">
        <deeds-buy-hub-card
          :link="marketplaceURL"
          extra-class="grey-border-color">
          <template #icon>
            fa-file-signature
          </template>
          <template #title>
            {{ $t('rent.hub.card.title') }}
          </template>
          <template #button>
            {{ $t('rent.hub.card.button') }}
          </template>
        </deeds-buy-hub-card>
      </v-col>
      <v-col
        cols="12"
        xs="12"
        md="6"
        lg="4">
        <deeds-buy-hub-card
          :link="mintUrl"
          extra-class="grey-border-color">
          <template #icon>
            fa-landmark
          </template>
          <template #title>
            {{ $t('buy.hub.card.title') }}
          </template>
          <template #button>
            {{ $t('buy.hub.card.button') }}
          </template>
        </deeds-buy-hub-card>
      </v-col>
    </v-row>
    <div class="d-flex flex-column mt-7">
      <span class="display-1 mt-5 mb-13 font-weight-bold d-flex justify-center">{{ $t('meeds.features.title') }}</span>
      <v-carousel
        active-class="primary"
        :show-arrows="false"
        continuous
        cycle
        light
        hide-delimiter-background
        delimiter-icon="fa-minus"
        height="300px">
        <template v-for="(feature, i) in meedsFeatures"> 
          <v-carousel-item
            v-if="(i + 1) % columns === 1 || columns === 1"
            :key="i">
            <v-row>
              <v-col
                :key="j"
                cols="12"
                xs="12"
                lg="4"
                v-for="(col,j) in columns">
                <div class="d-flex flex-column justify-center">
                  <v-img
                    v-if="meedsFeatures[i+j].imageUrl"
                    :src="meedsFeatures[i+j].imageUrl"
                    class="my-7 mx-auto"
                    :height="meedsFeatures[i+j].height"
                    :width="meedsFeatures[i+j].width" />
                  <v-icon 
                    v-else
                    class="my-7"
                    size="150px"
                    color="secondary">
                    {{ meedsFeatures[i+j].icon }}
                  </v-icon>
                  <span class="headline align-center mb-6">
                    {{ meedsFeatures[i+j].title }} 
                  </span>
                </div>
              </v-col>
            </v-row>
          </v-carousel-item>
        </template> 
      </v-carousel>
      <v-btn
        :href="tourURL"
        class="primary px-7 mx-auto my-16"
        width="150px"
        height="60px"
        text
        outlined>
        <span class="font-weight-bold text-h5 white--text">{{ $t('dapp.seeMore') }}</span>
      </v-btn>
    </div>
  </div>
</template>
<script>
export default {
  data() {
    return {
      meedsFeatures: [
        {
          icon: 'fa-puzzle-piece',
          title: this.$t('meeds.features.contribution'),
        },
        {
          icon: 'fa-award',
          title: this.$t('meeds.features.peerToPeer'),
        },
        {
          icon: 'fa-shop',
          title: this.$t('meeds.features.wallet'),
        },
        {
          icon: 'fa-users',
          title: this.$t('meeds.features.teamwork'),
        },
        {
          imageUrl: 'https://res.cloudinary.com/dcooc6vig/image/upload/v1698767424/meedsdao-site/assets/images/connectors_k6mbds.png',
          title: this.$t('meeds.features.connectors'),
          height: '150px',
          width: '165px'
        },
        {
          imageUrl: 'https://res.cloudinary.com/dcooc6vig/image/upload/v1699349941/meedsdao-site/assets/images/MEED_User_Engagement.png',
          title: this.$t('meeds.features.engagement'),
          height: '150px',
          width: '150px'
        }
      ]
    };
  },
  computed: Vuex.mapState({
    marketplaceURL: state => state.marketplaceURL,
    tourURL: state => state.tourURL,
    formLink: state => state.formLink,
    mintUrl: state => state.mintUrl,
    columns() {
      if ( this.$vuetify.breakpoint.lgAndUp) {
        return 3;
      } else {
        return 1;
      }
    }
  })
};

</script>