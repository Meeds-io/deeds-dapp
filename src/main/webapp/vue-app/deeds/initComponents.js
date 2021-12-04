import Deeds from './components/Deeds.vue';
import DeedsNftIntroduction from './components/DeedsNftIntroduction.vue';
import DeedsPointsSimulator from './components/DeedsPointsSimulator.vue';
import DeedsEarnedPoints from './components/DeedsEarnedPoints.vue';
import DeedsRedeem from './components/DeedsRedeem.vue';
import DeedsRedeemCard from './components/DeedsRedeemCard.vue';
import DeedsOwned from './components/DeedsOwned.vue';

const components = {
  'deeds-deeds': Deeds,
  'deeds-nft-introduction': DeedsNftIntroduction,
  'deeds-points-simulator': DeedsPointsSimulator,
  'deeds-earned-points': DeedsEarnedPoints,
  'deeds-redeem': DeedsRedeem,
  'deeds-redeem-card': DeedsRedeemCard,
  'deeds-owned': DeedsOwned,
};

for (const key in components) {
  Vue.component(key, components[key]);
}