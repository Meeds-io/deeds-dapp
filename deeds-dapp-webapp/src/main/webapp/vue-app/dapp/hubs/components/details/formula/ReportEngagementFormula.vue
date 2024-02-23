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
        <mi>E</mi>
        <mo stretchy="true" class="font-weight-bold">
          =
        </mo>
        <mfrac linethickness="2" class="fa-lg">
          <msub>
            <mi>E</mi>
            <mn>d</mn>
          </msub>
          <msub>
            <mi>E</mi>
            <mn>w</mn>
          </msub>
        </mfrac>
        <mo stretchy="true" class="font-weight-bold">
          =
        </mo>
        <mn>{{ engagementScore }}</mn>
        <mo stretchy="true" class="font-weight-bold">
          =
        </mo>
        <ms class="d-flex font-weight-bold">
          <div
            v-sanitized-html="$t('uem.formulaEngagementAverage', {
              0: `<span class='font-weight-bold me-1 ${engagementScoreColor}--text'>${engagementScorePercentage}</span>`
            })"></div>
        </ms>
      </mrow>
    </math>
    <ul>
      <li>
        <math class="my-1">
          <mrow>
            <msub>
              <mi>E</mi>
              <mn>d</mn>
            </msub>
            <mo stretchy="true" class="font-weight-bold">
              =
            </mo>
            <mn>{{ report.ed }}</mn>
            <mo stretchy="true" class="font-weight-bold">
              =
            </mo>
            <mfrac linethickness="2">
              <mi>A</mi>
              <mi>U</mi>
            </mfrac>
            <mo stretchy="true" class="font-weight-bold">
              =
            </mo>
            <ms>
              {{ $t('uem.formulaEngagementOfDeed') }}
            </ms>
          </mrow>
        </math>
        <ul>
          <li>
            <math class="my-1">
              <mrow>
                <mi>A</mi>
                <mo stretchy="true" class="font-weight-bold">
                  =
                </mo>
                <mn>{{ report.achievementsCount }}</mn>
                <mo stretchy="true" class="font-weight-bold">
                  =
                </mo>
                <ms>
                  {{ $t('uem.formulaActivity') }}
                </ms>
              </mrow>
            </math>
          </li>
          <li>
            <math class="my-1">
              <mrow>
                <mi>U</mi>
                <mo stretchy="true" class="font-weight-bold">
                  =
                </mo>
                <mn>{{ report.participantsCount }}</mn>
                <mo stretchy="true" class="font-weight-bold">
                  =
                </mo>
                <ms>
                  {{ $t('uem.formulaEngagedUsers') }}
                </ms>
              </mrow>
            </math>
          </li>
        </ul>
      </li>
      <li>
        <math class="my-1">
          <msub>
            <mi>E</mi>
            <mn>w</mn>
          </msub>
          <mo stretchy="true" class="font-weight-bold">
            =
          </mo>
          <mfrac linethickness="2" class="fa-lg">
            <mrow>
              <mo>∑</mo>
              <msub>
                <mi>E</mi>
                <mn>d</mn>
              </msub>
            </mrow>
            <mi>D</mi>
          </mfrac>
          <mo stretchy="true" class="font-weight-bold">
            =
          </mo>
          <mn>{{ reward.ew }}</mn>
          <mo stretchy="true" class="font-weight-bold">
            =
          </mo>
          <ms>
            {{ $t('uem.formulaEngagementOfAllDeed') }}
          </ms>
        </math>
        <ul>
          <li>
            <math class="my-1">
              <mrow>
                <mrow>
                  <mo>∑</mo>
                  <msub>
                    <mi>E</mi>
                    <mn>d</mn>
                  </msub>
                </mrow>
                <mo stretchy="true" class="font-weight-bold">
                  =
                </mo>
                <mn>{{ reward.sumEd }}</mn>
                <mo stretchy="true" class="font-weight-bold">
                  =
                </mo>
                <ms>
                  {{ $t('uem.formulaGlobalEngagementRate') }}
                </ms>
              </mrow>
            </math>
          </li>
          <li>
            <math class="my-1">
              <mrow>
                <mi>D</mi>
                <mo stretchy="true" class="font-weight-bold">
                  =
                </mo>
                <mn>{{ reward.reportsCount }}</mn>
                <mo stretchy="true" class="font-weight-bold">
                  =
                </mo>
                <ms>
                  {{ $t('uem.formulaDeedsCount') }}
                </ms>
              </mrow>
            </math>
          </li>
        </ul>
      </li>
    </ul>
  </li>
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
    engagementScore() {
      return this.report.ed / this.reward.ew;
    },
    engagementScoreColor() {
      return this.engagementScore >= 1 && 'success' || 'error';
    },
    engagementScorePercentage() {
      return this.$utils.percentage(this.engagementScore - 1, this.language, true);
    },
  }),
};
</script>