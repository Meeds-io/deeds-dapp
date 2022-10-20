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
        class="ps-2 pe-0"
        v-bind="attrs"
        v-on="on">
        <div>{{ selectedLanguageLabel }}</div>
        <v-icon size="12" class="mx-2">fa fa-caret-down</v-icon>
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
    language: state => state.language,
    isMobile() {
      return this.$vuetify.breakpoint.smAndDown;
    },
    selectedLanguageLabel() {
      return this.isMobile ? this.language : this.$t(`language.${this.language}`);
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
      this.$store.commit('selectLanguage', lang);
    },
  },
};
</script>