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
            ref="marketplace"
            id="marketplace"
            :href="marketplaceURL"
            value="marketplace"
            class="px-0 full-height"
            link
            @click="openPage">
            <h3 class="mx-2 font-size-normal">{{ $t('page.marketplace') }}</h3>
            <v-icon class="mb-1 mt-2">fas fa-store</v-icon>
            <v-tabs-slider color="secondary" class="mobile-menu-slider" />
          </v-btn>
        </v-col>
        <v-col cols="auto" class="my-0 pa-0 flex-grow-1">
          <v-btn
            ref="buy"
            id="buy"
            value="buy"
            class="px-0 full-height"
            link
            @click.prevent.stop="$root.$emit('open-buy-meed-drawer', true)">
            <h3 class="mx-2 font-size-normal">{{ $t('buy') }}</h3>
            <v-icon class="font-normal font-weight-bold mb-1 mt-2">Ɱ</v-icon>
          </v-btn>
        </v-col>
        <v-col cols="auto" class="my-0 pa-0 flex-grow-1">
          <v-btn
            ref="portfolio"
            id="portfolio"
            :href="portfolioURL"
            value="portfolio"
            class="px-0 full-height"
            link
            @click="openPage">
            <h3 class="mx-2 font-size-normal">{{ $t('page.overview') }}</h3>
            <v-icon class="mb-1 mt-2">fas fa-coins</v-icon>
            <v-tabs-slider color="secondary" class="mobile-menu-slider" />
          </v-btn>
        </v-col>
        <v-col cols="auto" class="my-0 pa-0">
          <v-btn
            ref="more"
            id="more"
            value="more"
            class="px-0 full-height"
            link
            @click.prevent.stop="showMoreMenu = !showMoreMenu">
            <h3 class="mx-2 font-size-normal">{{ $t('more') }}</h3>
            <v-icon class="mb-1 mt-2">fas fa-ellipsis</v-icon>
            <v-tabs-slider color="secondary" class="mobile-menu-slider" />
          </v-btn>
        </v-col>
      </v-row>
    </v-bottom-navigation>
    <v-bottom-sheet
      v-model="showMoreMenu"
      content-class="mb-14"
      eager>
      <v-list
        :class="menuColor"
        dense>
        <v-list-item-group
          v-model="selectedId"
          :active-class="activeMenuColor"
          :mandatory="selectedTab === 'more'">
          <v-list-item
            ref="tenants"
            id="tenants"
            :href="tenantsURL"
            :active-class="activeMenuColor"
            key="tenants"
            value="tenants"
            dense
            @click="openPage">
            <v-list-item-icon>
              <v-icon>fas fa-building-user</v-icon>
            </v-list-item-icon>
            <v-list-item-title>{{ $t('page.tenants') }}</v-list-item-title>
          </v-list-item>
          <v-list-item
            ref="owners"
            id="owners"
            :href="ownersURL"
            :active-class="activeMenuColor"
            key="owners"
            value="owners"
            dense
            @click="openPage">
            <v-list-item-icon>
              <v-icon>fas fa-city</v-icon>
            </v-list-item-icon>
            <v-list-item-title>{{ $t('page.owners') }}</v-list-item-title>
          </v-list-item>
          <v-list-item
            ref="stake"
            id="stake"
            :href="stakeURL"
            :active-class="activeMenuColor"
            key="stake"
            value="stake"
            dense
            @click="openPage">
            <v-list-item-icon>
              <v-icon>fas fa-piggy-bank</v-icon>
            </v-list-item-icon>
            <v-list-item-title>{{ $t('page.stake') }}</v-list-item-title>
          </v-list-item>
          <v-list-item
            ref="deeds"
            id="deeds"
            :href="deedsURL"
            :active-class="activeMenuColor"
            key="deeds"
            value="deeds"
            dense
            @click="openPage">
            <v-list-item-icon>
              <v-icon class="transform-rotate-270">fas fa-trowel</v-icon>
            </v-list-item-icon>
            <v-list-item-title>{{ $t('page.deeds') }}</v-list-item-title>
          </v-list-item>
          <v-list-item
            ref="farm"
            id="farm"
            :href="farmURL"
            :active-class="activeMenuColor"
            key="farm"
            value="farm"
            dense
            @click="openPage">
            <v-list-item-icon>
              <v-icon>fas fa-sack-dollar</v-icon>
            </v-list-item-icon>
            <v-list-item-title>{{ $t('page.farm') }}</v-list-item-title>
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
        :href="hubsUrl"
        class="no-decoration black--text text--color"
        @click="openPage">
        <span class="font-weight-black title">{{ $t('page.hubs') }}</span>
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
              <v-img :src="`${parentLocation}/static/images/stake_menu.webp`" />
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
          <v-list-item :href="farmURL">
            <v-list-item-avatar
              height="25px"
              width="27px"
              min-width="27px"
              tile>
              <v-img :src="`${parentLocation}/static/images/farm_menu.webp`" />
            </v-list-item-avatar>
            <v-hover v-slot="{hover}">
              <v-list-item-content>
                <v-list-item-title class="d-flex">
                  <span class="font-weight-black"> {{ $t('page.farm') }} </span>
                  <v-icon
                    v-if="hover"
                    class="ms-2 black--text text--color "
                    size="10">
                    fa fa-chevron-right
                  </v-icon>
                </v-list-item-title>
                <v-list-item-subtitle>
                  <span  
                    :class="hover && 'black--text text--color'"
                    class="text-body-2"> 
                    {{ $t('page.farm.menu.description') }}
                  </span>
                </v-list-item-subtitle>
              </v-list-item-content>
            </v-hover>
          </v-list-item>
          <v-list-item :href="tokenomicsURL">
            <v-list-item-avatar
              height="25px"
              width="25px"
              min-width="25px"
              tile>
              <v-img :src="`${parentLocation}/static/images/token_menu.webp`" />
            </v-list-item-avatar>
            <v-hover v-slot="{hover}">
              <v-list-item-content>
                <v-list-item-title class="d-flex">
                  <span class="font-weight-black"> {{ $t('page.token') }} </span>
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
                    {{ $t('page.token.menu.description') }} 
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
                <v-img :src="`${parentLocation}/static/images/marketplace_menu.webp`" />
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
                <v-img :src="`${parentLocation}/static/images/tenants_menu.webp`" />
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
            <v-list-item :href="deedsURL">
              <v-list-item-avatar
                height="21px"
                width="25px"
                min-width="25px"
                tile>
                <v-img :src="`${parentLocation}/static/images/mint_menu.webp`" />
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
                <v-img :src="`${parentLocation}/static/images/owners_menu.webp`" />
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
    showMoreMenu: false,
    showBottomNavigation: true,
    selectedId: null,
    updatingMenu: false,
    dappPages: [
      'hubs',
      'marketplace',
      'tenants',
      'owners',
      'stake',
      'deeds',
      'farm',
      'portfolio',
    ],
  }),
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    portfolioURL: state => state.portfolioURL,
    marketplaceURL: state => state.marketplaceURL,
    deedsURL: state => state.deedsURL,
    stakeURL: state => state.stakeURL,
    ownersURL: state => state.ownersURL,
    farmURL: state => state.farmURL,
    tenantsURL: state => state.tenantsURL,
    tokenomicsURL: state => state.tokenomicsURL,
    language: state => state.language,
    isMobile: state => state.isMobile,
    dark: state => state.dark,
    networkId: state => state.networkId,
    validNetwork: state => state.validNetwork,
    page: state => state.page,
    pageUriPerLanguages: state => state.pageUriPerLanguages,
    hubsUrl: state => state.hubsUrl,
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
  }),
  watch: {
    isMobile() {
      this.initSelectedTab();
    },
    selectedTab(newVal, oldVal) {
      if (newVal === 'buy') {
        this.closeMobileMenuNow();
      }
      if (newVal === 'buy' || (newVal === 'more' && !this.selectedId)) {
        this.$nextTick(() => {
          this.updateSelection(oldVal, null);
        });
      } else if (this.selectedTab !== 'more' && this.selectedId) {
        this.updateSelection(newVal, null);
      }
    },
    selectedId() {
      if (this.selectedId) {
        this.$nextTick(() => {
          this.updateSelection('more', this.selectedId);
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
          this.selectedTab = (this.dappPages.indexOf(this.page) >= 0) && tabToSelect || `${this.parentLocation}/static`;
          if (this.isMobile && this.selectedTab !== 'marketplace' && this.selectedTab !== 'portfolio') {
            this.$nextTick().then(() => {
              this.selectedId = this.page;
              this.selectedTab = 'more';
            });
          }
        }
      }
    },
    updateSelection(tab, mobileMenuId) {
      if (this.updatingMenu) {
        return;
      }
      this.updatingMenu = true;
      if (this.selectedTab !== tab) {
        this.selectedTab = tab;
      }
      if (this.selectedTab !== 'more' && this.selectedId !== mobileMenuId) {
        this.selectedId = mobileMenuId;
      }
      this.$nextTick(() => this.updatingMenu = false);
    },
    switchPage(tab, avoidResetTab) {
      if (tab && this.$refs[tab]) {
        this.avoidResetTab = avoidResetTab;
        this.$root.$emit('close-drawer');
        this.$nextTick().then(() => this.$refs[tab].$el.click());
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
      if (this.selectedTab === 'more') {
        this.closeMobileMenuAnimate();
      } else {
        this.closeMobileMenuNow();
      }
      window.scrollTo(0, 0);
    },
    closeMobileMenuAnimate() {
      window.setTimeout(() => this.showMoreMenu = false, 300);
    },
    closeMobileMenuNow() {
      this.showMoreMenu = false;
    },
  },
};
</script>