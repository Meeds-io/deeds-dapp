<template>
  <div class="d-flex mx-4 mb-4">
    <div class="d-flex flex-column flex-grow-1 flex-shrink-0">
      <div class="d-flex align-center fill-height flex-grow-1 mb-4">
        <v-icon size="45" class="secondary--text me-3">fa-rocket</v-icon>
        <div class="d-flex flex-wrap justify-center text-start flex-grow-1">
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
          <div class="text-light-color caption font-weight-bold ma-auto">
            <v-tooltip
              v-if="reward"
              bottom>
              <template #activator="{ on, attrs }">
                <v-chip
                  v-on="on"
                  v-bind="attrs"
                  :color="engagementRateColor"
                  class="px-1"
                  outlined
                  small>
                  <span class="font-weight-bold text-subtitle-2 pt-2px">
                    {{ engagementRatePercentage }}
                  </span>
                </v-chip>
              </template>
              <span>{{ $t('uem.engagementRatePercentage') }}</span>
            </v-tooltip>
          </div>
        </div>
      </div>
      <div class="d-flex align-center mb-2">
        <v-card
          width="28"
          min-width="28"
          class="me-2 d-flex align-center justify-center"
          flat>
          <v-icon size="22" class="primary--text">fa-user-check</v-icon>
        </v-card>
        <div class="d-flex">
          {{ recipientsCount }} <span class="hidden-xs-only ms-1">{{ $t('wom.recipientsCount') }}</span>
        </div>
      </div>
      <div class="d-flex align-center">
        <v-card
          width="28"
          min-width="28"
          class="me-2 d-flex align-center justify-center"
          flat>
          <v-icon size="22" class="primary--text">fa-trophy</v-icon>
        </v-card>
        <div class="d-flex">
          {{ achievementsCount }} <span class="hidden-xs-only ms-1">{{ $t('wom.achievementsCount') }}</span>
        </div>
      </div>
    </div>
    <div class="d-flex flex-column flex-grow-1 flex-shrink-0">
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
      <div class="d-flex align-center mb-2">
        <v-card
          width="28"
          min-width="28"
          class="me-2 d-flex align-center justify-center"
          flat>
          <v-icon size="22" class="primary--text">fa-users</v-icon>
        </v-card>
        <div class="d-flex">
          {{ usersCount }} <span class="hidden-xs-only ms-1">{{ $t('wom.usersCount') }}</span>
        </div>
      </div>
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
            <div class="d-flex">
              {{ hubTopRewardedAmountFormatted }} {{ $t('meedsSymbol') }}<span v-if="hubRewardsPeriodType" class="hidden-xs-only ms-1"> / {{ hubRewardsPeriod }}</span>
            </div>
          </div>
        </template>
        <span>{{ $t('wom.maxRewardAmountEarned') }}</span>
      </v-tooltip>
    </div>
  </div>
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
      return this.hubRewardsPeriodType && this.$t(`wom.${this.hubRewardsPeriodType}`) || '';
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