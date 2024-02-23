<!--

 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 
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
  <li>
    <math class="my-1">
      <mrow>
        <msub>
          <mi>D</mi>
          <mn>r</mn>
        </msub>
        <mo stretchy="true" class="font-weight-bold">
          =
        </mo>
        <mfrac linethickness="2" class="fa-lg">
          <msub>
            <mi>H</mi>
            <mn>r</mn>
          </msub>
          <msub>
            <mi>U</mi>
            <mn>r</mn>
          </msub>
        </mfrac>
        <mo stretchy="true" class="font-weight-bold">
          =
        </mo>
        <mn>{{ report.dr }}</mn>
        <mo stretchy="true" class="font-weight-bold">
          =
        </mo>
        <ms
          class="d-flex font-weight-bold">
          <div v-sanitized-html="$t('uem.formulaDistributionRate', {0: `<span class='font-weight-bold me-1 ${distributionRateColor}--text'>${distributionRatePercentage}</span>`})"></div>
        </ms>
      </mrow>
    </math>
    <ul>
      <li>
        <math class="my-1">
          <mrow>
            <msub>
              <mi>H</mi>
              <mn>r</mn>
            </msub>
            <mo stretchy="true" class="font-weight-bold">
              =
            </mo>
            <mn>{{ hubRewardAmount }}</mn>
            <mo stretchy="true" class="font-weight-bold">
              =
            </mo>
            <ms>
              {{ $t('uem.formulaHubRewardAmount') }}
            </ms>
          </mrow>
        </math>
      </li>
      <li>
        <math class="my-1">
          <mrow>
            <msub>
              <mi>U</mi>
              <mn>r</mn>
            </msub>
            <mo stretchy="true" class="font-weight-bold">
              =
            </mo>
            <mn>{{ lastPeriodUemRewardAmount }}</mn>
            <mo stretchy="true" class="font-weight-bold">
              =
            </mo>
            <ms>
              {{ $t('uem.formulaLastPeriodUemRewardAmount') }}
            </ms>
          </mrow>
        </math>
      </li>
    </ul>
  </li>
</template>
<script>
export default {
  props: {
    report: {
      type: Object,
      default: null,
    },
  },
  computed: Vuex.mapState({
    language: state => state.language,
    distributionRate() {
      return this.report.dr;
    },
    lastPeriodUemRewardAmount() {
      return this.report.lastPeriodUemRewardAmount;
    },
    hubRewardAmount() {
      return this.report.hubRewardAmount;
    },
    distributionRateColor() {
      return this.distributionRate >= 1 && 'success' || 'error';
    },
    distributionRatePercentage() {
      return this.$utils.percentage(this.distributionRate, this.language);
    },
  }),
};
</script>