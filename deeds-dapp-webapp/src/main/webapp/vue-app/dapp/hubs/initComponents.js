/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
import HubsList from './components/HubsList.vue';

import HubsIntroduction from './components/list/HubsIntroduction.vue';

import HubCard from './components/card/HubCard.vue';
import UpcominHubCard from './components/card/UpcominHubCard.vue';

import HubDetails from './components/details/HubDetails.vue';
import HubNotFound from './components/details/HubNotFound.vue';
import HubReportNotFound from './components/details/HubReportNotFound.vue';

import HubDeedCardTopbar from './components/details/header/HubDeedCardTopbar.vue';

import HubRewards from './components/details/rewards/HubRewards.vue';
import HubRewardItem from './components/details/rewards/HubRewardItem.vue';
import HubRewardItemMenu from './components/details/rewards/HubRewardItemMenu.vue';

import Address from './components/common/Address.vue';
import AddressIcon from './components/common/AddressIcon.vue';
import BlockchainChip from './components/common/BlockchainChip.vue';
import DeedChip from './components/common/DeedChip.vue';
import DeedManagerSelector from './components/common/DeedManagerSelector.vue';
import MetamaskButton from './components/common/MetamaskButton.vue';

const components = {
  'deeds-hubs': DeedsHubs,
  'deeds-hubs-list': HubsList,
  'deeds-hubs-introduction': HubsIntroduction,

  'deeds-hub-card': HubCard,
  'deeds-hub-upcoming-card': UpcominHubCard,

  'deeds-hub-details': HubDetails,
  'deeds-hub-not-found': HubNotFound,
  'deeds-hub-report-not-found': HubReportNotFound,
  'deeds-hub-details-deed-card-topbar': HubDeedCardTopbar,
  'deeds-hub-details-rewards': HubRewards,
  'deeds-hub-details-reward-item': HubRewardItem,
  'deeds-hub-details-reward-item-menu': HubRewardItemMenu,

  'deeds-hub-address': Address,
  'deeds-hub-address-icon': AddressIcon,
  'deeds-hub-blockchain-chip': BlockchainChip,
  'deeds-hub-deed-manager-selector': DeedManagerSelector,
  'deeds-hub-deed-chip': DeedChip,
  'deeds-hub-metamask-button': MetamaskButton,
};

for (const key in components) {
  Vue.component(key, components[key]);
}