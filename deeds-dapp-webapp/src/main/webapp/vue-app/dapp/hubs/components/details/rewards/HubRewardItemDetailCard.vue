<template>
  <v-card
    class="rounded-lg d-flex flex-column justify-space-between px-3 py-1"
    width="150"
    height="80"
    outlined
    hover>
    <div class="my-auto d-flex align-center flex-grow-1 text-light-color">
      {{ $t(label) }}
    </div>
    <div class="my-auto d-flex align-center flex-grow-1">
      <div class="font-weight-bold">
        {{ valueFormat }}
      </div>
      <v-spacer />
      <v-tooltip v-if="percentage" bottom>
        <template #activator="{ on, attrs }">
          <v-chip
            v-on="on"
            v-bind="attrs"
            :color="percentageColor"
            class="px-1"
            small
            dark>
            <span class="font-weight-bold caption pt-2px">
              {{ percentageFormat }}
            </span>
          </v-chip>
        </template>
        <span>{{ $t('uem.comparingToPreviousPeriod') }}</span>
      </v-tooltip>
    </div>
  </v-card>
</template>
<script>
export default {
  props: {
    label: {
      type: String,
      default: null,
    },
    value: {
      type: String,
      default: null,
    },
    previousValue: {
      type: String,
      default: null,
    },
  },
  computed: Vuex.mapState({
    language: state => state.language,
    parentLocation: state => state.parentLocation,
    valueFormat() {
      return this.$utils.numberFormat(this.value, this.language);
    },
    percentage() {
      return this.previousValue && (this.value / this.previousValue - 1) || 0;
    },
    percentageColor() {
      if (this.percentage > 0) {
        return 'success';
      } else if (this.percentage < 0) {
        return 'error';
      }
      return null;
    },
    percentageFormat() {
      return this.$utils.percentage(this.percentage, this.language, true, 0);
    },
  }),
};
</script>