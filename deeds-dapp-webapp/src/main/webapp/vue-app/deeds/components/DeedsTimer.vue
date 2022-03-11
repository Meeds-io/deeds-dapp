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