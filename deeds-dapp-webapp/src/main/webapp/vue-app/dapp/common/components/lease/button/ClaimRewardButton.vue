<template>
  <v-tooltip bottom>
    <template #activator="{ on, attrs }">
      <v-card
        class="mx-auto mt-2 mt-md-0"
        color="transparent"
        :width="buttonsWidth"
        :min-width="minButtonsWidth"
        :max-width="maxButtonsWidth"
        flat
        v-bind="attrs"
        v-on="on">
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
      </v-card>
    </template>
    <span>{{ $t('comingSoonStayTuned') }}</span>
  </v-tooltip>
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