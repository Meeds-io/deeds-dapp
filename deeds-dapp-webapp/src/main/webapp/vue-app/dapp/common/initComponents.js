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
import Drawer from './components/Drawer.vue';
import ConfirmDialog from './components/ConfirmDialog.vue';
import NumberFormat from './components/NumberFormat.vue';
import ContractAddress from './components/ContractAddress.vue';
import TabLink from './components/TabLink.vue';
import MetamaskButton from './components/MetamaskButton.vue';
import CardCarousel from './components/CardCarousel.vue';
import TokenAssetTemplate from './components/TokenAssetTemplate.vue';
import RentalOfferCard from './components/RentalOfferCard.vue';
import CardTypeChip from './components/CardTypeChip.vue';
import OfferTypeChip from './components/OfferTypeChip.vue';
import EmailField from './components/EmailField.vue';
import LoginButton from './components/LoginButton.vue';
import DateFormat from './components/DateFormat.vue';
import ExtendedTextarea from './components/ExtendedTextarea.vue';
import MediaSelector from './components/MediaSelector.vue';
import RentalExpirationDuration from './components/RentalExpirationDuration.vue';
import RentalDuration from './components/RentalDuration.vue';
import NoticePeriod from './components/NoticePeriod.vue';
import ButtonGroupItem from './components/ButtonGroupItem.vue';


import DeedsLeaseCardTitle from './components/lease/card/nft/CardTitle.vue';
import DeedsLeaseCardImage from './components/lease/card/nft/Image.vue';
import DeedsLeaseCardVotes from './components/lease/card/nft/CityVotes.vue';
import DeedsLeaseMaxUsers from './components/lease/card/nft/MaxUsers.vue';
import DeedsLeaseCardMintingPower from './components/lease/card/nft/MintingPower.vue';

import DeedsLeaseCardCumulativeRents from './components/lease/card/CumulativeRents.vue';
import DeedsLeaseCardStartDate from './components/lease/card/RentStartDate.vue';
import DeedsLeaseCardEndDate from './components/lease/card/RentEndDate.vue';
import DeedsLeaseCardHubStatus from './components/lease/card/HubStatus.vue';
import DeedsLeaseCardMintingDistribution from './components/lease/card/MintingDistribution.vue';
import DeedsLeaseCardDistributedRewards from './components/lease/card/DistributedRewards.vue';
import DeedsLeaseCardNoticePeriod from './components/lease/card/NoticePeriod.vue';
import DeedsLeaseCardNoticePeriodPaid from './components/lease/card/NoticePeriodPaid.vue';
import DeedsLeaseCardRemainingTime from './components/lease/card/RemainingTime.vue';
import DeedsLeaseCardRentAmount from './components/lease/card/RentAmount.vue';
import DeedsLeaseCardRentStatus from './components/lease/card/RentStatus.vue';

import DeedsLeaseCardHubAccessButton from './components/lease/button/AccessHubButton.vue';
import DeedsLeaseCardMoveButton from './components/lease/button/MoveInOutButton.vue';
import DeedsLeaseCardPayRentButton from './components/lease/button/PayRentButton.vue';
import DeedsLeaseCardClaimRewardButton from './components/lease/button/ClaimRewardButton.vue';
import DeedsLeaseCardEndRentButton from './components/lease/button/EndRentButton.vue';
import DeedsLeaseCardEvictTenantButton from './components/lease/button/EvictTenantButton.vue';
import DeedsLeaseCardSellNftButton from './components/lease/button/SellNftButton.vue';
import DeedsLeaseCardRentButtons from './components/lease/button/RentButtons.vue';

import DeedsMarketplaceOfferRentDrawer from './components/lease/drawer/AcquireOfferDrawer.vue';
import DeedManageRentOfferDrawer from './components/lease/drawer/RentOwnerFormOfferDrawer.vue';
import DeedsMarketplaceOfferPaymentDrawer from './components/lease/drawer/PaymentConfirmOfferDrawer.vue';
import DeedLeasePaymentDrawer from './components/lease/drawer/PaymentConfirmDrawer.vue';
import DeedLeaseRentPayDrawer from './components/lease/drawer/PayRentDrawer.vue';
import DeedLeaseEndRentingDrawer from './components/lease/drawer/EndRentingDrawer.vue';
import DeedMoveInDrawer from './components/lease/drawer/MoveInDrawer.vue';
import DeedMoveOutDrawer from './components/lease/drawer/MoveOutDrawer.vue';
import DeedLeaseEvictTenantDrawer from './components/lease/drawer/EvictTenantDrawer.vue';

const components = {
  'deeds-drawer': Drawer,
  'deeds-confirm-dialog': ConfirmDialog,
  'deeds-number-format': NumberFormat,
  'deeds-contract-address': ContractAddress,
  'deeds-metamask-button': MetamaskButton,
  'deeds-tab-link': TabLink,
  'deeds-card-caroussel': CardCarousel,
  'deeds-token-asset-template': TokenAssetTemplate,
  'deeds-renting-offer-card': RentalOfferCard,
  'deeds-card-type-chip': CardTypeChip,
  'deeds-offer-type-chip': OfferTypeChip,
  'deeds-email-field': EmailField,
  'deeds-login-button': LoginButton,
  'deeds-date-format': DateFormat,
  'deeds-extended-textarea': ExtendedTextarea,
  'deeds-media-selector': MediaSelector,
  'deeds-renting-expiration-duration': RentalExpirationDuration,
  'deeds-renting-duration': RentalDuration,
  'deeds-notice-period': NoticePeriod,
  'deeds-button-group-item': ButtonGroupItem,
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