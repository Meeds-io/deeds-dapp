/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
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
import DeedsTenants from './components/Tenants.vue';
import DeedsTenantsIntroduction from './components/DeedsTenantsIntroduction.vue';
import DeedsTenantsList from './components/DeedsTenantsList.vue';
import DeedsTenantsLeaseCard from './components/DeedsTenantsLeaseCard.vue';
import DeedLeasePaymentDrawer from './components/drawers/DeedLeasePaymentDrawer.vue';
import DeedLeaseRentPayDrawer from './components/drawers/DeedLeaseRentPayDrawer.vue';
import DeedLeaseEndRentingDrawer from './components/drawers/DeedLeaseEndRentingDrawer.vue';

import DeedsLeaseCardCumulativeRents from './components/card/DeedsLeaseCardCumulativeRents.vue';
import DeedsLeaseCardEndDate from './components/card/DeedsLeaseCardEndDate.vue';
import DeedsLeaseCardHubStatus from './components/card/DeedsLeaseCardHubStatus.vue';
import DeedsLeaseCardImage from './components/card/DeedsLeaseCardImage.vue';
import DeedsLeaseCardMintingDistribution from './components/card/DeedsLeaseCardMintingDistribution.vue';
import DeedsLeaseCardDistributedRewards from './components/card/DeedsLeaseCardDistributedRewards.vue';
import DeedsLeaseCardMintingPower from './components/card/DeedsLeaseCardMintingPower.vue';
import DeedsLeaseCardNoticePeriod from './components/card/DeedsLeaseCardNoticePeriod.vue';
import DeedsLeaseCardNoticePeriodPaid from './components/card/DeedsLeaseCardNoticePeriodPaid.vue';
import DeedsLeaseCardRemainingTime from './components/card/DeedsLeaseCardRemainingTime.vue';
import DeedsLeaseCardRentAmount from './components/card/DeedsLeaseCardRentAmount.vue';
import DeedsLeaseCardStartDate from './components/card/DeedsLeaseCardStartDate.vue';
import DeedsLeaseCardTitle from './components/card/DeedsLeaseCardTitle.vue';
import DeedsLeaseCardVotes from './components/card/DeedsLeaseCardVotes.vue';
import DeedsLeaseMaxUsers from './components/card/DeedsLeaseMaxUsers.vue';
import DeedsLeaseCardHubAccessButton from './components/card/DeedsLeaseCardHubAccessButton.vue';
import DeedsLeaseCardMoveButton from './components/card/DeedsLeaseCardMoveButton.vue';
import DeedsLeaseCardPayRentButton from './components/card/DeedsLeaseCardPayRentButton.vue';
import DeedsLeaseCardClaimRewardButton from './components/card/DeedsLeaseCardClaimRewardButton.vue';
import DeedsLeaseCardEndRentButton from './components/card/DeedsLeaseCardEndRentButton.vue';

const components = {
  'deeds-tenants': DeedsTenants,
  'deeds-tenants-introduction': DeedsTenantsIntroduction,
  'deeds-tenants-list': DeedsTenantsList,
  'deeds-tenants-lease-card': DeedsTenantsLeaseCard,
  'deeds-tenants-lease-payment-drawer': DeedLeasePaymentDrawer,
  'deeds-tenants-lease-rent-pay-drawer': DeedLeaseRentPayDrawer,
  'deeds-tenants-lease-end-renting-drawer': DeedLeaseEndRentingDrawer,
  'deeds-lease-card-title': DeedsLeaseCardTitle,
  'deeds-lease-card-image': DeedsLeaseCardImage,
  'deeds-lease-card-start-date': DeedsLeaseCardStartDate,
  'deeds-lease-card-end-date': DeedsLeaseCardEndDate,
  'deeds-lease-card-remaining-time': DeedsLeaseCardRemainingTime,
  'deeds-lease-card-hub-status': DeedsLeaseCardHubStatus,
  'deeds-lease-card-minting-distribution': DeedsLeaseCardMintingDistribution,
  'deeds-lease-card-minting-power': DeedsLeaseCardMintingPower,
  'deeds-lease-card-notice-period': DeedsLeaseCardNoticePeriod,
  'deeds-lease-card-rent-amount': DeedsLeaseCardRentAmount,
  'deeds-lease-card-cumulative-rents': DeedsLeaseCardCumulativeRents,
  'deeds-lease-card-notice-period-paid': DeedsLeaseCardNoticePeriodPaid,
  'deeds-lease-card-votes': DeedsLeaseCardVotes,
  'deeds-lease-card-max-users': DeedsLeaseMaxUsers,
  'deeds-lease-card-distributed-rewards': DeedsLeaseCardDistributedRewards,
  'deeds-lease-card-hub-access-button': DeedsLeaseCardHubAccessButton,
  'deeds-lease-card-move-button': DeedsLeaseCardMoveButton,
  'deeds-lease-card-pay-rent-button': DeedsLeaseCardPayRentButton,
  'deeds-lease-card-claim-reward-button': DeedsLeaseCardClaimRewardButton,
  'deeds-lease-card-end-rent-button': DeedsLeaseCardEndRentButton,
};

for (const key in components) {
  Vue.component(key, components[key]);
}