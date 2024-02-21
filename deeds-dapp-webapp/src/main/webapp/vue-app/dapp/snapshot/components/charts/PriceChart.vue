<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
<template>
  <div class="d-flex flex-column priceChartParent">
    <div class="priceChartPeriodSelector d-flex align-center ps-2 pe-12">
      <div class="d-none d-sm-flex align-center">
        <v-btn
          name="displayThisWeekPricesButton"
          link
          text
          color="secondary"
          x-small
          @click="period = '1w'">
          {{ $t('week') }}
        </v-btn>
        <v-btn
          name="displayThisMonthPricesButton"
          link
          text
          color="secondary"
          x-small
          @click="period = '1month'">
          {{ $t('month') }}
        </v-btn>
        <v-btn
          name="displayThisYearPricesButton"
          link
          text
          x-small
          color="secondary"
          @click="period = 'ytd'">
          {{ $t('year') }}
        </v-btn>
        <v-btn
          name="displayAllPricesButton"
          link
          text
          x-small
          color="secondary"
          @click="period = 'all'">
          {{ $t('all') }}
        </v-btn>
      </div>
    </div>
    <div id="priceChart" ref="priceChart"></div>
  </div>
</template>

<script>
export default {
  data: () => ({
    periods: ['1w' , '1month', 'ytd', 'all'],
    period: '1w',
    meedPriceHistory: [],
    overallStartTimetamp: 1636156800000,
    toTimetamp: Date.now(),
    chart: null,
    htmlMounted: false,
  }),
  computed: Vuex.mapState({
    language: state => state.language,
    address: state => state.address,
    selectedFiatCurrency: state => state.selectedFiatCurrency,
    echartsLoaded: state => state.echartsLoaded,
    selectedFiatCurrencyLabel() {
      return this.$t(`fiat.currency.${this.selectedFiatCurrency}`);
    },
    fromDate() {
      if (this.period === '1w') {
        return new Date(this.toTimetamp - 604800000).toISOString().substring(0, 10);
      } else if (this.period === '1month') {
        return new Date(this.toTimetamp - 2592000000).toISOString().substring(0, 10);
      } else if (this.period === 'ytd') {
        return new Date(this.toTimetamp - 31536000000).toISOString().substring(0, 10);
      } else if (this.period === 'all') {
        return new Date(this.overallStartTimetamp).toISOString().substring(0, 10);
      }
    },
    chartData() {
      return this.meedPriceHistory.sort((item1, item2) => item2.date.localeCompare(item1.date))
        .map(item => [item.date, item.currencyPrice]);
    },
    chartTitle() {
      if (this.chartData && this.chartData.length) {
        const meedPrice = this.chartData[0][1];
        if (meedPrice) {
          const meedPriceLabel = this.currencyFormat(meedPrice);
          return `${this.$t('meedPrice')}: ${meedPriceLabel}`;
        }
      }
      return this.$t('meedPrice');
    },
    chartLeftMargin() {
      if (this.chartData && this.chartData.length) {
        const meedsMaxPrice = Math.max(...this.chartData.map(values => values[1]));
        if (meedsMaxPrice) {
          const meedPriceLabel = this.currencyFormat(meedsMaxPrice);
          return Math.max(meedPriceLabel.length * 6, 62);
        }
      }
      return 62;
    },
    chartOptions() {
      return {
        tooltip: {
          trigger: 'axis',
          show: true,
          formatter: this.labelFormatter,
        },
        grid: {
          left: this.chartLeftMargin,
        },
        xAxis: {
          type: 'time',
          splitLine: {
            show: false
          },
          axisLabel: {
            interval: 0,
            rotate: 30,
            formatter: this.dateFormatI18N,
          },
          axisPointer: {
            label: {
              formatter: this.dateFormatI18N,
            }
          },
        },
        yAxis: {
          type: 'value',
          splitLine: {
            show: false
          },
          axisLabel: {
            formatter: this.currencyFormat,
          },
          axisPointer: {
            label: {
              formatter: this.currencyFormat,
            }
          },
        },
        series: [{
          type: 'line',
          data: this.chartData,
        }]
      };
    },
  }),
  watch: {
    fromDate(newVal, oldVal) {
      if (newVal && newVal !== oldVal) {
        this.refreshData();
      }
    },
    language() {
      if (this.chart && this.chartOptions) {
        this.chart.setOption(this.chartOptions);
      }
    },
    selectedFiatCurrency(newVal, oldVal) {
      if (newVal && newVal !== oldVal) {
        this.refreshData();
      }
    },
    meedPriceHistory() {
      if (!this.chart) {
        this.initChart();
      }
      if (this.chart && this.chartOptions) {
        this.chart.setOption(this.chartOptions);
      }
      this.setMeedPrice();
    },
    htmlMounted: {
      immediate: true,
      handler: function() {
        this.initChart();
      },
    },
    echartsLoaded: {
      immediate: true,
      handler: function() {
        this.initChart();
      },
    },
  },
  mounted() {
    this.htmlMounted = true;
    this.refreshData();
  },
  methods: {
    initChart() {
      if (this.htmlMounted && this.echartsLoaded) {
        this.chart = echarts.init(this.$refs.priceChart);
        if (this.chartOptions) {
          this.chart.setOption(this.chartOptions);
        }
      }
    },
    refreshData() {
      return this.$exchange.getMeedsExchange(this.fromDate, this.selectedFiatCurrency)
        .then(result => this.meedPriceHistory = result);
    },
    currencyFormat(price) {
      const value = price && price.value || price;
      if (this.selectedFiatCurrency === 'eth') {
        return `${this.$ethUtils.toFixed(value, 8)} ${this.selectedFiatCurrencyLabel}`;
      } else {
        return this.$ethUtils.toCurrencyDisplay(this.$ethUtils.toFixed(value, 2), this.selectedFiatCurrency, this.language);
      }
    },
    dateFormat(timestamp) {
      const value = parseInt(timestamp && timestamp.value || timestamp);
      return new Date(value).toISOString().substring(0, 10);
    },
    dateFormatI18N(timestamp) {
      if (timestamp.value) {
        timestamp = timestamp.value;
      } else if ((typeof timestamp) === 'string' && timestamp.indexOf('-') < 0) {
        timestamp = parseInt(timestamp);
      }
      return this.$ethUtils.formatDate(new Date(timestamp), this.language);
    },
    labelFormatter(item) {
      const date = item[0].value[0];
      const price = item[0].value[1];
      return `${this.dateFormatI18N(date)}: <strong>${this.currencyFormat(price)}</strong>`;
    },
    setMeedPrice() {
      if (this.meedPriceHistory?.length) {
        const today = new Date().toISOString().substring(0, 10);
        const todayItem = this.meedPriceHistory.find(item => item.date === today);
        if (todayItem?.currencyPrice) {
          this.$store.commit('setMeedPrice', new BigNumber(todayItem.currencyPrice));
        }
      }
    },
  },
};
</script>
