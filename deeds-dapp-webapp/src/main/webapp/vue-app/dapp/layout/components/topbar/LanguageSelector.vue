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
      let label ='';
      if (this.language === 'fr') {
        switch (pageName) {
        case 'place-de-marche': 
          label = 'marketplace';
          break;
        case 'portefeuille': 
          label = 'portfolio';
          break;
        case 'visite-guidee': 
          label = 'tour';
          break;
        case 'livre-blanc': 
          label = 'whitepaper';
          break;
        case 'tokenomics-fr': 
          label = 'tokenomics';
          break;
        case 'qui-sommes-nous': 
          label = 'about-us';
          break;
        case 'deeds-fr': 
          label = 'deeds';
          break;
        case 'mentions-legales': 
          label = 'legals';
          break;
        case 'rejoindre-dao': 
          label = 'stake';
          break;
        case 'proprietaires': 
          label = 'owners';
          break;
        case 'farm-fr': 
          label = 'farm';
          break;
        case 'locataires': 
          label = 'tenants';
          break;
        default: label = '';
        }
      } else {
        switch (pageName) {
        case 'marketplace': 
          label = 'place-de-marche';
          break;
        case 'portfolio': 
          label = 'portefeuille';
          break;
        case 'tour': 
          label = 'visite-guidee';
          break;
        case 'whitepaper': 
          label = 'livre-blanc';
          break;
        case 'tokenomics': 
          label = 'tokenomics-fr';
          break;
        case 'about-us': 
          label = 'qui-sommes-nous';
          break;
        case 'deeds': 
          label = 'deeds-fr';
          break;
        case 'legals': 
          label = 'mentions-legales';
          break;
        case 'stake': 
          label = 'rejoindre-dao';
          break;
        case 'owners': 
          label = 'proprietaires';
          break;
        case 'farm': 
          label = 'farm-fr';
          break;
        case 'tenants': 
          label = 'locataires';
          break;
        default: label = '';
        }
      }
      this.$store.commit('selectLanguage', lang);
      window.history.replaceState('', '', `${this.parentLocation}/${this.$t(label)}`); 
      window.location.reload();     
    },
  },
};
</script>