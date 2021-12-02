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