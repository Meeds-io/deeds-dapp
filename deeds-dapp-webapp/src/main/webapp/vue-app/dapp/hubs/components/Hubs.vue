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
  <v-row class="my-15">
    <v-col cols="12">
      <deeds-hubs-introduction
        :reduced="selectedHub"
        @keyword-changed="keyword = $event" />
    </v-col>
    <v-scale-transition>
      <v-col v-show="!selectedHub" cols="12">
        <div class="d-flex flex-row flex-grow-1">
          <div v-if="keyword" class="headline font-weight-bold pb-1">{{ hubsCount }} {{ $t('hubs.hubsFound') }} </div>
          <div v-else class="headline font-weight-bold pb-1">{{ $t('hubs.title.featuredHubs') }}</div>
        </div>
      </v-col>
    </v-scale-transition>
    <v-scale-transition>
      <v-col v-show="selectedHub" cols="12">
        <deeds-hub-details :hub="selectedHub" />
      </v-col>
    </v-scale-transition>
    <template v-if="!selectedHub">
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
      <v-col v-if="!selectedHub" cols="12">
        <div class="d-flex flex-row flex-grow-1">
          <div class="headline font-weight-bold pt-10 pb-1">{{ $t('hubs.title.upcomingHubs') }}</div>
        </div>
      </v-col>
      <template v-if="!selectedHub">
        <v-col
          v-for="(hub, index) in filteredUpcomingHubs"
          :key="`${hub.address}-${index}`"
          class="d-flex justify-center"
          cols="12"
          lg="4"
          md="12">
          <v-slide-x-transition>
            <deeds-hub-upcoming-card :hub="hub" />
          </v-slide-x-transition>
        </v-col>
      </template>
      <v-col v-if="!selectedHub" cols="12">
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
    </template>
  </v-row>
</template>
<script>
export default {
  data: () => ({
    keyword: null,
    loading: false,
    pageSize: 10,
    page: -1,
    hasMore: false,
    hubs: [],
    selectedHub: null,
    upcomingHubs: [
      {
        address: '0xfefefefefefefefefefef3',
        name: {
          fr: 'Cabin',
          en: 'Cabin'
        },
        description: {
          fr: 'Cabin est une cité réseau qui met les gens en contact avec des expériences de coliving et des résidences de travail/séjour',
          en: 'Cabin is a network city that connects people to coliving experiences and work/stay residencies'
        },
        logoUrl: 'https://res.cloudinary.com/dcooc6vig/image/upload/v1689868155/meedsdao-site/assets/images/upcoming%20hubs/cabin_lqm3hf.webp',
        backgroundColor: '#FEE9CB',
        websiteUrl: 'https://cabin.city/',
        twitterURL: 'https://twitter.com/creatorcabins',
        discordUrl: 'https://discord.com/invite/TUaF464d9e',
        githubUrl: 'https://github.com/CabinDAO',
      },
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
    this.retrieveHubs();
    this.$root.$on('open-hub-details', this.openHubDetails);
    this.$root.$on('close-hub-details', this.closeHubDetails);
    this.$root.$on('hub-disconnection-success', this.closeHubDetailsAndRefresh);
  },
  methods: {
    openHubDetails(hub) {
      this.selectedHub = hub;
    },
    closeHubDetails() {
      this.selectedHub = null;
    },
    closeHubDetailsAndRefresh() {
      this.page = 0;
      this.hubs = [];
      this.retrieveHubs();
      this.closeHubDetails();
    },
    retrieveHubs() {
      this.loading = true;
      this.$hubService.getHubs({
        page: this.page,
        size: this.pageSize,
      })
        .then(data => {
          if (data?._embedded?.hubs?.length) {
            this.hubs.push(...data._embedded.hubs);
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