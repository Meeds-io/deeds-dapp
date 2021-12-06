<template>
  <v-bottom-navigation
    v-model="selectedTab"
    width="fit-content"
    height="38px"
    class="elevation-1 my-4 rounded-lg justify-start text-truncate">
    <v-btn
      ref="snapshot"
      :class="selectedTab === 'snapshot' && 'primary white--text' || ''"
      link
      href="./snapshot"
      value="snapshot"
      @click="openPage">
      <h3 class="px-2">{{ $t('page.snapshot') }}</h3>
    </v-btn>
    <v-btn
      ref="stake"
      :class="selectedTab === 'stake' && 'primary white--text' || ''"
      link
      href="./stake"
      value="stake"
      revert
      @click="openPage">
      <h3 class="px-2">{{ $t('page.stake') }}</h3>
    </v-btn>
    <v-btn
      ref="deeds"
      :class="selectedTab === 'deeds' && 'primary white--text' || ''"
      link
      href="./deeds"
      value="deeds"
      revert
      @click="openPage">
      <h3 class="px-2">{{ $t('page.deeds') }}</h3>
    </v-btn>
  </v-bottom-navigation>
</template>
<script>
export default {
  data: () => ({
    defaultTab: 'snapshot',
    selectedTab: null,
  }),
  created() {
    const href = window.location.pathname;
    const hrefParts = href.split('/');
    let selectedTab = hrefParts[hrefParts.length - 1];
    if (!selectedTab || !selectedTab.length) {
      selectedTab = this.defaultTab;
    }
    this.selectedTab = selectedTab;
    this.$root.$on('switch-page', this.switchPage);
  },
  methods: {
    switchPage(tab) {
      this.selectedTab = tab;
      this.openPage();
    },
    openPage(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
        const link = event.target.href || event.target.parentElement && (event.target.parentElement.href || (event.target.parentElement.parentElement && event.target.parentElement.parentElement.href));
        if (link) {
          window.history.pushState({}, '', link);
          this.$root.$emit('location-change');
        }
      } else if (this.selectedTab && this.$refs[this.selectedTab]) {
        const link = this.$refs[this.selectedTab].href;
        if (link) {
          window.history.pushState({}, '', link);
          this.$root.$emit('location-change');
        }
      }
    }
  },
};
</script>