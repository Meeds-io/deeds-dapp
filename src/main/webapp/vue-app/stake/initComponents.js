import Stake from './components/Stake.vue';

const components = {
  'deeds-stake': Stake,
};

for (const key in components) {
  Vue.component(key, components[key]);
}