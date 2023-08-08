<template>
  <component
    :is="expand && 'v-dialog' || 'v-card'"
    v-on="componentEvents"
    v-bind="componentProps">
    <v-scale-transition>
      <v-card
        :loading="loading"
        class="overflow-y-auto fill-height"
        flat>
        <v-card-title class="title d-flex flex-sm-nowrap">
          <v-icon
            color="secondary"
            class="d-none d-sm-block mt-n1 me-2">
            fa-calendar
          </v-icon>
          <div class="d-flex flex-column flex-sm-row flex-grow-1 flex-shrink-1 ms-0 ms-sm-2 justify-start align-center">
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
          <deeds-hub-details-reward-item-menu
            :report="report"
            :loading="loading"
            :expanded="expand"
            @expand="$emit('expand')"
            @collapse="$emit('collapse')" />
        </v-card-title>
        <v-card-text
          :class="expand && 'mt-4 pb-6' || 'mt-n2 pb-2'"
          class="d-flex flex-wrap">
          <deeds-hub-details-reward-status
            :report="report"
            class="me-2 mt-2" />
          <deeds-hub-blockchain-chip
            :network-id="blockchainNetworkId"
            class="mt-2 me-2" />
          <deeds-hub-address
            :title="$t('wom.tokenAddress')"
            :address="tokenAddress"
            :network-id="blockchainNetworkId"
            class="mt-2 me-2"
            small
            token />
        </v-card-text>
        <v-list :class="expand && 'py-0 px-2' || 'pa-0'">
          <v-row
            class="mx-0 border-box-sizing"
            no-gutters
            dense>
            <v-col
              cols="12"
              sm="6">
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title :title="$t('wom.participantsCount')">{{ $t('wom.participantsCount') }}</v-list-item-title>
                  <v-list-item-subtitle>{{ participantsCount }} {{ $t('wom.users') }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
            </v-col>
            <v-col
              cols="12"
              sm="6">
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title :title="$t('wom.recipientsCount')">{{ $t('wom.recipientsCount') }}</v-list-item-title>
                  <v-list-item-subtitle>{{ recipientsCount }} {{ $t('wom.users') }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
            </v-col>
            <v-col
              cols="12"
              sm="6">
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title :title="$t('wom.achievementsCount')">{{ $t('wom.achievementsCount') }}</v-list-item-title>
                  <v-list-item-subtitle>{{ achievementsCount }} {{ $t('wom.achievements') }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
            </v-col>
            <v-col
              cols="12"
              sm="6">
              <v-list-item>
                <v-list-item-content>
                  <v-list-item-title :title="$t('wom.usersRewardAmount')">{{ $t('wom.usersRewardAmount') }}</v-list-item-title>
                  <v-list-item-subtitle>{{ rewardAmount }} Ɱ</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
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
    dateFormat: {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    },
  }),
  computed: Vuex.mapState({
    language: state => state.language,
    componentProps() {
      return this.expand && {
        value: true,
        eager: true,
        fullscreen: true,
      } || {
        elevation: 0,
        hover: true,
        class: 'border-color',
      };
    },
    componentEvents() {
      return this.expand && {
        input: opened => {
          if (!opened) {
            this.$emit('collapse');
          }
        }
      } || {};
    },
    hubRewardReport() {
      return this.report?.hubRewardReport;
    },
    fromDate() {
      return this.hubRewardReport?.fromDate;
    },
    toDate() {
      return this.hubRewardReport?.toDate;
    },
    participantsCount() {
      return new Intl.NumberFormat(this.language, {
        style: 'decimal',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
      }).format(this.hubRewardReport?.participantsCount || 0);
    },
    recipientsCount() {
      return new Intl.NumberFormat(this.language, {
        style: 'decimal',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
      }).format(this.hubRewardReport?.recipientsCount || 0);
    },
    achievementsCount() {
      return new Intl.NumberFormat(this.language, {
        style: 'decimal',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
      }).format(this.hubRewardReport?.achievementsCount || 0);
    },
    rewardAmount() {
      return new Intl.NumberFormat(this.language, {
        style: 'decimal',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
      }).format(this.hubRewardReport?.rewardAmount || 0);
    },
    blockchainNetworkId() {
      return this.hubRewardReport?.rewardTokenNetworkId || 0;
    },
    tokenAddress() {
      return this.hubRewardReport?.rewardTokenAddress || 0;
    },
  }),
};
</script>