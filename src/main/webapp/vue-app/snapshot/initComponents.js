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
import Snapshot from './components/Snapshot.vue';
import PriceChart from './components/PriceChart.vue';
import LiquidityActions from './components/LiquidityActions.vue';
import TradeMeeds from './components/TradeMeeds.vue';
import Assets from './components/Assets.vue';
import TokenAssets from './components/assets/TokenAssets.vue';
import DeedAssets from './components/assets/DeedAssets.vue';

const components = {
  'deeds-snapshot': Snapshot,
  'deeds-price-chart': PriceChart,
  'deeds-liquidity-actions': LiquidityActions,
  'deeds-trade-meeds': TradeMeeds,
  'deeds-assets': Assets,
  'deeds-token-assets': TokenAssets,
  'deeds-deed-assets': DeedAssets,
};

for (const key in components) {
  Vue.component(key, components[key]);
}