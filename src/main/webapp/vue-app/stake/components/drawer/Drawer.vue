<template>
  <v-navigation-drawer
    v-model="drawer"
    :width="width"
    temporary
    right
    fixed
    height="100%"
    max-height="100%"
    max-width="100vw"
    class="d-sm-flex drawerParent">
    <v-container
      fill-height
      class="pa-0">
      <v-layout column>
        <template v-if="$slots.title">
          <v-flex class="mx-0 drawerHeader flex-grow-0">
            <v-list-item class="pe-0">
              <v-list-item-content class="drawerTitle align-start text-header-title text-truncate">
                <slot name="title"></slot>
              </v-list-item-content>
              <v-list-item-action class="drawerIcons align-end d-flex flex-row">
                <slot name="titleIcons"></slot>
                <v-btn
                  v-if="allowExpand && !isMobile"
                  icon
                  @click="toogleExpand">
                  <v-icon v-text="expandIcon" size="18" />
                </v-btn>
                <v-btn
                  :title="$t('label.close')"
                  icon>
                  <v-icon @click="close()">mdi-close</v-icon>
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
        <v-flex class="drawerContent flex-grow-1 overflow-auto border-box-sizing">
          <slot name="content"></slot>
        </v-flex>
        <template v-if="$slots.footer">
          <v-divider class="my-0" />
          <v-flex v-if="$slots.footer" class="drawerFooter border-box-sizing flex-grow-0 px-4 py-3">
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
  },
  data: () => ({
    drawer: false,
    loading: false,
    expand: true,
  }),
  computed: {
    width() {
      return this.expand && '100%' || this.drawerWidth;
    },
    isMobile() {
      return this.$vuetify && this.$vuetify.breakpoint && this.$vuetify.breakpoint.name === 'xs';
    },
    expandIcon() {
      return this.expand && 'mdi-arrow-collapse' || 'mdi-arrow-expand';
    },
  },
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
        this.$emit('opened');
        if (this.disablePullToRefresh) {
          document.body.style.overscrollBehaviorY = 'contain';
        }
      } else {
        this.$emit('closed');
        if (this.disablePullToRefresh) {
          document.body.style.overscrollBehaviorY = '';
        }
      }
      this.$emit('input', this.drawer);
      this.expand = false;
    },
  },
  created() {
    this.$root.$on('close-drawer', this.close);
  },
  methods: {
    open() {
      this.drawer = true;
    },
    close() {
      this.drawer = false;
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