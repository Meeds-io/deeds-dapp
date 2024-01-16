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
  <div class="d-flex flex-column mt-4 mt-sm-10">
    <v-card flat>
      <deeds-page-title-layout>
        <template #title>
          {{ $t('page.legals.title') }}
        </template>
        <template #subtitle>
          {{ $t('page.legals.subtitle') }}
        </template>
      </deeds-page-title-layout>
    </v-card>
    <div class="d-flex justify-center flex-wrap mb-7 pt-4 pt-md-11">
      <v-btn
        href="#termsOfUse"
        :class="!dark && 'dark-grey-border-color'"
        class="mx-5 dark-grey--text rounded-xl"
        outlined
        x-large>
        <h4 class="font-weight-normal">{{ $t('termsOfUse.title') }}</h4>
      </v-btn>
      <v-btn
        href="#privacyPolicy"
        :class="!dark && 'dark-grey-border-color'"
        class="mx-5 dark-grey--text rounded-xl"
        outlined
        x-large>
        <h4 class="font-weight-normal">{{ $t('privacyPolicy.title') }}</h4>
      </v-btn>
      <v-btn
        href="#cookiePolicy"
        :class="!dark && 'dark-grey-border-color'"
        class="mx-5 dark-grey--text rounded-xl"
        outlined
        x-large>
        <h4 class="font-weight-normal">{{ $t('cookiePolicy.title') }}</h4>
      </v-btn>
      <v-btn
        href="#risks"
        :class="!dark && 'dark-grey-border-color'"
        class="mx-5 dark-grey--text rounded-xl"
        outlined
        x-large>
        <h4 class="font-weight-normal">{{ $t('risks.title') }}</h4>
      </v-btn>
      <v-btn
        href="#legalNotice"
        :class="!dark && 'dark-grey-border-color'"
        class="mx-5 dark-grey--text rounded-xl"
        outlined
        x-large>
        <h4 class="font-weight-normal">{{ $t('legalNotice.title') }}</h4>
      </v-btn>
    </div>
    <v-divider class="mt-8" />
    <v-card id="termsOfUse" flat>
      <v-card-title class="px-0 pt-8 font-weight-black dark-grey--text">{{ $t('termsOfUse.title') }}</v-card-title>
      <h4 class="dark-grey--text font-weight-black">{{ $t('termsOfUse.introduction.title') }}</h4>
      <v-card-text class="px-0 pb-2 subtitle-2 font-weight-normal dark-grey--text" v-html="$t('termsOfUse.introduction.descriptionPart1', {0:`<a class='no-decoration primary--text font-weight-bold' href='https://www.meeds.io'>`, 1: `</a>`, 2:`<a class='no-decoration primary--text font-weight-bold' href='https://holders.meeds.io'>`, 3:`<a class='no-decoration primary--text font-weight-bold' href='https://investors.meeds.io'>`})" />
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.introduction.descriptionPart2') }}</v-card-text>
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.introduction.descriptionPart3') }}</v-card-text>
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.introduction.descriptionPart4') }}</v-card-text>
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.introduction.descriptionPart5') }}</v-card-text>
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.introduction.descriptionPart6') }}</v-card-text>
      <v-card-text class="px-0 pt-2 subtitle-2 font-weight-normal dark-grey--text" v-html="$t('termsOfUse.introduction.descriptionPart7', {0: `<a class='no-decoration primary--text font-weight-bold' href='mailto:contact@meeds.io'>`, 1:`</a>`})" />  
      <h4 class="dark-grey--text font-weight-black">{{ $t('termsOfUse.website.title') }}</h4>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.website.description') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('termsOfUse.access.title') }}</h4>
      <v-card-text class="px-0 pb-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.access.descriptionPart1') }}</v-card-text>
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.access.descriptionPart2') }}</v-card-text>
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.access.descriptionPart3') }}</v-card-text>
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.access.descriptionPart4') }}</v-card-text>
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.access.descriptionPart5') }}</v-card-text>
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.access.descriptionPart6') }}</v-card-text>
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.access.descriptionPart7') }}</v-card-text>
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text" v-html="$t('termsOfUse.access.descriptionPart8', {0: '<br>'})" />
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.access.descriptionPart9') }}</v-card-text>
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text" v-html="$t('termsOfUse.access.descriptionPart10', {0: '<br>'})" />
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.access.descriptionPart11') }}</v-card-text>
      <v-card-text class="px-0 pt-2 subtitle-2 font-weight-normal dark-grey--text" v-html="$t('termsOfUse.access.descriptionPart12', {0: '<br>'})" />
      <h4 class="dark-grey--text font-weight-black">{{ $t('termsOfUse.thirdPartyLinks.title') }}</h4>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.thirdPartyLinks.description') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('termsOfUse.privacy.title') }}</h4>
      <v-card-text class="px-0 pb-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.privacy.descriptionPart1') }}</v-card-text>
      <v-card-text class="px-0 pt-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.privacy.descriptionPart2') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('termsOfUse.propertyRights.title') }}</h4>
      <v-card-text class="px-0 pb-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.propertyRights.descriptionPart1') }}</v-card-text>
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text" v-html="$t('termsOfUse.propertyRights.descriptionPart2', {0:`<a class='no-decoration primary--text font-weight-bold' href='https://www.meeds.io'>`, 1: `</a>`})" />
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.propertyRights.descriptionPart3') }}</v-card-text>
      <v-card-text class="px-0 pt-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.propertyRights.descriptionPart4') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('termsOfUse.disclaimers.title') }}</h4>
      <v-card-text class="px-0 pb-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.disclaimers.descriptionPart1') }}</v-card-text>
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.disclaimers.descriptionPart2') }}</v-card-text>
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.disclaimers.descriptionPart3') }}</v-card-text>
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.disclaimers.descriptionPart4') }}</v-card-text>
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.disclaimers.descriptionPart5') }}</v-card-text>
      <v-card-text class="px-0 pt-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.disclaimers.descriptionPart6') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('termsOfUse.law.title') }}</h4>
      <v-card-text class="px-0 pb-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.law.descriptionPart1') }}</v-card-text>
      <v-card-text class="px-0 pt-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.law.descriptionPart1') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('termsOfUse.waiver.title') }}</h4>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.waiver.description') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('termsOfUse.agreement.title') }}</h4>
      <v-card-text class="px-0 pb-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.agreement.descriptionPart1') }}</v-card-text>
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.agreement.descriptionPart2') }}</v-card-text>
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.agreement.descriptionPart3') }}</v-card-text>
      <v-card-text class="px-0 py-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.agreement.descriptionPart4') }}</v-card-text>
      <v-card-text class="px-0 pt-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('termsOfUse.agreement.descriptionPart5') }}</v-card-text>
    </v-card>
    <v-divider class="mt-8" />
    <v-card id="privacyPolicy" flat>
      <v-card-title class="px-0 pt-8 font-weight-black dark-grey--text">{{ $t('privacyPolicy.title') }}</v-card-title>
      <h4 class="dark-grey--text font-weight-black">{{ $t('privacyPolicy.introduction.title') }}</h4>
      <v-card-text class="px-0 pb-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('privacyPolicy.introduction.descriptionPart1') }}</v-card-text>
      <v-card-text class="px-0 pt-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('privacyPolicy.introduction.descriptionPart2') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('privacyPolicy.purposeOfPrivacy.title') }}</h4>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('privacyPolicy.purposeOfPrivacy.description') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('privacyPolicy.purposeOfPersonalInformation.title') }}</h4>
      <v-card-text class="px-0 pb-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('privacyPolicy.purposeOfPersonalInformation.descriptionPart1') }}</v-card-text>
      <v-card-text class="px-0 pt-2 subtitle-2 font-weight-normal dark-grey--text" v-html="$t('privacyPolicy.purposeOfPersonalInformation.descriptionPart2', {0: '<br>'})" />
      <h4 class="dark-grey--text font-weight-black">{{ $t('privacyPolicy.protection.title') }}</h4>
      <v-card-text class="px-0 pb-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('privacyPolicy.protection.descriptionPart1') }}</v-card-text>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('privacyPolicy.protection.descriptionPart2') }}</v-card-text>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('privacyPolicy.protection.descriptionPart3') }}</v-card-text>
      <v-card-text class="px-0 pt-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('privacyPolicy.protection.descriptionPart4') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('privacyPolicy.consent.title') }}</h4>
      <v-card-text class="px-0 pb-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('privacyPolicy.consent.descriptionPart1') }}</v-card-text>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('privacyPolicy.consent.descriptionPart2') }}</v-card-text>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('privacyPolicy.consent.descriptionPart3') }}</v-card-text>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('privacyPolicy.consent.descriptionPart4') }}</v-card-text>
      <v-card-text class="px-0 pt-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('privacyPolicy.consent.descriptionPart5') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('privacyPolicy.access.title') }}</h4>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('privacyPolicy.access.description') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('privacyPolicy.security.title') }}</h4>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('privacyPolicy.security.description') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('privacyPolicy.retention.title') }}</h4>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('privacyPolicy.retention.description') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('privacyPolicy.userAge.title') }}</h4>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('privacyPolicy.userAge.description') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('privacyPolicy.links.title') }}</h4>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('privacyPolicy.links.description') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('privacyPolicy.changes.title') }}</h4>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('privacyPolicy.changes.description') }}</v-card-text>
    </v-card>
    <v-divider class="mt-8" />
    <v-card id="cookiePolicy" flat>
      <v-card-title class="px-0 pt-8 font-weight-black dark-grey--text">{{ $t('cookiePolicy.title') }}</v-card-title>
      <h4 class="dark-grey--text font-weight-black">{{ $t('cookiePolicy.introduction.title') }}</h4>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('cookiePolicy.introduction.description') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('cookiePolicy.definition.title') }}</h4>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('cookiePolicy.definition.description') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('cookiePolicy.usage.title') }}</h4>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('cookiePolicy.usage.description') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('cookiePolicy.types.title') }}</h4>
      <v-card-text class="px-0 pb-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('cookiePolicy.types.description') }}</v-card-text>
      <h4 class="px-0 font-weight-normal dark-grey--text">{{ $t('cookiePolicy.types.necessary.title') }}</h4>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('cookiePolicy.types.necessary.description') }}</v-card-text>
      <h4 class="px-0 font-weight-normal dark-grey--text">{{ $t('cookiePolicy.types.analytics.title') }}</h4>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('cookiePolicy.types.analytics.description') }}</v-card-text>
      <h4 class="px-0 font-weight-normal dark-grey--text">{{ $t('cookiePolicy.types.functionality.title') }}</h4>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('cookiePolicy.types.functionality.description') }}</v-card-text>
      <h4 class="px-0 font-weight-normal dark-grey--text">{{ $t('cookiePolicy.types.advertising.title') }}</h4>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('cookiePolicy.types.advertising.description') }}</v-card-text>
      <h4 class="px-0 font-weight-normal dark-grey--text">{{ $t('cookiePolicy.types.socialMedia.title') }}</h4>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('cookiePolicy.types.socialMedia.descriptionPart1') }}</v-card-text>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('cookiePolicy.types.socialMedia.descriptionPart2') }}</v-card-text>
      <h4 class="px-0 font-weight-normal dark-grey--text">{{ $t('cookiePolicy.types.firstAndThird.title') }}</h4>
      <v-card-text class="px-0 pt-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('cookiePolicy.types.firstAndThird.description') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('cookiePolicy.manage.title') }}</h4>
      <v-card-text class="px-0 pb-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('cookiePolicy.manage.descriptionPart1') }}</v-card-text>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('cookiePolicy.manage.descriptionPart2') }}</v-card-text>
      <v-card-text class="px-0 pt-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('cookiePolicy.manage.descriptionPart3') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('cookiePolicy.changes.title') }}</h4>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('cookiePolicy.changes.description') }}</v-card-text>
    </v-card>
    <v-divider class="mt-8" />
    <v-card id="risks" flat>
      <v-card-title class="px-0 pt-8 font-weight-black dark-grey--text">{{ $t('risks.title') }}</v-card-title>
      <h4 class="dark-grey--text font-weight-black">{{ $t('risks.investingInDefi.title') }}</h4>
      <v-card-text class="px-0 pb-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('risks.investingInDefi.descriptionPart1') }}</v-card-text>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('risks.investingInDefi.descriptionPart2') }}</v-card-text>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('risks.investingInDefi.descriptionPart3') }}</v-card-text>
      <v-card-text class="px-0 pt-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('risks.investingInDefi.descriptionPart4') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('risks.SCVulnerability.title') }}</h4>
      <v-card-text class="px-0 pb-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('risks.SCVulnerability.descriptionPart1') }}</v-card-text>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('risks.SCVulnerability.descriptionPart2') }}</v-card-text>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('risks.SCVulnerability.descriptionPart3') }}</v-card-text>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('risks.SCVulnerability.descriptionPart4') }}</v-card-text>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('risks.SCVulnerability.descriptionPart5') }}</v-card-text>
      <v-card-text class="px-0 pt-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('risks.SCVulnerability.descriptionPart6') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('risks.marketVolatility.title') }}</h4>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('risks.marketVolatility.description') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('risks.userError.title') }}</h4>
      <v-card-text class="px-0 subtitle-2 font-weight-normal dark-grey--text">{{ $t('risks.userError.description') }}</v-card-text>
      <h4 class="dark-grey--text font-weight-black">{{ $t('risks.FX.title') }}</h4>
      <v-card-text class="px-0 pb-2 subtitle-2 font-weight-normal dark-grey--text">{{ $t('risks.FX.description') }}</v-card-text>
    </v-card>
    <v-divider class="mt-8" />
    <v-card id="legalNotice" flat>
      <v-card-title class="px-0 pt-8 font-weight-black dark-grey--text">{{ $t('legalNotice.title') }}</v-card-title>
      <v-card-text class="px-0 pb-2 subtitle-2 font-weight-normal dark-grey--text" v-html="$t('legalNotice.descriptionPart1', {0:`<a class='no-decoration primary--text font-weight-bold' href='https://www.meeds.io'>`, 1: `</a>`})" />
      <v-card-text class="px-0 pt-2 subtitle-2 font-weight-normal dark-grey--text" v-html="$t('legalNotice.descriptionPart2', {0: '<br>', 1: `<a class='no-decoration primary--text font-weight-bold' href='mailto:info@meeds.io'>`, 2:`</a>`})" />
    </v-card>
  </div>
</template>
<script>
export default {
  computed: Vuex.mapState({
    parentLocation: state => state.parentLocation,
    dark: state => state.dark,
  }),
};
</script>