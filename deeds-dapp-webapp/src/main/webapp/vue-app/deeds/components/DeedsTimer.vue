<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 
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
  <span class="ms-2 red--text">
    {{ $t('timer', {0: days, 1: hours, 2: minutes, 3: seconds}) }}
  </span>
</template>
<script>
export default {
  props: {
    endTime: {
      type: Number,
      default: 0,
    },
  },
  data: () => ({
    remainingSeconds: 0,
    days: 0,
    hours: 0,
    minutes: 0,
    seconds: 0,
  }),
  computed: Vuex.mapState({
    now: state => state.now,
  }),
  watch: {
    now() {
      this.updateCountDown();
    },
  },
  created() {
    this.$store.commit('startTimer');
    this.updateCountDown();
  },
  destroyed() {
    this.$store.commit('endTimer');
  },
  methods: {
    updateCountDown() {
      this.remainingSeconds = parseInt((this.endTime - this.now) / 1000);
      if (this.remainingSeconds < 0) {
        this.$emit('end');
      } else {
        // Time calculations for days, hours, minutes and seconds
        this.days = Math.floor(this.remainingSeconds / (60 * 60 * 24));
        this.hours = Math.floor((this.remainingSeconds % (60 * 60 * 24)) / (60 * 60));
        this.minutes = Math.floor((this.remainingSeconds % (60 * 60)) / 60);
        this.seconds = Math.floor((this.remainingSeconds % 60));
      }
    },
  },
};
</script>