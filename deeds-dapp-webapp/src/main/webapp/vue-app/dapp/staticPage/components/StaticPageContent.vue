<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 
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
  <div>
    <deeds-dynamic-html v-if="content" :content="content" />
  </div>
</template>
<script>
export default {
  data: () => ({
    content: null,
  }),
  computed: Vuex.mapState({
    buildNumber: state => state.buildNumber,
  }),
  created() {
    const pathParts = window.location.pathname.split('/');
    const fileName = pathParts.length > 2 && pathParts[2]?.length && pathParts[2] || 'index';
    fetch(`/${window.parentAppLocation}/static/html/${fileName}.html?version=${this.buildNumber}`)
      .then(resp => resp && resp.ok && resp.text())
      .then(content => this.content = content?.replace(/images\//g, `/${window.location.pathname.split('/')[1]}/static/images/`) || '');
  },
};
</script>
