<!--
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 
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
  <v-card class="d-flex flex-column justify-center align-center" flat>
    <div class="py-0 mb-4 headline font-weight-medium">{{ $t('dapp.marketplace.introductionTitle') }}</div>
    <v-card-subtitle class="py-0 subtitle-1">{{ $t('dapp.marketplace.introductionSummary1') }}</v-card-subtitle>
    <v-card-subtitle class="py-0 subtitle-1">{{ $t('dapp.marketplace.introductionSummary2') }}</v-card-subtitle>
    <v-card-text class="d-flex justify-center pa-0 my-8">
      <deeds-marketplace-introduction-item
        image-src="onboard.jpg"
        title="dapp.marketplace.onboard"
        subtitle="dapp.marketplace.onboardDescription" />
      <deeds-marketplace-introduction-item
        image-src="organize.png"
        title="dapp.marketplace.organize"
        subtitle="dapp.marketplace.organizeDescription" />
      <deeds-marketplace-introduction-item
        image-src="reward.png"
        title="dapp.marketplace.reward"
        subtitle="dapp.marketplace.rewardDescription" />
    </v-card-text>
  </v-card>
</template>
