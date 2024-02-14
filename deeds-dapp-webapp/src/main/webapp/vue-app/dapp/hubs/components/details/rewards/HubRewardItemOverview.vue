<template>
  <v-list class="pa-0">
    <v-row
      class="mx-4 mb-4 border-box-sizing"
      dense>
      <v-col cols="6">
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
      <v-col cols="6">
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
      <v-col cols="6">
        <div class="d-flex align-center mb-2">
          <v-card
            width="28"
            min-width="28"
            class="me-2 d-flex align-center justify-center"
            flat>
            <v-icon size="22" class="primary--text">fa-user-check</v-icon>
          </v-card>
          <div class="text-truncate d-flex">
            {{ recipientsCount }} <span class="hidden-xs-only">{{ $t('wom.recipientsCount') }}</span>
          </div>
        </div>
      </v-col>
      <v-col cols="6">
        <div class="d-flex align-center mb-2">
          <v-card
            width="28"
            min-width="28"
            class="me-2 d-flex align-center justify-center"
            flat>
            <v-icon size="22" class="primary--text">fa-users</v-icon>
          </v-card>
          <div class="text-truncate d-flex">
            {{ usersCount }} <span class="hidden-xs-only">{{ $t('wom.usersCount') }}</span>
          </div>
        </div>
      </v-col>
      <v-col cols="6">
        <div class="d-flex align-center">
          <v-card
            width="28"
            min-width="28"
            class="me-2 d-flex align-center justify-center"
            flat>
            <v-icon size="22" class="primary--text">fa-trophy</v-icon>
          </v-card>
          <div class="text-truncate d-flex">
            {{ achievementsCount }} <span class="hidden-xs-only">{{ $t('wom.achievementsCount') }}</span>
          </div>
        </div>
      </v-col>
      <v-col cols="6">
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
              <div class="text-truncate d-flex">
                {{ hubTopRewardedAmountFormatted }} {{ $t('meedsSymbol') }}<span class="hidden-xs-only"> / {{ hubRewardsPeriod }}</span>
              </div>
            </div>
          </template>
          <span>{{ $t('wom.maxRewardAmountEarned') }}</span>
        </v-tooltip>
      </v-col>
    </v-row>
  </v-list>
</template>
<script>
export default {
  props: {
    report: {
      type: Object,
      default: null,
    },
    reward: {
      type: Object,
      default: null,
    },
  },
  computed: Vuex.mapState({
    language: state => state.language,
    parentLocation: state => state.parentLocation,
    usersCount() {
      return this.formatNumber(this.report?.usersCount || 0);
    },
    recipientsCount() {
      return this.formatNumber(this.report?.recipientsCount || 0);
    },
    achievementsCount() {
      return this.formatNumber(this.report?.achievementsCount || 0);
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
      return this.$utils.numberFormatWithDigits(this.engagementScore, this.language, 0, 1);
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
      return this.$utils.percentage(this.engagementRate - 1, this.language, true, '0');
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