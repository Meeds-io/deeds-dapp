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
            class="d-none d-sm-block mt-n1 me-2">
            fa-calendar
          </v-icon>
          <div class="d-flex flex-column flex-sm-row flex-grow-1 flex-shrink-1 text-subtitle-1 font-weight-bold ms-0 ms-sm-2 justify-start align-center">
            <deeds-date-format
              :value="fromDate"
              :format="dateFormat"
              class="text-break" />
            <span class="mx-2 text-no-wrap">{{ $t('wom.toDate') }}</span>
            <deeds-date-format
              :value="toDate"
              :format="dateFormat"
              class="text-break" />
          </div>
          <v-spacer />
          <deeds-hub-deed-chip
            :deed="report"
            :height="24"
            color="grey lighten-2"
            font-color="text-color"
            class="ms-2"
            small />
          <deeds-hub-details-reward-item-menu
            :report="report"
            :loading="loading"
            :expanded="expand"
            @expand="$emit('expand')"
            @collapse="$emit('collapse')" />
        </v-card-title>
        <v-list
          v-if="!expand"
          class="pa-0">
          <v-row
            class="mx-4 mb-4 border-box-sizing"
            dense>
            <v-col
              cols="12"
              sm="6">
              <div class="d-flex align-center fill-height flex-grow-1 mb-4">
                <v-icon size="45" class="secondary--text me-3">fa-rocket</v-icon>
                <div class="d-flex justify-center text-start flex-grow-1">
                  <v-tooltip bottom>
                    <template #activator="{ on, attrs }">
                      <div
                        v-on="on"
                        v-bind="attrs"
                        :class="engagementScoreClass"
                        class="title font-weight-bold my-auto">
                        {{ engagementScoreFormatted }}
                      </div>
                    </template>
                    <span>{{ $t('hubs.engagementScore') }}</span>
                  </v-tooltip>
                  <v-spacer />
                  <div class="text-light-color caption font-weight-bold my-auto me-1">
                    <v-tooltip
                      v-if="reward"
                      bottom>
                      <template #activator="{ on, attrs }">
                        <v-chip
                          v-on="on"
                          v-bind="attrs"
                          :color="engagementRateColor"
                          class="px-1 mt-n1"
                          outlined
                          small>
                          <span class="font-weight-bold text-subtitle-2 pt-1">
                            {{ engagementRatePercentage }}
                          </span>
                        </v-chip>
                      </template>
                      <span>{{ $t('uem.engagementRatePercentage') }}</span>
                    </v-tooltip>
                  </div>
                </div>
              </div>
            </v-col>
            <v-col
              cols="12"
              sm="6">
              <div class="d-flex align-center me-auto fill-height mb-4">
                <v-icon size="45" class="secondary--text me-3">fa-coins</v-icon>
                <v-tooltip bottom>
                  <template #activator="{ on, attrs }">
                    <div
                      v-on="on"
                      v-bind="attrs"
                      class="title font-weight-bold my-auto text-light-color">
                      {{ uemRewardAmountFormatted }} {{ $t('meedsSymbol') }}
                    </div>
                  </template>
                  <span>{{ $t('uem.mintiumRewards') }}</span>
                </v-tooltip>
              </div>
            </v-col>
            <v-col
              cols="12"
              sm="6">
              <div class="d-flex align-center mb-2">
                <v-card
                  width="28"
                  min-width="28"
                  class="me-2 d-flex align-center justify-center"
                  flat>
                  <v-icon size="22" class="primary--text">fa-user-check</v-icon>
                </v-card>
                <div class="text-truncate">
                  {{ recipientsCount }} {{ $t('wom.recipientsCount') }}
                </div>
              </div>
            </v-col>
            <v-col
              cols="12"
              sm="6">
              <div class="d-flex align-center mb-2">
                <v-card
                  width="28"
                  min-width="28"
                  class="me-2 d-flex align-center justify-center"
                  flat>
                  <v-icon size="22" class="primary--text">fa-users</v-icon>
                </v-card>
                <div class="text-truncate">
                  {{ usersCount }} {{ $t('wom.usersCount') }}
                </div>
              </div>
            </v-col>
            <v-col
              cols="12"
              sm="6">
              <div class="d-flex align-center">
                <v-card
                  width="28"
                  min-width="28"
                  class="me-2 d-flex align-center justify-center"
                  flat>
                  <v-icon size="22" class="primary--text">fa-trophy</v-icon>
                </v-card>
                <div class="text-truncate">
                  {{ achievementsCount }} {{ $t('wom.achievementsCount') }}
                </div>
              </div>
            </v-col>
            <v-col
              cols="12"
              sm="6">
              <v-tooltip bottom>
                <template #activator="{ on, attrs }">
                  <div
                    v-on="on"
                    v-bind="attrs"
                    class="d-flex align-center">
                    <v-card
                      width="28"
                      min-width="28"
                      class="me-2 d-flex align-center justify-center"
                      flat>
                      <img
                        :src="`${parentLocation}/static/images/meed_circle-green.png`"
                        :alt="$t('page.token')"
                        width="25"
                        height="25">
                    </v-card>
                    <div class="text-truncate">
                      {{ hubTopRewardedAmountFormatted }} {{ $t('meedsSymbol') }} / {{ hubRewardsPeriod }}
                    </div>
                  </div>
                </template>
                <span>{{ $t('wom.maxRewardAmountEarned') }}</span>
              </v-tooltip>
            </v-col>
          </v-row>
        </v-list>
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
  }),
  computed: Vuex.mapState({
    language: state => state.language,
    parentLocation: state => state.parentLocation,
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
    fromDate() {
      return this.report?.fromDate;
    },
    toDate() {
      return this.report?.toDate;
    },
    usersCount() {
      return this.formatNumber(this.report?.usersCount || 0);
    },
    recipientsCount() {
      return this.formatNumber(this.report?.recipientsCount || 0);
    },
    achievementsCount() {
      return this.formatNumber(this.report?.achievementsCount || 0);
    },
    blockchainNetworkId() {
      return this.report?.rewardTokenNetworkId || 0;
    },
    tokenAddress() {
      return this.report?.rewardTokenAddress || 0;
    },
    uemRewardAmount() {
      return this.report?.uemRewardAmount || 0;
    },
    uemRewardAmountFormatted() {
      return this.formatNumber(this.uemRewardAmount);
    },
    engagementScore() {
      return this.report?.engagementScore || 0;
    },
    engagementScoreFormatted() {
      return this.engagementScore && new Intl.NumberFormat(this.language, {
        style: 'decimal',
        minimumFractionDigits: 0,
        maximumFractionDigits: 1,
      }).format(this.engagementScore);
    },
    engagementScoreClass() {
      if (!this.engagementScore) {
        return 'text-light-color';
      } else if (this.engagementScore > 11) {
        return 'success--text';
      } else if (this.engagementScore < 9) {
        return 'error--text';
      } else {
        return 'text-light-color';
      }
    },
    rewardId() {
      return this.report?.rewardId;
    },
    fixedGlobalIndex() {
      return this.reward?.fixedGlobalIndex;
    },
    hubsCount() {
      return this.reward?.reportIds?.length;
    },
    fixedRewardIndex() {
      return this.report?.fixedRewardIndex;
    },
    averageFixedRewardIndex() {
      return this.hubsCount && this.fixedGlobalIndex / this.hubsCount || this.fixedRewardIndex;
    },
    engagementRate() {
      return this.fixedRewardIndex / this.averageFixedRewardIndex;
    },
    engagementRateColor() {
      return this.engagementRate >= 1 && 'success' || 'error';
    },
    engagementRatePercentage() {
      return this.$utils.percentage(this.engagementRate - 1, true, '0');
    },
    hubRewardsPeriodType() {
      return this.report?.periodType?.toLowerCase();
    },
    hubRewardsPeriod() {
      return this.$t(`wom.${this.hubRewardsPeriodType}`);
    },
    hubTopRewardedAmount() {
      return this.report?.hubTopRewardedAmount || 0;
    },
    hubTopRewardedAmountFormatted() {
      return this.formatNumber(this.hubTopRewardedAmount);
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
      const formatted = new Intl.NumberFormat(this.language, {
        style: 'decimal',
        minimumFractionDigits: 0,
        maximumFractionDigits: useKilo && 1 || (value < 1 && 2 || 0),
      }).format(value);
      return useKilo ? this.$t('kilo', {0: formatted}) : formatted;
    },
  },
};
</script>