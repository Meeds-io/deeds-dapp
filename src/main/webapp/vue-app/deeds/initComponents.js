import Deeds from './components/Deeds.vue';

const components = {
  'deeds-deeds': Deeds,
};

for (const key in components) {
  Vue.component(key, components[key]);
}