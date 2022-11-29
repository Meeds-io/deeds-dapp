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
import DeedsLeaseCardTitle from './components/card/nft/CardTitle.vue';
import DeedsLeaseCardImage from './components/card/nft/Image.vue';
import DeedsLeaseCardVotes from './components/card/nft/CityVotes.vue';
import DeedsLeaseMaxUsers from './components/card/nft/MaxUsers.vue';
import DeedsLeaseCardMintingPower from './components/card/nft/MintingPower.vue';

import DeedsLeaseCardCumulativeRents from './components/card/CumulativeRents.vue';
import DeedsLeaseCardStartDate from './components/card/RentStartDate.vue';
import DeedsLeaseCardEndDate from './components/card/RentEndDate.vue';
import DeedsLeaseCardHubStatus from './components/card/HubStatus.vue';
import DeedsLeaseCardMintingDistribution from './components/card/MintingDistribution.vue';
import DeedsLeaseCardDistributedRewards from './components/card/DistributedRewards.vue';
import DeedsLeaseCardNoticePeriod from './components/card/NoticePeriod.vue';
import DeedsLeaseCardNoticePeriodPaid from './components/card/NoticePeriodPaid.vue';
import DeedsLeaseCardRemainingTime from './components/card/RemainingTime.vue';
import DeedsLeaseCardRentAmount from './components/card/RentAmount.vue';
import DeedsLeaseCardRentStatus from './components/card/RentStatus.vue';

import DeedsLeaseCardHubAccessButton from './components/button/AccessHubButton.vue';
import DeedsLeaseCardMoveButton from './components/button/MoveInOutButton.vue';
import DeedsLeaseCardPayRentButton from './components/button/PayRentButton.vue';
import DeedsLeaseCardClaimRewardButton from './components/button/ClaimRewardButton.vue';
import DeedsLeaseCardEndRentButton from './components/button/EndRentButton.vue';
import DeedsLeaseCardEvictTenantButton from './components/button/EvictTenantButton.vue';
import DeedsLeaseCardSellNftButton from './components/button/SellNftButton.vue';
import DeedsLeaseCardRentButtons from './components/button/RentButtons.vue';

import DeedsMarketplaceOfferRentDrawer from './components/drawer/AcquireOfferDrawer.vue';
import DeedManageRentOfferDrawer from './components/drawer/RentOwnerFormOfferDrawer.vue';
import DeedsMarketplaceOfferPaymentDrawer from './components/drawer/PaymentConfirmOfferDrawer.vue';
import DeedLeasePaymentDrawer from './components/drawer/PaymentConfirmDrawer.vue';
import DeedLeaseRentPayDrawer from './components/drawer/PayRentDrawer.vue';
import DeedLeaseEndRentingDrawer from './components/drawer/EndRentingDrawer.vue';
import DeedMoveInDrawer from './components/drawer/MoveInDrawer.vue';
import DeedMoveOutDrawer from './components/drawer/MoveOutDrawer.vue';
import DeedLeaseEvictTenantDrawer from './components/drawer/EvictTenantDrawer.vue';

const components = {
  'deeds-marketplace-offer-rent-drawer': DeedsMarketplaceOfferRentDrawer,
  'deeds-marketplace-offer-payment-drawer': DeedsMarketplaceOfferPaymentDrawer,
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
  'deeds-lease-card-rent-status': DeedsLeaseCardRentStatus,
  'deeds-move-in-drawer': DeedMoveInDrawer,
  'deeds-move-out-drawer': DeedMoveOutDrawer,
  'deeds-manage-rent-offer-drawer': DeedManageRentOfferDrawer,
  'deeds-evict-tenant-drawer': DeedLeaseEvictTenantDrawer,
  'deeds-lease-card-evict-tenant-button': DeedsLeaseCardEvictTenantButton,
  'deeds-lease-card-sell-nft-button': DeedsLeaseCardSellNftButton,
  'deeds-lease-card-rent-buttons': DeedsLeaseCardRentButtons,
};

for (const key in components) {
  Vue.component(key, components[key]);
}