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
  <div class="d-flex flex-column">
    <div class="d-flex flex-column fa-lg">
      <math display="block" class="my-1">
        <mrow>
          <msub>
            <mi>{{ $t('uem.hub') }}</mi>
            <mn>{{ $t('uem.hubReward') }}</mn>
          </msub>
          <mo stretchy="true" class="font-weight-bold">
            =
          </mo>
          <mfrac linethickness="2" class="fa-lg">
            <mi>{{ $t('uem.engagementScore') }}</mi>
            <mrow>
              <mo>{{ $t('uem.allEngagementScores') }}</mo>
            </mrow>
          </mfrac>
          <mo stretchy="true" class="font-weight-bold mx-2">*</mo>
          <mi>{{ $t('uem.rewardAmount') }}</mi>
        </mrow>
      </math>
      <math display="block" class="my-1">
        <mrow>
          <mo stretchy="true" class="font-weight-bold">
            =
          </mo>
        </mrow>
      </math>
      <math display="block" class="my-1">
        <mrow>
          <mfrac linethickness="2">
            <mn>{{ report.fixedRewardIndex }}</mn>
            <mn>{{ reward.fixedGlobalIndex }}</mn>
          </mfrac>
          <mo stretchy="true" class="font-weight-bold mx-2">*</mo>
          <mn>{{ reward.amount }}</mn>
          <mn class="secondary--text ms-1">Ɱ</mn>
        </mrow>
      </math>
      <math display="block" class="my-1">
        <mrow>
          <mo stretchy="true" class="font-weight-bold">
            =
          </mo>
        </mrow>
      </math>
      <math display="block" class="my-1">
        <mrow>
          <mn class="info--text">{{ uemRewardPercentage }}</mn>
          <mo stretchy="true" class="font-weight-bold mx-2">*</mo>
          <mn>{{ reward.amount }}</mn>
          <mn class="secondary--text ms-1">Ɱ</mn>
        </mrow>
      </math>
      <math display="block" class="my-1">
        <mrow>
          <mo stretchy="true" class="font-weight-bold">
            =
          </mo>
        </mrow>
      </math>
    </div>
    <div class="d-flex">
      <v-chip
        class="mt-4 mx-auto"
        color="secondary"
        large
        dark>
        {{ $utils.numberFormatWithDigits(report.uemRewardAmount, language, 0, 2) }}<span class="ms-2">Ɱ</span>
      </v-chip>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    reward: {
      type: Object,
      default: null,
    },
    report: {
      type: Object,
      default: null,
    },
  },
  computed: Vuex.mapState({
    language: state => state.language,
    uemRewardScore() {
      return this.reward.amount && (this.report.uemRewardAmount / this.reward.amount) || 0;
    },
    uemRewardPercentage() {
      return this.$utils.percentage(this.uemRewardScore, this.language);
    },
  }),
};
</script>