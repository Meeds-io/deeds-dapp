<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2022 Meeds Association contact@meeds.io
 
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
  <div ref="echartCurrencies" id="echartCurrencies"></div>
</template>
<script>
export default {
  data: () => ({
    chart: null,
  }),
  props: {
    metrics: {
      type: Object,
      default: null,
    },
  },
  computed: Vuex.mapState({
    sushiswapPairAddress: state => state.sushiswapPairAddress,
    xMeedAddress: state => state.xMeedAddress,
    comethPairAddress: state => state.comethPairAddress,
    vestingAddress: state => state.vestingAddress,
    language: state => state.language,
    chartOptions() {
      const chartData = [];
      if (this.metrics) {
        Object.keys(this.metrics.lockedBalances).forEach((address) => {
          let name;
          const lockedBalance = this.metrics.lockedBalances[address];
          if (address.toLowerCase() === this.comethPairAddress.toLowerCase()) {
            name = this.$t('comethPool');
          } else if (address.toLowerCase() === this.xMeedAddress.toLowerCase()) {
            name = this.$t('xMeedsStaked');
          } else if (address.toLowerCase() === this.sushiswapPairAddress.toLowerCase()) {
            name = this.$t('sushiSwapPool');
          } else if (address.toLowerCase() === this.vestingAddress.toLowerCase()) {
            name = this.$t('vestedMeeds');
          } else {
            name = this.$t('others');
          }
          const serie = {
            name,
            value: lockedBalance,
          }; 
          chartData.push(serie);
        });
      }
      return {
        title: [{
          text: this.$t('currencies'),
          left: '64%',
          textStyle: {
            fontStyle: 'normal',
            color: '#4d5466',
            fontWeight: 'normal',
            fontSize: '16',
          },
          top: '44%',
          textAlign: 'center'
        }],
        tooltip: { 
          trigger: 'item',
          show: true,
          formatter: this.labelFormatter,
        },
        legend: {
          display: false,
          orient: 'vertical',
          left: 82,
          top: 12,
        },
        series: [{
          type: 'pie',
          radius: ['45%', '88%'],
          center: ['65%', '50%'],
          label: {
            show: false,
          },
          data: chartData,
        }],
        color: ['#53BF9D', '#F94C66', '#BD4291', '#FFC54D']};
    },
  }),
  watch: {
    metrics() {
      if (this.chart && this.chartOptions) {
        this.chart.setOption(this.chartOptions);
      }
    },
    language() {
      if (this.chart && this.chartOptions) {
        this.chart.setOption(this.chartOptions);
      }
    },
  },
  mounted() {
    this.initChart();
  },
  methods: {
    initChart() {
      this.chart = echarts.init(this.$refs.echartCurrencies);
    },
    labelFormatter(item) {
      const name = item.data.name;
      const value = this.$ethUtils.toFixedDisplay(item.data.value, 3, this.language);
      const percent = item.percent;
      return `<strong>${name}:</strong> ${value} (${percent}%)`;
    },
  }
};
</script>