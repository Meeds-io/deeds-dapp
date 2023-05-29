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
<script>
import routes from './../routes';

export default {
  data: () => ({
    regex: /(\/)([a-zA-Z]+)(\/?)([a-zA-Z]*)/,
    currentRoute: '/',
    show: true,
  }),
  computed: Vuex.mapState({
    address: state => state.address,
    provider: state => state.provider,
    viewComponent() {
      if (!this.currentRoute) {
        return {template: ''};
      } else {
        return routes[this.currentRoute] || routes['/'];
      }
    },
  }),
  watch: {
    address(newVal, oldVal) {
      if (newVal?.toUpperCase() !== oldVal?.toUpperCase()) {
        this.refreshPage();
      }
    },
    provider(newVal, oldVal) {
      if (newVal && newVal !== oldVal) {
        this.refreshPage();
      }
    },
  },
  created() {
    this.$root.$on('location-change', this.refreshRoute);
    this.refreshRoute(window.location.pathname);
  },
  render(h) {
    return h(this.viewComponent);
  },
  methods: {
    refreshPage() {
      const route = this.currentRoute;
      this.currentRoute = 'notFound';
      this.$nextTick(() => this.currentRoute = route);
    },
    refreshRoute(path) {
      const parts = path && window.location.pathname.split('/').filter(pathPart => pathPart?.length) || [];
      let currentRoute = '/';
      if (parts.length > 0 ) {
        currentRoute = parts[parts.length - 1];
        if (!currentRoute || !currentRoute.length || currentRoute === 'fr') {
          currentRoute = '/';
        } else {
          currentRoute = `/${currentRoute}`;
        }
      }
      this.currentRoute = currentRoute || '/';
    },
  },
};
</script>