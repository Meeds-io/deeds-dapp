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
  <v-menu offset-y>
    <template #activator="{ on, attrs }">
      <v-btn
        name="languageSelectorButton"
        outlined
        text
        class="ps-2 pe-0 mb-4 mb-sm-0 mx-auto"
        width="98px"
        v-bind="attrs"
        v-on="on">
        <div :class="textColor">{{ selectedLanguageLabel }}</div>
        <v-icon size="12" class="mx-2 mt-n1px">fa fa-caret-down</v-icon>
      </v-btn>
    </template>
    <v-list>
      <v-list-item
        v-for="languageOption in languageOptions"
        :key="languageOption.value"
        @click="changeLanguage(languageOption.value)">
        <v-list-item-title>{{ languageOption.label }}</v-list-item-title>
      </v-list-item>
    </v-list>
  </v-menu>
</template>
<script>
export default {
  data: () => ({
    languages: ['en', 'fr'],
  }),
  computed: Vuex.mapState({
    isMobile: state => state.isMobile,
    selectedLanguage: state => state.language,
    parentLocation: state => state.parentLocation,
    language: state => state.language,
    dark: state => state.dark,
    textColor() {
      return this.dark && 'white--text' || 'text-sub-title';
    },
    selectedLanguageLabel() {
      return this.isMobile ? this.selectedLanguage : this.$t(`language.${this.selectedLanguage}`);
    },
    languageOptions() {
      return this.languages.map(language => ({
        value: language,
        label: this.isMobile ? language.toUpperCase() : this.$t(`language.${language}`),
      }));
    },
  }),
  methods: {
    changeLanguage(lang) {
      const pageName = window.location.pathname.split('/')[2];
      let uri = '';
      if (this.language === 'fr') {
        switch (pageName) {
        case 'place-de-marche': 
          uri = 'marketplace';
          break;
        case 'portefeuille': 
          uri = 'portfolio';
          break;
        case 'visite-guidee': 
          uri = 'tour';
          break;
        case 'livre-blanc': 
          uri = 'whitepaper';
          break;
        case 'tokenomics-fr': 
          uri = 'tokenomics';
          break;
        case 'qui-sommes-nous': 
          uri = 'about-us';
          break;
        case 'deeds-fr': 
          uri = 'deeds';
          break;
        case 'mentions-legales': 
          uri = 'legals';
          break;
        case 'rejoindre-dao': 
          uri = 'stake';
          break;
        case 'proprietaires': 
          uri = 'owners';
          break;
        case 'farm-fr': 
          uri = 'farm';
          break;
        case 'locataires': 
          uri = 'tenants';
          break;
        default: uri = '';
        }
      } else {
        switch (pageName) {
        case 'marketplace': 
          uri = 'place-de-marche';
          break;
        case 'portfolio': 
          uri = 'portefeuille';
          break;
        case 'tour': 
          uri = 'visite-guidee';
          break;
        case 'whitepaper': 
          uri = 'livre-blanc';
          break;
        case 'tokenomics': 
          uri = 'tokenomics-fr';
          break;
        case 'about-us': 
          uri = 'qui-sommes-nous';
          break;
        case 'deeds': 
          uri = 'deeds-fr';
          break;
        case 'legals': 
          uri = 'mentions-legales';
          break;
        case 'stake': 
          uri = 'rejoindre-dao';
          break;
        case 'owners': 
          uri = 'proprietaires';
          break;
        case 'farm': 
          uri = 'farm-fr';
          break;
        case 'tenants': 
          uri = 'locataires';
          break;
        default: uri = '';
        }
      }
      this.$store.commit('selectLanguage', lang);
      window.history.replaceState('', '', `${this.parentLocation}/${this.$t(uri)}`); 
      window.location.reload();     
    },
  },
};
</script>