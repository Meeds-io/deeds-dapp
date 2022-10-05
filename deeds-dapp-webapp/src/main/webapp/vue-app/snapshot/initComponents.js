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
import Overview from './components/Overview.vue';
import PriceChart from './components/charts/PriceChart.vue';
import TradeMeeds from './components/TradeMeeds.vue';
import Assets from './components/Assets.vue';
import TokenAssets from './components/assets/TokenAssets.vue';
import EmptyTokenAssets from './components/assets/EmptyTokenAssets.vue';
import TokenAssetTemplate from './components/assets/TokenAssetTemplate.vue';
import LiquidityPoolAsset from './components/assets/LiquidityPoolAsset.vue';
import XMeedAsset from './components/assets/XMeedAsset.vue';
import MeedAsset from './components/assets/MeedAsset.vue';
import DeedAssets from './components/assets/DeedAssets.vue';
import DeedAsset from './components/assets/DeedAsset.vue';
import MeedsInfo from './components/MeedsInfo.vue';
import CurrenciesChart from './components/charts/CurrenciesChart.vue';

const components = {
  'deeds-overview': Overview,
  'deeds-price-chart': PriceChart,
  'deeds-trade-meeds': TradeMeeds,
  'deeds-assets': Assets,
  'deeds-token-assets': TokenAssets,
  'deeds-empty-token-assets': EmptyTokenAssets,
  'deeds-token-asset-template': TokenAssetTemplate,
  'deeds-liquidity-pool-asset': LiquidityPoolAsset,
  'deeds-x-meed-asset': XMeedAsset,
  'deeds-meed-asset': MeedAsset,
  'deeds-deed-assets': DeedAssets,
  'deeds-deed-asset': DeedAsset,
  'deeds-meeds-info': MeedsInfo,
  'deeds-currencies-chart': CurrenciesChart,
};

for (const key in components) {
  Vue.component(key, components[key]);
}