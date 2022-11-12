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
  <v-card
    :loading="loading"
    v-touch="{
      left: () => moveToNext(),
      right: () => moveToPrevious(),
    }"
    min-height="calc(100vh - 150px)"
    flat>
    <keep-alive>
      <component :is="animationTransition">
        <div v-show="showOffer">
          <v-card
            max-height="1000px"
            min-width="100%"
            class="d-flex flex-column"
            flat>
            <div class="flex-grow-0">
              <deeds-marketplace-offer-card-details-topbar :offer="selectedOffer" />
            </div>
            <div>
              <deeds-marketplace-offer-card-details-nav
                :loading="loading"
                :has-next="hasNext"
                :has-previous="hasPrevious"
                @next="moveToNext"
                @previous="moveToPrevious" />
            </div>
            <template v-if="!loading">
              <template v-if="offer">
                <deeds-marketplace-offer-card-details-charcteristics
                  :offer="selectedOffer"
                  :image-size="imageSize"
                  :max-image-size="maxImageSize"
                  class="flex-grow-0" />
                <deeds-marketplace-offer-card-details-rental
                  :offer="selectedOffer"
                  :left-size="imageSize"
                  :max-left-size="maxImageSize"
                  class="flex-grow-0" />
                <div class="flex-grow-1 d-flex justify-end">
                  <deeds-marketplace-offer-card-details-footer
                    :offer="selectedOffer" />
                </div>
              </template>
              <template v-else>
                <deeds-marketplace-deeds-empty offer-not-found />
              </template>
            </template>
          </v-card>
        </div>
      </component>
    </keep-alive>
  </v-card>
</template>
<script>
export default {
  props: {
    offer: {
      type: Object,
      default: null,
    },
    hasNext: {
      type: Boolean,
      default: false,
    },
    hasPrevious: {
      type: Boolean,
      default: false,
    },
    loading: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    selectedOffer: null,
    imageSize: '127px',
    maxImageSize: '30vw',
    showOffer: true,
    reverseAnimation: false,
  }),
  computed: {
    animationTransition() {
      return this.reverseAnimation && 'v-slide-x-reverse-transition' || 'v-slide-x-transition';
    },
  },
  watch: {
    offer() {
      if (this.selectedOffer && this.offer) {
        this.animateRefreshOffer();
      } else {
        this.refreshOffer();
      }
    },
  },
  created() {
    this.refreshOffer();
  },
  methods: {
    animateRefreshOffer() {
      this.showOffer = false;
      this.$nextTick().then(() => {
        window.setTimeout(() => {
          this.refreshOffer();
          this.showOffer = true;
        }, 200);
      });
    },
    refreshOffer() {
      this.selectedOffer = this.offer;
    },
    moveToPrevious() {
      this.reverseAnimation = true;
      this.$emit('previous');
    },
    moveToNext() {
      this.reverseAnimation = false;
      this.$emit('next');
    },
  },
};
</script>