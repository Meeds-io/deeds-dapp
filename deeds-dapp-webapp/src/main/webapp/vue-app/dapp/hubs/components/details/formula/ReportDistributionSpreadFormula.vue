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
          <mn>s</mn>
        </msub>
        <mo stretchy="true" class="font-weight-bold">
          =
        </mo>
        <mfrac linethickness="2" class="fa-lg">
          <msub>
            <mi>H</mi>
            <mn>rv</mn>
          </msub>
          <msub>
            <mi>H</mi>
            <mn>us</mn>
          </msub>
        </mfrac>
        <mo stretchy="true" class="font-weight-bold">
          =
        </mo>
        <mn>{{ distributionSpread }}</mn>
        <mo stretchy="true" class="font-weight-bold">
          =
        </mo>
        <ms
          v-sanitized-html="$t('uem.formulaDistributionSpread', {0: `<span class='font-weight-bold me-1 ${distributionSpreadColor}--text'>${distributionSpreadPercentage}</span>`})"
          class="d-flex font-weight-bold">
        </ms>
      </mrow>
    </math>
    <ul>
      <li>
        <math class="my-1">
          <mrow>
            <msub>
              <mi>H</mi>
              <mn>rv</mn>
            </msub>
            <mo stretchy="true" class="font-weight-bold">
              =
            </mo>
            <mn v-if="deedMaxUsersReached">{{ recipientsCount }} ({{ $t('uem.deedMaxUsersReached', {0: report.recipientsCount, 1: report.maxUsers}) }})</mn>
            <mn v-else>{{ recipientsCount }}</mn>
            <mo stretchy="true" class="font-weight-bold">
              =
            </mo>
            <ms>
              {{ $t('uem.formulaHubRecipientsCount') }}
            </ms>
          </mrow>
        </math>
      </li>
      <li>
        <math class="my-1">
          <mrow>
            <msub>
              <mi>H</mi>
              <mn>us</mn>
            </msub>
            <mo stretchy="true" class="font-weight-bold">
              =
            </mo>
            <mn>{{ usersCount }}</mn>
            <mo stretchy="true" class="font-weight-bold">
              =
            </mo>
            <ms>
              {{ $t('uem.formulaHubUsersCount') }}
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
    hub: {
      type: Object,
      default: null,
    },
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
    distributionSpread() {
      return this.report.ds;
    },
    deedMaxUsersReached() {
      return this.report.recipientsCount > this.report.maxUsers;
    },
    recipientsCount() {
      return Math.min(this.report.recipientsCount, this.report.maxUsers);
    },
    usersCount() {
      return this.report.usersCount;
    },
    distributionSpreadColor() {
      if (this.distributionSpread > 0.75) {
        return 'success';
      } else if (this.distributionSpread > 0.5) {
        return 'info';
      } else if (this.distributionSpread > 0.25) {
        return 'orange';
      } else {
        return 'error';
      }
    },
    distributionSpreadPercentage() {
      return this.$utils.percentage(this.distributionSpread, this.language);
    },
  }),
};
</script>