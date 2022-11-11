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
  <v-fab-transition v-if="isSelected || showOffer">
    <div v-show="showOffer" id="marketplaceSelectedOffer">
      <deeds-renting-offer-card
        :offer="offer"
        :selected-cards="selectedCards"
        :selected-offers="selectedOffers" />
    </div>
  </v-fab-transition>
  <deeds-renting-offer-card
    v-else
    :offer="offer"
    :selected-cards="selectedCards"
    :selected-offers="selectedOffers" />
</template>
<script>
export default {
  props: {
    offer: {
      type: Object,
      default: null,
    },
    selectedOffers: {
      type: Array,
      default: null,
    },
    selectedCards: {
      type: Array,
      default: null,
    },
  },
  data: () => ({
    showOffer: false,
  }),
  computed: Vuex.mapState({
    selectedOfferId: state => state.selectedOfferId,
    offerId() {
      return this.offer?.id;
    },
    isSelected() {
      return this.offerId === this.selectedOfferId;
    },
  }),
  watch: {
    isSelected() {
      this.animateSelectedOffer();
    },
  },
  mounted() {
    this.animateSelectedOffer();
  },
  methods: {
    animateSelectedOffer() {
      if (this.isSelected) {
        document.querySelector('#marketplaceOffersList').scrollIntoView({
          block: 'start',
        });
        window.setTimeout(() => {
          this.showOffer = true;
          this.$store.commit('setOfferId', null);
        }, 200);
      }
    },
  },
};
</script>