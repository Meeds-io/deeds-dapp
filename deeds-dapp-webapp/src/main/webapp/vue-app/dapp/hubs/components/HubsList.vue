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
  <v-row class="mb-15 mt-7">
    <v-col cols="12">
      <div class="d-flex flex-row flex-grow-1">
        <div v-if="keyword" class="headline font-weight-bold pb-1">{{ hubsCount }} {{ $t('hubs.hubsFound') }} </div>
        <div v-else class="headline font-weight-bold pb-1">{{ $t('hubs.title.featuredHubs') }}</div>
      </div>
    </v-col>
    <v-col
      v-for="(hub, index) in filteredHubs"
      :key="`${hub.address}-${index}`"
      class="d-flex justify-center pb-12"
      cols="12"
      lg="4"
      md="12">
      <v-slide-x-transition>
        <deeds-hub-card :hub="hub" />
      </v-slide-x-transition>
    </v-col>
    <v-col cols="12">
      <div class="d-flex flex-row flex-grow-1">
        <div class="headline font-weight-bold pt-10 pb-1">{{ $t('hubs.title.upcomingHubs') }}</div>
      </div>
    </v-col>
    <v-col
      v-for="(hub, index) in filteredUpcomingHubs"
      :key="`${hub.address}-${index}`"
      class="d-flex justify-center"
      cols="12"
      lg="4"
      md="12">
      <v-slide-x-transition>
        <deeds-hub-upcoming-card 
          :hub="hub" />
      </v-slide-x-transition>
    </v-col>
    <v-col cols="12">
      <v-card class="full-width fill-height py-2" flat>
        <v-card-title class="justify-center headline py-10">
          <span v-if="!keyword">
            {{ $t('notListedYet') }}
          </span>
          <div v-else class="d-flex flex-column">
            <span class="text-center"> {{ $t('joinWaitlist.descriptionPart1') }} </span>
            <span class="text-center"> {{ $t('joinWaitlist.descriptionPart2') }} </span>
          </div>
        </v-card-title>
        <v-card-actions class="py-10">
          <v-btn
            id="hJoin-button"
            :href="formLink"
            target="_blank"
            class="mx-auto px-8"
            color="primary"
            height="60px"
            dark
            depressed>
            <h4 class="font-weight-bold">{{ $t('joinWaitlist') }}</h4>
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-col>
  </v-row>
</template>
<script>
export default {
  props: {
    keyword: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    loading: false,
    pageSize: 10,
    page: 0,
    hasMore: false,
    hubs: [
      {
        address: '0xfefefefefefefefefefef1',
        name: {
          fr: 'Builders Hub',
          en: 'Builders Hub'
        },
        description: {
          fr: 'Hub officiel de la DAO Meeds',
          en: 'Official Hub of the Meeds DAO'
        },
        logoUrl: 'https://res.cloudinary.com/dcooc6vig/image/upload/v1685699618/meedsdao-site/assets/images/MeedsDAO%20Logo.png',
        backgroundColor: '#3F8487',
        usersCount: 248,
        hubUrl: 'https://builders.meeds.io',
        rewardsPerPeriod: 1000,
        rewardsPeriodType: 'week',
      },
      {
        address: '0xfefefefefefefefefefef2',
        name: {
          fr: 'Communauté eXo',
          en: 'eXo Community'
        },
        description: {
          fr: 'Communauté officielle d\'eXo',
          en: 'Official community of eXo'
        },
        logoUrl: 'https://res.cloudinary.com/dcooc6vig/image/upload/v1688983553/meedsdao-site/assets/images/exo-logo.webp',
        backgroundColor: '#6083B6',
        usersCount: 10000,
        hubUrl: 'https://community.exoplatform.com',
        rewardsPerPeriod: 3000,
        rewardsPeriodType: 'month',
      },
      {
        address: '0xfefefefefefefefefefef10',
        name: {
          fr: 'Rainbow Partners',
          en: 'Rainbow Partners'
        },
        description: {
          fr: 'Hub d\'engagement pour les collaborateurs du groupe Rainbow Partners',
          en: 'Engagement Hub for Rainbow Partners employees'
        },
        logoUrl: 'https://res.cloudinary.com/dcooc6vig/image/upload/v1696606342/meedsdao-site/assets/images/Rainbow_q9vimd.png',
        backgroundColor: '#2c3163',
        usersCount: 78,
        hubUrl: 'https://rainbowpartners.meeds.io/',
        rewardsPerPeriod: 800,
        rewardsPeriodType: 'week',
      },
      {
        address: '0xfefefefefefefefefefef13',
        name: {
          fr: 'TheGallery Ambassadors',
          en: 'TheGallery Ambassadors'
        },
        description: {
          fr: 'TheGallery Ambassadors est un club privé dédié à la promotion des artistes et des activités de TheGallery. Cette communauté exclusive est accessible uniquement par invitation et récompense ses membres en fonction de leurs performances et de leur soutien au projet et à ses artistes.',
          en: 'TheGallery Ambassadors is a private club dedicated to promoting the artists and activity of TheGallery. This exclusive community is only accessible through invitation and will reward its members depending on their performance and support to the project and its artists.'
        },
        logoUrl: 'https://res.cloudinary.com/dcooc6vig/image/upload/v1701768967/meedsdao-site/assets/images/Featured%20hubs/TheGallery_swwey3.png',
        backgroundColor: '#00263a',
        hubUrl: 'https://thegallery.meeds.io/'
      }
    ],
    upcomingHubs: [
      {
        address: '0xfefefefefefefefefefef4',
        name: {
          fr: 'Radicle',
          en: 'Radicle'
        },
        description: {
          fr: 'Radicle est un réseau souverain peer-to-peer pour la collaboration de code, construit au-dessus de Git',
          en: 'Radicle is a sovereign peer-to-peer network for code collaboration, built on top of Git'
        },
        logoUrl: 'https://res.cloudinary.com/dcooc6vig/image/upload/v1689868155/meedsdao-site/assets/images/upcoming%20hubs/radicle_r3eoew.webp',
        backgroundColor: '#5555FF',
        websiteUrl: 'https://radicle.xyz',
        discordUrl: 'https://discord.com/invite/HRdnwAwGbG',
        githubUrl: 'https://github.com/radicle-dev',
      },
      {
        address: '0xfefefefefefefefefefef5',
        name: {
          fr: 'Hopr',
          en: 'Hopr'
        },
        description: {
          fr: 'Nous construisons un internet privé pour tout le monde',
          en: 'We\'re building a private internet for everyone'
        },
        logoUrl: 'https://res.cloudinary.com/dcooc6vig/image/upload/v1689868155/meedsdao-site/assets/images/upcoming%20hubs/hopr_czrkki.webp',
        backgroundColor: '#FFFFFF',
        websiteUrl: 'https://hoprnet.org/',
        twitterURL: 'https://twitter.com/hoprnet',
        discordUrl: 'https://discord.gg/dEAWC4G',
      },
      {
        address: '0xfefefefefefefefefefef6',
        name: {
          fr: 'Développeur DAO',
          en: 'Developer DAO'
        },
        description: {
          fr: 'Développeur DAO est une communauté de milliers de web3 builders qui créent un meilleur internet',
          en: 'Developer DAO is a community of thousands of web3 builders creating a better internet'
        },
        logoUrl: 'https://res.cloudinary.com/dcooc6vig/image/upload/v1689868155/meedsdao-site/assets/images/upcoming%20hubs/D_D_logo-dark_yzzsxi.webp',
        backgroundColor: '#000000',
        websiteUrl: 'https://www.developerdao.com/',
        twitterURL: 'https://twitter.com/developer_dao/',
        discordUrl: 'https://discord.gg/devdao',
        githubUrl: 'https://github.com/Developer-DAO',
      },
      {
        address: '0xfefefefefefefefefefef7',
        name: {
          fr: 'Fondation ResearchHub',
          en: 'ResearchHub Foundation'
        },
        description: {
          fr: 'Une communauté mondiale décentralisée qui vise à harmoniser les incitations dans le monde universitaire',
          en: 'A global decentralized community that aims to align incentives in academia'
        },
        logoUrl: 'https://res.cloudinary.com/dcooc6vig/image/upload/v1689868155/meedsdao-site/assets/images/upcoming%20hubs/ResearchHub_ksnxlu.webp',
        backgroundColor: '#64409E',
        websiteUrl: 'https://www.researchhub.foundation',
        twitterURL: 'https://twitter.com/ResearchHubF',
        discordUrl: 'https://discord.com/invite/YdNtQfQmEg',
        githubUrl: 'https://github.com/ResearchHub',
      },
      {
        address: '0xfefefefefefefefefefef9',
        name: {
          fr: 'Vigilance DAO',
          en: 'Vigilance DAO'
        },
        description: {
          fr: 'Nous protégeons les gens contre l\'arnaque aux liens d\'hameçonnage.',
          en: 'We are saving people from getting scammed to phishing links'
        },
        logoUrl: 'https://res.cloudinary.com/dcooc6vig/image/upload/v1691769965/meedsdao-site/assets/images/upcoming%20hubs/Vigilance_ltuwgl.jpg',
        backgroundColor: '#5400CD',
        websiteUrl: 'https://www.vigilancedao.org/',
        discordUrl: 'https://discord.gg/xUSf2zdYmD',
      },
      {
        address: '0xfefefefefefefefefefef8',
        name: {
          fr: 'Communauté Tech Fleet DAO',
          en: 'Tech Fleet Community DAO'
        },
        description: {
          fr: 'Développement professionnel et renforcement des compétences dans des rôles techniques tels que l\'UX, la gestion de produits et le développement',
          en: 'Professional development and upskilling in tech roles like UX, product management, and dev'
        },
        logoUrl: 'https://res.cloudinary.com/dcooc6vig/image/upload/v1689868155/meedsdao-site/assets/images/upcoming%20hubs/tech_fleet_c3smje.webp',
        backgroundColor: '#01061E',
        websiteUrl: 'https://techfleet.org/',
        twitterURL: 'https://twitter.com/techfleetworks',
      },
    ],
  }),
  computed: Vuex.mapState({
    language: state => state.language,
    formLink: state => state.formLink,
    hubsCount() {
      return this.filteredHubs.length + this.filteredUpcomingHubs.length;
    },
    filteredHubs() {
      if (this.keyword) {
        if (this.language === 'fr') {
          return this.hubs.filter(hub => hub?.name?.fr?.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0 || hub?.description?.fr?.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0);
        } else {
          return this.hubs.filter(hub => hub?.name?.en?.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0 || hub?.description?.en?.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0);
        }
      } 
      else {
        return this.hubs; 
      }
    },
    filteredUpcomingHubs() {
      if (this.keyword) {
        if (this.language === 'fr') {
          return this.upcomingHubs.filter(hub => hub?.name?.fr?.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0 || hub?.description?.fr?.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0);
        } else {
          return this.upcomingHubs.filter(hub => hub?.name?.en?.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0 || hub?.description?.en?.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0);
        }
      } 
      else {
        return this.upcomingHubs; 
      }
    },
  }),
  created() {
    this.hubs = this.$utils.sortByName(this.hubs, this.language);
    this.upcomingHubs = this.$utils.sortByName(this.upcomingHubs, this.language);
    this.retrieveHubs();
  },
  methods: {
    retrieveHubs() {
      this.loading = true;
      this.$hubService.getHubs({
        page: this.page,
        size: this.pageSize,
      })
        .then(data => {
          const hubs = data?._embedded?.hubs;
          if (hubs?.length) {
            this.hubs.push(...hubs);
            this.hasMore = data.page.totalPages > (this.page + 1);
          } else {
            this.hasMore = false;
          }
        })
        .finally(() => this.loading = false);
    },
  },
};
</script>