<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 
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
    <div class="priceChartPeriodSelector d-none d-sm-block">
      <v-btn
        link
        text
        color="primary"
        x-small
        @click="period = '1w'">
        {{ $t('week') }}
      </v-btn>
      <v-btn
        link
        text
        color="primary"
        x-small
        @click="period = '1month'">
        {{ $t('month') }}
      </v-btn>
      <v-btn
        link
        text
        x-small
        color="primary"
        @click="period = 'ytd'">
        {{ $t('year') }}
      </v-btn>
      <v-btn
        link
        text
        x-small
        color="primary"
        @click="period = 'all'">
        {{ $t('all') }}
      </v-btn>
    </div>
    <div id="priceChart" ref="priceChart"></div>
  </div>
</template>

<script>
export default {
  data: () => ({
    periods: ['1w' , '1month', 'ytd', 'all'],
    period: '1w',
    overallStartTimetamp: 1636156800000,
    toTimetamp: Date.now(),
    chart: null,
  }),
  computed: Vuex.mapState({
    language: state => state.language,
    address: state => state.address,
    selectedFiatCurrency: state => state.selectedFiatCurrency,
    pairHistoryData: state => state.pairHistoryData,
    currencyExchangeRate: state => state.currencyExchangeRate,
    needsExchangeRate() {
      return this.selectedFiatCurrency === 'eur';
    },
    fromTimetamp() {
      if (this.period === '1w') {
        return this.toTimetamp - 604800000;
      } else if (this.period === '1month') {
        return this.toTimetamp - 2592000000;
      } else if (this.period === 'ytd') {
        return this.toTimetamp - 31536000000;
      } else if (this.period === 'all') {
        return this.overallStartTimetamp;
      }
    },
    dates() {
      const dates = [];
      let timestamp = this.fromTimetamp;
      do {
        dates.push(timestamp);
        timestamp += 86400000;
      } while (timestamp <= this.toTimetamp);
      return dates;
    },
    meedsPriceData() {
      if (!this.pairHistoryData || (this.needsExchangeRate && !this.currencyExchangeRate)) {
        return [];
      }
      // Build Meeds price data items
      return this.dates.map(date => {
        const dateString = this.dateFormat(date);
        const meedsPriceItem = this.pairHistoryData[dateString];
        if (!meedsPriceItem) {
          return null;
        }
        meedsPriceItem.date = date;
        if (this.needsExchangeRate) {
          const rate = this.currencyExchangeRate[dateString] || Object.values(this.currencyExchangeRate)[0];
          if (rate) {
            const usdPrice = new BigNumber(meedsPriceItem.meedsPrice).multipliedBy(meedsPriceItem.ethPrice);
            meedsPriceItem.price = usdPrice.multipliedBy(rate).toNumber();
          }
        } else if (this.selectedFiatCurrency === 'usd') {
          meedsPriceItem.price = new BigNumber(meedsPriceItem.meedsPrice).multipliedBy(meedsPriceItem.ethPrice).toNumber();
        } else if (this.selectedFiatCurrency === 'eth') {
          meedsPriceItem.price = new BigNumber(meedsPriceItem.meedsPrice).toNumber();
        }
        return meedsPriceItem;
      }).filter(item => !!item);
    },
    chartData() {
      return this.meedsPriceData.map(item => [item.date, item.price]);
    },
    chartTitle() {
      if (this.chartData && this.chartData.length) {
        const meedsPrice = this.chartData[this.chartData.length - 1][1];
        if (meedsPrice) {
          const meedsPriceLabel = this.currencyFormat(meedsPrice);
          return `${this.$t('meedsPrice')}: ${meedsPriceLabel}`;
        }
      }
      return this.$t('meedsPrice');
    },
    chartLeftMargin() {
      if (this.chartData && this.chartData.length) {
        const meedsMaxPrice = Math.max(...this.chartData.map(values => values[1]));
        if (meedsMaxPrice) {
          const meedsPriceLabel = this.currencyFormat(meedsMaxPrice);
          return meedsPriceLabel.length * 8;
        }
      }
      return 0;
    },
    chartOptions() {
      return {
        title: {
          text: this.chartTitle,
          textAlign: 'left',
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            label: {backgroundColor: '#6a7985'}
          },
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
    selectedFiatCurrency() {
      if (this.chart && this.chartOptions && this.meedsPriceData.length) {
        this.chart.setOption(this.chartOptions);
      }
    },
    currencyExchangeRate() {
      if (this.chart && this.chartOptions && this.meedsPriceData.length) {
        this.chart.setOption(this.chartOptions);
      }
    },
    chart() {
      if (this.chart && this.chartOptions && this.meedsPriceData.length) {
        this.chart.setOption(this.chartOptions);
      }
    },
    chartOptions() {
      if (this.chart && this.chartOptions && this.meedsPriceData.length) {
        this.chart.setOption(this.chartOptions);
      }
    },
  },
  mounted() {
    this.initChart();
  },
  methods: {
    initChart() {
      this.chart = echarts.init(this.$refs.priceChart);
    },
    currencyFormat(price) {
      const value = price && price.value || price;
      if (this.selectedFiatCurrency === 'eth') {
        return `${this.$ethUtils.toFixed(value, 8)} ${this.$t(`fiat.currency.${this.selectedFiatCurrency}`)}`;
      } else {
        const price = this.$ethUtils.toFixed(value, 2);
        return this.$ethUtils.toCurrencyDisplay(price, this.selectedFiatCurrency, this.language);
      }
    },
    dateFormat(timestamp) {
      const value = parseInt(timestamp && timestamp.value || timestamp);
      return new Date(value).toISOString().substring(0, 10);
    },
    dateFormatI18N(timestamp) {
      timestamp = parseInt(timestamp && timestamp.value || timestamp);
      return this.$ethUtils.formatDate(new Date(timestamp), this.language);
    },
    labelFormatter(item) {
      const date = item[0].value[0];
      const price = item[0].value[1];
      return `${this.dateFormatI18N(date)}: <strong>${this.currencyFormat(price)}</strong>`;
    },
  },
};
</script>
