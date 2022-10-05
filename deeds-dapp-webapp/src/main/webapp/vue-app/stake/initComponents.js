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
import Stake from './components/Stake.vue';
import StakeDeeds from './components/StakeDeeds.vue';
import StakeGovernance from './components/StakeGovernance.vue';
import StakeYield from './components/StakeYield.vue';
import StakeContribute from './components/StakeContribute.vue';
import StakeMeeds from './components/StakeMeeds.vue';
import StakeMeedsDrawer from './components/drawer/StakeMeedsDrawer.vue';
import StakeMeedsSteps from './components/drawer/StakeMeedsSteps.vue';
import StakeMeedsStepUnstake from './components/drawer/StakeMeedsStepUnstake.vue';

const components = {
  'deeds-stake': Stake,
  'deeds-stake-deeds': StakeDeeds,
  'deeds-stake-governance': StakeGovernance,
  'deeds-stake-yield': StakeYield,
  'deeds-stake-contribute': StakeContribute,
  'deeds-stake-meeds': StakeMeeds,
  'deeds-stake-meeds-drawer': StakeMeedsDrawer,
  'deeds-stake-meeds-steps': StakeMeedsSteps,
  'deeds-stake-meeds-step-unstake': StakeMeedsStepUnstake,
};

for (const key in components) {
  Vue.component(key, components[key]);
}