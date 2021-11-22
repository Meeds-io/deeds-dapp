import Snapshot from './components/Snapshot.vue';
import PriceChart from './components/PriceChart.vue';
import LiquidityActions from './components/LiquidityActions.vue';
import BuyMeeds from './components/BuyMeeds.vue';

const components = {
  'deeds-snapshot': Snapshot,
  'deeds-price-chart': PriceChart,
  'deeds-liquidity-actions': LiquidityActions,
  'deeds-buy-meeds': BuyMeeds,
};

for (const key in components) {
  Vue.component(key, components[key]);
}