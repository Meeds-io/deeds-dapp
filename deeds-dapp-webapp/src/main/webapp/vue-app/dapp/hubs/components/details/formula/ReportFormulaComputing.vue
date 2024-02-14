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
  <div class="d-flex flex-column fa-lg">
    <math display="block">
      <mrow>
        <mrow>
          <mi>S</mi>
        </mrow>
        <mo stretchy="true" class="font-weight-bold">=</mo>
        <mi>E</mi>
        <mo stretchy="true" class="font-weight-bold">*</mo>
        <msub>
          <mi>D</mi>
          <mn>r</mn>
        </msub>
        <mo stretchy="true" class="font-weight-bold">*</mo>
        <msub>
          <mi>D</mi>
          <mn>s</mn>
        </msub>
        <mo stretchy="true" class="font-weight-bold">*</mo>
        <mi>M</mi>
        <mo stretchy="true" class="font-weight-bold">=</mo>
        <mfrac linethickness="2">
          <msub>
            <mi>E</mi>
            <mn>d</mn>
          </msub>
          <msub>
            <mi>E</mi>
            <mn>w</mn>
          </msub>
        </mfrac>
        <mo stretchy="true" class="font-weight-bold">*</mo>
        <msub>
          <mi>D</mi>
          <mn>r</mn>
        </msub>
        <mo stretchy="true" class="font-weight-bold">*</mo>
        <msub>
          <mi>D</mi>
          <mn>s</mn>
        </msub>
        <mo stretchy="true" class="font-weight-bold">*</mo>
        <mi>M</mi>
      </mrow>
    </math>
    <math display="block" class="my-4">
      <mo stretchy="true" class="font-weight-bold">
        =
      </mo>
    </math>
    <math display="block">
      <mrow>
        <mfrac linethickness="2">
          <mn>{{ report.ed }}</mn>
          <mn>{{ reward.ew }}</mn>
        </mfrac>
        <mo stretchy="true" class="font-weight-bold">*</mo>
        <mn>{{ report.dr }}</mn>
        <mo stretchy="true" class="font-weight-bold">*</mo>
        <mn>{{ report.ds }}</mn>
        <mo stretchy="true" class="font-weight-bold">*</mo>
        <mn>{{ report.mp }}</mn>
      </mrow>
    </math>
    <math display="block" class="my-4">
      <mo stretchy="true" class="font-weight-bold">
        =
      </mo>
    </math>
    <div class="d-flex">
      <v-chip
        class="mt-4 mx-auto"
        color="primary"
        large
        dark>
        {{ $utils.numberFormatWithDigits(report.fixedRewardIndex, language, 2, 5) }}
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
  }),
};
</script>