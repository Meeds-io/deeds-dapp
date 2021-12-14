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
    interval: null,
    days: 0,
    hours: 0,
    minutes: 0,
    seconds: 0,
  }),
  watch: {
    endTime: {
      immediate: true,
      handler() {
        if (!this.interval) {
          this.setCountDown();
        }
      }
    },
  },
  methods: {
    setCountDown() {
      if (!this.endTime) {
        return;
      }
      this.updateCountDown();
      this.interval = setInterval(this.updateCountDown, 1000);
    },
    updateCountDown() {
      const distance = this.endTime - Date.now();
      if (distance <= 0) {
        this.$emit('end');
        window.setTimeout(() => {
          window.clearInterval(this.interval);
        }, 500);
      } else {
        // Time calculations for days, hours, minutes and seconds
        this.days = Math.floor(distance / (1000 * 60 * 60 * 24));
        this.hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        this.minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
        this.seconds = Math.floor((distance % (1000 * 60)) / 1000);
      }
    },
  },
};
</script>