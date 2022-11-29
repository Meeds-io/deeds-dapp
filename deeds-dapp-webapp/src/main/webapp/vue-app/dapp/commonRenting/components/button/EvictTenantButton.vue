<template>
  <v-tooltip z-index="4" bottom>
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
          :title="$t('endRental')"
          color="secondary"
          outlined
          depressed
          dark
          @click="$root.$emit('deed-evict-tenant-open', lease)">
          <span class="text-truncate position-absolute full-width text-capitalize">
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