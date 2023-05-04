<template>
  <div>
    <v-tooltip
      z-index="4"
      max-width="300px"
      bottom>
      <template #activator="{ on, attrs }">
        <v-card
          :class="cssClass"
          color="transparent"
          flat
          v-bind="attrs"
          v-on="on">
          <v-btn
            :color="themeButtonColor"
            :outlined="isSystemThemePreferred"
            height="36"
            width="36"
            icon
            @click="switchThemeMode">
            <v-icon>{{ themeButtonIcon }}</v-icon>
          </v-btn>
        </v-card>
      </template>
      <span>{{ themeButtonTooltip }}</span>
    </v-tooltip>
  </div>
</template>
<script>
export default {
  props: {
    cssClass: {
      type: String,
      default: () => '',
    }
  },
  computed: Vuex.mapState({
    dark: state => state.dark,
    themePreference: state => state.themePreference,
    systemThemeDark: state => state.systemThemeDark,
    isSystemThemePreferred() {
      return this.themePreference === 'system';
    },
    themeButtonColor() {
      return this.dark && 'white' || 'amber lighten-2';
    },
    themeButtonIcon() {
      return this.dark && 'fas fa-moon' || 'fas fa-sun';
    },
    themeButtonTooltip() {
      switch (this.themePreference) {
      case 'system': return this.$t('systemTheme');
      case 'dark': return this.$t('darkTheme');
      case 'light': return this.$t('lightTheme');
      }
      return '';
    },
  }),
  methods: {
    switchThemeMode() {
      let preference = 'light';
      if (this.themePreference === 'system') {
        preference = this.systemThemeDark && 'light' || 'dark';
      } else if (this.themePreference === 'light') {
        preference = this.systemThemeDark && 'dark' || 'system';
      } else if (this.themePreference === 'dark') {
        preference = this.systemThemeDark && 'system' || 'light';
      }
      this.$store.commit('setThemePreference', preference);
    },
  },
};
</script>