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
    metrics: null,
    chart: null,
  }),
  computed: Vuex.mapState({
    sushiswapPairAddress: state => state.sushiswapPairAddress,
    xMeedAddress: state => state.xMeedAddress,
    comethswapddress: state => state.comethswapddress,
    chartOptions() {
      return {
        title: [{
          text: 'Currencies',
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
          formatter: '{b} : {c} ({d}%)'
        },
        legend: {
          display: false,
          orient: 'vertical',
          left: 120,
          top: 12,
        },
        series: [{
          type: 'pie',
          radius: ['45%', '88%'],
          center: ['65%', '50%'],
          label: {
            show: false,
          },
          data: []
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
  },
  mounted() {
    this.initChart();
  },
  created() {
    this.$deedService.getMetrics()
      .then(metrics => {
        this.metrics = metrics;
        Object.keys(this.metrics.lockedBalances).forEach((address, key) => {
          if (address.toLowerCase() === this.comethswapddress.toLowerCase()) {
            const serie = {
              name: 'SLP',
              value: Object.values(this.metrics.lockedBalances)[key],
            }; 
            this.chartOptions.series[0].data.push(serie);
          } else if (address.toLowerCase() === this.xMeedAddress.toLowerCase()) {
            const serie = {
              name: 'XMeed',
              value: Object.values(this.metrics.lockedBalances)[key],
            }; 
            this.chartOptions.series[0].data.push(serie);
          } else if (address.toLowerCase() === this.sushiswapPairAddress.toLowerCase()) {
            const serie = {
              name: 'UNI-V2',
              value: Object.values(this.metrics.lockedBalances)[key],
            }; 
            this.chartOptions.series[0].data.push(serie);
          } else {
            const serie = {
              name: 'Meed',
              value: Object.values(this.metrics.lockedBalances)[key],
            }; 
            this.chartOptions.series[0].data.push(serie);
          }
        });
      });
  },
  methods: {
    initChart() {
      this.chart = echarts.init(this.$refs.echartCurrencies);
    },
  }
};
</script>