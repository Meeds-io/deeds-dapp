<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 
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
  <div v-if="isMobile">
    <v-bottom-navigation
      v-model="selectedTab"
      :class="menuColor"
      :input-value="showBottomNavigation"
      style="z-index: 250;"
      active-class="selected-item"
      class="bottom-navigation justify-start"
      fixed
      grow>
      <v-row class="ma-0 pa-0 flex-nowrap d-flex" no-gutters>
        <v-col cols="auto" class="my-0 pa-0 flex-grow-1">
          <v-btn
            ref="why-meeds"
            id="why-meeds"
            :href="whyMeedsUrl"
            value="why-meeds"
            class="px-0 full-height"
            link>
            <h3 class="mx-2 font-size-normal" :class="whyMeedsTextColor">{{ $t('whyMeeds') }}</h3>
            <img
              :src="`${parentLocation}/static/images/tenantsMenu.webp`"
              alt=""
              height="27px"
              width="24px"
              min-width="24px"
              class="my-1 object-fit-cover"
              :class="positionWhyMeedsIconTab">
          </v-btn>
        </v-col>
        <v-col cols="auto" class="my-0 pa-0 flex-grow-1">
          <v-btn
            ref="tokens"
            id="tokens"
            value="tokens"
            class="px-0 full-height"
            link
            @click.prevent.stop="tokensMenu = !tokensMenu">
            <h3 class="mx-2 font-size-normal" :class="textTokensColor">{{ $t('tokens.title') }}</h3>
            <img
              :src="`${parentLocation}/static/images/tokenMenu.webp`"
              alt=""
              height="25px"
              width="25px"
              min-width="25px"
              class="my-1 object-fit-cover"
              :class="positionTokensIconTab">
          </v-btn>
        </v-col>
        <v-col cols="auto" class="my-0 pa-0 flex-grow-1">
          <v-btn
            ref="nft"
            id="nft"
            value="nft"
            class="px-0 full-height"
            link
            @click.prevent.stop="nftMenu = !nftMenu">
            <h3 class="mx-2 font-size-normal" :class="textNftColor">{{ $t('nft.id') }}</h3>
            <img
              :src="`${parentLocation}/static/images/ownersMenu.webp`"
              alt=""
              height="25px"
              width="24px"
              min-width="24px"
              class="my-1 object-fit-cover"
              :class="positionNftIconTab">
          </v-btn>
        </v-col>
      </v-row>
    </v-bottom-navigation>
    <v-bottom-sheet
      v-model="tokensMenu"
      content-class="mb-14"
      eager>
      <v-list
        :class="menuColor"
        dense>
        <v-list-item-group
          v-model="selectedTokensId"
          :active-class="activeMenuColor"
          :mandatory="selectedTab === 'tokens'">
          <v-list-item
            ref="stake"
            id="stake"
            :href="stakeURL"
            :active-class="activeMenuColor"
            class="py-2"
            key="stake"
            value="stake"
            dense
            @click="openPage">
            <v-list-item-icon>
              <v-img
                :src="`${parentLocation}/static/images/stakeMenu.webp`"
                position="right"
                height="30px"
                width="24px"
                min-width="24px"
                class="my-1" />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title class="text-sub-title font-weight-bold font-size-normal">{{ $t('page.stake') }}</v-list-item-title>
              <v-list-item-subtitle class="caption">{{ $t('page.stake.menu.description') }}</v-list-item-subtitle>
            </v-list-item-content>
          </v-list-item>
        </v-list-item-group>
      </v-list>
    </v-bottom-sheet>
    <v-bottom-sheet
      v-model="nftMenu"
      content-class="mb-14"
      eager>
      <v-list
        :class="menuColor"
        dense>
        <v-list-item-group
          v-model="selectedNftId"
          :active-class="activeMenuColor"
          :mandatory="selectedTab === 'nft'">
          <v-list-item
            ref="marketplace"
            id="marketplace"
            :href="marketplaceURL"
            :active-class="activeMenuColor"
            class="py-2"
            key="marketplace"
            value="marketplace"
            dense
            @click="openPage">
            <v-list-item-icon>
              <v-img
                :src="`${parentLocation}/static/images/marketplaceMenu.webp`"
                position="right"
                height="26px"
                width="24px"
                min-width="24px"
                class="my-1" />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title class="text-sub-title font-weight-bold font-size-normal">{{ $t('page.marketplace') }}</v-list-item-title>
              <v-list-item-subtitle class="caption">{{ $t('page.marketplace.menu.description') }}</v-list-item-subtitle>
            </v-list-item-content>
          </v-list-item>
          <v-list-item
            ref="tenants"
            id="tenants"
            :href="tenantsURL"
            :active-class="activeMenuColor"
            class="py-2"
            key="tenants"
            value="tenants"
            dense
            @click="openPage">
            <v-list-item-icon>
              <v-img
                :src="`${parentLocation}/static/images/tenantsMenu.webp`"
                position="right"
                height="26px"
                width="24px"
                min-width="24px"
                class="my-1" />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title class="text-sub-title font-weight-bold font-size-normal">{{ $t('page.tenants') }}</v-list-item-title>
              <v-list-item-subtitle class="caption">{{ $t('page.tenants.menu.description') }}</v-list-item-subtitle>
            </v-list-item-content>
          </v-list-item>
          <v-list-item
            ref="mint"
            id="mint"
            :href="mintUrl"
            :active-class="activeMenuColor"
            class="py-2"
            key="mint"
            value="mint"
            dense
            @click="openPage">
            <v-list-item-icon>
              <v-img
                :src="`${parentLocation}/static/images/mintMenu.webp`"
                position="right"
                height="20px"
                width="24px"
                min-width="24px"
                class="my-1" />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title class="text-sub-title font-weight-bold font-size-normal">{{ $t('page.deeds') }}</v-list-item-title>
              <v-list-item-subtitle class="caption">{{ $t('page.deeds.menu.description') }}</v-list-item-subtitle>
            </v-list-item-content>
          </v-list-item>
          <v-list-item
            ref="owners"
            id="owners"
            :href="ownersURL"
            :active-class="activeMenuColor"
            class="py-2"
            key="owners"
            value="owners"
            dense
            @click="openPage">
            <v-list-item-icon>
              <v-img
                :src="`${parentLocation}/static/images/ownersMenu.webp`"
                position="right"
                height="26px"
                width="24px"
                min-width="24px"
                class="my-1" />
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title class="text-sub-title font-weight-bold font-size-normal">{{ $t('page.owners') }}</v-list-item-title>
              <v-list-item-subtitle class="caption">{{ $t('page.owners.menu.description') }}</v-list-item-subtitle>
            </v-list-item-content>
          </v-list-item>
        </v-list-item-group>
      </v-list>
    </v-bottom-sheet>
  </div>
  <v-row
    v-else
    class="flex-nowrap d-flex"
    no-gutters>
    <v-col cols="auto" class="d-flex justify-center align-center ps-15 ms-16 me-8">
      <a
        :href="whyMeedsUrl"
        class="no-decoration black--text text--color">
        <span class="font-weight-black title">{{ $t('whyMeeds') }}</span>
      </a>
    </v-col>
    <v-col cols="auto" class="d-flex justify-center align-center mx-8">
      <deeds-topbar-menu>
        <template #activator="{ on, attrs }">
          <div 
            v-bind="attrs"
            v-on="on">
            <span class="font-weight-black title"> {{ $t('tokens.title') }} </span>
          </div>
        </template>
        <v-list 
          width="400px" 
          max-width="400px">
          <v-list-item :href="stakeURL">
            <v-list-item-avatar
              height="31px"
              width="25px"
              min-width="25px"
              tile>
              <v-img 
                :src="`${parentLocation}/static/images/stakeMenu.webp`"
                position="left" />
            </v-list-item-avatar>
            <v-hover v-slot="{hover}">
              <v-list-item-content>
                <v-list-item-title class="d-flex">
                  <span class="font-weight-black"> {{ $t('page.stake') }} </span>
                  <v-icon
                    v-if="hover"
                    class="ms-2 black--text text--color"
                    size="10">
                    fa fa-chevron-right
                  </v-icon>
                </v-list-item-title>
                <v-list-item-subtitle>
                  <span  
                    :class="hover && 'black--text text--color'"
                    class="text-body-2"> 
                    {{ $t('page.stake.menu.description') }} 
                  </span>
                </v-list-item-subtitle>
              </v-list-item-content>
            </v-hover>
          </v-list-item>
        </v-list>
      </deeds-topbar-menu>
    </v-col>
    <v-col cols="auto" class="d-flex justify-center align-center mx-8">
      <deeds-topbar-menu>
        <template #activator="{ on, attrs }">
          <div 
            v-bind="attrs"
            v-on="on">
            <span class="font-weight-black title"> {{ $t('nft.id') }} </span>
          </div>
        </template>
        <div class="d-flex">
          <v-list 
            width="300px" 
            max-width="300px">
            <v-list-item :href="marketplaceURL">
              <v-list-item-avatar
                height="27px"
                width="22px"
                min-width="22px"
                tile>
                <v-img 
                  :src="`${parentLocation}/static/images/marketplaceMenu.webp`"
                  position="left" />
              </v-list-item-avatar>
              <v-hover v-slot="{hover}">
                <v-list-item-content>
                  <v-list-item-title class="d-flex">
                    <span class="font-weight-black"> {{ $t('page.marketplace') }} </span>
                    <v-icon
                      v-if="hover"
                      class="ms-2 black--text text--color"
                      size="10">
                      fa fa-chevron-right
                    </v-icon>
                  </v-list-item-title>
                  <v-list-item-subtitle>
                    <span  
                      :class="hover && 'black--text text--color'"
                      class="text-body-2">
                      {{ $t('page.marketplace.menu.description') }} 
                    </span>
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-hover>
            </v-list-item>
            <v-list-item :href="tenantsURL">
              <v-list-item-avatar
                height="26px"
                width="23px"
                min-width="23px"
                tile>
                <v-img 
                  :src="`${parentLocation}/static/images/tenantsMenu.webp`"
                  position="left" />
              </v-list-item-avatar>
              <v-hover v-slot="{hover}">
                <v-list-item-content>
                  <v-list-item-title class="d-flex">
                    <span class="font-weight-black"> {{ $t('page.tenants') }} </span>
                    <v-icon
                      v-if="hover"
                      class="ms-2 black--text text--color"
                      size="10">
                      fa fa-chevron-right
                    </v-icon>
                  </v-list-item-title>
                  <v-list-item-subtitle>
                    <span  
                      :class="hover && 'black--text text--color'"
                      class="text-body-2">
                      {{ $t('page.tenants.menu.description') }} 
                    </span>
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-hover>
            </v-list-item>
          </v-list>
          <v-list 
            width="350px" 
            max-width="350px">
            <v-list-item :href="mintUrl">
              <v-list-item-avatar
                height="21px"
                width="25px"
                min-width="25px"
                tile>
                <v-img 
                  :src="`${parentLocation}/static/images/mintMenu.webp`"
                  position="left" />
              </v-list-item-avatar>
              <v-hover v-slot="{hover}">
                <v-list-item-content>
                  <v-list-item-title class="d-flex">
                    <span class="font-weight-black"> {{ $t('page.deeds') }} </span>
                    <v-icon
                      v-if="hover"
                      class="ms-2 black--text text--color"
                      size="10">
                      fa fa-chevron-right
                    </v-icon>
                  </v-list-item-title>
                  <v-list-item-subtitle>
                    <span  
                      :class="hover && 'black--text text--color'"
                      class="text-body-2">
                      {{ $t('page.deeds.menu.description') }} 
                    </span>
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-hover>
            </v-list-item>
            <v-list-item :href="ownersURL">
              <v-list-item-avatar
                height="25px"
                width="26px"
                min-width="25px"
                tile>
                <v-img 
                  :src="`${parentLocation}/static/images/ownersMenu.webp`"
                  position="left" />
              </v-list-item-avatar>
              <v-hover v-slot="{hover}">  
                <v-list-item-content>
                  <v-list-item-title class="d-flex">
                    <span class="font-weight-black"> {{ $t('page.owners') }} </span>
                    <v-icon
                      v-if="hover"
                      class="ms-2 black--text text--color"
                      size="10">
                      fa fa-chevron-right
                    </v-icon>
                  </v-list-item-title>
                  <v-list-item-subtitle>
                    <span  
                      :class="hover && 'black--text text--color'"
                      class="text-body-2"> 
                      {{ $t('page.owners.menu.description') }} 
                    </span>
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-hover>
            </v-list-item>
          </v-list>
        </div>
      </deeds-topbar-menu>
    </v-col>
  </v-row>
</template>
<script>
export default {
  data: () => ({
    selectedTab: null,
    avoidAddToHistory: false,
    avoidResetTab: false,
    tokensMenu: false,
    nftMenu: false,
    showBottomNavigation: true,
    selectedTokensId: null,
    selectedNftId: null,
    updatingMenu: false,
    navTabs: [
      'why-meeds',
      'marketplace',
      'tenants',
      'owners',
      'stake',
      'mint',
      'farm',
    ],
  }),
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    marketplaceURL: state => state.marketplaceURL,
    mintUrl: state => state.mintUrl,
    stakeURL: state => state.stakeURL,
    ownersURL: state => state.ownersURL,
    tenantsURL: state => state.tenantsURL,
    language: state => state.language,
    isMobile: state => state.isMobile,
    dark: state => state.dark,
    networkId: state => state.networkId,
    validNetwork: state => state.validNetwork,
    page: state => state.page,
    pageUriPerLanguages: state => state.pageUriPerLanguages,
    whyMeedsUrl: state => state.whyMeedsUrl,
    menuColor() {
      return this.dark && 'grey darken-3' || 'grey lighten-3';
    },
    activeMenuColor() {
      return this.dark && 'grey darken-4' || 'grey lighten-2';
    },
    currentTabUri() {
      const currentPageIndex = this.pageUriPerLanguages['en'].pages.indexOf(this.page);
      return `${this.parentLocation}/${this.pageUriPerLanguages[this.language].uriPrefix}${this.pageUriPerLanguages[this.language].pages[currentPageIndex]}`;
    },
    selectedWhyMeedsTab() {
      return this.selectedTab && this.selectedTab === 'why-meeds';
    },
    selectedTokensTab() {
      return this.selectedTab && this.selectedTab === 'tokens';
    },
    selectedNftTab() {
      return this.selectedTab && this.selectedTab === 'nft';
    },
    positionWhyMeedsIconTab() {
      return this.selectedWhyMeedsTab && this.selectedWhyMeedsTab ? 'object-position-left' : 'object-position-right';
    },
    positionTokensIconTab() {
      return this.selectedTokensTab && this.selectedTokensTab ? 'object-position-left' : 'object-position-right';
    },
    positionNftIconTab() {
      return this.selectedNftTab && this.selectedNftTab ? 'object-position-left' : 'object-position-right';
    },
    whyMeedsTextColor() {
      return this.selectedWhyMeedsTab && this.selectedWhyMeedsTab ? 'secondary--text' : 'text-sub-title';
    },
    textTokensColor() {
      return this.selectedTokensTab && this.selectedTokensTab ? 'secondary--text' : 'text-sub-title';
    },
    textNftColor() {
      return this.selectedNftTab && this.selectedNftTab ? 'secondary--text' : 'text-sub-title';
    },
  }),
  watch: {
    isMobile() {
      this.initSelectedTab();
    },
    selectedTab(newVal, oldVal) {
      if ((newVal === 'tokens' && !this.selectedTokensId) || (newVal === 'nft' && !this.selectedNftId)) {
        this.$nextTick(() => {
          this.updateSelection(oldVal);
        });
      } else if ((this.selectedTab !== 'tokens' && this.selectedTokensId) || (this.selectedTab !== 'nft' && this.selectedNftId)) {
        this.updateSelection(newVal);
      }
    },
    selectedTokensId() {
      if (this.selectedTokensId) {
        this.$nextTick(() => {
          this.updateSelection('tokens');
        });
      }
    },
    selectedNftId() {
      if (this.selectedNftId) {
        this.$nextTick(() => {
          this.updateSelection('nft');
        });
      }
    },
  },
  created() {
    this.initSelectedTab();
    this.$root.$on('switch-page', this.switchPage);
    this.$root.$on('drawer-opened', this.closeBottomMenu);
    this.$root.$on('drawer-closed', this.openBottomMenu);
    window.addEventListener('popstate', this.initSelectedTab);
  },
  beforeDestroy() {
    this.$root.$off('switch-page', this.switchPage);
    this.$root.$off('drawer-opened', this.closeBottomMenu);
    this.$root.$off('drawer-closed', this.openBottomMenu);
    window.removeEventListener('popstate', this.initSelectedTab);
  },
  methods: {
    initSelectedTab(event) {
      let tabToSelect;
      if (this.isMobile) {
        tabToSelect = this.page;
      } else {
        tabToSelect = this.currentTabUri;
      }
      if (this.selectedTab === tabToSelect) {
        // Stay on the same tab, no change to apply
        // To let Tab apply its proper state
      } else {
        if (event) {
          this.avoidAddToHistory = true;
          this.switchPage(this.page);
        } else {
          this.selectedTab = (this.navTabs.indexOf(this.page) >= 0) && tabToSelect || `${this.parentLocation}/static`;
          if (this.isMobile && this.selectedTab !== 'why-meeds') {
            if (this.selectedTab === 'stake' || this.selectedTab === 'farm' || this.selectedTab === 'tokens') {
              this.selectedTokensId = this.page;
              this.selectedTab = 'tokens';
            } else if (this.selectedTab === 'marketplace' || this.selectedTab === 'tenants' || this.selectedTab === 'mint' || this.selectedTab === 'owners') {
              this.selectedNftId = this.page;
              this.selectedTab = 'nft';
            }
          }
        }
      }
    },
    updateSelection(tab) {
      if (this.updatingMenu) {
        return;
      }
      this.updatingMenu = true;
      if (this.selectedTab !== tab) {
        this.selectedTab = tab;
      }
      if (tab === 'tokens') {
        this.selectedNftId = null;
      } else if (tab === 'nft') {
        this.selectedTokensId = null;
      } else {
        this.selectedTokensId = null;
        this.selectedNftId = null;
      }
      this.$nextTick(() => this.updatingMenu = false);
    },
    switchPage(link, avoidResetTab) {
      if (link) {
        this.avoidResetTab = avoidResetTab;
        this.$root.$emit('close-drawer');
        window.history.pushState({}, '', link);
        this.$root.$emit('location-change', link);
      }
    },
    closeBottomMenu(_drawer, level) {
      if (level === 1) {
        this.showBottomNavigation = false;
      }
    },
    openBottomMenu(_drawer, level) {
      if (level === 1) {
        this.showBottomNavigation = true;
      }
    },
    openPage(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
        const link = event.target.href || event.target.parentElement && (event.target.parentElement.href || (event.target.parentElement.parentElement && event.target.parentElement.parentElement.href));
        if (link) {
          if (!this.avoidAddToHistory) {
            window.history.pushState({}, '', link);
          }
          this.avoidAddToHistory = false;
          this.$root.$emit('location-change', `/${event.target.id}`, link, this.avoidResetTab);
          this.avoidResetTab = false;
        }
        if (this.isMobile) {
          this.handlePageOpenedMobile();
        }
        this.$store.commit('refreshDocumentHead');
      }
    },
    handlePageOpenedMobile() {
      if (this.selectedTab === 'tokens' || this.selectedTab === 'nft') {
        this.closeMobileMenuAnimate();
      } else {
        this.closeMobileMenuNow();
      }
      window.scrollTo(0, 0);
    },
    closeMobileMenuAnimate() {
      window.setTimeout(() => {
        this.tokensMenu = false;
        this.nftMenu = false;
      }, 300);
    },
    closeMobileMenuNow() {
      this.tokensMenu = false;
      this.nftMenu = false;
    },
  },
};
</script>