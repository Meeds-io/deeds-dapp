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
  <deeds-drawer
    ref="drawer"
    v-model="drawer"
    :permanent="sending"
    second-level
    @opened="$emit('opened')"
    @closed="$emit('closed')">
    <template #title>
      <h4>{{ $t('deedRentingTitle', {0: cardTypeName, 1: nftId}) }}</h4>
    </template>
    <template #content>
      <v-card-text>
        {{ $t('deedRentingDescription1') }}
        <ul>
          <li>{{ $t('deedRentingDescriptionBulletPoint1') }}</li>
          <li>{{ $t('deedRentingDescriptionBulletPoint2') }}</li>
          <li>{{ $t('deedRentingDescriptionBulletPoint3') }}</li>
        </ul>
        <div class="pt-4">{{ $t('deedRentingDescription2') }}</div>
      </v-card-text>
      <v-card-text class="d-flex flex-column flex-grow-1">
        <div class="d-flex align-center">
          <v-chip color="primary"><span class="font-weight-bold">1</span></v-chip>
          <span class="subtitle-1 ms-4">{{ $t('deedRentingStep1Title') }}</span>
        </div>
        <v-expand-transition>
          <v-card
            v-show="step === 1"
            class="flex-grow-1"
            flat>
            <v-card-text class="px-0">
              {{ $t('deedRentingStep1Description') }}
              <deeds-extended-textarea
                v-model="description"
                :placeholder="$t('deedRentingDescriptionPlaceholder')"
                :max-length="descriptionMaxLength"
                class="mt-1" />
            </v-card-text>
          </v-card>
        </v-expand-transition>
        <div class="d-flex align-center mt-4">
          <v-chip color="primary"><span class="font-weight-bold">2</span></v-chip>
          <span class="subtitle-1 ms-4">{{ $t('deedRentingStep2Title') }}</span>
        </div>
        <v-expand-transition>
          <v-card
            v-show="step === 2"
            class="flex-grow-1"
            flat>
            <v-card-text />
          </v-card>
        </v-expand-transition>
      </v-card-text>
    </template>
    <template #footer>
      <v-btn
        :min-width="minButtonsWidth"
        outlined
        text
        class="ms-auto me-2"
        name="cancelMoveIn"
        @click="close(nftId)">
        <span class="text-capitalize">
          {{ $t('cancel') }}
        </span>
      </v-btn>
      <v-btn
        :min-width="minButtonsWidth"
        name="moveInConfirmButton"
        color="tertiary"
        outlined
        depressed
        dark
        @click="confirm">
        <span class="text-capitalize">
          {{ $t('next') }}
        </span>
      </v-btn>
    </template>
  </deeds-drawer>
</template>
<script>
export default {
  data: () => ({
    nft: null,
    drawer: false,
    step: 0,
    description: null,
    descriptionMaxLength: 200,
    minButtonsWidth: 120,
  }),
  computed: Vuex.mapState({
    nftId() {
      return this.nft?.id;
    },
    cardTypeName() {
      return this.nft?.cardTypeName;
    },
  }),
  created() {
    this.$root.$on('deeds-rent-drawer', this.open);
    this.$root.$on('deeds-rent-close', this.close);
  },
  methods: {
    open(nft) {
      this.nft = nft;
      this.step = 1;
      this.description = null;
      this.$nextTick()
        .then(() => {
          if (this.$refs.drawer) {
            this.$refs.drawer.open();
          }
        });
    },
    close(nftId) {
      if (nftId === this.nftId) {
        this.$refs.drawer.close();
      }
    },
    confirm() {
      if (this.step === 1) {
        this.step = 2;
      } else {
        // TODO
      }
    },
  },
};
</script>