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
import Farm from './components/Farm.vue';
import AddLiquidity from './components/AddLiquidity.vue';
import SellLiquidity from './components/SellLiquidity.vue';
import RentLiquidity from './components/RentLiquidity.vue';
import LiquidityPools from './components/LiquidityPools.vue';

const components = {
  'deeds-farm': Farm,
  'deeds-add-liquidity': AddLiquidity,
  'deeds-sell-liquidity': SellLiquidity,
  'deeds-rent-liquidity': RentLiquidity,
  'deeds-liquidity-pools': LiquidityPools,
};

for (const key in components) {
  Vue.component(key, components[key]);
}