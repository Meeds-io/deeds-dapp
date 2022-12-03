<template>
  <v-btn
    :disabled="disabled"
    :min-width="minButtonsWidth"
    :width="buttonsWidth"
    :title="$t('endRental')"
    class="mx-auto mt-2 mt-md-0"
    color="secondary"
    outlined
    depressed
    dark
    @click="$root.$emit('deed-end-renting-open', lease)">
    <span class="text-truncate position-absolute full-width">
      {{ $t('endRental') }}
    </span>
  </v-btn>
</template>
<script>
export default {
  props: {
    lease: {
      type: Object,
      default: null,
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    minButtonsWidth: {
      type: String,
      default: () => '150px',
    },
    maxButtonsWidth: {
      type: String,
      default: () => '250px',
    },
    buttonsWidth: {
      type: String,
      default: () => '100%',
    },
  },
};
</script>