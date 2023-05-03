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
  <v-card flat class="d-flex flex-column">
    <v-card-title v-if="isMobile" class="ps-0 py-0 mx-auto">
      {{ $t('distribution') }}
    </v-card-title>
    <div
      ref="echartCurrencies"
      id="echartCurrencies"
      class="d-flex align-center"></div>
  </v-card>
</template>
<script>
export default {
  props: {
    metrics: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    chart: null,
    htmlMounted: false,
  }),
  computed: Vuex.mapState({
    sushiswapPairAddress: state => state.sushiswapPairAddress,
    xMeedAddress: state => state.xMeedAddress,
    comethPairAddress: state => state.comethPairAddress,
    vestingAddress: state => state.vestingAddress,
    language: state => state.language,
    isMobile: state => state.isMobile,
    echartsLoaded: state => state.echartsLoaded,
    chartOptions() {
      const chartData = [];
      if (this.metrics) {
        Object.keys(this.metrics.lockedBalances).forEach((address) => {
          let name;
          const lockedBalance = this.metrics.lockedBalances[address];
          if (address.toLowerCase() === this.comethPairAddress?.toLowerCase()) {
            name = this.$t('comethPool');
          } else if (address.toLowerCase() === this.xMeedAddress?.toLowerCase()) {
            name = this.$t('xMeedsStaked');
          } else if (address.toLowerCase() === this.sushiswapPairAddress?.toLowerCase()) {
            name = this.$t('sushiSwapPool');
          } else if (address.toLowerCase() === this.vestingAddress?.toLowerCase()) {
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
      const chartOptions = {
        tooltip: {
          trigger: 'item',
          show: true,
          formatter: this.labelFormatter,
        },
        legend: {
          orient: 'vertical',
          textStyle: {
            color: 'gray',
          },
        },
        series: [{
          type: 'pie',
          center: ['65%', '50%'],
          label: {
            show: false,
          },
          data: chartData,
        }],
        color: ['#53BF9D', '#F94C66', '#BD4291', '#FFC54D']
      };
      if (this.isMobile) {
        chartOptions.legend.top = this.isMobile && '20px' || '0';
        chartOptions.legend.left = 'left';
        chartOptions.series[0].radius = '75%';
      } else {
        chartOptions.legend.display = false;
        chartOptions.legend.left = 82;
        chartOptions.legend.top = 12;
        chartOptions.series[0].radius = ['45%', '88%'];
        chartOptions.title = {
          text: this.$t('distribution'),
          top: '44%',
          left: '64%',
          textAlign: 'center',
          textStyle: {
            fontStyle: 'normal',
            color: 'gray',
            fontWeight: 'normal',
            fontSize: '16',
            textTransform: 'capitalize',
          },
        };
      }
      return chartOptions;
    },
  }),
  watch: {
    metrics() {
      if (!this.chart) {
        this.initChart();
      }
      if (this.chart && this.chartOptions) {
        this.chart.setOption(this.chartOptions);
      }
    },
    language() {
      if (this.chart && this.chartOptions) {
        this.chart.setOption(this.chartOptions);
      }
    },
    isMobile() {
      if (this.chart && this.chartOptions) {
        this.chart.setOption(this.chartOptions);
      }
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
  },
  methods: {
    initChart() {
      if (this.htmlMounted && this.echartsLoaded) {
        this.chart = echarts.init(this.$refs.echartCurrencies);
        if (this.chartOptions) {
          this.chart.setOption(this.chartOptions);
        }
      }
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