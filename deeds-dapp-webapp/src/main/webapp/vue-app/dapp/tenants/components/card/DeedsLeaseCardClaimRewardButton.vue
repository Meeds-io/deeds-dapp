<template>
  <v-btn
    :disabled="disabled"
    :min-width="minButtonsWidth"
    :width="buttonsWidth"
    :max-width="maxButtonsWidth"
    :title="$t('claimRewards')"
    class="mx-auto mt-2 mt-md-0"
    color="primary"
    outlined
    depressed
    dark>
    <span class="text-truncate position-absolute full-width text-capitalize">
      {{ $t('claimRewards') }}
    </span>
  </v-btn>
</template>
<script>
export default {
  props: {
    disabled: {
      type: Object,
      default: null,
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