<template>
  <component
    :is="expand && 'v-dialog' || 'v-card'"
    v-on="componentEvents"
    v-bind="componentProps">
    <v-scale-transition>
      <v-card
        :loading="loading"
        :class="expand && 'overflow-y-auto' || 'overflow-hidden'"
        class="fill-height"
        flat>
        <v-card-title class="title d-flex flex-sm-nowrap">
          <v-icon
            color="secondary"
            class="mt-n1 me-2">
            fa-calendar
          </v-icon>
          <div class="d-flex flex-column flex-grow-1 flex-shrink-1 ms-0 ms-sm-2 align-start justify-center">
            <div class="d-flex flex-grow-1 flex-shrink-1 text-subtitle-1 font-weight-bold justify-start align-center">
              <v-tooltip bottom>
                <template #activator="{on, attrs}">
                  <div
                    v-bind="attrs"
                    v-on="on">
                    <deeds-date-format
                      :value="fromDate"
                      :format="dateFormat"
                      class="text-break" />
                  </div>
                </template>
                <deeds-date-format
                  :value="fromDate"
                  :format="fullDateFormat" />
              </v-tooltip>
              <span class="mx-2 text-no-wrap">{{ $t('wom.toDate') }}</span>
              <v-tooltip bottom>
                <template #activator="{on, attrs}">
                  <div
                    v-bind="attrs"
                    v-on="on">
                    <deeds-date-format
                      :value="toDate"
                      :format="dateFormat"
                      class="text-break" />
                  </div>
                </template>
                <deeds-date-format
                  :value="toDate"
                  :format="fullDateFormat" />
              </v-tooltip>
            </div>
            <div class="text-light-color caption d-flex mt-n2">
              {{ $t('uem.sentOn') }}
              <v-tooltip bottom>
                <template #activator="{on, attrs}">
                  <div
                    v-bind="attrs"
                    v-on="on"
                    class="ms-1">
                    <deeds-date-format
                      :value="report.sentDate"
                      :format="dateFormat"
                      class="text-break" />
                  </div>
                </template>
                <deeds-date-format
                  :value="report.sentDate"
                  :format="fullDateFormat"
                  class="text-break ms-1" />
              </v-tooltip>
              <v-tooltip bottom>
                <template #activator="{on, attrs}">
                  <div
                    v-bind="attrs"
                    v-on="on"
                    class="ms-1">
                    ({{ $t('wom.week') }} {{ uemRewardPeriodWeek }})
                  </div>
                </template>
                {{ $t('uem.mintiumRewardPeriod', {0: uemRewardPeriodWeek}) }}
              </v-tooltip>
            </div>
          </div>
          <v-spacer />
          <deeds-hub-deed-chip
            :deed="report"
            :height="expand && 36 || 24"
            :small="!expand"
            :font-color="dark && 'white--text' || 'text-color'"
            color="grey lighten-2"
            class="ms-2 hidden-xs-only" />
          <deeds-hub-details-reward-item-menu
            :report="report"
            :loading="loading"
            :expanded="expand"
            @expand="$emit('expand')"
            @collapse="$emit('collapse')" />
        </v-card-title>
        <v-card
          v-if="reward"
          class="mainPageLayout mx-auto"
          flat>
          <template v-if="expand">
            <deeds-hub-details-reward-item-details
              :report="report"
              :reports="reports"
              :reward="reward"
              class="mx-6 my-8" />
            <deeds-hub-report-formula
              :report="report"
              :reward="reward"
              class="mx-6 mb-8 hidden-xs-only" />
          </template>
          <deeds-hub-details-reward-item-overview
            v-else
            :report="report"
            :reward="reward" />
        </v-card>
      </v-card>
    </v-scale-transition>
  </component>
</template>
<script>
export default {
  props: {
    report: {
      type: Object,
      default: null,
    },
    reports: {
      type: Array,
      default: null,
    },
    expand: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    loading: false,
    reward: null,
    dateFormat: {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    },
    fullDateFormat: {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: 'numeric',
      minute: 'numeric',
    },
  }),
  computed: Vuex.mapState({
    language: state => state.language,
    parentLocation: state => state.parentLocation,
    dark: state => state.dark,
    componentProps() {
      return this.expand && {
        value: true,
        eager: true,
        fullscreen: true,
      } || {
        outlined: true,
        hover: true,
        class: 'rounded-lg',
      };
    },
    componentEvents() {
      return this.expand && {
        input: opened => {
          if (!opened) {
            this.$emit('collapse');
          }
        }
      } || {
        click: () => {
          this.$emit('expand');
        }
      };
    },
    uemRewardPeriodWeek() {
      const startOfYear = new Date(`${this.report?.sentDate.substring(0, 4)}-01-01T00:00:00Z`);
      const sentDate = new Date(this.report?.sentDate);
      return Math.ceil((sentDate.getTime()  - startOfYear.getTime()) / 7 / 24 / 3600 / 1000).toLocaleString('en-US', {
        minimumIntegerDigits: 2,
        useGrouping: false
      });
    },
    fromDate() {
      return new Date(this.report?.fromDate).getTime();
    },
    toDate() {
      return new Date(this.report?.toDate).getTime() - 1;
    },
    rewardId() {
      return this.report?.rewardId;
    },
  }),
  created() {
    this.$hubReportService.getReward(this.rewardId)
      .then(data => this.reward = data);
  },
  methods: {
    formatNumber(num) {
      const useKilo = num >= 1000;
      const value = useKilo ? num / 1000 : num;
      const formatted = this.$utils.numberFormatWithDigits(value, this.language, 0, useKilo && 1 || (value < 1 && 2 || 0));
      return useKilo ? this.$t('kilo', {0: formatted}) : formatted;
    },
  },
};
</script>