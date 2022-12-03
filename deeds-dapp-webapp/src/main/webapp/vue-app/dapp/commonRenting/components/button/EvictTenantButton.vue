<template>
  <v-tooltip
    z-index="4"
    max-width="300px"
    bottom>
    <template #activator="{ on, attrs }">
      <v-card
        class="mx-auto mt-2 mt-md-0"
        color="transparent"
        :width="buttonsWidth"
        :min-width="minButtonsWidth"
        flat
        v-bind="attrs"
        v-on="on">
        <v-btn
          :disabled="disabled"
          :min-width="minButtonsWidth"
          :width="buttonsWidth"
          :title="$t('endRental')"
          color="secondary"
          outlined
          depressed
          dark
          @click="$root.$emit('deed-evict-tenant-open', lease)">
          <span class="text-truncate position-absolute full-width">
            {{ $t('evictTenant') }}
          </span>
        </v-btn>
      </v-card>
    </template>
    <span>{{ disabled && $t('evictTenantPlaceHolderDisabledTooltip') || $t('evictTenantPlaceHolderEnabledTooltip') }}</span>
  </v-tooltip>
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