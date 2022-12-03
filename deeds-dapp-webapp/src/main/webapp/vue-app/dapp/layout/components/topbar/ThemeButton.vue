<template>
  <div>
    <v-tooltip
      v-if="topbar"
      z-index="4"
      max-width="300px"
      bottom>
      <template #activator="{ on, attrs }">
        <v-card
          :class="!displayThemeButton && 'd-none' || cssClass"
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
    <v-list-item
      v-else-if="isTestNetwork"
      key="dayNight"
      dense>
      <v-switch
        :value="dark"
        :prepend-icon="`${themeButtonIcon} ${themeButtonColorText} me-10`"
        class="my-auto ms-5"
        inset
        dense
        hide-details
        @click="switchThemeMode" />
    </v-list-item>
  </div>
</template>
<script>
export default {
  props: {
    topbar: {
      type: Boolean,
      default: false,
    },
    cssClass: {
      type: String,
      default: () => '',
    }
  },
  computed: Vuex.mapState({
    isMobile: state => state.isMobile,
    dark: state => state.dark,
    themePreference: state => state.themePreference,
    systemThemeDark: state => state.systemThemeDark,
    networkId: state => state.networkId,
    validNetwork: state => state.validNetwork,
    isTestNetwork() {
      return this.networkId !== 1 && this.validNetwork;
    },
    displayThemeButton() {
      return !this.isMobile && this.isTestNetwork;
    },
    isSystemThemePreferred() {
      return this.themePreference === 'system';
    },
    themeButtonColor() {
      return this.dark && 'primary' || 'amber lighten-2';
    },
    themeButtonColorText() {
      return this.dark && 'primary--text' || 'amber--text text--lighten-2';
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
      if (this.topbar) {
        if (this.themePreference === 'system') {
          preference = this.systemThemeDark && 'light' || 'dark';
        } else if (this.themePreference === 'light') {
          preference = this.systemThemeDark && 'dark' || 'system';
        } else if (this.themePreference === 'dark') {
          preference = this.systemThemeDark && 'system' || 'light';
        }
      } else {
        preference = this.dark ? 'light' : 'dark';
      }
      this.$store.commit('setThemePreference', preference);
    },
  },
};
</script>