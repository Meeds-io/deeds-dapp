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
  <v-navigation-drawer
    v-model="drawer"
    v-click-outside="onClickOutside"
    :width="width"
    :hide-overlay="firstLevel || isMobile"
    :permanent="permanentDrawer"
    :color="whiteThemeColor"
    :overlay-opacity="overlayOpacity"
    temporary
    right
    fixed
    height="100%"
    max-height="100%"
    max-width="100vw"
    class="d-sm-flex drawerParent">
    <v-container
      fill-height
      class="pa-0 full-width">
      <v-layout column>
        <template v-if="$slots.title">
          <v-flex class="mx-0 drawerHeader flex-grow-0">
            <v-list-item class="pe-0">
              <v-list-item-action
                v-if="secondLevel"
                class="me-2"
                @click="close">
                <v-btn
                  :title="$t('label.close')"
                  name="closeDrawer"
                  icon
                  @click="close">
                  <v-icon class="heading">fas fa-arrow-left</v-icon>
                </v-btn>
              </v-list-item-action>
              <v-list-item-content class="drawerTitle align-start text-header-title text-truncate">
                <slot name="title"></slot>
              </v-list-item-content>
              <v-list-item-action class="drawerIcons align-end d-flex flex-row">
                <slot name="titleIcons"></slot>
                <v-btn
                  v-if="!isMobile && !secondLevel"
                  name="expandDrawer"
                  icon
                  @click="toogleExpand">
                  <v-icon v-text="expandIcon" size="18" />
                </v-btn>
                <v-btn
                  :title="$t('label.close')"
                  name="closeDrawer"
                  icon
                  @click="close">
                  <v-icon>mdi-close</v-icon>
                </v-btn>
              </v-list-item-action>
            </v-list-item>
          </v-flex>
          <v-progress-linear
            v-if="loading"
            indeterminate
            color="primary" />
          <v-divider v-else class="my-0" />
        </template>
        <v-flex class="drawerContent flex-grow-1 border-box-sizing d-flex flex-column overflow-y-auto overflow-x-hidden">
          <slot name="content"></slot>
        </v-flex>
        <template v-if="$slots.footer">
          <v-divider class="my-0" />
          <v-flex v-if="$slots.footer" class="drawerFooter border-box-sizing d-flex flex-grow-0 px-4 py-3 full-width justify-end">
            <slot name="footer"></slot>
          </v-flex>
        </template>
      </v-layout>
    </v-container>
  </v-navigation-drawer>
</template>

<script>
export default {
  props: {
    value: {
      type: Boolean,
      default: () => false,
    },
    fixed: {
      type: Boolean,
      default: () => false,
    },
    drawerWidth: {
      type: String,
      default: () => '420px',
    },
    allowExpand: {
      type: Boolean,
      default: false,
    },
    disablePullToRefresh: {
      type: Boolean,
      default: false,
    },
    permanent: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    drawer: false,
    loading: false,
    expand: true,
    level: 1,
    permanentBehavior: false,
  }),
  computed: Vuex.mapState({
    isMobile: state => state.isMobile,
    scrollbarWidth: state => state.scrollbarWidth,
    whiteThemeColor: state => state.whiteThemeColor,
    dark: state => state.dark,
    openedDrawersCount: state => state.openedDrawersCount,
    width() {
      return this.expand && '100%' || this.drawerWidth;
    },
    expandIcon() {
      return this.expand && 'mdi-arrow-collapse' || 'mdi-arrow-expand';
    },
    overlayOpacity() {
      return this.dark && '0.8' || '0.46';
    },
    firstLevel() {
      return this.level === 1 && this.openedDrawersCount > this.level;
    },
    secondLevel() {
      return this.level > 1;
    },
    permanentDrawer() {
      return this.permanentBehavior || this.permanent || (this.firstLevel && this.drawer);
    },
    hideOverlay() {
      return this.firstLevel || this.isMobile;
    },
  }),
  watch: {
    value() {
      if (this.value && !this.drawer) {
        this.open();
      } else if (!this.value && this.drawer) {
        this.close();
      }
    },
    drawer() {
      if (this.drawer) {
        this.permanentBehavior = true;
        this.$emit('opened');
        this.$store.commit('incrementOpenedDrawer');
        this.level = this.openedDrawersCount;
        this.$root.$emit('drawer-opened', this, this.level);
        if (!this.secondLevel) {
          if (this.disablePullToRefresh) {
            document.body.style.overscrollBehaviorY = 'contain';
          }
          if (window.innerHeight < window.document.body.offsetHeight) {
            Object.assign(window.document.querySelector('html').style, {overflowY: 'hidden', marginRight: `${this.scrollbarWidth}px`});
            Object.assign(window.document.querySelector('#banner').style, {overflowY: 'hidden', paddingRight: `${this.scrollbarWidth}px`});
          }
        }
      } else {
        this.permanentBehavior = false;
        this.$emit('closed');
        this.$store.commit('decrementOpenedDrawer');
        this.$root.$emit('drawer-closed', this, this.level);
        if (!this.secondLevel) {
          if (this.disablePullToRefresh) {
            document.body.style.overscrollBehaviorY = '';
          }
          Object.assign(window.document.querySelector('html').style, {overflowY: '', marginRight: ''});
          Object.assign(window.document.querySelector('#banner').style, {overflowY: '', paddingRight: ''});
        }
      }
      this.$emit('input', this.drawer);
      this.expand = false;
    },
  },
  created() {
    document.addEventListener('keydown', this.closeByEscape);
    this.$root.$on('close-drawer', this.closeIfNotFirstLevel);
  },
  beforeDestroy() {
    document.removeEventListener('keydown', this.closeByEscape);
    this.$root.$off('close-drawer', this.closeIfNotFirstLevel);
  },
  methods: {
    open() {
      this.drawer = true;
    },
    onClickOutside(event) {
      if (this.permanentBehavior && event?.target?.closest('.v-overlay')) {
        this.permanentBehavior = false;
        this.$nextTick().then(() => {
          this.closeIfNotFirstLevel();
        });
      }
    },
    closeByEscape(event) {
      if (this.drawer && event?.key === 'Escape') {
        this.permanentBehavior = false;
        this.closeIfNotFirstLevel();
      }
    },
    closeIfNotFirstLevel(event) {
      if (this.level > 1 || this.openedDrawersCount < 2) {
        window.setTimeout(() => {
          if (!this.permanentDrawer) {
            this.close(event);
          }
        }, 50);
      } else {
        if (!this.permanentDrawer) {
          this.close(event);
        }
      }
    },
    close(event) {
      if (this.drawer) {
        if (event) {
          event.preventDefault();
          event.stopPropagation();
        }
        this.drawer = false;
      }
    },
    startLoading() {
      this.loading = true;
    },
    endLoading() {
      this.loading = false;
    },
    toogleExpand() {
      this.expand = !this.expand;
    },
  },
};
</script>