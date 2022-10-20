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
  }),
  computed: {
    viewComponent() {
      if (!this.currentRoute) {
        return {template: '<p>Not Found</p>'};
      } else {
        return routes[this.currentRoute] || {template: '<p>Not Found</p>'};
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
    refreshRoute(path) {
      const parts = path && window.location.pathname.split('/') || [];
      let currentRoute = '/';
      if (parts.length > 1) {
        currentRoute = parts[parts.length - 1];
        if (!currentRoute || !currentRoute.length) {
          currentRoute = '/';
        } else {
          currentRoute = `/${currentRoute}`;
        }
      }
      this.currentRoute = currentRoute;
    },
  },
};
</script>