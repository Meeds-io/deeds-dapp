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
  <v-container class="mt-2">
    <deeds-card-caroussel>
      <div
        v-for="card in cards"
        :key="card.name"
        class="mx-2">
        <deeds-redeem-card :card="card" />
      </div>
    </deeds-card-caroussel>
  </v-container>
</template>
<script>
export default {
  data: () => ({
    cards: [
      {
        'name': 'Common',
        'cardType': 0,
        'amount': new BigNumber('0x01b1ae4d6e2ef5000000')
      },
      {
        'name': 'Uncommon',
        'cardType': '1',
        'amount': new BigNumber('0x06c6b935b8bbd4000000')
      },
      {
        'name': 'Rare',
        'cardType': '2',
        'amount': new BigNumber('0x0a968163f0a57b400000')
      },
      {
        'name': 'Legendary',
        'cardType': '3',
        'amount': new BigNumber('0x152d02c7e14af6800000')
      }
    ],
  }),
};
</script>