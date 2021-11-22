<template>
  <v-menu offset-y>
    <template v-slot:activator="{ on, attrs }">
      <v-btn
        elevation="1"
        color="grey lighten-5"
        class="ps-2 pe-0"
        v-bind="attrs"
        v-on="on">
        <div>{{ selectedLanguageLabel }}</div>
        <v-icon>mdi-menu-down</v-icon>
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
    selectedLanguage: state => state.language,
    selectedLanguageLabel() {
      return this.$t(`language.${this.selectedLanguage}`);
    },
    languageOptions() {
      return this.languages.map(language => ({
        value: language,
        label: this.$t(`language.${language}`),
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