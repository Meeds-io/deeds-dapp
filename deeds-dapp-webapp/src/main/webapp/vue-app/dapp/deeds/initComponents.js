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
import Deeds from './components/Deeds.vue';
import DeedsNftIntroduction from './components/DeedsNftIntroduction.vue';
import DeedsNftTitle from './components/DeedsNftTitle.vue';
import DeedsPointsSimulator from './components/DeedsPointsSimulator.vue';
import DeedsEarnedPoints from './components/DeedsEarnedPoints.vue';
import DeedsRedeem from './components/DeedsRedeem.vue';
import DeedsRedeemCard from './components/DeedsRedeemCard.vue';
import DeedsEmptyCity from './components/DeedsEmptyCity.vue';
import DeedsOwnedTable from './components/DeedsOwnedTable.vue';
import DeedsOwnedItemId from './components/deed-cells/DeedsOwnedItemId.vue';
import DeedsOwnedItemStatus from './components/deed-cells/DeedsOwnedItemStatus.vue';
import DeedsOwnedItemEarned from './components/deed-cells/DeedsOwnedItemEarned.vue';
import DeedsOwnedItemMenu from './components/deed-cells/DeedsOwnedItemMenu.vue';
import DeedsTimer from './components/DeedsTimer.vue';
import DeedsTrade from './components/DeedsTrade.vue';
import DeedMoveInDrawer from './components/drawers/DeedMoveInDrawer.vue';
import DeedMoveOutDrawer from './components/drawers/DeedMoveOutDrawer.vue';
import DeedManageRentOfferDrawer from './components/drawers/DeedManageRentOfferDrawer.vue';
import DeedManageDrawer from './components/drawers/DeedManageDrawer.vue';
import ExtendedTextarea from './components/form/ExtendedTextarea.vue';
import MediaSelector from './components/form/MediaSelector.vue';
import RentalExpirationDuration from './components/form/RentalExpirationDuration.vue';
import RentalDuration from './components/form/RentalDuration.vue';
import NoticePeriod from './components/form/NoticePeriod.vue';
import SecurityDepositPeriod from './components/form/SecurityDepositPeriod.vue';
import ButtonGroupItem from './components/form/ButtonGroupItem.vue';

const components = {
  'deeds-deeds': Deeds,
  'deeds-nft-introduction': DeedsNftIntroduction,
  'deeds-nft-title': DeedsNftTitle,
  'deeds-points-simulator': DeedsPointsSimulator,
  'deeds-earned-points': DeedsEarnedPoints,
  'deeds-redeem': DeedsRedeem,
  'deeds-redeem-card': DeedsRedeemCard,
  'deeds-empty-city': DeedsEmptyCity,
  'deeds-owned-table': DeedsOwnedTable,
  'deeds-owned-item-id': DeedsOwnedItemId,
  'deeds-owned-item-status': DeedsOwnedItemStatus,
  'deeds-owned-item-earned': DeedsOwnedItemEarned,
  'deeds-owned-item-menu': DeedsOwnedItemMenu,
  'deeds-move-in-drawer': DeedMoveInDrawer,
  'deeds-move-out-drawer': DeedMoveOutDrawer,
  'deeds-manage-rent-offer-drawer': DeedManageRentOfferDrawer,
  'deeds-manage-drawer': DeedManageDrawer,
  'deeds-timer': DeedsTimer,
  'deeds-trade': DeedsTrade,
  'deeds-extended-textarea': ExtendedTextarea,
  'deeds-media-selector': MediaSelector,
  'deeds-renting-expiration-duration': RentalExpirationDuration,
  'deeds-renting-duration': RentalDuration,
  'deeds-notice-period': NoticePeriod,
  'deeds-security-deposit-period': SecurityDepositPeriod,
  'deeds-button-group-item': ButtonGroupItem,
};

for (const key in components) {
  Vue.component(key, components[key]);
}