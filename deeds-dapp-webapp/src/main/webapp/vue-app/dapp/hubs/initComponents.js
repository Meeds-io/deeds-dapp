/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import DeedsHubs from './components/Hubs.vue';

import HubsIntroduction from './components/list/HubsIntroduction.vue';

import HubCard from './components/card/HubCard.vue';
import UpcominHubCard from './components/card/UpcominHubCard.vue';

import HubDetails from './components/details/HubDetails.vue';

import HubDeedCard from './components/details/header/HubDeedCard.vue';
import HubDeedCardTopbar from './components/details/header/HubDeedCardTopbar.vue';

import HubRewards from './components/details/rewards/HubRewards.vue';
import HubRewardItem from './components/details/rewards/HubRewardItem.vue';
import HubRewardStatus from './components/details/rewards/HubRewardStatus.vue';

import Address from './components/common/Address.vue';
import AddressIcon from './components/common/AddressIcon.vue';
import BlockchainChip from './components/common/BlockchainChip.vue';

const components = {
  'deeds-hubs': DeedsHubs,
  'deeds-hubs-introduction': HubsIntroduction,

  'deeds-hub-card': HubCard,
  'deeds-hub-upcoming-card': UpcominHubCard,

  'deeds-hub-details': HubDetails,
  'deeds-hub-details-deed-card': HubDeedCard,
  'deeds-hub-details-deed-card-topbar': HubDeedCardTopbar,
  'deeds-hub-details-rewards': HubRewards,
  'deeds-hub-details-reward-item': HubRewardItem,
  'deeds-hub-details-reward-status': HubRewardStatus,

  'deeds-hub-address': Address,
  'deeds-hub-address-icon': AddressIcon,
  'deeds-hub-blockchain-chip': BlockchainChip,
};

for (const key in components) {
  Vue.component(key, components[key]);
}