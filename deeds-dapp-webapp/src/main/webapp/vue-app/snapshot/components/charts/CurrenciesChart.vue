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
    <div id="echartCurrencies" style="width:320px; height:220px; "></div>
</template>
<script>
export default {
  data: () => ({
    chartOptions: {
      title: [{
        text: 'Currencies',
        textStyle: {
          fontStyle: 'normal',
          color: '#4d5466',
          fontWeight: 'normal',
          fontSize: '16',
        },
        textAlign: 'center'
      }],
      tooltip: { 
        trigger: 'item',
        formatter: '{b} : {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 5,
        top: 12,
      },
      series: [{
        type: 'pie',
        data: [
          {value: 50, name: 'Meed'},
          {value: 20, name: 'UNI-V2'},
          {value: 10, name: 'SLP'},
          {value: 20, name: 'xMeed'},
        ],
      }]
    },
    chart: null,
  }),
  mounted() {
    this.initChart();
  },
  methods: {
    initChart() {
      const echartCurrencies = document.getElementById('echartCurrencies');
      this.chart = echarts.init(echartCurrencies);
      //this.chart = echarts.init(this.$refs.echartCurrencies);
      this.chart.setOption(this.chartOptions);
    },
  }
  
};
</script>