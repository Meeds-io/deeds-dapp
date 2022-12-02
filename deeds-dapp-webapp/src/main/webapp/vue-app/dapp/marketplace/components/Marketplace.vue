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
  <div class="d-flex flex-column mt-8">
    <v-scale-transition>
      <div v-show="!selectedStandaloneOfferId">
        <deeds-marketplace-introduction />
      </div>
    </v-scale-transition>
    <deeds-marketplace-deeds />
    <deeds-manage-rent-offer-drawer />
    <deeds-marketplace-offer-rent-drawer />
  </div>
</template>
<script>
export default {
  computed: Vuex.mapState({
    selectedStandaloneOfferId: state => state.selectedStandaloneOfferId,
  }),
  watch: {
    selectedStandaloneOfferId() {
      this.refreshUrl();
    },
  },
  created() {
    window.addEventListener('popstate', this.goBackLink);
    this.$root.$on('location-change', this.refreshSelectedOfferId);
    this.init();
  },
  mounted() {
    this.refreshUrl();
  },
  beforeDestroy() {
    window.removeEventListener('popstate', this.goBackLink);
    this.$root.$off('location-change', this.refreshSelectedOfferId);
  },
  methods: {
    init() {
      this.$store.commit('installRentingListeners');
    },
    goBackLink(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
        this.refreshSelectedOfferId();
      }
    },
    refreshSelectedOfferId(_location, link, avoidResetTab) {
      if (link && (!link.includes('marketplace') || avoidResetTab)) {
        return;
      }
      const offerId = this.$utils.getQueryParam('offer');
      if (offerId) {
        this.$store.commit('setStandaloneOfferId', offerId);
      } else {
        this.$store.commit('setStandaloneOfferId', null);
      }
    },
    refreshUrl() {
      const link = this.selectedStandaloneOfferId
          && `${window.location.pathname}?offer=${this.selectedStandaloneOfferId}`
          || window.location.pathname;
      if (!window.location.href.endsWith(link)) {
        const fullLink = `${origin}${link}`;
        this.$nextTick().then(() =>
          window.history.pushState({}, '', fullLink)
        );
      }
    },
  }
};
</script>