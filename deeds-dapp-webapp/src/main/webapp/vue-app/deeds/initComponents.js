/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
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
import DeedsPointsSimulator from './components/DeedsPointsSimulator.vue';
import DeedsEarnedPoints from './components/DeedsEarnedPoints.vue';
import DeedsRedeem from './components/DeedsRedeem.vue';
import DeedsRedeemCard from './components/DeedsRedeemCard.vue';
import DeedsEmptyCity from './components/DeedsEmptyCity.vue';
import DeedsOwned from './components/DeedsOwned.vue';
import DeedsOwnedActions from './components/DeedsOwnedActions.vue';
import DeedsTimer from './components/DeedsTimer.vue';
import DeedsTrade from './components/DeedsTrade.vue';
import DeedMoveInDrawer from './components/drawers/DeedMoveInDrawer.vue';
import DeedMoveOutDrawer from './components/drawers/DeedMoveOutDrawer.vue';

const components = {
  'deeds-deeds': Deeds,
  'deeds-nft-introduction': DeedsNftIntroduction,
  'deeds-points-simulator': DeedsPointsSimulator,
  'deeds-earned-points': DeedsEarnedPoints,
  'deeds-redeem': DeedsRedeem,
  'deeds-redeem-card': DeedsRedeemCard,
  'deeds-empty-city': DeedsEmptyCity,
  'deeds-owned': DeedsOwned,
  'deeds-owned-actions': DeedsOwnedActions,
  'deeds-move-in-drawer': DeedMoveInDrawer,
  'deeds-move-out-drawer': DeedMoveOutDrawer,
  'deeds-timer': DeedsTimer,
  'deeds-trade': DeedsTrade,
};

for (const key in components) {
  Vue.component(key, components[key]);
}