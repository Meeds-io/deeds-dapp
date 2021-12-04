import Stake from './components/Stake.vue';
import StakeDeeds from './components/StakeDeeds.vue';
import StakeGovernance from './components/StakeGovernance.vue';
import StakeYield from './components/StakeYield.vue';
import StakeMeeds from './components/StakeMeeds.vue';
import Drawer from './components/drawer/Drawer.vue';
import StakeMeedsDrawer from './components/drawer/StakeMeedsDrawer.vue';
import StakeMeedsSteps from './components/drawer/StakeMeedsSteps.vue';
import StakeMeedsStepUnstake from './components/drawer/StakeMeedsStepUnstake.vue';

const components = {
  'deeds-stake': Stake,
  'deeds-stake-deeds': StakeDeeds,
  'deeds-stake-governance': StakeGovernance,
  'deeds-stake-yield': StakeYield,
  'deeds-stake-meeds': StakeMeeds,
  'deeds-drawer': Drawer,
  'deeds-stake-meeds-drawer': StakeMeedsDrawer,
  'deeds-stake-meeds-steps': StakeMeedsSteps,
  'deeds-stake-meeds-step-unstake': StakeMeedsStepUnstake,
};

for (const key in components) {
  Vue.component(key, components[key]);
}