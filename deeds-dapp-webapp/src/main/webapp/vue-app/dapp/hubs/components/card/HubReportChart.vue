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
  <v-card
    v-if="reports.length > 1"
    class="d-flex flex-column rounded-lg reportChartParent"
    height="270"
    max-height="270"
    outlined>
    <div class="absolute-horizontal-center z-index-two mt-4">
      {{ $t('uem.hubEngagementScore') }}
    </div>
    <v-card
      id="reportChart"
      ref="reportChart"
      class="rounded-lg"
      height="100%"
      width="100%"
      flat />
  </v-card>
</template>
<script>
export default {
  props: {
    hub: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    chart: null,
    reports: [],
    htmlMounted: false,
  }),
  computed: Vuex.mapState({
    language: state => state.language,
    echartsLoaded: state => state.echartsLoaded,
    chartData() {
      return this.reports.reverse().map(r => [
        this.getStartOfTheWeek(r.sentDate),
        r.engagementScore,
      ]);
    },
    chartOptions() {
      return {
        tooltip: {
          trigger: 'axis',
          show: true,
          formatter: this.labelFormatter,
        },
        grid: {
          left: '10%',
          right: '12%',
        },
        xAxis: {
          type: 'time',
          show: false,
          axisPointer: {
            label: {
              formatter: this.dateFormatI18N,
            }
          },
        },
        yAxis: {
          type: 'value',
          minInterval: 20,
          interval: 10,
          splitLine: {
            show: false
          },
          axisLabel: {
            formatter: this.formatNumber,
          },
          axisPointer: {
            label: {
              formatter: this.formatNumber,
            }
          },
        },
        series: [{
          type: 'line',
          data: this.chartData,
          smooth: true,
          markLine: {
            data: [
              {
                name: this.$t('uem.averageEngagementScore'),
                yAxis: 10,
                label: {
                  formatter: '{b}',
                  position: 'end',
                  distance: 5,
                  rotate: 45,
                },
              }
            ],
            label: {
              distance: [20, 8]
            }
          }
        }]
      };
    },
  }),
  watch: {
    htmlMounted: {
      immediate: true,
      handler: function() {
        this.$nextTick().then(() => this.initChart());
      },
    },
    echartsLoaded: {
      immediate: true,
      handler: function() {
        this.$nextTick().then(() => this.initChart());
      },
    },
    reports: {
      immediate: true,
      handler: function() {
        this.$nextTick().then(() => this.initChart());
      },
    },
  },
  created() {
    this.getReports();
    if (!document.querySelector('#echarts-script')) {
      const script = document.createElement('script');
      script.id = 'echarts-script';
      script.src = `${this.language === 'fr' ? '../' : './'}static/js/echarts.min.js?_=5.4.0`;
      script.type = 'text/javascript';
      script.onload = () => {
        this.$store.commit('echartsLoaded');
      };
      document.getElementsByTagName('head')[0].appendChild(script);
    }
  },
  mounted() {
    this.htmlMounted = true;
  },
  methods: {
    initChart() {
      if (this.htmlMounted && this.echartsLoaded && this.reports.length > 1) {
        this.chart = echarts.init(this.$refs.reportChart.$el);
        if (this.chartOptions) {
          this.chart.setOption(this.chartOptions);
        }
      }
    },
    getReports() {
      return this.$hubReportService.getReports({
        hubAddress: this.hub?.address,
        page: 0,
        size: 100,
      })
        .then(data => this.reports = data._embedded.reports || [])
        .finally(() => this.loading = false);
    },
    dateFormatI18N(value) {
      return this.$ethUtils.formatDate(new Date(value?.value || value), this.language);
    },
    labelFormatter(item) {
      const date = item[0].value[0];
      const value = item[0].value[1];
      return `${this.dateFormatI18N(date)}: <strong>${this.formatNumber(value)}</strong>`;
    },
    formatNumber(num) {
      const useKilo = num >= 1000;
      const value = useKilo ? num / 1000 : num;
      const formatted = new Intl.NumberFormat(this.language, {
        style: 'decimal',
        minimumFractionDigits: 0,
        maximumFractionDigits: 2,
      }).format(value);
      return useKilo ? this.$t('kilo', {0: formatted}) : formatted;
    },
    getStartOfTheWeek(value) {
      const date = new Date(value);
      const day = date.getDay();
      const diff = date.getDate() - day + (day === 0 ? -6 : 1);
      return new Date(date.setDate(diff)).getTime();
    },
  },
};
</script>
